package bgu.spl.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class GPUTest {

	GPU testGPU;


	@Before
	public void setUp() {
		testGPU = new GPU("GTX1080");
	}


	@After
	public void tearDown() {
	}


	@Test
	public void testAddModel() {
		Model model1 = new Model("testModel", "Images", 1000000, new Student("testStudent", "CS", "PHD"));

		model1.advanceStatus();
		model1.advanceStatus();
		assertFalse("in case of testModelEvent should return false!", testGPU.addModel(new TestModelEvent(model1)));

		Model model2 = new Model("testModel", "Images", 1000000, new Student("testStudent", "CS", "PHD"));
		assertTrue("in case of trainModelEvent should return true!", testGPU.addModel(new TrainModelEvent(model2)));
	}


	@Test
	public void testActOnTick() {
		Model model1 = new Model("testModel", "Images", 1000, new Student("testStudent", "CS", "PHD"));

		assertNull("should return null when modelEventsQueue is empty", testGPU.actOnTick());

		testGPU.addModel(new TrainModelEvent(model1));
		model1.advanceStatus();
		model1.advanceStatus();

		assertNull("should return null when modelEventsQueue didn't train the model", testGPU.actOnTick());
	}


	@Test
	public void testReturnProcessedBatch() {
		assertEquals("vRAM is not empty", testGPU.getVRAM().size(), 0);
		testGPU.returnProcessedBatch(new DataBatch(new Data(Data.Type.Images, 100000), 0, testGPU));
		assertEquals("vRAM is not empty", testGPU.getVRAM().size(), 1);
	}


	@Test
	public void testTestModel() {
		Model model = new Model("testModel", "Images", 1000, new Student("testStudent", "CS", "PHD"));
		model.advanceStatus();
		model.advanceStatus();

		assertFalse("should be not tested", model.getStatus() == Model.Status.Tested);
		assertTrue("should not have result!", model.getResults() == Model.Results.None);

		testGPU.testModel(new TestModelEvent(model));

		assertTrue("should be tested", model.getStatus() == Model.Status.Tested);
		assertFalse("should have result!", model.getResults() == Model.Results.None);
	}
}
