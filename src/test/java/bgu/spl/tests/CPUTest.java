package bgu.spl.tests;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.objects.Data.Type;

public class CPUTest {

    private CPU cpu;

    @Before
    public void setUp(){
        cpu=new CPU(32);
    }

    @Test
    public void testCalculateProcessingTime(){

        assertEquals(cpu.calculateProcessingTime(Type.Images), 4);
        assertEquals(cpu.calculateProcessingTime(Type.Text), 2);
        assertEquals(cpu.calculateProcessingTime(Type.Tabular), 1);

        CPU cpu2=new CPU(8);

        assertEquals(cpu2.calculateProcessingTime(Type.Images), 16);
        assertEquals(cpu2.calculateProcessingTime(Type.Text), 8);
        assertEquals(cpu2.calculateProcessingTime(Type.Tabular), 4);
    }

    @Test
    public void testAddBatch(){
        DataBatch toAdd=new DataBatch(new Data(Data.Type.Images, 100000), 0, null);
        cpu.addBatch(toAdd);

        assertEquals("",toAdd, cpu.getData().peek());
    }
   
    @Test
    public void testTickCallback(){
        
        CPU testCpu=new CPU(16);
        assertEquals(0, testCpu.getTickToCompletion());

        DataBatch batch=new DataBatch(new Data(Data.Type.Images, 100000), 0, null);
        testCpu.addBatch(batch);

        assertEquals(testCpu.calculateProcessingTime(Type.Images),testCpu.getTickToCompletion());
        assertFalse(batch.isInProcessing());
		assertEquals(0, Cluster.getInstance().getStatistics().getCPUTimeUsed().get());

        testCpu.tickCallback();
        assertTrue(batch.isInProcessing());
        assertNotEquals(testCpu.calculateProcessingTime(Type.Images), testCpu.getTickToCompletion());
        
		assertEquals(1, Cluster.getInstance().getStatistics().getCPUTimeUsed().get());
    }
    
}
