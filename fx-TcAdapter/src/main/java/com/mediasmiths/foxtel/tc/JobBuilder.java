package com.mediasmiths.foxtel.tc;

import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

public class JobBuilder
{
	
	/**
	 * 
	 * 					Source				|      Options  |Profile
	 * video	audio						dolbyEe downres |	
	 *	SD		Stereo						No		n/a		MAM-SD-12ST_GXF-SD-12ST
	 *	SD		Stereo + Descrete Surround	Yes		n/a		MAM-SD-12ST_38SUR_GXF-SD-12ST-34DBE
	 *	HD		Stereo						No		No		MAM-HD-12ST_GXF-HD-12ST
	 *	HD		Stereo + Descrete Surround	Yes		No		MAM-SD-12ST_38SUR_GXF-HD-12ST-34DBE
	 *	HD		Stereo						No		Yes		MAM-HD-12ST_GXF-SD-12ST
	 *	HD		Stereo + Descrete Surround	Yes		Yes		MAM-SD-12ST_38SUR_GXF-SD-12ST-34DBE
	 * 
	 */

	@Inject
	private MayamClient mayamClient;
	
	public String buildJobForTranscode(String packageID) throws MayamClientException
	{
		//fetch package and material information to determine profile
		PackageType pack = mayamClient.getPackage(packageID);
		MaterialType material = mayamClient.getMaterial(pack.getMaterialID());
		
		boolean isMaterialSD = (material.getRequiredFormat().equals("SD"));
		boolean isMaterialHD = (material.getRequiredFormat().equals("HD"));
		
		
		
		return "";
	}

}
