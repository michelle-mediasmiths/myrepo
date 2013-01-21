package com.mediasmiths.mq.handlers;

import java.util.Date;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamTaskListType;

public class IngestCompleteHandler extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(IngestCompleteHandler.class);

	public void process(AttributeMap messageAttributes)
	{
		String taskListID = messageAttributes.getAttribute(Attribute.TASK_LIST_ID);
		if (taskListID.equals(MayamTaskListType.INGEST.getText()))
		{
			TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);
			if (taskState == TaskState.FINISHED)
			{
				//ingest task finished, open qc task
				try
				{
					String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
					Date requiredby = messageAttributes.getAttribute(Attribute.COMPLETE_BY_DATE);
					String previewStatus = messageAttributes.getAttribute(Attribute.QC_PREVIEW_RESULT);
					
					taskController.createQCTaskForMaterial(assetID, requiredby, previewStatus);
				}
				catch (Exception e)
				{
					log.error("Exception in the Mayam client when creating qc task: ", e);
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
