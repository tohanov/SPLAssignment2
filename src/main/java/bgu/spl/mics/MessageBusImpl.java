package bgu.spl.mics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.example.messages.ExampleEvent;



/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private static MessageBusImpl instance= null;
	
	HashMap<MicroService,Queue<Message>> microServicesHashMap;
	
	HashMap<Class<? extends Broadcast>,LinkedList<MicroService>> broadcastHashMap; 

	HashMap<Class<? extends Event>,LinkedList<MicroService>> eventHashMap;

	HashMap<Class<? extends Event>,Integer> roundRobinOrder;

	private MessageBusImpl(){
	
		microServicesHashMap=new HashMap<>();
		
		broadcastHashMap=new HashMap<>();
		broadcastHashMap.put(PublishConferenceBroadcast.class, new LinkedList<>());
		broadcastHashMap.put(TickBroadcast.class, new LinkedList<>());
		
		eventHashMap=new HashMap<>();
		eventHashMap.put(PublishResultsEvent.class, new LinkedList<>());
		eventHashMap.put(TestModelEvent.class, new LinkedList<>());
		eventHashMap.put(TrainModelEvent.class, new LinkedList<>());
		eventHashMap.put(ExampleEvent.class, new LinkedList<>());	
		roundRobinOrder=new HashMap<>();

		for(Class<? extends Event> key:roundRobinOrder.keySet())
			roundRobinOrder.put(key, 0);
		
	}

	public static MessageBusImpl getInstance(){

		if(instance==null)
			instance=new MessageBusImpl();

		return instance;

	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		
		eventHashMap.get(type).addLast(m);

	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		
		broadcastHashMap.get(type).addLast(m);
		
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendBroadcast(Broadcast b) {

		LinkedList<MicroService> ls=broadcastHashMap.get(b.getClass());

		for(MicroService m: ls)
			microServicesHashMap.get(m).add(b);
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		//choose microservice
		LinkedList<MicroService> ls=eventHashMap.get(e.getClass());
		
		if(ls.size()==0)
			return null;

		int orderNum=roundRobinOrder.get(e.getClass());		// the current microservices in the round robin order
		
		MicroService chosenMicroService=ls.get(orderNum);
		roundRobinOrder.put(e.getClass(), (orderNum+1) % ls.size());

		microServicesHashMap.get(chosenMicroService).add(e);

		return new Future<T>();
	}

	@Override
	public void register(MicroService m) {

		Queue<Message> ls=new LinkedList<Message>();
		microServicesHashMap.put(m, ls);

	}

	@Override
	public void unregister(MicroService m) {
		
		microServicesHashMap.remove(m);
		
		for(LinkedList<MicroService> ls:broadcastHashMap.values())
			ls.remove(m);
		

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isRegistered(MicroService m){

		return microServicesHashMap.get(m)!=null;

	}

	public boolean isSubscribedToBroadcast(Class<? extends Broadcast> type, MicroService m){

		if(broadcastHashMap.get(type)==null)
			return false;
		return broadcastHashMap.get(type).contains(m);

	}

	public <T> boolean isSubscribedToEvent(Class<? extends Event<T>> type, MicroService m){

		if(eventHashMap.get(type)==null)
			return false;
		return eventHashMap.get(type).contains(m);
	}

	
}
