package com.mediasmiths.mq.handlers.compliance;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;
import org.apache.log4j.Logger;

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
			String assetPeerID = messageAttributes.getAttribute(Attribute.ASSET_PEER_ID);
			createComplianceEditTask(assetID, assetType, notes, assetPeerID);
						
		} catch (Exception e) {
			log.error("Exception in the Mayam client while handling Compliance logging Task Message : " + e,e);
		}
	}
	
	private void createComplianceEditTask(String assetID, AssetType assetType, String complianceNotes, String assetPeerID) throws MayamClientException, RemoteException
	{
		
		AttributeMap map = tasksClient.createAttributeMap();
		map.setAttribute(Attribute.COMPLIANCE_NOTES, complianceNotes);
		map.setAttribute(Attribute.ASSET_PEER_ID, assetPeerID);
		taskController.createTask(assetID, MayamAssetType.fromAssetType(assetType), MayamTaskListType.COMPLIANCE_EDIT,map);
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
