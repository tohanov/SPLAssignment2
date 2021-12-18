package bgu.spl.mics.application.objects;

import java.util.ArrayDeque;
import java.util.Collection;

import bgu.spl.mics.application.objects.Data.Type;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    private int cores;
    private Collection<DataBatch> data;
    private Cluster cluster;
    private int ticksToCompletion;
    private int cpuTimeUsed;
    private int totalBatchesProcessed;


	// region for serialization from json
	public CPU(int _cores) {
		cores = _cores;
		data = new ArrayDeque<>();
		cluster = Cluster.getInstance();
		cluster.registerCPU(this);
		ticksToCompletion = 0;
		cpuTimeUsed = 0;
		totalBatchesProcessed = 0;
	}
	// endregion for serialization from json

    
	/**
     * @param type Type 
     * @pre type==Data.Type.Images || type==Data.Type.Images || type==Data.Type.Images 
     *  
     */
	int calculateProcessingTime(Data.Type type) {
        
		if (type == Type.Images)
			return 32 / cores * 4;
		else if (type == Type.Text)
			return 32 / cores * 2;
		else // type==Type.Tabular
			return 32 / cores;
    }


	public void tickCallback() {
        if( ! isEmpty()) {
            // synchronized (System.out) {
            //     System.out.println("\nentered !isEmpty()\n");
            // }

            --ticksToCompletion;
            ++cpuTimeUsed;

            // synchronized(System.out) {
            //     System.out.println("\n[*] CPU cores=" + cores +
            //         "\nticksToCompetion=" + ticksToCompletion + 
            //         "\nindex=" + ((ArrayDeque<DataBatch>) data).peekFirst().getIndex() +
            //         "\n");
            // }
            DataBatch batch;

			synchronized (data) {
			    batch = ((ArrayDeque<DataBatch>)data).peek(); 
            }

			if ( ! batch.isInProcessing()) {
				batch.setStartProcessing(calculateProcessingTime(batch.getData().getType()));
			}

			if (batch.process()) {
				++totalBatchesProcessed;
				cluster.sendProcessedBatchToTraining(removeBatch());    

                // synchronized (System.out) {
                //     System.out.println("\nentered if(batch.process())\n");
                // }
			}
		}
	}


	/**
     * 
     * @param toAdd The batch to add
     * @pre toAdd!=null
     * @post data.last()=toAdd
     */
    public void addBatch(DataBatch toAdd){
        synchronized(data){
            this.data.add(toAdd);
        }

		ticksToCompletion += calculateProcessingTime(toAdd.getData().getType());

        // synchronized(System.out){
        //     System.out.println("CPU with "+cores+" Cores received batch" + 
        //         "\nindex "+toAdd.getIndex() +
        //         "\nticksToCompletion=" + ticksToCompletion +
        //         "\ntype=" + toAdd.getData().getType());
        // }
    }


    /**
     * @post data.size=@pre databatch.size()-1
     */
    public DataBatch removeBatch(){
        DataBatch removed;

		synchronized (data) {
			removed = ((ArrayDeque<DataBatch>) data).removeFirst();
		}

		return removed;
    }


	/**
	 * @return True if CPU is ready for batches, False otherwise
	 */
    public boolean isEmpty(){
        return data.isEmpty();
    }


    public boolean isCurrentBatchReady(){
		return ((ArrayDeque<DataBatch>) data).getFirst().isProcessed();
    }


	public int getCores() {
		return cores;
	}


    public Cluster getCluster(){
        return cluster;
    }


    public int getTickToCompletion(){
        return ticksToCompletion;
    }


    public void updateTotalCPUTimeUsed() {
        cluster.updateTotalCPUTimeUsed(cpuTimeUsed);
    }


    public void updateTotalBatchesProcessed(){
        cluster.updateTotalBatchesProcessed(totalBatchesProcessed);
    }
}
