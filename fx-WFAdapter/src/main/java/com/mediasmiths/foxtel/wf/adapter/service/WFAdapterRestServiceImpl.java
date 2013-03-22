package com.mediasmiths.foxtel.wf.adapter.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIF;
import com.mediasmiths.foxtel.ip.common.events.TcNotification;
import com.mediasmiths.foxtel.ip.common.events.TxDelivered;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCErrorNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCPassNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCResultNotification;
import com.mediasmiths.foxtel.wf.adapter.model.GetQCProfileResponse;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.TCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCPassedNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCTotalFailure;
import com.mediasmiths.foxtel.wf.adapter.model.TXDeliveryFailure;
import com.mediasmiths.foxtel.wf.adapter.model.TXDeliveryFinished;
import com.mediasmiths.foxtel.wf.adapter.util.TxUtil;
import com.mediasmiths.mayam.MayamButtonType;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

public class WFAdapterRestServiceImpl implements WFAdapterRestService
{
	private static final String TC_EVENT_NAMESPACE = "http://www.foxtel.com.au/ip/tc";
	private static final String QC_EVENT_NAMESPACE = "http://www.foxtel.com.au/ip/qc";
	private static final String TX_EVENT_NAMESPACE = "http://www.foxtel.com.au/ip/delivery";

	private final static Logger log = Logger.getLogger(WFAdapterRestServiceImpl.class);

	@Inject
	private EventService events;

	@Inject
	private MayamClient mayamClient;
	@Inject
	private QcProfileSelector qcProfileSelector;
	@Inject
	@Named("tx.delivery.location")
	private String txDeliveryLocation;
	@Inject
	@Named("ao.tx.delivery.location")
	private String aoTxDeliveryLocation;

	@Inject
	@Named("mex.context")
	private JAXBContext mexContext;
	@Inject
	@Named("mex.marshaller")
	private Marshaller mexMarshaller;
	@Inject
	@Named("ruzz.marshaller")
	private Marshaller ruzzMarshaller;
	@Inject
	@Named("wfe.marshaller")
	private Marshaller wfeMarshaller;

	@Inject
	@Named("cerify.report.location")
	private String cerifyReportLocation;
	@Inject
	@Named("cerify.report.location.ardome")
	private String ardomeCerifyReportLocation;
	@Inject
	@Named("cerfiy.report.ardomehandle")
	private String cerifyReportArdomeserviceHandle;
	@Inject
	@Named("cerify.report.attatch")
	private boolean attachQcReports;

	@Inject
	@Named("ao.tx.delivery.ftp.proxy.host")
	private String aoFTPProxyHost;
	@Inject
	@Named("ao.tx.delivery.ftp.proxy.user")
	private String aoFTPProxyUser;
	@Inject
	@Named("ao.tx.delivery.ftp.proxy.pass")
	private String aoFTPProxyPass;

	@Inject
	@Named("ao.tx.delivery.ftp.gxf.host")
	private String aoGXFFTPDestinationHost;
	@Inject
	@Named("ao.tx.delivery.ftp.gxf.user")
	private String aoGXFFTPDestinationUser;
	@Inject
	@Named("ao.tx.delivery.ftp.gxf.pass")
	private String aoGXFFTPDestinationPass;
	@Inject
	@Named("ao.tx.delivery.ftp.gxf.path")
	private String aoGXFFTPDestinationPath;

	@Inject
	@Named("ao.tx.delivery.ftp.xml.host")
	private String aoXMLFTPDestinationHost;
	@Inject
	@Named("ao.tx.delivery.ftp.xml.user")
	private String aoXMLFTPDestinationUser;
	@Inject
	@Named("ao.tx.delivery.ftp.xml.pass")
	private String aoXMLFTPDestinationPass;
	@Inject
	@Named("ao.tx.delivery.ftp.xml.path")
	private String aoXMLFTPDestinationPath;

	@Inject
	@Named("ao.tx.delivery.ftp.xml.source.path")
	private String aoXMLFTPSourcePath = "ready/";
	@Inject
	@Named("ao.tx.delivery.ftp.gxf.source.path")
	private String aoGXFFTPSourcePath = "ready/";

	@Override
	public String ping()
	{
		log.info("Ping request receivied");
		return "ping";
	}

	@Override
	public AssetTransferForQCResponse transferMaterialForQC(final AssetTransferForQCRequest req) throws MayamClientException
	{
		// doesnt actually do a transfer! just returns location (method needs renamed)
		log.info("Received AssetTransferForQCRequest " + req.toString());
		final String id = req.getAssetId();
		File destinationFile;

		if (req.isForTXDelivery())
		{
			log.debug("Material is for TX delivery");
			boolean packageAO = mayamClient.isPackageAO(id);
			String tcoutputlocation = TxUtil.deliveryLocationForPackage(
					id,
					mayamClient,
					txDeliveryLocation,
					aoTxDeliveryLocation,
					packageAO);

			// for tx delivery we return the location of transcoded package
			String ret = tcoutputlocation + id + "/" + id + ".gxf";
			destinationFile = new File(ret);
		}
		else
		{
			log.debug("Material is NOT for TX delivery");
			final String destination = mayamClient.pathToMaterial(id);
			log.debug(String.format("Material destination set to %s", destination));
			destinationFile = new File(destination);
		}

		return new AssetTransferForQCResponse(destinationFile.getAbsolutePath());
	}

	@Override
	public GetQCProfileResponse getProfileForQc(final String assetID, final boolean isForTXDelivery) throws MayamClientException
	{
		GetQCProfileResponse resp = new GetQCProfileResponse();
		String profile = qcProfileSelector.getProfileFor(assetID, isForTXDelivery);
		resp.setProfile(profile);
		return resp;
	}

	@Override
	public void notifyAutoQCFailed(final AutoQCFailureNotification notification) throws MayamClientException
	{
		log.info(String.format(
				"Received notification of Auto QC failure ID %s isTX %b",
				notification.getAssetId(),
				notification.isForTXDelivery()));

		try
		{
			if (notification.isForTXDelivery())
			{
				// id is a package id
				saveEvent("QCProblemwithTCMedia", notification, QC_EVENT_NAMESPACE);
				mayamClient.txDeliveryFailed(notification.getAssetId(), notification.getTaskID(), "AUTO QC FAILED");
				// attach qc report if qc is failed
				attachQcReports(notification.getAssetId(), notification.getJobName());
			}
			else
			{
				// id is an item id
				saveEvent("QcFailedReOrder", notification, QC_EVENT_NAMESPACE);
				mayamClient.autoQcFailedForMaterial(notification.getAssetId(), notification.getTaskID());
				attachQcReports(notification.getAssetId(), notification.getJobName());
			}
		}
		catch (MayamClientException e)
		{
			log.error("Failed to fail task!", e);
			throw e;
		}
	}

	@Override
	public MaterialTransferForTCResponse transferMaterialForTC(MaterialTransferForTCRequest req) throws MayamClientException
	{
		log.info("Received MaterialTransferForTCRequest " + req.toString());
		final String assetID = req.getAssetID();
		String materialID;

		// we dont actually need to transfer the media, just get its location
		if (req.isForTX())
		{
			materialID = mayamClient.getMaterialIDofPackageID(assetID);
		}
		else
		{
			materialID = assetID;
		}

		String path = mayamClient.pathToMaterial(materialID);

		return new MaterialTransferForTCResponse(path);
	}

	@Override
	public void notifyAutoQCPassed(AutoQCPassNotification notification) throws MayamClientException
	{
		log.info(String.format(
				"Received notification of Auto QC Pass ID %s isTX %b",
				notification.getAssetId(),
				notification.isForTXDelivery()));

		// comment out as version 7 220213
		// saveEvent("AutoQCPassed", notification, QC_EVENT_NAMESPACE);

		if (notification.isForTXDelivery())
		{
			// attach qc report if qc is passed
			attachQcReports(notification.getAssetId(), notification.getJobName());
			// update tasks status as required, next stage will be kicked off by intalio
		}
		else
		{
			mayamClient.autoQcPassedForMaterial(notification.getAssetId(), notification.getTaskID());
			attachQcReports(notification.getAssetId(), notification.getJobName());

		}
	}

	private void attachQcReports(final String assetID, final String jobName) throws MayamClientException
	{
		// find report pdfs
		IOFileFilter reportPDFAcceptor = new IOFileFilter()
		{

			@Override
			public boolean accept(File dir, String name)
			{
				return false; // dont look in sub directories
			}

			@Override
			public boolean accept(File file)
			{
				String name = FilenameUtils.getName(file.getAbsolutePath());
				if (name.startsWith(jobName))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		};

		Collection<File> reports = FileUtils.listFiles(new File(cerifyReportLocation), reportPDFAcceptor, reportPDFAcceptor);
		// attach report(s) to item

		for (File file : reports)
		{
			if (attachQcReports)
			{
				String ardomeFilePath = ardomeCerifyReportLocation + file.getName();

				log.info(String.format(
						"attach file {%s} to material using ardome path {%s}",
						file.getAbsolutePath(),
						ardomeFilePath));
				mayamClient.attachFileToMaterial(assetID, ardomeFilePath, cerifyReportArdomeserviceHandle);
			}
		}
	}

	@Override
	public void notifyAutoQCError(AutoQCErrorNotification notification) throws MayamClientException
	{
		log.info(String.format(
				"Received notification of Auto QC Error ID %s isTX %b",
				notification.getAssetId(),
				notification.isForTXDelivery()));
		try
		{
			if (notification.isForTXDelivery())
			{
				// auto qc was for tx delivery
				saveEvent("CerifyQCError", notification, QC_EVENT_NAMESPACE);
				mayamClient.txDeliveryFailed(notification.getAssetId(), notification.getTaskID(), "AUTO QC ERROR");
			}
			else
			{
				// auto qc was for qc task
				saveEvent("CerifyQCError", notification, QC_EVENT_NAMESPACE);
				mayamClient.autoQcFailedForMaterial(notification.getAssetId(), notification.getTaskID());
			}
		}
		catch (MayamClientException e)
		{
			log.error("Failed to fail task!", e);
			throw e;
		}
	}

	@Override
	public void notifyTCFailedTotal(TCTotalFailure notification) throws MayamClientException
	{
		log.info(String.format(
				"Received notification of transcode Error asset ID %s isTX %b",
				notification.getAssetID(),
				notification.isForTXDelivery()));

		// based on email notifications version 7 220213
		// saveEvent("PersistentFailure", notification, TC_EVENT_NAMESPACE, new TcNotification());

		try
		{
			if (notification.isForTXDelivery())
			{
				// auto qc was for tx delivery
				mayamClient.txDeliveryFailed(notification.getAssetID(), notification.getTaskID(), "TRANSCODE");
			}
			else
			{
				// auto qc was for export task
				mayamClient.exportFailed(notification.getTaskID());
			}
		}
		catch (MayamClientException e)
		{
			log.error("Failed to fail task!", e);
			throw e;
		}

	}

	@Override
	public void notifyTCFailed(TCFailureNotification notification) throws MayamClientException
	{

		log.info(String.format("Received notification of TC failure asset id %s", notification.getAssetID()));
		long taskId = notification.getTaskID();
		AttributeMap task = mayamClient.getTask(taskId);

		String taskListID = task.getAttribute(Attribute.TASK_LIST_ID);

		if (taskListID.equals(MayamButtonType.CAPTION_PROXY.getText()))
		{
			saveEvent(
					"CaptionProxyFailure",
					notification,
					TC_EVENT_NAMESPACE,
					new com.mediasmiths.foxtel.ip.common.events.TcNotification(),
					false);
		}
		else if (taskListID.equals(MayamButtonType.PUBLICITY_PROXY.getText()))
		{
			saveEvent(
					"ClassificationProxyFailure",
					notification,
					TC_EVENT_NAMESPACE,
					new com.mediasmiths.foxtel.ip.common.events.TcNotification(),
					false);
		}
		else if (taskListID.equals(MayamButtonType.COMPLIANCE_PROXY.getText()))
		{
			saveEvent(
					"ComplianceProxyFailure",
					notification,
					TC_EVENT_NAMESPACE,
					new com.mediasmiths.foxtel.ip.common.events.TcNotification(),
					false);
		}
		else
		{
			saveEvent(
					"TCFailed",
					notification,
					TC_EVENT_NAMESPACE,
					new com.mediasmiths.foxtel.ip.common.events.TcNotification(),
					false);
		}
	}

	@Override
	public void notifyTCPassed(TCPassedNotification notification) throws MayamClientException
	{
		log.info(String.format("Received notification of TC passed asset id %s", notification.getAssetID()));

		long taskId = notification.getTaskID();
		AttributeMap task = mayamClient.getTask(taskId);

		String taskListID = task.getAttribute(Attribute.TASK_LIST_ID);

		if (taskListID.equals(MayamButtonType.CAPTION_PROXY.getText()))
		{
			saveEvent(
					"CaptionProxySuccess",
					notification,
					TC_EVENT_NAMESPACE,
					new com.mediasmiths.foxtel.ip.common.events.TcNotification(),
					true);
		}
		else if (taskListID.equals(MayamButtonType.PUBLICITY_PROXY.getText()))
		{
			saveEvent(
					"ClassificationProxySuccess",
					notification,
					TC_EVENT_NAMESPACE,
					new com.mediasmiths.foxtel.ip.common.events.TcNotification(),
					true);
		}
		else if (taskListID.equals(MayamButtonType.COMPLIANCE_PROXY.getText()))
		{
			saveEvent(
					"ComplianceProxySuccess",
					notification,
					TC_EVENT_NAMESPACE,
					new com.mediasmiths.foxtel.ip.common.events.TcNotification(),
					true);
		}
		else
		{
			// saveEvent("Transcoded", notification, TC_EVENT_NAMESPACE, new com.mediasmiths.foxtel.ip.common.events.TcNotification());
		}

		if (!notification.isForTXDelivery())
		{
			mayamClient.exportCompleted(notification.getTaskID());
		}
	}

	@Override
	public Boolean autoQCRequiredForTxTask(final Long taskID) throws MayamClientException
	{

		return Boolean.valueOf(mayamClient.autoQcRequiredForTXTask(taskID));

	}

	@Override
	public void notifyTXDeliveryFailed(TXDeliveryFailure notification) throws MayamClientException
	{
		log.fatal(String.format(
				"TX DELIVERY FAILURE FOR PACKAGE %s AT STAGE %s",
				notification.getPackageID(),
				notification.getStage()));

		mayamClient.txDeliveryFailed(notification.getPackageID(), notification.getTaskID(), notification.getStage());

		TxDelivered d = new TxDelivered();
		d.setPackageId(notification.getPackageID());
		d.setStage(notification.getStage());
		d.setTaskId(notification.getTaskID() + "");

		// events.saveEvent(TX_EVENT_NAMESPACE, "DeliveryFailed", d);

	}

	@Override
	public String deliveryLocationForPackage(final String packageID) throws MayamClientException
	{

		boolean packageAO = mayamClient.isPackageAO(packageID);
		return TxUtil.deliveryLocationForPackage(packageID, mayamClient, txDeliveryLocation, aoTxDeliveryLocation, packageAO);
	}

	@Override
	public boolean writeSegmentXML(final String packageID) throws MayamClientException, IOException, JAXBException
	{
		log.debug(String.format("Writing segment xml for package %s", packageID));

		String companion;

		boolean ao = mayamClient.isPackageAO(packageID);

		if (ao)
		{
			log.debug(String.format("Getting AO xml for packageId %s", packageID));
			companion = getAOSegmentXML(packageID);
		}
		else
		{
			log.debug(String.format("Getting xml for packageId %s", packageID));
			companion = getSegmentXML(packageID);
		}
		String deliveryLocation = deliveryLocationForPackage(packageID);

		File deliveryLocationFile = new File(deliveryLocation);
		deliveryLocationFile.mkdirs();

		File segmentXmlFile = new File(String.format("%s/%s.xml", deliveryLocation, packageID));
		File gxfFile = new File(String.format("%s/%s.gxf", deliveryLocation, packageID));
		try
		{
			log.debug("Writing segmentinfo to " + segmentXmlFile);
			log.debug("Segment info is " + companion);
			FileUtils.writeStringToFile(segmentXmlFile, companion);

			if (ao)
			{
				// should probably be its own intalio workflow step but doing it here for now
				return aoFXPtransfer(segmentXmlFile, gxfFile);
			}

			return true;
		}
		catch (IOException e)
		{
			log.error(String.format("Error writing companion xml for package %s", packageID), e);
			throw e;
		}

	}

	private boolean aoFXPtransfer(File segmentXmlFile, File gxfFile)
	{

		String segmentFileName = FilenameUtils.getName(segmentXmlFile.getAbsolutePath());
		String gxfFileName = FilenameUtils.getName(gxfFile.getAbsolutePath());

		// first upload xml
		boolean xmlUpload = ftpProxyTransfer(
				aoXMLFTPSourcePath,
				segmentFileName,
				aoXMLFTPDestinationPath,
				segmentFileName,
				aoXMLFTPDestinationHost,
				aoXMLFTPDestinationUser,
				aoXMLFTPDestinationPass);

		if (xmlUpload)
		{
			// next upload gxf
			boolean gxfUpload = ftpProxyTransfer(
					aoGXFFTPSourcePath,
					gxfFileName,
					aoGXFFTPDestinationPath,
					gxfFileName,
					aoGXFFTPDestinationHost,
					aoGXFFTPDestinationUser,
					aoGXFFTPDestinationPass);

			if (gxfUpload)
			{
				log.info("gxf upload complete");
				return true;
			}
			else
			{
				log.error("gxf upload failed");
				return false;
			}
		}
		else
		{
			log.error("xml segment upload failed");
			return false;
		}
	}

	private boolean ftpProxyTransfer(
			String sourcePath,
			String sourceFileName,
			String targetPath,
			String targetFile,
			String targetHost,
			String targetUser,
			String targetPass)
	{
		return Fxp.ftpProxyTransfer(
				sourcePath,
				sourceFileName,
				aoFTPProxyHost,
				aoFTPProxyUser,
				aoFTPProxyPass,
				targetPath,
				targetFile,
				targetHost,
				targetUser,
				targetPass);
	}

	@Override
	public String getAOSegmentXML(final String packageID) throws MayamClientException, JAXBException
	{

		RuzzIF ruzzProgramme = mayamClient.getRuzzProgramme(packageID);

		StringWriter sw = new StringWriter();
		ruzzMarshaller.marshal(ruzzProgramme, sw);
		return sw.toString();

	}

	@Override
	public String getSegmentXML(final String packageID) throws MayamClientException, JAXBException
	{
		log.debug(">>>getSegmentXML");
		Programme programme = mayamClient.getProgramme(packageID);

		// Validate the programme type returned from mayam information against the schema and log errors
		/*
		 * log.debug(String.format("Validating programme information against the schema for programme with packageId %s", packageID)); if(!validateProgrammeInformation(programme)) { //TODO - could
		 * stop the xml being written / stop task processing here...
		 * log.error(String.format("The information being written about the programme with packageId %s is not valid according to the schema.", packageID)); }
		 */

		StringWriter sw = new StringWriter();
		mexMarshaller.marshal(programme, sw);
		return sw.toString();
	}

	@Override
	public void notifiyTXDelivered(final TXDeliveryFinished deliveryFinished) throws MayamClientException
	{
		mayamClient.txDeliveryCompleted(deliveryFinished.getPackageID(), deliveryFinished.getTaskID());

		// TxDelivered txDelivered = new TxDelivered();
		// txDelivered.setPackageId(deliveryFinished.getPackageID());
		// txDelivered.setTaskId(deliveryFinished.getTaskID() + "");
		// events.saveEvent("http://www.foxtel.com.au/ip/delivery", "Delivered", txDelivered);
	}

	@Override
	public boolean isTaskCancelled(final long taskid) throws MayamClientException
	{
		TaskState state = mayamClient.getTaskState(taskid);
		return state == TaskState.REJECTED || state == TaskState.REMOVED;
	}

	@Override
	public Integer deliveryVersionForMaterial(final String materialID) throws MayamClientException
	{

		if (mayamClient.materialExists(materialID))
		{
			return mayamClient.getLastDeliveryVersionForMaterial(materialID);
		}

		throw new IllegalArgumentException("specified material does not exist");

	}

	@Override
	public String getPurgePendingList() throws MayamClientException
	{
		List<AttributeMap> purgeTasks = mayamClient.getAllPurgeCandidatesPendingDeletion();

		StringBuilder sb = new StringBuilder();
		sb.append("TASK_ID\tASSET_ID\tASSET_TYPE\tASSET_TITLE\tDATE\n");

		for (AttributeMap attributeMap : purgeTasks)
		{
			sb.append(String.format(
					"%s\t%s\t%s\t%s\t%s\n",
					attributeMap.getAttributeAsString(Attribute.TASK_ID),
					attributeMap.getAttributeAsString(Attribute.ASSET_ID),
					attributeMap.getAttributeAsString(Attribute.ASSET_TYPE),
					attributeMap.getAttributeAsString(Attribute.ASSET_TITLE),
					attributeMap.getAttributeAsString(Attribute.OP_DATE)));
		}

		return sb.toString();

	}

	@Override
	public boolean performPendingPerges() throws MayamClientException
	{
		return mayamClient.deletePurgeCandidates();
	}

	// //// -------------------- Eventing

	// temporary serialiser until moved within event service.

	private static JAXBSerialiser JAXB = JAXBSerialiser.getInstance("com.mediasmiths.foxtel.ip.common.events");

	// TC Events

	protected void saveEvent(
			String name,
			TCNotification payload,
			String nameSpace,
			com.mediasmiths.foxtel.ip.common.events.TcNotification eventNotify,
			boolean success)
	{
		try
		{
			eventNotify.setAssetID(payload.getAssetID());
			eventNotify.setTitle(payload.getTitle());
			eventNotify.setTaskID(payload.getTaskID() + "");
			if (success)
			{
				String deliveryLocation = deliveryLocationForPackage(payload.getTaskID() + "");
				eventNotify.setDeliveryLocation(deliveryLocation);
			}
			events.saveEvent(nameSpace, name, eventNotify);

		}
		catch (Exception e)
		{
			log.error("Events unable to serialise message", e);
		}
	}

	// Auto QC Event

	protected void saveEvent(String name, AutoQCResultNotification payload, String nameSpace)
	{
		try
		{
			com.mediasmiths.foxtel.ip.common.events.AutoQCFailureNotification qcErrorNotification = new com.mediasmiths.foxtel.ip.common.events.AutoQCFailureNotification();
			qcErrorNotification.setAssetId(payload.getAssetId());
			qcErrorNotification.setForTXDelivery(payload.isForTXDelivery());
			qcErrorNotification.setTitle(payload.getTitle());
			events.saveEvent(nameSpace, name, qcErrorNotification);
		}
		catch (Exception e)
		{
			e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
		}
	}

	private boolean validateProgrammeInformation(Programme programme)
	{
		boolean isValidProgramme = false;
		try
		{
			SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			Schema schema = factory.newSchema(WFAdapterRestServiceImpl.class.getClassLoader().getResource(
					"MediaExchange_V1.2.xsd"));
			mexMarshaller.setSchema(schema);

			JAXBSource source = new JAXBSource(mexContext, programme);
			Validator validator = schema.newValidator();
			validator.setErrorHandler(new MediaExchangeErrorHandler());
			validator.validate(source);
			isValidProgramme = true;
		}
		catch (SAXException e)
		{
			log.error("A SAXException was thrown whilst validating the returned programme against the schema: " + e.getMessage());
			e.printStackTrace();
		}
		catch (Throwable e)
		{
			log.error("An exception was thrown whilst validating the programme against the schema: " + e.getMessage());
			e.printStackTrace();
		}
		return isValidProgramme;
	}

	static class MediaExchangeErrorHandler implements ErrorHandler
	{
		@Override
		public void warning(SAXParseException exception) throws SAXException
		{
			System.out.println("\nWARNING");
			exception.printStackTrace();
		}

		@Override
		public void error(SAXParseException exception) throws SAXException
		{
			System.out.println("\nERROR");
			exception.printStackTrace();
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException
		{
			System.out.println("\nFATAL ERROR");
			exception.printStackTrace();
		}
	}
}
