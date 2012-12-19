package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class IngestCompleteHandler  implements AttributeHandler
{
	private static final String PREVIEW_STATUS_PASS = "pass";
	MayamTaskController taskController;
	private final static Logger log = Logger.getLogger(IngestCompleteHandler.class);
	
	public IngestCompleteHandler(MayamTaskController controller) 
	{
		taskController = controller;
	}
	
	public void process(AttributeMap messageAttributes)
	{	
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(MayamTaskListType.INGEST.getText())) 
		{
			TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);	
			if (taskState == TaskState.FINISHED) 
			{
				
				QcStatus previewStatus = messageAttributes.getAttribute(Attribute.QC_PREVIEW_RESULT);
				boolean qcpass = (previewStatus != null && (previewStatus == QcStatus.PASS || previewStatus == QcStatus.PASS_MANUAL));
				
				if (!qcpass)
				{

					try
					{
						String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
						AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
						taskController.createTask(
								assetID,
								MayamAssetType.fromString(assetType.toString()),
								MayamTaskListType.QC_VIEW);
					}
					catch (Exception e)
					{
						log.error("Exception in the Mayam client while handling Ingest Complete Task Message : " + e);
						e.printStackTrace();
					}
				
				}
			}
		}
	}

	@Override
	public String getName()
	{
		return "Ingest Complete";		
	}
}
