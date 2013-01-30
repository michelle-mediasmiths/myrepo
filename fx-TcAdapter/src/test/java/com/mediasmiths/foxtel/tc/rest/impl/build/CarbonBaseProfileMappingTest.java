package com.mediasmiths.foxtel.tc.rest.impl.build;

import com.mediasmiths.foxtel.tc.CarbonBaseProfile;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCOutputPurpose;
import com.mediasmiths.foxtel.tc.rest.api.TCResolution;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * unit tests to confirm some SIT tests (FXT 4.4.x profile selection tests)
 */
public class CarbonBaseProfileMappingTest
{
	CarbonProjectBuilder builder = new CarbonProjectBuilder();


	/**
	 * Test that we have a suitable profile to use for all combinations of inputs
	 */
	@Test
	public void testThatAllInputsAreMapped()
	{
		for (TCOutputPurpose purpose : TCOutputPurpose.values())
		{
			for (TCResolution resolution : TCResolution.values())
			{
				// There is one combination that's not permitted: HD TX for an SD asset
				if (resolution == TCResolution.SD && purpose == TCOutputPurpose.TX_HD)
					continue;

				for (TCAudioType audio : TCAudioType.values())
				{
					assertNotNull("Profile for " + purpose + ", " + resolution + ", " + audio, pickProfile(purpose,
					                                                                                       resolution,
					                                                                                       audio));
				}
			}
		}
	}

	@Test
	public void fxt_4_4_2_Stereo_HD_Profile()
	{
		assertProfile(CarbonBaseProfile.TX_HD_STEREO, TCOutputPurpose.TX_HD, TCResolution.HD, TCAudioType.STEREO);
	}

	@Test
	public void fxt_4_4_7_Dolby_HD_Profile()
	{
		assertProfile(CarbonBaseProfile.TX_HD_DOLBYE,
		              TCOutputPurpose.TX_HD,
		              TCResolution.HD,
		              TCAudioType.DOLBY_E);
	}

	@Test
	public void fxt_4_4_8_HD_to_SD_Profile_Stereo_Profile()
	{
		assertProfile(CarbonBaseProfile.TX_SD_STEREO_FROM_HD_SOURCE, TCOutputPurpose.TX_SD, TCResolution.HD, TCAudioType.STEREO);
	}

	@Test
	public void fxt_4_4_8b_HD_to_SD_Profile_Dolby_Profile()
	{
		assertProfile(CarbonBaseProfile.TX_SD_DOLBYE_FROM_HD_SOURCE,
		              TCOutputPurpose.TX_SD,
		              TCResolution.HD,
		              TCAudioType.DOLBY_E);
	}

	@Test
	public void fxt_4_4_10_Dolby_SD_Profile()
	{
		assertProfile(CarbonBaseProfile.TX_SD_DOLBYE,
		              TCOutputPurpose.TX_SD,
		              TCResolution.SD,
		              TCAudioType.DOLBY_E);
	}

	@Test
	public void fxt_4_4_11_Stereo_SD_Profile()
	{
		assertProfile(CarbonBaseProfile.TX_SD_DOLBYE,
		              TCOutputPurpose.TX_SD,
		              TCResolution.SD,
		              TCAudioType.DOLBY_E);
	}

	@Test
	public void fxt_4_4_12_Captioning_Profile_Profile()
	{
		assertProfile(CarbonBaseProfile.CAPTIONING, TCOutputPurpose.CAPTIONING, TCResolution.SD, TCAudioType.DOLBY_E);
		assertProfile(CarbonBaseProfile.CAPTIONING, TCOutputPurpose.CAPTIONING, TCResolution.HD, TCAudioType.DOLBY_E);
		assertProfile(CarbonBaseProfile.CAPTIONING, TCOutputPurpose.CAPTIONING, TCResolution.SD, TCAudioType.STEREO);
		assertProfile(CarbonBaseProfile.CAPTIONING, TCOutputPurpose.CAPTIONING, TCResolution.HD, TCAudioType.STEREO);

	}


	@Test
	public void fxt_4_4_12_Stereo_DVD_Profile()
	{
		assertProfile(CarbonBaseProfile.DVD_STEREO, TCOutputPurpose.DVD, TCResolution.SD, TCAudioType.STEREO);
		assertProfile(CarbonBaseProfile.DVD_STEREO, TCOutputPurpose.DVD, TCResolution.HD, TCAudioType.STEREO);
	}

	@Test
	public void fxt_4_4_12_Dolby_DVD_Profile()
	{
		assertProfile(CarbonBaseProfile.DVD_DOLBYE, TCOutputPurpose.DVD, TCResolution.SD, TCAudioType.DOLBY_E);
		assertProfile(CarbonBaseProfile.DVD_DOLBYE, TCOutputPurpose.DVD, TCResolution.HD, TCAudioType.DOLBY_E);
	}


	void assertProfile(CarbonBaseProfile expected, TCOutputPurpose purpose, TCResolution source, TCAudioType audio)
	{
		final CarbonBaseProfile chosen = pickProfile(purpose, source, audio);

		assertEquals(expected, chosen);
	}

	CarbonBaseProfile pickProfile(TCOutputPurpose purpose, TCResolution source, TCAudioType audio)
	{
		TCJobParameters params = new TCJobParameters();
		params.purpose = purpose;
		params.resolution = source;
		params.audioType = audio;

		return builder.pickProfile(params);
	}
}
