package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.LinkedList;
import java.util.jar.Attributes.Name;

import javax.print.event.PrintJobListener;

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
    private int ticksToCompletion;
	// private static int[] processing = calcProcessingTicks();

	// private static int[] calcProcessingTicks() {
	// 	int[] arr = new int[18];
	// 	for () { 

	// 	}
	// }

    //  public CPU(int cores,Cluster cluster){
    //     this.cores=cores;
    //     this.data=new LinkedList<>();
    //     this.cluster=cluster;
    //     ticksToCompletion=0;


    //  }


	// region for serialization from json
	public CPU(int _cores) {
		cores = _cores;
        data = new LinkedList<>(); // TODO: make thread-safe?
        cluster = Cluster.getInstance();
        cluster.registerCPU(this);
        ticksToCompletion=0;
	}
	// endregion for serialization from json

    
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

        ticksToCompletion+=calculateProcessingTime(toAdd.getData().getType());

        synchronized(System.out){
            System.out.println("CPU with "+cores+" Cores received batch" + 
                "\nindex "+toAdd.getIndex() +
                "\nticksToCompletion=" + ticksToCompletion +
                "\ntype=" + toAdd.getData().getType());
        }

    }


	public void tickCallback() {
        
        if(! isEmpty()) {
            // synchronized (System.out) {
            //     System.out.println("\nentered !isEmpty()\n");
            // }

            --ticksToCompletion;

            

            synchronized(System.out) {
                System.out.println("\n[*] CPU cores=" + cores +
                    "\nticksToCompetion=" + ticksToCompletion + 
                    "\nindex=" + ((LinkedList<DataBatch>) data).peekFirst().getIndex() +
                    "\n");
            }
			DataBatch batch = ((LinkedList<DataBatch>)data).peek();

			if (! batch.isInProcessing()) {
				batch.setStartProcessing(calculateProcessingTime(batch.getData().getType()));
			}

			if(batch.process()){
				cluster.sendProcessedBatchToTraining(removeBatch());

                synchronized (System.out) {
                    System.out.println("\nentered if(batch.process())\n");
                }
			}
			
		}
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
    // public void process(){
    //     if(!((LinkedList<DataBatch>) data).isEmpty()){
    //             --ticksToCompletion;
    

    //             ((LinkedList<DataBatch>) data).peekFirst().process();

    //             if(isCurrentBatchReady()) {
    //                 cluster.sendProcessedBatchToTraining(((LinkedList<DataBatch>) data).pollFirst());
    //             }
                
                

    //     }
    // }


	/**
	 * @return True if CPU is ready for batches, False otherwise
	 */
    public boolean isEmpty(){
        return data.isEmpty();
    }

    public boolean isCurrentBatchReady(){
        return ((LinkedList<DataBatch>) data).getFirst().isProcessed();
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

}
