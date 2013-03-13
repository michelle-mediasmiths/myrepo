package com.mediasmiths.mq.handlers.fixstitch;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.Job.JobStatus;
import com.mayam.wf.attributes.shared.type.Job.JobType;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.util.RevisionUtil;
import com.mediasmiths.mq.handlers.JobHandler;

public class ConformJobHandler extends JobHandler
{

	private final static Logger log = Logger.getLogger(ConformJobHandler.class);

	@Override
	public void process(Job jobMessage)
	{
		boolean isConform = jobMessage.getJobType().equals(JobType.CONFORM);
		boolean isFinished = jobMessage.getJobStatus().equals(JobStatus.FINISHED);

		String assetID = jobMessage.getAssetId();

		if (StringUtils.isEmpty(assetID))
		{
			log.error("asset id is null or empty in CONFORM job message, I cant do anything");
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
				log.error("error fetching revisions for item, will not try to reassociate tx packages or remove any revisions", e);
				return;
			}

			if (allRevisions == null)
			{
				log.error("no revisions found for asset that has just had a conform finished against it, this doesnt seem right");
				return;
			}

			int numRevisions = allRevisions.size();

			log.info(String.format("Found %d revisions for asset %s (%s)", numRevisions, materialID, assetID));

			if (numRevisions == 0)
			{
				log.error("no revisions found for asset that has just had a conform finished against it, this doesnt seem right");
			}
			else if (numRevisions == 1)
			{
				log.info("Only one revision for this asset, I dont need to mark any old ones for deletion");				
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

				boolean segmentReasociationErrors = reasociateSegments(highestRevisionID, allSegments);

				if (!segmentReasociationErrors)
				{
					deleteOldRevisions(allRevisionsExceptHighest);
				}
				else
				{
					log.warn("There was at least one error reasociating segmentlists after a conform completed, did not mark old revisions for deletion");
					try
					{
						taskController.createWFEErorTask(
								MayamAssetType.MATERIAL,
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

	private boolean reasociateSegments(String highestRevisionID, SegmentListList allSegments)
	{
		boolean segmentReasociationErrors = false;

		if (allSegments != null)
		{
			log.info(String.format("found %d segmentLists for item", allSegments.size()));

			for (SegmentList segmentList : allSegments)
			{
				if (!highestRevisionID.equals(segmentList.getAttributeMap().getAttribute(Attribute.ASSET_PARENT_ID)))
				{
					try
					{
						reasociateSementToHighestRevision(highestRevisionID, segmentList);
					}
					catch (RemoteException e)
					{
						log.error("error reasociating segment to highest revision", e);
						segmentReasociationErrors = true;
					}
				}
			}
		}
		return segmentReasociationErrors; // returns true if there were any errors reasociating segments
	}

	private void reasociateSementToHighestRevision(String highestRevisionID, SegmentList segmentList) throws RemoteException
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
