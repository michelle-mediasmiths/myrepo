package com.mediasmiths.foxtel.placeholder;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientImpl;

public class PlaceHolderManagerCLI {

	static Logger logger = Logger.getLogger(PlaceHolderManagerCLI.class);

	/**
	 * @param args
	 * @throws JAXBException
	 * @throws SAXException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws JAXBException, SAXException,
			MalformedURLException {

		//load configuration
		PlaceHolderManagerConfiguration configuration = null;
		try {
			configuration = new PlaceHolderManagerConfiguration(
					"placeholdermanagement.properties");

			//TODO : check configuration (make sure folders exist etc)
			
		} catch (IOException e) {
			logger.fatal("Failed to load configuration", e);
			System.exit(1);
		}

		if (configuration != null) {
			// TODO use values from configuration for mayam client connection
			MayamClient mc = new MayamClientImpl();
			// start agent:
			new PlaceHolderManager(mc, configuration).run();
		}

	}

}
