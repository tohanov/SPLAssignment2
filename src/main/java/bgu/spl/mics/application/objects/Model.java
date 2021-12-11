package bgu.spl.mics.application.objects;

import java.util.Map;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {

	private enum Status {
		PreTrained, 
		Training,
		Trained,
		Tested
	}

	private enum Results {
		None,
		Good,
		Bad
	}

	private String name;
	private Data data;
	private Student student;
	private Status status;
	private Results results;


	// region for serialization from json

	public Model(String _name, String _dataType, int _size, Student _student) {
		name = _name;
		data = new Data( Data.typeFromString(_dataType), _size );
		status = Status.PreTrained;
		results = Results.None;
		
		student = _student;
	}

	public Model(Map<String,Object> _model, Student _student) {
		this((String)_model.get("name"), (String)_model.get("type"), ((Double)_model.get("size")).intValue(), _student);
	}

	// endregion for serialization from json
}
