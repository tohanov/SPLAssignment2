package bgu.spl.mics.application.services;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.text.MessageFormat;
import java.util.Timer;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService {

	private int duration;
	private int tickTime;


	public TimeService(int _duration, int _tickTime) {
		super("TimeService");
		// TODO Implement this
		// Timer timer = new Timer();
		duration = _duration;
		tickTime = _tickTime;
	}

	
	@Override
	protected void initialize() {
		// TODO Implement this
		TickBroadcast tick = new TickBroadcast(false);
		MessageBusImpl messageBus = MessageBusImpl.getInstance();

		try {
			for ( ; duration != 1; --duration) {
				Thread.sleep(tickTime);
				messageBus.sendBroadcast(tick);
			}

			Thread.sleep(tickTime);
			messageBus.sendBroadcast(new TickBroadcast(true));
		}
		catch (Exception e) { 
			synchronized (System.out) {
				e.printStackTrace(); 
			} 
		}

		terminate(); // not entering the run() loop
	}
}
