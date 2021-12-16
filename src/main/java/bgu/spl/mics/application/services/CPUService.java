package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.DataBatch;

/**
 * CPU service is responsible for handling the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {

    private CPU cpu;


	// region for serialization from json
	private static int cpuCounter = 0;

	public CPUService(int _cpu) {
		super("CPU_" + cpuCounter);

		cpu = new CPU(_cpu);
		++cpuCounter;
	}
	// endregion for serialization from json


    @Override
    protected void initialize() {
        
        subscribeBroadcast(TickBroadcast.class, (tickBroadcast)->{
            cpu.tickCallback();

			// if (tickBroadcast.isLast()) {

			// 	// TODO: remove debug block
			// 	synchronized (System.out) {
			// 		System.out.println("[*] " + getName() + ": got LAST tick");
			// 	}

			// 	terminate();
			// }
        });

    }
}
