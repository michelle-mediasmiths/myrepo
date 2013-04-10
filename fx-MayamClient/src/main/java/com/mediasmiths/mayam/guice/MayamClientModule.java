package com.mediasmiths.mayam.guice;

import static com.mediasmiths.mayam.MayamClientConfig.MAYAM_AUTH_TOKEN;
import static com.mediasmiths.mayam.MayamClientConfig.MAYAM_ENDPOINT;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.server.AttributesModule;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientImpl;
import com.mediasmiths.mayam.controllers.MayamPackageController;
import com.mediasmiths.mayam.controllers.PackageController;
import com.mediasmiths.mayam.validation.MayamValidator;
import com.mediasmiths.mayam.validation.MayamValidatorImpl;

public class MayamClientModule extends AbstractModule
{

	public final static String SETUP_TASKS_CLIENT = "SETUP_TASKS_CIENT";
	
	private final static Logger log = Logger.getLogger(MayamClientModule.class);
	
	@Override
	protected void configure()
	{
		install(new SecurityModule());
		install(new AttributesModule());
		bind(MayamClient.class).to(MayamClientImpl.class);
		bind(MayamValidator.class).to(MayamValidatorImpl.class);
		bind(PackageController.class).to(MayamPackageController.class);
		
	}

	@Provides
	@Singleton
	@Named(SETUP_TASKS_CLIENT)
	public TasksClient getSetupTasksClient(TasksClient tc,@Named(MAYAM_ENDPOINT) String endpoint,
			@Named(MAYAM_AUTH_TOKEN) String token) throws MalformedURLException{
		
		log.info(String.format("Using mayam endpoint %s and auth token %s", endpoint,token));
		
		URL url = new URL(endpoint);
		return tc.setup(url, token);
	}
	
	
	@Provides
	@Named("material.exchange.marshaller")
	public Marshaller provideMaterialExchangeMarhsaller(@Named("material.exchange.context") JAXBContext jc) throws JAXBException{
		Marshaller marshaller = null;
		try
		{
			marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		}
		catch (JAXBException e)
		{
			log.fatal("Could not create marshaller", e);
			throw e;
		}
		return marshaller;
	}
	
	@Provides
	@Named("material.exchange.context")
	JAXBContext provideMEXJAXBContext() throws JAXBException{
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.generated.MaterialExchange");			
			} catch (JAXBException e) {
				log.fatal("Could not create jaxb context", e);
				throw e;
			}
		return jc;
		}
	
	
	@Provides
	@Named("material.exchange.unmarshaller")
	public Unmarshaller provideMaterialExchangeUnMarhsaller(@Named("material.exchange.context") JAXBContext jc) throws JAXBException{
	
		return jc.createUnmarshaller();
	
	}
}
