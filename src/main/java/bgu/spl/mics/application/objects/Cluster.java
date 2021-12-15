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

	private static Cluster instance = new Cluster();


	// region According to instructions
	private Collection<GPU> GPUS;
	private Collection<CPU> CPUS;
	private HashMap<GPU,LinkedList<DataBatch>> databatchQueues;
	// TODO add statistics
	private LinkedTreeMap<String,Object> Statistics = new LinkedTreeMap<>();
	// endregion According to instructions


	// region
	// private int availableCPUSNumber;
	private ArrayList<ConfrenceInformation> finishedConferences;
	// endregion

	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		return instance;
	}

	private Cluster() {
		GPUS = new ArrayList<>();
		CPUS = new PriorityQueue<>(new Comparator<CPU>() {
			
			@Override
			public int compare(CPU first, CPU second) {
				return first.getTickToCompletion()-second.getTickToCompletion();
			}
			
		});
		

		
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
		CPU minimalWorkCPU=((PriorityQueue<CPU>)CPUS).poll();
		minimalWorkCPU.addBatch(dataBatch);
		CPUS.add(minimalWorkCPU);
	}


	// TODO: implement sending multiple batches at once, according to available vRam
	// and let cluster handle assigning them
	public void sendProcessedBatchToTraining(DataBatch dataBatch) {
		GPU destinationGPU=dataBatch.getOwnerGPU();
		destinationGPU.addTovRAM(dataBatch);
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
    }

	
}