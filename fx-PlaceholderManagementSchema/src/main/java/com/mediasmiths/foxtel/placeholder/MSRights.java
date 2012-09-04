package com.mediasmiths.foxtel.placeholder;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import au.com.foxtel.cf.mam.pms.Channel;
import au.com.foxtel.cf.mam.pms.Channels;
import au.com.foxtel.cf.mam.pms.License;
import au.com.foxtel.cf.mam.pms.LicenseHolder;
import au.com.foxtel.cf.mam.pms.LicensePeriod;
import au.com.foxtel.cf.mam.pms.RightsType;

public class MSRights {

	/**
	 * Generates an object of type RightsType
	 * 
	 * @param rights
	 * @return
	 * @throws DatatypeConfigurationException
	 */
	public RightsType validRights(RightsType rights)
			throws DatatypeConfigurationException {

		License license1 = new License();

		LicenseHolder licenseHolder = new LicenseHolder();
		LicensePeriod licensePeriod = new LicensePeriod();
		Channel channel1 = new Channel();
		Channels channels = new Channels();

		licenseHolder.setOrganistaionID("abc123");
		licenseHolder.setOrganistationName("TNC");
		license1.setLicenseHolder(licenseHolder);

		HelperMethods method = new HelperMethods();
		XMLGregorianCalendar xmlCal = method.giveValidDate();
		licensePeriod.setStartDate(xmlCal);
		xmlCal = method.giveValidDate();
		licensePeriod.setEndDate(xmlCal);
		license1.setLicensePeriod(licensePeriod);

		channel1.setChannelTag("BBC");
		channel1.setChannelName("British Broadcasting Company");
		channels.getChannel().add(channel1);
		license1.setChannels(channels);

		rights.getLicense().add(license1);

		return rights;
	}

}
