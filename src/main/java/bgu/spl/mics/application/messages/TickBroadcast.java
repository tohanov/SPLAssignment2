package bgu.spl.mics.application.messages;

import javax.swing.text.StyledEditorKit.BoldAction;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    
	private boolean last;


	public TickBroadcast(boolean _isLast) {
		last = _isLast;
	}


	public boolean isLast() {
		return last;
	}
}
