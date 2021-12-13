package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class PublishResultsEvent implements Event<Model> {

	private Student author;

	public PublishResultsEvent(Student _student) {
		author = _student;
	}

	public Student getAuthor() {
		return author;
	}
    
}
