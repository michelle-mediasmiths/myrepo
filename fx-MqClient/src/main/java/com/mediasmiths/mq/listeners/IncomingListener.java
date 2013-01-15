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
import com.mediasmiths.foxtel.agent.processing.EventService;
import com.mediasmiths.mayam.LogUtil;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mq.handlers.AssetDeletionHandler;
import com.mediasmiths.mq.handlers.AssetPurgeHandler;
import com.mediasmiths.mq.handlers.ComplianceEditingHandler;
import com.mediasmiths.mq.handlers.ComplianceLoggingHandler;
import com.mediasmiths.mq.handlers.EmergencyIngestHandler;
import com.mediasmiths.mq.handlers.FixAndStitchHandler;
import com.mediasmiths.mq.handlers.ImportFailureHandler;
import com.mediasmiths.mq.handlers.IngestCompleteHandler;
import com.mediasmiths.mq.handlers.IngestJobHandler;
import com.mediasmiths.mq.handlers.InitiateQcHandler;
import com.mediasmiths.mq.handlers.PackageUpdateHandler;
import com.mediasmiths.mq.handlers.PreviewTaskHandler;
import com.mediasmiths.mq.handlers.QcTaskUpdateHandler;
import com.mediasmiths.mq.handlers.QcCompleteHandler;
import com.mediasmiths.mq.handlers.SegmentationCompleteHandler;
import com.mediasmiths.mq.handlers.TemporaryContentHandler;
import com.mediasmiths.mq.handlers.UnmatchedHandler;
import com.mediasmiths.mq.handlers.UnmatchedJobHandler;

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
	// @Inject
	// failed to inject
	EventService eventService;
	@Inject
	AssetDeletionHandler assetDeletionHandler;
	@Inject
	AssetPurgeHandler assetPurgeHandler;
	@Inject
	EmergencyIngestHandler emergencyIngestHandler;
	@Inject
	PackageUpdateHandler packageUpdateHandler;
	@Inject
	TemporaryContentHandler temporaryContentHandler;
	@Inject
	UnmatchedHandler unmatchedHandler;
	@Inject
	ComplianceEditingHandler compEditHandler;
	@Inject
	ComplianceLoggingHandler comLoggingHandler;
	@Inject
	FixAndStitchHandler fixAndStitchHandler;
	@Inject
	ImportFailureHandler importFailHandler;
	@Inject
	IngestCompleteHandler ingestCompleteHandler;
	@Inject
	InitiateQcHandler initiateQcHandler;
	@Inject
	PreviewTaskHandler previewHandler;
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

		try
		{
			logger.trace(String.format("Attributes message: " + LogUtil.mapToString(messageAttributes)));
		}
		catch (Exception e)
		{
			logger.error("error logging attributes message");
		}

		 
		// passEventToHandler(fixAndStitchHandler, messageAttributes);
		// passEventToHandler(importFailHandler, messageAttributes);

		passEventToHandler(initiateQcHandler, messageAttributes);

		// passEventToHandler(segmentationHandler, messageAttributes);
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
			logger.trace(String.format("Attributes message (before): " + LogUtil.mapToString(beforeAttributes)));
			logger.trace(String.format("Attributes message (changed): " + LogUtil.mapToString(afterAttributes)));
			logger.trace(String.format("Attributes message (current): " + LogUtil.mapToString(currentAttributes)));

			TaskState initialState = beforeAttributes.getAttribute(Attribute.TASK_STATE);
			TaskState newState = currentAttributes.getAttribute(Attribute.TASK_STATE);

			if (!initialState.equals(newState))
			{
				passEventToHandler(ingestCompleteHandler,currentAttributes);
				passEventToUpdateHandler(qcTaskUpdateHandler,currentAttributes, beforeAttributes, afterAttributes);
				passEventToHandler(qcCompleteHandler, currentAttributes);
				passEventToHandler(compEditHandler, currentAttributes);
				passEventToHandler(comLoggingHandler, currentAttributes);
				passEventToHandler(previewHandler, currentAttributes);
				passEventToHandler(fixAndStitchHandler, currentAttributes);
				passEventToHandler(unmatchedHandler, currentAttributes);
				passEventToHandler(segmentationHandler, currentAttributes);
				
				taskController.updateAccessRights(currentAttributes);
			}
			else{
				passEventToUpdateHandler(qcTaskUpdateHandler,currentAttributes, beforeAttributes, afterAttributes);
			}
		}
		catch (Exception e)
		{
			logger.error("error logging attributes message");
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
			passEventToHandler(unmatchedHandler, messageAttributes);
			logger.trace(String.format("Attributes message: " + LogUtil.mapToString(messageAttributes)));
		}
		catch (Exception e)
		{
			logger.error("error logging attributes message");
		}

		// passEventToHandler(assetDeletionHandler, messageAttributes);
		// passEventToHandler(assetPurgeHandler, messageAttributes);
		// passEventToHandler(emergencyIngestHandler, messageAttributes);
		// passEventToHandler(packageUpdateHandler, messageAttributes);
		// passEventToHandler(temporaryContentHandler, messageAttributes);
	}

	private void onAssetUpdate(MqMessage msg)
	{
		logger.trace("onAssetUpdate");
		AttributeMapPair messageAttributes = msg.getSubjectPair();
		AttributeMap beforeAttributes = messageAttributes.getBefore();
		AttributeMap afterAttributes = messageAttributes.getAfter();
		try
		{
			logger.trace(String.format("Attributes message (before): " + LogUtil.mapToString(beforeAttributes)));
			logger.trace(String.format("Attributes message (after): " + LogUtil.mapToString(afterAttributes)));
		}
		catch (Exception e)
		{
			logger.error("error logging attributes message");
		}

		// TODO:Handlers for asset updates
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
