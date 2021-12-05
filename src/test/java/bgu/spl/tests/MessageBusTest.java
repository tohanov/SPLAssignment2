package bgu.spl.tests;

import static org.junit.Assert.*;

import java.util.concurrent.locks.Condition;

import javax.lang.model.type.NullType;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import bgu.spl.mics.application.objects.Data.Type;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.services.StudentService;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.messages.TestModelEvent;

public class MessageBusTest {

    MessageBusImpl messageBus;
    MicroService m1;

    @Before
    public void setUp(){

        messageBus=MessageBusImpl.getInstance();
        m1=new ConferenceService("conference");
    }

    @Test
    public void testRegister(){

      assertFalse("Expected to return false before microservice is registered!", messageBus.isRegistered(m1));
      messageBus.register(m1);
      assertTrue("Expected to return true after microservice is registered!",  messageBus.isRegistered(m1));
      
      
    }

    @Test
    public void testUnregister(){
        messageBus.register(m1);
        assertTrue("Assure m1 is registered!",  messageBus.isRegistered(m1));

        messageBus.subscribeBroadcast(TickBroadcast.class , m1);

        messageBus.unregister(m1);
        
        assertFalse("should not be subscribed to broadcast after unregister", messageBus.isSubscribedToBroadcast(TickBroadcast.class, m1));
        assertFalse("m1 is expected to be unregistered!", messageBus.isRegistered(m1));
    }
    
    @Test
    public void testSubscribeEvent(){
        messageBus.register(m1);
        assertTrue("Assure m1 is registered!",  messageBus.isRegistered(m1));

        messageBus.subscribeEvent(ExampleEvent.class, m1);    //arbitrarily chosen event
        assertTrue("m1 should be subscribed to example!", messageBus.isSubscribedToEvent(ExampleEvent.class, m1));

        


    }

    @Test
    public void testSubscribeBroadcast(){
        messageBus.register(m1);
        assertTrue("Assure m1 is registered!",  messageBus.isRegistered(m1));

        messageBus.subscribeBroadcast(PublishConferenceBroadcast.class , m1);
        assertTrue("m1 should be subscribed to tick broadcast!", messageBus.isSubscribedToBroadcast(PublishConferenceBroadcast.class, m1));



    }

    @Test
    public void testSendEvent(){
        StudentService student1Service = new StudentService("student1");
        ConferenceService conference = new ConferenceService("conference");

        messageBus.subscribeEvent((Class<? extends Event<Data>>) PublishResultsEvent.class, conference);
        // messageBus.subscribeBroadcast(PublishConferenceBroadcast.class , student2Service);
        
        int prevSuccessfulModelsCount = conference.getNamesOfSuccessfulModels().length;
        Future<Data> publishFuture = messageBus.sendEvent(new PublishResultsEvent<Data>(student1Service.getStudent()));

        publishFuture.get(); // wait for conference to be done handling

        assertEquals("Conference didn't get the event notification", prevSuccessfulModelsCount + 1, conference.getNamesOfSuccessfulModels().length);
    }

    @Test
    public void testSendBroadcast(){
        StudentService student1Service = new StudentService("student1");
        messageBus.subscribeBroadcast(PublishConferenceBroadcast.class , student1Service);

        int previousPapersRead = student1Service.getStudent().getPapersRead();

        messageBus.sendBroadcast((Broadcast)new PublishConferenceBroadcast());

        assertEquals("Broadcast didn't reach the student.", previousPapersRead + 1, student1Service.getStudent().getPapersRead());
    }




}
