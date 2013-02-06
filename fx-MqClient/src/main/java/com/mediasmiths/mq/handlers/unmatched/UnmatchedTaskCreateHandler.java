package com.mediasmiths.mq.handlers.unmatched;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.FilterResult;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskStateChangeHandler;

public class UnmatchedTaskCreateHandler extends TaskStateChangeHandler
{

	private final static Logger log = Logger.getLogger(UnmatchedAssetCreateHandler.class);
	
	@Override
	protected void stateChanged(AttributeMap messageAttributes)
	{
		
		String houseID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
		
		//Attempt to find Aggregator
		String title = messageAttributes.getAttribute(Attribute.SERIES_TITLE);
		
		try{
		AttributeMap aggregatorEqualities = tasksClient.createAttributeMap();
		AttributeMap aggregatorSimilarities = tasksClient.createAttributeMap();
		aggregatorEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.WFE_ERROR.toString());
		aggregatorSimilarities.setAttribute(Attribute.ASSET_TITLE, title);
		FilterCriteria aggregatorCriteria = new FilterCriteria();
		aggregatorCriteria.setFilterEqualities(aggregatorEqualities);
		aggregatorCriteria.setFilterSimilarities(aggregatorSimilarities);
		FilterResult wfeTasks = tasksClient.taskApi().getTasks(aggregatorCriteria, 50, 0);
		
		if (wfeTasks.getTotalMatches() > 0) 
		{
			for (int i = 0; i < wfeTasks.getTotalMatches(); i++)
			{
				AttributeMap wfeTask = wfeTasks.getMatches().get(i);
				String assetTitle = wfeTask.getAttribute(Attribute.ASSET_TITLE);
				if (assetTitle.contains(title))
				{
					String aggregator = wfeTask.getAttribute(Attribute.AGGREGATOR);
					AttributeMap updateMap = taskController.updateMapForAsset(messageAttributes);
					updateMap.setAttribute(Attribute.AGGREGATOR, aggregator);
					tasksClient.assetApi().updateAsset(updateMap);
					log.info("Aggregator " + aggregator + " added for unmatched asset " + houseID);
					
					
					AttributeMap taskUpdateMap = taskController.updateMapForAsset(wfeTask);
					taskUpdateMap.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
					tasksClient.taskApi().updateTask(taskUpdateMap);
					break;
				}
			}
		}
		else {
			log.warn("Failed to locate an Aggregator for unmatched asset " + houseID);
		}
		}
		catch(Exception e){
			log.error("error finding aggregator for unmatched item",e);
		}

	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.UNMATCHED_MEDIA;
	}

	@Override
	public TaskState getTaskState()
	{
		return TaskState.OPEN;
	}

	@Override
	public String getName()
	{
		return "Unmatched Task Create";
	}

}
