package com.medismiths.foxtel.mpa;

import java.io.File;

import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;

public class MaterialEnvelope extends MessageEnvelope<Material> {

	private String masterID;
	
	public MaterialEnvelope(File file, Material message) {
		super(file, message);		
	}
	
	public MaterialEnvelope(File file, Material message, String masterID) {
		super(file, message);		
		setMasterID(masterID);
	}
	
	public MaterialEnvelope(MessageEnvelope<Material> envelope, String masterID) {
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
