package com.mediasmiths.foxtel.wf.adapter.guice;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.wf.adapter.service.WFAdapterRestService;
import com.mediasmiths.foxtel.wf.adapter.service.WFAdapterRestServiceImpl;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

public class WFAdapterModule extends AbstractModule
{
	private static Logger logger = Logger
			.getLogger(WFAdapterModule.class);
	
	@Override
	protected void configure()
	{
		bind(WFAdapterRestService.class).to(WFAdapterRestServiceImpl.class);
		RestResourceRegistry.register(WFAdapterRestService.class);

	}
	

	@Provides
	Marshaller provideMarshaller(JAXBContext jc, @Named("schema.location") String schemaLocation) throws JAXBException, SAXException {
		Marshaller marshaller = null;
		try {
			marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//			SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
//			Schema schema = factory.newSchema(getClass().getClassLoader().getResource(schemaLocation));
//			marshaller.setSchema(schema);
			} catch (JAXBException e) {
				logger.fatal("Could not create marshaller", e);
				throw e;
				}
		return marshaller;
		}
	
	
	@Provides
	JAXBContext provideJAXBContext() throws JAXBException{
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.generated.MaterialExchange");
			} catch (JAXBException e) {
				logger.fatal("Could not create jaxb context", e);
				throw e;
			}
		return jc;
		}
}
