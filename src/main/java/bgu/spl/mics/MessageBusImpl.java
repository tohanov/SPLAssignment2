package bgu.spl.mics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.mics.application.CRMSRunner;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.services.TimeService;
import bgu.spl.mics.example.messages.ExampleEvent;



/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private static final class InternalSingleton {
		private static final MessageBusImpl instance = new MessageBusImpl();
	}
	
	
	private HashMap<MicroService,Queue<Message>> microServicesHashMap;
	private HashMap<Class<? extends Broadcast>,LinkedList<MicroService>> broadcastHashMap; 
	private HashMap<Class<? extends Message>,LinkedList<MicroService>> eventHashMap;
	private HashMap<Event<? extends Object>,Future<? extends Object>> futureHashMap;


	private MessageBusImpl(){
		microServicesHashMap = new HashMap<>();
		
		broadcastHashMap = new HashMap<>();
		broadcastHashMap.put(PublishConferenceBroadcast.class, new LinkedList<>());
		broadcastHashMap.put(TickBroadcast.class, new LinkedList<>());
		
		eventHashMap = new HashMap<>();
		eventHashMap.put(PublishResultsEvent.class, new LinkedList<>());
		eventHashMap.put(TestModelEvent.class, new LinkedList<>());
		eventHashMap.put(TrainModelEvent.class, new LinkedList<>());
		eventHashMap.put(ExampleEvent.class, new LinkedList<>());

		futureHashMap=new HashMap<>();
	}


	public static MessageBusImpl getInstance() {
		return InternalSingleton.instance;
	}


	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		LinkedList<MicroService> microServices;

		synchronized (eventHashMap) {
			microServices = eventHashMap.get(type);
		}

		synchronized (microServices) {
			microServices.addLast(m);
		}
	}


	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		LinkedList<MicroService> ls;
		synchronized (broadcastHashMap) {
			ls = broadcastHashMap.get(type);
		}

		synchronized (ls) {
			ls.addLast(m);
		}
	}


	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> future;
		synchronized (futureHashMap) {
			future = (Future<T>)futureHashMap.remove(e);
		}

		future.resolve(result);

		// synchronized (futureHashMap) {
		// 	futureHashMap.remove(e);
		// }
	}


	private void completeAll(){ // TODO : rethink
		
		for(Future<? extends Object> future: futureHashMap.values()){
			synchronized(future){
				future.notifyAll();
			}
		}
	}


	@Override
	public void sendBroadcast(Broadcast b) {
		LinkedList<MicroService> ls;
		synchronized (broadcastHashMap) {
			ls = broadcastHashMap.get(b.getClass());
		}

		Queue<Message> queue;

		synchronized (ls) {
			for(MicroService m : ls) {
				synchronized (microServicesHashMap) {
					queue = microServicesHashMap.get(m);
				}

				synchronized (queue) {
					queue.add(b);
					queue.notifyAll();
				}
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		LinkedList<MicroService> microServices;

		synchronized (eventHashMap) {
			microServices = eventHashMap.get(e.getClass());		//choose microservice
		}

		MicroService chosenMicroService;

		synchronized (microServices) {
			if(microServices.isEmpty())
				return null;

			// TODO: make synchronized
		
			chosenMicroService = microServices.removeFirst();
			microServices.addLast(chosenMicroService);
		}

		Queue<Message> eventQueue;
		// TODO: use a thread safe queue? so no problems happen with the get query
		synchronized(microServicesHashMap) {
			eventQueue = microServicesHashMap.get(chosenMicroService);
		}

		Future<T> future = new Future<>();
		// TODO: maybe change synchronization to be on microservice
		synchronized (eventQueue) {
			eventQueue.add(e);	//adds the message to the chosen micro-service Queue
			// TODO: move the futureHashMap synchronized block out of the eventQueue block?
			synchronized (futureHashMap) {
				futureHashMap.put(e, future);
			}

			eventQueue.notify(); // only one thread waiting for events on each queue
		}
		// chosenMicroService.notify();
		return future;
	}


	@Override
	public void register(MicroService m) {
		Queue<Message> ls = new LinkedList<Message>();

		synchronized (microServicesHashMap) {
			microServicesHashMap.put(m, ls);
		}
	}


	@Override
	public void unregister(MicroService m) {
		synchronized (microServicesHashMap) {
			microServicesHashMap.remove(m);
		}
		
		synchronized (broadcastHashMap) { // TODO: use fast fail iterator functionality??
			for(LinkedList<MicroService> ls : broadcastHashMap.values()) {
				synchronized (ls) {
					ls.remove(m);
				}
			}
		}

		synchronized (eventHashMap) {
			for(LinkedList<MicroService> ls : eventHashMap.values()) {
				synchronized (ls) {
					ls.remove(m);
				}
			}
		}

		if(m instanceof TimeService){
			completeAll();
			//TODO: remove debug
			CRMSRunner.synchronizedSyso(Cluster.getInstance().toString());
		}
	}


	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		//TODO: need to check when to throw exception
		Queue<Message> ls;

		synchronized (microServicesHashMap) {
			ls = microServicesHashMap.get(m);
		}

		Message message;

		synchronized (ls) {
			if(ls.isEmpty()) {
				// try{
				   ls.wait();
				// }
			}

			message = ((LinkedList<Message>) ls).removeFirst();
		}

		return message;
	}


	public boolean isRegistered(MicroService m) {
		synchronized (microServicesHashMap) {
			return microServicesHashMap.get(m) != null;
		}
	}


	public boolean isSubscribedToBroadcast(Class<? extends Broadcast> type, MicroService m){
		LinkedList<MicroService> ls;

		synchronized (broadcastHashMap) {
			ls = broadcastHashMap.get(type);
		}

		if(ls == null)
			return false;

		synchronized (ls) {
			return ls.contains(m);
		}
	}


	public <T> boolean isSubscribedToEvent(Class<? extends Event<T>> type, MicroService m){
		LinkedList<MicroService> ls;

		synchronized (eventHashMap) {
			ls = eventHashMap.get(type);
		}

		if(ls == null)
			return false;

		synchronized (ls) {
			return ls.contains(m);
		}
	}
}
