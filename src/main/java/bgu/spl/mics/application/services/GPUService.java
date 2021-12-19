package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

import java.util.ArrayList;


/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

	// region for serialization from json
	private static int gpuCounter = 0;
	private GPU gpu;

	public GPUService(String _gpu) {
		super("GPU_" + gpuCounter);

		gpu = new GPU(_gpu);
		++gpuCounter;
	}
	// endregion for serialization from json


    @Override
    protected void initialize() {

		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> { 
			ArrayList<Event<Model>> handledEvents = gpu.actOnTick();
			if(handledEvents != null){
			 for(Event<Model> toHandle: handledEvents){
						MessageBusImpl.getInstance().complete(toHandle, toHandle.getValue());
				}
			}

			if (tickBroadcast.isLast()) {

				// TODO: remove debug block
				synchronized (System.out) {
					System.out.println("[*] " + getName() + ": got LAST tick");
				}

				terminate();
			}
		});

		subscribeEvent(TrainModelEvent.class, trainModelEvent -> {
			// TODO : remove debug
			synchronized(System.out){
				System.out.println(getName()+" Received model "+trainModelEvent.getValue().getName() + " for training");
			}

			gpu.addModel(trainModelEvent);
		});
		
		subscribeEvent(TestModelEvent.class, testModelEvent -> {
			// was tested now, needs to be resolved now
			if (gpu.addModel(testModelEvent) == false)
				MessageBusImpl.getInstance().complete(testModelEvent, testModelEvent.getValue());
		});
    }


    public GPU getGPU() {
        return gpu;
    }
}
