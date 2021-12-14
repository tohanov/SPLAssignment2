package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.LinkedList;

import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Data.Type;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    private int cores;
    private Collection<DataBatch> data;
    private Cluster cluster;
	// private static int[] processing = calcProcessingTicks();

	// private static int[] calcProcessingTicks() {
	// 	int[] arr = new int[18];
	// 	for () { 

	// 	}
	// }

     public CPU(int cores,Cluster cluster){
        this.cores=cores;
        this.data=new LinkedList<>();
        this.cluster=cluster;

     }

    
	/**
     * @param type Type 
     * @pre type==Data.Type.Images || type==Data.Type.Images || type==Data.Type.Images 
     *  
     */
    int calculateProcessingTime(Data.Type type){
        
        if(type==Type.Images)
            return 32/cores*4;
        else if(type==Type.Text)
            return 32/cores*2;
        else // type==Type.Tabular
            return 32/cores;
    }

    /**
     * 
     * @param toAdd!=null
     * 
  	
	/**
     * 
     * @param toAdd The batch to add
     * @pre toAdd!=null
     * @post data.last()=toAdd
     */
    public void addBatch(DataBatch toAdd){
        this.data.add(toAdd);

    }

    /**
     * @post data.size=@pre databatch.size()-1
     */
    public DataBatch removeBatch(){
        return ((LinkedList<DataBatch>) data).removeFirst();
    }


	/**
     * @pre  data.getFirst().getData().getData().processed <  data.getFirst().getData().size
     * @post data.getData().processed= @pre data.getData().processed + 1000 / calculateProcessingTime(dataType) 
     */
    public void process(){
        if(!((LinkedList<DataBatch>) data).isEmpty()){
                ((LinkedList<DataBatch>) data).peekFirst().process();

                if(isCurrentBatchReady())
                cluster.sendProcessedBatchToTraining(((LinkedList<DataBatch>) data).pollFirst());
        }
    }


	/**
	 * @return True if CPU is ready for batches, False otherwise
	 */
    public boolean isEmpty(){
        return data.isEmpty();
    }

    public boolean isCurrentBatchReady(){
        return ((LinkedList<DataBatch>) data).getFirst().isFirstBatchProcessed();
    }


	public int getCores() {
		return cores;
	}


	// region for serialization from json
	public CPU(int _cores) {
		cores = _cores;
        data = new LinkedList<>(); // TODO: make thread-safe?
        cluster = Cluster.getInstance();
	}
	// endregion for serialization from json


	public void tickCallback() {
		
        // if(isEmpty()){
        //     DataBatch batch=cluster.getBatchToProcess();


        // }
        
        
        if(! isEmpty()) {
			DataBatch batch = ((LinkedList<DataBatch>)data).peek();

			if (batch.isInProcessing()) {
				batch.setStartProcessing(calculateProcessingTime(batch.getData().getType()));
			}

			if(batch.process()){
				removeBatch();
				cluster.sendProcessedBatchToTraining(batch);
			}
			
		}
	}

    public Cluster getCluster(){
        return cluster;
    }

}
