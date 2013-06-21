package com.mediasmiths.mq.handlers.purge;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.FilterResult;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskUpdateHandler;

public class PurgeCandidateExtendHandler extends TaskUpdateHandler
{
	private final static Logger log = Logger.getLogger(PurgeCandidateExtendHandler.class);
	
	@Override
	public String getName()
	{
		return "Purge Candidate Extend";
	}

	private final static long FOURTEEN_DAYS = 1000l * 3600 * 24 * 14;
	
	@Override
	protected void onTaskUpdate(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		TaskState taskState = currentAttributes.getAttribute(Attribute.TASK_STATE);
		
		try {			
			if (attributeChanged(Attribute.TASK_STATE, before, after,currentAttributes) && taskState != null && taskState.equals(TaskState.EXTENDED))
			{
				//extends date by fourteen days
				AttributeMap updateMap = taskController.updateMapForTask(currentAttributes);
				
				Date currentValue = (Date) currentAttributes.getAttribute(Attribute.OP_DATE);
				
				if(currentValue==null){
					log.info("current expiration date is null, adding fourteen days to current time");
					currentValue = new Date();
				}
				
				long currentValueMillis = currentValue.getTime();
				long newValueMillis = currentValueMillis + FOURTEEN_DAYS;
				
				Date newValue = new Date(newValueMillis);
				updateMap.setAttribute(Attribute.OP_DATE, newValue);
				updateMap.setAttribute(Attribute.TASK_STATE, TaskState.PENDING);
				taskController.saveTask(updateMap);			
			}
		}
		catch (Exception e) {
			log.error("Exception in the Mayam client while handling Extend Purge Candidate Message : "+e.getMessage(), e);
		}
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.PURGE_CANDIDATE_LIST;
	}
}
