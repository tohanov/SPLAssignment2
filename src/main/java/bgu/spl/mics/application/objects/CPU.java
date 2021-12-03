package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.objects.Data.Type;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    private int cores;
    private DataBatch dataBatch;
    private Cluster cluster;

    public CPU(int cores,DataBatch dataBatch,Cluster cluster){
        this.cores=cores;
        this.dataBatch=dataBatch;
        this.cluster=cluster;

    }

    private int calculateProcessingTime(Data.Type type){
        
        if(type==Type.Images)
            return 32/cores*4;
        else if(type==Type.Text)
            return 32/cores*2;
        else // type==Type.Tabular
            return 32/cores;
    }

    public void setBatch(DataBatch dataBatch){
        this.dataBatch=dataBatch;

    }

    public void processSample(){
        
        dataBatch.getData().increaseNumOfProcessedSamples();

    }





}
