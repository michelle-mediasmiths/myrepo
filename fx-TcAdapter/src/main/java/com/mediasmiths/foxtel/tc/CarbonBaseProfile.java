package com.mediasmiths.foxtel.tc;

import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.foxtel.tc.rest.api.TCResolution;

/**
 * Describes the base profiles
 */
public enum CarbonBaseProfile
{
	/**
	 * HD GXF with Stereo audio
	 * <ul>
	 * <li>GXF</li>
	 * <li>HD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * </ul>
	 */
	MAM_HD_12ST_GXF_HD_12ST(TCOutputPurpose.TX_HD, TCResolution.HD, TCAudioType.STEREO, "pcp/quicktime.xml"),
	/**
	 * SD GXF (from HD source) with Stereo audio
	 * <ul>
	 * <li>GXF</li>
	 * <li>Downres HD to SD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * </ul>
	 */
	MAM_HD_12ST_GXF_SD_12ST(TCOutputPurpose.TX_SD, TCResolution.HD, TCAudioType.STEREO, "pcp/quicktime.xml"),
	/**
	 * SD GXF with Stereo audio
	 * <ul>
	 * <li>GXF</li>
	 * <li>SD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * </ul>
	 */
	MAM_SD_12ST_GXF_SD_12ST(TCOutputPurpose.TX_SD, TCResolution.SD, TCAudioType.STEREO, "pcp/quicktime.xml"),

	/**
	 * HD GXF with Stereo and DolbyE
	 * <ul>
	 * <li>GXF</li>
	 * <li>HD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * <li>A3 and A4 are DolbyE</li>
	 * </ul>
	 */
	MAM_HD_12ST_38SUR_GXF_HD_12ST_34DBE(TCOutputPurpose.TX_HD, TCResolution.HD, TCAudioType.DOLBY_E, "pcp/quicktime.xml"),

	/**
	 * SD GXF (from HD source) with Stereo and DolbyE
	 * <ul>
	 * <li>GXF</li>
	 * <li>Downres HD to SD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * <li>A3 and A4 are DolbyE</li>
	 * </ul>
	 */
	MAM_HD_12ST_38SUR_GXF_SD_12ST_34DBE(TCOutputPurpose.TX_SD, TCResolution.HD, TCAudioType.DOLBY_E, "pcp/quicktime.xml"),

	/**
	 * SD GXF with Stereo and DolbyE
	 * <ul>
	 * <li>SD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * <li>A3 and A4 are DolbyE</li>
	 * </ul>
	 */
	MAM_SD_12ST_38SUR_GXF_SD_12ST_34DBE(TCOutputPurpose.TX_SD, TCResolution.SD, TCAudioType.DOLBY_E, "pcp/quicktime.xml");

	private final TCAudioType audioType;
	private final TCOutputPurpose purpose;
	private final TCResolution source;

	private final String filename;

	CarbonBaseProfile(TCOutputPurpose purpose, TCResolution source, TCAudioType audio, String fname)
	{
		this.purpose = purpose;
		this.source = source;
		this.audioType = audio;
		this.filename = fname;
	}

	public String getFileName()
	{
		return filename;
	}


	public boolean suitableFor(TCOutputPurpose purpose, TCResolution source, TCAudioType audio)
	{
		return (this.purpose == purpose) && (this.source == source) && (this.audioType == audio);
	}
}
