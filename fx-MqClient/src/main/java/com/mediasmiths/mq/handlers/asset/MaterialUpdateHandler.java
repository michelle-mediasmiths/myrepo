package com.mediasmiths.mq.handlers.asset;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mq.handlers.UpdateAttributeHandler;
import org.apache.log4j.Logger;

import java.util.List;

public class MaterialUpdateHandler extends UpdateAttributeHandler
{
	private static final String ARCHIVE_POLICY_MANUAL = "M";
	private static final String ARCHIVE_POLICY_STANDARD = "1";
	private static final String ARCHIVE_POLICY_CRITICAL = "2";
	private static final String ARCHIVE_POLICY_AO = "R";

	private final static Logger log = Logger.getLogger(MaterialUpdateHandler.class);

	//the attributes that affect what the archive flag for a given asset should be
	private final static Attribute[] ARCHIVE_FLAG_CHANGE_TRIGGERS = new Attribute[] {Attribute.PURGE_PROTECTED,Attribute.CONT_RESTRICTED_MATERIAL, Attribute.QC_PREVIEW_RESULT, Attribute.CONT_CLASSIFICATION};


	/**
	 * The policy
	 AO material shouldn't be set to R until passing preview (I think that's implied but just wanted to double check?)
	 My understanding is that Ardome defaults all items to a manual policy.

	 Once preview pass has been set then it uses the rules above to check which archive policy it should use -
	 if asset is marked as AO, use R
	 if asset is marked as being protected (at time of preview pass), then use critical (2)
	 else use standard (1)
	 AO assets should not be marked as protected, but if they are then it should still use the AO policy.
	 Once the item has been archived, then it is pointless updating the archive policy as it does not get reflected in TSM (Woody can you confirm?)..
	 i.e. If someone decides that an item should be protected after preview pass, and updates the item (either from the BMS or on the item page),
	 then even if the archive policy is changed on the AGL, the asset will not be moved into the 'critical' TSM pool.
	 * @param currentAttributes
	 * @param before
	 * @param after
	 */
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
				boolean isProtected = AssetProperties.isPurgeProtected(currentAttributes);
				boolean isAO = AssetProperties.isAO(currentAttributes);
				boolean isPreviewPass = MayamPreviewResults.isPreviewPass((String) currentAttributes.getAttribute(Attribute.QC_PREVIEW_RESULT));
				String classification = currentAttributes.getAttribute(Attribute.CONT_CLASSIFICATION);

				if (isAO)
				{
					if (isProtected)
						log.warn("Material " + materialID + " set to AO and Protected. This should not happen.");

					if (isPreviewPass)
					{
						if (classification != null)
						{
							log.debug(String.format("Setting Archive Policy to %s", ARCHIVE_POLICY_AO));
							currentAttributes.setAttribute(Attribute.ARCHIVE_POLICY, ARCHIVE_POLICY_AO); // always set the ao policy for ao material

							// now need to set the policy for the children.
							String assetId = currentAttributes.getAttribute(Attribute.ASSET_ID);

							updateMaterialAOChildren(assetType, assetId);
						}
						else
						{
							// do nothing
						}
					}
					else
					{
                        // I assume we do nothing.
					}
				}
				else if (isPreviewPass)
				{
					if (isProtected)
					{
						log.debug(String.format("Setting Archive Policy to %s", ARCHIVE_POLICY_CRITICAL));
						currentAttributes.setAttribute(Attribute.ARCHIVE_POLICY, ARCHIVE_POLICY_CRITICAL); // set to critical for assets that are both protected and have passed preview
					}
					else
					{
						log.debug(String.format("Setting Archive Policy to %s", ARCHIVE_POLICY_STANDARD));
						currentAttributes.setAttribute(Attribute.ARCHIVE_POLICY, ARCHIVE_POLICY_STANDARD); // set to standard for assets  have passed preview
					}

				}
				else
				{
					log.debug(String.format("Setting Archive Policy to %s", ARCHIVE_POLICY_MANUAL));
					currentAttributes.setAttribute(Attribute.ARCHIVE_POLICY, ARCHIVE_POLICY_MANUAL); // set to manual if content has just been unprotected and hasn't yet passed preview
				}

				try
				{
					if (attributeChanged(Attribute.ARCHIVE_POLICY, before, after, currentAttributes)) // if the archive policy changed, save
					{
						log.info("Archive policy changed, saving");
						
						AttributeMap update = taskController.updateMapForAsset(currentAttributes);
						update.setAttribute(Attribute.ARCHIVE_POLICY, currentAttributes.getAttribute(Attribute.ARCHIVE_POLICY));
						tasksClient.assetApi().updateAsset(update);
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

	private void updateMaterialAOChildren(final AssetType assetType, final String assetID)
	{
		try
		{
			log.debug("Update the children for " + assetType + " id: " + assetID);

			List<AttributeMap> materials = tasksClient.assetApi().getAssetChildren(assetType, assetID, MayamAssetType.MATERIAL.getAssetType());

			if (materials != null && materials.size() > 0)
			{
				for (AttributeMap material : materials)
				{
                    log.debug("Update AO Policy for " + material.getAttribute(Attribute.ASSET_ID));

					material.setAttribute(Attribute.ARCHIVE_POLICY, ARCHIVE_POLICY_AO);

					tasksClient.assetApi().updateAsset(material);
				}
				log.debug("Updated the children for " + assetType + " id: " + assetID);
			}
		}
		catch (Exception e)
		{
			log.error("Unable to update child assets.");
		}
	}

	
	@Override
	public String getName()
	{
		return "Material Update";
	}
}
