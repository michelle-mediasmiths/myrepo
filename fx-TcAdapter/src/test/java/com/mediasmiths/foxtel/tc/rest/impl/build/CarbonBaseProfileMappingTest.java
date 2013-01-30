package com.mediasmiths.foxtel.tc.rest.impl.build;

import com.mediasmiths.foxtel.tc.CarbonBaseProfile;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.foxtel.tc.rest.api.TCResolution;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * unit tests to confirm some SIT tests (FXT 4.4.x profile selection tests)
 */
public class CarbonBaseProfileMappingTest
{
	CarbonProjectBuilder builder = new CarbonProjectBuilder();

	@Test
	public void fxt_4_4_2_Stereo_HD_Profile()
	{
		assertProfile(CarbonBaseProfile.MAM_HD_12ST_GXF_HD_12ST, TCOutputPurpose.TX_HD, TCResolution.HD, TCAudioType.STEREO);
	}

	@Test
	public void fxt_4_4_7_Dolby_HD_Profile()
	{
		assertProfile(CarbonBaseProfile.MAM_HD_12ST_38SUR_GXF_HD_12ST_34DBE,
		              TCOutputPurpose.TX_HD,
		              TCResolution.HD,
		              TCAudioType.DOLBY_E);
	}

	@Test
	public void fxt_4_4_8_HD_to_SD_Profile_Stereo_Profile()
	{
		assertProfile(CarbonBaseProfile.MAM_HD_12ST_GXF_SD_12ST, TCOutputPurpose.TX_SD, TCResolution.HD, TCAudioType.STEREO);
	}

	@Test
	public void fxt_4_4_8b_HD_to_SD_Profile_Dolby_Profile()
	{
		assertProfile(CarbonBaseProfile.MAM_HD_12ST_38SUR_GXF_SD_12ST_34DBE,
		              TCOutputPurpose.TX_SD,
		              TCResolution.HD,
		              TCAudioType.DOLBY_E);
	}

	@Test
	public void fxt_4_4_10_Dolby_SD_Profile()
	{
		assertProfile(CarbonBaseProfile.MAM_SD_12ST_38SUR_GXF_SD_12ST_34DBE,
		              TCOutputPurpose.TX_SD,
		              TCResolution.SD,
		              TCAudioType.DOLBY_E);
	}

	@Test
	public void fxt_4_4_11_Stereo_SD_Profile()
	{
		assertProfile(CarbonBaseProfile.MAM_SD_12ST_38SUR_GXF_SD_12ST_34DBE,
		              TCOutputPurpose.TX_SD,
		              TCResolution.SD,
		              TCAudioType.DOLBY_E);
	}

	@Test
	public void fxt_4_4_12_Captioning_Profile_Profile()
	{
		assertProfile(CarbonBaseProfile.MAM_MXF_MPG1VCD_TLC, TCOutputPurpose.CAPTIONING, TCResolution.SD, TCAudioType.DOLBY_E);
		assertProfile(CarbonBaseProfile.MAM_MXF_MPG1VCD_TLC, TCOutputPurpose.CAPTIONING, TCResolution.HD, TCAudioType.DOLBY_E);
		assertProfile(CarbonBaseProfile.MAM_MXF_MPG1VCD_TLC, TCOutputPurpose.CAPTIONING, TCResolution.SD, TCAudioType.STEREO);
		assertProfile(CarbonBaseProfile.MAM_MXF_MPG1VCD_TLC, TCOutputPurpose.CAPTIONING, TCResolution.HD, TCAudioType.STEREO);

	}


	@Test
	public void fxt_4_4_12_Stereo_DVD_Profile()
	{
		assertProfile(CarbonBaseProfile.MAM_MXF_DVD_ST_BUG_TLC, TCOutputPurpose.DVD, TCResolution.SD, TCAudioType.STEREO);
		assertProfile(CarbonBaseProfile.MAM_MXF_DVD_ST_BUG_TLC, TCOutputPurpose.DVD, TCResolution.HD, TCAudioType.STEREO);
	}

	@Test
	public void fxt_4_4_12_Dolby_DVD_Profile()
	{
		assertProfile(CarbonBaseProfile.MAM_MXF_DVD_DBE_BUG_TLC, TCOutputPurpose.DVD, TCResolution.SD, TCAudioType.DOLBY_E);
		assertProfile(CarbonBaseProfile.MAM_MXF_DVD_DBE_BUG_TLC, TCOutputPurpose.DVD, TCResolution.HD, TCAudioType.DOLBY_E);
	}


	void assertProfile(CarbonBaseProfile expected, TCOutputPurpose purpose, TCResolution source, TCAudioType audio)
	{
		TCJobParameters params = new TCJobParameters();
		params.purpose = purpose;
		params.resolution = source;
		params.audioType = audio;

		final CarbonBaseProfile chosen = builder.pickProfile(params);

		assertEquals(expected, chosen);
	}
}
