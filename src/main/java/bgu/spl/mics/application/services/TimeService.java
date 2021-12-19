package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.CRMSRunner;


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
		
		duration = _duration;
		tickTime = _tickTime;
	}

	
	@Override
	protected void initialize() {
		TickBroadcast tick = new TickBroadcast(false);
		MessageBusImpl messageBus = MessageBusImpl.getInstance();

		try {
			for (int i = 1; i < duration; ++i) {
				Thread.sleep(tickTime);
				messageBus.sendBroadcast(tick);

				// TODO: remove debug block
				// synchronized (System.out) {
				// 	System.out.println("[*] Timeservice: sent tick " + i);
				// }
			}

			Thread.sleep(tickTime);
			messageBus.sendBroadcast(new TickBroadcast(true));
			
			// TODO: remove debug block
			synchronized (System.out) {
				System.out.println("[*] Timeservice: sent the LAST tick (" + duration + ")");
			}
		}
		catch (Exception e) { 
			CRMSRunner.synchronizedPrintStackTrace(e); 
		}
		
		
		terminate(); // not entering the run() loop since doesn't subscribe to any messages
	}
	
}
