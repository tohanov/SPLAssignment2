package bgu.spl.mics.application.services;

import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {

	int currentModelNumber;			
	
     public StudentService(String name) {
        super(name);
        currentModelNumber=0; 
		
     }

    @Override
    protected void initialize() {
       subscribeBroadcast(TickBroadcast.class, message->{
			
			if(currentModelNumber<models.size()){
				
				Model currentModel=models.get(currentModelNumber);

				if(currentModel.getStatus().equals(Model.Status.PreTrained)){
					currentModel.changeStatus(Model.Status.Training);
					Future<Model> f=sendEvent(new TrainModelEvent(currentModel));
					currentModel=f.get();
					
				}

				else if(currentModel.getStatus().equals(Model.Status.Trained)){
					Future<Model> f=sendEvent(new TestModelEvent(currentModel));
					currentModel=f.get();

					if(currentModel.getResults().equals(Model.Results.Good))
						sendEvent(new PublishResultsEvent(currentModel));

					currentModelNumber++;

				}

				

			}

			

			
		});

		subscribeBroadcast(PublishConferenceBroadcast.class, message->{//TODO: implement

		});



		



    }

    public Student getStudent() {
        // TODO: change
        return null;
    }


	
	// region for serialization from json
	Student student;
	ArrayList<Model> models;

	public StudentService(Map<String,Object> _student) {
		super((String)_student.get("name"));

		student = new Student(
			(String)_student.get("name"),
			(String)_student.get("department"),
			(String)_student.get("status")
		);

		ArrayList<Map> _models = (ArrayList<Map>)_student.get("models");
		models = new ArrayList<>();
		
		for (Map model : _models)	{
			models.add(models.size(), new Model(model, student));
		}
	}
	// endregion for serialization from json
}