package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;


public class TestModelEvent implements Event<Model>{

    Model model;

	
    public TestModelEvent(Model model){
		this.model = model;

		// TODO : remove debug
       // CRMSRunner.synchronizedSyso("now testing model "+model.getName());
    }


    public Model getValue(){
        return model;
    }
}
