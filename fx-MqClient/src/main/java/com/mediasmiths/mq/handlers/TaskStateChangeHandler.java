package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamTaskListType;

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
		if (taskListID.equals(getTaskType().getText()))
		{
			TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);
			if (taskState == getTaskState())
			{
				logger.debug(String.format("{%s} Begin", getName()));
				stateChanged(messageAttributes);
				logger.debug(String.format("{%s} End", getName()));
			}
		}

	}
	
	protected void closeTask(AttributeMap messageAttributes)
	{
		try
		{
			messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
			taskController.saveTask(messageAttributes);
		}
		catch (Exception e)
		{
			logger.error("Exception removing task "+getTaskType(), e);
		}
	}

	/*
	 * call by process once it is known that the task concerned is of the correct type and in the correct state
	 */
	protected abstract void stateChanged(AttributeMap messageAttributes);

	public abstract MayamTaskListType getTaskType();

	public abstract TaskState getTaskState();
	

}
