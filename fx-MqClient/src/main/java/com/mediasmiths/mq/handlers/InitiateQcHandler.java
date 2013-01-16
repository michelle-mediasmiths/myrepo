package com.mediasmiths.mq.handlers;


import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;

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
				try
				{
					materialController.verifyFileMaterialFileFormat(messageAttributes);
				}
				catch (MayamClientException e)
				{
					log.error("MayamClient exception while performing file format verification for asset" + messageAttributes.getAttributeAsString(Attribute.ASSET_ID),e);
					messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
					try
					{
						taskController.saveTask(messageAttributes);
					}
					catch (MayamClientException e1)
					{
						log.error("error setting task to error state", e1);
					}
				}				
			}
		}
	}
	
	@Override
	public String getName()
	{
		return "Initiate QC";
	}
}
