package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private int publications;
    private int papersRead;
    private Model[] models;
    private LinkedList<Model> unpublishedModels;



    public int getPapersRead() {
        return papersRead;
    }

    public Model[] getModels(){
        return models;
    }

    
	// region for serialization from json

	

	public Student(String _name, String _department, String _status) {

		name = _name;
		department = _department;
		status = (_status == "MSc") ? Degree.MSc : Degree.PhD;
		publications = 0;
		papersRead = 0;
	}

	// endregion for serialization from json

}
