package bgu.spl.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.DataBatch;
 

public class CPUTest {

    private CPU cpu;

    @Before
    public void setUp(){
        cpu=new CPU(8, new Cluster());
    }

    @Test
    public void testIsReady(){
        assertEquals("Expected cpu.isReady() to be true, instead got false",true, cpu.isReady());
        
    }

    @Test
    public void testSetBatch(){
        DataBatch dataBatch=new DataBatch((new Data(Data.Type.Images, 0, 10)), 0);
        cpu.setBatch(dataBatch);
        assertEquals("Expected cpu.isReady() to be false, instead got true",false, cpu.isReady());

    }

    @Test
    public void testRemoveBatch(){
        cpu.removeBatch();
        assertEquals("Expected cpu.isReady() to be true, instead got false",true, cpu.isReady());

    }

    @Test
    public void testProcessSample(){
        DataBatch dataBatch=new DataBatch((new Data(Data.Type.Images, 0, 10)), 0);
        cpu.setBatch(dataBatch);
        assertEquals(dataBatch.getData().getProcessed(), 0);
        cpu.processSample();
        assertEquals(dataBatch.getData().getProcessed(), 1);


    }


    
}
