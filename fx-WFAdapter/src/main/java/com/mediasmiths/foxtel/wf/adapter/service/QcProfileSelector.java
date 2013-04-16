package com.mediasmiths.foxtel.wf.adapter.service;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.util.AssetProperties;

public class QcProfileSelector
{

	@Inject
	private MayamClient mayamClient;

	@Inject @Named("qc.profile.incoming.sd.stereo")
	private String incomingSDStereo;
	@Inject @Named("qc.profile.incoming.sd.surround")
	private String incomingSDSurround;
	@Inject @Named("qc.profile.incoming.sd.internal")
	private String incomingSDInternal;
	@Inject @Named("qc.profile.incoming.hd.stereo")
	private String incomingHDStereo;
	@Inject @Named("qc.profile.incoming.hd.surround")
	private String incomingHDSurround;
	@Inject @Named("qc.profile.incoming.hd.internal")
	private String incomingHDInternal;
	@Inject @Named("qc.profile.incoming.sd.8audiotrack")
	private String dartSDInternal;
	@Inject @Named("qc.profile.incoming.hd.8audiotrack")
	private String dartHDInternal;

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
		
		AttributeMap materialAttributes = mayamClient.getMaterialAttributes(materialID);
		return getProfileForMaterial(materialAttributes);
	}

	private String getProfileForMaterial(AttributeMap materialAttributes)
	{
		boolean isMaterialSD = AssetProperties.isMaterialSD(materialAttributes);
		boolean isMaterialSurround = AssetProperties.isMaterialSurround(materialAttributes);
		boolean isFromDartOrVizCapture = AssetProperties.isFromDARTorVizCapture(materialAttributes);
		
		if (isFromDartOrVizCapture)
		{
			log.info("this content is from dart or vizcap, will apply the 8 audio trac profile.");
			
		}

		final String profile;

		if (isMaterialSD)
		{
			if (isFromDartOrVizCapture)
			{
				profile = dartSDInternal;
			}
			else if (isMaterialSurround)
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
			if (isFromDartOrVizCapture)
			{
				profile = dartHDInternal;
			}
			else if (isMaterialSurround)
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
		AttributeMap packageAttributes = mayamClient.getPackageAttributes(packageID);
		
		String materialID = packageAttributes.getAttributeAsString(Attribute.PARENT_HOUSE_ID);
		AttributeMap materialAttributes = mayamClient.getMaterialAttributes(materialID);
		
		return getProfileForPackage(packageAttributes,materialAttributes);
	}

	private String getProfileForPackage(AttributeMap packageAttributes, AttributeMap materialAttributes)
	{
		boolean isPackageSD = AssetProperties.isPackageSD(packageAttributes);
		boolean isDolbyE = AssetProperties.isMaterialSurround(materialAttributes);

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
