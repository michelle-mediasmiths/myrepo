package com.mediasmiths.mq.handlers.pendingtx;

import java.util.EnumSet;

import org.apache.hadoop.mapred.TaskController;
import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskUpdateHandler;

public class PendingTxUpdateHandler extends TaskUpdateHandler
{

	private final static Logger log = Logger.getLogger(PendingTxUpdateHandler.class);

	@Override
	protected void onTaskUpdate(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		final EnumSet<TaskState> OPEN_STATES = EnumSet.of(TaskState.OPEN, TaskState.ERROR);

		TaskState taskState = currentAttributes.getAttribute(Attribute.TASK_STATE);
		if (OPEN_STATES.contains(taskState))
		{

			boolean pendingBefore = AssetProperties.isMaterialsReadyForPackages(before);
			boolean pendingAfter = AssetProperties.isMaterialsReadyForPackages(currentAttributes);

			boolean pendingStateChanges = pendingBefore && !pendingAfter; 
			
			if (pendingStateChanges)
			{
				log.info("item is now ready for tx packages to be created");

				AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);

				SegmentList segmentList = currentAttributes.getAttribute(Attribute.SEGMENTATION_LIST);
				try
				{
					packageController.createSegmentList(
							currentAttributes.getAttributeAsString(Attribute.AUX_EXTIDSTR),
							currentAttributes.getAttributeAsString(Attribute.ASSET_ID),
							segmentList);
					
					try
					{
						// update the pending tx delivery task
						updateMap.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
						taskController.saveTask(updateMap);
					}
					catch (MayamClientException e)
					{
						log.error("error updating pending tx delivery task status", e);
					}
					
				}
				catch (MayamClientException e)
				{
					log.error("error creating segment list", e);
					updateMap.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
					updateMap.setAttribute(Attribute.ERROR_MSG, "Error creating segmentlist "+e.getErrorcode().toString());
					try
					{
						taskController.saveTask(updateMap);
					}
					catch (MayamClientException e1)
					{
						log.error("error updating pending tx package task to error state", e1);
					}
				}

			}
		}
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.PENDING_TX_PACKAGE;
	}

	@Override
	public String getName()
	{
		return "Pending TX Package update";
	}
}
