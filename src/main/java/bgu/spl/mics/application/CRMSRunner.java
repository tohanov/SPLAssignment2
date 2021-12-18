package bgu.spl.mics.application;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.DeserializedJson;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.services.StudentService;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.internal.LinkedTreeMap;


/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) throws Exception {
		// read config file
		DeserializedJson system = deserializeConfigFile(args[0]);
		
		startUp(system);

		String outputFilePath = outputPathFromArgs(args);

		// output a json file with statistics
		serializeOutputFile(outputFilePath, system);
    }


	private static String outputPathFromArgs(String[] args) throws Exception {
		String outputFilePath;
		String inputFilePath = args[0];

		if (args.length >= 2) {
			outputFilePath = args[1];
		}
		else {
			outputFilePath =
				inputFilePath.substring(0, inputFilePath.lastIndexOf(File.separator) + 1)
				+ "output.json";
		}

		return outputFilePath;
	}


	private static void startUp(DeserializedJson system) {
		// Start up threads	of microservices that aren't the TimeService
		for (MicroService microService : system.getMicroServices()) {
			synchronized (microService) {
		 		new Thread(microService).start();

				// get notified when the new thread finished initialization
				try { microService.wait(); }
				catch (InterruptedException ie) { synchronizedPrintStackTrace(ie); } // TODO: remove??
			}
		}

		Thread timeServiceThread = new Thread(system.getTimeService());
		timeServiceThread.start();

		// main thread should await TimeService's termination
		// terminate threads
		try {
			timeServiceThread.join();
		}
		catch (InterruptedException e) { 
			synchronizedPrintStackTrace(e); 
		}
	}


	@SuppressWarnings("unchecked")
	private static DeserializedJson deserializeConfigFile(String configFilePath) {
		try (Reader reader = Files.newBufferedReader(Paths.get(configFilePath))) {
			return new DeserializedJson( new Gson().fromJson( reader, LinkedTreeMap.class) );
		}
		catch (Exception ex) {
			synchronizedPrintStackTrace(ex); // TODO: remove??
		}

		return null;
	}


	private static void serializeOutputFile(String outputFilePath, DeserializedJson system) {
		Gson gson = new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.setPrettyPrinting()
			.create();

		try (Writer writer = Files.newBufferedWriter(Paths.get(outputFilePath))) {
			LinkedTreeMap<String,Object> temp = new LinkedTreeMap<>();
			temp.put("students", system.getStudents() );
			temp.put("conferences", system.getConferences() );
			temp.put("statistics", Cluster.getInstance().getStatistics() );
			writer.write(gson.toJson(temp, LinkedTreeMap.class));
		}
		catch (Exception ex) {
			synchronizedPrintStackTrace(ex); // TODO : remove??
		}
	}


	// TODO : remove debug function
	public static void synchronizedPrintStackTrace(Exception exception) {
		synchronized(System.out){
			exception.printStackTrace();
		}
	}


	// TODO : remove debug function
	public static void synchronizedSyso(String output){
		synchronized(System.out){
			System.out.println(output);
		}
	}
}
