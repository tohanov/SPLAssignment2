package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import bgu.spl.mics.MicroService;
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
    // public StudentService(String name) {
    //     super(name);
    //     // TODO Implement this
    // }

    @Override
    protected void initialize() {
        // TODO Implement this

    }

    public Student getStudent() {
        // TODO: change
        return null;
    }


	
	// region for serialization from json
	
	Student student;
	LinkedList<Model> models;

	public StudentService(Map<String,Object> _student) {
		super((String)_student.get("name"));

		student = new Student(
			(String)_student.get("name"),
			(String)_student.get("department"),
			(String)_student.get("status")
		);

		ArrayList<Map> _models = (ArrayList<Map>)_student.get("models");
		models = new LinkedList<>();
		
		for (Map model : _models)	{
			models.addLast(new Model(model, student));
		}
	}

	// endregion for serialization from json
}
