package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Student;

public class PublishResultsEvent<T> implements Event<T> {

	private Student author;

	public PublishResultsEvent(Student _student) {
		author = _student;
	}

	public Student getAuthor() {
		return author;
	}
    
}
