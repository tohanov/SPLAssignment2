package bgu.spl.mics;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.services.TimeService;



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

	private ReentrantReadWriteLock microServicesHashMapRWL;
	private ReentrantReadWriteLock futureHashMapRWL;


	private MessageBusImpl(){
		microServicesHashMap = new HashMap<>();
		microServicesHashMapRWL = new ReentrantReadWriteLock();
		
		broadcastHashMap = new HashMap<>();
		broadcastHashMap.put(PublishConferenceBroadcast.class,new LockPair<>(new LinkedList<>(), new ReentrantReadWriteLock()));
		broadcastHashMap.put(TickBroadcast.class, new LockPair<>(new LinkedList<>(), new ReentrantReadWriteLock()));

		eventHashMap = new HashMap<>();
		eventHashMap.put(PublishResultsEvent.class, new LockPair<>(new LinkedList<>(), new ReentrantReadWriteLock()));
		eventHashMap.put(TestModelEvent.class, new LockPair<>(new LinkedList<>(), new ReentrantReadWriteLock()));
		eventHashMap.put(TrainModelEvent.class, new LockPair<>(new LinkedList<>(), new ReentrantReadWriteLock()));

		futureHashMap = new HashMap<>();
		futureHashMapRWL = new ReentrantReadWriteLock();
	}


	public static MessageBusImpl getInstance() {
		return InternalSingleton.instance;
	}


	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> eventType, MicroService subscriberMS) {
		LockPair<LinkedList<MicroService>> pairSubscribed = eventHashMap.get(eventType);
		
		pairSubscribed.lock.writeLock().lock();
			pairSubscribed.object.addLast(subscriberMS);
		pairSubscribed.lock.writeLock().unlock();
	}


	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> broadcastType, MicroService subscriberMS) {
		LockPair<LinkedList<MicroService>> pairSubscribed = broadcastHashMap.get(broadcastType);

		pairSubscribed.lock.writeLock().lock();
			pairSubscribed.object.addLast(subscriberMS);
		pairSubscribed.lock.writeLock().unlock();
	}


	@Override
	@SuppressWarnings("unchecked")
	public <T> void complete(Event<T> completeEvent, T result) {
		Future<T> futureToResolve;

		futureHashMapRWL.writeLock().lock();
			futureToResolve = (Future<T>)futureHashMap.remove(completeEvent);
		futureHashMapRWL.writeLock().unlock();

		futureToResolve.resolve(result);
	}


	/**
	 * @Post: for(Future<? extends Object> overDueFuture: futureHashMap.values()){
	 * 			no object is awaiting overDueFuture
	 * } 
	 */
	private void completeAll() {
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
			
				// needed for thread safety reasons in case the microservice got unregistered
				// in between us locking for read on microServicesHashMap and us running getting
				if (broadcastAcceptingQueue != null) {
					synchronized (broadcastAcceptingQueue) {
						broadcastAcceptingQueue.add(broadcast);
						broadcastAcceptingQueue.notifyAll();
					}
				}
			}
		pairSubscribed.lock.readLock().unlock();
		
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> event) {
		//choose microservice
		LockPair<LinkedList<MicroService>> pairSubscribed = eventHashMap.get(event.getClass());
		MicroService roundRobinChosenMS;

		pairSubscribed.lock.writeLock().lock();
			if (pairSubscribed.object.isEmpty()) {
				pairSubscribed.lock.writeLock().unlock();
				return null;
			}
			roundRobinChosenMS = pairSubscribed.object.removeFirst();
			pairSubscribed.object.addLast(roundRobinChosenMS);
		pairSubscribed.lock.writeLock().unlock();

		Queue<Message> chosenMSEventQueue;

		microServicesHashMapRWL.readLock().lock();
			chosenMSEventQueue = microServicesHashMap.get(roundRobinChosenMS);
		microServicesHashMapRWL.readLock().unlock();

		Future<T> eventFuture = new Future<>();
		
		// needed for thread safety reasons in case the microservice got unregistered
		// in between us locking for read on microServicesHashMap and us running getting
		if (chosenMSEventQueue != null) {
			synchronized (chosenMSEventQueue) {
				//adds the message to the chosen micro-service Queue
				chosenMSEventQueue.add(event);
				
				futureHashMapRWL.writeLock().lock();
					futureHashMap.put(event, eventFuture);
				futureHashMapRWL.writeLock().unlock();

				// only one thread waiting for events on each queue
				chosenMSEventQueue.notify();
			}
		}

		return eventFuture;
	}


	@Override
	public void register(MicroService msToRegister) {
		microServicesHashMapRWL.writeLock().lock();
			microServicesHashMap.put(msToRegister, new ArrayDeque<Message>());
		microServicesHashMapRWL.writeLock().unlock();
	}


	@Override
	public void unregister(MicroService msToUnregister) {
		microServicesHashMapRWL.writeLock().lock();
			microServicesHashMap.remove(msToUnregister);
		microServicesHashMapRWL.writeLock().unlock();
	
		for(LockPair<LinkedList<MicroService>> pairSubscribed : broadcastHashMap.values()) {
			pairSubscribed.lock.writeLock().lock();
				pairSubscribed.object.remove(msToUnregister);
			pairSubscribed.lock.writeLock().unlock();
		}
		
		for(LockPair<LinkedList<MicroService>> pairSubscribed : eventHashMap.values()) {
			pairSubscribed.lock.writeLock().lock();
				pairSubscribed.object.remove(msToUnregister);
			pairSubscribed.lock.writeLock().unlock();
		}

		// notify unresolved futures at end of programs
		if(msToUnregister instanceof TimeService){
			completeAll();

			//TODO: remove debug
			//CRMSRunner.synchronizedSyso(Cluster.getInstance().toString());
		}
	}


	@Override
	public Message awaitMessage(MicroService waitingMS) throws InterruptedException {
		Queue<Message> msMessageQueue;

		microServicesHashMapRWL.readLock().lock();
			msMessageQueue = microServicesHashMap.get(waitingMS);
		microServicesHashMapRWL.readLock().unlock();

		Message receivedMessage;

		synchronized (msMessageQueue) {
			if(msMessageQueue.isEmpty()) {
				msMessageQueue.wait();
			}

			receivedMessage = msMessageQueue.poll();
		}

		return receivedMessage;
	}


	// region Test Methods
	public boolean isRegistered(MicroService m) {
		microServicesHashMapRWL.readLock().lock();
			boolean ans= microServicesHashMap.get(m) != null;
		microServicesHashMapRWL.readLock().unlock();

		return ans;
	}


	public boolean isSubscribedToBroadcast(Class<? extends Broadcast> type, MicroService m){
		LinkedList<MicroService> ls;

		ls = broadcastHashMap.get(type).object;

		if(ls == null)
			return false;

		synchronized (ls) {
			return ls.contains(m);
		}
	}


	public <T> boolean isSubscribedToEvent(Class<? extends Event<T>> type, MicroService m){
		LinkedList<MicroService> ls;

		ls = eventHashMap.get(type).object;

		if(ls == null)
			return false;

		synchronized (ls) {
			return ls.contains(m);
		}
	}
	// endregion Test Methods
}

