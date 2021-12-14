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
	private byte empyVRAM;
	private int storedProcessedBatchesNumber;
	// private int ticksToTrainBatch;
	// private boolean training;
	// private boolean testing;
	private Queue<DataBatch> processBatches;
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
		processBatches = new ArrayDeque<>();
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

		// if (modelEventsQueue.peek() instanceof TrainModelEvent) {
			
		// }


		// // TODO: treat the case of last tick??

		// // TODO: split to smaller functions (queries + actions)
	}


	public void gotModelEvent(Event<Model> modelEvent) {
		// TODO: put aside to wait for ticks
		modelEventsQueue.add(modelEvent);
	}


	/**
	 * @inv processedBatchesNum >= 0
	 */
	public void finishTrainingBatch() {
		--storedProcessedBatchesNumber;
	}


	// private void sendNextBatches() {
	// 	DataBatch dataBatch;
	// 	cluster.sendBatchesForProcessing(this, dataBatch);
	// }


	public void returnProcessedBatch(DataBatch batch) {
		processBatches.add(batch);
	}
}