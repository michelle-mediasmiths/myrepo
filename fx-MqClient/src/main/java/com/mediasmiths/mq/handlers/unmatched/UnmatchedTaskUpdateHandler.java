package com.mediasmiths.mq.handlers.unmatched;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.FilterResult;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskUpdateHandler;

public class UnmatchedTaskUpdateHandler extends TaskUpdateHandler
{
	private final static Logger log = Logger.getLogger(UnmatchedTaskUpdateHandler.class);
	
	@Override
	public String getName()
	{
		return "Unmatch Task Update";
	}

	@Override
	protected void onTaskUpdate(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		// Title ID of temporary material updated - add to source ids of title, remove material from any purge lists
			AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);
			String assetID = currentAttributes.getAttribute(Attribute.HOUSE_ID);
			
			
			try {			
				if (assetType.equals(MayamAssetType.MATERIAL.getAssetType()) && attributeChanged(Attribute.ASSET_PEER_ID, before, after,currentAttributes)) 
				{
					//Attempt to find Aggregator
					String title = currentAttributes.getAttribute(Attribute.SERIES_TITLE);
					
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
								currentAttributes.setAttribute(Attribute.AGGREGATOR, aggregator);
								tasksClient.assetApi().updateAsset(currentAttributes);
								log.info("Aggregator " + aggregator + " added for unmatched asset " + assetID);
								break;
							}
						}
					}
					else {
						log.warn("Failed to locate an Aggregator for unmatched asset " + assetID);
					}
					
					//Remove from any task lists
					AttributeMap filterEqualities = tasksClient.createAttributeMap();
					//filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PURGE_CANDIDATE_LIST.toString());
					filterEqualities.setAttribute(Attribute.HOUSE_ID, assetID);
					FilterCriteria criteria = new FilterCriteria();
					criteria.setFilterEqualities(filterEqualities);
					FilterResult existingTasks = tasksClient.taskApi().getTasks(criteria, 50, 0);
					
					if (existingTasks.getTotalMatches() > 0) 
					{
						List<AttributeMap> tasks = existingTasks.getMatches();
						for (int i = 0; i < existingTasks.getTotalMatches(); i++) 
						{
							AttributeMap task = tasks.get(i);
							task.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
							taskController.saveTask(task);
						}
					}
					
					// Move media
					tasksClient.assetApi().moveMediaEssence(MayamAssetType.MATERIAL.getAssetType(), currentAttributes.getAttribute(Attribute.ASSET_ID).toString(), MayamAssetType.MATERIAL.getAssetType(), currentAttributes.getAttribute(Attribute.ASSET_PEER_ID).toString());
					
					//close unmatched task
					closeTask(currentAttributes);
					
					//Create QC task
					AttributeMap matchedAsset = tasksClient.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), currentAttributes.getAttribute(Attribute.ASSET_PEER_ID).toString());
					taskController.createQCTaskForMaterial(matchedAsset.getAttributeAsString(Attribute.HOUSE_ID), (Date) matchedAsset.getAttribute(Attribute.COMPLETE_BY_DATE),matchedAsset.getAttributeAsString(Attribute.QC_PREVIEW_RESULT));
				}
			}
			catch (Exception e) {
				log.error("Exception in the Mayam client while handling unmatched task update Message : "+e.getMessage(), e);
			}
	
	}
	
	protected void closeTask(AttributeMap messageAttributes)
	{
		try
		{
			messageAttributes.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
			taskController.saveTask(messageAttributes);
		}
		catch (Exception e)
		{
			log.error("Exception removing task "+getTaskType(), e);
		}
	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.UNMATCHED_MEDIA;
	}
}
