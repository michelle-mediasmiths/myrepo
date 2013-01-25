package com.mediasmiths.mq.handlers.fixstitch;

import com.mayam.wf.attributes.shared.type.TaskState;

public class FixAndStitchCancelHandler extends FixAndStitchRevertHandler
{
	@Override
	public TaskState getTaskState()
	{
		return TaskState.REJECTED;
	}
	
	@Override
	public String getName()
	{
		return "Fix and Stitch Cancel";
	}
}
