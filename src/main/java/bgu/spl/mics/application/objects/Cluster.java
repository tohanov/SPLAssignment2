package bgu.spl.mics.application.objects;

import java.util.Collection;

import com.google.gson.internal.LinkedTreeMap;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	private static Cluster instance = null; // lazy initialization


	// region According to instructions
	Collection<GPU> GPUS;
	Collection<CPU> CPUS;
	// TODO add statistics
	LinkedTreeMap<String,Object> Statistics = new LinkedTreeMap<>();
	// endregion According to instructions


	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		if(instance == null)
			instance = new Cluster();

		return instance;
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
		}
	}


	// for json output
	public LinkedTreeMap<String,Object> getStatistics() {
		return null; // TODO: change to something working
	}
}
