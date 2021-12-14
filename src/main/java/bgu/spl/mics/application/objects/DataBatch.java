package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    private Data data;
    private int start_index;
	private GPU ownerGpu;
	// private int processed_index;
	// private int end_index;
	private int processingTickCount;
	private int trainingTickCount;
	private boolean inProcessing;
	private boolean inTraining;

    public DataBatch(Data data, int start_index, GPU _ownerGpu){
        this.data=data;
        this.start_index=start_index;
		this.ownerGpu = _ownerGpu;
		inProcessing = true;
		
    }


	public void setStartProcessing(int tickCount) {
		processingTickCount = tickCount;
	}

	public void setStartTraining(int tickCount) {
		trainingTickCount = tickCount;
	}


    public Data getData(){
        return data;

    }

    public boolean isFirstBatchProcessed(){
        return data.isProcessed();
    }


	public GPU getOwnerGPU() {
		return ownerGpu;
	}


	public boolean process() {
   		return --processingTickCount == 0;
    }

	public boolean train() {
		return --trainingTickCount == 0;
	}
	

	public boolean isInProcessing() {
		return inProcessing;
	}

	public boolean isInTraining(){
		return inTraining;
	}

}
