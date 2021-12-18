package bgu.spl.mics.application.objects;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;


/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    @Expose private String name;
    @Expose private int date;
    private int currentTime;
    @Expose private ArrayList<Model> publications;


    public ConfrenceInformation(String name, int date) {
		this.name = name;
		this.date = date;
		currentTime = 0;
		publications = new ArrayList<>();
	}


    public boolean increaseTime() {
		return ++currentTime == date;
	}

	
    public void addSuccessfulModel(Model m) {
		publications.add(m);
	}


	public ArrayList<Model> returnSuccessfulModels() {
		return publications;
    }
    

	// TODO: remove??
    @Override
	public String toString() {
		String output = 
			" name of conference= " + name + 
			", date of conference= " + date +
			"\n the successful models are: " + publications;

		return output;
	}
}
