package bgu.spl.mics.application.objects;

import java.util.ArrayList;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    private int currentTime;
    private ArrayList<Model> successfulModels;

    public ConfrenceInformation(String name, int date){
        this.name=name;
        this.date=date;
        currentTime=0;
        successfulModels=new ArrayList<>();
    }

    public boolean increaseTime(){
        return ++currentTime==date;
    }

    public void addSuccessfulModel(Model m){
        successfulModels.add(m);
    }

    public ArrayList<Model> returnSuccessfulModels(){
        return successfulModels;
    }
    
    @Override
    public String toString(){
        String output=" name of conference= "+name+", date of conference= "+date+"\n the successful models are: "+successfulModels;

        return output;

    }
}
