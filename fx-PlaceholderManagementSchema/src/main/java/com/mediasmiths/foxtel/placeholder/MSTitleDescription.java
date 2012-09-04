package com.mediasmiths.foxtel.placeholder;

import java.math.BigInteger;

import au.com.foxtel.cf.mam.pms.TitleDescriptionType;

public class MSTitleDescription {

	/**
	 * Creates a valid object of type TitleDescriptionType
	 * 
	 * @param titleDescription
	 * @param titleId
	 * @return titleDescription
	 */
	public TitleDescriptionType validTitleDescription(
			TitleDescriptionType titleDescription, String titleId) {

		titleDescription.setTitle(titleId);
		titleDescription.setEpisodeTitle(titleId);
		titleDescription.setProductionNumber("123");
		titleDescription.setEpisodeNumber(new BigInteger("11"));
		titleDescription.setCountryOfProduction("Britian");
		titleDescription.setYearOfProduction(new BigInteger("2002"));
		titleDescription.setStyle("Comedy");
		titleDescription.setShow(titleId);
		titleDescription.setSeries(new BigInteger("2"));

		return titleDescription;
	}

}
