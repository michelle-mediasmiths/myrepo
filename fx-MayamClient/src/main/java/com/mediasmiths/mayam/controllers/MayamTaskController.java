package com.mediasmiths.mayam.controllers;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamTaskController {
	private final TasksClient client;
	private final Logger log = Logger.getLogger(MayamPackageController.class);
	
	@Inject
	public MayamTaskController(@Named(SETUP_TASKS_CLIENT)TasksClient mayamClient) {
		client = mayamClient;
	}
	
	public long createTask(String assetID, MayamAssetType assetType, MayamTaskListType taskList) throws MayamClientException
	{
		long taskID = 0;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;
		
		AttributeMap assetAttributes = null;
		try {
			assetAttributes = client.getAsset(AssetType.valueOf(assetType.toString()), assetID);
		} catch (RemoteException e) {
			throw new MayamClientException(MayamClientErrorCode.MAYAM_EXCEPTION);
		}
		
		if (assetAttributes != null) 
		{
			//TODO: Generate Task ID
			//taskID = 0;
			//attributesValid = attributesValid && attributes.setAttribute(Attribute.TASK_ID, taskID);
			
			attributesValid = attributesValid && attributes.setAttribute(Attribute.TASK_LIST_ID, taskList.toString());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
			
			attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_ID, taskList.toString());
			attributes.copyAttributes(assetAttributes);
			
			try {
				client.createTask(attributes.getAttributes());
			} catch (RemoteException e) {
				throw new MayamClientException(MayamClientErrorCode.MAYAM_EXCEPTION);
			}
			
		}
		else {
			throw new MayamClientException(MayamClientErrorCode.ASSET_FIND_FAILED);	
		}
		return taskID;
	}
	
	public MayamClientErrorCode deleteTask(long taskID ) throws MayamClientException
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		try {
			client.deleteTask(taskID);
		} catch (RemoteException e) {
			log.error("Error deleting task : "+ taskID);
			returnCode = MayamClientErrorCode.TASK_DELETE_FAILED;
		}
		return returnCode;
	}

}
