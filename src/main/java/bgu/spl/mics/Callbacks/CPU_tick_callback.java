package bgu.spl.mics.Callbacks;
import bgu.spl.mics.Callback;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.DataBatch;

public class CPU_tick_callback implements Callback<TickBroadcast> {

private CPU cpu;

    public CPU_tick_callback(CPU _cpu){
        
        this.cpu=_cpu;

    }
    @Override
    public void call(TickBroadcast c) {
        if(!cpu.isEmpty()){
            
            cpu.process();

            if(cpu.isCurrentBatchReady()){
                DataBatch readyBatch=cpu.removeBatch();

                //TODO: implementation of sending back to cluster
            
            }
            


        }
        
    }
    
}
