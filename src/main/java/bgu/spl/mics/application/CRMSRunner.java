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

		String inputFilePath = "C:\\Users\\SB\\Desktop\\SPL\\Assignment 2\\example_input.json";
		String outputFilePath = inputFilePath.substring(0, inputFilePath.lastIndexOf('\\') + 1) + "output.json";

		// read config file
		DeserializedJsonParser parser = deserializeConfigFile(inputFilePath); // TODO: remove debug line
		
		if (args.length >= 2) {
			deserializeConfigFile(args[1]); // TODO: can assume correctness of input?
		}

		// handle a possibility of incorrect number of arguments?
		
		// Start threads
		for (MicroService microService : parser.getMicroServices()) {
			new Thread(microService).start();
		}
		Thread timeServiceThread = new Thread(parser.getTimeService());
		timeServiceThread.start();

		// join with TimeService
		// terminate threads
		try {
			timeServiceThread.join();
		} catch (Exception e) { synchronized (System.out) { e.printStackTrace(); } }

		// output a json file form statistics object of cluster
		serializeOutputFile(outputFilePath);
    }


	private static DeserializedJsonParser deserializeConfigFile(String configFilePath) {
		// Gson gson = new Gson();
		// JsonReader reader = new JsonReader(new FileReader(filename));
		// List<Review> data = gson.fromJson(reader, REVIEW_TYPE); //
		try (Reader reader = Files.newBufferedReader(Paths.get(configFilePath))) {
			Gson gson = new Gson();
		
			// create a reader
			// Reader reader = Files.newBufferedReader(Paths.get(configFilePath));
		
			// convert JSON string to Useclr object
			LinkedTreeMap deserializedJson = gson.fromJson(reader, LinkedTreeMap.class);
			// System.out.println(deserializedJson); // TODO: remove debug line

			return new DeserializedJsonParser(deserializedJson);
		
		} catch (Exception ex) {
			synchronized (System.out) { ex.printStackTrace(); }
		}

		return null;
	}

	private static void serializeOutputFile(String outputFilePath) {
		// TODO: output a json file form statistics object of cluster
		try (Writer writer = Files.newBufferedWriter(Paths.get(outputFilePath))) {
			Gson gson = new Gson();
			writer.write(gson.toJson(Cluster.getInstance().getStatistics(), LinkedTreeMap.class));
		}
		catch (Exception ex) {
			synchronized (System.out) { ex.printStackTrace(); }
		}
	}
}
