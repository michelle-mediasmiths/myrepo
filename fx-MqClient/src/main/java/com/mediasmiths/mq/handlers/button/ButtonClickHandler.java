package com.mediasmiths.mq.handlers.button;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamButtonType;
import com.mediasmiths.mq.handlers.AttributeHandler;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;
import org.apache.log4j.Logger;

public abstract class ButtonClickHandler extends AttributeHandler
{

	private final static Logger logger = Logger.getLogger(TaskStateChangeHandler.class);

	@Override
	public final void process(AttributeMap messageAttributes)
	{
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		TaskState state = (TaskState) messageAttributes.getAttribute(Attribute.TASK_STATE);
		
		if (taskListID != null && taskListID.equals(getButtonType().getText()) && state.equals(TaskState.FINISHED))
		{
			logger.debug(String.format("{%s} Begin", getName()));
			buttonClicked(messageAttributes);
			logger.debug(String.format("{%s} End", getName()));
		}

	}

	protected abstract void buttonClicked(AttributeMap messageAttributes);

	public abstract MayamButtonType getButtonType();

}
