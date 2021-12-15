package bgu.spl.mics.application.services;

import java.util.Map;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConfrenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {
    
    @Override
    protected void initialize() {

		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            if(conference.increaseTime()){
                MessageBusImpl.getInstance().sendBroadcast(new PublishConferenceBroadcast(conference.returnSuccessfulModels()));
                uploadConferenceInformation();
                terminate();
            }
        } );

        subscribeEvent(PublishResultsEvent.class, (message)->{
            Model m=message.getValue();
            conference.addSuccessfulModel(m);

        });
    }

    public void uploadConferenceInformation(){
        Cluster.getInstance().uploadConferenceInformation(conference);

    }

    private ConfrenceInformation conference;

	// region for serialization from json
	public ConferenceService(Map<String,Object> _conference) {
        super("Conference_" + (String)_conference.get("name"));
        
		conference=new ConfrenceInformation((String)_conference.get("name"),((Double)_conference.get("date")).intValue());
    }
	// endregion for serialization from json
}
