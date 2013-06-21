package com.mediasmiths.mq.handlers.asset;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;
import com.mediasmiths.mq.handlers.preview.PreviewTaskCreateHandler;

public class TaskCreateHandler extends TaskStateChangeHandler
{

	private final static Logger log = Logger.getLogger(PreviewTaskCreateHandler.class);
	
	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		try
		{
			AttributeMap withNewAccessRights = accessRightsController.updateAccessRights(messageAttributes.copy());
			AttributeMap update = taskController.updateMapForTask(withNewAccessRights);
			update.setAttribute(Attribute.ASSET_ACCESS, withNewAccessRights.getAttribute(Attribute.ASSET_ACCESS));
			tasksClient.taskApi().updateTask(update);
		}
		catch (Exception e)
		{
			log.error("error updating assets access rights", e);
		}
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return null;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.OPEN;
	}

	@Override
	public String getName()
	{
		return "Task Create";
	}

}
