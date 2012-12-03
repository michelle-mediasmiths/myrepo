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

public class TaskListener {
	protected final static Logger log = Logger.getLogger(TaskListener.class);

	public static Listener getInstance(final MayamTaskController taskController) {
		final ComplianceEditingHandler compEditHandler = new ComplianceEditingHandler(
				taskController);
		final ComplianceLoggingHandler comLoggingHandler = new ComplianceLoggingHandler(
				taskController);
		final FixAndStitchHandler fixAndStitchHandler = new FixAndStitchHandler(
				taskController);
		final ImportFailureHandler importFailHandler = new ImportFailureHandler(
				taskController);
		final IngestCompleteHandler ingestCompleteHandler = new IngestCompleteHandler(
				taskController);
		final InitiateQcHandler initiateQcHandler = new InitiateQcHandler(
				taskController);
		final PreviewTaskHandler previewHandler = new PreviewTaskHandler(
				taskController);
		final QcCompleteHandler qcCompleteHandler = new QcCompleteHandler(
				taskController);
		final SegmentationCompleteHandler segmentationHandler = new SegmentationCompleteHandler(
				taskController);

		return new Listener() {
			public void onMessage(MqMessage msg) throws Throwable {
				log.trace("TaskListener onMessage");
				MqContentType type = msg.getType();
				
				if(type==null){
					log.warn("Message has a null type");
				}
				else{
					log.debug("Message is of type"+type.toString());
				
					if (type.equals(ContentTypes.ATTRIBUTES)) {
	
						AttributeMap messageAttributes = msg.getSubject();
	
						log.trace(String.format("Attributes message: "
								+ LogUtil.mapToString(messageAttributes)));
	
						try {
							compEditHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in compliance editing handler", e);
						}
						try {
							comLoggingHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in compliance logging handler", e);
						}
						try {
							fixAndStitchHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in fix and stitch handler", e);
						}
						try {
							importFailHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in import failure handler", e);
						}
						try {
							ingestCompleteHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in ingest completion handler", e);
						}
						try {
							initiateQcHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in initiate qc handler", e);
						}
						try {
							previewHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in preview handler", e);
						}
						try {
							qcCompleteHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in qc complete handler", e);
						}
						try {
							segmentationHandler.process(messageAttributes);
						} catch (Exception e) {
							log.error("exception in segmentation handler", e);
						}
					}
					else{
						log.trace("Message not of type ATTRIBUTES, ignoring");
					}
				}
			}
		};
	}
}
