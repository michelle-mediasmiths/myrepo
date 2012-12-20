package com.mediasmiths.foxtel.tc;

import java.io.IOException;
import java.util.Date;

import org.apache.cxf.helpers.IOUtils;
import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.pathresolver.PathResolver;
import com.mediasmiths.foxtel.pathresolver.PathResolver.PathType;
import com.mediasmiths.foxtel.tc.model.TCBuildJobXMLRequest;
import com.mediasmiths.foxtel.tc.model.TCStartRequest;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

public class JobBuilder
{

	private final static Logger log = Logger.getLogger(JobBuilder.class);

	/**
	 * 
	 * video audio dolbyEe downres profile SD Stereo No n/a MAM-SD-12ST_GXF-SD-12ST SD Stereo + Descrete Surround Yes n/a MAM-SD-12ST_38SUR_GXF-SD-12ST-34DBE HD Stereo No No MAM-HD-12ST_GXF-HD-12ST HD
	 * Stereo + Descrete Surround Yes No MAM-HD-12ST_38SUR_GXF-HD-12ST-34DBE HD Stereo No Yes MAM-HD-12ST_GXF-SD-12ST HD Stereo + Descrete Surround Yes Yes MAM-HD-12ST_38SUR_GXF-SD-12ST-34DBE
	 * 
	 */

	@Inject
	private MayamClient mayamClient;

	@Inject
	private PathResolver pathResolver;

	@Inject
	@Named(Config.BUG_FOLDER)
	private String buglocation;
	
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
	private final static String FULL_UNC_INPUT_PATH_PH = "FULL_UNC_INPUT_PATH";
	private final static String OUTPUT_FOLDER_PATH_PH = "OUTPUT_FOLDER_PATH";
	private final static String FULL_UNC_OUTPUT_PATH_PH = "FULL_UNC_OUTPUT_PATH";
	private final static String OUTPUT_BASENAME_PH = "OUTPUT_BASENAME";

	public String buildJobForTxPackageTranscode(String packageID, String inputfile, String outputFolder)
			throws MayamClientException,
			JobBuilderException
	{

		log.info(String.format(
				"Building job for tx package transcode packageId %s inputfile %s outputfolder %s",
				packageID,
				inputfile,
				outputFolder));
		// fetch package and material information to determine profile
		PackageType pack = mayamClient.getPackage(packageID);
		MaterialType material = mayamClient.getMaterial(pack.getMaterialID());

		TxProfile profile;

		try
		{
			profile = pickTxProfile(pack, material);
			log.debug(String.format("Selected profile %s for packageId %s", profile.toString(), packageID));
		}
		catch (Exception e)
		{
			// TODO: make this a severe and throw jobbuilderexception once we can persist stuff through mayam
			log.fatal("exception picking profile for package " + packageID, e);
			profile = TxProfile.MAM_HD_12ST_GXF_HD_12ST;
		}

		String pcp = loadProfileForPackage(packageID, profile);
		pcp = setInputAndOutputPaths(packageID, inputfile, outputFolder, pcp);

		
		
		return pcp;
	}

	private String setInputAndOutputPaths(String packageID, String inputfile, String outputFolder, String pcp)
	{
		// assuming for now that the path is a linux style path	
		String uncInputPath = pathResolver.uncPath(PathType.NIX, inputfile);
		String uncOutputPath = pathResolver.uncPath(PathType.NIX, outputFolder);
		
		pcp = pcp.replace(INPUT_FILE_PATH_PH, uncInputPath);
		pcp = pcp.replace(OUTPUT_FOLDER_PATH_PH, uncOutputPath);
		
		pcp = pcp.replace(FULL_UNC_INPUT_PATH_PH, uncInputPath);
		pcp = pcp.replace(FULL_UNC_OUTPUT_PATH_PH, uncOutputPath);
		
		pcp = pcp.replace(OUTPUT_BASENAME_PH, packageID);
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
			throw new JobBuilderException(message, e);
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

	public void setMayamClient(MayamClient mayamClient)
	{
		this.mayamClient = mayamClient;
	}

	public void setPathResolver(PathResolver pathResolver)
	{
		this.pathResolver = pathResolver;
	}

	private final static long ONE_HOUR = 1000 * 3600l;
	private final static long TWELVE_HOURS = ONE_HOUR * 12;
	private final static long ONE_DAY = ONE_HOUR * 24;
	private final static long THREE_DAYS = ONE_DAY * 3;
	private final static long EIGHT_DAYS = ONE_DAY * 8;
	
	
	public Integer getPriorityForTXJob(TCBuildJobXMLRequest buildJobXMLRequest)
	{
		log.debug(String.format("determining prority for job for asset %s tx date is %s",buildJobXMLRequest.getPackageID(),buildJobXMLRequest.getTxDate().toString()));
	
		int priority=1;
		
		long now = System.currentTimeMillis();
		long txTime = buildJobXMLRequest.getTxDate().getTime();
		long difference = txTime - now;
		
		if(difference < TWELVE_HOURS){
			priority=8;
		}
		else if(difference < ONE_DAY){
			priority=7;
		}
		else if(difference < THREE_DAYS){
			priority=6;
		}		
		else if(difference < EIGHT_DAYS){
			priority=4;
		}	
		else{
			priority=2;
		}
		
		log.debug("returning prority "+priority);
		return Integer.valueOf(priority);
	}

}
