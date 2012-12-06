package com.mediasmiths.mq.listeners;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.mq.MqContentType;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mediasmiths.foxtel.agent.processing.EventService;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mq.LogUtil;
import com.mediasmiths.mq.handlers.ComplianceEditingHandler;
import com.mediasmiths.mq.handlers.ComplianceLoggingHandler;
import com.mediasmiths.mq.handlers.FixAndStitchHandler;
import com.mediasmiths.mq.handlers.ImportFailureHandler;
import com.mediasmiths.mq.handlers.IngestCompleteHandler;
import com.mediasmiths.mq.handlers.InitiateQcHandler;
import com.mediasmiths.mq.handlers.PreviewTaskHandler;
import com.mediasmiths.mq.handlers.QcCompleteHandler;
import com.mediasmiths.mq.handlers.SegmentationCompleteHandler;

public class TaskListener
{
	public static final String ATTRIBUTE_MESSAGE_TYPE = "mayam#attributes";
	
	protected final static Logger logger = Logger.getLogger(TaskListener.class);

	public static Listener getInstance(final MayamTaskController taskController, EventService eventService)
	{
		final ComplianceEditingHandler compEditHandler = new ComplianceEditingHandler(taskController);
		final ComplianceLoggingHandler comLoggingHandler = new ComplianceLoggingHandler(taskController);
		final FixAndStitchHandler fixAndStitchHandler = new FixAndStitchHandler(taskController);
		final ImportFailureHandler importFailHandler = new ImportFailureHandler(taskController);
		final IngestCompleteHandler ingestCompleteHandler = new IngestCompleteHandler(taskController);
		final InitiateQcHandler initiateQcHandler = new InitiateQcHandler(taskController);
		final PreviewTaskHandler previewHandler = new PreviewTaskHandler(taskController);
		final QcCompleteHandler qcCompleteHandler = new QcCompleteHandler(taskController);
		final SegmentationCompleteHandler segmentationHandler = new SegmentationCompleteHandler(taskController);

		return new MqClientListener()
		{
			public void onMessage(MqMessage msg) throws Throwable
			{
				try
				{

					logger.trace("TaskListener onMessage");
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

					logger.trace("origin is:" + origin);

					if (type != null && origin != null)
					{
						logger.trace("Type and origin both not null");

						if (type.type().equals(ATTRIBUTE_MESSAGE_TYPE) && origin.contains("task"))
						{
							log.trace("fetching message subject");
							AttributeMap messageAttributes = msg.getSubject();

							try
							{
								logger.trace(String.format("Attributes message: " + LogUtil.mapToString(messageAttributes)));
							}
							catch (Exception e)
							{
								logger.error("error logging attributes message");
							}
							
							passEventToHandler(compEditHandler, messageAttributes);
							passEventToHandler(comLoggingHandler, messageAttributes);
							passEventToHandler(fixAndStitchHandler, messageAttributes);
							passEventToHandler(importFailHandler, messageAttributes);
							passEventToHandler(ingestCompleteHandler, messageAttributes);
							passEventToHandler(initiateQcHandler, messageAttributes);
							passEventToHandler(previewHandler, messageAttributes);
							passEventToHandler(qcCompleteHandler, messageAttributes);
							passEventToHandler(segmentationHandler, messageAttributes);
						}
						else
						{
							logger.debug("Message is not of types ATTRIBUTES or is not a task message, ignoring");
						}
					}
					else
					{
						logger.debug(String.format("TaskListener onMessage, type or origin was null. Msg: %s", msg.toString()));
					}

				}
				catch (Exception e)
				{
					logger.error("Exception in task listener",e);
					throw e;
				}
			}
		};
	}
}
