package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.annotations.Expose;
import com.google.gson.internal.LinkedTreeMap;


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

	public class ClusterStatistics {
		// @Expose private ArrayList<Student> students;			//FIXME
		// @Expose private ArrayList<ConfrenceInformation> conferences;	//FIXME
		@Expose private AtomicInteger cpuTimeUsed;
		@Expose private AtomicInteger gpuTimeUsed;
		@Expose private AtomicInteger batchesProcessed;
		@Expose private double Batches_CPUTime_Ratio;
		@Expose private double Batches_GPUTime_Ratio;
		// @Expose private int modelsTrained;	//FIXME

		public AtomicInteger getCPUTimeUsed(){
			return cpuTimeUsed;
		}
	}


	// region According to instructions
	private Collection<GPU> GPUS;	// FIXME : we're not using GPUS
	private Collection<CPU> CPUS;	// FIXME : we're not using CPUS
	// FIXME: maybe create new object for Statistics and return it during serialization
	private ClusterStatistics Statistics; // FIXME : we're not using statistics correctly
	// endregion According to instructions


	// region Operation
	private HashMap<GPU,LinkedList<DataBatch>> databatchQueues;
	private HashMap<Data.Type,PriorityQueue<CPU>> CPUsMinHeapsByDataType;
	// endregion Operation


	// region For statistics
	// private ArrayList<ConfrenceInformation> Statistics.conferences;
	// private ArrayList<Student> studentsList;
	// private AtomicInteger Statistics.cpuTimeUsed;
	// private AtomicInteger Statistics.gpuTimeUsed;
	// private AtomicInteger totalBatchesProcessed;
	// endregion For statistics


	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		return InternalSingleton.instance;
	}


	private Cluster() {
		GPUS = new ArrayList<>();
		CPUS = new ArrayList<>();
		CPUsMinHeapsByDataType = new HashMap<Data.Type,PriorityQueue<CPU>>();

		initCPUinitCPUHeaps();

		Statistics = new ClusterStatistics();
		// Statistics.conferences = new ArrayList<>();
		// Statistics.students = new ArrayList<>();
		Statistics.cpuTimeUsed = new AtomicInteger(0);
		Statistics.gpuTimeUsed = new AtomicInteger(0);
		Statistics.batchesProcessed = new AtomicInteger(0);
		
	}


	private void initCPUinitCPUHeaps() {
		for(Data.Type type : new Data.Type[]{ Data.Type.Images, Data.Type.Text, Data.Type.Tabular }) {

			CPUsMinHeapsByDataType.put(
				type,
				new PriorityQueue<>(
					new Comparator<CPU>() {
						@Override
						public int compare(CPU first, CPU second) {
							return (first.getTickToCompletion() + first.calculateProcessingTime(type)) -
									(second.getTickToCompletion() + second.calculateProcessingTime(type) );
						}
					}
				)
			);
		}
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

			// add CPU to each minheap
			for(PriorityQueue<CPU> CPU_Queue: CPUsMinHeapsByDataType.values()) {
				CPU_Queue.add(cpu);
			}
		}
	}


	public void registerGPU(GPU gpu) {
		synchronized(GPUS) {
			GPUS.add(gpu);
			databatchQueues.put(gpu, new LinkedList<DataBatch>()); // FIXME : relevant?
		}
	}


	public void sendBatchForProcessing(DataBatch dataBatch) {
		PriorityQueue<CPU> priorityQueueByType = CPUsMinHeapsByDataType.get(dataBatch.getData().getType());
		
		CPU minimalWorkCPU;
		synchronized (priorityQueueByType) {
			minimalWorkCPU = priorityQueueByType.poll();
		}

		minimalWorkCPU.addBatch(dataBatch);

		synchronized (priorityQueueByType) {
			priorityQueueByType.add(minimalWorkCPU);
		}
	}


	// and let cluster handle assigning them
	public void sendProcessedBatchToTraining(DataBatch dataBatch) {
		GPU destinationGPU = dataBatch.getOwnerGPU();
		destinationGPU.returnProcessedBatch(dataBatch);
	}

	
	// for json output
	// FIXME : use Statistics somehow
	public ClusterStatistics getStatistics() {
		Statistics.Batches_CPUTime_Ratio = ((double) Statistics.batchesProcessed.get()) / Statistics.cpuTimeUsed.get();
		Statistics.Batches_GPUTime_Ratio = ((double) Statistics.batchesProcessed.get()) / Statistics.gpuTimeUsed.get();

		return Statistics;
	}


    // public void uploadConferenceInformation(ConfrenceInformation conference) {
	// 	synchronized(Statistics.conferences){
	// 		Statistics.conferences.add(conference);			
	// 	}

	// 	//TODO: remove debug
	// 	//CRMSRunner.synchronizedSyso(conference.toString());
    // }


	public void increaseTotalCPUTimeUsed(int toAdd){
		int oldValue;

		do{
			oldValue=Statistics.cpuTimeUsed.get();
		} while ( ! Statistics.cpuTimeUsed.compareAndSet(oldValue, oldValue + toAdd));
	}


	public void increaseTotalGPUTimeUsed(int toAdd) {
		int oldValue;

		do {
			oldValue = Statistics.gpuTimeUsed.get();
		} while ( ! Statistics.gpuTimeUsed.compareAndSet(oldValue, oldValue + toAdd));
	}

	
	public void increaseTotalBatchesProcessed(int toAdd) {
		int oldValue;

		do {
			oldValue = Statistics.batchesProcessed.get();
		} while ( ! Statistics.batchesProcessed.compareAndSet(oldValue, oldValue + toAdd));
	}

	
	//FIXME: should be synched??
	// public /* synchronized */ void registerStudent(Student student) {
	// 	Statistics.students.add(student);
	// }
	

	// TODO : remove??
	@Override
	public String toString() {
		String output = 
			"[*] cpuTimeUsed= " + Statistics.cpuTimeUsed +
			"\ngpuTimeUsed= " + Statistics.gpuTimeUsed;

		return output;
	}
}