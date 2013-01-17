package com.mediasmiths.stdEvents.persistence.db.impl;

import java.math.BigInteger;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.stdEvents.events.db.entity.PlaceholderMessage;
import com.mediasmiths.stdEvents.events.db.entity.placeholder.CreateOrUpdateTitle;
import com.mediasmiths.stdEvents.events.db.marshaller.PlaceholderMarshaller;

public class CreateOrUpdateTitleImpl implements PlaceholderMarshaller<CreateOrUpdateTitle>
{
	@Transactional
	public CreateOrUpdateTitle get (PlaceholderMessage message)
	{
		CreateOrUpdateTitle title  = new CreateOrUpdateTitle();
		String str = message.getActions();
		if (str.contains("titleID"))
			title.setTitleID(str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9))));
		if (str.contains("ProgrammeTitle"))
			title.setProgrammeTitle(str.substring(str.indexOf("ProgrammeTitle")+15, str.indexOf('<', str.indexOf("ProgrammeTitle"))));
		if (str.contains("ProductionNumber"))
			title.setProductionNumber(str.substring(str.indexOf("ProductionNumber")+17, str.indexOf('<', str.indexOf("ProductionNumber"))));
		if (str.contains("EpisodeTitle"))
			title.setEpisodeTitle(str.substring(str.indexOf("EpisodeTitle")+13, str.indexOf('<', str.indexOf("EpisodeTitle"))));
		if (str.contains("SeriesNumber"))
			title.setSeriesNumber(new BigInteger(str.substring(str.indexOf("SeriesNumber")+13, str.indexOf('<', str.indexOf("SeriesNumber")))));
		if (str.contains("EpisodeNumber"))
			title.setEpisodeNumber(new BigInteger(str.substring(str.indexOf("EpisodeNumber")+14, str.indexOf('<', str.indexOf("EpisodeNumber")))));
		if (str.contains("YearOfProduction"))
			title.setYearOfProduction(new BigInteger(str.substring(str.indexOf("YearOfProduction")+17, str.indexOf('<', str.indexOf("YearOfProduction")))));
		if (str.contains("CountryOfProduction"))
			title.setCountryOfProduction(str.substring(str.indexOf("CountryOfProduction")+20, str.indexOf('<', str.indexOf("CountryOfProduction"))));
		if (str.contains("Style"))
			title.setStyle(str.substring(str.indexOf("Style")+6, str.indexOf('<', str.indexOf("Style"))));
		if (str.contains("Show"))
			title.setShow(str.substring(str.indexOf("Show")+5, str.indexOf('<', str.indexOf("Show"))));
		if (str.contains("LicenseHolder"))
			title.setLicenseHolder(str.substring(str.indexOf("LicenseHolder")+14, str.indexOf('/', (str.indexOf("LicenseHolder")))));
		if (str.contains("LicensePeriod"))
			title.setLicensePeriod(str.substring(str.indexOf("LicensePeriod")+14, str.indexOf('/', (str.indexOf("LicensePeriod")))));
		if (str.contains("channelName"))
			title.setChannelName(str.substring(str.indexOf("channelName")+13, str.indexOf('"', (str.indexOf("channelName")+13))));
		if (str.contains("channelTag"))
				title.setChannelTag(str.substring(str.indexOf("channelTag")+12, str.indexOf('"', (str.indexOf("channelTag")+12))));

		return title;
	}
}
