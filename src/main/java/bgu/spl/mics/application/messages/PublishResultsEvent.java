package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class PublishResultsEvent implements Event<Model> {

	private Model modelToPublish;

	public PublishResultsEvent(Model modelToPublish) {
		this.modelToPublish=modelToPublish;

		
	}

	public Model getValue() {
		return modelToPublish;
	}
}
