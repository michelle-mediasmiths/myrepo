package com.medismiths.foxtel.mpa;

import java.io.File;

public class AgentCli {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
			//read configuration files
		
			//start agent:		
			MediaPickupAgentConfiguration configuration = new MediaPickupAgentConfiguration();				
			new MediaPickupAgent(configuration).start();
	}

}
