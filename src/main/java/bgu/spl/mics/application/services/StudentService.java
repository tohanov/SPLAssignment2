package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.Iterator;
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


	@SuppressWarnings("unchecked")
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
	}
	// endregion for serialization from json


    @Override
    protected void initialize() {
		subscribeBroadcast(
			TickBroadcast.class, tickBroadcast -> {
				if (waitingFuture != null) {
					if (waitingFuture.isDone()) {
						Model currentModel = waitingFuture.get();
						if (currentModel.getStatus() == Status.Trained) {
							student.addTrainedModel(currentModel);
							
							sendEvent(new TestModelEvent(currentModel)).get();

							if (currentModel.getResults() == Model.Results.Good) {
								sendEvent(new PublishResultsEvent(currentModel));						
							}

							waitingFuture = null;

							if (modelsIterator.hasNext()) { // waitingFuture == null
								currentModel = modelsIterator.next();
								waitingFuture = sendEvent(new TrainModelEvent(currentModel));
							
								//TODO: remove debug
								synchronized(System.out){
									System.out.println(getName()+" Sending model "+currentModel.getName());
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
						System.out.println("(first model sent) " + getName()+" Sending model "+currentModel.getName());
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

		// FIXME: move into student object's function
		subscribeBroadcast(PublishConferenceBroadcast.class, message -> {
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
}