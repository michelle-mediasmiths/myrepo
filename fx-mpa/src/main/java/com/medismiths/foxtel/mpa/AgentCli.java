package com.medismiths.foxtel.mpa;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

public class AgentCli {

	/**
	 * @param args
	 * @throws JAXBException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws JAXBException, SAXException {
		
			//read configuration files
		
			//start agent:		
			MediaPickupAgentConfiguration configuration = new MediaPickupAgentConfiguration();				
			new MediaPickupAgent(configuration).start();
	}

}
