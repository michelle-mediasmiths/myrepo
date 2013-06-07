package com.mediasmiths.mq.handlers.asset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.Job.JobStatus;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.JobHandler;

public class TransferJobHandler extends JobHandler
{
	private final static Logger log = Logger.getLogger(TransferJobHandler.class);

	public void process(Job jobMessage)
	{
		String assetId = jobMessage.getAssetId();
		JobStatus jobStatus = jobMessage.getJobStatus();

		log.trace(String.format("assetId %s jobStatus %s", assetId, jobStatus.toString()));

		
		if(assetId==null){
			log.error("null asset id in tranfer message");
			return; //no asset id cant do anything
		}
		
		//first reopen tasks for the itme that are in sys_wait state then extend to any packages
		
		try
		{
			AttributeMap itemAttributes = tasksClient.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), assetId);
			reopenTasksForAsset(MayamTaskListType.EXTENDED_PUBLISHING, jobStatus, itemAttributes, assetId);
		}
		catch (Exception e)
		{
			log.error("error searching for or reopending export tasks for asset", e);
		}

		List<AttributeMap> childAssets = null;
		try
		{
			tasksClient.assetApi().getAsset(AssetType.ITEM, assetId);
			childAssets = new ArrayList<AttributeMap>();
			SegmentListList segmentLists = tasksClient.segmentApi().getSegmentListsForAsset(AssetType.ITEM, assetId);
			for (SegmentList segList : segmentLists)
			{
				childAssets.add(segList.getAttributeMap());
			}
		}
		catch (RemoteException e1)
		{
			log.error("Remote Exception thrown while retrieving children for asset : " + assetId, e1);
		}

		if (childAssets != null)
		{
			log.info("Number of child assets found = " + childAssets.size());

			for (AttributeMap childAsset : childAssets)
			{
				String childId = childAsset.getAttribute(Attribute.ASSET_ID);
				if (childId != null)
				{
					reopenTasksForAsset(MayamTaskListType.TX_DELIVERY,jobStatus, childAsset, childId);
					reopenTasksForAsset(MayamTaskListType.EXTENDED_PUBLISHING,jobStatus, childAsset, childId);
				}
			}
		}
	}

	private void reopenTasksForAsset(MayamTaskListType taskListType, JobStatus jobStatus, AttributeMap assetAttributes, String assetID)
	{
		List<AttributeMap> tasks = Collections.<AttributeMap>emptyList();
		
		try
		{

			// find tasks for reopening
			tasks = taskController.getOpenTasksForAsset(
					taskListType,
					Attribute.ASSET_ID,
					assetID,
					Collections.<Attribute, Object> singletonMap(Attribute.TASK_STATE, TaskState.SYS_WAIT));

		}
		catch (MayamClientException e)
		{
			log.error("error searching for tasks for asset " + assetID, e);
		}

		reopenTasks(jobStatus, assetAttributes, assetID, tasks);
	}

	private void reopenTasks(JobStatus jobStatus, AttributeMap assetAttributes, String assetId, List<AttributeMap> tasks)
	{
		for (AttributeMap task : tasks)
		{
			log.info("Tx delivery task found for assetId : " + assetId);

			if (jobStatus.equals(JobStatus.STARTED))
			{
				log.info("High Res Transfer job has started for assetId : " + assetId);
			}
			else if (jobStatus.equals(JobStatus.FAILED))
			{
				log.warn("High Res Transfer job has failed for assetId : " + assetId);
				taskController.setTaskToErrorWithMessage(task, "High Res Transfer failed");
			}
			else if (jobStatus.equals(JobStatus.FINISHED))
			{
				log.info("High Res Transfer job has completed for assetId : " + assetId);
				AttributeMap updateMap = taskController.updateMapForTask(task);
				updateMap.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
				try
				{
					taskController.saveTask(updateMap);
				}
				catch (MayamClientException e)
				{
					log.error("error setting tx delivery task to open state", e);
					try
					{
						taskController.createWFEErrorTask(MayamAssetType.PACKAGE,
						                                  assetAttributes.getAttributeAsString(Attribute.ASSET_SITE_ID),
						                                  "error setting task to open state after high res transfer");
					}
					catch (MayamClientException e1)
					{
						log.error("error saving error task!", e1);
						// not much can be done about this
					}
				}
			}
			else
			{
				log.warn("Ingnoring message due to unknown jobStatus " + jobStatus
						+ " for Transfer task on asset id : " + assetId);
			}
		}
	}

	@Override
	public String getName()
	{
		return "Transfer Job";
	}

}
