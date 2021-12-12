package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.LinkedList;

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
    private Collection<DataBatch> dataBatch;
    private Cluster cluster;

     public CPU(int cores,Cluster cluster){
        this.cores=cores;
        this.dataBatch=new LinkedList<>();
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
     * @post dataBatch.last()=toAdd
     */
    public void addBatch(DataBatch toAdd){
        this.dataBatch.add(toAdd);

    }

    /**
     * @post dataBatch.size=@pre databatch.size()-1
     */
    public DataBatch removeBatch(){
        return ((LinkedList<DataBatch>) dataBatch).removeFirst();
    }

    

    /**

	/**
     * @pre  dataBatch.getFirst().getData().getData().processed <  dataBatch.getFirst().getData().size
     * @post dataBatch.getData().processed= @pre dataBatch.getData().processed + 1000 / calculateProcessingTime(dataType) 
     */
    public void process(){

        int toAdd=1000/calculateProcessingTime(((LinkedList<DataBatch>) dataBatch).getFirst().getData().getType());
        ((LinkedList<DataBatch>) dataBatch).getFirst().getData().increaseNumOfProcessedSamples(toAdd);
    }


	/**
	 * @return True if CPU is ready for batches, False otherwise
	 */
    public boolean isEmpty(){
        return dataBatch.isEmpty();
    }

    public boolean isCurrentBatchReady(){
        return ((LinkedList<DataBatch>) dataBatch).getFirst().isFirstBatchProcessed();
    }


	// region for serialization from json
	public CPU(int _cores) {
		cores = _cores;
        dataBatch = new LinkedList<>(); // TODO: make thread-safe?
        cluster = Cluster.getInstance();
	}
	// endregion for serialization from json



}
