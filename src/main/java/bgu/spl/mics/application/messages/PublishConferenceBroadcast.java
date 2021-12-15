package bgu.spl.mics.application.messages;

import java.util.ArrayList;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Model;

public class PublishConferenceBroadcast implements Broadcast {

    private ArrayList<Model> toPublish;

    public PublishConferenceBroadcast(ArrayList<Model> toPublish){
        this.toPublish=toPublish;
    }
    
    public ArrayList<Model> getValue(){
        return toPublish;
    }


}
