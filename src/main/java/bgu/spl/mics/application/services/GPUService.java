package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

import java.util.HashMap;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Callback;
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

	// Original
    // public GPUService(String name) {
    //     super("Change_This_Name");
    //     // TODO Implement this
    // }

	// private Class<? extends Event<Object>> types = {
		
	// };

	// region Added
	// private static Cluster cluster = Cluster.getInstance();
	// private GPU gpu;
	// private static final HashMap<GPU.Type,int> delays = new HashMap<GPU.Type,int>() {GPU.Type.RTX3090 : 1, GPU.Type.RTX2080 : 2, GPU.Type.GTX1080 : 4};
	// endregion Added


	// public GPUService(String _name, GPU _gpu) {
	// 	// TODO implement this
    //     super(_name);
	// 	// this.cluster = Cluster.getInstance();
	// 	// this.gpu = _gpu;

	// 	// ticksToTrainBatch = 0; // TODO: remove?
	// 	// training = false;
    // }


    @Override
    protected void initialize() {
        // TODO Implement this
		// MessageBusImpl messageBus = MessageBusImpl.getInstance();
		// messageBus.register(this); // moved to MicroService


		subscribeBroadcast(TickBroadcast.class, message -> gpu.gotTick());
		subscribeEvent(TrainModelEvent.class, trainModelEvent -> gpu.gotModelEvent(trainModelEvent));
		subscribeEvent(TestModelEvent.class, trainModelEvent -> gpu.gotModelEvent(trainModelEvent));
    }


	// region for serialization from json
	private static int gpuCounter = 0;
	private GPU gpu;

	public GPUService(String _gpu) {
		super("GPU_" + gpuCounter);

		gpu = new GPU(_gpu);
		++gpuCounter;
	}
	// endregion for serialization from json
}
