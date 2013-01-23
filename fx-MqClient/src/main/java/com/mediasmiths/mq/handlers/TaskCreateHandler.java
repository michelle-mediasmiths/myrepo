package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamTaskListType;

public abstract class TaskCreateHandler extends AttributeHandler
{
	
	private final static Logger logger = Logger.getLogger(TaskCreateHandler.class);

	@Override
	public final void process(AttributeMap messageAttributes)
	{
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(getTaskType().getText()))
		{
			TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);
			if (taskState == getTaskState())
			{
				logger.debug(String.format("{%s} Task of the type and state I am interested in", getName()));
				taskCreated(messageAttributes);
			}
		}

	}

	/*
	 * call by process once it is known that the task concerned is of the correct type and in the correct state
	 */
	protected abstract void taskCreated(AttributeMap messageAttributes);

	public abstract MayamTaskListType getTaskType();

	public TaskState getTaskState()
	{
		return TaskState.OPEN;
	}

}
