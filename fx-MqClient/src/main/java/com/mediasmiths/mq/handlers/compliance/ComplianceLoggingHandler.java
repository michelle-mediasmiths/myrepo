package com.mediasmiths.mq.handlers.compliance;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class ComplianceLoggingHandler extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(ComplianceLoggingHandler.class);

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		try {					
			String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
			AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
			String notes = messageAttributes.getAttribute(Attribute.COMPLIANCE_NOTES);
			createComplianceEditTask(assetID, assetType, notes);
						
		} catch (Exception e) {
			log.error("Exception in the Mayam client while handling Compliance logging Task Message : " + e,e);
		}
	}
	
	private void createComplianceEditTask(String assetID, AssetType assetType, String complianceNotes) throws MayamClientException, RemoteException
	{
		
		AttributeMap map = tasksClient.createAttributeMap();
		map.setAttribute(Attribute.COMPLIANCE_NOTES, complianceNotes);
		taskController.createTask(assetID, MayamAssetType.fromString(assetType.toString()), MayamTaskListType.COMPLIANCE_EDIT,map);
	}

	@Override
	public String getName()
	{
		return "Compliance Logging";
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.COMPLIANCE_LOGGING;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.FINISHED;
	}
}
