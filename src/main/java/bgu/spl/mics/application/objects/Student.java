package bgu.spl.mics.application.objects;

import java.util.ArrayList;
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
    private ArrayList<Model> models;
    
    public Student(String name, String department, Degree status, ArrayList<Model> models){
        this.name=name;
        this.department=department;
        this.status=status;
        this.models=models;     // the models should belong to the student and not to the studentService
        publications=0;
        papersRead=0;


    }

    public String getName(){
        return name;
    }

    public String getDepartment(){
        return department;
    }

    public Degree getStatus(){
        return status;
    }

    public int getPublications(){
        return publications;
    }

    public int getPapersRead() {
        return papersRead;
    }

    public ArrayList<Model> getModels(){
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
