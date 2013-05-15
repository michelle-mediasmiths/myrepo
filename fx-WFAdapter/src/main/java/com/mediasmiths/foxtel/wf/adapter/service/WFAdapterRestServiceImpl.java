package com.mediasmiths.foxtel.wf.adapter.service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme;
import com.mediasmiths.foxtel.generated.outputruzz.RuzzIF;
import com.mediasmiths.foxtel.ip.common.events.Emailaddresses;
import com.mediasmiths.foxtel.ip.common.events.TxDelivered;
import com.mediasmiths.foxtel.ip.common.events.TcEvent;
import com.mediasmiths.foxtel.ip.common.events.QcServerFail;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import com.mediasmiths.foxtel.tc.priorities.TranscodePriorities;
import com.mediasmiths.foxtel.tc.rest.api.TCFTPUpload;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCErrorNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCPassNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCResultNotification;
import com.mediasmiths.foxtel.wf.adapter.model.GetPriorityRequest;
import com.mediasmiths.foxtel.wf.adapter.model.GetPriorityResponse;
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
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

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
	@Named("mex.serialiser")
	private JAXBSerialiser mexSerialiser;
	@Inject
	@Named("outputruzz.serialiser")
	private JAXBSerialiser ruzzSerialiser;

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
	@Named("ao.tx.delivery.ftp.gxf.source.path")
	private String aoGXFFTPSourcePath = "ready/";

	@Inject
	private TranscodePriorities transcodePriorities;
	
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
			final String destination = mayamClient.pathToMaterial(id,false);
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
				saveEvent("QCProblemWithTCMedia", notification, QC_EVENT_NAMESPACE);
				mayamClient.txDeliveryFailed(notification.getAssetId(), notification.getTaskID(), "AUTO QC FAILED");
				// attach qc report if qc is failed
				attachQcReports(notification.getAssetId(), notification.getJobName());
			}
			else
			{
				// id is an item id
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

		String path = mayamClient.pathToMaterial(materialID,false);

		return new MaterialTransferForTCResponse(path);
	}

	@Override
	public void notifyAutoQCPassed(AutoQCPassNotification notification) throws MayamClientException
	{
		log.info(String.format(
				"Received notification of Auto QC Pass ID %s isTX %b",
				notification.getAssetId(),
				notification.isForTXDelivery()));

		com.mediasmiths.foxtel.ip.common.events.AutoQCPassNotification qcPass = new com.mediasmiths.foxtel.ip.common.events.AutoQCPassNotification();
		qcPass.setAssetId(notification.getAssetId());
		qcPass.setForTXDelivery(notification.isForTXDelivery());
		qcPass.setMaterialID(String.valueOf(notification.getTaskID()));
		qcPass.setTitle(notification.getTitle());
		events.saveEvent(QC_EVENT_NAMESPACE, "AutoQCPassed", qcPass);

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

	private Collection<File> attachQcReports(final String assetID, final String jobName) throws MayamClientException
	{
		Collection<File> reports = getQCFiles(jobName);

		if (reports.size() == 0)
		{
			log.warn("no qc reports found for " + jobName);
		}

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

		return reports;
	}

	/**
	 *
	 * @param jobName
	 * @return the list of cerify QC report files defines for the jobname.
	 *
	 */
	private Collection<File> getQCFiles(final String jobName)
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

		return FileUtils.listFiles(new File(cerifyReportLocation), reportPDFAcceptor, reportPDFAcceptor);
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
				
				TcEvent tce = new TcEvent();
				AttributeMap task = mayamClient.getTask(notification.getTaskID());
				if (task != null)
				{
					tce.setAssetID(notification.getAssetId());
					tce.setPackageID(task.getAttributeAsString(Attribute.HOUSE_ID));
					tce.setTitle(notification.getTitle());
					events.saveEvent("http://foxtel.com.au/ip/tc", "QcServerFailureDuringTranscode", tce);
				}
			}
			else
			{
				// auto qc was for qc task
				saveEvent("CerifyQCError", notification, QC_EVENT_NAMESPACE);
				mayamClient.autoQcErrorForMaterial(notification.getAssetId(), notification.getTaskID());
				
				AttributeMap task = mayamClient.getTask(notification.getTaskID());
				if (task != null)
				{
					QcServerFail qcsf = new QcServerFail();
					String assetID = notification.getAssetId();
					qcsf.setAssetID(assetID);
					qcsf.setMaterialID(task.getAttributeAsString(Attribute.HOUSE_ID));
					qcsf.setTitle(notification.getTitle());
					events.saveEvent("http://foxtel.com.au/ip/qc", "QcServerFail", qcsf);
				}
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

		com.mediasmiths.foxtel.ip.common.events.TcNotification tcFailure = new com.mediasmiths.foxtel.ip.common.events.TcNotification();

		try
		{
            tcFailure.setAssetID(notification.getAssetID());
			tcFailure.setTaskID(notification.getTaskID()+"");
			tcFailure.setTitle(notification.getTitle());

			if (notification.isForTXDelivery())
			{
				// auto qc was for tx delivery
				mayamClient.txDeliveryFailed(notification.getAssetID(), notification.getTaskID(), "TRANSCODE");

				events.saveEvent("http://www.foxtel.com.au/ip/tc", "TCFailed", tcFailure);
			}
			else
			{
				// auto qc was for export task
				mayamClient.exportFailed(notification.getTaskID());

				events.saveEvent("http://www.foxtel.com.au/ip/tc", "ExportFailure", tcFailure);
			}
		}
		catch (MayamClientException e)
		{
			log.error("Failed to fail task!", e);
			events.saveEvent("http://www.foxtel.com.au/ip/tc", "TCFailed", tcFailure);

			throw e;
		}

	}

	@Override
	public void notifyTCFailed(TCFailureNotification notification) throws MayamClientException
	{

		log.info(String.format("Received notification of TC failure asset id %s", notification.getAssetID()));

		if (! notification.isForTXDelivery())
		{
			long taskId = notification.getTaskID();
			AttributeMap task = mayamClient.getTask(taskId);
			String taskListID = task.getAttribute(Attribute.OP_TYPE);
	
			String username = task.getAttributeAsString(Attribute.TASK_CREATED_BY);
			Emailaddresses emails = null;
			if (username != null)
			{
				String email = String.format("%s@foxtel.com.au",username);
				emails = new Emailaddresses();
				emails.getEmailaddress().add(email);
				
			}
			
			if (taskListID.equals(TranscodeJobType.CAPTION_PROXY.getText()))
			{
				saveEvent(
						"CaptionProxyFailure",
						notification,
						TC_EVENT_NAMESPACE,
						new com.mediasmiths.foxtel.ip.common.events.TcNotification(),
						false,emails);
			}
			else if (taskListID.equals(TranscodeJobType.PUBLICITY_PROXY.getText()))
			{
				saveEvent(
						         "PublicityProxyFailure",
						         notification,
						         TC_EVENT_NAMESPACE,
						         new com.mediasmiths.foxtel.ip.common.events.TcNotification(),
						         false,emails);
			}
			else if (taskListID.equals(TranscodeJobType.COMPLIANCE_PROXY.getText()))
			{
				saveEvent(
						"ClassificationProxyFailure",
						notification,
						TC_EVENT_NAMESPACE,
						new com.mediasmiths.foxtel.ip.common.events.TcNotification(),
						false,emails);
			}
			else
			{
				saveEvent(
						"TCFailed",
						notification,
						TC_EVENT_NAMESPACE,
						new com.mediasmiths.foxtel.ip.common.events.TcNotification(),
						false,emails);
			}
		}
	}

	@Override
	public void notifyTCPassed(TCPassedNotification notification) throws MayamClientException
	{
		log.info(String.format("Received notification of TC passed asset id %s", notification.getAssetID()));

		if (notification.isForTXDelivery())
		{
			// saveEvent("Transcoded", notification, TC_EVENT_NAMESPACE, new com.mediasmiths.foxtel.ip.common.events.TcNotification());
		}
		else
		// extended publishing
		{
			String deliveryLocation = "";

			if (notification.getFtpupload() != null)
			{
				log.debug("destination received from TCPassedNotification");
				TCFTPUpload ftpupload = notification.getFtpupload();
				deliveryLocation = String.format("%s/%s", ftpupload.folder, ftpupload.filename);
				log.debug("delivery location :" + deliveryLocation);
			}
			else
			{
				log.debug("no destination received");
			}

			long taskId = notification.getTaskID();
			AttributeMap task = mayamClient.getTask(taskId);
			String taskListID = task.getAttribute(Attribute.OP_TYPE);
			log.debug("Task Button: " + taskListID);
			
			String username = task.getAttributeAsString(Attribute.TASK_CREATED_BY);
			Emailaddresses emails = null;
			
			if (username != null)
			{
				String email = String.format("%s@foxtel.com.au",username);
				emails = new Emailaddresses();
				emails.getEmailaddress().add(email);
			}
			
			if (taskListID.equals(TranscodeJobType.CAPTION_PROXY.getText()))
			{
				saveEvent(
						"CaptionProxySuccess",
						notification,
						TC_EVENT_NAMESPACE,
						new com.mediasmiths.foxtel.ip.common.events.TcNotification(),
						true,
						deliveryLocation,emails);
			}
			else if (taskListID.equals(TranscodeJobType.COMPLIANCE_PROXY.getText()))
			{
				saveEvent(
						"ClassificationProxySuccess",
						notification,
						TC_EVENT_NAMESPACE,
						new com.mediasmiths.foxtel.ip.common.events.TcNotification(),
						true,
						deliveryLocation,emails);
			}
			else if (taskListID.equals(TranscodeJobType.PUBLICITY_PROXY.getText()))
			{
				saveEvent(
						"PublicityProxySuccess",
						notification,
						TC_EVENT_NAMESPACE,
						new com.mediasmiths.foxtel.ip.common.events.TcNotification(),
						true,
						deliveryLocation,emails);
			}
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
		
		TcEvent tce = new TcEvent();
		String packageID = notification.getPackageID();
		tce.setPackageID(packageID);
		AttributeMap packageAttributes = mayamClient.getPackageAttributes(packageID);
		String title = packageAttributes.getAttributeAsString(Attribute.SERIES_TITLE);
		tce.setTitle(title);
		events.saveEvent("http://foxtel.com.au/ip/tc", "TranscodeDeliveryFailed", tce);
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
				// ftp xml file
				boolean segmentxmlTransferred = aoXMLFtp(segmentXmlFile);

				if (segmentxmlTransferred)
				{
					// should probably be its own intalio workflow step but doing it here for now
					return aoFXPtransfer(gxfFile);
				}
				else
				{
					return false;
				}
			}

			return true;
		}
		catch (IOException e)
		{
			TcEvent tce = new TcEvent();
			tce.setPackageID(packageID);
			AttributeMap packageAttributes = mayamClient.getPackageAttributes(packageID);
			String title = packageAttributes.getAttributeAsString(Attribute.SERIES_TITLE);
			tce.setTitle(title);
			events.saveEvent("http://foxtel.com.au/ip/tc", "FailedToGenerateXML", tce);
			
			log.error(String.format("Error writing companion xml for package %s", packageID), e);
			throw e;
		}

	}

	private boolean aoXMLFtp(File segmentXmlFile)
	{
		String segmentFileName = FilenameUtils.getName(segmentXmlFile.getAbsolutePath());

		boolean xmlFtp = Ftp.ftpTransfer(
				aoXMLFTPDestinationHost,
				aoXMLFTPDestinationUser,
				aoXMLFTPDestinationPass,
				segmentXmlFile.getAbsolutePath(),
				aoXMLFTPDestinationPath,
				segmentFileName);

		if (xmlFtp)
		{
			log.info("xml upload complete");
			return true;
		}
		else
		{
			log.error("xml upload failed");
			return false;
		}

	}

	private boolean aoFXPtransfer(File gxfFile)
	{

		String gxfFileName = FilenameUtils.getName(gxfFile.getAbsolutePath());

		// upload gxf
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
		log.debug(">>>getAOSegmentXML");
		RuzzIF ruzzProgramme = mayamClient.getRuzzProgramme(packageID);
		return ruzzSerialiser.serialise(ruzzProgramme);

	}

	@Override
	public String getSegmentXML(final String packageID) throws MayamClientException, JAXBException
	{
		log.debug(">>>getSegmentXML");
		Programme programme = mayamClient.getProgramme(packageID);
		return mexSerialiser.serialise(programme);
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


	// TC Events

	protected void saveEvent(
			String name,
			TCNotification payload,
			String nameSpace,
			com.mediasmiths.foxtel.ip.common.events.TcNotification eventNotify,
			boolean success, Emailaddresses emailaddress){
		saveEvent(name, payload, nameSpace, eventNotify, success, null,emailaddress);
	}
	
	protected void saveEvent(
			String name,
			TCNotification payload,
			String nameSpace,
			com.mediasmiths.foxtel.ip.common.events.TcNotification eventNotify,
			boolean success, String deliveryLocation, Emailaddresses emailaddress)
	{
		try
		{
			log.debug("Save event: " + nameSpace + ":" + name);
			eventNotify.setAssetID(payload.getAssetID());
			eventNotify.setTitle(payload.getTitle());
			eventNotify.setTaskID(payload.getTaskID() + "");
			if (success)
			{
				eventNotify.setDeliveryLocation(deliveryLocation);
			}

			if (emailaddress != null)
			{
				eventNotify.setEmailaddresses(emailaddress);
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
			for (File qcFile : getQCFiles(payload.getJobName()))
			{
                qcErrorNotification.getQcReportFilePath().add(qcFile.getAbsolutePath());
			}
			events.saveEvent(nameSpace, name, qcErrorNotification);
		}
		catch (Exception e)
		{
			log.error("Event serialisation issue: ", e);
		}
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


	@Override
	@POST
	@Path("/tc/priority")
	@Produces("application/xml")
	@Consumes("application/xml")
	public GetPriorityResponse getTCPriority(GetPriorityRequest request) throws MayamClientException
	{
		Date created = request.getCreated();		
		String packageID = request.getPackageID();
		
		Date firstTX = null;
		
		if (!StringUtils.isEmpty(packageID))
		{
			AttributeMap packageAttributes = mayamClient.getPackageAttributes(packageID);
			firstTX = (Date) packageAttributes.getAttribute(Attribute.TX_FIRST); // package first tx date
		}
		else
		{
			log.trace("No package id specified, transcode is assumed to be against an item only");
		}
	
		Integer currentPriority = request.getCurrentPriority();
		String strJobType = request.getJobType();
		TranscodeJobType jobType = TranscodeJobType.fromText(strJobType);

		Integer newPriority = transcodePriorities.getPriorityForTranscodeJob(jobType, firstTX, created, currentPriority);
		log.debug("priority returned : " + newPriority);

		GetPriorityResponse ret = new GetPriorityResponse();
		ret.setPriority(newPriority);
		ret.setChanged(false);

		if (!newPriority.equals(currentPriority))
		{
			log.info("priority change for transcode job " + jobType);
			ret.setChanged(true);
		}
		else
		{
			ret.setChanged(false);
		}

		return ret;
	}
}
