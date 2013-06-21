package com.mediasmiths.mq.handlers.qc;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class QcStateChangeHandler extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(QcStateChangeHandler.class);

	@Inject
	private QcEvent qcEvent;

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		log.info("QC Task " + messageAttributes.getAttributeAsString(Attribute.TASK_STATE) + " for asset :"
				+ messageAttributes.getAttributeAsString(Attribute.HOUSE_ID));
		qcEvent.sendAutoQcEvent(messageAttributes);
	}

	@Override
	public String getName()
	{
		return "QC State Task State Changed";
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.QC_VIEW;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.FINISHED_FAILED;
	}

	@Override
	public boolean handlesAnyTaskState()
	{
		return true;
	}
}
