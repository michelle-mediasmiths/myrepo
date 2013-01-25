package com.mediasmiths.mq.handlers.ingest;

import java.util.Date;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class IngestCompleteHandler extends TaskStateChangeHandler
{
	private final static Logger log = Logger.getLogger(IngestCompleteHandler.class);

	@Override
	public String getName()
	{
		return "Ingest Complete";
	}

	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		//ingest task finished, open qc task
		try
		{
			String assetID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
			Date requiredby = messageAttributes.getAttribute(Attribute.COMPLETE_BY_DATE);
			String previewStatus = messageAttributes.getAttribute(Attribute.QC_PREVIEW_RESULT);
			
			taskController.createQCTaskForMaterial(assetID, requiredby, previewStatus);
			
			closeTask(messageAttributes);
		}
		catch (Exception e)
		{
			log.error("Exception in the Mayam client when creating qc task: ", e);
		}
		
		
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.INGEST;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.FINISHED;
	}
}
