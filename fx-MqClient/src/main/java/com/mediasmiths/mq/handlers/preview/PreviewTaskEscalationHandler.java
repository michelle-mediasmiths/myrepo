package com.mediasmiths.mq.handlers.preview;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.ip.common.events.ManualQANotification;
import com.mediasmiths.foxtel.ip.common.events.PreviewFailed;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Set;

public class PreviewTaskEscalationHandler extends TaskStateChangeHandler
{

	@Inject
	private PreviewEventUtil previewEvent;
	
	@Inject(optional = false)
	@Named("preview.events.namespace")
	private String qaEventNamespace;
	
	private final static Logger log = Logger.getLogger(PreviewTaskEscalationHandler.class);

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		try
		{
			onPreviewEscalated(messageAttributes);
		}
		catch (Exception e)
		{
			log.error("Exception in the Mayam client while handling Preview Task Escalation : ", e);
		}
		
	}
	
	private void onPreviewEscalated(AttributeMap messageAttributes) throws MayamClientException, RemoteException
	{
		String previewResult = messageAttributes.getAttribute(Attribute.QC_PREVIEW_RESULT);
		
		String escalationLevel = messageAttributes.getAttributeAsString(Attribute.ESCALATION_LEVEL);

		log.info("preview state is " + previewResult);
		log.info("escalation Level: " + escalationLevel);
		
		
		if (escalationLevel != null)
		{
			
			try
			{
				if (Integer.parseInt(escalationLevel) == MayamPreviewResults.ESCALATION_LEVEL_1)
				{
					sendEscalationEvent(messageAttributes, "PreviewFurtherAnalysis");
				}
			}
			catch (Exception e)
			{
				log.fatal("exception in retrieving escalatiuon level.", e);
			}
		}
		else
		{
			log.error("null PREVIEW_ESCALATED LEVEL after preview task escalated.");
		}
	}

	private void sendEscalationEvent(final AttributeMap messageAttributes, String escalationEventName)
	{
		String houseID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
		PreviewFailed pf = new PreviewFailed();
		//pf.setDate((new Date()).toString());
		pf.setTitle(messageAttributes.getAttribute(Attribute.ASSET_TITLE).toString());
		pf.setAssetId(houseID);
		pf.setPreviewNotes(messageAttributes.getAttributeAsString(Attribute.QC_PREVIEW_NOTES));
		
		try
		{
			Set<String> channelGroups = mayamClient.getChannelGroupsForItem(messageAttributes);
			pf.getChannelGroup().addAll(channelGroups);
		}
		catch (Exception e)
		{
			log.error("error determining channel groups for event", e);
		}
		eventsService.saveEvent("http://www.foxtel.com.au/ip/preview", escalationEventName, pf);
	}

	@Override
	public String getName()
	{
		return "Preview task escalation";
	}


	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.PREVIEW;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.ESCALATED;
	}
}
