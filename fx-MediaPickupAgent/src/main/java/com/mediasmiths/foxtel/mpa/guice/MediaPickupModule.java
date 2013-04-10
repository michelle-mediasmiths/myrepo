package com.mediasmiths.foxtel.mpa.guice;

import static com.mediasmiths.foxtel.agent.Config.WATCHFOLDER_LOCATIONS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.WatchFolder;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;
import com.mediasmiths.foxtel.agent.queue.FilePickUpFromDirectories;
import com.mediasmiths.foxtel.agent.queue.FilePickUpProcessingQueue;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;
import com.mediasmiths.foxtel.agent.validation.SchemaValidator;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.mpa.MediaPickupAgent;
import com.mediasmiths.foxtel.mpa.validation.MediaPickupAgentConfigValidator;

public class MediaPickupModule extends AbstractModule {

	private static Logger logger = Logger
			.getLogger(MediaPickupModule.class);
	
	@Override
	protected void configure() {
		bind(ConfigValidator.class).to(MediaPickupAgentConfigValidator.class);
		bind(FilePickUpProcessingQueue.class).to(FilePickUpFromDirectories.class);
		bind(MediaPickupAgent.class).asEagerSingleton();
	}

	protected static final TypeLiteral<MessageProcessor<Material>> MESSAGEPROCESSOR_LITERAL =  new TypeLiteral<MessageProcessor<Material>>(){};

	
	@Provides
	public JAXBContext provideJAXBContext() throws JAXBException{
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.generated.MaterialExchange");
		} catch (JAXBException e) {
			logger.fatal("Could not create jaxb context", e);
			throw e;
		}
		return jc;
	}
	
	
	@Provides
	public Unmarshaller provideUnmarshaller(JAXBContext jc) throws JAXBException {
		
		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = jc.createUnmarshaller();
		} catch (JAXBException e) {
			logger.fatal("Could not create unmarshaller", e);
			throw e;
		}
		return unmarshaller;
	}

	@Provides
	public Marshaller provideMarshaller(JAXBContext jc, @Named("schema.location") String schemaLocation) throws JAXBException, SAXException {
		
		Marshaller marshaller = null;
		try {
			marshaller = jc.createMarshaller();
			
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			SchemaFactory factory = SchemaFactory
					.newInstance("http://www.w3.org/2001/XMLSchema");
			Schema schema = factory.newSchema(getClass().getClassLoader()
					.getResource(schemaLocation));
			marshaller.setSchema(schema);
			
		} catch (JAXBException e) {
			logger.fatal("Could not create marshaller", e);
			throw e;
		}
		return marshaller;
	}
	
	@Provides
	@Named("ruzz.jaxb.context")
	public JAXBContext provideRuzzJaxBContext() throws JAXBException{
		JAXBContext jc = null;
		try {
			jc = JAXBContext.newInstance("com.mediasmiths.foxtel.generated.ruzz");
		} catch (JAXBException e) {
			logger.fatal("Could not create jaxb context", e);
			throw e;
		}
		return jc;
	}
	
	
	@Provides
	@Named("ruzz.unmarshaller")
	public Unmarshaller provideRuzzUnmarshaller(@Named("ruzz.jaxb.context")JAXBContext jc) throws JAXBException {
		
		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = jc.createUnmarshaller();
		} catch (JAXBException e) {
			logger.fatal("Could not create unmarshaller", e);
			throw e;
		}
		return unmarshaller;
	}
	
	@Provides
	@Named("ruzz.marshaller")
	public Marshaller provideRuzzMarshaller(@Named("ruzz.jaxb.context")JAXBContext jc, @Named("ruzz.schema.location") String schemaLocation) throws JAXBException, SAXException {
		Marshaller marshaller = null;
		try {
			marshaller = jc.createMarshaller();
			
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			SchemaFactory factory = SchemaFactory
					.newInstance("http://www.w3.org/2001/XMLSchema");
			Schema schema = factory.newSchema(getClass().getClassLoader()
					.getResource(schemaLocation));
			marshaller.setSchema(schema);
			
		} catch (JAXBException e) {
			logger.fatal("Could not create marshaller", e);
			throw e;
		}
		return marshaller;
	}
	
	@Provides
	@Named("ruzz.schema.validator") 
	public SchemaValidator ruzzSchemaValidator(@Named("ruzz.schema.location") String schemaLocation) throws SAXException{
		SchemaValidator sv = new SchemaValidator(schemaLocation);
		return sv;
	}
	
	@Singleton
	@Provides
	@Named("ruzz.watched.directories")
	public File[] provideRuzzWatchedDirectories(@Named(WATCHFOLDER_LOCATIONS) WatchFolders watchFolders)
	{
		
		return getFileArrayOfWatchFolders(watchFolders, true);

	}
	
	@Singleton
	@Provides
	@Named("mex.watched.directories")
	public File[] provideMexWatchedDirectories(@Named(WATCHFOLDER_LOCATIONS) WatchFolders watchFolders)
	{
		return getFileArrayOfWatchFolders(watchFolders, false);

	}

	private File[] getFileArrayOfWatchFolders(WatchFolders watchFolders, boolean isRuzz)
	{
		if (watchFolders == null || watchFolders.isEmpty())
		{
			logger.error("No Directory Watched Paths defined in filepickup.watched.directoryNames");

			throw new IllegalArgumentException("No Directory Watched Paths defined in filepickup.watched.directoryNames");
		}

		List<File> watchedPaths = new ArrayList<File>();

		for (int i = 0; i < watchFolders.size(); i++)
		{
			WatchFolder wf = watchFolders.get(i);
			if (wf.isRuzz() == isRuzz)
			{
				File source = new File(wf.getSource());
				if (!acceptableFilePermissions(source))
				{
					logger.error("Watched path is not usable: " +source);

					throw new IllegalArgumentException("Watched path is not usable: " + source);

				}
				else{
					watchedPaths.add(source);
				}
			}
		}

		return watchedPaths.toArray(new File[watchedPaths.size()]);
	}

	/**
	 *
	 * @param fileRef of the file being considered
	 * @return true if the file has permissions that will enable its correct processing by a FileProcessing instance
	 */
	private boolean acceptableFilePermissions(final File fileRef)
	{
		return fileRef.exists() && fileRef.isDirectory() && fileRef.canRead() && fileRef.canWrite();
	}

}
