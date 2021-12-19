package bgu.spl.tests;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.application.objects.GPU.Type;
import bgu.spl.mics.application.services.TimeService;

public class GPUTest {
    
	private static final MessageBusImpl messageBus = MessageBusImpl.getInstance();
	GPU testGPU;
	
	// switch (type) {
	// 	case GTX1080:
	// 		_vRAMCapacity = 8;
	// 		break;
	// 	case RTX2080:
	// 		_vRAMCapacity = 16;
	// 		break;
	// 	default: // RTX3090:
	// 		_vRAMCapacity = 32;
	// }

	@Before
	public void setUp() {
		testGPU= new GPU("GTX1080");
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testAddModel(){
		Model model1=new Model("testModel", "Images", 1000000, new Student("testStudent", "CS", "PHD"));
		
		
		model1.advanceStatus();model1.advanceStatus();
		assertFalse("in case of testModelEvent should return false!", testGPU.addModel(new TestModelEvent(model1)));
		
		Model model2=new Model("testModel", "Images", 1000000, new Student("testStudent", "CS", "PHD"));
		assertTrue("in case of trainModelEvent should return true!", testGPU.addModel(new TrainModelEvent(model2)));



	}


	@Test
	public void testActOnTick() {

		Model model1=new Model("testModel", "Images", 1000, new Student("testStudent", "CS", "PHD"));
		Model model2=new Model("testMode2", "Images", 1000000, new Student("testStudent", "CS", "PHD"));
		
		assertNull("should return null when modelEventsQueue is empty", testGPU.actOnTick());

		testGPU.addModel(new TrainModelEvent(model1));
		model1.advanceStatus();
		model1.advanceStatus();

		assertNull("should return null when modelEventsQueue didn't train the model", testGPU.actOnTick());
		//TODO

	}

	@Test
	public void testReturnProcessedBatch(){
		assertEquals("vRAM is not empty", testGPU.getVRAM().size(),0);
		testGPU.returnProcessedBatch(new DataBatch(new Data(Data.Type.Images, 100000), 0, testGPU));
		assertEquals("vRAM is not empty", testGPU.getVRAM().size(),1);

	}

	@Test 
	public void testTestModel(){
		Model model=new Model("testModel", "Images", 1000, new Student("testStudent", "CS", "PHD"));
		model.advanceStatus();model.advanceStatus();
		
		assertFalse("should be not tested", model.getStatus()==Model.Status.Tested);
		assertTrue("should not have result!", model.getResults()==Model.Results.None);
		
		testGPU.testModel(new TestModelEvent(model));

		assertTrue("should be tested", model.getStatus()==Model.Status.Tested);
		assertFalse("should have result!", model.getResults()==Model.Results.None);




	}

}
