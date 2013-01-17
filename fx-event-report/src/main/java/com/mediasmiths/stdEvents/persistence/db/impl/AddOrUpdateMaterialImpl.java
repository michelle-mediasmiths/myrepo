package com.mediasmiths.stdEvents.persistence.db.impl;

import org.apache.log4j.Logger;

import com.mediasmiths.stdEvents.events.db.entity.PlaceholderMessage;
import com.mediasmiths.stdEvents.events.db.entity.placeholder.AddOrUpdateMaterial;
import com.mediasmiths.stdEvents.events.db.marshaller.PlaceholderMarshaller;

public class AddOrUpdateMaterialImpl implements PlaceholderMarshaller<AddOrUpdateMaterial>
{
	public static final transient Logger logger = Logger.getLogger(AddOrUpdateMaterialImpl.class);
	
	public AddOrUpdateMaterial get (PlaceholderMessage message)
	{
		AddOrUpdateMaterial material = new AddOrUpdateMaterial();
		String str = message.getActions();
		logger.info(str);
		if (str.contains("titleID"))
			material.setTitleID(str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9))));
		if (str.contains("materialID"))
			material.setMaterialID(str.substring(str.indexOf("materialID")+12, str.indexOf('"', (str.indexOf("materialID")+12))));
		if (str.contains("RequiredBy"))
			material.setRequiredBy(str.substring(str.indexOf("RequiredBy")+11, str.indexOf('<', str.indexOf("RequiredBy")+11)));
		if (str.contains("RequiredFormat"))
			material.setRequiredFormat(str.substring(str.indexOf("RequiredFormat")+15, str.indexOf('<', (str.indexOf("RequiredFormat")))));
		if (str.contains("QualityCheckTask"))
			material.setQualityCheckTask(str.substring(str.indexOf("QualityCheckTask")+17, str.indexOf('<', (str.indexOf("QualityCheckTask")))));
		if (str.contains("OrderCreated"))
			material.setOrderCreated(str.substring(str.indexOf("OrderCreated")+13, str.indexOf('<',(str.indexOf("OrderCreated")))));
		if (str.contains("OrderReference"))
			material.setOrderReference(str.substring(str.indexOf("OrderReference")+15, str.indexOf('<', (str.indexOf("OrderReference")))));
		if (str.contains("aggregatorID"))
			material.setAggregatorID(str.substring(str.indexOf("aggregatorID")+13, str.indexOf('"', (str.indexOf("aggregatorID")))));
		if (str.contains("aggregatorName"))
			material.setAggregatorName(str.substring(str.indexOf("aggregatorName")+15, str.indexOf('"', (str.indexOf("aggregatorName")))));
		
		return material;
	}
}
