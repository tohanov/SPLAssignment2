package bgu.spl.tests;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.concurrent.locks.Condition;

import javax.lang.model.type.NullType;

import com.google.gson.internal.LinkedTreeMap;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

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
import bgu.spl.mics.application.objects.Model.Results;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.services.GPUService;
import bgu.spl.mics.application.services.StudentService;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.messages.TestModelEvent;

public class MessageBusTest {

    MessageBusImpl messageBus;
    MicroService microservice;
	//Thread messageBusThread = null;
	// Thread m1thrd = null;


    @Before
    public void setUp(){

        messageBus = MessageBusImpl.getInstance();

        microservice=new GPUService("test_gpu");
		// Thread m1thrd = new Thread(m1);
		// m1thrd.start();
    }


	@After
	public void tearDown() {
		// if (messageBusThread != null) {
		// 	messageBusThread.interrupt();
		// 	try { messageBusThread.join(); }
		// 	catch (Exception e) { }
		// }
		// if (m1thrd != null) {
		// 	messageBusThread.interrupt();
		// 	try { messageBusThread.join(); }
		// 	catch (Exception e) { }
		// }
	}


    @Test
    public void testRegister(){

      assertFalse("Expected to return false before microservice is registered!", messageBus.isRegistered(microservice));
      messageBus.register(microservice);
      assertTrue("Expected to return true after microservice is registered!",  messageBus.isRegistered(microservice));
      
      
    }


    @Test
    public void testUnregister(){
        messageBus.register(microservice);
        assertTrue("Assure m1 is registered!",  messageBus.isRegistered(microservice));

        messageBus.subscribeBroadcast(TickBroadcast.class , microservice);

        messageBus.unregister(microservice);
        
        assertFalse("should not be subscribed to broadcast after unregister", messageBus.isSubscribedToBroadcast(TickBroadcast.class, microservice));
        assertFalse("m1 is expected to be unregistered!", messageBus.isRegistered(microservice));
    }
    
    @Test
    public void testSubscribeEvent(){
        messageBus.register(microservice);
        assertTrue("Assure m1 is registered!",  messageBus.isRegistered(microservice));

        messageBus.subscribeEvent(TrainModelEvent.class, microservice);    //arbitrarily chosen event
        assertTrue("m1 should be subscribed to trainModelEvent!", messageBus.isSubscribedToEvent(TrainModelEvent.class, microservice));
        assertFalse("m1 should not be subscribed to PublishResultsEvent!", messageBus.isSubscribedToEvent(PublishResultsEvent.class,microservice));
    }


    @Test
    public void testSubscribeBroadcast(){
        messageBus.register(microservice);
        assertTrue("Assure m1 is registered!",  messageBus.isRegistered(microservice));

        messageBus.subscribeBroadcast(TickBroadcast.class , microservice);
        assertTrue("m1 should be subscribed to tick broadcast!", messageBus.isSubscribedToBroadcast(TickBroadcast.class, microservice));
        assertFalse("m1 should not be subscribed to PublishConferenceBroadcast!", messageBus.isSubscribedToBroadcast(PublishConferenceBroadcast.class,microservice));

    }


    @Test
    public void testSendEvent(){
        Map<String,Object> map = new LinkedTreeMap<>();
        map.put("name", "testConf");
        map.put("date", 2000000.0);
        ConferenceService tesConService = new ConferenceService(map);
        


		Thread t1 = new Thread(tesConService);
		t1.start();
        try {
            Thread.currentThread().sleep(2000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


		assertEquals("message queue should be empty!", 0,tesConService.getConference().returnSuccessfulModels().size());
        messageBus.sendEvent(new PublishResultsEvent(new Model("testModel", "Images", 1000000, new Student("testStudent", "CS", "PHD"))));
        try {
            Thread.currentThread().sleep(2000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

		assertNotEquals("message queue should be empty!", 0,tesConService.getConference().returnSuccessfulModels().size());
        
		
		try {
            messageBus.sendBroadcast(new TickBroadcast(true));
			t1.join();
			
		} catch (Exception e) {

		}
    }


    @Test
    public void testSendBroadcast(){
        GPUService testGPUService = new GPUService("test");
        Thread t1 = new Thread(testGPUService);
		t1.start(); //awaits message in the run() loop
        try {
            Thread.currentThread().sleep(2000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


        assertTrue("should be alive", t1.isAlive());

        messageBus.sendBroadcast(new TickBroadcast(true));

        try {
            Thread.currentThread().sleep(2000);

            assertFalse("should be dead", t1.isAlive());
            t1.join();
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    @Test
    public void testComplete(){
        GPUService testGPUService = new GPUService("test");
        Thread t1 = new Thread(testGPUService);
		t1.start(); //awaits message in the run() loop
        try {
            Thread.currentThread().sleep(2000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Model model=new Model("testModel", "Images", 1000000, new Student("testStudent", "CS", "PHD"));
        model.advanceStatus();model.advanceStatus();model.advanceStatus();
        TrainModelEvent event=new TrainModelEvent(model);
        Future<Model> f=messageBus.sendEvent(event);

        assertFalse("future is not resolved yet", f.isDone());

        messageBus.complete(event, model);

        assertTrue("future should be resolved", f.isDone());
        assertEquals(model, f.get());




    }

    @Test
    public void testAwaitMessage(){
        GPUService testGPUService = new GPUService("test");
        Thread t1 = new Thread(testGPUService);
		t1.start(); //awaits message in the run() loop
        try {
            Thread.currentThread().sleep(2000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


        assertTrue("should be alive", t1.isAlive());
        try {
            Thread.currentThread().sleep(2000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        assertTrue("should be alive", t1.isAlive());

        messageBus.sendBroadcast(new TickBroadcast(true));

        try {
            Thread.currentThread().sleep(2000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        assertFalse("should be dead", t1.isAlive());
        
    }


}


