package bgu.spl.mics.application;

import bgu.spl.mics.Event;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleBroadcastListenerService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {
        System.out.println("Hello World!");

       PublishResultsEvent a=new PublishResultsEvent<>(null); 
       ExampleEvent b=new ExampleEvent("idan");
       System.out.println(PublishResultsEvent.class);
        if (args.length >= 2) {
			parseConfigFile(args[1]); // TODO: can assume correctness of input?
		}

		// Start threads
		//join with TimeService
		//termiate threads



	// 	PublishResultsEvent a=new PublishResultsEvent<>(); 
    //    ExampleEvent b=new ExampleEvent("idan");
    //    System.out.println(PublishResultsEvent.class);
    }

	private static void parseConfigFile(String configFilePath) {
		Gson gson = new Gson();
		// JsonReader reader = new JsonReader(new FileReader(filename));
		// List<Review> data = gson.fromJson(reader, REVIEW_TYPE); // 
	}
}
