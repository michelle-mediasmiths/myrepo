package com.mediasmiths.mayam.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.server.AttributesModule;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientImpl;
import com.mediasmiths.mayam.RaceLoggable;
import com.mediasmiths.mayam.RaceLoggingInterceptor;
import com.mediasmiths.mayam.controllers.MayamPackageController;
import com.mediasmiths.mayam.controllers.PackageController;
import com.mediasmiths.mayam.retrying.TasksWSRetryModule;
import com.mediasmiths.mayam.validation.MayamValidator;
import com.mediasmiths.mayam.validation.MayamValidatorImpl;
import com.mediasmiths.mayam.veneer.AssetApiVeneer;
import com.mediasmiths.mayam.veneer.TaskApiVeneer;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.net.MalformedURLException;
import java.net.URL;

public class MayamClientModule extends AbstractModule
{

	private final static Logger log = Logger.getLogger(MayamClientModule.class);
	public static final String SETUP_TASKS_CLIENT = "SETUP.TASKS.CLIENT";
	private static final String MAYAM_AUTH_TOKEN =  "mayam.auth.token";
	private static final String MAYAM_ENDPOINT ="mayam.endpoint";
	boolean requiresSecurity = true;
	
	public MayamClientModule(boolean securityRequired)
	{
		requiresSecurity = securityRequired;
	}
	
	public MayamClientModule()
	{

	}
	
	@Override
	protected void configure()
	{
		if (requiresSecurity)
		{
			install(new SecurityModule());
		}
		install(new AttributesModule());
		install(new MayamAudioVisualModule());

		install(new TasksWSRetryModule());

		RaceLoggingInterceptor raceLoggingInterceptor = new RaceLoggingInterceptor();
		bindInterceptor(Matchers.subclassesOf(TaskApiVeneer.class),  Matchers.annotatedWith(RaceLoggable.class), raceLoggingInterceptor);
		bindInterceptor(Matchers.subclassesOf(AssetApiVeneer.class),  Matchers.annotatedWith(RaceLoggable.class), raceLoggingInterceptor);


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
