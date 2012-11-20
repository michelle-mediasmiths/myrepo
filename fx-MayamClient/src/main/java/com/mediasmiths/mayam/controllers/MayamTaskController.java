package com.mediasmiths.mayam.controllers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetAccess;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.AssetAccess.EntityType;
import com.mayam.wf.attributes.shared.type.FilterCriteria.SortOrder;
import com.mayam.wf.attributes.shared.type.GenericTable;
import com.mayam.wf.attributes.shared.type.GenericTable.Row;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.FilterResult;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.accessrights.MayamAccessRights;
import com.mediasmiths.mayam.accessrights.MayamAccessRightsController;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamTaskController extends MayamController{
	private final TasksClient client;
	
	@Inject
	private final MayamAccessRightsController accessRightsController;
	
	private final Logger log = Logger.getLogger(MayamPackageController.class);

	@Inject
	public MayamTaskController(
			@Named(SETUP_TASKS_CLIENT) TasksClient mayamClient, MayamAccessRightsController rightsController) {
		client = mayamClient;
		accessRightsController = rightsController;
	}

	public long createTask(String assetID, MayamAssetType assetType, MayamTaskListType taskList) throws MayamClientException {
		long taskID = 0;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;

		AttributeMap assetAttributes = null;
		try {
			assetAttributes = client.assetApi().getAsset(AssetType.valueOf(assetType.toString()), assetID);
		} catch (RemoteException e) {
			log.error("Exception thrown by Mayam while attempting to find asset with ID: " + assetID);
			throw new MayamClientException(MayamClientErrorCode.MAYAM_EXCEPTION);
		}

		if (assetAttributes != null) {
			attributesValid &= attributes.setAttribute(Attribute.TASK_LIST_ID, taskList.toString());
			attributesValid &= attributes.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);

			attributesValid &= attributes.setAttribute(Attribute.ASSET_ID, taskList.toString());
			attributes.copyAttributes(assetAttributes);

			if (!attributesValid)
			{
				log.warn("Task created but one or more attributes was invalid");
			}
			try {
				AttributeMap newTask = updateAccessRights(attributes.getAttributes());
				client.taskApi().createTask(newTask);
			} catch (RemoteException e) {
				log.error("Exception thrown by Mayam while attempting to create task");
				throw new MayamClientException(MayamClientErrorCode.MAYAM_EXCEPTION);
			}

		} else {
			log.warn("Failed to find asset with ID: " + assetID);
			throw new MayamClientException(MayamClientErrorCode.ASSET_FIND_FAILED);
		}
		return taskID;
	}

	public MayamClientErrorCode deleteTask(long taskID)
			throws MayamClientException {
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		try {
			client.taskApi().deleteTask(taskID);
		} catch (RemoteException e) {
			log.error("Error deleting task : " + taskID);
			returnCode = MayamClientErrorCode.TASK_DELETE_FAILED;
		}
		return returnCode;
	}

	public AttributeMap getTaskForAsset(MayamTaskListType type, String id) throws MayamClientException 
	{
		log.info(String.format("Searching for task of type %s for asset %s", type.getText(), id));

		final FilterCriteria criteria = client.taskApi().createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(Attribute.TASK_LIST_ID, type.getText());
		criteria.getFilterEqualities().setAttribute(Attribute.ASSET_ID, id);
		criteria.getSortOrders().add(new SortOrder(Attribute.TASK_CREATED, SortOrder.Direction.DESC));
		FilterResult result;
		try {
			result = client.taskApi().getTasks(criteria, 10, 0);
			log.info("Total matches: " + result.getTotalMatches());

			if (result.getTotalMatches() != 1) {
				log.error("unexpected number of results for search");
				throw new MayamClientException(MayamClientErrorCode.UNEXPECTED_NUMBER_OF_TASKS);
			}

			return result.getMatches().get(0);
		} catch (RemoteException e) {
			log.error("remote expcetion searching for task", e);
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED);
		}

	}

	public void saveTask(AttributeMap task) throws MayamClientException 
	{	
		try {
			task = updateAccessRights(task);
			client.taskApi().updateTask(task);
		} catch (RemoteException e) {
			log.error("remote expcetion saving task", e);
			throw new MayamClientException(MayamClientErrorCode.TASK_UPDATE_FAILED);
		}
	}
	
	public AttributeMap getTask(long taskId) throws RemoteException
	{
		return client.taskApi().getTask(taskId);
	}
	
	private AttributeMap updateAccessRights(AttributeMap task)
	{
		String taskType = task.getAttribute(Attribute.TASK_LIST_ID);
		String taskState = task.getAttribute(Attribute.TASK_STATE);
		String assetType = task.getAttribute(Attribute.ASSET_TYPE);
		boolean purgeProtected = task.getAttribute(Attribute.AUX_FLAG);
		
		GenericTable mediaRights = task.getAttribute(Attribute.MEDIA_RIGHTS);
		
		ArrayList <String> channelList = new ArrayList <String> ();
		if (mediaRights != null) {
			List<Row> rows = mediaRights.getRows();
			if (rows != null) {
				for (int i = 0; i < rows.size(); i++)
				{
					String channel = rows.get(i).get(5);
					channelList.add(channel);
				}
			}
		}
		
		List <MayamAccessRights> allRights = accessRightsController.retrieve(MayamTaskListType.fromString(taskType), TaskState.valueOf(taskState), MayamAssetType.valueOf(assetType), channelList, purgeProtected);
		
		AssetAccess accessRights = new AssetAccess();
		for (int i = 0; i < allRights.size(); i++)
		{
			AssetAccess.ControlList.Entry entry = new AssetAccess.ControlList.Entry();
			entry.setEntityType(EntityType.GROUP);
			entry.setEntity(allRights.get(i).getGroupName());
			entry.setRead(allRights.get(i).getReadAccess());
			entry.setWrite(allRights.get(i).getWriteAccess());
			entry.setAdmin(allRights.get(i).getAdminAccess());
			accessRights.getStandard().add(entry);
		}
		task.setAttribute(Attribute.ASSET_ACCESS, accessRights);
		return task;
	}
}