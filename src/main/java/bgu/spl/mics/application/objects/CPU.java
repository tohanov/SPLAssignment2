package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.LinkedList;

import bgu.spl.mics.application.objects.Data.Type;

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
     * 
     * @param type==Data.Type.Images || @param type==Data.Type.Images || @param type==Data.Type.Images 
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
     * @post dataBatch.last()=toAdd
     */
    public void addBatch(DataBatch toAdd){
        this.dataBatch.add(toAdd);

    }

    /**
     * @post dataBatch.size=@pre databatch.size()-1
     */
    public void removeBatch(){
        ((LinkedList<DataBatch>) dataBatch).removeLast();
    }

    /**
     * @pre  dataBatch.getFirst().getData().getData().processed <  dataBatch.getFirst().getData().size
     * @post dataBatch.getData().processed= @pre dataBatch.getData().processed + 1
     */
    public void processSample(){

        ((LinkedList<DataBatch>) dataBatch).getFirst().getData().increaseNumOfProcessedSamples();
       
    }

    public boolean isReady(){
        return dataBatch.isEmpty();
    }







}
