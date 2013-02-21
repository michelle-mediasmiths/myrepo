package com.mediasmiths.mq.handlers.button;

import org.apache.log4j.Logger;
import org.mortbay.log.Log;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamButtonType;
import com.mediasmiths.mayam.MayamClientException;

public class QcParallel extends ButtonClickHandler
{

	private final static Logger log = Logger.getLogger(QcParallel.class);
	
	@Override
	protected void buttonClicked(AttributeMap messageAttributes)
	{
		log.info(String.format("QC Parallel clicked for item with house id %s", messageAttributes.getAttributeAsString(Attribute.HOUSE_ID)));
		AttributeMap update = taskController.updateMapForAsset(messageAttributes);
		update.setAttribute(Attribute.QC_PARALLEL_ALLOWED, Boolean.TRUE);
		try
		{
			tasksClient.assetApi().updateAsset(update);
		}
		catch (RemoteException e)
		{
			log.error("error setting qc parallel allow flag on asset ",e);
		}
		
		if(!AssetProperties.isMaterialPlaceholder(messageAttributes) && ! AssetProperties.isQCStatusDetermined(messageAttributes)) {
			log.info("QC Parallel: creating preview task _ item is not a place holder and QC status is not set ");
			
			try
			{
				taskController.createPreviewTaskForMaterial(messageAttributes.getAttributeAsString(Attribute.HOUSE_ID), null);
			}
			catch (MayamClientException e)
			{
				log.error("error creating preview task for material",e);
			}
		}
	}

	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.QC_PARALLEL_ALLOWED;
	}

	@Override
	public String getName()
	{
		return "QC Parallel Allowed";
	}

}
