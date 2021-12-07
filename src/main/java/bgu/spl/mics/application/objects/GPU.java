package bgu.spl.mics.application.objects;

import java.util.Collection;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.services.GPUService;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080}


	// region According to assignment instructions
    private Type type;
    private Cluster cluster;
    private Model model;
	// endregion According to assignment instructions


	// region Added fields
    private GPUService service;
	// private Collection<DataBatch> processedBatches;
	private final byte vRAM;
	private int processedBatchesNum;
	// endregion Added fields

	/**
	 * @param _name Name given to the GPU
	 * @param _type GPU type
	 * 
	 * @post vRAM is in {8, 16, 32}
	 * @post processedBatchesNum == 0
	 * @post MessageBusImpl.getInstance().isRegistered(service) == true
	 * @post service != null
	 */
    public GPU(String _name, Type _type) {
        this.type = _type;
		this.service = new GPUService(_name, this);

		processedBatchesNum = 0;

		switch(type) {
			case GTX1080:
				vRAM = 8;
				break;
			case RTX2080:
				vRAM = 16;
				break;
			default : // RTX3090:
				vRAM = 32;
		}

		// processedBatches = 
    }

    /**
     * @return The type of the GPU
     */
    public Type getType () {
        return type;
    }


	// public void runService() { // TODO remove ?
	// 	service.run();
	// }


	/**
	 * @return Reference to the corresponding GPUService
	 */
	public GPUService getService() {
		return service;
	}


	// public Collection<DataBatch> getProcessedBatches() {
	// 	return processedBatches;
	// }


	/**
	 * @return The amount of vRAM the GPU has
	 */
	public byte getVRAM() {
		return vRAM;
	}

	/**
	 * @return Number of processed batches currently in training
	 */
	public int getProcessedBatchesNum() {
		return processedBatchesNum;
	}


	/**
	 * @inv processedBatchesNum >= 0
	 */
	public void finishTrainingBatch() {
		--processedBatchesNum;
	}

}
