package com.mediasmiths.mq.handlers.unmatched;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mq.handlers.AttributeHandler;
import org.apache.log4j.Logger;

public class UnmatchedAssetCreateHandler extends AttributeHandler
{
	private final static Logger log = Logger.getLogger(UnmatchedAssetCreateHandler.class);

	@Inject
	@Named("purge.unmatch.material.days")
	private int purgeTimeForUmatchedMaterial;
	
	public void process(AttributeMap messageAttributes)
	{	
		// create ingest task for MAM-79
		String contentMaterialType = messageAttributes.getAttribute(Attribute.CONT_MAT_TYPE);
		if (
			(contentMaterialType != null && contentMaterialType.equals(MayamContentTypes.UNMATCHED))
			|| (contentMaterialType != null && contentMaterialType.equals(MayamContentTypes.EDIT_CLIPS))
			|| (contentMaterialType != null && contentMaterialType.equals(MayamContentTypes.EPK))
			|| (contentMaterialType != null && contentMaterialType.equals(MayamContentTypes.PUBLICITY))
			)
		{
			String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);
			String houseID = messageAttributes.getAttribute(Attribute.HOUSE_ID);
			// create ingest task for the material	
	 		try
	 		{
	 			log.debug("create ingest task for the material " + houseID);
	 			taskController.createIngestTaskForMaterial(houseID);
	 		} 		
	 		catch (MayamClientException e)
	 		{
	 			log.error("Exception caught in creating inmgest task for assetID" + assetID, e); 		
	 		}
		}
		
		
		if((contentMaterialType != null && contentMaterialType.equals(MayamContentTypes.UNMATCHED)))
		{
			try
			{
				log.info("unmatched asset created, adding to purge candidate list");
				
				// Add to purge candidate list with expiry date of 30 days
				String houseId = messageAttributes.getAttribute(Attribute.HOUSE_ID);
				taskController.createOrUpdatePurgeCandidateTaskForAsset(MayamAssetType.MATERIAL,houseId, purgeTimeForUmatchedMaterial);
			}
			catch (Exception e)
			{
				log.error("Exception creating purge candidate task : ", e);
			}
			
			log.info("setting access rights on new unmatched asset");
			
			try
			{
				AttributeMap withNewAccessRights = accessRightsController.updateAccessRights(messageAttributes.copy());
				AttributeMap update = taskController.updateMapForAsset(withNewAccessRights);
				update.setAttribute(Attribute.ASSET_ACCESS, withNewAccessRights.getAttribute(Attribute.ASSET_ACCESS));
				tasksClient.assetApi().updateAsset(update);
			}
			catch (Exception e)
			{
				log.error("error setting assets access rights", e);
			}
		}
	}

	@Override
	public String getName()
	{
		return "Unmatched Asset Create";
	}
}
