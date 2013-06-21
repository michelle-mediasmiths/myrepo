package com.mediasmiths.mq.handlers.fixstitch;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.Job.JobStatus;
import com.mayam.wf.attributes.shared.type.Job.JobType;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.RevisionUtil;
import com.mediasmiths.mq.handlers.JobHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;

public class ConformJobHandler extends JobHandler
{

	private final static Logger log = Logger.getLogger(ConformJobHandler.class);
	
	@Inject
	@Named("purge.content.type.change.days.editclips")
	private int editClipsPurgeTime;
	
	@Inject
	@Named("purge.content.type.change.days.associated")
	private int associatedPurgeTime;
	
	@Inject
	@Named("purge.content.type.change.days.publicity")
	private int publicityPurgeTime;
	
	@Inject
	@Named("purge.presentation.flag.removed.days.default")
	private int defaultPurgeTime;
	
	@Override
	public void process(Job jobMessage)
	{
		boolean isConform = jobMessage.getJobType().equals(JobType.CONFORM);
		boolean isFinished = jobMessage.getJobStatus().equals(JobStatus.FINISHED);

		String assetID = jobMessage.getAssetId();

		if (StringUtils.isEmpty(assetID))
		{
			log.error("Asset id is null or empty in CONFORM job message, I can't do anything");
			return;
		}

		if (isConform && isFinished)
		{

			log.info(String.format("A conform job has finished for assetID %s", assetID));

			AttributeMap item = null;
			String materialID = null;

			try
			{
				item = materialController.getMaterialByAssetId(assetID);
				materialID = item.getAttributeAsString(Attribute.HOUSE_ID);
				log.info(String.format("Found item with house id %s", materialID));
				
				if(materialID==null){
					log.info("HOUSE_ID was null, falling back to ASSET_SITE_ID");
					materialID = item.getAttributeAsString(Attribute.ASSET_SITE_ID);
					log.info(String.format("Found item with site id %s", materialID));	
				}
				
			
			}
			catch (MayamClientException mce)
			{
				log.warn("Failed to find item with asset id " + assetID, mce);
				return;
			}

			List<AttributeMap> allRevisions = null;
			try
			{
				allRevisions = RevisionUtil.getAllRevisionsForItem(assetID, tasksClient);
			}
			catch (RemoteException e)
			{
				log.error("Error fetching revisions for item, will not try to reassociate tx packages or remove any revisions", e);
				return;
			}

			if (allRevisions == null)
			{
				log.error("No revisions found for asset that has just had a conform finished against it, this doesnt seem right");
				return;
			}

			int numRevisions = allRevisions.size();

			log.info(String.format("Found %d revisions for asset %s (%s)", numRevisions, materialID, assetID));

			if (numRevisions == 0)
			{
				closeFixAndStitch(materialID);
				log.error("No revisions found for asset that has just had a conform finished against it, this doesn't seem right");
			}
			else if (numRevisions == 1)
			{
				// If this is the first revision and not a compliance item then create
				// purge candidate list for associated and edit clips
				if (item != null)
				{
					AssetType assetType = item.getAttribute(Attribute.ASSET_TYPE);
					String contentType = item.getAttribute(Attribute.CONT_MAT_TYPE);
					String houseID = item.getAttributeAsString(Attribute.HOUSE_ID);
					String siteID =  item.getAttributeAsString(Attribute.ASSET_SITE_ID);
					String sourceHouseID = item.getAttributeAsString(Attribute.SOURCE_HOUSE_ID);
					String previewStatus = item.getAttributeAsString(Attribute.QC_PREVIEW_STATUS);
					
					if (sourceHouseID == null) //item is not a compliance item
					{
						log.info("asset is not a compliance item, checking content type to see if Purge Candidate required");
						if (contentType.equals(MayamContentTypes.EPK.toString()) || contentType.equals(MayamContentTypes.EDIT_CLIPS.toString()) || contentType.equals(MayamContentTypes.PUBLICITY.toString()))
						{
							log.info("Content Type is :" + contentType + ", attempting to create new Purge Candidate task");
							int numberOfDays = defaultPurgeTime;

							if (contentType.equals(MayamContentTypes.EPK.toString())) 
							{
								log.info("Attempting to set purge time for associated content of " + associatedPurgeTime);
								numberOfDays = associatedPurgeTime;
							}
							else if (contentType.equals(MayamContentTypes.EDIT_CLIPS.toString())) 
							{
								log.info("Attempting to set purge time for edit clips content of " + associatedPurgeTime);
								numberOfDays = editClipsPurgeTime;
							}
							else if (contentType.equals(MayamContentTypes.PUBLICITY.toString())) 
							{
								log.info("Attempting to set purge time for publicity content of " + publicityPurgeTime);
								numberOfDays = publicityPurgeTime;
							}
						
							try
							{
								log.debug(String.format("Asset Type: %s houseID %s siteID %s numberOfDays %d",assetType,houseID,siteID,numberOfDays));
								taskController.createOrUpdatePurgeCandidateTaskForAsset(MayamAssetType.fromAssetType(assetType), siteID, numberOfDays);
								log.info("New Purge Candidate Task created");
							}						
							catch (Exception e)
							{
								log.error("Error creating Purge Candidate task for new conform", e);
							}
						}
						else {
							log.info("Content not of type edit clips, publicity or associated, no purge candidate task required. Content Type = " + contentType);
						}
					}

					else {
						log.info("asset is a compliance item, no purge candidate task required.");
					}
					//Initiates a QC task if the content is Edit_Clip.
					if (sourceHouseID == null && contentType.equals(MayamContentTypes.EDIT_CLIPS.toString()))
					{
						try
						{
							log.info("Creating QC task for Edit Clips content");
							if (siteID == null)
							{
								log.warn("SiteID is null- cannot create a QC task for the edit clip content");
							}
							else
							{
							taskController.createQCTaskForMaterial(siteID,previewStatus,item);
							}
							log.info("QC task been created");
						}
						catch (MayamClientException e)
						{
							log.error("Error creating QC task for Edit Clips content", e);
						}
					}
				}

				closeFixAndStitch(materialID);
				log.info("Only one revision for this asset, I don't need to mark any old ones for deletion");				
			}
			else
			{
				log.info("Item has multiple revisions");

				AttributeMap highestRevision = RevisionUtil.findHighestRevision(allRevisions);
				int highestRevisionNumber = ((Integer) highestRevision.getAttribute(Attribute.REVISION_NUMBER)).intValue();
				String highestRevisionID = highestRevision.getAttribute(Attribute.ASSET_ID);

				log.info(String.format("Highest revision number is %d", highestRevisionNumber));

				List<AttributeMap> allRevisionsExceptHighest = RevisionUtil.findAllButHighestRevision(allRevisions);
				log.info(String.format(
						"Going to mark %d revisions for deletion and associate any segmentlists with the latest revision",
						allRevisionsExceptHighest.size()));

				SegmentListList allSegments = null;
				try
				{
					allSegments = tasksClient.segmentApi().getSegmentListsForAsset(AssetType.ITEM, assetID);
				}
				catch (RemoteException e)
				{
					log.error("Exception getting segment lists for item", e);
				}

				boolean segmentReassociationErrors = reassociateSegments(highestRevisionID, allSegments);

				if (!segmentReassociationErrors)
				{
					deleteOldRevisions(allRevisionsExceptHighest);
					closeFixAndStitch(materialID);
				}
				else
				{
					log.warn("There was at least one error reasociating segmentlists after a conform completed, did not mark old revisions for deletion");
					try
					{
						taskController.createWFEErrorTaskBySiteID(MayamAssetType.MATERIAL,
						                                          materialID,
						                                          "There was at least one error reasociating segmentlists after a conform completed, did not mark old revisions for deletion");
					}
					catch (MayamClientException e)
					{
						// error creating error task, abandon all hope
						log.error("error creating error task!", e);
					}
				}
			}

		}
	}
	
	private void closeFixAndStitch(String materialID)
	{
		// Retrieve and close the Fix and Stitch task
		try {
			List<AttributeMap> fixAndStitchTasks = taskController.getOpenTasksForAsset(MayamTaskListType.FIX_STITCH_EDIT, Attribute.HOUSE_ID, materialID);
	
			if (fixAndStitchTasks != null && fixAndStitchTasks.size() > 0)
			{
				for (AttributeMap task: fixAndStitchTasks)
				{
					TaskState state = task.getAttribute(Attribute.TASK_STATE);
					if (state != null && (state.equals(TaskState.SYS_WAIT) || state.equals(TaskState.ACTIVE) || state.equals(TaskState.OPEN)))
					{
						AttributeMap updateMap = taskController.updateMapForTask(task);
						updateMap.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
						taskController.saveTask(updateMap);
					}
				}
			}
		} catch (MayamClientException e) {
			log.error("error closing fix and stitch task for task : " + materialID, e);
			e.printStackTrace();
		}
	}

	private void deleteOldRevisions(List<AttributeMap> allRevisionsExceptHighest)
	{
		for (AttributeMap attributeMap : allRevisionsExceptHighest)
		{
			String revisionId = attributeMap.getAttributeAsString(Attribute.ASSET_ID);

			try
			{
				log.info(String.format("About to try deleting revision %s", revisionId));
				tasksClient.assetApi().deleteAsset(AssetType.REVISION, revisionId);
			}
			catch (RemoteException e)
			{
				log.error("error deleting revision", e);
			}

		}
	}

	private boolean reassociateSegments(String highestRevisionID, SegmentListList allSegments)
	{
		boolean segmentReassociationErrors = false;

		if (allSegments != null)
		{
			log.info(String.format("found %d segmentLists for item", allSegments.size()));

			for (SegmentList segmentList : allSegments)
			{
				if (!highestRevisionID.equals(segmentList.getAttributeMap().getAttribute(Attribute.ASSET_PARENT_ID)))
				{
					try
					{
						reassociateSegmentToHighestRevision(highestRevisionID, segmentList);
					}
					catch (RemoteException e)
					{
						log.error("error reasociating segment to highest revision", e);
						if(!segmentReassociationErrors)
						{
							segmentReassociationErrors = true;
						}
					}
				}
			}
		}
		return segmentReassociationErrors; // returns true if there were any errors reasociating segments
	}

	private void reassociateSegmentToHighestRevision(String highestRevisionID, SegmentList segmentList) throws RemoteException
	{
		log.info(String.format("going to try to reassociate segment %s with revision %s", segmentList.getId(), highestRevisionID));

		// copy the segment list, create it against the latest revision with -PLACEHOLDER on the end of its packageid
		// delete the original that is on the old revision
		// update the house id on the latest revision so it no longer has -PLACEHOLDER on the end

		// copy the segment list

		SegmentList copy = (SegmentList) segmentList.copy();

		// I dont know if these are even present in the attribute map after copying but we certainly dont want to be reusing them
		copy.getAttributeMap().removeAttribute(Attribute.ASSET_ID);
		copy.getAttributeMap().removeAttribute(Attribute.ASSET_PARENT_ID);
		copy.setId(null);

		// append -PACEHOLDER to the package id
		String packageID = copy.getAttributeMap().getAttributeAsString(Attribute.HOUSE_ID);
		copy.getAttributeMap().setAttribute(Attribute.HOUSE_ID, String.format("%s-MOVED", packageID));

		SegmentList reasociatedSegment = null;

		try
		{
			reasociatedSegment = tasksClient.segmentApi().createSegmentList(AssetType.REVISION, highestRevisionID, copy);

		}
		catch (RemoteException e)
		{
			log.error(String.format("error associating package %s with highest revisions", packageID), e);
			throw e;
		}

		// delete the old segment
		try
		{
			tasksClient.segmentApi().deleteSegmentList(segmentList.getId());
		}
		catch (RemoteException e)
		{
			log.error("Error deleting original segment after reasociating", e);
			throw e;

		}

		// fix the package id on the reasociated segment
		reasociatedSegment.getAttributeMap().setAttribute(Attribute.HOUSE_ID, packageID);
		try
		{
			tasksClient.assetApi().updateAsset(reasociatedSegment.getAttributeMap());
		}
		catch (RemoteException e)
		{
			log.error("error setting house id on reasociated segment", e);
			throw e;
		}
	}

	@Override
	public String getName()
	{
		return "Conform Job";
	}

}
