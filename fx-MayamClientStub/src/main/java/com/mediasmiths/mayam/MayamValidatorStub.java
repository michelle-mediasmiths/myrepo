package com.mediasmiths.mayam;

import javax.xml.datatype.XMLGregorianCalendar;

import com.mediasmiths.mayam.validation.MayamValidator;

public class MayamValidatorStub implements MayamValidator
{

	@Override
	public boolean validateMaterialBroadcastDate(XMLGregorianCalendar targetDate, String materialID) throws MayamClientException
	{
		return true;
	}

	@Override
	public boolean validateTitleBroadcastDate(
			String titleID,
			XMLGregorianCalendar licenseStartDate,
			XMLGregorianCalendar licenseEndDate) throws MayamClientException
	{
		return true;
	}

}
