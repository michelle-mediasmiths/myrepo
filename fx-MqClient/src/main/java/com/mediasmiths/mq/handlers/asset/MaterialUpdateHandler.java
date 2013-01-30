package com.mediasmiths.mq.handlers.asset;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.UpdateAttributeHandler;

public class MaterialUpdateHandler extends UpdateAttributeHandler
{
	private static final String ARCHIVE_POLICIY_MANUAL = "M";
	private static final String ARCHIVE_POLICY_STANDARD = "1";
	private static final String ARCHIVE_POLICY_CRITICAL = "2";
	private static final String ARCHIVE_POLICY_AO = "R";

	private final static Logger log = Logger.getLogger(MaterialUpdateHandler.class);

	//the attributes that affect what the archive flag for a given asset should be
	private final static Attribute[] ARCHIVE_FLAG_CHANGE_TRIGGERS = new Attribute[] {Attribute.PURGE_PROTECTED,Attribute.CONT_RESTRICTED_MATERIAL, Attribute.QC_PREVIEW_RESULT};
	
	public void process(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);
		String materialID = currentAttributes.getAttribute(Attribute.HOUSE_ID);

		if (assetType.equals(MayamAssetType.MATERIAL.getAssetType()))
		{
			boolean anyChanged = false;
			for (Attribute att : ARCHIVE_FLAG_CHANGE_TRIGGERS)
			{
				if(attributeChanged(att, before, after, currentAttributes)){
					log.debug(String.format("Attribute {%s} of MATERIAL {%s} changed", att.toString(),materialID));
					anyChanged=true;
					break;
				}
			}
			
			if (anyChanged)
			{
				
				log.debug("Attribute affecting archive policy has changed");
				boolean isProtected = AssetProperties.isProtected(currentAttributes);
				boolean isAO = AssetProperties.isAO(currentAttributes);
				boolean isPreviewPass = MayamPreviewResults.isPreviewPass((String) currentAttributes.getAttribute(Attribute.QC_PREVIEW_RESULT));

				if (isAO)
				{
					log.debug(String.format("Setting Archive Policy to %s", ARCHIVE_POLICY_AO));
					currentAttributes.setAttribute(Attribute.ARCHIVE_POLICY, ARCHIVE_POLICY_AO); // always set the ao policy for ao material
				}
				else if (isProtected && isPreviewPass)
				{
					log.debug(String.format("Setting Archive Policy to %s", ARCHIVE_POLICY_CRITICAL));
					currentAttributes.setAttribute(Attribute.ARCHIVE_POLICY, ARCHIVE_POLICY_CRITICAL); // set to critical for assets that are both protected and have passed preview
				}
				else if (isProtected || isPreviewPass)
				{
					log.debug(String.format("Setting Archive Policy to %s", ARCHIVE_POLICY_STANDARD));
					currentAttributes.setAttribute(Attribute.ARCHIVE_POLICY, ARCHIVE_POLICY_STANDARD); // set to standard for assets that are both protected or have passed preview
				}
				else
				{
					log.debug(String.format("Setting Archive Policy to %s", ARCHIVE_POLICIY_MANUAL));
					currentAttributes.setAttribute(Attribute.ARCHIVE_POLICY, ARCHIVE_POLICIY_MANUAL); // set to manual if content has just been unprotected and hasn't yet passed preview
				}

				try
				{
					if (attributeChanged(Attribute.ARCHIVE_POLICY, before, after, currentAttributes)) // if the archive policy changed, save
					{
						log.info("Archive policy changed, saving");
						tasksClient.assetApi().updateAsset(currentAttributes);
					}
					else
					{
						log.info("No change to archive policy made");
					}
				}
				catch (RemoteException e)
				{
					log.error("Exception thrown by Mayam whille updating archive policy for material : " + materialID, e);
				}
			}
		}
	}

	@Override
	public String getName()
	{
		return "Material Update";
	}
}
