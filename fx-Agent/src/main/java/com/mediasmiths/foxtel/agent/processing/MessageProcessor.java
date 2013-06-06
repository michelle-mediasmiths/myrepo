package com.mediasmiths.foxtel.agent.processing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.ReceiptWriter;
import com.mediasmiths.foxtel.agent.WatchFolders;
import com.mediasmiths.foxtel.agent.queue.IFilePickup;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResultPackage;
import com.mediasmiths.foxtel.agent.validation.MessageValidator;
import com.mediasmiths.std.guice.common.shutdown.iface.StoppableService;
import com.mediasmiths.std.threading.Daemon;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Processes messages taken from a queue
 * 
 * @author Mediasmiths Forge
 * @see MessageValidator
 * 
 */
public abstract class MessageProcessor<T> extends Daemon implements StoppableService
{

	private static Logger logger = Logger.getLogger(MessageProcessor.class);

	protected final Unmarshaller unmarhsaller;
	protected final Marshaller marshaller;

	public final static String FAILUREFOLDERNAME = "failed";
	public final static String ARCHIVEFOLDERNAME = "completed";
	public final static String PROCESSINGFOLDERNAME = "processing";

	private final MessageValidator<T> messageValidator;
	private final ReceiptWriter receiptWriter;
	protected final com.mediasmiths.foxtel.ip.event.EventService eventService;


	@Inject
	@Named("watchfolder.locations")
	WatchFolders watchedFolders;

	private IFilePickup filePickup;

	public IFilePickup getFilePickup()
	{
		return filePickup;
	}

	@Inject
	public MessageProcessor(
			IFilePickup filePickup,
			MessageValidator<T> messageValidator,
			ReceiptWriter receiptWriter,
			Unmarshaller unmarshaller,
			Marshaller marshaller,
			com.mediasmiths.foxtel.ip.event.EventService eventService)
	{
		this.filePickup = filePickup;
		this.unmarhsaller = unmarshaller;
		this.marshaller = marshaller;
		this.messageValidator = messageValidator;
		this.receiptWriter = receiptWriter;
		this.eventService = eventService;
	}


	protected boolean isAOPickUpLocation(PickupPackage pp)
	{
		return watchedFolders.isAo(pp.getRootPath());
	}

	/**
	 * Processes a given filepath : assumes it has already been validated
	 * 
	 * @param pp file pick up path
	 * @throws MessageProcessingFailedException
	 * @returns the messageID of the message at filePath
	 */
	public String processPickupPackage(PickupPackage pp, T message) throws MessageProcessingFailedException
	{

		MessageEnvelope<T> envelope = new MessageEnvelope<T>(pp, message);
		try
		{
			processMessage(envelope);
		}
		catch (MessageProcessingFailedException e)
		{
			logger.error(String.format("Message processing failed for %s and reason %s", pp.getRootName(), e.getReason()), e);

			eventService.saveEvent("Error", message);
			throw e;
		}
		return getIDFromMessage(envelope);
	}

	/**
	 * Called to check the type of an unmarshalled object, left up to implementing classes as (unmarshalled instanceof T) cannot be checked;
	 * 
	 * @param unmarshalled
	 * @throws ClassCastException
	 *             if the unmarshalled object is not of the expected type
	 */
	protected abstract void typeCheck(Object unmarshalled) throws ClassCastException;

	protected abstract String getIDFromMessage(MessageEnvelope<T> envelope);

	protected final void validateThenProcessPickupPackage(PickupPackage pp)
	{

		if (pp.isComplete() || pp.isPickedUpSuffix("xml"))
		{
			logger.info("package is complete or contains xml");

			MessageValidationResultPackage<T> resultPackage;
			MessageValidationResult result;

			try
			{
				logger.debug("Asking for validation of pickup package " + pp.getRootName());
				resultPackage = messageValidator.validatePickupPackage(pp);
			}
			catch (Exception e)
			{
				logger.error("uncaught exception validating file", e);
				resultPackage = new MessageValidationResultPackage<>(pp, MessageValidationResult.UNKOWN_VALIDATION_FAILURE);
			}

			result = resultPackage.getResult();

			if (result == MessageValidationResult.IS_VALID)
			{
				logger.info(String.format("Message at %s validates", pp.getRootName()));
				try
				{
					String messageID = processPickupPackage(pp, resultPackage.getMessage());
					writeReceipt(pp.getPickUp("xml"), messageID);
					postProcessing(pp);

				}
				catch (MessageProcessingFailedException e)
				{
					logger.error(String.format("Error processing %s", pp.getRootName()), e);
					processingError(pp);

				}
				catch (Exception e)
				{
					logger.error(String.format("uncaught exception processing file %s", pp.getRootName()), e);
					processingError(pp);
				}
			}
			else
			{
				logger.warn(String.format("Message at %s did not validate", pp.getRootPath()));
				messageValidationFailed(resultPackage);
			}
		}
		else
		{
			processPickupPackageNoXML(pp);
		}
	}

	protected void processPickupPackageNoXML(PickupPackage pp)
	{
		logger.info("received a pickup package with no xml, not doing anything with it");
		logger.debug("MP test 1");
	}

	protected void processingError(PickupPackage pp)
	{
		moveFileToFailureFolder(pp.getPickUp("xml"));
	}

	protected void postProcessing(PickupPackage pp)
	{
		if (shouldArchiveMessages())
		{
			moveMessageToArchiveFolder(pp.getPickUp("xml"));
		}
	}

	/**
	 * informs MessageProcesors of a message validation failure, does not move messages to the failure folder
	 * 
	 * @param resultPackage
	 */
	protected abstract void messageValidationFailed(MessageValidationResultPackage<T> resultPackage);

	protected abstract void processMessage(MessageEnvelope<T> envelope) throws MessageProcessingFailedException;

	/**
	 * Moves erroneous messages to a configurable location
	 * 
	 * @param file
	 */
	protected void moveFileToFailureFolder(File file)
	{

		logger.info(String.format("File %s, sending to failure folder", file.getAbsolutePath()));
		String failurePath = getFailureFolderForFile(file);

		logger.debug(String.format("Failure folder is: %s ", failurePath));

		try
		{
			moveFileToFolder(file, failurePath, true);
		}
		catch (IOException e)
		{
			logger.error(String.format("IOException moving invalid file %s to %s", file.getAbsolutePath(), failurePath), e);
		}
	}

	public static String getFailureFolderForFile(File file)
	{

		String pathToFile = file.getAbsolutePath();
		String failurePath = FilenameUtils.getFullPath(pathToFile) + FAILUREFOLDERNAME + IOUtils.DIR_SEPARATOR;
		logger.debug(String.format("returning failure folder %s for file %s ", failurePath, pathToFile));

		return failurePath;
	}

	/**
	 * Moves messages which have been processed, to the archive path
	 * 
	 * @param file
	 */
	protected void moveMessageToArchiveFolder(File file)
	{
		logger.info(String.format("Message %s is complete, sending to archive folder", file));
		String archivePath = getArchivePathForFile(file.getAbsolutePath());
		logger.debug(String.format("Archive folder is: %s ", archivePath));

		try
		{
			moveFileToFolder(file, archivePath, true);
		}
		catch (IOException e)
		{

			logger.warn(String.format("IOException moving message %s to archive %s", file, archivePath), e);
		}

	}

	public static String getArchivePathForFile(String messagePath)
	{

		String pathToFile = messagePath;

		String archivePath = FilenameUtils.getFullPath(pathToFile) + ARCHIVEFOLDERNAME + IOUtils.DIR_SEPARATOR;
		logger.debug(String.format("returning archivePath folder %s for file %s ", archivePath, pathToFile));

		return archivePath;
	}

	protected synchronized String moveFileToFolder(File file, String destinationFolderPath, boolean ensureUniqueFirst)
			throws IOException
	{
		final String destination = getDestinationPathForFileMove(file, destinationFolderPath, ensureUniqueFirst);

		logger.trace(String.format("Moving file from %s to %s", file.getAbsolutePath(), destination));

		FileUtils.moveFile(file, new File(destination));
		return destination;
	}

	public static String getDestinationPathForFileMove(File file, String destinationFolderPath, boolean ensureUnique)
	{

		StringBuilder sb = new StringBuilder(destinationFolderPath);

		// only add slash if required
		if (destinationFolderPath.charAt(destinationFolderPath.length() - 1) != IOUtils.DIR_SEPARATOR)
		{
			sb.append(IOUtils.DIR_SEPARATOR);
		}
		sb.append(FilenameUtils.getName(file.getAbsolutePath()));

		String destination = sb.toString();

		if (ensureUnique)
		{
			File dest = new File(destination);

			int i = 1;

			while (dest.exists())
			{
				logger.warn(String.format("Destination file %s already exists", destination));

				sb = new StringBuilder(destinationFolderPath);
				sb.append(IOUtils.DIR_SEPARATOR);
				sb.append(FilenameUtils.getBaseName(file.getAbsolutePath()));
				sb.append("_");
				sb.append(i);
				sb.append(FilenameUtils.EXTENSION_SEPARATOR);
				sb.append(FilenameUtils.getExtension(file.getAbsolutePath()));
				destination = sb.toString();
				dest = new File(destination);
				i++;
			}
		}
		logger.trace("returning destination" + destination);
		return destination;
	}

	private void writeReceipt(File file, String messageID)
	{
		try
		{
			receiptWriter.writeRecipet(file.getAbsolutePath(), messageID);
		}
		catch (IOException e)
		{
			logger.error("Failed to write receipt for message" + messageID);
		}
	}

	@Override
	public void run()
	{

		logger.info("MessageProcessor run");
		
		while (isRunning())
		{
			try
			{
				PickupPackage pp = filePickup.take();
				validateThenProcessPickupPackage(pp);
				
				for (File f : pp.getAllFiles())
				{
					if(f.exists()){
						logger.warn(String.format("%s still exists after processing!",f.getAbsolutePath()));
					}
				}
			}
			catch (Exception e)
			{
				logger.fatal(
						"Uncaught exception almost killed MessageProcessor thread, if there was not a shutdown in progress then this is very bad",
						e);

				if (isRunning())
				{
					eventService.saveEvent("error", "Uncaught exception almost killed MessageProcessor");
				}
			}
		}

	}

	protected abstract boolean shouldArchiveMessages();

	protected boolean isMessage(String filePath)
	{
		return FilenameUtils.getExtension(filePath).toLowerCase(Locale.ENGLISH).equals("xml");
	}

	@Override
	protected boolean shouldStartAsDaemon()
	{
		return true;
	}

	@Override
	public void shutdown()
	{
		stopThread();
		filePickup.shutdown();
	}

}
