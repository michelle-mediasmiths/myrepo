package com.mediasmiths.mq.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.Job.JobSubType;
import com.mayam.wf.attributes.shared.type.Job.JobType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.mq.MqContentType;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.MqMessage.AttributeMapPair;
import com.mediasmiths.mayam.accessrights.MayamAccessRightsController;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.veneer.TasksClientVeneer;
import com.mediasmiths.mq.handlers.FTPTransferJobHandler;
import com.mediasmiths.mq.handlers.asset.AccessUpdateHandler;
import com.mediasmiths.mq.handlers.asset.AssetDeleteHandler;
import com.mediasmiths.mq.handlers.asset.DartRecordingTitleAssociationHandler;
import com.mediasmiths.mq.handlers.asset.MaterialProtectHandler;
import com.mediasmiths.mq.handlers.asset.MaterialUpdateHandler;
import com.mediasmiths.mq.handlers.asset.MediaMoveHandler;
import com.mediasmiths.mq.handlers.asset.PresentationFlagClearedHandler;
import com.mediasmiths.mq.handlers.asset.PurgeProtectedUpdateHandler;
import com.mediasmiths.mq.handlers.asset.TaskCreateHandler;
import com.mediasmiths.mq.handlers.asset.TemporaryContentHandler;
import com.mediasmiths.mq.handlers.asset.TitleUpdateHandler;
import com.mediasmiths.mq.handlers.asset.TransferJobHandler;
import com.mediasmiths.mq.handlers.button.DeleteButton;
import com.mediasmiths.mq.handlers.button.ExportMarkersButton;
import com.mediasmiths.mq.handlers.button.ProtectButton;
import com.mediasmiths.mq.handlers.button.QcParallel;
import com.mediasmiths.mq.handlers.button.UningestButton;
import com.mediasmiths.mq.handlers.button.UnprotectButton;
import com.mediasmiths.mq.handlers.button.export.CaptionProxy;
import com.mediasmiths.mq.handlers.button.export.ComplianceProxy;
import com.mediasmiths.mq.handlers.button.export.PublicityProxy;
import com.mediasmiths.mq.handlers.compliance.ComplianceEditingHandler;
import com.mediasmiths.mq.handlers.compliance.ComplianceLoggingHandler;
import com.mediasmiths.mq.handlers.export.ExportTaskStateChangeHandler;
import com.mediasmiths.mq.handlers.export.InitiateExportHandler;
import com.mediasmiths.mq.handlers.fixstitch.ConformJobHandler;
import com.mediasmiths.mq.handlers.fixstitch.FixAndStitchCancelHandler;
import com.mediasmiths.mq.handlers.fixstitch.FixAndStitchFinishHandler;
import com.mediasmiths.mq.handlers.fixstitch.FixAndStitchRevertHandler;
import com.mediasmiths.mq.handlers.ingest.IngestCompleteHandler;
import com.mediasmiths.mq.handlers.ingest.IngestJobHandler;
import com.mediasmiths.mq.handlers.ingest.IngestTaskCompleteHandler;
import com.mediasmiths.mq.handlers.ingest.PurgeTaskUpdateForUnmatchedHandler;
import com.mediasmiths.mq.handlers.pendingtx.PendingTxUpdateHandler;
import com.mediasmiths.mq.handlers.preview.PreviewTaskCreateHandler;
import com.mediasmiths.mq.handlers.preview.PreviewTaskEscalationHandler;
import com.mediasmiths.mq.handlers.preview.PreviewTaskFailHandler;
import com.mediasmiths.mq.handlers.preview.PreviewTaskFinishHandler;
import com.mediasmiths.mq.handlers.purge.PurgeCandidateCreateHandler;
import com.mediasmiths.mq.handlers.purge.PurgeCandidateExtendHandler;
import com.mediasmiths.mq.handlers.purge.PurgeCandidateUpdateHandler;
import com.mediasmiths.mq.handlers.qc.InitiateQcHandler;
import com.mediasmiths.mq.handlers.qc.QcCompleteHandler;
import com.mediasmiths.mq.handlers.qc.QcStateChangeHandler;
import com.mediasmiths.mq.handlers.qc.QcTaskUpdateHandler;
import com.mediasmiths.mq.handlers.segmentation.SegmentationCompleteHandler;
import com.mediasmiths.mq.handlers.tx.InitiateTxHandler;
import com.mediasmiths.mq.handlers.unmatched.UnmatchedAssetCreateHandler;
import com.mediasmiths.mq.handlers.unmatched.UnmatchedTaskCreateHandler;
import com.mediasmiths.mq.handlers.unmatched.UnmatchedTaskUpdateHandler;
import org.apache.log4j.Logger;


@Singleton
public class IncomingListener extends MqClientListener
{
	private static final String CREATE_CHANGE_TYPE = "create";
	private static final String UPDATE_CHANGE_TYPE = "update";
	private static final String DELETE_CHANGE_TYPE = "delete";
	private static final String MEDIA_MOVE_CHANGE_TYPE = "media_move";
	
	private static final String MAYAM_CHANGE_TYPE_PROPERTY = "MayamChangeType";
	protected final static Logger logger = Logger.getLogger(IncomingListener.class);
	public static final String ATTRIBUTE_MESSAGE_TYPE = "mayam#attributes";
	public static final String ATTRIBUTE_PAIR = "mayam#attribute-pairs";
	public static final String JOB_MESSAGE_TYPE = "mayam#job";

	@Inject
	private TasksClientVeneer client;
	@Inject
	private MayamTaskController taskController;
	@Inject
	private MayamAccessRightsController accessRightsController;

	@Inject
	TemporaryContentHandler temporaryContentHandler;
	@Inject
	UnmatchedAssetCreateHandler unmatchedAssetCreateHandler;
	@Inject 
	MediaMoveHandler mediaMoveHandler;
	@Inject
	DartRecordingTitleAssociationHandler dartRecordingTitleAssociationHandler;
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
	IngestTaskCompleteHandler ingestTaskCompleteHandler;
	@Inject
	InitiateQcHandler initiateQcHandler;
	@Inject
	PreviewTaskCreateHandler previewTaskCreateHandler;
	@Inject
	TaskCreateHandler taskCreateHandler;
	@Inject
	PreviewTaskFinishHandler previewTaskFinishHandler;
	@Inject
	PreviewTaskFailHandler previewTaskFailHandler;
	@Inject
	PreviewTaskEscalationHandler previewTaskEscalationHandler;
	@Inject
	QcCompleteHandler qcCompleteHandler;
	@Inject
	SegmentationCompleteHandler segmentationHandler;
	@Inject
	IngestJobHandler ingestJobHandler;
	@Inject
	TransferJobHandler transferJobHandler;
	@Inject
	FTPTransferJobHandler ftpTransferJobHandler;
	@Inject
	QcTaskUpdateHandler qcTaskUpdateHandler;
	@Inject
	QcStateChangeHandler qcStateChangeHandler;
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
	@Inject
	QcParallel qcParallelButton;
	@Inject
	UnmatchedTaskCreateHandler unmatchedTaskCreate;
	@Inject
	ConformJobHandler conformJobHandler;
	@Inject
	PendingTxUpdateHandler pendingTxUpdate;
	@Inject
	MaterialProtectHandler materialProtected;
	@Inject
	IngestCompleteHandler ingestCompleteHandler;
	@Inject
	PurgeTaskUpdateForUnmatchedHandler purgeTaskUpdateForUnmatchedHandler;
	@Inject
	PurgeCandidateUpdateHandler purgeCandidateUpdateHandler;
	@Inject
	InitiateExportHandler initiateExportHandler;
	@Inject
	PresentationFlagClearedHandler presentationFlagClearedHandler;
	@Inject
	AssetDeleteHandler assetDeleteHandler;
	@Inject
	PurgeProtectedUpdateHandler purgeProtectedUpdateHandler;
	@Inject
	PurgeCandidateCreateHandler purgeCandidateCreateHandler;
	@Inject
	ExportTaskStateChangeHandler exportTaskStateChangeHandler;

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
					if (isAttributes(type) && isAsset(origin) && isMediaMove(changeType))
					{
						onMediaMove(msg);
					}
					else if (isAttributePair(type) && isAsset(origin) && isUpdate(changeType))
					{
						onAssetUpdate(msg);
					}
					else if (isAttributes(type) && isAsset(origin) && isDelete(changeType))
					{
						onAssetDelete(msg);
					}
					else if (isAttributes(type) && isTask(origin) && isCreate(changeType))
					{
						onTaskCreate(msg);
					}
					else if (isAttributePair(type) && isTask(origin) && isUpdate(changeType))
					{
						onTaskUpdateWithBeforeAndAfter(msg);
					}
					else if (isAttributes(type) && isTask(origin) && isUpdate(changeType))
					{
						onTaskUpdateCurrentOnly(msg);
					}
					else
					{
						logger.warn("message was not a type I handle");
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
		passEventToHandler(taskCreateHandler, messageAttributes);
		passEventToHandler(initiateQcHandler, messageAttributes);
		passEventToHandler(initiateTxHandler, messageAttributes);
		passEventToHandler(initiateExportHandler, messageAttributes);
		passEventToHandler(previewTaskCreateHandler, messageAttributes);
		passEventToHandler(unmatchedTaskCreate, messageAttributes);
		passEventToHandler(purgeCandidateCreateHandler, messageAttributes);
		passEventToHandler(exportTaskStateChangeHandler, messageAttributes);

	}

	private void onTaskUpdateWithBeforeAndAfter(MqMessage msg)
	{
		logger.trace("onTaskUpdate (message included before and after attributes)");
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
			passEventToUpdateHandler(pendingTxUpdate,currentAttributes, beforeAttributes, afterAttributes);
			passEventToUpdateHandler(unmatchedTaskUpdateHandler, currentAttributes, beforeAttributes, afterAttributes);
			passEventToUpdateHandler(purgeTaskUpdateForUnmatchedHandler,currentAttributes, beforeAttributes, afterAttributes);
			passEventToUpdateHandler(purgeCandidateUpdateHandler,currentAttributes, beforeAttributes, afterAttributes);
			passEventToUpdateHandler(ingestCompleteHandler,currentAttributes, beforeAttributes, afterAttributes);
			
			if (!initialState.equals(newState))
			{
				//tasks
				passEventToHandler(ingestTaskCompleteHandler,currentAttributes);
				passEventToHandler(qcCompleteHandler, currentAttributes);
				passEventToHandler(qcStateChangeHandler, currentAttributes);
				passEventToHandler(compEditHandler, currentAttributes);
				passEventToHandler(comLoggingHandler, currentAttributes);
				passEventToHandler(previewTaskCreateHandler, currentAttributes);
				passEventToHandler(previewTaskFinishHandler, currentAttributes);
				passEventToHandler(previewTaskFailHandler, currentAttributes);
				passEventToHandler(previewTaskEscalationHandler, currentAttributes);
				passEventToHandler(fixAndStitchFinishHandler, currentAttributes);
				passEventToHandler(fixAndStitchRevertHandler, currentAttributes);
				passEventToHandler(fixAndStitchCancelHandler, currentAttributes);
				passEventToHandler(segmentationHandler, currentAttributes);
				passEventToUpdateHandler(purgeCandidateExtendHandler, currentAttributes, beforeAttributes, afterAttributes);
				//initiate tx handler, export and qc is present for both task create and state change in order to enable 'retry'
				passEventToHandler(initiateTxHandler, currentAttributes);
				passEventToHandler(initiateExportHandler, currentAttributes);
				passEventToHandler(initiateQcHandler, currentAttributes);
				passEventToHandler(purgeCandidateCreateHandler, currentAttributes);
				passEventToHandler(exportTaskStateChangeHandler, currentAttributes);
			}
		}
		catch (Exception e)
		{
			logger.error("error onTaskUpdateWithBeforeAndAfter",e);
		}
	}

	private void onTaskUpdateCurrentOnly(MqMessage msg)
	{
		log.debug("onTaskUpdateCurrentOnly");
		try
		{
			AttributeMap messageAttributes = msg.getSubject();

			// buttons
			passEventToHandler(uningestButton, messageAttributes);
			passEventToHandler(deleteButton, messageAttributes);
			passEventToHandler(exportMarkersButton, messageAttributes);
			passEventToHandler(publicityProxyButton, messageAttributes);
			passEventToHandler(captionProxyButton, messageAttributes);
			passEventToHandler(complianceProxyButton, messageAttributes);
			passEventToHandler(unprotectButton, messageAttributes);
			passEventToHandler(protectButton, messageAttributes);
			passEventToHandler(qcParallelButton, messageAttributes);
		}
		catch (Exception e)
		{
			logger.error("error onTaskUpdateWithBeforeAndAfter",e);
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
				if (jobType.equals(JobType.INGEST) || jobType.equals(JobType.IMPORT) || jobType.equals(JobType.CONFORM))
				{
					passEventToHandler(ingestJobHandler, jobMessage);
					if(jobType.equals(JobType.CONFORM))
					{
						passEventToHandler(conformJobHandler, jobMessage);
					}
				}
				else if (jobType.equals(JobType.TRANSFER))
				{
					JobSubType subType = jobMessage.getJobSubType();
					if (subType.equals(JobSubType.TSM))
					{
						passEventToHandler(transferJobHandler, jobMessage);
					}
					else if(subType.equals(JobSubType.FTP)){
						passEventToHandler(ftpTransferJobHandler, jobMessage);
					}
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
			passEventToHandler(dartRecordingTitleAssociationHandler, messageAttributes);
		}
		catch (Exception e)
		{
			logger.error("error onAssetCreate");
		}
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
			passEventToUpdateHandler(materialProtected, currentAttributes, beforeAttributes, afterAttributes);
			passEventToUpdateHandler(materialUpdateHandler, currentAttributes, beforeAttributes, afterAttributes);
			passEventToUpdateHandler(temporaryContentHandler, currentAttributes, beforeAttributes, afterAttributes);	
			passEventToUpdateHandler(accessUpdateHandler, currentAttributes, beforeAttributes, afterAttributes);
			passEventToUpdateHandler(presentationFlagClearedHandler,currentAttributes,beforeAttributes,afterAttributes);
			passEventToUpdateHandler(purgeProtectedUpdateHandler,currentAttributes,beforeAttributes,afterAttributes);
		}
		catch (Exception e)
		{
			logger.error("error onAssetUpdate",e);
		}
	
	}


	private void onAssetDelete(MqMessage msg)
	{
		logger.trace("onAssetDelete");

		AttributeMap messageAttributes = msg.getSubject();

		try
		{
			passEventToHandler(assetDeleteHandler,messageAttributes);
		}
		catch (Exception e)
		{
			log.error("error onAssetDelete", e);
		}
	}

	private void onMediaMove(MqMessage msg)
	{
		logger.trace("onMediaMove");
		AttributeMap messageAttributes = msg.getSubject();

		try
		{
			passEventToHandler(mediaMoveHandler, messageAttributes);
		}
		catch (Exception e)
		{
			logger.error("error onMediaMove");
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

	private boolean isDelete(String changeType)
	{
		return changeType.equals(DELETE_CHANGE_TYPE);
	}

	private boolean isAttributePair(MqContentType type)
	{
		return type.type().equals(IncomingListener.ATTRIBUTE_PAIR);
	}

	private boolean isCreate(String changeType)
	{
		return changeType.equals(CREATE_CHANGE_TYPE);
	}
	
	private boolean isMediaMove(String changeType)
	{
		return changeType.equals(MEDIA_MOVE_CHANGE_TYPE);
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
