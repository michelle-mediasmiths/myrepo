package com.mediasmiths.mq.handlers.asset;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mq.handlers.UpdateAttributeHandler;
import org.apache.log4j.Logger;

public class TemporaryContentHandler extends UpdateAttributeHandler
{
	private final static Logger log = Logger.getLogger(TemporaryContentHandler.class);


	public void process(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		// Title ID of temporary material updated - add to source ids of title, remove material from any purge lists
		AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);
		String assetID = currentAttributes.getAttribute(Attribute.HOUSE_ID);

		try
		{
			if (attributeChanged(Attribute.CONT_MAT_TYPE, before, after, currentAttributes))
			{
				log.debug("CONT_MAT_TYPE changed");

				// - Content Type has changed, create or update purge candidate task for item
				String contentType = currentAttributes.getAttribute(Attribute.CONT_MAT_TYPE);
				if (contentType.equals(MayamContentTypes.EPK) ||
				    contentType.equals(MayamContentTypes.EDIT_CLIPS) ||
				    contentType.equals(MayamContentTypes.PUBLICITY))
				{

					Integer numberOfDays = null;

					if (contentType.equals(MayamContentTypes.EPK))
					{
						numberOfDays = associatedPurgeTime;
					}
					else if (contentType.equals(MayamContentTypes.EDIT_CLIPS))
					{
						numberOfDays = editClipsPurgeTime;
					}
					else if (contentType.equals(MayamContentTypes.PUBLICITY))
					{
						numberOfDays = publicityPurgeTime;
					}
					else if (contentType.equals(MayamContentTypes.UNMATCHED))
					{
						numberOfDays = unmatchedPurgeTime;
					}

					if (numberOfDays != null)
					{
						taskController.createOrUpdatePurgeCandidateTaskForAsset(MayamAssetType.fromAssetType(assetType),
						                                                        currentAttributes.getAttributeAsString(Attribute.HOUSE_ID),
						                                                        numberOfDays);
					}
				}
			}
		}
		catch (Exception e)
		{
			log.error("Exception in the Mayam client while handling Temporary Content Message : " + e.getMessage(), e);
			e.printStackTrace();
		}
	}


	@Inject
	@Named("purge.content.type.change.days.editclips")
	private int editClipsPurgeTime;

	@Inject
	@Named("purge.content.type.change.days.associated")
	private int associatedPurgeTime;

	@Inject
	@Named("purge.content.type.change.days.associated")
	private int publicityPurgeTime;

	@Inject
	@Named("purge.unmatch.material.days")
	private int unmatchedPurgeTime;


	@Override
	public String getName()
	{
		return "Temporary Content";
	}
}
