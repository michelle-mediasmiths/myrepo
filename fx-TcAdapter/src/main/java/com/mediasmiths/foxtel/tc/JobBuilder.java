package com.mediasmiths.foxtel.tc;

import java.io.IOException;

import org.apache.cxf.helpers.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.common.IOUtil;

import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

public class JobBuilder
{

	private final static Logger log = Logger.getLogger(JobBuilder.class);

	/**
	 * 
	 * Source | Options |Profile video audio dolbyEe downres | SD Stereo No n/a MAM-SD-12ST_GXF-SD-12ST SD Stereo + Descrete Surround Yes n/a MAM-SD-12ST_38SUR_GXF-SD-12ST-34DBE HD Stereo No No
	 * MAM-HD-12ST_GXF-HD-12ST HD Stereo + Descrete Surround Yes No MAM-HD-12ST_38SUR_GXF-HD-12ST-34DBE HD Stereo No Yes MAM-HD-12ST_GXF-SD-12ST HD Stereo + Descrete Surround Yes Yes
	 * MAM-HD-12ST_38SUR_GXF-SD-12ST-34DBE
	 * 
	 */

	@Inject
	private MayamClient mayamClient;

	public enum TxProfile
	{
		MAM_SD_12ST_GXF_SD_12ST("pcp/quicktime.xml"),
		MAM_SD_12ST_38SUR_GXF_SD_12ST_34DBE("pcp/quicktime.xml"),
		MAM_HD_12ST_GXF_HD_12ST("pcp/quicktime.xml"),
		MAM_HD_12ST_38SUR_GXF_HD_12ST_34DBE("pcp/quicktime.xml"),
		MAM_HD_12ST_GXF_SD_12ST("pcp/quicktime.xml"),
		MAM_HD_12ST_38SUR_GXF_SD_12ST_34DBE("pcp/quicktime.xml");

		private String filename;

		TxProfile(String fname)
		{
			filename = fname;
		}

		public String getFileName()
		{
			return filename;
		}
	}

	private final static String INPUT_FILE_PATH_PH = "INPUT_FILE_PATH";
	private final static String OUTPUT_FOLDER_PATH_PH = "OUTPUT_FOLDER_PATH";

	public String buildJobForTxPackageTranscode(String packageID, String inputfile, String outputFolder) throws MayamClientException, JobBuilderException
	{

		log.info(String.format("Building job for tx package transcode packageId %s inputfile %s outputfolder %s", packageID, inputfile, outputFolder));
		// fetch package and material information to determine profile
		PackageType pack = mayamClient.getPackage(packageID);
		MaterialType material = mayamClient.getMaterial(pack.getMaterialID());
		
		TxProfile profile = pickTxProfile(pack, material);
		log.debug(String.format("Selected profile %s for packageId %s", profile.toString(), packageID));

		String pcp = loadProfileForPackage(packageID, profile);
		
		pcp.replace(INPUT_FILE_PATH_PH, inputfile);
		pcp.replace(OUTPUT_FOLDER_PATH_PH, outputFolder);
		
		return pcp;
	}

	private String loadProfileForPackage(String packageID, TxProfile profile) throws JobBuilderException
	{
		String pcp;
		try
		{
			pcp = IOUtils.readStringFromStream(getClass().getClassLoader().getResourceAsStream(profile.getFileName()));
		}
		catch (IOException e)
		{
			String message = String.format("Error fetching profile %s for package %s", profile, packageID);
			log.error(message, e);
			throw new JobBuilderException(message,e);
		}
		return pcp;
	}

	private TxProfile pickTxProfile(PackageType pack, MaterialType material)
	{
		boolean isMaterialSD = (material.getRequiredFormat().equals("SD"));
		boolean isMaterialHD = (material.getRequiredFormat().equals("HD"));

		boolean isDolbyE = (false); // TODO replace once we can store retrieve audio track information in mayam

		boolean isPackageHD = (pack.getPresentationFormat() == PresentationFormatType.HD);
		boolean downRes = isMaterialHD && !isPackageHD;

		TxProfile profile;

		if (isMaterialSD)
		{
			if (isDolbyE)
			{
				profile = TxProfile.MAM_SD_12ST_38SUR_GXF_SD_12ST_34DBE;
			}
			else
			{
				profile = TxProfile.MAM_SD_12ST_GXF_SD_12ST;
			}
		}
		else
		{
			if (downRes)
			{
				if (isDolbyE)
				{
					profile = TxProfile.MAM_HD_12ST_38SUR_GXF_SD_12ST_34DBE;
				}
				else
				{
					profile = TxProfile.MAM_HD_12ST_GXF_SD_12ST;
				}
			}
			else
			{
				if (isDolbyE)
				{
					profile = TxProfile.MAM_HD_12ST_38SUR_GXF_HD_12ST_34DBE;
				}
				else
				{
					profile = TxProfile.MAM_HD_12ST_GXF_HD_12ST;
				}
			}
		}
		return profile;
	}

}
