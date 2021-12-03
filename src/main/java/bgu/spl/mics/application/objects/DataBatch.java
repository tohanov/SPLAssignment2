package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    private Data data;
    private int processed;
    private int size;

    public DataBatch(Data data, int processed, int size){
        this.data=data;
        this.processed=processed;
        this.size=size;

    }

    protected Data getData(){
        return data;

    }

}
