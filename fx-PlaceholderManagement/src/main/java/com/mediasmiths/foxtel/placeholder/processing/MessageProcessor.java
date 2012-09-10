package com.mediasmiths.foxtel.placeholder.processing;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.placeholder.validation.MessageValidator;
import com.mediasmiths.mayam.MayamClient;

/**
 * Processes placeholder messages taken from a queue, performs no validation!
 * 
 * @author Mediasmiths Forge
 * @see MessageValidator
 * 
 */
public class MessageProcessor implements Runnable {

	private static Logger logger = Logger
			.getLogger(MessageProcessor.class);

	private final LinkedBlockingQueue<String> filePathsPendingProcessing;
	private boolean stopRequested = false;

	private final Unmarshaller unmarhsaller;
	private final MayamClient mayamClient;

	public MessageProcessor(
			LinkedBlockingQueue<String> filePathsPendingProcessing,
			Unmarshaller unmarhsaller, MayamClient mayamClient) {
		this.filePathsPendingProcessing = filePathsPendingProcessing;
		this.unmarhsaller = unmarhsaller;
		this.mayamClient = mayamClient;
	}

	@Override
	public void run() {

		while (!stopRequested) {
			try {
				String filePath = filePathsPendingProcessing.take();

				try {
					processFile(filePath);
				} catch (Exception e) {
					logger.error(
							String.format("Error processing %s", filePath), e);
				}

			} catch (InterruptedException e) {
				logger.warn("Interruped!", e);
			}
		}

	}

	private void processFile(String filePath) {
		// we assume that the file at filePath has already been validated

		try {
			
			Object unmarshalled = unmarhsaller.unmarshal(new File(filePath));
			logger.debug(String.format("unmarshalled object of type %s",
					unmarshalled.getClass().toString()));
			
			PlaceholderMessage message = (PlaceholderMessage) unmarshalled;
			
			Object action = message.getActions()
					.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial()
					.get(0);

			boolean isCreateOrUpdateTitle = (action instanceof CreateOrUpdateTitle);
			boolean isPurgeTitle = (action instanceof PurgeTitle);
			boolean isAddOrUpdateMaterial = (action instanceof AddOrUpdateMaterial);
			boolean isDeleteMaterial = (action instanceof DeleteMaterial);
			boolean isAddOrUpdatePackage = (action instanceof AddOrUpdatePackage);
			boolean isDeletePackage = (action instanceof DeletePackage);

			if (isCreateOrUpdateTitle) {
				createOrUpdateTitle((CreateOrUpdateTitle) action);
			} else if (isPurgeTitle) {
				purgeTitle((PurgeTitle) action);
			} else if (isAddOrUpdateMaterial) {
				addOrUpdateMaterial((AddOrUpdateMaterial) action);
			} else if (isDeleteMaterial) {
				deleteMaterial((DeleteMaterial) action);
			} else if (isAddOrUpdatePackage) {
				addOrUpdatePackage((AddOrUpdatePackage) action);
			} else if (isDeletePackage) {
				deletePackage((DeletePackage) action);
			}

		} catch (JAXBException e) {
			logger.fatal("A previously validated file did not unmarshall sucessfully, this is very bad");
		} catch (ClassCastException cce){
			logger.fatal("A prevously validated file did not have an action of one of the expected types");
		}
		
	}

	private void deletePackage(DeletePackage action) {
		
	}

	private void addOrUpdatePackage(AddOrUpdatePackage action) {
		// TODO Auto-generated method stub
	}

	private void deleteMaterial(DeleteMaterial action) {
		// TODO Auto-generated method stub
	}

	private void addOrUpdateMaterial(AddOrUpdateMaterial action) {
		// TODO Auto-generated method stub
	}

	private void purgeTitle(PurgeTitle action) {
		// TODO Auto-generated method stub
	}

	private void createOrUpdateTitle(CreateOrUpdateTitle action) {
		// TODO Auto-generated method stub
	}

	public void stop() {
		stopRequested = true;
	}
}
