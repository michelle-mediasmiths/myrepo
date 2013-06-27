package com.mediasmiths.foxtel.wf.adapter.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

public class TxUtil
{

	private final static Logger log = Logger.getLogger(TxUtil.class);


	public static String deliveryLocationForPackage(String packageID,
	                                                MayamClient mayamClient,
	                                                String baseTXDeliveryLocation,
	                                                String aoDeliveryLocation,
	                                                boolean isAO) throws MayamClientException
	{

		boolean isSD = mayamClient.isPackageSD(packageID);
		String subFolder;

		if (isSD)
		{
			subFolder = "SD";
		}
		else
		{
			subFolder = "HD";
		}

		String ret;
		if (!isAO)
		{
			ret = String.format("%s/%s/", baseTXDeliveryLocation, subFolder);
		}
		else
		{
			ret = String.format("%s/", aoDeliveryLocation);
		}

		log.info(String.format("Returning delivery location %s for package %s", ret, packageID));

		return ret;
	}


	public static String transcodeFolderForPackage(String packageID, String txWaitingFolder, String aoWaitingFolder, boolean isAO)
	{
		String ret = pickFolderAndNormalise(txWaitingFolder, aoWaitingFolder, isAO);

		log.info(String.format("Returning transcode destination folder %s for package %s", ret, packageID));

		return ret;
	}


	public static String quarrentineLocationForPackage(final String packageID,
	                                                   final String txQuarantineLocation,
	                                                   final String aoQuarantineLocation,
	                                                   final boolean isAO, Long taskID)
	{
		String base;

		if (isAO)
		{
			base = aoQuarantineLocation;
		}
		else
		{
			base = txQuarantineLocation;
		}

		String location = String.format("%s/%s_%s.gxf", base,packageID,taskID);
		String ret = FilenameUtils.normalize(location);

		log.info(String.format("Returning qurrentine location %s for package %s", ret, packageID));

		return ret;
	}


	private static String pickFolderAndNormalise(String nonaofolder, String aofolder, boolean isAO)
	{

		String base;

		if (isAO)
		{
			base = aofolder;
		}
		else
		{
			base = nonaofolder;
		}

		String location = String.format("%s/", base);
		String ret = FilenameUtils.normalize(location);

		return ret;
	}
}
