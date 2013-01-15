package com.mediasmiths.mq.handlers;


import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

public class InitiateQcHandler  extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(InitiateQcHandler.class);
	
	public void process(AttributeMap messageAttributes)
	{
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(MayamTaskListType.QC_VIEW.getText())) 
		{
			TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
			if (taskState == TaskState.OPEN) 
			{
				//task just created, lets perform file format verification
				materialController.verifyFileMaterialFileFormat(messageAttributes);				
			}
		}
	}
	
	private void channelCondidition(AttributeMap messageAttributes)
	{
		log.info("channel condition monitoring not performed");
	}


	@Override
	public String getName()
	{
		return "Initiate QC";
	}
}
