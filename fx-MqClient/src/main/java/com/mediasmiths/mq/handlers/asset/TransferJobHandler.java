package com.mediasmiths.mq.handlers.asset;

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
		
		List<AttributeMap> childAssets = null;
		try {
			tasksClient.assetApi().getAsset(AssetType.ITEM, assetId);
			childAssets = tasksClient.assetApi().getAssetChildren(AssetType.ITEM, assetId, AssetType.PACKAGE);
			SegmentListList segmentLists = tasksClient.segmentApi().getSegmentListsForAsset(AssetType.ITEM, assetId);
			for (SegmentList segList:segmentLists)
			{
				childAssets.add(segList.getAttributeMap());
			}
		} catch (RemoteException e1) {
			log.error("Remote Exception thrown while retrieving children for asset : " + assetId, e1);
			e1.printStackTrace();
		}
			
		if (childAssets != null)
		{
			log.info("Number of child assets found = " + childAssets.size());
			
			for (AttributeMap childAsset: childAssets)
			{
				String childId = childAsset.getAttribute(Attribute.ASSET_ID);
				if(childId!=null)
				{
					AttributeMap task = null;
					long taskId = -1;
					try {
						task = taskController.getOnlyOpenTaskForAssetByAssetID(MayamTaskListType.TX_DELIVERY, childId);
						taskId = task.getAttribute(Attribute.TASK_ID);
					} catch (MayamClientException e) {
						log.info("Mayam exception thrown while retrieving Tx Delivery task for asset : " + childId, e);
					}
					
					try {
						if (task != null)
						{
							log.info("Tx delivery task found for assetId : " + childId);
							TaskState taskState = (TaskState) task.getAttribute(Attribute.TASK_STATE);
							
							if(taskState == TaskState.SYS_WAIT){
							
								if (jobStatus.equals(JobStatus.STARTED))
								{
									log.info("High Res Transfer job has started for assetId : " + childId);
								}
								else if (jobStatus.equals(JobStatus.FAILED))
								{
									log.warn("High Res Transfer job has failed for assetId : " + childId);
									taskController.setTaskToErrorWithMessage(taskId, "High Res Transfer failed");
								}
								else if (jobStatus.equals(JobStatus.FINISHED))
								{
									log.info("High Res Transfer job has completed for assetId : " + childId);
									task.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
									taskController.saveTask(task);
								}
								else {
									log.warn("Ingnoring message due to unknown jobStatus " + jobStatus + " for Transfer task on asset id : " + childId);
								}
							}
							else{
								log.info("Items tx delivery task is not in a SYS_WAIT state, I am making no updates");
							}
						}
						else {
							log.warn("Unable to find tx delivery task for assetId : " + childId);
						}
					}
					catch (MayamClientException e) {
						log.warn("Mayam exception thrown while updating Tx Delivery task for asset : " + childId, e);
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		return "Transfer Job";
	}

}
