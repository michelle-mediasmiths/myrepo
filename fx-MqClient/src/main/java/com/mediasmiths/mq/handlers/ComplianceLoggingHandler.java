package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;

public class ComplianceLoggingHandler extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(ComplianceLoggingHandler.class);

	public void process(AttributeMap messageAttributes)
	{
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(MayamTaskListType.COMPLIANCE_LOGGING.getText()))
		{
			TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
			if (taskState == TaskState.FINISHED) 
			{
				try {
					
					try{
						messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
						taskController.saveTask(messageAttributes);								
					}
					catch(Exception e){
						log.error("error updating compliance logging task state to removed",e);
					}
								
					String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
					AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
					createComplianceEditTask(assetID, assetType);
								
				} catch (Exception e) {
					log.error("Exception in the Mayam client while handling Compliance logging Task Message : " + e,e);
				}
			}	
		}
	}

	private void createComplianceEditTask(String assetID, AssetType assetType) throws MayamClientException, RemoteException
	{
		taskController.createTask(assetID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.COMPLIANCE_EDIT);
	}

	@Override
	public String getName()
	{
		return "Compliance Logging";
	}
}
