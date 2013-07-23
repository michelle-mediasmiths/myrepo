package com.mediasmiths.mq.handlers.pendingtx;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.PackageNotFoundException;
import com.mediasmiths.mayam.controllers.MayamPackageController;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.TaskUpdateHandler;
import org.apache.log4j.Logger;

import java.util.EnumSet;

public class PendingTxUpdateHandler extends TaskUpdateHandler
{

	private final static Logger log = Logger.getLogger(PendingTxUpdateHandler.class);

	@Override
	protected void onTaskUpdate(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		final EnumSet<TaskState> OPEN_STATES = EnumSet.of(TaskState.OPEN, TaskState.ERROR);

		// Refetching task to ensure attributes are latest
		try {
			Long taskId = currentAttributes.getAttribute(Attribute.TASK_ID);
			currentAttributes = taskController.getTask(taskId);
			currentAttributes.putAll(after);
		} catch (RemoteException e) {
			log.warn("Error refetching task attributes. Some attributes may be out of date.", e);
		}
		
		TaskState taskState = currentAttributes.getAttribute(Attribute.TASK_STATE);
		if (OPEN_STATES.contains(taskState))
		{

			boolean packagesConsideredPendingAfter = AssetProperties.shouldPackagesForMaterialBeConsideredPending(currentAttributes);
			boolean fromAuthSourceAfter = packageSeenFromAuthoritativeSource(currentAttributes);

			boolean shouldPackageBeCreatedAfter = (!packagesConsideredPendingAfter) && fromAuthSourceAfter;

			log.debug(String.format(
					"packagesConsideredPendingAfter %b, fromAuthSourceAfter %b, shouldPackageBeCreatedAfter %b",
					packagesConsideredPendingAfter,
					fromAuthSourceAfter,
					shouldPackageBeCreatedAfter));

			if (shouldPackageBeCreatedAfter)
			{
				log.info("item is now ready for tx packages to be created (package has been seen coming from bms and item has passed qc)");

				AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);

				SegmentList segmentList = currentAttributes.getAttribute(Attribute.SEGMENTATION_LIST);

				String packageID = (String) currentAttributes.getAttribute(Attribute.AUX_EXTIDSTR);

				if (packageID == null)
				{
					errorWithReason(updateMap, "AUX_EXTIDSTR null on a pending tx package task");
					return;
				}

				try
				{
					log.debug("attemping to load segmentlist with  id " + packageID);
					SegmentList realPackage = packageController.getSegmentList(packageID);

					if (realPackage != null)
					{
						log.error(String.format("Package %s already exists", packageID));
						errorWithReason(updateMap, "Package already exists");
						return;
					}
					else
					{
						log.debug("No real package with id " + packageID);
					}
				}
				catch (PackageNotFoundException e2)
				{
					log.debug("ignoring package not found exception for package " + packageID
							+ " as it is not expected to currently exist");
				}

				try
				{
					// check if there has ever been a fix and stitch task for this asset
					boolean fixAndStitchItem = taskController.fixAndStitchTaskExistsForItem(currentAttributes.getAttributeAsString(Attribute.ASSET_ID));

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
					errorWithReason(updateMap, "Error creating segmentlist " + e.getErrorcode().toString());
				}

			}
		}
	}

	private void errorWithReason(AttributeMap updateMap, String reason)
	{

		updateMap.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
		updateMap.setAttribute(Attribute.ERROR_MSG, reason);
		try
		{
			taskController.saveTask(updateMap);
		}
		catch (MayamClientException e1)
		{
			log.error("error updating pending tx package task to error state", e1);
		}
	}

	/*
	 * if pending package information has only come from an aggregator a real tx package should not be created.
	 */
	private boolean packageSeenFromAuthoritativeSource(AttributeMap currentAttributes)
	{
		String auxSrc = currentAttributes.getAttribute(Attribute.AUX_SRC);

		log.info("AUX_SRC is" + auxSrc);

		if (auxSrc != null && auxSrc.equals(MayamPackageController.PENDING_TX_PACKAGE_SOURCE_BMS))
		{
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
