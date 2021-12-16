package bgu.spl.mics.application.objects;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.PriorityQueue;

import com.google.gson.internal.LinkedTreeMap;

import bgu.spl.mics.application.CRMSRunner;
import bgu.spl.mics.application.objects.Data.Type;
// import bgu.spl.mics.application.objects.DataBatch;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	private static final class InternalSingleton {
		private static final Cluster instance = new Cluster();
	}


	// region According to instructions
	private Collection<GPU> GPUS;
	private Collection<CPU> CPUS;
	private HashMap<Data.Type,PriorityQueue<CPU>> CPUS_PriorityQueues;
	private HashMap<GPU,LinkedList<DataBatch>> databatchQueues;
	// TODO add statistics
	private LinkedTreeMap<String,Object> Statistics = new LinkedTreeMap<>();
	// endregion According to instructions


	// region
	// private int availableCPUSNumber;
	private ArrayList<ConfrenceInformation> finishedConferences;
	private int totalCPUTimeUsed;
	private int totalGPUTimeUsed;
	// endregion

	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		return InternalSingleton.instance;
	}

	private Cluster() {
		GPUS = new ArrayList<>();
		CPUS=new ArrayList<>();
		CPUS_PriorityQueues=new HashMap<Data.Type,PriorityQueue<CPU>>();

		for(Data.Type type: new Data.Type[] {Data.Type.Images, Data.Type.Text, Data.Type.Tabular}){
			CPUS_PriorityQueues.put(type,new PriorityQueue<>(new Comparator<CPU>() {
			
			@Override
			public int compare(CPU first, CPU second) {
				int a =( first.getTickToCompletion()+first.calculateProcessingTime(type) )-
					   ( second.getTickToCompletion() + second.calculateProcessingTime(type) );

				return a;
				
			}
			
		}));
	}

		finishedConferences=new ArrayList<>();
		totalCPUTimeUsed=0;
		totalGPUTimeUsed=0;

		
		// //TODO: check if needed
		// availableCPUS = new LinkedList[6];

		// for (int i = 0; i < 6; ++i) {
		// 	availableCPUS[i] = new LinkedList<>();
		// }


	}


	public Collection<GPU> getGPUs() {
		return GPUS;
	}


	public Collection<CPU> gCPUs() {
		return CPUS;
	}


	public void registerCPU(CPU cpu) {
		synchronized(CPUS) {
			CPUS.add(cpu);

			for(PriorityQueue<CPU> CPU_Queue: CPUS_PriorityQueues.values())
				CPU_Queue.add(cpu);
		}
	}


	public void registerGPU(GPU gpu) {
		synchronized(GPUS) {
			GPUS.add(gpu);
			databatchQueues.put(gpu, new LinkedList<DataBatch>());
		}
	}


	// public int getAvailableCPUSNumber() {
	// 	return availableCPUSNumber;
	// }


	// public void sendBatchForProcessing(DataBatch dataBatch) {
	// 	switch(dataBatch.getData().getType()) {
	// 		case Images:
	// 			for (int i = 5; i >= 0; --i) {
	// 				if ( ! availableCPUS[i].isEmpty()) {
	// 					// --availableCPUSNumber;
	// 					CPU chosenCPU = availableCPUS[i].remove();
	// 					chosenCPU.addBatch( dataBatch );
	// 					break;
	// 				}
	// 			}
	// 		break;
	// 		case Text:
	// 			for (int i = 2, j = 3; i >= 0; --i, ++j) {
	// 				if ( ! availableCPUS[j].isEmpty()) {
	// 					// --availableCPUSNumber;
	// 					CPU chosenCPU = availableCPUS[j].remove();
	// 					chosenCPU.addBatch( dataBatch );
	// 					break;
	// 				}
	// 				else if ( ! availableCPUS[i].isEmpty()) {
	// 					// --availableCPUSNumber;
	// 					CPU chosenCPU = availableCPUS[i].remove();
	// 					chosenCPU.addBatch( dataBatch );
	// 					break;
	// 				}
	// 			}
	// 		break;
	// 		default: // Tabular
	// 			for (int i = 0; i <= 5; ++i) {
	// 				if ( ! availableCPUS[i].isEmpty()) {
	// 					// --availableCPUSNumber;
	// 					CPU chosenCPU = availableCPUS[i].remove();
	// 					chosenCPU.addBatch( dataBatch );
	// 					break;
	// 				}
	// 			}
	// 	}
	// }

	public void sendBatchForProcessing(DataBatch dataBatch) {
		PriorityQueue<CPU> q = CPUS_PriorityQueues.get(dataBatch.getData().getType());
		CPU minimalWorkCPU=q.poll();
		minimalWorkCPU.addBatch(dataBatch);
		q.add(minimalWorkCPU);
	}


	// TODO: implement sending multiple batches at once, according to available vRam
	// and let cluster handle assigning them
	public void sendProcessedBatchToTraining(DataBatch dataBatch) {
		GPU destinationGPU=dataBatch.getOwnerGPU();
		destinationGPU.returnProcessedBatch(dataBatch);
	}


	/*public LinkedList<DataBatch> getBatchToProcess() {
		for(LinkedList)
		
	}*/
	
	// for json output
	public LinkedTreeMap<String,Object> getStatistics() {
		return null; // TODO: change to something working
	}

    public void uploadConferenceInformation(ConfrenceInformation conference) {
		finishedConferences.add(conference);

		//TODO: remove debug
		CRMSRunner.synchronizedSyso(conference.toString());
    }

	public synchronized void updateTotalCPUTimeUsed(int toAdd){
		totalCPUTimeUsed+=toAdd;
	}

	public synchronized void updateTotalGPUTimeUsed(int toAdd){
		totalGPUTimeUsed+=toAdd;
	}
	
	@Override
	public String toString(){
		String output=" cpuTimeUsed= "+totalCPUTimeUsed+"\ngpuTimeUsed= "+totalGPUTimeUsed;
		return output;
	}
}