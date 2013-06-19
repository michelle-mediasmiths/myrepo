package com.mediasmiths.mq.handlers.purge;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class PurgeCandidateCreateHandler extends TaskStateChangeHandler
{
	@Inject
	PurgeEvent purgeEvent;

	@Override
	protected void stateChanged(final AttributeMap messageAttributes)
	{
		purgeEvent.setPurgeEventForPurgeCandidateTask(messageAttributes);
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.PURGE_CANDIDATE_LIST;
	}


	@Override
	public TaskState getTaskState()
	{
		return TaskState.OPEN;
	}


	@Override
	public String getName()
	{
		return "Purge candidate create";
	}
}
