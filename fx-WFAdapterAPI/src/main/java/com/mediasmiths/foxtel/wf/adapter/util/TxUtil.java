package com.mediasmiths.foxtel.wf.adapter.util;

import org.apache.log4j.Logger;

import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

public class TxUtil
{
		
	private final static Logger log = Logger.getLogger(TxUtil.class);
	
	public static String deliveryLocationForPackage(String packageID, MayamClient mayamClient, String baseTXDeliveryLocation, String aoDeliveryLocation, boolean isAO) throws MayamClientException{
	
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

		log.info(String.format("Returning delivery location %s for package %s", ret,packageID));
		
		return ret;
	}
	
}
