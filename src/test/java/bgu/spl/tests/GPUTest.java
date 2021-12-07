package bgu.spl.tests;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.GPU.Type;
import bgu.spl.mics.application.services.TimeService;

public class GPUTest {
    
	private static final MessageBusImpl messageBus = MessageBusImpl.getInstance();

	
	@Before
	public void setUp() {
		// TimeService timeService = new TimeService();
	}


	@After
	public void tearDown() {
	}


	@Test
	public void testConstructor() {
		int previousLength = Cluster.getInstance().getGPUs().size();

		GPU gpu1 = new GPU("GPU_1", Type.RTX3090);
		GPU gpu2 = new GPU("GPU_2", Type.RTX2080);
		GPU gpu3 = new GPU("GPU_3", Type.GTX1080);
		
		assertEquals("GPU of type RTX3090 should have VRAM", 32, gpu1.getVRAM());
		assertEquals("GPU of type RTX3090 should have VRAM", 16, gpu2.getVRAM());
		assertEquals("GPU of type RTX3090 should have VRAM", 8, gpu3.getVRAM());
		assertEquals("Just after creation, the GPU will not have any models added", gpu3.getProcessedBatchesNum());


		assertTrue("Increase of number of GPUs in Cluster's storage isn't matching amount added.", Cluster.getInstance().getGPUs().size() == previousLength + 3);
	}


	@Test
	public void testInitialize() {
		GPU gpu1 = new GPU("GPU_1", Type.RTX3090);
		MicroService ms1 = gpu1.getService();
		assertTrue("GPUService should register itself with proper callbacks in function initialize()", messageBus.isRegistered(ms1));
	}

	@Test
	public void testCallback() {
		GPU gpu1 = new GPU("GPU_1", Type.RTX3090);
		assertEquals("A new gpu should start with empty processed data storage", 0, gpu1.getProcessedBatchesNum());
		messageBus.sendBroadcast(new TickBroadcast());
		
	}
}
