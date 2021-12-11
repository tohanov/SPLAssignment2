package bgu.spl.mics.application.objects;

import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;

import com.google.gson.internal.LinkedTreeMap;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.services.CPUService;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.services.GPUService;
import bgu.spl.mics.application.services.StudentService;
import bgu.spl.mics.application.services.TimeService;

public class DeserializedJsonParser {
	
	// LinkedList<StudentService> students;
	// LinkedList<GPUService> gpus;
	// LinkedList<CPUService> cpus;
	// LinkedList<ConferenceService> conferences;
	TimeService timeService;
	LinkedList<MicroService> microServices;


	public DeserializedJsonParser(LinkedTreeMap _deserializedJson) {
		this(
			(ArrayList<Map<String,Object>>)_deserializedJson.get("Students"),
			(ArrayList<String>)_deserializedJson.get("GPUS"),
			(ArrayList<Double>)_deserializedJson.get("CPUS"),
			(ArrayList<Map<String,Object>>)_deserializedJson.get("Conferences"),
			((Double)_deserializedJson.get("TickTime")).intValue(),
			((Double)_deserializedJson.get("Duration")).intValue()
		);
	}


	public DeserializedJsonParser(ArrayList<Map<String,Object>> _students, ArrayList<String> _gpus, ArrayList<Double> _cpus, 
		ArrayList<Map<String,Object>> _conferences, int _tickTime, int _programDuration) {

		microServices = new LinkedList<>();

		for ( Map<String,Object> student : _students ) {
			microServices.addLast( new StudentService(student) );
		}

		for ( String gpu : _gpus ) {
			microServices.addLast( new GPUService(gpu) );
		}

		for ( double cpu : _cpus ) {
			microServices.addLast( new CPUService((int)cpu) );
		}

		for ( Map<String,Object> conference : _conferences) {
			microServices.addLast( new ConferenceService(conference) );
		}

		microServices.addLast(new TimeService(_programDuration, _tickTime));
	}


	public LinkedList<MicroService> getMicroServices() {
		return microServices;
	}

	public TimeService getTimeService() {
		return timeService;
	}

	// private void parse(Iterable<Object> _iterable, LinkedList<Object> _newContainer, Callable<Object> _constructor) {
	// 	for (Object obj : _iterable) {
	// 		_newContainer.addLast(_constructor.call(obj));
	// 	}
	// }


	// @Override
	// public String toString() {
	// 	return (students).toString() + 
	// 	(gpus).toString() + 
	// 	(cpus).toString() + 
	// 	(conferences).toString() + 
	// 	(timeService).toString();
	// }
}
