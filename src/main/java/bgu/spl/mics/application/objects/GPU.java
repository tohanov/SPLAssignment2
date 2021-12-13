package bgu.spl.mics.application.objects;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.experimental.theories.Theories;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.services.GPUService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.Callback;
import bgu.spl.mics.Event;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
	
    /**
     * Enum representing the type of the GPU.
     */
    public enum Type { RTX3090, RTX2080, GTX1080 }


	// region According to assignment instructions
    private Type type;
    private Model model; // the model being taken care of currently
    private static Cluster cluster = Cluster.getInstance();
	// endregion According to assignment instructions


	// region Added fields
    // private GPUService service;
	// private Collection<DataBatch> processedBatches;
	private Queue<Event<Model>> modelEventsQueue;
	private final int trainingDelay; // according to the type of the gpu
	private final byte vRAM; // according to the type of the gpu
	private int storedProcessedBatchesNumber;
	private int ticksToTrainBatch;
	private boolean training;
	private boolean testing;
	// endregion Added fields

	/**
	 * @param _type a string representing the GPU type
	 * 
	 * @post vRAM is in {8, 16, 32}
	 * @post processedBatchesNum == 0
	 * @post MessageBusImpl.getInstance().isRegistered(service) == true
	 * @post service != null
	 */
    // public GPU(/*String _name,*/ String _type) {
    //     this.type = typeFromString(_type);
	// 	// this.service = new GPUService(_name, this);

	// 	processedBatchesNum = 0;

	// 	switch(type) {
	// 		case GTX1080:
	// 			vRAM = 8;
	// 			break;
	// 		case RTX2080:
	// 			vRAM = 16;
	// 			break;
	// 		default : // RTX3090:
	// 			vRAM = 32;
	// 	}
    // }


	/**
     * @return The type of the GPU
     */
    // public Type getType () {
    //     return type;
    // }


	// public void runService() { // TODO remove ?
	// 	service.run();
	// }


	/**
	 * @return Reference to the corresponding GPUService
	 */
	// public GPUService getService() {
	// 	return service;
	// }


	// public Collection<DataBatch> getProcessedBatches() {
	// 	return processedBatches;
	// }


	// region for serialization from json
	public GPU(String _type) {
        this.type = typeFromString(_type);
		model = null;
		storedProcessedBatchesNumber = 0;

		switch(type) {
			case GTX1080:
				vRAM = 8;
				trainingDelay = 4;
				break;
			case RTX2080:
				vRAM = 16;
				trainingDelay = 2;
				break;
			default : // RTX3090:
				vRAM = 32;
				trainingDelay = 1;
		}

		modelEventsQueue = new ArrayDeque<>();
    }


	private Type typeFromString(String _type) {
		Type returnType;
		String uppercaseType = _type.toUpperCase();

		if (uppercaseType == "RTX3090")
			returnType = Type.RTX3090;
		else if (uppercaseType == "RTX2080")
			returnType = Type.RTX2080;
		else
			returnType = Type.GTX1080;
		
		return returnType;
	}
	// endregion for serialization from json


	public void gotTick() {
		// TODO: treat the case of last tick??

		// TODO: split to smaller functions (queries + actions)
		if (training) { // && storedProcessedBatchesNumber != 0) {
			--ticksToTrainBatch;
			
			if (ticksToTrainBatch == 0) { // if finished training batch
				ticksToTrainBatch = trainingDelay; // reset the counter
				finishTrainingBatch();	// 

				if (storedProcessedBatchesNumber == 0) {
					training = false; // TODO: maybe incorrect since someone could tell me to test a model while i haven't gotten all the batches from cpu yet
					// TODO set Future to done / send more batches to cpu for processing
				}
			}
		}
		else if (testing) {
			// TODO
		}
		// else if (storedProcessedBatchesNumber != 0) {
		// 	training = true;
		// 	--ticksToTrainBatch;

		// 	if (trainingDelay == 0) { 
		// 		gpu.finishTrainingBatch();

		// 		if (gpu.getProcessedBatchesNum() == 0) {
		// 			// TODO set Future to done
		// 		}
		// 	}
		// }
	}


	public void gotModelEvent(Event<Model> modelEvent) {
		// TODO: put aside to wait for ticks
		modelEventsQueue.add(modelEvent);
	}


	// public void gotModelToTest(TestModelEvent testModelEvent) {
	// 	// TODO: put aside to wait for ticks
	// }


	/**
	 * @return The amount of vRAM the GPU has
	 */
	// public byte getVRAM() {
	// 	return vRAM;
	// }

	
	/**
	 * @return Number of processed batches currently in training
	 */
	// public int getProcessedBatchesNum() {
	// 	return storedProcessedBatchesNumber;
	// }


	/**
	 * @inv processedBatchesNum >= 0
	 */
	public void finishTrainingBatch() {
		--storedProcessedBatchesNumber;
	}
}