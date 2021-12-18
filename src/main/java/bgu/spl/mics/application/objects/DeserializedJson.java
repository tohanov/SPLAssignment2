package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import com.google.gson.internal.LinkedTreeMap;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.services.CPUService;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.services.GPUService;
import bgu.spl.mics.application.services.StudentService;
import bgu.spl.mics.application.services.TimeService;


public class DeserializedJson {
	TimeService timeService;
	ArrayList<MicroService> microServices;


	@SuppressWarnings("unchecked")
	public DeserializedJson(LinkedTreeMap<String,Object> _deserializedJson) {
		this(
			(ArrayList<Map<String, Object>>) _deserializedJson.get("Students"),
			(ArrayList<String>) _deserializedJson.get("GPUS"),
			(ArrayList<Double>) _deserializedJson.get("CPUS"),
			(ArrayList<Map<String, Object>>) _deserializedJson.get("Conferences"),
			((Double) _deserializedJson.get("TickTime")).intValue(),
			((Double) _deserializedJson.get("Duration")).intValue()
		);
	}


	public DeserializedJson(ArrayList<Map<String,Object>> _students, ArrayList<String> _gpus, ArrayList<Double> _cpus, 
							ArrayList<Map<String,Object>> _conferences, int _tickTime, int _programDuration) {

		microServices = new ArrayList<>();

		for ( String gpu : _gpus ) {
			microServices.add( new GPUService(gpu) );
		}

		for ( double cpu : _cpus ) {
			microServices.add( new CPUService((int)cpu) );
		}

		for ( Map<String,Object> conference : _conferences) {
			microServices.add( new ConferenceService(conference) );
		}

		for ( Map<String,Object> student : _students ) {
			microServices.add( new StudentService(student) );
		}

		timeService = new TimeService(_programDuration, _tickTime);

		Collections.shuffle(microServices); // FIXME : rethink
		
		// TODO : maybe sort students by their model sizes
	}


	public ArrayList<MicroService> getMicroServices() {
		return microServices;
	}


	public TimeService getTimeService() {
		return timeService;
	}
}