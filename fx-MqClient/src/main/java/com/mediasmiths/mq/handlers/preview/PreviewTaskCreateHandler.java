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
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class PreviewTaskCreateHandler extends TaskStateChangeHandler
{
	
	@Inject
	private PreviewEventUtil previewEvent;

	private final static Logger log = Logger.getLogger(PreviewTaskCreateHandler.class);
	
	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{

		if(! (AssetProperties.isQCPassed(messageAttributes) || AssetProperties.isQCParallel(messageAttributes))){
			
			log.warn("Preview task created for material that has not passed qc or marked as qc parallel, rejecting");
			
			
			AttributeMap updateMap = taskController.updateMapForTask(messageAttributes);
			updateMap.setAttribute(Attribute.ERROR_MSG, "Item has not passed qc");
			updateMap.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
			try
			{
				taskController.saveTask(updateMap);
			}
			catch (MayamClientException e)
			{
				log.error("Error removing preview task whose item had not passed qc",e);
			}
		}
		previewEvent.sendManualQANotification(messageAttributes,getTaskState(),false, MayamPreviewResults.PREVIEW_NOT_DONE);
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.PREVIEW;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.OPEN;
	}

	@Override
	public String getName()
	{
		return "Preview Task Create";
	}

}
