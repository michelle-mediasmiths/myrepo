package com.mediasmiths.mayam.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.server.AttributesModule;
import com.mayam.wf.ws.client.TaskApi;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientImpl;
import com.mediasmiths.mayam.controllers.MayamPackageController;
import com.mediasmiths.mayam.controllers.PackageController;
import com.mediasmiths.mayam.retrying.TasksWSRetryModule;
import com.mediasmiths.mayam.validation.MayamValidator;
import com.mediasmiths.mayam.validation.MayamValidatorImpl;
import com.mediasmiths.mayam.veneer.AssetApiVeneer;
import com.mediasmiths.mayam.veneer.AssetApiVeneerImpl;
import com.mediasmiths.mayam.veneer.SegmentApiVeneer;
import com.mediasmiths.mayam.veneer.SegmentApiVeneerImpl;
import com.mediasmiths.mayam.veneer.TaskApiVeneer;
import com.mediasmiths.mayam.veneer.TaskApiVeneerImpl;
import com.mediasmiths.mayam.veneer.TasksClientVeneer;
import com.mediasmiths.mayam.veneer.TasksClientVeneerImpl;
import com.mediasmiths.mayam.veneer.UserApiVeneer;
import com.mediasmiths.mayam.veneer.UserApiVeneerImpl;

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
	
	@Override
	protected void configure()
	{
		install(new SecurityModule());
		install(new AttributesModule());
		install(new MayamAudioVisualModule());
		install(new TasksWSRetryModule());
		
		bind(MayamClient.class).to(MayamClientImpl.class);
		bind(MayamValidator.class).to(MayamValidatorImpl.class);
		bind(PackageController.class).to(MayamPackageController.class);
		
		bind(AssetApiVeneer.class).to(AssetApiVeneerImpl.class);
		bind(TasksClientVeneer.class).to(TasksClientVeneerImpl.class);
		bind(SegmentApiVeneer.class).to(SegmentApiVeneerImpl.class);
		bind(TaskApiVeneer.class).to(TaskApiVeneerImpl.class);
		bind(UserApiVeneer.class).to(UserApiVeneerImpl.class);
		
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
