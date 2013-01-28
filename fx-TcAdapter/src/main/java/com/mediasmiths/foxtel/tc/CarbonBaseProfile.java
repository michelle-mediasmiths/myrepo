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
	MAM_HD_12ST_GXF_HD_12ST(TCOutputPurpose.TX_HD, TCResolution.HD, TCAudioType.STEREO),
	/**
	 * SD GXF (from HD source) with Stereo audio
	 * <ul>
	 * <li>GXF</li>
	 * <li>Downres HD to SD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * </ul>
	 */
	MAM_HD_12ST_GXF_SD_12ST(TCOutputPurpose.TX_SD, TCResolution.HD, TCAudioType.STEREO),
	/**
	 * SD GXF with Stereo audio
	 * <ul>
	 * <li>GXF</li>
	 * <li>SD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * </ul>
	 */
	MAM_SD_12ST_GXF_SD_12ST(TCOutputPurpose.TX_SD, TCResolution.SD, TCAudioType.STEREO),

	/**
	 * HD GXF with Stereo and DolbyE
	 * <ul>
	 * <li>GXF</li>
	 * <li>HD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * <li>A3 and A4 are DolbyE</li>
	 * </ul>
	 */
	MAM_HD_12ST_38SUR_GXF_HD_12ST_34DBE(TCOutputPurpose.TX_HD, TCResolution.HD, TCAudioType.DOLBY_E),

	/**
	 * SD GXF (from HD source) with Stereo and DolbyE
	 * <ul>
	 * <li>GXF</li>
	 * <li>Downres HD to SD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * <li>A3 and A4 are DolbyE</li>
	 * </ul>
	 */
	MAM_HD_12ST_38SUR_GXF_SD_12ST_34DBE(TCOutputPurpose.TX_SD, TCResolution.HD, TCAudioType.DOLBY_E),

	/**
	 * SD GXF with Stereo and DolbyE
	 * <ul>
	 * <li>SD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * <li>A3 and A4 are DolbyE</li>
	 * </ul>
	 */
	MAM_SD_12ST_38SUR_GXF_SD_12ST_34DBE(TCOutputPurpose.TX_SD, TCResolution.SD, TCAudioType.DOLBY_E),

	/**
	 * VCD for captioning
	 * <ul>
	 * <li>Does not seem to matter whether SD/HD</li>
	 * <li>Does not seem to matter whether Stereo/DolbyE</li>
	 * </ul>
	 */
	MAM_MXF_MPG1VCD_TLC(TCOutputPurpose.CAPTIONING, null, null),
	/**
	 * Delivery content for DVD (where the source has DolbyE audio)
	 * <ul>
	 * <li>Does not seem to matter whether SD/HD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * <li>A3 and A4 are DolbyE</li>
	 * </ul>
	 */
	MAM_MXF_DVD_DBE_BUG_TLC(TCOutputPurpose.DVD, null, TCAudioType.DOLBY_E),
	/**
	 * Delivery content for DVD (where the source has only Stereo audio)
	 * <ul>
	 * <li>Does not seem to matter whether SD/HD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * </ul>
	 */
	MAM_MXF_DVD_ST_BUG_TLC(TCOutputPurpose.DVD, null, TCAudioType.STEREO);

	private final TCAudioType audioType;
	private final TCOutputPurpose purpose;
	private final TCResolution source;

	CarbonBaseProfile(TCOutputPurpose purpose, TCResolution source, TCAudioType audio)
	{
		this.purpose = purpose;
		this.source = source;
		this.audioType = audio;
	}

	public boolean suitableFor(TCOutputPurpose purpose, TCResolution source, TCAudioType audio)
	{
		return matchingPurpose(purpose) && matchingResolution(source) && matchingAudioType(audio);
	}

	private boolean matchingPurpose(TCOutputPurpose purpose)
	{
		return (this.purpose == null || this.purpose == purpose);
	}

	private boolean matchingResolution(TCResolution source)
	{
		return (this.source == null || this.source == source);
	}

	private boolean matchingAudioType(TCAudioType audio)
	{
		return (this.audioType == null || this.audioType == audio);
	}
}
