package com.mediasmiths.mq.handlers.preview;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.ip.common.events.ManualQANotification;
import com.mediasmiths.foxtel.ip.event.EventService;

public class PreviewEventUtil
{
	@Inject(optional = false)
	@Named("preview.events.namespace")
	private String qaEventNamespace;

	@Inject
	protected EventService eventsService;

	private final static Logger log = Logger.getLogger(PreviewEventUtil.class);

	public void sendManualQANotification(AttributeMap messageAttributes, TaskState taskState, boolean reorder, String previewResult)
	{
		sendManualQANotification(messageAttributes, taskState, reorder, previewResult,null);
	}
	
	public void sendManualQANotification(AttributeMap messageAttributes, TaskState taskState, boolean reorder, String previewResult, String operator)
	{
		try
		{
			ManualQANotification qa = new ManualQANotification();
			qa.setAggregatorID(messageAttributes.getAttributeAsString(Attribute.AGGREGATOR));

			StringList channels = (StringList) messageAttributes.getAttribute(Attribute.CHANNELS);

			if (channels != null)
			{
				qa.setChannels(StringUtils.join(channels, ','));
			}

			String escalationLevel = messageAttributes.getAttributeAsString(Attribute.ESCALATION_LEVEL);
					
			if(escalationLevel==null){
				escalationLevel="0";
			}
			
			qa.setEscalated(escalationLevel);
			qa.setMaterialID(messageAttributes.getAttributeAsString(Attribute.HOUSE_ID));
			qa.setPreviewStatus(previewResult);
			qa.setReordered("0");

			if (reorder)
			{
				qa.setReordered("1");
			}

			qa.setTaskStatus(taskState.toString());
			qa.setTitle(messageAttributes.getAttributeAsString(Attribute.ASSET_TITLE));
			qa.setTitleLength(messageAttributes.getAttributeAsString(Attribute.MEDIA_DURATION));
			
			qa.setOperator(operator);
			
			eventsService.saveEvent(qaEventNamespace, "ManualQA", qa);

		}
		catch (Exception e)
		{
			log.error("error constructing ManualQANotification!", e);
		}
	}
}
