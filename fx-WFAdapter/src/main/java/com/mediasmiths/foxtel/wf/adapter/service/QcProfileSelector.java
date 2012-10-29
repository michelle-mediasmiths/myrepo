package com.mediasmiths.foxtel.wf.adapter.service;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;

public class QcProfileSelector
{

	@Inject
	private MayamClient mayamClient;

	@Inject @Named("qc.profile.incoming.sd.stereo")
	private String incomingSDStereo;
	@Inject @Named("qc.profile.incoming.sd.surround")
	private String incomingSDSurround;
	@Inject @Named("qc.profile.incoming.hd.stereo")
	private String incomingHDStereo;
	@Inject @Named("qc.profile.incoming.hd.surround")
	private String incomingHDSurround;

	@Inject @Named("qc.profile.tx.sd.stereo")
	private String txSDStereo;
	@Inject @Named("qc.profile.tx.sd.surround")
	private String txSDSurround;
	@Inject @Named("qc.profile.tx.hd.stereo")
	private String txHDStereo;
	@Inject @Named("qc.profile.tx.hd.surround")
	private String txHDSurround;
	
	private static final Logger log = Logger.getLogger(QcProfileSelector.class);
	
	public String getProfileFor(String assetID, boolean isForTXDelivery) throws MayamClientException
	{

		log.debug(String.format("Getting QC Profile for asset %s tx delivery %b",assetID,isForTXDelivery));
		String profile;
		
		if (isForTXDelivery)
		{
			profile = getProfileForPackage(assetID);
		}
		else
		{
			profile = getProfileForMaterial(assetID);
		}
		
		log.info(String.format("Returning QC Profile %s for asset %s tx delivery %b",profile,assetID,isForTXDelivery));
		return profile;

	}

	private String getProfileForMaterial(String materialID) throws MayamClientException
	{
		MaterialType material = mayamClient.getMaterial(materialID);
		return getProfileForMaterial(material);
	}

	private String getProfileForMaterial(MaterialType material)
	{
		boolean isMaterialSD = (material.getRequiredFormat().equals("SD"));
		boolean isDolbyE = (false); // TODO replace once we can store retrieve audio track information in mayam

		final String profile;

		if (isMaterialSD)
		{
			if (isDolbyE)
			{
				profile = incomingSDSurround;
			}
			else
			{
				profile = incomingSDStereo;
			}
		}
		else
		{
			if (isDolbyE)
			{
				profile = incomingHDSurround;
			}
			else
			{
				profile = incomingHDStereo;
			}
		}
		return profile;
	}

	private String getProfileForPackage(String packageID) throws MayamClientException
	{
		PackageType pack = mayamClient.getPackage(packageID);
		return getProfileForPackage(pack);
	}

	private String getProfileForPackage(PackageType pack)
	{
		boolean isPackageSD = (pack.getPresentationFormat() == PresentationFormatType.SD);
		boolean isDolbyE = (false); // TODO replace once we can store retrieve audio track information in mayam

		final String profile;

		if (isPackageSD)
		{
			if (isDolbyE)
			{
				profile = txSDSurround;
			}
			else
			{
				profile = txSDStereo;
			}
		}
		else
		{
			if (isDolbyE)
			{
				profile = txHDSurround;
			}
			else
			{
				profile = txHDStereo;
			}
		}
		return profile;
	}

}
