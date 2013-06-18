package com.mediasmiths.mq.handlers.asset;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mq.handlers.UpdateAttributeHandler;
import org.apache.log4j.Logger;


public class PresentationFlagClearedHandler extends UpdateAttributeHandler
{
	private Logger log = Logger.getLogger(PresentationFlagClearedHandler.class);

	@Inject
	@Named("purge.presentation.flag.removed.days.default")
	private int defaultPurgeTime;

	@Inject
	@Named("purge.presentation.flag.removed.days.editclips")
	private int editClipsPurgeTime;

	@Inject
	@Named("purge.presentation.flag.removed.days.associated")
	private int associatedPurgeTime;

	@Inject
	@Named("purge.presentation.flag.removed.days.publicity")
	private int publicityPurgeTime;

	@Override
	public void process(final AttributeMap currentAttributes, final AttributeMap before, final AttributeMap after)
	{
		AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);
		String assetID = currentAttributes.getAttributeAsString(Attribute.ASSET_ID);
		String houseID = currentAttributes.getAttributeAsString(Attribute.HOUSE_ID);

		if (!MayamAssetType.MATERIAL.getAssetType().equals(assetType))
		{
			return; // only interested in materials
		}

		if (attributeChanged(Attribute.PRESENTATION_FLAG, before, after, currentAttributes))
		{
			log.info(String.format("Presentation flag changed to %s for asset %s (%s)",
			                       currentAttributes.getAttributeAsString(Attribute.PRESENTATION_FLAG),
			                       assetID,
			                       houseID));

			Boolean presentationFlag = currentAttributes.getAttribute(Attribute.PRESENTATION_FLAG);

			if (presentationFlag == null)
			{
				log.warn("Presentation flag is null");
			}
			else if (presentationFlag)
			{
				log.debug("Presentation set to true/yes...doing nothing");
			}
			else
			{
				log.info("Presentation flag set to false");

				int numberOfDays = defaultPurgeTime;
				String contentType = currentAttributes.getAttribute(Attribute.CONT_CATEGORY);

				if (contentType != null)
				{
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
					else
					{
						numberOfDays = defaultPurgeTime;
					}

					try
					{
						taskController.createOrUpdatePurgeCandidateTaskForAsset(MayamAssetType.MATERIAL, houseID,numberOfDays);

					}
					catch (MayamClientException e)
					{
						log.error("Exception thrown handling change in presentation flag for material : " + houseID, e);
					}
				}
			}
		}
	}


	@Override
	public String getName()
	{
		return "Presentation flag clear";
	}
}
