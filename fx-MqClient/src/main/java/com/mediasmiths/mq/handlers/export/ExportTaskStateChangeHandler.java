package com.mediasmiths.mq.handlers.export;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.foxtel.ip.common.events.ExtendedPublishingTaskEvent;
import com.mediasmiths.mayam.DateUtil;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * Sends events used to track extended publishing tasks for reporting
 */
public class ExportTaskStateChangeHandler extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(ExportTaskStateChangeHandler.class);

	@Inject
	@Named("tc.events.namespace")
	private String nameSpace;

	@Inject
	DateUtil dateUtil;

	@Override
	protected void stateChanged(final AttributeMap messageAttributes)
	{
		log.info("Building event for extended publishing reporting");
		final String houseID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
		final AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
		final Long taskID = (Long) messageAttributes.getAttribute(Attribute.TASK_ID);

		String materialID = null;

		if (MayamAssetType.PACKAGE.equals(assetType))
		{
			log.debug("Export task is for package");
			materialID = messageAttributes.getAttribute(Attribute.PARENT_HOUSE_ID);
		}
		else
		{
			materialID = houseID;
		}

		ExtendedPublishingTaskEvent ep = new ExtendedPublishingTaskEvent();
		ep.setMaterialID(materialID);
		ep.setTaskID(taskID);
		ep.setExportType((String)messageAttributes.getAttribute(Attribute.OP_TYPE));
		ep.setRequestedBy((String) messageAttributes.getAttribute(Attribute.TASK_CREATED_BY));
		Date created = messageAttributes.getAttribute(Attribute.TASK_CREATED);
		Date updated = messageAttributes.getAttribute(Attribute.TASK_UPDATED);

		if (created != null)
		{
			ep.setTaskCreated(dateUtil.fromDate(created));
		}
		else
		{
			log.warn("Null created date on export task, using current date " + taskID);
			ep.setTaskCreated(dateUtil.fromDate(new Date()));
		}

		if (updated != null)
		{
			ep.setTaskUpdated(dateUtil.fromDate(updated));
		}

		TaskState state = messageAttributes.getAttribute(Attribute.TASK_STATE);
		ep.setTaskStatus(state.toString());

		eventsService.saveEvent(nameSpace, EventNames.EXTENDED_PUBLISHING_TASK_EVENT, ep);
	}


	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.EXTENDED_PUBLISHING;
	}


	@Override
	public TaskState getTaskState()
	{
		return TaskState.OPEN;
	}


	@Override
	public String getName()
	{
		return "Extended Publishing Task State Change";
	}


	@Override
	public boolean handlesAnyTaskState()
	{
		return true;
	}
}
