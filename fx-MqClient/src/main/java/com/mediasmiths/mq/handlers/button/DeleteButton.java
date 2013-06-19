package com.mediasmiths.mq.handlers.button;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.foxtel.ip.common.events.ManualPurge;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamButtonType;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.log4j.Logger;

import java.util.Date;

public class DeleteButton extends ButtonClickHandler
{

	private final static Logger log = Logger.getLogger(DeleteButton.class);
	
	@Inject(optional=false)
	@Named("bms.events.namespace")
	private String bmsEventsNamespace;
	
	@Inject
	@Named("placeholderManagement.serialiser")
	private JAXBSerialiser serialiser;
	
	@Inject
	@Named("manual.delete.grace.period.seconds")
	private int gracePeriod = 86400;
	
	@Override
	protected void buttonClicked(AttributeMap messageAttributes)
	{
		String houseID = (String) messageAttributes.getAttribute(Attribute.HOUSE_ID);
		AssetType type = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
		log.info(String.format("Delete Requested for asset %s of type %s",houseID,type.toString()));
		log.info(String.format("Grace Period is %d seconds",gracePeriod));
		
		if(type==MayamAssetType.MATERIAL.getAssetType()){
			materialController.deleteMaterial(houseID, gracePeriod);
			sendManualItemPurgeEvent(messageAttributes, MayamAssetType.MATERIAL.getText());
		}
		else if(type==MayamAssetType.TITLE.getAssetType()){
			titlecontroller.purgeTitle(houseID, gracePeriod);
			sendManualItemPurgeEvent(messageAttributes, MayamAssetType.TITLE.getText());
		}
		else if(type==MayamAssetType.PACKAGE.getAssetType()){		
			packageController.deletePackage(houseID, gracePeriod);
			sendManualItemPurgeEvent(messageAttributes, MayamAssetType.PACKAGE.getText());
		}
		
	}

	private void sendManualItemPurgeEvent(AttributeMap messageAttributes, String assetType)
	{
		try
		{
			String title = messageAttributes.getAttributeAsString(Attribute.SERIES_TITLE);
			sendManualPurgeEvent((String) messageAttributes.getAttribute(Attribute.HOUSE_ID), assetType, title,(StringList)messageAttributes.getAttribute(Attribute.CHANNELS), AssetProperties.isAO(messageAttributes));
			
		}
		catch (Exception e)
		{
			log.error("error sending ManualPurge event", e);
		}
		
	}



	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.DELETE;
	}

	@Override
	public String getName()
	{
		return "Delete Button Click";
	}


	private void sendManualPurgeEvent(String houseId, String assetType, String title, StringList channels, boolean isAo)
	{
		ManualPurge manualPurge = new ManualPurge();
		manualPurge.setHouseId(houseId);
		manualPurge.setAssetType(assetType);
		manualPurge.setTime(((new Date()).toString()));
		manualPurge.setTitle(title);
		manualPurge.getChannelGroup().addAll(channelProperties.groupsForEmail(channels,isAo));		
		
		String eventName = EventNames.MANUAL_PURGE;
		String namespace = bmsEventsNamespace;

		eventsService.saveEvent(namespace, eventName,  manualPurge);
	}

}
