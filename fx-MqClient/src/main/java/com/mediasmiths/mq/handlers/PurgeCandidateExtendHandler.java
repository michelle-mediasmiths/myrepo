package com.mediasmiths.mq.handlers;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.FilterResult;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mayam.MayamTaskListType;

public class PurgeCandidateExtendHandler extends UpdateAttributeHandler
{
	private final static Logger log = Logger.getLogger(PurgeCandidateExtendHandler.class);
	
	public void process(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{	
		// Title ID of temporary material updated - add to source ids of title, remove material from any purge lists
		String assetID = currentAttributes.getAttribute(Attribute.HOUSE_ID);
		TaskState taskState = currentAttributes.getAttribute(Attribute.TASK_STATE);
		
		try {			
			if (attributeChanged(Attribute.TASK_STATE, before, after) && taskState != null && taskState.equals(TaskState.EXTENDED))
			{
				// - Content Type changed to “Associated” - Item added to Purge candidate if not already, expiry date set as 90 days
				// - Content Type set to "Edit Clips" - Item added to purge list if not already there and expiry set for 7 days
				String contentType = currentAttributes.getAttribute(Attribute.CONT_CATEGORY);
				if (contentType.equals(MayamContentTypes.EPK) || contentType.equals(MayamContentTypes.EDIT_CLIPS)) 
				{
					AttributeMap filterEqualities = tasksClient.createAttributeMap();
					filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PURGE_CANDIDATE_LIST.toString());
					filterEqualities.setAttribute(Attribute.HOUSE_ID, assetID);
					FilterCriteria criteria = new FilterCriteria();
					criteria.setFilterEqualities(filterEqualities);
					FilterResult existingTasks = tasksClient.taskApi().getTasks(criteria, 10, 0);
				
					if (existingTasks.getTotalMatches() > 0) 
					{
						List<AttributeMap> tasks = existingTasks.getMatches();
						for (int i = 0; i < existingTasks.getTotalMatches(); i++) 
						{
							AttributeMap task = tasks.get(i);
							Calendar date = Calendar.getInstance();
							if (contentType.equals(MayamContentTypes.EPK)) 
							{
								date.add(Calendar.DAY_OF_MONTH, 90);
								task.setAttribute(Attribute.OP_DATE, date.getTime());
								task.setAttribute(Attribute.TASK_STATE, TaskState.PENDING);
							}
							else if (contentType.equals(MayamContentTypes.EDIT_CLIPS)) 
							{
								date.add(Calendar.DAY_OF_MONTH, 7);
								task.setAttribute(Attribute.OP_DATE, date.getTime());
								task.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
							}
							taskController.saveTask(task);
						}
					}
				}
			}
		}
		catch (Exception e) {
			log.error("Exception in the Mayam client while handling Extend Purge Candidate Message : "+e.getMessage(), e);
			e.printStackTrace();	
		}
	}

	@Override
	public String getName()
	{
		return "Purge Candidate Extend";
	}
}
