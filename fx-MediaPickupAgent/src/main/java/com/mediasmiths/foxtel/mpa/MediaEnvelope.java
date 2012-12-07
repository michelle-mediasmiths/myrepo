package com.mediasmiths.foxtel.mpa;

import java.io.File;

import com.mediasmiths.foxtel.agent.MessageEnvelope;

public class MediaEnvelope<T> extends MessageEnvelope<T> {

	private String masterID;
	
	public MediaEnvelope(File file, T message) {
		super(file, message);		
	}
	
	public MediaEnvelope(File file, T message, String masterID) {
		super(file, message);		
		setMasterID(masterID);
	}
	
	public MediaEnvelope(MessageEnvelope<T> envelope, String masterID) {
		super(envelope.getFile(), envelope.getMessage());
		setMasterID(masterID);
	}

	public String getMasterID() {
		return masterID;
	}

	public final void setMasterID(String masterID) {
		this.masterID = masterID;
	}


}
