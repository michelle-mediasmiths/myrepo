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

			Boolean currentPresentationFlag = currentAttributes.getAttribute(Attribute.PRESENTATION_FLAG);

			if (currentPresentationFlag == null)
			{
				log.warn("Presentation flag is null");
			}
			else if (currentPresentationFlag)
			{
				log.debug("Presentation set to true/yes...doing nothing");
			}
			else
			{
				log.info("Presentation flag set to false");

				Boolean previousPresentationFlag = before.getAttribute(Attribute.PRESENTATION_FLAG);

				if (previousPresentationFlag == null)
				{
					log.info("Presentation flag was previously null, will not consider this a change");
					return;
				}

				int numberOfDays = defaultPurgeTime;
				String contentType = currentAttributes.getAttribute(Attribute.CONT_MAT_TYPE);

				if (contentType != null)
				{
					if (contentType.equals(MayamContentTypes.EPK))
					{
						log.debug("Associated content");
						numberOfDays = associatedPurgeTime;
					}
					else if (contentType.equals(MayamContentTypes.EDIT_CLIPS))
					{
						log.debug("Edit clips");
						numberOfDays = editClipsPurgeTime;
					}
					else if (contentType.equals(MayamContentTypes.PUBLICITY))
					{
						log.debug("Publicity");
						numberOfDays = publicityPurgeTime;
					}
					else
					{
						log.debug("Content not one of associated, edit clips or publicity, using default purge time");
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
				else
				{
					log.info("Content type was null, no action performed");
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
