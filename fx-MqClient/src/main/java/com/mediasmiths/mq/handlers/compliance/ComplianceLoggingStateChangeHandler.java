package com.mediasmiths.mq.handlers.compliance;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.ip.common.events.ComplianceLoggingTaskEvent;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.mayam.DateUtil;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;
import org.apache.log4j.Logger;

import java.util.Date;

public class ComplianceLoggingStateChangeHandler extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(ComplianceLoggingStateChangeHandler.class);

	@Inject
	DateUtil dateUtil;


	@Inject
	@Named("system.events.namespace")
	private String nameSpace;


	@Override
	protected void stateChanged(final AttributeMap messageAttributes)
	{
		log.info("Building event for compliance logging reporting");
		final String houseID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
		final Long taskID = (Long) messageAttributes.getAttribute(Attribute.TASK_ID);
		final String sourceID = messageAttributes.getAttribute(Attribute.SOURCE_HOUSE_ID);
		final String titleID = messageAttributes.getAttribute(Attribute.PARENT_HOUSE_ID);

		String materialID = houseID;

		ComplianceLoggingTaskEvent ep = new ComplianceLoggingTaskEvent();
		ep.setMaterialID(materialID);
		ep.setTaskID(taskID);
		ep.setSourceMaterialID(sourceID);
		ep.setTitleID(titleID);

		TaskState state = messageAttributes.getAttribute(Attribute.TASK_STATE);
		ep.setTaskStatus(state.toString());

		Date created = messageAttributes.getAttribute(Attribute.TASK_CREATED);
		Date updated = messageAttributes.getAttribute(Attribute.TASK_UPDATED);

		if (created != null)
		{
			ep.setTaskCreated(dateUtil.fromDate(created));
		}
		else
		{
			log.warn("Null created date on export task, using current date" + taskID);
			ep.setTaskCreated(dateUtil.fromDate(new Date()));
		}

		if (updated != null)
		{
			ep.setTaskUpdated(dateUtil.fromDate(updated));
		}


		eventsService.saveEvent(nameSpace, EventNames.COMPLIANCE_LOGGING_TASK_EVENT, ep);
	}


	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.COMPLIANCE_LOGGING;
	}


	@Override
	public TaskState getTaskState()
	{
		return TaskState.OPEN;
	}


	@Override
	public String getName()
	{
		return "Compliance Logging Task State Change";
	}


	@Override
	public boolean handlesAnyTaskState()
	{
		return true;
	}

}
