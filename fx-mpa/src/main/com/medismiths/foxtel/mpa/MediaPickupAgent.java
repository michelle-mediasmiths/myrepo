package com.medismiths.foxtel.mpa;

import java.io.File;

public class MediaPickupAgent {

	final MediaPickupAgentConfiguration configuration;
	
	public MediaPickupAgent(MediaPickupAgentConfiguration configuration){
		this.configuration = configuration;
	}

	public void start() {
		// setup our FileWatcherImplementation, pass in self reference
	}
	
	protected void onXmlArrival(File xml, boolean valid){		
		
		if(valid){
			processValidXML(xml);
		}else{
			processInvalidXML(xml);
		}
		
	}
	
	private void processValidXML(File xml){
		throw new RuntimeException("Not Implemented");
	}
	
	private void processInvalidXML(File xml){
		throw new RuntimeException("Not Implemented");
	}
	
}
