package bgu.spl.tests;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.Future;
import bgu.spl.mics.example.messages.ExampleEvent;

public class FutureTest {

Future<ExampleEvent> future;
ExampleEvent e;


@Before
public void setUp(){
    e=new ExampleEvent("Idan");
    future=new Future<ExampleEvent>();
}    

@Test
public void testGetBeforeResolved(){
    

    assertEquals("Expected output of future.get() to be null",null,future.get(10,TimeUnit.MILLISECONDS));
    
}

@Test
public void testIsDoneBeforeResolved(){
    assertEquals("Expected output of future.isDone() to be true, instead got true",future.isDone(), false);

}

@Test 
public void testResolve(){
    
    future.resolve(e);

    assertEquals("Expected output of future.get() to be ExampleEvent e",future.get(),e);
    assertEquals("Expected output of future.isDone() to be true, instead got false",future.isDone(), true);

}




    
}
