package com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators;


import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.RandomStringUtils;

import au.com.foxtel.cf.mam.pms.ChannelType;
import au.com.foxtel.cf.mam.pms.Channels;
import au.com.foxtel.cf.mam.pms.License;
import au.com.foxtel.cf.mam.pms.LicenseHolderType;
import au.com.foxtel.cf.mam.pms.LicensePeriodType;
import au.com.foxtel.cf.mam.pms.RightsType;

import com.mediasmiths.foxtel.placeholder.messagecreation.elementgenerators.HelperMethods.Relative;

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

		LicenseHolderType licenseHolder = new LicenseHolderType();
		LicensePeriodType licensePeriod = new LicensePeriodType();
		ChannelType channel1 = new ChannelType();
		Channels channels = new Channels();

		licenseHolder.setOrganisationID("ID"+RandomStringUtils.randomAlphanumeric(20));
		licenseHolder.setOrganisationName("NAME"+RandomStringUtils.randomAlphanumeric(20));
		license1.setLicenseHolder(licenseHolder);

		HelperMethods method = new HelperMethods();
		XMLGregorianCalendar startDate = method.giveValidDate();
		licensePeriod.setStartDate(startDate);
		XMLGregorianCalendar endDate = method.giveValidDate(Relative.AFTER,startDate.toGregorianCalendar());
		licensePeriod.setEndDate(endDate);
		license1.setLicensePeriod(licensePeriod);

		channel1.setChannelTag("FOX");  //TODO : pick these out of a larger set
		channel1.setChannelName("FOX8");
		channels.getChannel().add(channel1);
		license1.setChannels(channels);

		rights.getLicense().add(license1);

		return rights;
	}

}
