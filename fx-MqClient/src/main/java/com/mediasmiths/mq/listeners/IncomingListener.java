package com.mediasmiths.mq.listeners;

import java.util.Map;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.Job.JobType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.mq.MqContentType;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.MqMessage.AttributeMapPair;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.foxtel.agent.processing.EventService;
import com.mediasmiths.mayam.LogUtil;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mq.handlers.AssetDeletionHandler;
import com.mediasmiths.mq.handlers.AssetPurgeHandler;
import com.mediasmiths.mq.handlers.ComplianceEditingHandler;
import com.mediasmiths.mq.handlers.ComplianceLoggingHandler;
import com.mediasmiths.mq.handlers.EmergencyIngestHandler;
import com.mediasmiths.mq.handlers.FixAndStitchHandler;
import com.mediasmiths.mq.handlers.AttributeHandler;
import com.mediasmiths.mq.handlers.ImportFailureHandler;
import com.mediasmiths.mq.handlers.IngestCompleteHandler;
import com.mediasmiths.mq.handlers.IngestJobHandler;
import com.mediasmiths.mq.handlers.InitiateQcHandler;
import com.mediasmiths.mq.handlers.ItemCreationHandler;
import com.mediasmiths.mq.handlers.PackageUpdateHandler;
import com.mediasmiths.mq.handlers.PreviewTaskHandler;
import com.mediasmiths.mq.handlers.QcCompleteHandler;
import com.mediasmiths.mq.handlers.SegmentationCompleteHandler;
import com.mediasmiths.mq.handlers.TemporaryContentHandler;
import com.mediasmiths.mq.handlers.UnmatchedHandler;
import com.mediasmiths.mq.handlers.UnmatchedJobHandler;

public class IncomingListener
{
	private static final String CREATE_CHANGE_TYPE = "create";
	private static final String UPDATE_CHANGE_TYPE = "update";
	private static final String MAYAM_CHANGE_TYPE_PROPERTY = "MayamChangeType";
	protected final static Logger logger = Logger.getLogger(IncomingListener.class);
	public static final String ATTRIBUTE_MESSAGE_TYPE = "mayam#attributes";
	public static final String ATTRIBUTE_PAIR = "mayam#attribute-pairs";
	public static final String JOB_MESSAGE_TYPE = "mayam#job";

	public static Listener getInstance(
			final TasksClient client,
			final MayamTaskController taskController,
			EventService eventService)
	{
		final AssetDeletionHandler assetDeletionHandler = new AssetDeletionHandler();
		final AssetPurgeHandler assetPurgeHandler = new AssetPurgeHandler();
		final EmergencyIngestHandler emergencyIngestHandler = new EmergencyIngestHandler(client, taskController);
		final ItemCreationHandler itemCreationHandler = new ItemCreationHandler(client, taskController);
		final PackageUpdateHandler packageUpdateHandler = new PackageUpdateHandler(client, taskController);
		final TemporaryContentHandler temporaryContentHandler = new TemporaryContentHandler(client, taskController);
		final UnmatchedHandler unmatchedHandler = new UnmatchedHandler(taskController);
		final ComplianceEditingHandler compEditHandler = new ComplianceEditingHandler(taskController);
		final ComplianceLoggingHandler comLoggingHandler = new ComplianceLoggingHandler(taskController);
		final FixAndStitchHandler fixAndStitchHandler = new FixAndStitchHandler(taskController);
		final ImportFailureHandler importFailHandler = new ImportFailureHandler(taskController);
		final IngestCompleteHandler ingestCompleteHandler = new IngestCompleteHandler(taskController);
		final InitiateQcHandler initiateQcHandler = new InitiateQcHandler(taskController);
		final PreviewTaskHandler previewHandler = new PreviewTaskHandler(taskController);
		final QcCompleteHandler qcCompleteHandler = new QcCompleteHandler(taskController);
		final SegmentationCompleteHandler segmentationHandler = new SegmentationCompleteHandler(taskController);
		final IngestJobHandler ingestJobHandler = new IngestJobHandler(taskController);
		final UnmatchedJobHandler unmatchedJobHandler = new UnmatchedJobHandler(taskController);

		return new MqClientListener()
		{
			public void onMessage(MqMessage msg) throws Throwable
			{
				try
				{
					logger.trace("IncomingListener onMessage");
					logger.trace("Message is: " + msg.toString());
					MqContentType type = msg.getType();

					if (type != null)
					{
						logger.debug("Message type not null " + type.type());
					}
					else
					{
						logger.debug("Message type is null");
					}
					
					String origin = msg.getProperties().get(MqMessage.PROP_ORIGIN_DESTINATION);
					String changeType = msg.getProperties().get(MAYAM_CHANGE_TYPE_PROPERTY);

					logger.trace(String.format("origin is %s changeType is %s ",origin,changeType));
					
					if (type != null && origin != null)
					{

						if (isJob(type))
						{
							onJobMessage(msg);
						}
						else if (changeType != null)
						{
							logger.trace("changeType not null");

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
							else{
								log.warn("could not discern nature of message");
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
				log.trace("reading message subject");
				AttributeMap messageAttributes = msg.getSubject();

				try
				{
					logger.trace(String.format("Attributes message: " + LogUtil.mapToString(messageAttributes)));
				}
				catch (Exception e)
				{
					logger.error("error logging attributes message");
				}

				// passEventToHandler(compEditHandler, messageAttributes);
				// passEventToHandler(comLoggingHandler, messageAttributes);
				// passEventToHandler(fixAndStitchHandler, messageAttributes);
				// passEventToHandler(importFailHandler, messageAttributes);

				passEventToHandler(initiateQcHandler, messageAttributes);

				// passEventToHandler(segmentationHandler, messageAttributes);
			}
			
			private void onTaskUpdate(MqMessage msg)
			{
				logger.trace("reading message subject");
				AttributeMapPair messageAttributes = msg.getSubjectPair();
				AttributeMap beforeAttributes = messageAttributes.getBefore();
				AttributeMap afterAttributes = messageAttributes.getAfter();
				
				//afterAttributes does not contain all of the tasks attributes, so we need to do this to have an attributemap containing them all
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
						passEventToHandler(ingestCompleteHandler, currentAttributes);
						passEventToHandler(qcCompleteHandler, currentAttributes);
						passEventToHandler(previewHandler, currentAttributes);
						passEventToHandler(unmatchedHandler, currentAttributes);
					}
				}
				catch (Exception e)
				{
					logger.error("error logging attributes message");
				}
			}

			private void onJobMessage(MqMessage msg)
			{
				logger.trace("fetching job message");
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
				logger.trace("reading message subject");
				AttributeMap messageAttributes = msg.getSubject();

				try
				{
					logger.trace(String.format("Attributes message: " + LogUtil.mapToString(messageAttributes)));
				}
				catch (Exception e)
				{
					logger.error("error logging attributes message");
				}

				// passEventToHandler(assetDeletionHandler, messageAttributes);
				// passEventToHandler(assetPurgeHandler, messageAttributes);
				// passEventToHandler(emergencyIngestHandler, messageAttributes);
				passEventToHandler(itemCreationHandler, messageAttributes);
				// passEventToHandler(packageUpdateHandler, messageAttributes);
				// passEventToHandler(temporaryContentHandler, messageAttributes);
			}
			
			private void onAssetUpdate(MqMessage msg)
			{
				logger.trace("reading message subject");
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

		};

	}
}
