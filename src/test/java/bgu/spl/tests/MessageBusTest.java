package bgu.spl.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import bgu.spl.mics.Event;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.example.messages.ExampleEvent;

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


    }

    @Test
    public void testSendBroadcast(){
        



    }




}
