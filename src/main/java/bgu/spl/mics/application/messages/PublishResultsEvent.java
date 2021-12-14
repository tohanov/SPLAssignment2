package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class PublishResultsEvent implements Event<Model> {

	private Model modelsToPublish;

	public PublishResultsEvent(Model modelToPublish) {
		this.modelsToPublish=modelsToPublish;
	}

	public Model getModelsToPublish() {
		return modelsToPublish;
	}
}
