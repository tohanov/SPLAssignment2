package bgu.spl.mics.application;

import bgu.spl.mics.Event;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.DeserializedJsonParser;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleBroadcastListenerService;

import java.io.Console;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {

		String inputFilePath = "C:\\Users\\USER\\OneDrive\\Desktop\\SPL\\SPLAssignment2\\example_input.json"; // TODO: remove
		String outputFilePath = inputFilePath.substring(0, inputFilePath.lastIndexOf('\\') + 1) + "output.json"; // TODO: move to after reading from args

		// read config file
		DeserializedJsonParser parser = deserializeConfigFile(inputFilePath); // TODO: remove debug line
		
		if (args.length >= 2) {
			deserializeConfigFile(args[1]); // TODO: can assume correctness of input?
		}

		// handle a possibility of incorrect number of arguments?
		
		// Start threads
		for (MicroService microService : parser.getMicroServices()) {
			synchronized (microService) {
				new Thread(microService).start();
				try { microService.wait(); }
				catch (InterruptedException ie) { }
			}
		}

		Thread timeServiceThread = new Thread(parser.getTimeService());
		timeServiceThread.start();

		// join with TimeService
		// terminate threads
		try {
			timeServiceThread.join();
		}
		catch (InterruptedException e) { 
			synchronizedPrintStackTrace(e); 
		}

		// TODO: go over created threads and interrupt each
		// for (MicroService microService : parser.getMicroServices()) {
		// 	new Thread(microService).start();
		// }

		// output a json file form statistics object of cluster
		serializeOutputFile(outputFilePath);
    }


	private static DeserializedJsonParser deserializeConfigFile(String configFilePath) {
		
		try (Reader reader = Files.newBufferedReader(Paths.get(configFilePath))) {
			return new DeserializedJsonParser( new Gson().fromJson(reader, LinkedTreeMap.class) );
		}
		catch (Exception ex) {
			synchronizedPrintStackTrace(ex); // TODO: remove??
		}

		return null;
	}

	private static void serializeOutputFile(String outputFilePath) {
		// TODO: output a json file form statistics object of cluster

		try (Writer writer = Files.newBufferedWriter(Paths.get(outputFilePath))) {
			writer.write(new Gson().toJson( Cluster.getInstance().getStatistics(), LinkedTreeMap.class) );
		}
		catch (Exception ex) {
			synchronizedPrintStackTrace(ex); 
		} // TODO: remove??
	}


	public static void synchronizedPrintStackTrace(Exception exception) {
		synchronized (System.out) { exception.printStackTrace(); }
	}
}
