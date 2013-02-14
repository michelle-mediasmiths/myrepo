package com.mediasmiths.mq.handlers.button;

import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.Material;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamButtonType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

public class DeleteButton extends ButtonClickHandler
{

	private static final String MANUAL_PURGE = "ManualPurge";

	private final static Logger log = Logger.getLogger(DeleteButton.class);
	
	@Inject(optional=false)
	@Named("bms.events.namespace")
	private String bmsEventsNamespace;
	
	@Inject
	@Named("placeholderManagement.serialiser")
	private JAXBSerialiser serialiser;
	
	@Override
	protected void buttonClicked(AttributeMap messageAttributes)
	{
		String houseID = (String) messageAttributes.getAttribute(Attribute.HOUSE_ID);
		AssetType type = messageAttributes.getAttribute(Attribute.ASSET_TYPE);
		log.info(String.format("Delete Requested for asset %s of type %s",houseID,type.toString()));
		
		if(type==MayamAssetType.MATERIAL.getAssetType()){
			materialController.deleteMaterial(houseID);
			sendManualItemPurgeEvent(messageAttributes);
		}
		else if(type==MayamAssetType.TITLE.getAssetType()){
			titlecontroller.purgeTitle(houseID);			
		}
		else if(type==MayamAssetType.PACKAGE.getAssetType()){		
			packageController.deletePackage(houseID);
		}
		
	}

	private void sendManualItemPurgeEvent(AttributeMap messageAttributes)
	{
		try
		{
			PlaceholderMessage ph = new PlaceholderMessage();
			DeleteMaterial dm = new DeleteMaterial();
			Material m = new Material();
			m.setMaterialID((String) messageAttributes.getAttribute(Attribute.HOUSE_ID));
			
			dm.setTitleID(messageAttributes.getAttributeAsString(Attribute.PARENT_HOUSE_ID));
			dm.setMaterial(m);
			
			Actions a = new Actions();
			a.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(dm);
			ph.setActions(a);
			
			String eventName = MANUAL_PURGE;
			String event = serialiser.serialise(ph);
			String namespace = bmsEventsNamespace;
			
			eventsService.saveEvent(eventName, event, namespace);
			
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

}
