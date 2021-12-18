package bgu.spl.mics.application.objects;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;


/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {

    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }

    @Expose private String name;
    @Expose private String department;
    @Expose private Degree status;
    @Expose private int publications;
    @Expose private int papersRead;
    //private ArrayList<Model> models; // FIXME : should student have models or service
    @Expose private ArrayList<Model> trainedModels;
    

    // region for serialization from json
	public Student(String _name, String _department, String _status) {

		name = _name;
		department = _department;
		status = (_status.toUpperCase().equals("MSC")) ? Degree.MSc : Degree.PhD;
		publications = 0;
		papersRead = 0;
        trainedModels=new ArrayList<>();
        Cluster.getInstance().registerStudent(this); //FIXME: can student talk to cluster??
	}
	// endregion for serialization from json
	

	public String getName() {
		return name;
	}


	public String getDepartment() {
		return department;
	}


	public Degree getStatus() {
		return status;
	}


	public int getPublications() {
		return publications;
	}

	
	public void updatePublications() {
		++publications;
	}


	public int getPapersRead() {
		return papersRead;
	}


	public void updatePapersRead() {
		++papersRead;
	}


	public void addTrainedModel(Model modelToAdd) {
		trainedModels.add(modelToAdd);
	}

	// public ArrayList<Model> getModels(){ // FIXME
	// return models;
	// }
}
