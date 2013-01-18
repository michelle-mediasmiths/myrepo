package com.mediasmiths.mq.handlers;

import java.util.List;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.FilterResult;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;

public class PackageUpdateHandler  extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(PackageUpdateHandler.class);

	public void process(AttributeMap messageAttributes)
	{	
		String houseID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
		String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
		AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
		
		if (houseID == null || houseID.equals(""))
		{			
			// If the message is for an existing package
			if (assetType.equals(MayamAssetType.PACKAGE.getAssetType()))
			{
				try {
					//Update metadata for asset
					tasksClient.assetApi().updateAsset(messageAttributes);
					
					AttributeMap filterEqualities = tasksClient.createAttributeMap();
					filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.SEGMENTATION.toString());
					filterEqualities.setAttribute(Attribute.HOUSE_ID, houseID);
					FilterCriteria criteria = new FilterCriteria();
					criteria.setFilterEqualities(filterEqualities);
					FilterResult existingTasks = tasksClient.taskApi().getTasks(criteria, 10, 0);
					
					// Check that no segmentation task already exists
					if (existingTasks.getTotalMatches() == 0) {
						boolean requiresSegTask = false;
						
						//Retrieve the tx-tasks and preview tasks associated with the asset
						filterEqualities.clear();
						filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.TX_DELIVERY.toString());
						filterEqualities.setAttribute(Attribute.HOUSE_ID, houseID);
						criteria.setFilterEqualities(filterEqualities);
						FilterResult txTasks = tasksClient.taskApi().getTasks(criteria, 10, 0);
						boolean txReady = false;
						
						filterEqualities.clear();
						filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PREVIEW.toString());
						filterEqualities.setAttribute(Attribute.HOUSE_ID, houseID);
						criteria.setFilterEqualities(filterEqualities);
						FilterResult previewTasks = tasksClient.taskApi().getTasks(criteria, 10, 0);
						
						if (txTasks.getTotalMatches() > 0) {
							//Check that the status is set to ready
							List<AttributeMap> txTaskList = txTasks.getMatches();
							for (int i = 0; i < txTasks.getTotalMatches(); i++) {
								AttributeMap txTask = txTaskList.get(i);
								TaskState taskState = txTask.getAttribute(Attribute.TASK_STATE);

								if (taskState.equals(TaskState.OPEN)) {
									txReady = true;
								}
							}
						}
						
						if (txReady && previewTasks.getTotalMatches() > 0) {
							//Check that the status is set to pass
							List<AttributeMap> previewTaskList = previewTasks.getMatches();
							for (int i = 0; i < previewTasks.getTotalMatches(); i++) {
								AttributeMap previewTask = previewTaskList.get(i);
								TaskState taskState = previewTask.getAttribute(Attribute.TASK_STATE);
								
								if (taskState.equals(TaskState.FINISHED)) {
									requiresSegTask = true;
								}
							}
						}
						
						if (requiresSegTask) {
							//Create new segmentation task for this asset
							long taskID = taskController.createTask(houseID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.SEGMENTATION);
							AttributeMap newTask = taskController.getTask(taskID);
							newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
							taskController.saveTask(newTask);
						}
					}
				}
				catch (Exception e) {
					log.error("Exception in the Mayam client while handling Package Message : ", e);
				}
			}
		}
		else {
			try {
				// If the message is for a package which doesnt exist
				if (assetType.equals(MayamAssetType.PACKAGE.getAssetType()))
				{	
					//Create new Tx-Package + associate with parent (setting Parent ID should be handled automatically)
					tasksClient.assetApi().createAsset(messageAttributes);
					long parentId = messageAttributes.getAttribute(Attribute.ASSET_PARENT_ID);

					//If parent asset has a Preview status of Pass then create a new segmentation task
					boolean requiresSegTask = false;
					AttributeMap filterEqualities = tasksClient.createAttributeMap();
					filterEqualities.setAttribute(Attribute.ASSET_TYPE, MayamTaskListType.PREVIEW.toString());
					filterEqualities.setAttribute(Attribute.HOUSE_ID, parentId);
					FilterCriteria criteria = new FilterCriteria();
					criteria.setFilterEqualities(filterEqualities);
					FilterResult previewTasks = tasksClient.taskApi().getTasks(criteria, 10, 0);
					
					if (previewTasks.getTotalMatches() > 0) {
						//Check that the status is set to pass
						List<AttributeMap> previewTaskList = previewTasks.getMatches();
						for (int i = 0; i < previewTasks.getTotalMatches(); i++) {
							AttributeMap previewTask = previewTaskList.get(i);
							TaskState taskState = previewTask.getAttribute(Attribute.TASK_STATE);
						
							if (taskState.equals(TaskState.FINISHED)) {
								requiresSegTask = true;
							}
						}
					}
					
					if (requiresSegTask) {
						//Create new segmentation task for this asset
						SegmentListList lists = tasksClient.segmentApi().getSegmentListsForAsset(assetType, assetID);
						if (lists != null) 
						{
							for (SegmentList segmentList : lists)
							{
								String segHouseID = segmentList.getAttributeMap().getAttribute(Attribute.HOUSE_ID);
								long taskID = taskController.createTask(segHouseID, MayamAssetType.PACKAGE, MayamTaskListType.SEGMENTATION);
								AttributeMap newTask = taskController.getTask(taskID);
								newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
								taskController.saveTask(newTask);
							}
						}
					}
				}
			}
			catch (Exception e) {
				log.error("Exception in the Mayam client while handling Package Message : ", e);
			}
		}
	}

	@Override
	public String getName()
	{
		return "Package Update";
	}
}					