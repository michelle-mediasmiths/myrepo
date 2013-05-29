package com.mediasmiths.mq.handlers.asset;

import java.util.List;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.AttributeHandler;

public class MediaMoveHandler extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(MediaMoveHandler.class);

	@Override
	public String getName()
	{
		return "Media Move";
	}

	@Override
	public void process(AttributeMap messageAttributes)
	{
		// Title ID of temporary material updated - add to source ids of title, remove material from any purge lists
		AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
		
		if(assetType==null){
			log.debug("Asset Type is null");
			return;
		}
		
		final String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
		final String peerID = messageAttributes.getAttributeAsString(Attribute.ASSET_PEER_ID);

		log.debug(String.format("Media move assetID : %s peerID : %s", assetID,peerID));
		
		try
		{
			AttributeMap assetAttributes = tasksClient.assetApi().getAsset(
					assetType,
					assetID);
			
			//close any preview or qc tasks open for source item
			List<AttributeMap> previewTasks = removeTasksOfType(assetID, MayamTaskListType.PREVIEW);
			
			if (previewTasks.size() > 1)
			{
				log.warn("More than 1 preview task found for asset : " + assetID + ", " + previewTasks.size() + "tasks found.");
			}
			
			List<AttributeMap> qcTasks = removeTasksOfType(assetID, MayamTaskListType.QC_VIEW);
			
			if (qcTasks.size() > 1)
			{
				log.warn("More than 1 qc task found for asset : " + assetID + ", " + qcTasks.size() + "tasks found.");
			}
			
			//close any unmatched tasks for the source item
			removeTasksOfType(assetID, MayamTaskListType.UNMATCHED_MEDIA);
			
			//close any purge candidate tasks for the source item
			removeTasksOfType(assetID, MayamTaskListType.PURGE_CANDIDATE_LIST);
			
			//if source was programme open a new ingest task
			if (AssetProperties.isMaterialProgramme(assetAttributes))
			{
				taskController.createIngestTaskForMaterial(assetAttributes.getAttributeAsString(Attribute.HOUSE_ID));
			}
			
			//close ingest task for peer
			taskController.closeIngestTaskForAssetAsUmatched(peerID);
			
		}
		catch (Exception e)
		{
			log.error("Exception in the Mayam client while handling Media Move Message : " + e.getMessage(), e);
		}

	}

	private List<AttributeMap> removeTasksOfType(final String assetID, final MayamTaskListType type) throws MayamClientException
	{
		List<AttributeMap> tasks = taskController.getOpenTasksForAsset(type, Attribute.ASSET_ID, assetID);
		for (AttributeMap task: tasks)
		{
			AttributeMap updateMap = taskController.updateMapForTask(task);
			updateMap.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
			taskController.saveTask(task);
		}
		return tasks;
	}

}
