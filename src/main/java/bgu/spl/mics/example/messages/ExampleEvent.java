package bgu.spl.mics.example.messages;

import bgu.spl.mics.Event;

public class ExampleEvent implements Event<String>{

    private String senderName;

    public ExampleEvent(String senderName) {
        this.senderName = senderName;
    }

    @Override
    public String getValue() {
        return null;
    }

    public String getSenderName() {
        return senderName;
    }

    
}