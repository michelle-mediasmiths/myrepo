package com.mediasmiths.mq.handlers.asset;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mq.handlers.AttributeHandler;

/***
 * When an asset is created through DART a title id may be specified, if so this will be included in the AUX_VAL attribute in the asset create message if this is present attempt to associate the asst
 * to the specified title
 * 
 */
public class DartRecordingTitleAssociationHandler extends AttributeHandler
{

	private final static Logger log = Logger.getLogger(DartRecordingTitleAssociationHandler.class);

	@Override
	public void process(AttributeMap messageAttributes)
	{
		AssetType assetType = messageAttributes.getAttribute(Attribute.ASSET_TYPE);

		if (!assetType.equals(MayamAssetType.MATERIAL.getAssetType()))
		{
			return; // only interested in materials\items
		}

		String titleID = messageAttributes.getAttribute(Attribute.AUX_VAL);
		String assetID = messageAttributes.getAttribute(Attribute.ASSET_ID);

		if (titleID == null) // not interested in assets that do not have AUX_VAL populated
		{
			return;
		}
		else
		{
			log.info(String.format(
					"AUX_VAL populated with {%s}, treating as user specified title id that this asset should be associated to",
					titleID));

			try
			{
				AttributeMap title = titlecontroller.getTitle(titleID);

				if (title == null)
				{
					log.warn("Title not found");
					createErrorTask(String.format("Could not locate title id {%s}", titleID), messageAttributes);
					return;
				}

				String titleAssetID = title.getAttribute(Attribute.ASSET_ID);
				AttributeMap updateMap = taskController.updateMapForAsset(messageAttributes);
				updateMap.setAttribute(Attribute.ASSET_PARENT_ID, titleAssetID);
				
				log.debug(String.format("Setting ASSET_PARENT_ID to %s on asset %s", titleAssetID, assetID));
				
				try
				{
					tasksClient.assetApi().updateAsset(updateMap);
					log.debug("update complete");
				}
				catch (RemoteException e)
				{
					String errorMessage = String.format(
							"Error setting parent of asset %s to %s",
							messageAttributes.getAttributeAsString(Attribute.ASSET_ID),
							titleAssetID);
					log.error(errorMessage, e);
					createErrorTask(errorMessage, messageAttributes);
					return;
				}

			}
			catch (MayamClientException e)
			{
				log.error("title find failed: ", e);
				createErrorTask(String.format("Could not locate title id {%s}", titleID), messageAttributes);
				return;
			}

		}

	}

	private void createErrorTask(String error, AttributeMap messageAttributes)
	{
		try
		{
			taskController.createWFEErorTask(
					MayamAssetType.MATERIAL,
					messageAttributes.getAttributeAsString(Attribute.HOUSE_ID),
					error);
		}
		catch (Exception e1)
		{
			log.error("error creating error task!", e1);
		}
	}

	@Override
	public String getName()
	{
		return "Dart Recording Title Association";
	}

}