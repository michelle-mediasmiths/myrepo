package com.mediasmiths.stdEvents.persistence.db.impl;

import java.math.BigInteger;

import com.mediasmiths.stdEvents.events.db.entity.PlaceholderMessage;
import com.mediasmiths.stdEvents.events.db.entity.placeholder.AddOrUpdatePackage;
import com.mediasmiths.stdEvents.events.db.marshaller.PlaceholderMarshaller;

public class AddOrUpdatePackageImpl implements PlaceholderMarshaller<AddOrUpdatePackage>
{

	@Override
	public AddOrUpdatePackage get(PlaceholderMessage message)
	{
		AddOrUpdatePackage pack = new AddOrUpdatePackage();
		String str = message.getActions();
		if (!str.contains("Invalid"))
			{
			if (str.contains("titleID"))
				pack.setTitleID(str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9))));
			if (str.contains("presentationID"))
				pack.setPresentationID(str.substring(str.indexOf("presentationID")+15, str.indexOf('"', (str.indexOf("presentationID")+15))));
			if (str.contains("MaterialID"))
				pack.setMaterialID(str.substring(str.indexOf("MaterialID")+11, str.indexOf('<', str.indexOf("MaterialID"))));
			if(str.contains("PresentationFormat"))
				pack.setPresentationFormat(str.substring(str.indexOf("PresentationFormat")+19, str.indexOf('<', str.indexOf("PresentationFormat"))));
			if(str.contains("Classification"))
				pack.setClassification(str.substring(str.indexOf("Classification")+16, str.indexOf('<', str.indexOf("Classification"))));
			if(str.contains("ConsumerAdvice"))
				pack.setConsumerAdvice(str.substring(str.indexOf("ConsumerAdvice")+15, str.indexOf('<', str.indexOf("ConsumerAdvice"))));
			if(str.contains("NumberOfSegments"))
				pack.setNumberOfSegments(new BigInteger(str.substring(str.indexOf("NumberOfSegments")+17, str.indexOf('<', str.indexOf("NumberOfSegments")))));
			if (str.contains("TargetDate"))
				pack.setTargetDate(str.substring(str.indexOf("TargetDate")+11, str.indexOf('<', str.indexOf("TargetDate"))));
			if (str.contains("Notes"))
				pack.setNotes(str.substring(str.indexOf("Notes")+6, str.indexOf('<', str.indexOf("Notes"))));
		}
		
		return pack;
	}

}
