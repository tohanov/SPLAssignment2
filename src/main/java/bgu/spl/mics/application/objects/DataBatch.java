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
	private boolean isFresh;

    public DataBatch(Data data, int start_index, GPU _ownerGpu){
        this.data=data;
        this.start_index=start_index;
		this.ownerGpu = _ownerGpu;
		isFresh = true;
    }


	public void setStartProcessing(int tickCount) {
		processingTickCount = tickCount;
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
    //     processed+=number_of_processed_samples_to_add;

    // // extreme case    
    //     if(processed>size)
    //         processed=size;

		return --processingTickCount == 0;
    }


	public boolean getIsFresh() {
		return isFresh;
	}

}
