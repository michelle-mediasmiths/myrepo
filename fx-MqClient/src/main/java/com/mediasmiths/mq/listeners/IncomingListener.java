package com.mediasmiths.mq.listeners;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.Job.JobType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.mq.MqContentType;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.MqMessage.AttributeMapPair;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.accessrights.MayamAccessRightsController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mq.handlers.asset.AssetDeletionHandler;
import com.mediasmiths.mq.handlers.asset.AssetPurgeHandler;
import com.mediasmiths.mq.handlers.asset.PackageUpdateHandler;
import com.mediasmiths.mq.handlers.asset.TemporaryContentHandler;
import com.mediasmiths.mq.handlers.asset.TitleUpdateHandler;
import com.mediasmiths.mq.handlers.asset.AccessUpdateHandler;
import com.mediasmiths.mq.handlers.asset.MaterialUpdateHandler;
import com.mediasmiths.mq.handlers.button.DeleteButton;
import com.mediasmiths.mq.handlers.button.ExportMarkersButton;
import com.mediasmiths.mq.handlers.button.ProtectButton;
import com.mediasmiths.mq.handlers.button.UningestButton;
import com.mediasmiths.mq.handlers.button.UnprotectButton;
import com.mediasmiths.mq.handlers.button.export.CaptionProxy;
import com.mediasmiths.mq.handlers.button.export.ComplianceProxy;
import com.mediasmiths.mq.handlers.button.export.PublicityProxy;
import com.mediasmiths.mq.handlers.compliance.ComplianceEditingHandler;
import com.mediasmiths.mq.handlers.compliance.ComplianceLoggingHandler;
import com.mediasmiths.mq.handlers.fixstitch.FixAndStitchCancelHandler;
import com.mediasmiths.mq.handlers.fixstitch.FixAndStitchFinishHandler;
import com.mediasmiths.mq.handlers.fixstitch.FixAndStitchRevertHandler;
import com.mediasmiths.mq.handlers.ingest.IngestCompleteHandler;
import com.mediasmiths.mq.handlers.ingest.IngestJobHandler;
import com.mediasmiths.mq.handlers.preview.PreviewTaskCreateHandler;
import com.mediasmiths.mq.handlers.preview.PreviewTaskFailHandler;
import com.mediasmiths.mq.handlers.preview.PreviewTaskFinishHandler;
import com.mediasmiths.mq.handlers.purge.PurgeCandidateExtendHandler;
import com.mediasmiths.mq.handlers.qc.InitiateQcHandler;
import com.mediasmiths.mq.handlers.qc.QcCompleteHandler;
import com.mediasmiths.mq.handlers.qc.QcTaskUpdateHandler;
import com.mediasmiths.mq.handlers.segmentation.SegmentationCompleteHandler;
import com.mediasmiths.mq.handlers.tx.InitiateTxHandler;
import com.mediasmiths.mq.handlers.unmatched.UnmatchedAssetCreateHandler;
import com.mediasmiths.mq.handlers.unmatched.UnmatchedJobHandler;
import com.mediasmiths.mq.handlers.unmatched.UnmatchedTaskUpdateHandler;

@Singleton
public class IncomingListener extends MqClientListener
{
	private static final String CREATE_CHANGE_TYPE = "create";
	private static final String UPDATE_CHANGE_TYPE = "update";
	private static final String MAYAM_CHANGE_TYPE_PROPERTY = "MayamChangeType";
	protected final static Logger logger = Logger.getLogger(IncomingListener.class);
	public static final String ATTRIBUTE_MESSAGE_TYPE = "mayam#attributes";
	public static final String ATTRIBUTE_PAIR = "mayam#attribute-pairs";
	public static final String JOB_MESSAGE_TYPE = "mayam#job";

	@Named(MayamClientModule.SETUP_TASKS_CLIENT)
	@Inject
	private TasksClient client;
	@Inject
	private MayamTaskController taskController;
	@Inject
	private MayamAccessRightsController accessRightsController;
	
	//needs a marshaller before this will inject
//	@Inject
//	EventService eventService;
	@Inject
	AssetDeletionHandler assetDeletionHandler;
	@Inject
	AssetPurgeHandler assetPurgeHandler;
	@Inject
	PackageUpdateHandler packageUpdateHandler;
	@Inject
	TemporaryContentHandler temporaryContentHandler;
	@Inject
	UnmatchedAssetCreateHandler unmatchedAssetCreateHandler;
	@Inject
	TitleUpdateHandler titleUpdateHandler;
	@Inject
	AccessUpdateHandler accessUpdateHandler;
	@Inject
	MaterialUpdateHandler materialUpdateHandler;
	@Inject
	ComplianceEditingHandler compEditHandler;
	@Inject
	ComplianceLoggingHandler comLoggingHandler;
	@Inject
	FixAndStitchFinishHandler fixAndStitchFinishHandler;
	@Inject
	FixAndStitchRevertHandler fixAndStitchRevertHandler;
	@Inject
	FixAndStitchCancelHandler fixAndStitchCancelHandler;
	@Inject
	IngestCompleteHandler ingestCompleteHandler;
	@Inject
	InitiateQcHandler initiateQcHandler;
	@Inject
	PreviewTaskCreateHandler previewTaskCreateHandler;
	@Inject
	PreviewTaskFinishHandler previewTaskFinishHandler;
	@Inject
	PreviewTaskFailHandler previewTaskFailHandler;
	@Inject
	QcCompleteHandler qcCompleteHandler;
	@Inject
	SegmentationCompleteHandler segmentationHandler;
	@Inject
	IngestJobHandler ingestJobHandler;
	@Inject
	UnmatchedJobHandler unmatchedJobHandler;
	@Inject
	QcTaskUpdateHandler qcTaskUpdateHandler;
	@Inject
	UnmatchedTaskUpdateHandler unmatchedTaskUpdateHandler;
	@Inject
	InitiateTxHandler initiateTxHandler;
	@Inject	
	PurgeCandidateExtendHandler purgeCandidateExtendHandler;
	@Inject
	UningestButton uningestButton;
	@Inject
	DeleteButton deleteButton;
	@Inject
	ExportMarkersButton exportMarkersButton;
	@Inject
	PublicityProxy publicityProxyButton;
	@Inject
	CaptionProxy captionProxyButton;
	@Inject
	ComplianceProxy complianceProxyButton;
	@Inject
	UnprotectButton unprotectButton;
	@Inject
	ProtectButton protectButton;
	
	public void onMessage(MqMessage msg) throws Throwable
	{
		try
		{
			logger.trace("IncomingListener onMessage");
			logger.trace("Message is: " + msg.toString());
			MqContentType type = msg.getType();

			String origin = msg.getProperties().get(MqMessage.PROP_ORIGIN_DESTINATION);
			String changeType = msg.getProperties().get(MAYAM_CHANGE_TYPE_PROPERTY);

			if (type != null && origin != null)
			{

				if (isJob(type))
				{
					onJobMessage(msg);
				}
				else if (changeType != null)
				{
					if (isAttributes(type) && isAsset(origin) && isCreate(changeType))
					{
						onAssetCreate(msg);
					}
					else if (isAttributePair(type) && isAsset(origin) && isUpdate(changeType))
					{
						onAssetUpdate(msg);
					}
					else if (isAttributes(type) && isTask(origin) && isCreate(changeType))
					{
						onTaskCreate(msg);
					}
					else if (isAttributePair(type) && isTask(origin) && isUpdate(changeType))
					{
						onTaskUpdate(msg);
					}
					else
					{
						logger.warn("could not discern nature of message");
					}
				}
				else
				{
					logger.warn("Change type was null, ignoring");
				}
			}
			else
			{
				logger.debug(String.format("onMessage, type or origin was null. Msg: %s", msg.toString()));
			}
		}
		catch (Exception e)
		{
			logger.error("Exception in incoming listener", e);
			throw e;
		}
	}

	private void onTaskCreate(MqMessage msg)
	{
		log.trace("onTaskCreate");
		AttributeMap messageAttributes = msg.getSubject();
 
		passEventToHandler(initiateQcHandler, messageAttributes);
		passEventToHandler(initiateTxHandler, messageAttributes);
		passEventToHandler(previewTaskCreateHandler, messageAttributes);
		passEventToHandler(uningestButton, messageAttributes);
		passEventToHandler(deleteButton,messageAttributes);
		passEventToHandler(exportMarkersButton, messageAttributes);
		passEventToHandler(publicityProxyButton, messageAttributes);
		passEventToHandler(captionProxyButton, messageAttributes);
		passEventToHandler(complianceProxyButton,messageAttributes);
		passEventToHandler(unprotectButton, messageAttributes);
		passEventToHandler(protectButton, messageAttributes);
	}

	private void onTaskUpdate(MqMessage msg)
	{
		logger.trace("onTaskUpdate");
		AttributeMapPair messageAttributes = msg.getSubjectPair();
		AttributeMap beforeAttributes = messageAttributes.getBefore();
		AttributeMap afterAttributes = messageAttributes.getAfter();

		// afterAttributes does not contain all of the tasks attributes, so we need to do this to have an attributemap containing them all
		AttributeMap currentAttributes = beforeAttributes.copy();
		currentAttributes.putAll(afterAttributes);

		try
		{
			TaskState initialState = beforeAttributes.getAttribute(Attribute.TASK_STATE);
			TaskState newState = currentAttributes.getAttribute(Attribute.TASK_STATE);

			passEventToUpdateHandler(qcTaskUpdateHandler,currentAttributes, beforeAttributes, afterAttributes);
			passEventToUpdateHandler(unmatchedTaskUpdateHandler, currentAttributes, beforeAttributes, afterAttributes);
			
			if (!initialState.equals(newState))
			{
				passEventToHandler(ingestCompleteHandler,currentAttributes);
				passEventToHandler(qcCompleteHandler, currentAttributes);
				passEventToHandler(compEditHandler, currentAttributes);
				passEventToHandler(comLoggingHandler, currentAttributes);
				passEventToHandler(previewTaskFinishHandler, currentAttributes);
				passEventToHandler(previewTaskFailHandler, currentAttributes);
				passEventToHandler(fixAndStitchFinishHandler, currentAttributes);
				passEventToHandler(fixAndStitchRevertHandler, currentAttributes);
				passEventToHandler(fixAndStitchCancelHandler, currentAttributes);
				passEventToHandler(segmentationHandler, currentAttributes);
				passEventToUpdateHandler(purgeCandidateExtendHandler, currentAttributes, beforeAttributes, afterAttributes);
				
				accessRightsController.updateAccessRights(currentAttributes);
			}
		}
		catch (Exception e)
		{
			logger.error("error onTaskUpdate");
		}
	}


	
	private void onJobMessage(MqMessage msg)
	{
		logger.trace("onJobMessage");
		Job jobMessage = msg.getJob();

		if (jobMessage != null)
		{
			JobType jobType = jobMessage.getJobType();
			log.debug("jobType is " + jobType.name());

			if (jobType != null)
			{
				if (jobType.equals(JobType.INGEST) || jobType.equals(JobType.IMPORT))
				{
					passEventToHandler(ingestJobHandler, jobMessage);
					passEventToHandler(unmatchedJobHandler, jobMessage);
				}
			}
		}
		else
		{
			logger.error("Message properties were null for job message");
		}
	}

	private void onAssetCreate(MqMessage msg)
	{
		logger.trace("onAssetCreate");
		AttributeMap messageAttributes = msg.getSubject();

		try
		{
			passEventToHandler(unmatchedAssetCreateHandler, messageAttributes);
		}
		catch (Exception e)
		{
			logger.error("error onAssetCreate");
		}

		// passEventToHandler(assetDeletionHandler, messageAttributes);
		// passEventToHandler(assetPurgeHandler, messageAttributes);
		// passEventToHandler(emergencyIngestHandler, messageAttributes);
		// passEventToHandler(packageUpdateHandler, messageAttributes);
		 passEventToHandler(unmatchedAssetCreateHandler, messageAttributes);
	}

	private void onAssetUpdate(MqMessage msg)
	{
		logger.trace("onAssetUpdate");
		AttributeMapPair messageAttributes = msg.getSubjectPair();
		AttributeMap beforeAttributes = messageAttributes.getBefore();
		AttributeMap afterAttributes = messageAttributes.getAfter();
		
		AttributeMap currentAttributes = beforeAttributes.copy();
		currentAttributes.putAll(afterAttributes);
		
		try
		{
			passEventToUpdateHandler(titleUpdateHandler, currentAttributes, beforeAttributes, afterAttributes);
			passEventToUpdateHandler(materialUpdateHandler, currentAttributes, beforeAttributes, afterAttributes);
			passEventToUpdateHandler(temporaryContentHandler, currentAttributes, beforeAttributes, afterAttributes);	
			passEventToUpdateHandler(accessUpdateHandler, currentAttributes, beforeAttributes, afterAttributes);	
		}
		catch (Exception e)
		{
			logger.error("error onAssetUpdate");
		}
	
	}

	private boolean isTask(String origin)
	{
		return origin.contains("task");
	}

	private boolean isUpdate(String changeType)
	{
		return changeType.equals(UPDATE_CHANGE_TYPE);
	}

	private boolean isAttributePair(MqContentType type)
	{
		return type.type().equals(IncomingListener.ATTRIBUTE_PAIR);
	}

	private boolean isCreate(String changeType)
	{
		return changeType.equals(CREATE_CHANGE_TYPE);
	}

	private boolean isAsset(String origin)
	{
		return origin.contains("asset");
	}

	private boolean isAttributes(MqContentType type)
	{
		return type.type().equals(IncomingListener.ATTRIBUTE_MESSAGE_TYPE);
	}

	private boolean isJob(MqContentType type)
	{
		return type.type().equals(IncomingListener.JOB_MESSAGE_TYPE);
	}

}
