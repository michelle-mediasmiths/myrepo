package com.mediasmiths.mq.handlers.ingest;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.MediaStatus;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.UpdateAttributeHandler;

/**
 * 
 * Monitors for the conditions required to consider in ingest complete. One would think it would be an 'ingest' job finishing for a given item but its not
 * 
 * According to viz we cant start qc until lowres on central is closed + hires on central is closed
 * 
 * For most ingests these things happen before and ingest job finished message is sent from ardome, but not always
 * 
 * MAM-79
 * 
 * QC not to be done until:
 * 
 * MEDST_LR = Ready MEDST_HR = Ready AND there is a storage url in the hires storage
 * 
 */
public class IngestCompleteHandler extends UpdateAttributeHandler
{

	private final static Logger log = Logger.getLogger(IngestCompleteHandler.class);

	@Override
	public void process(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);

		if (!assetType.equals(MayamAssetType.MATERIAL.getAssetType()))
		{
			return; // only interested in materials\items
		}

		String assetID = currentAttributes.getAttribute(Attribute.ASSET_ID);
		String houseID = currentAttributes.getAttribute(Attribute.HOUSE_ID);

		if (AssetProperties.hasQCStatus(currentAttributes))
		{
			log.debug("asset already has a non TBD qc status");
			return; // asset has had qc performed on it already, it cant have just been ingested
		}

		boolean readyForQCBefore = isMaterialsReadyForQC(before);

		if (readyForQCBefore)
		{
			return; // asset was already ready for qc before this attribute update, nothing more to do
		}

		boolean readyForQCAfter = isMaterialsReadyForQC(currentAttributes);

		if (!readyForQCAfter)
		{
			return; // not ready for qc after the attribute update, nothing more to do.
		}

		if ((!readyForQCBefore) && readyForQCAfter)
		{
			log.info(String.format("Asset %s (%s) was not ready for qc and now is", houseID, assetID));

			// finish any ingest tasks or other on ingest logic

		}

	}

	private boolean isMaterialsReadyForQC(AttributeMap attributes)
	{

		{
			MediaStatus lr = attributes.getAttribute(Attribute.MEDST_LR);

			log.debug("MEDST_LR : " + lr);

			if (lr == null)
			{
				log.debug("MEDST_LR is null, material is not ready for qc");
				return false;
			}
			else if (!lr.equals(MediaStatus.READY))
			{
				log.debug("MEDST_LR is not READY, material is not ready for qc");
				return false;
			}

		}

		{
			MediaStatus hr = attributes.getAttribute(Attribute.MEDST_HR);

			log.debug("MEDST_HR : " + hr);

			if (hr == null)
			{
				log.debug("MEDST_HR is null, material is not ready for qc");
				return false;
			}
			else if (!hr.equals(MediaStatus.READY))
			{
				log.debug("MEDST_HR is not READY, material is not ready for qc");
				return false;
			}
		}

		{
			String assetID = attributes.getAttribute(Attribute.ASSET_ID);

			try
			{
				//get file location, do not accept paths not on hires (preferred locations)
				String assetPath = materialController.getAssetPath(assetID, false);
				log.debug(String.format("asset is at %s material is ready for qc", assetPath));
				return true;
			}
			catch (MayamClientException e)
			{
				if (e.getErrorcode().equals(MayamClientErrorCode.FILE_NOT_IN_PREFERRED_LOCATION))
				{
					log.debug("Asset is not in preferred location (hires), not ready for qc");
					return false;
				}
				else
				{
					log.error("unexpected exception queurying assets location", e);
					return false;
				}
			}

		}

	}

	@Override
	public String getName()
	{
		return "Ingest Complete";
	}

}
