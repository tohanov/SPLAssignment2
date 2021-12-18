package bgu.spl.mics;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
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
import bgu.spl.mics.application.services.GPUService;
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
	
	private class LockPair<T> {
		private T object; 
		private ReentrantReadWriteLock lock;

		private LockPair (T _object, ReentrantReadWriteLock _lock) {
			object = _object;
			lock = _lock;
		}
	}
	
	private HashMap<MicroService,ArrayDeque<Message>> microServicesHashMap;
	private HashMap<Class<? extends Broadcast>,LockPair<LinkedList<MicroService>>> broadcastHashMap; 
	private HashMap<Class<? extends Message>,LockPair<LinkedList<MicroService>>> eventHashMap;
	private HashMap<Event<? extends Object>,Future<? extends Object>> futureHashMap;
	// private HashMap<LinkedList<MicroService>,ReentrantReadWriteLock> broadcastListLocksMap;

	private ReentrantReadWriteLock microServicesHashMapRWL; 
	//private ReentrantReadWriteLock broadcastHashMapRWL;
	private ReentrantReadWriteLock futureHashMapRWL;

	private MessageBusImpl(){
		microServicesHashMap = new HashMap<>();
		
		broadcastHashMap = new HashMap<>();
		broadcastHashMap.put(PublishConferenceBroadcast.class, new LockPair<>(new LinkedList<>(), new ReentrantReadWriteLock()));
		broadcastHashMap.put(TickBroadcast.class, new LockPair<>(new LinkedList<>(), new ReentrantReadWriteLock()));
		
		eventHashMap = new HashMap<>();
		eventHashMap.put(PublishResultsEvent.class, new LockPair<>(new LinkedList<>(), new ReentrantReadWriteLock()));
		eventHashMap.put(TestModelEvent.class, new LockPair<>(new LinkedList<>(), new ReentrantReadWriteLock()));
		eventHashMap.put(TrainModelEvent.class, new LockPair<>(new LinkedList<>(), new ReentrantReadWriteLock()));

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
		// LinkedList<MicroService> eventSubscribedMicroServices;

	//	synchronized (eventHashMap) {
		LockPair<LinkedList<MicroService>> pairSubscribed = eventHashMap.get(eventType);
	//	}

		// synchronized (eventSubscribedMicroServices) {
		// 	eventSubscribedMicroServices.addLast(subscriberMS);
		// }
		
		pairSubscribed.lock.writeLock().lock();
		pairSubscribed.object.addLast(subscriberMS);
		pairSubscribed.lock.writeLock().unlock();
	}


	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> broadcastType, MicroService subscriberMS) {
		// LinkedList<MicroService> broadcastSubscribedMicroServices;
		//synchronized (broadcastHashMap) {
			//broadcastHashMapRWL.readLock().lock();

			LockPair<LinkedList<MicroService>> pairSubscribed = broadcastHashMap.get(broadcastType);
			//broadcastHashMapRWL.readLock().unlock();

		//}

		// synchronized (broadcastSubscribedMicroServices) {
			pairSubscribed.lock.writeLock().lock();
			pairSubscribed.object.addLast(subscriberMS);
			pairSubscribed.lock.writeLock().unlock();
		// }
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


	/**
	 * @Post: for(Future<? extends Object> overDueFuture: futureHashMap.values()){
	 * 			no object is awaiting overDueFuture
	 * } 
	 */
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
		LockPair<LinkedList<MicroService>> pairSubscribed = broadcastHashMap.get(broadcast.getClass());
		Queue<Message> broadcastAcceptingQueue;

	

			// lock to sync with conference unsubsribe requests
			pairSubscribed.lock.readLock().lock();
			for(MicroService subscribedMicroService : pairSubscribed.object) {
					microServicesHashMapRWL.readLock().lock();
					broadcastAcceptingQueue = microServicesHashMap.get(subscribedMicroService);
					microServicesHashMapRWL.readLock().unlock();
			
				if (broadcastAcceptingQueue != null) { // TODO: check if needed in case the microservice got unregistered in between us locking for read on microServicesHashMap and us running getting
					synchronized (broadcastAcceptingQueue) { // leaving on synchronized because of notifyAll()??
						broadcastAcceptingQueue.add(broadcast);
						broadcastAcceptingQueue.notifyAll();
					}
				}
			}
			pairSubscribed.lock.readLock().unlock();
		
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> event) {
		// LinkedList<MicroService> eventSubscribedMicroServices;

	//	synchronized (eventHashMap) {
		LockPair<LinkedList<MicroService>> pairSubscribed = eventHashMap.get(event.getClass());		//choose microservice
	//	}

		MicroService roundRobinChosenMS;

		// synchronized (eventSubscribedMicroServices) {
			pairSubscribed.lock.writeLock().lock();
			if(pairSubscribed.object.isEmpty()){
				
				pairSubscribed.lock.writeLock().unlock();
				return null;
			}
			roundRobinChosenMS = pairSubscribed.object.removeFirst();
			pairSubscribed.object.addLast(roundRobinChosenMS);
			
			pairSubscribed.lock.writeLock().unlock();
		// }

		Queue<Message> chosenMSEventQueue;
		// TODO: use a thread safe queue? so no problems happen with the get query
		//synchronized(microServicesHashMap) {
			microServicesHashMapRWL.readLock().lock();
			chosenMSEventQueue = microServicesHashMap.get(roundRobinChosenMS);
			microServicesHashMapRWL.readLock().unlock();
		//}

		Future<T> eventFuture = new Future<>();
		// TODO: maybe change synchronization to be on microservice
		
		if (chosenMSEventQueue != null) { // TODO: check if needed in case the microservice got unregistered in between us locking for read on microServicesHashMap and us running getting
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
		
		//synchronized (broadcastHashMap) {
			for(LockPair<LinkedList<MicroService>> pairSubscribed : broadcastHashMap.values()) {
				// synchronized (pairSubscribed.lock) {
					pairSubscribed.lock.writeLock().lock();
					pairSubscribed.object.remove(msToUnregister);
					pairSubscribed.lock.writeLock().unlock();
				// }
			}
		//}

	//	synchronized (eventHashMap) {
			for(LockPair<LinkedList<MicroService>> pairSubscribed : eventHashMap.values()) {
				// synchronized (eventSubscribedMSList) {
					pairSubscribed.lock.writeLock().lock();
					pairSubscribed.object.remove(msToUnregister);
					pairSubscribed.lock.writeLock().unlock();
				// }
			}
	//	}

		// notify unresolved futures at end of programs
		if(msToUnregister instanceof TimeService){
			completeAll();
			//TODO: remove debug
			//CRMSRunner.synchronizedSyso(Cluster.getInstance().toString());
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


	// region Test Methods-------------
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
			ls = broadcastHashMap.get(type).object;
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
			ls = eventHashMap.get(type).object;
		//}

		if(ls == null)
			return false;

		synchronized (ls) {
			return ls.contains(m);
		}
	}
	// endregion Test Methods


    public Object getNumberOfMessagesInQueue(MicroService ms) {
        return microServicesHashMap.get(ms).size();
    }
}

