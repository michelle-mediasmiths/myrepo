package com.mediasmiths.mq.listeners;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.mq.MqContentType;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
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

public class TaskListener {
	protected final static Logger log = Logger.getLogger(TaskListener.class);
	
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
				log.trace("TaskListener onMessage");				
				log.trace("Message is: " + msg.toString());
				
				MqContentType type = msg.getType();

				if(type != null){
					log.trace("Message type not null "+type.toString());
					log.debug("Message type is "+type.toString());
				}
				
				log.trace("post msg.getType()");
				
				String origin = msg.getProperties().get(MqMessage.PROP_ORIGIN_DESTINATION);
				
				log.trace("origin is:"+origin);
				
				if (type != null && origin != null) 
				{
					if (type.equals(ContentTypes.ATTRIBUTES) && origin.contains("task"))
					{			
						AttributeMap messageAttributes = msg.getSubject();
						
						log.trace(String.format("Attributes message: "+LogUtil.mapToString(messageAttributes)));
						
						passEventToHandler(compEditHandler,messageAttributes);
						passEventToHandler(comLoggingHandler,messageAttributes);
						passEventToHandler(fixAndStitchHandler,messageAttributes);
						passEventToHandler(importFailHandler,messageAttributes);
						passEventToHandler(ingestCompleteHandler,messageAttributes);
						passEventToHandler(initiateQcHandler,messageAttributes);
						passEventToHandler(previewHandler,messageAttributes);
						passEventToHandler(qcCompleteHandler,messageAttributes);
						passEventToHandler(segmentationHandler,messageAttributes);
					}else{
						log.debug("Message is not of types ATTRIBUTES, ignoring");
					}
				}
				else {
					log.debug(String.format("TaskListener onMessage, type or origin was null. Msg: %s", msg.toString()));
				}
			}
		};
	}
}
