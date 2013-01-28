//package com.mediasmiths.mq.guice;
//
//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.JAXBException;
//import javax.xml.bind.Marshaller;
//
//import org.apache.log4j.Logger;
//import org.xml.sax.SAXException;
//
//import com.google.inject.AbstractModule;
//import com.google.inject.Provides;
//import com.google.inject.name.Named;
//
//public class JaxModule extends AbstractModule
//{
//
//	private final static Logger logger = Logger.getLogger(JaxModule.class);
//	
//	@Override
//	protected void configure()
//	{
//		
//	}
//	
//	@Provides
//	@Named("wfe.marshaller")
//	Marshaller provideWFEMarshaller(@Named("wfe.context") JAXBContext jc) throws JAXBException, SAXException
//	{
//		Marshaller marshaller = null;
//		try
//		{
//			marshaller = jc.createMarshaller();
//			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//
//		}
//		catch (JAXBException e)
//		{
//			logger.fatal("Could not create marshaller", e);
//			throw e;
//		}
//		return marshaller;
//	}
//	
//	@Provides
//	@Named("wfe.context")
//	JAXBContext provideWFEJAXBContext() throws JAXBException
//	{
//		JAXBContext jc = null;
//		try
//		{
//			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.wf.adapter.model");
//		}
//		catch (JAXBException e)
//		{
//			logger.fatal("Could not create jaxb context", e);
//			throw e;
//		}
//		return jc;
//	}
//
//}
