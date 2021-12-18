package bgu.spl.mics.application.objects;

import java.util.Map;

import com.google.gson.annotations.Expose;


/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model { // FIXME : remove unneeded/unused methods

	public enum Status {
		PreTrained, 
		Training,
		Trained,
		Tested
	}

	public enum Results {
		None,
		Good,
		Bad
	}
	
    @Expose private String name;
    @Expose private Data data;
	// not exposed on purpose
	private Student student;
	@Expose private Status status;
    @Expose private Results results;


	// region for serialization from json
	public Model(String _name, String _dataType, int _size, Student _student) {
		name = _name;
		data = new Data( Data.typeFromString(_dataType), _size );
		status = Status.PreTrained;
		results = Results.None;
		student = _student;
	}


	public Model(Map<String,Object> _model, Student _student) {
		this(
			(String)_model.get("name"), 
			(String)_model.get("type"), 
			((Double)_model.get("size")).intValue(),
			_student
		);
	}
	// endregion for serialization from json


	public String getName() {
        return name;
    }


	public Status getStatus() {
		return status;
	}


	public Results getResults() {
		return results;
	}


	public void changeResults(Results newResult) {
		results=newResult;
	}


	public Data getData() {
		return data;
	}


	public Student getStudent() {
		return student;
	}


	// TODO : remove??
	@Override
	public String toString() {
		return getName();
	}


	public void advanceStatus() {
		switch (status) {
			case PreTrained:
				status = Status.Training;
				break;
			case Training:
				status = Status.Trained;
				break;
			default: // case Trained:
				status = Status.Tested;
		}
	}
}
