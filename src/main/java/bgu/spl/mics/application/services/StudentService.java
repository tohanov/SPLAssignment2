package bgu.spl.mics.application.services;

import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.CRMSRunner;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
//import bgu.spl.mics.application.messages.SystemStartupBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.application.objects.Model.Status;


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

	// region for serialization from json
	private Student student;
	private ArrayList<Model> models;
	private Iterator<Model> modelsIterator;
	private Future<Model> waitingFuture;

	public StudentService(Map<String,Object> _student) {
		super((String)_student.get("name"));

		student = new Student(
			(String)_student.get("name"),
			(String)_student.get("department"),
			(String)_student.get("status")
		);

		models = new ArrayList<>();
		ArrayList<Map<String,Object>> _models = (ArrayList<Map<String,Object>>)_student.get("models");
		
		for (Map<String,Object> model : _models)	{
			models.add(new Model(model, student));
		}

		waitingFuture = null;
		modelsIterator = models.iterator();
		// currentModelNumber=0;
	}
	// endregion for serialization from json


	// int currentModelNumber;


    @Override
    protected void initialize() {
	
		subscribeBroadcast(
			TickBroadcast.class, tickBroadcast -> {
				if (waitingFuture != null) {
					if (waitingFuture.isDone()) {
						Model currentModel = waitingFuture.get();
						if (currentModel.getStatus() == Status.Trained) {
							
							student.addTrainedModel(currentModel);
							
							/* currentModel =  */sendEvent(new TestModelEvent(currentModel)).get(); // TODO: check what happens if returns null (if could happen)

							if (currentModel.getResults() == Model.Results.Good) {
								sendEvent(new PublishResultsEvent(currentModel));						
							}

							waitingFuture=null;

							if (modelsIterator.hasNext()) { // waitingFuture == null
								currentModel = modelsIterator.next();
								waitingFuture = sendEvent(new TrainModelEvent(currentModel));
							
								//TODO: remove debug
								synchronized(System.out){
									System.out.println("(if) " + getName()+" Sending model "+currentModel.getName());
								}

							}
						
						}
					}
				}
				else if (modelsIterator.hasNext()) { // waitingFuture == null
					Model currentModel=modelsIterator.next();
					waitingFuture = sendEvent(new TrainModelEvent(currentModel));
					//TODO: remove debug
					synchronized(System.out){
						System.out.println("(else if) " + getName()+" Sending model "+currentModel.getName());
					}
				}

				if (tickBroadcast.isLast()) {

					
					// TODO: remove debug block
				//	CRMSRunner.synchronizedSyso("student "+getName()+" publications= "+student.getPublications()+" papers read= "+student.getPapersRead()+"\n");
					synchronized (System.out) {
						System.out.println("[*] " + getName() + ": got LAST tick");
					}

					terminate();
				}
			}
		);

		subscribeBroadcast(PublishConferenceBroadcast.class, message->{ // TODO: move into student object's function
			for(Model m: message.getSuccessfulModels()){
				if(m.getStudent()==student)
					student.updatePublications();
				else
					student.updatePapersRead();
			}

		});
    }


	public Student getStudent() {
		return student;
	}


	public ArrayList<Model> getModels() {
		return models;
	}



			// if(currentModel.getStatus()==(Model.Status.PreTrained)){
			// 	currentModel.changeStatus(Model.Status.Training);

			// 	// TODO: remove debug
			// 	synchronized(System.out){
			// 		System.out.println(getName()+" Sending model "+currentModel.getName());
			// 	}

			// 	Future<Model> f=sendEvent(new TrainModelEvent(currentModel));
			// 	currentModel=f.get();
			// 	if (Thread.currentThread().isInterrupted()) {
			// 		break;
			// 	}				
			// }
			// else if(currentModel.getStatus()==(Model.Status.Trained)){
			// 	Future<Model> f=sendEvent(new TestModelEvent(currentModel));
			// 	currentModel=f.get();
			// 	if (Thread.currentThread().isInterrupted()) {
			// 		break;
			// 	}
			// 	if(currentModel.getResults()==(Model.Results.Good))
			// 		sendEvent(new PublishResultsEvent(currentModel));
				
			// 	currentModelNumber++;
			// }


    //    subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
			
	// 		if(currentModelNumber < models.size()){
				
	// 			Model currentModel=models.get(currentModelNumber);

	// 			if(currentModel.getStatus()==(Model.Status.PreTrained)){
	// 				currentModel.changeStatus(Model.Status.Training);
	// 				Future<Model> f=sendEvent(new TrainModelEvent(currentModel));

	// 				// TODO: remove debug
	// 				synchronized(System.out){
	// 					System.out.println(getName()+" Sending model "+currentModel.getName());
	// 				}
	// 				currentModel=f.get();
					
	// 			}
	// 			else if(currentModel.getStatus()==(Model.Status.Trained)){
	// 				Future<Model> f=sendEvent(new TestModelEvent(currentModel));
	// 				currentModel=f.get();

	// 				if(currentModel.getResults()==(Model.Results.Good))
	// 					sendEvent(new PublishResultsEvent(currentModel));
					
	// 				currentModelNumber++;

	// 			}
	// 		}

	// 		if (tickBroadcast.isLast()) {

	// 			// TODO: remove debug block
	// 			synchronized (System.out) {
	// 				System.out.println("[*] " + getName() + ": got LAST tick");
	// 			}

	// 			terminate();
	// 		}
	// 	});


    // public Student getStudent() {
    //     // TODO: change
    //     return null;
    // }
}