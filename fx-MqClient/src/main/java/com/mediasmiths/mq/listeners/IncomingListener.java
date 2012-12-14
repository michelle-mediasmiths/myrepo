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

public class IncomingListener
{
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
		final IngestJobHandler unmatchedJobHandler = new IngestJobHandler(taskController);
		
		return new MqClientListener()
		{
			public void onMessage(MqMessage msg) throws Throwable
			{
				try
				{
					logger.trace("AssetListener onMessage");
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
					String changeType = msg.getProperties().get("MayamChangeType");

					logger.trace("origin is:" + origin);
					logger.trace("Change Type is:" + changeType);

					if (type != null && origin != null)
					{
						logger.trace("Type and origin both not null");

						if ((type.type().equals(IncomingListener.JOB_MESSAGE_TYPE)))
						{
							logger.trace("fetching job message");
							Job jobMessage = msg.getJob();

							if (jobMessage != null)
							{
								JobType jobType = jobMessage.getJobType();
								log.debug("jobType is " + jobType.name());

								if (jobType != null)
								{
									if (jobType.equals(JobType.INGEST))
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
						else if (changeType != null)
						{

							logger.trace("changeType not null");

							if (type.type().equals(IncomingListener.ATTRIBUTE_MESSAGE_TYPE) && origin.contains("asset")
									&& changeType.equals("CREATE"))
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
							else if (type.type().equals(IncomingListener.ATTRIBUTE_PAIR) && origin.contains("asset")
									&& changeType.equals("UPDATE"))
							{
								logger.trace("reading message subject");
								AttributeMapPair messageAttributes = msg.getSubjectPair();
								AttributeMap beforeAttributes = messageAttributes.getBefore();
								AttributeMap afterAttributes = messageAttributes.getAfter();
								try
								{
									logger.trace(String.format("Attributes message (before): "
											+ LogUtil.mapToString(beforeAttributes)));
									logger.trace(String.format("Attributes message (after): "
											+ LogUtil.mapToString(afterAttributes)));
								}
								catch (Exception e)
								{
									logger.error("error logging attributes message");
								}

								// TODO:Handlers for asset updates
							}
							else if (type.type().equals(TaskListener.ATTRIBUTE_MESSAGE_TYPE) && origin.contains("task")
									&& changeType.equals("CREATE"))
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
							else if (type.type().equals(IncomingListener.ATTRIBUTE_PAIR) && origin.contains("task")
									&& changeType.equals("UPDATE"))
							{
								logger.trace("reading message subject");
								AttributeMapPair messageAttributes = msg.getSubjectPair();
								AttributeMap beforeAttributes = messageAttributes.getBefore();
								AttributeMap afterAttributes = messageAttributes.getAfter();
								try
								{
									logger.trace(String.format("Attributes message (before): "
											+ LogUtil.mapToString(beforeAttributes)));
									logger.trace(String.format("Attributes message (after): "
											+ LogUtil.mapToString(afterAttributes)));
									
									TaskState initialState = beforeAttributes.getAttribute(Attribute.TASK_STATE);
									TaskState newState = afterAttributes.getAttribute(Attribute.TASK_STATE);
									
									if (!initialState.equals(newState)) {
										passEventToHandler(ingestCompleteHandler, afterAttributes);
										passEventToHandler(qcCompleteHandler, afterAttributes);
										passEventToHandler(previewHandler, afterAttributes);
										passEventToHandler(unmatchedHandler, afterAttributes);
									}
								}
								catch (Exception e)
								{
									logger.error("error logging attributes message");
								}
							}
						}
						else
						{
							logger.debug("Message is not of an expected type, ignoring");
						}
					}
					else
					{
						logger.debug(String.format("AssetListener onMessage, type or origin was null. Msg: %s", msg.toString()));
					}
				}
				catch (Exception e)
				{
					logger.error("Exception in asset listener", e);
					throw e;
				}
			}

		};

	}
}
