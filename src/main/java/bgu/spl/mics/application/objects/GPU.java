package bgu.spl.mics.application.objects;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.Event;


/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {

    /**
     * Enum representing the type of the GPU.
     */
    public enum Type { RTX3090, RTX2080, GTX1080 }


	// region According to assignment instructions
    private Type type;
    private Model model; // the model being taken care of currently
    private static Cluster cluster = Cluster.getInstance();
	// endregion According to assignment instructions


	// region Added fields
	private PriorityQueue<Event<Model>> modelEventsQueue;
	// private LinkedList<Event<Model>> modelEventsQueue;
	private Event<Model> currentEvent;
	private ArrayDeque<DataBatch> vRAM;
	private final byte trainingDelay; // according to the type of the gpu
	private byte vRAMCapacity;	// according to the type of the gpu
	private int currentBatchIndex;
	private int numberOfTrainedSamples;
	// endregion Added fields


	// region for serialization from json
	public GPU(String _type) {
        this.type = typeFromString(_type);
		model = null;

		vRAMCapacity = calcVRAMCapacity();
		trainingDelay = calcTrainingDelay();
		
		vRAM = new ArrayDeque<>();
		// modelEventsQueue = new LinkedList<>();

		// priority queue that sorts by model size
		modelEventsQueue = new PriorityQueue<Event<Model>> (
			new Comparator<Event<Model>>() {
				@Override
				public int compare(Event<Model> e1, Event<Model> e2) {
					if (e1 instanceof TestModelEvent) {
						return -1;
					}
					else if (e2 instanceof TestModelEvent) {
						return 1;
					}
					else { // both are of type for training
						int val = ( ((TrainModelEvent)e1).getValue().getData().getSize() - ((TrainModelEvent)e2).getValue().getData().getSize() );

						if (GPU.this.type == Type.RTX3090 || GPU.this.type == Type.RTX2080) {
							return -val;
						}

						return val;
					}
				}
			}
		);
    }


	private Type typeFromString(String _type) {
		Type returnType;
		String uppercaseType = _type.toUpperCase();

		if (uppercaseType.equals("RTX3090"))
			returnType = Type.RTX3090;
		else if (uppercaseType.equals("RTX2080"))
			returnType = Type.RTX2080;
		else
			returnType = Type.GTX1080;
		
		return returnType;
	}


	private byte calcVRAMCapacity() {
		byte _vRAMCapacity;

		switch (type) {
			case GTX1080:
				_vRAMCapacity = 8;
				break;
			case RTX2080:
				_vRAMCapacity = 16;
				break;
			default: // RTX3090:
				_vRAMCapacity = 32;
		}

		return _vRAMCapacity;
	}


	private byte calcTrainingDelay() {
		byte _trainingDelay;
		
		switch (type) {
			case GTX1080:
				_trainingDelay = 4;
				break;
			case RTX2080:
				_trainingDelay = 2;
				break;
			default: // RTX3090:
				_trainingDelay = 1;
		}

		return _trainingDelay;
	}
	// endregion for serialization from json


	/**
	 * @param modelEvent event to be dealt with
	 * @post: if modelEventQueue.size() >= @pre:modelEventQueue.size()
	 * @return modelEventQueue.size() > @pre:modelEventQueue.size()
	 */
	public boolean addModel(Event<Model> modelEvent) {
		if (model == null && modelEvent.getValue().getStatus() == Model.Status.Trained) { // if TestModelEvent and no events in queue - execute immediately
			testModel((TestModelEvent) modelEvent);
		 
			return false;	// nothing added to queue
		}
		// esle : either model is not null or the gotten model isn't for testing
		// so if : the gotten event is not for testing - it's for training and should be just added to the queue
		// and else : current model is not null, meaning there's a model being trained by the GPU
		// and the received event is for testing -- either way adding to the queue
		else {
			modelEventsQueue.add(modelEvent);

			return true;
		}
	}


	private ArrayList<Event<Model>> trainAndCheck() {
		if (train() == true) {
			ArrayList<Event<Model>> eventsToHandle = new ArrayList<>();

			// add event of model that finished training
			eventsToHandle.add(currentEvent);
			// reset the current model before next tick
			model = null;
			currentEvent = null;

			// test all test events that got in the queue in the meantime and prepare for resolution by the service
			while ( ! modelEventsQueue.isEmpty()
					&& modelEventsQueue.peek().getValue().getStatus() == Model.Status.Trained) { // modelEventsQueue.peek()==testModelEvent(...)
				
				testModel((TestModelEvent) modelEventsQueue.peek());
				eventsToHandle.add(modelEventsQueue.poll());
			}

			return eventsToHandle;
		}

		return null;
	}


	/**
	 * @pre: Received a tick
	 * @post: model.getStatus() > @pre(model.getStatus())
	 * @return: ArrayList of eventModels of models that contains all models that were trained/tested this ticks. if no such models exist, returns null
	 */
	public ArrayList<Event<Model>> actOnTick() {
		if (model == null) {
			// if queue is not empty then init training of models
			if ( ! modelEventsQueue.isEmpty()) {
				initBatchTraining(); // model put in status of training

				return trainAndCheck();
			}
			// if queue is empty then there's nothing to do
		}
		else { //if (model.getStatus() == Status.Training) {
			// --- model is not null, means we are in the process of training it

			// if model is in training
				// train and check if done training

			// else not in training, shouldn't be possible since we're removing tested models when we removed a trained one

			return trainAndCheck();
		}

		return null; // reaching here means there's nothing to do (both model is null and queue is empty)
	}


	/**
	 * @inv: modelsEventsQueue.isEmpty()==false 
	 * @pre: modelEventsQueue.peek().getValue().getStatus == preTrained
	 * @post: modelEventsQueue.peek().getValue().getStatus == Training
	 */
	private void initBatchTraining() {
		model = modelEventsQueue.peek().getValue();
		currentEvent = modelEventsQueue.poll();

		model.advanceStatus();
		currentBatchIndex = 0;
		numberOfTrainedSamples = 0;

		// TODO : remove debug
		// synchronized(System.out){
		// 	System.out.println("initializing training on model " + model.getName() + " of size " + model.getData().getSize());
		// }
	}


	/**
	 * @pre: currentBatchIndex <= model.getData().getSize() 
	 * @post: currentBatchIndex >= @pre(currentBatchIndex) && cluster.statistics.totalGPUTimeUsed  == @pre(cluster.statistics.totalGPUTimeUsed) + 1
	 * 
	 * @return true if batch is trained
	 */
	private boolean train() {
		// TODO : remove debug
		// synchronized(System.out){
		// 	System.out.println("\n[*] Before batch creation loop, model=" + model.getName() +
		// 	 "\nof size " + model.getData().getSize() +
		// 	  "\ncurrbatchindex=" + currentBatchIndex +
		// 	  "\nemptyVRam=" + emptyVRAM +
		// 	  "\n");
		// }
		
		// send more batches to cluster if there's available space to store them when they get back
		while (currentBatchIndex < model.getData().getSize() && vRAMCapacity != 0) {
			DataBatch dataBatch = new DataBatch(model.getData(), currentBatchIndex, this);
			cluster.sendBatchForProcessing(dataBatch);
			--vRAMCapacity; // reserving space for it to come back to
			currentBatchIndex += 1000;
		}
		
		// TODO : remove debug
		// synchronized(System.out){
		// 	System.out.println("\n[*] After batch creation loop, model=" + model.getName() +
		// 	 "\nof size " + model.getData().getSize() +
		// 	  "\ncurrbatchindex=" + currentBatchIndex +
		// 	  "\nemptyVRam=" + emptyVRAM + 
		// 	  "\n");
		// }	


		if( ! vRAM.isEmpty()) { // left unsynched on purpose
			updateTotalGPUTimeUsed();
			DataBatch batch = vRAM.peek(); // left unsynched on purpose

			if ( ! batch.isInTraining()) {
				batch.initTraining(trainingDelay);
			}

			if (batch.train() == true) {
				synchronized (vRAM) {
					vRAM.poll();
				}

				numberOfTrainedSamples += 1000;
				++vRAMCapacity;

				if (hasFinishedTraining()) {
					model.advanceStatus(); // change status to "Trained"
					return true;
				}
			}
		}

		return false;
	}


	/**
	 * @pre: modelsEventQueue.isEmpty() == false 
	 * @pre: model.getStatus() == training
	 * @return true if current model is trained
	 */
	private boolean hasFinishedTraining() {
		return numberOfTrainedSamples >= model.getData().getSize();
	}


	/**
	 * @param databatch
	 * @post: vRAM.size() == @pre(vRAM.size()) + 1
	 * post@: vRAM.getLast() == databatch
	 */
	public void returnProcessedBatch(DataBatch databatch) {
		synchronized (vRAM) {
			vRAM.add(databatch);
		}
	}


	/**
	 * @param testModelEvent: trained model to be tested
	 * @pre: model.status== trained
	 * @pre: model.getResults == none
	 * @post: model.getStatus()==tested
	 * @post: model.getResults == Good || model.getResults == BAD
	 */
	public void testModel(TestModelEvent testModelEvent) {
		Model model = testModelEvent.getValue();
		double chance = (model.getStudent().getStatus() == Student.Degree.MSc) ? 0.6 : 0.8; // MSC : PHD

		model.changeResults((Math.random() <= chance) ? Model.Results.Good : Model.Results.Bad);
		model.advanceStatus(); // change status to "Tested"

		// TODO:remove debug
		// CRMSRunner.synchronizedSyso("testing model "+model.getName()+", result is: "+model.getResults()+" status is: "+model.getStatus());
	}

	
	private void updateTotalGPUTimeUsed() {
		 cluster.increaseTotalGPUTimeUsed(1);
	}


	public int getNumberOfMessagesInQueue() {
		return modelEventsQueue.size();
	}

	public ArrayDeque<DataBatch> getVRAM(){
		return vRAM;
	}
}