package com.mediasmiths.foxtel.placeholder;

import java.net.MalformedURLException;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientImpl;

public class PlaceHolderManagerCLI {

	/**
	 * @param args
	 * @throws JAXBException 
	 * @throws SAXException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws JAXBException, SAXException, MalformedURLException {
		
			//read configuration files
		
			//start agent:		
			PlaceHolderManagerConfiguration configuration = new PlaceHolderManagerConfiguration();
			//TODO use values from configuration for mayam client connection
			MayamClient mc = new MayamClientImpl();
			new PlaceHolderManager(mc,configuration).run();
	}

}
