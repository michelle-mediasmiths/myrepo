package com.mediasmiths.foxtel.tc;

import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.foxtel.tc.rest.api.TCResolution;

/**
 * Describes the base profiles available to this adapter
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
	TX_HD_STEREO(TCOutputPurpose.TX_HD, TCResolution.HD, TCAudioType.STEREO, "TX_HD_STEREO.pcp"),
	/**
	 * SD GXF (from HD source) with Stereo audio
	 * <ul>
	 * <li>GXF</li>
	 * <li>Downres HD to SD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * </ul>
	 */
	TX_SD_STEREO_FROM_HD_SOURCE(TCOutputPurpose.TX_SD, TCResolution.HD, TCAudioType.STEREO, "TX_SD_STEREO.pcp"),
	/**
	 * SD GXF with Stereo audio
	 * <ul>
	 * <li>GXF</li>
	 * <li>SD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * </ul>
	 */
	TX_SD_STEREO(TCOutputPurpose.TX_SD, TCResolution.SD, TCAudioType.STEREO, "TX_SD_STEREO.pcp"),

	/**
	 * HD GXF with Stereo and DolbyE
	 * <ul>
	 * <li>GXF</li>
	 * <li>HD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * <li>A3 and A4 are DolbyE</li>
	 * </ul>
	 */
	TX_HD_DOLBYE(TCOutputPurpose.TX_HD, TCResolution.HD, TCAudioType.DOLBY_E, "TX_HD_DOLBYE.pcp"),

	/**
	 * SD GXF (from HD source) with Stereo and DolbyE
	 * <ul>
	 * <li>GXF</li>
	 * <li>Downres HD to SD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * <li>A3 and A4 are DolbyE</li>
	 * </ul>
	 */
	TX_SD_DOLBYE_FROM_HD_SOURCE(TCOutputPurpose.TX_SD, TCResolution.HD, TCAudioType.DOLBY_E, "TX_SD_DOLBYE.pcp"),

	/**
	 * SD GXF with Stereo and DolbyE
	 * <ul>
	 * <li>SD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * <li>A3 and A4 are DolbyE</li>
	 * </ul>
	 */
	TX_SD_DOLBYE(TCOutputPurpose.TX_SD, TCResolution.SD, TCAudioType.DOLBY_E, "TX_SD_DOLBYE.pcp"),

	/**
	 * VCD for captioning
	 * <ul>
	 * <li>Does not seem to matter whether SD/HD</li>
	 * <li>Does not seem to matter whether Stereo/DolbyE</li>
	 * </ul>
	 */
	CAPTIONING(TCOutputPurpose.CAPTIONING, null, null, "VCD_ALL_ALL.pcp"),
	/**
	 * Delivery content for DVD (where the source has DolbyE audio)
	 * <ul>
	 * <li>Does not seem to matter whether SD/HD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * <li>A3 and A4 are DolbyE</li>
	 * </ul>
	 */
	DVD_DOLBYE(TCOutputPurpose.DVD, null, TCAudioType.DOLBY_E, "DVD_ALL_DOLBYE.pcp"),
	/**
	 * Delivery content for DVD (where the source has only Stereo audio)
	 * <ul>
	 * <li>Does not seem to matter whether SD/HD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * </ul>
	 */
	DVD_STEREO(TCOutputPurpose.DVD, null, TCAudioType.STEREO, "DVD_ALL_STEREO.pcp"),
	/**
	 * Delivery content for single MPG4 file (where the source has DolbyE audio)
	 * <ul>
	 * <li>Does not seem to matter whether SD/HD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * <li>A3 and A4 are DolbyE</li>
	 * </ul>
	 */
	MPG4_DOLBYE(TCOutputPurpose.MPG4, null, TCAudioType.DOLBY_E, "MPG4_ALL_DOLBYE.pcp"),
	/**
	 * Delivery content for single MPG4 file (where the source has only Stereo audio)
	 * <ul>
	 * <li>Does not seem to matter whether SD/HD</li>
	 * <li>A1 and A2 are Stereo tracks</li>
	 * </ul>
	 */
	MPG4_STEREO(TCOutputPurpose.MPG4, null, TCAudioType.STEREO, "MPG4_ALL_STEREO.pcp");
	
	private final TCAudioType audioType;
	private final TCOutputPurpose purpose;
	private final TCResolution source;
	private final String filename;

	CarbonBaseProfile(TCOutputPurpose purpose, TCResolution source, TCAudioType audio, String filename)
	{
		this.purpose = purpose;
		this.source = source;
		this.audioType = audio;
		this.filename = filename;
	}

	/**
	 * Determines whether this profile is suitable for the given output, resolution and audio type
	 *
	 * @param purpose
	 * 		the output purpose
	 * @param source
	 * 		the source resolution
	 * @param audio
	 * 		the output audio type
	 *
	 * @return
	 */
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

	public String getFilename()
	{
		return filename;
	}
}
