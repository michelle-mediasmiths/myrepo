package com.mediasmiths.mq.listeners;

import java.util.List;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.FilterResult;
import com.mayam.wf.ws.client.jaxws.AssetType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class PackageUpdateListener 
{
	public static Listener getInstance(final TasksClient client, final MayamTaskController taskController) 
	{
		return new Listener() 
		{
			public void onMessage(MqMessage msg) throws Throwable 
			{
				if (msg.getType().equals(ContentTypes.ATTRIBUTES)) 
				{
					AttributeMap messageAttributes = msg.getSubject();
					String assetID = messageAttributes.getAttribute(Attribute.ASSET_GUID);
					String assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
					
					//TODO: Need to confirm check for already existing asset
					if (assetID == null || assetID.equals(""))
					{			
						// If the message is for an existing package
						if (assetType.equals(MayamAssetType.PACKAGE.getAssetType()))
						{
							//Update metadata for asset
							client.updateAsset(messageAttributes);
							
							AttributeMap filterEqualities = client.createAttributeMap();
							filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.SEGMENTATION.toString());
							filterEqualities.setAttribute(Attribute.ASSET_GUID, assetID);
							FilterCriteria criteria = new FilterCriteria();
							criteria.setFilterEqualities(filterEqualities);
							FilterResult existingTasks = client.getTasks(criteria, 10, 0);
							
							// Check that no segmentation task already exists
							if (existingTasks.getTotalMatches() == 0) {
								boolean requiresSegTask = false;
								
								//Retrieve the tx-tasks and preview tasks associated with the asset
								filterEqualities.clear();
								filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.TX_DELIVERY.toString());
								filterEqualities.setAttribute(Attribute.ASSET_GUID, assetID);
								criteria.setFilterEqualities(filterEqualities);
								FilterResult txTasks = client.getTasks(criteria, 10, 0);
								boolean txReady = false;
								
								filterEqualities.clear();
								filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PREVIEW.toString());
								filterEqualities.setAttribute(Attribute.ASSET_GUID, assetID);
								criteria.setFilterEqualities(filterEqualities);
								FilterResult previewTasks = client.getTasks(criteria, 10, 0);
								
								if (txTasks.getTotalMatches() > 0) {
									//Check that the status is set to ready
									List<AttributeMap> txTaskList = txTasks.getMatches();
									for (int i = 0; i < txTasks.getTotalMatches(); i++) {
										AttributeMap txTask = txTaskList.get(i);
										TaskState taskState = txTask.getAttribute(Attribute.TASK_STATE);
										
										//TODO: Confirm that a task state of Open (rather than Active) equates to tx Ready
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
										
										//TODO: Confirm that a task state of Finished equates to preview passed
										if (taskState.equals(TaskState.FINISHED)) {
											requiresSegTask = true;
										}
									}
								}
								
								if (requiresSegTask) {
									//Create new segmentation task for this asset
									long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.SEGMENTATION);
									AttributeMap newTask = client.getTask(taskID);
									newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
									client.updateTask(newTask);
								}
							}
						}
					}
					else {
						// If the message is for a package which doesnt exist
						if (assetType.equals(MayamAssetType.PACKAGE.getAssetType()))
						{	
							//Create new Tx-Package + associate with parent (setting Parent ID should be handled automatically)
							client.createAsset(messageAttributes);
							
							long parentId = 0;
							//TODO: Parent ID attribute not yet implemented
							//parentId = messageAttributes.getAttribute(Attribute.ASSET_PARENT_ID);
							
							
							//TODO: set the ACLs for the Item in accordance with the FOXTEL security model
							
							
							//If parent asset has a Preview status of Pass then create a new segmentation task
							boolean requiresSegTask = false;
							AttributeMap filterEqualities = client.createAttributeMap();
							filterEqualities.setAttribute(Attribute.ASSET_TYPE, MayamTaskListType.PREVIEW.toString());
							filterEqualities.setAttribute(Attribute.ASSET_GUID, parentId);
							FilterCriteria criteria = new FilterCriteria();
							criteria.setFilterEqualities(filterEqualities);
							FilterResult previewTasks = client.getTasks(criteria, 10, 0);
							
							if (previewTasks.getTotalMatches() > 0) {
								//Check that the status is set to pass
								List<AttributeMap> previewTaskList = previewTasks.getMatches();
								for (int i = 0; i < previewTasks.getTotalMatches(); i++) {
									AttributeMap previewTask = previewTaskList.get(i);
									TaskState taskState = previewTask.getAttribute(Attribute.TASK_STATE);
									
									//TODO: Confirm that a task state of Finished equates to preview passed
									if (taskState.equals(TaskState.FINISHED)) {
										requiresSegTask = true;
									}
								}
							}
							
							if (requiresSegTask) {
								//Create new segmentation task for this asset
								long taskID = taskController.createTask(assetID, MayamAssetType.fromString(assetType), MayamTaskListType.SEGMENTATION);
								AttributeMap newTask = client.getTask(taskID);
								newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
								client.updateTask(newTask);
							}
						}
					}
				}
			}
		};
	}
}
