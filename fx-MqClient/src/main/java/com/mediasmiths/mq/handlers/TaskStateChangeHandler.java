package com.mediasmiths.mq.handlers;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamTaskListType;
import org.apache.log4j.Logger;

/**
 * Handles a state change for a task, takes only the current attributes of the task
 *
 */
public abstract class TaskStateChangeHandler extends AttributeHandler
{
	
	private final static Logger logger = Logger.getLogger(TaskStateChangeHandler.class);

	@Override
	public final void process(AttributeMap messageAttributes)
	{
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (getTaskType() == null || taskListID.equals(getTaskType().getText()))
		{
			TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);
			if (handlesAnyTaskState() || taskState == getTaskState())
			{
				String houseID = messageAttributes.getAttributeAsString(Attribute.HOUSE_ID);
				logger.debug(String.format("{%s} Begin, %s", getName(), houseID));
				stateChanged(messageAttributes);
				logger.debug(String.format("{%s} End, %s", getName(), houseID));
			}
		}

	}
	
	/*
	 * call by process once it is known that the task concerned is of the correct type and in the correct state
	 */
	protected abstract void stateChanged(AttributeMap messageAttributes);

	public abstract MayamTaskListType getTaskType();

	public abstract TaskState getTaskState();
	
	public boolean handlesAnyTaskState(){
		return false;
	}

}
