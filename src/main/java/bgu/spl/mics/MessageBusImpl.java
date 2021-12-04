package bgu.spl.mics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.text.AbstractDocument.BranchElement;

import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;



/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private static MessageBusImpl instance= null;
	
	HashMap<MicroService,Queue<Message>> microServicesHashMap;
	
	HashMap<Class<? extends Broadcast>,LinkedList<MicroService>> broadcastHashMap; 

	private MessageBusImpl(){
	
		microServicesHashMap=new HashMap<>();
		
		broadcastHashMap=new HashMap<>();
		broadcastHashMap.put(new PublishConferenceBroadcast().getClass(), new LinkedList<>());
		broadcastHashMap.put(new TickBroadcast().getClass(), new LinkedList<>());
		
	

		
		

	}

	public static MessageBusImpl getInstance(){

		if(instance==null)
			instance=new MessageBusImpl();

		return instance;

	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void register(MicroService m) {

		Queue<Message> ls=new LinkedList<Message>();
		microServicesHashMap.put(m, ls);

	}

	@Override
	public void unregister(MicroService m) {
		
		microServicesHashMap.remove(m);

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	

}
