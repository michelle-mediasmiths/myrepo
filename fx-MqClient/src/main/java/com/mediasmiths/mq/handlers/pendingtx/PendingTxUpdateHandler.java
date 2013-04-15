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
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.controllers.MayamPackageController;
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

			boolean packagesConsideredPendingBefore = AssetProperties.shouldPackagesForMaterialBeConsideredPending(before);			
			boolean fromAuthSourceBefore =  packageSeenFromAuthoritativeSource(before);
			
			boolean packagesConsideredPendingAfter = AssetProperties.shouldPackagesForMaterialBeConsideredPending(currentAttributes);			
			boolean fromAuthSourceAfter=  packageSeenFromAuthoritativeSource(currentAttributes);

			boolean shouldPackageBeCreatedBefore = (!packagesConsideredPendingBefore) && fromAuthSourceBefore;
			boolean shouldPackageBeCreatedAfter = (!packagesConsideredPendingAfter) && fromAuthSourceAfter ;
			
			
			boolean pendingStateChanges = (!shouldPackageBeCreatedBefore) && shouldPackageBeCreatedAfter; 
			
			if (pendingStateChanges)
			{
				log.info("item is now ready for tx packages to be created (package has been seen coming from bms)");

				AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);

				SegmentList segmentList = currentAttributes.getAttribute(Attribute.SEGMENTATION_LIST);
				
				

				try
				{
					// check if there has ever been a fix and stitch task for this asset
					boolean fixAndStitchItem = taskController.fixAndStitchTaskExistsForItem(currentAttributes.getAttributeAsString(Attribute.HOUSE_ID));

					if (fixAndStitchItem && segmentList != null && segmentList.getEntries() != null)
					{
						segmentList.getEntries().clear(); // clear segmentation information if there was ever a fix and stitch task for this item
					}

				}
				catch (Exception e)
				{
					log.error("error searching for fix and stitch tasks, segmentation information will be populated", e);
				}
				
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

	/*
	 * if pending package information has only come from an aggregator a real tx package should not be created.
	 */
	private boolean packageSeenFromAuthoritativeSource(AttributeMap currentAttributes)
	{
		String auxSrc = currentAttributes.getAttribute(Attribute.AUX_SRC);
		
		log.debug("AUX_SRC is"+auxSrc);
		
		if(auxSrc!=null && auxSrc.equals(MayamPackageController.PENDING_TX_PACKAGE_SOURCE_BMS)){
			return true;
		}
		
		return false;
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
