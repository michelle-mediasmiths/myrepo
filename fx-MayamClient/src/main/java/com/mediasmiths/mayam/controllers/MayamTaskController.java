package com.mediasmiths.mayam.controllers;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.MqClient;

public class MayamTaskController {
	private final TasksClient client;
	private final MqClient mq;
	
	public MayamTaskController(TasksClient mayamClient, MqClient mqClient) {
		client = mayamClient;
		mq = mqClient;
	}
	
	public MayamClientErrorCode createTask(String assetID, MayamAssetType assetType, MayamTaskListType taskList)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;
		
		AttributeMap assetAttributes = null;
		try {
			assetAttributes = client.getAsset(assetTypeMapper(assetType), assetID);
		} catch (RemoteException e) {
			returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
		}
		
		if (assetAttributes != null) 
		{
			//TODO: Generate Task ID
			//long taskID = 0;
			//attributesValid = attributesValid && attributes.setAttribute(Attribute.TASK_ID, taskID);
			
			attributesValid = attributesValid && attributes.setAttribute(Attribute.TASK_LIST_ID, taskList.toString());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
			
			attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_ID, taskList.toString());
			attributes.copyAttributes(assetAttributes);
			
			try {
				client.createTask(attributes.getAttributes());
			} catch (RemoteException e) {
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}
			
		}
		return returnCode;
	}
	
	public MayamClientErrorCode updateTaskState(long taskID )
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS; 
		AttributeMap task = null;
		try {
			task = client.getTask(taskID);
		} catch (RemoteException e) {
			returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
		}
		
		return returnCode;
	}
	
	private AssetType assetTypeMapper(MayamAssetType assetType)
	{
		AssetType returnType = null;
		if (assetType == MayamAssetType.TITLE) {
			returnType = AssetType.SER;
		}
		else if (assetType == MayamAssetType.MATERIAL) {
			returnType = AssetType.ITEM;
		}
		else if (assetType == MayamAssetType.PACKAGE) {
			returnType = AssetType.PACK;
		}
		return returnType;
	}
}
