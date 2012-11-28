package com.mediasmiths.mq.listeners;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.mq.MqContentType;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
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
	protected final static Logger log = Logger.getLogger(TaskListener.class);
	
	public static Listener getInstance(final MayamTaskController taskController) 
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
		
		return new Listener() 
		{
			public void onMessage(MqMessage msg) throws Throwable 
			{
				log.trace("TaskListener onMessage");
				log.trace("pre msg.getType()");  
				MqContentType type = msg.getType();
				log.trace("post msg.getType()");
				
				if (type.equals(ContentTypes.ATTRIBUTES)) 
				{
					
					AttributeMap messageAttributes = msg.getSubject();
					
					log.trace(String.format("Attributes message: "+LogUtil.mapToString(messageAttributes)));
					
					compEditHandler.process(messageAttributes);
					comLoggingHandler.process(messageAttributes);
					fixAndStitchHandler.process(messageAttributes);
					importFailHandler.process(messageAttributes);
					ingestCompleteHandler.process(messageAttributes);
					initiateQcHandler.process(messageAttributes);
					previewHandler.process(messageAttributes);
					qcCompleteHandler.process(messageAttributes);
					segmentationHandler.process(messageAttributes);
				}
			}
		};
	}
}
