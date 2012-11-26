package com.mediasmiths.mayam.validation;

import javax.xml.datatype.XMLGregorianCalendar;

import com.mediasmiths.mayam.MayamClientException;

public interface MayamValidator
{

	public abstract boolean validateMaterialBroadcastDate(XMLGregorianCalendar targetDate, String materialID, String channelTag)
			throws MayamClientException;

	public abstract boolean validateTitleBroadcastDate(
			String titleID,
			XMLGregorianCalendar licenseStartDate,
			XMLGregorianCalendar licenseEndDate) throws MayamClientException;

}