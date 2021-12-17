package bgu.spl.mics;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
	
	
	private HashMap<MicroService,ArrayDeque<Message>> microServicesHashMap;
	private HashMap<Class<? extends Broadcast>,LinkedList<MicroService>> broadcastHashMap; 
	private HashMap<Class<? extends Message>,LinkedList<MicroService>> eventHashMap;
	private HashMap<Event<? extends Object>,Future<? extends Object>> futureHashMap;
	// private HashMap<LinkedList<MicroService>,ReentrantReadWriteLock> broadcastListLocksMap;

	private ReentrantReadWriteLock microServicesHashMapRWL; 
	//private ReentrantReadWriteLock broadcastHashMapRWL;
	private ReentrantReadWriteLock futureHashMapRWL;

	private MessageBusImpl(){
		microServicesHashMap = new HashMap<>();
		
		broadcastHashMap = new HashMap<>();
		broadcastHashMap.put(PublishConferenceBroadcast.class, new LinkedList<>());
		broadcastHashMap.put(TickBroadcast.class, new LinkedList<>());
		
		eventHashMap = new HashMap<>();
		eventHashMap.put(PublishResultsEvent.class, new LinkedList<>());
		eventHashMap.put(TestModelEvent.class, new LinkedList<>());
		eventHashMap.put(TrainModelEvent.class, new LinkedList<>());
		// eventHashMap.put(ExampleEvent.class, new LinkedList<>());

		futureHashMap=new HashMap<>();

		microServicesHashMapRWL=new ReentrantReadWriteLock();
		//broadcastHashMapRWL=new ReentrantReadWriteLock();
		futureHashMapRWL=new ReentrantReadWriteLock();

		// broadcastListLocksMap = new HashMap<>();
	}


	public static MessageBusImpl getInstance() {
		return InternalSingleton.instance;
	}


	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> eventType, MicroService subscriberMS) {
		LinkedList<MicroService> eventSubscribedMicroServices;

	//	synchronized (eventHashMap) {
			eventSubscribedMicroServices = eventHashMap.get(eventType);
	//	}

		synchronized (eventSubscribedMicroServices) {
			eventSubscribedMicroServices.addLast(subscriberMS);
		}
	}


	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> broadcastType, MicroService subscriberMS) {
		LinkedList<MicroService> broadcastSubscribedMicroServices;
		//synchronized (broadcastHashMap) {
			//broadcastHashMapRWL.readLock().lock();
			broadcastSubscribedMicroServices = broadcastHashMap.get(broadcastType);
			//broadcastHashMapRWL.readLock().unlock();

		//}

		synchronized (broadcastSubscribedMicroServices) {
			broadcastSubscribedMicroServices.addLast(subscriberMS);
		}
		// synchronized (broadcastListLocksMap) {
		// 	broadcastListLocksMap.addLast(subscriberMS);
		// }
	}


	@Override
	public <T> void complete(Event<T> completeEvent, T result) {
		Future<T> futureToResolve;
		//synchronized (futureHashMap) {
			futureHashMapRWL.writeLock().lock();
			futureToResolve = (Future<T>)futureHashMap.remove(completeEvent);
			futureHashMapRWL.writeLock().unlock();
		//}

		futureToResolve.resolve(result);
	}


	private void completeAll(){ // TODO : rethink
		futureHashMapRWL.readLock().lock();
		for(Future<? extends Object> overDueFuture: futureHashMap.values()){
			synchronized(overDueFuture) {
				overDueFuture.notifyAll();
			}
		}
		futureHashMapRWL.readLock().unlock();
	}


	@Override
	public void sendBroadcast(Broadcast broadcast) {
		LinkedList<MicroService> broadcastSubscribedMicroServices;
		//synchronized (broadcastHashMap) {
			//broadcastHashMapRWL.readLock().lock();
			broadcastSubscribedMicroServices = broadcastHashMap.get(broadcast.getClass());
			//broadcastHashMapRWL.readLock().unlock();

		//}

		Queue<Message> broadcastAcceptingQueue;

		synchronized (broadcastSubscribedMicroServices) {

			// TODO: lock to sync with conference unsubsribe requests

			for(MicroService subscribedMicroService : broadcastSubscribedMicroServices) {
				//synchronized (microServicesHashMap) {
					microServicesHashMapRWL.readLock().lock();
					broadcastAcceptingQueue = microServicesHashMap.get(subscribedMicroService);
					microServicesHashMapRWL.readLock().unlock();
			//	}

				synchronized (broadcastAcceptingQueue) {
					broadcastAcceptingQueue.add(broadcast);
					broadcastAcceptingQueue.notifyAll();
				}
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> event) {
		LinkedList<MicroService> eventSubscribedMicroServices;

	//	synchronized (eventHashMap) {
			eventSubscribedMicroServices = eventHashMap.get(event.getClass());		//choose microservice
	//	}

		MicroService roundRobinChosenMS;

		synchronized (eventSubscribedMicroServices) {
			if(eventSubscribedMicroServices.isEmpty())
				return null;

			// TODO: make synchronized
		
			roundRobinChosenMS = eventSubscribedMicroServices.removeFirst();
			eventSubscribedMicroServices.addLast(roundRobinChosenMS);
		}

		Queue<Message> chosenMSEventQueue;
		// TODO: use a thread safe queue? so no problems happen with the get query
		//synchronized(microServicesHashMap) {
			microServicesHashMapRWL.readLock().lock();
			chosenMSEventQueue = microServicesHashMap.get(roundRobinChosenMS);
			microServicesHashMapRWL.readLock().unlock();
		//}

		Future<T> eventFuture = new Future<>();
		// TODO: maybe change synchronization to be on microservice
		synchronized (chosenMSEventQueue) {
			chosenMSEventQueue.add(event);	//adds the message to the chosen micro-service Queue
			// TODO: move the futureHashMap synchronized block out of the eventQueue block?
			//synchronized (futureHashMap) {
				futureHashMapRWL.writeLock().lock();
				futureHashMap.put(event, eventFuture);
				futureHashMapRWL.writeLock().unlock();

			//}

			chosenMSEventQueue.notify(); // only one thread waiting for events on each queue
		}
		// chosenMicroService.notify();
		return eventFuture;
	}


	@Override
	public void register(MicroService msToRegister) {
		//synchronized (microServicesHashMap) {
			microServicesHashMapRWL.writeLock().lock();
			microServicesHashMap.put(msToRegister, new ArrayDeque<Message>());
			microServicesHashMapRWL.writeLock().unlock();
		//}
	}


	@Override
	public void unregister(MicroService msToUnregister) {
		//synchronized (microServicesHashMap) {
			microServicesHashMapRWL.writeLock().lock();
			microServicesHashMap.remove(msToUnregister);
			microServicesHashMapRWL.writeLock().unlock();

		//}
		
		//synchronized (broadcastHashMap) { // TODO: use fast fail iterator functionality??
			for(LinkedList<MicroService> broadcastSubscribedMSList : broadcastHashMap.values()) {
				synchronized (broadcastSubscribedMSList) {
					broadcastSubscribedMSList.remove(msToUnregister);
				}
			}
		//}

	//	synchronized (eventHashMap) {
			for(LinkedList<MicroService> eventSubscribedMSList : eventHashMap.values()) {
				synchronized (eventSubscribedMSList) {
					eventSubscribedMSList.remove(msToUnregister);
				}
			}
	//	}

		// notify unresolved futures at end of programs
		if(msToUnregister instanceof TimeService){
			completeAll();
			//TODO: remove debug
			CRMSRunner.synchronizedSyso(Cluster.getInstance().toString());
		}
	}


	@Override
	public Message awaitMessage(MicroService waitingMS) throws InterruptedException {
		//TODO: need to check when to throw exception
		Queue<Message> msMessageQueue;

		//synchronized (microServicesHashMap) {
			microServicesHashMapRWL.readLock().lock();
			msMessageQueue = microServicesHashMap.get(waitingMS);
			microServicesHashMapRWL.readLock().unlock();

		//}

		Message receivedMessage;

		synchronized (msMessageQueue) {
			if(msMessageQueue.isEmpty()) {
				// try{
				   msMessageQueue.wait();
				// }
			}

			receivedMessage = msMessageQueue.poll();
		}

		return receivedMessage;
	}


	// region Test Methods
	public boolean isRegistered(MicroService m) {
		//synchronized (microServicesHashMap) {
			microServicesHashMapRWL.readLock().lock();
			boolean ans= microServicesHashMap.get(m) != null;
			microServicesHashMapRWL.readLock().unlock();

			return ans;
		//}
	}


	public boolean isSubscribedToBroadcast(Class<? extends Broadcast> type, MicroService m){
		LinkedList<MicroService> ls;

		//synchronized (broadcastHashMap) {
			ls = broadcastHashMap.get(type);
		//}

		if(ls == null)
			return false;

		synchronized (ls) {
			return ls.contains(m);
		}
	}


	public <T> boolean isSubscribedToEvent(Class<? extends Event<T>> type, MicroService m){
		LinkedList<MicroService> ls;

		//synchronized (eventHashMap) {
			ls = eventHashMap.get(type);
		//}

		if(ls == null)
			return false;

		synchronized (ls) {
			return ls.contains(m);
		}
	}
	// endregion Test Methods
}

