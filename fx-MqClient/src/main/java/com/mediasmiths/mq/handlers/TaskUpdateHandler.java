package com.mediasmiths.mq.handlers;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.mayam.MayamTaskListType;

/**
 * Handles updates for a given task list, recieves the attributes before and after the task update
 *
 */
public abstract class TaskUpdateHandler extends UpdateAttributeHandler
{

	@Override
	public void process(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		String taskListID = currentAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(getTaskType().getText()))
		{
			onTaskUpdate(currentAttributes,before,after);
		}
	}

	protected abstract void onTaskUpdate(AttributeMap currentAttributes, AttributeMap before, AttributeMap after);
	

	public abstract MayamTaskListType getTaskType();
}
