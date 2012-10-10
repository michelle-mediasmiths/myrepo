package com.mediasmiths.mayam.controllers;

import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import au.com.foxtel.cf.mam.pms.TitleDescriptionType;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title.Distributor;
import com.mediasmiths.mayam.MayamClientErrorCode;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamTitleController {
	private final TasksClient client;
	
	@Inject
	public MayamTitleController(@Named(SETUP_TASKS_CLIENT)TasksClient mayamClient) {
		client = mayamClient;
	}
	
	public MayamClientErrorCode createTitle(Material.Title title)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;
		
		if (title != null)
		{
			attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_TYPE, AssetType.SER);
			attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_GUID, title.getTitleID());
			
			attributesValid = attributesValid && attributes.setAttribute(Attribute.SERIES_TITLE, title.getProgrammeTitle());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.SEASON_NUMBER, title.getSeriesNumber());
			
			attributesValid = attributesValid && attributes.setAttribute(Attribute.EPISODE_TITLE, title.getEpisodeTitle());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.EPISODE_NUMBER, title.getEpisodeNumber());
			
			attributesValid = attributesValid && attributes.setAttribute(Attribute.AUX_ID, title.getProductionNumber());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.SERIES_YEAR, title.getYearOfProduction());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.AUX_VAL, title.getCountryOfProduction());
			
			//TODO: Rights attributes are not the ideal location for distributor values
			Distributor distributor = title.getDistributor();
			if (distributor != null) {
				attributesValid = attributesValid && attributes.setAttribute(Attribute.RIGHTS_CODE, distributor.getDistributorID());
				attributesValid = attributesValid && attributes.setAttribute(Attribute.RIGHTS_SUMMARY, title.getDistributor().getDistributorName());
			}
			
			//TODO: Is marketing material required in Title? Most of these attributes seem more in keeping with Material
/*			MarketingMaterialType marketingMaterial = title.getMarketingMaterial();
			Object additionalMarketingMaterial = marketingMaterial.getAdditionalMarketingDetail();
			marketingMaterial.getAspectRatio();
			marketingMaterial.getDuration();
			marketingMaterial.getFirstFrameTimecode();
			marketingMaterial.getFormat();
			marketingMaterial.getLastFrameTimecode();
			
			AudioTracks audioTracks = marketingMaterial.getAudioTracks();
			List<Track> tracks = audioTracks.getTrack();
			for (int i = 0; i < tracks.size(); i++) {
				Track track = tracks.get(i);
				track.getTrackNumber();
				track.getTrackName().toString();
				track.getTrackEncoding().toString();
			}*/
			
			if (!attributesValid) {
				returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
			}
			
			AttributeMap result;
			try {
				result = client.createAsset(attributes.getAttributes());
				if (result == null) {
					returnCode = MayamClientErrorCode.TITLE_CREATION_FAILED;
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				e.printRemoteMessages(System.err);
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}
		}
		else {
			returnCode = MayamClientErrorCode.TITLE_UNAVAILABLE;
		}
		return returnCode;
	}
	
	public MayamClientErrorCode createTitle(CreateOrUpdateTitle title)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;
		
		if (title != null)
		{
			TitleDescriptionType titleDescription = title.getTitleDescription();
			
			if (titleDescription != null) {				
				//	TODO: Rights management - Mayam to add a new complex type to allow Rights attributes to be added
				//	RightsType titleRights = title.getRights();		
				//	List<License> licenses = titleRights.getLicense();
				//	License license = licenses.get(0);
				//	LicenseHolderType holder = license.getLicenseHolder();
				//	holder.getOrganisationID();
				//	holder.getOrganisationName();
				//	LicensePeriodType period = license.getLicensePeriod();
				//	period.getEndDate();
				//	period.getStartDate();
				//	Channels channels = license.getChannels();
				//	List<ChannelType> channelList = channels.getChannel();
				//	ChannelType channel = channelList.get(0);
				//	channel.getChannelName();
				//	channel.getChannelTag();
				
				attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_TYPE, AssetType.SER);
		
				attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_ID, title.getTitleID());	
				attributesValid = attributesValid && attributes.setAttribute(Attribute.AUX_SRC, titleDescription.getShow());
				
				attributesValid = attributesValid && attributes.setAttribute(Attribute.SERIES_TITLE, titleDescription.getProgrammeTitle());
				attributesValid = attributesValid && attributes.setAttribute(Attribute.SEASON_NUMBER, titleDescription.getSeriesNumber());
				
				attributesValid = attributesValid && attributes.setAttribute(Attribute.EPISODE_TITLE, titleDescription.getEpisodeTitle());
				attributesValid = attributesValid && attributes.setAttribute(Attribute.EPISODE_NUMBER, titleDescription.getEpisodeNumber());
				
				attributesValid = attributesValid && attributes.setAttribute(Attribute.AUX_ID, titleDescription.getProductionNumber());
				attributesValid = attributesValid && attributes.setAttribute(Attribute.CONT_CATEGORY, titleDescription.getStyle());
				attributesValid = attributesValid && attributes.setAttribute(Attribute.SERIES_YEAR, titleDescription.getYearOfProduction());
				attributesValid = attributesValid && attributes.setAttribute(Attribute.AUX_VAL, titleDescription.getCountryOfProduction());
				
				attributesValid = attributesValid && attributes.setAttribute(Attribute.APP_FLAG, title.isRestrictAccess());
				attributesValid = attributesValid && attributes.setAttribute(Attribute.AUX_FLAG, title.isPurgeProtect());
				
				if (!attributesValid) {
					returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
				}
				
				AttributeMap result;
				try {
					result = client.createAsset(attributes.getAttributes());
					if (result == null) {
						returnCode = MayamClientErrorCode.TITLE_CREATION_FAILED;
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					e.printRemoteMessages(System.err);
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
			}
			else {
				returnCode = MayamClientErrorCode.TITLE_METADATA_UNAVAILABLE;
			}
		}
		else {
			returnCode = MayamClientErrorCode.TITLE_UNAVAILABLE;
		}
		return returnCode;
	}

	public MayamClientErrorCode updateTitle(Material.Title title)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		boolean attributesValid = true;
		
		if (title != null) {
				AttributeMap assetAttributes = null;
				MayamAttributeController attributes = null;
				
				try {
					assetAttributes = client.getAsset(AssetType.SER, title.getTitleID());
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
				
				if (assetAttributes != null) {
					attributes = new MayamAttributeController(assetAttributes);
					
					attributesValid = attributesValid && attributes.setAttribute(Attribute.SERIES_TITLE, title.getProgrammeTitle());
					attributesValid = attributesValid && attributes.setAttribute(Attribute.SEASON_NUMBER, title.getSeriesNumber());
					
					attributesValid = attributesValid && attributes.setAttribute(Attribute.EPISODE_TITLE, title.getEpisodeTitle());
					attributesValid = attributesValid && attributes.setAttribute(Attribute.EPISODE_NUMBER, title.getEpisodeNumber());
					
					attributesValid = attributesValid && attributes.setAttribute(Attribute.AUX_ID, title.getProductionNumber());
					attributesValid = attributesValid && attributes.setAttribute(Attribute.SERIES_YEAR, title.getYearOfProduction());
					attributesValid = attributesValid && attributes.setAttribute(Attribute.AUX_VAL, title.getCountryOfProduction());
					
					//TODO: Rights attributes are not the ideal location for distributor values
					Distributor distributor = title.getDistributor();
					if (distributor != null) {
						attributesValid = attributesValid && attributes.setAttribute(Attribute.RIGHTS_CODE, distributor.getDistributorID());
						attributesValid = attributesValid && attributes.setAttribute(Attribute.RIGHTS_SUMMARY, title.getDistributor().getDistributorName());
					}
					
					//TODO: Is marketing material required in Title? Most of these attributes seem more in keeping with Material
		/*			MarketingMaterialType marketingMaterial = title.getMarketingMaterial();
					Object additionalMarketingMaterial = marketingMaterial.getAdditionalMarketingDetail();
					marketingMaterial.getAspectRatio();
					marketingMaterial.getDuration();
					marketingMaterial.getFirstFrameTimecode();
					marketingMaterial.getFormat();
					marketingMaterial.getLastFrameTimecode();
					
					AudioTracks audioTracks = marketingMaterial.getAudioTracks();
					List<Track> tracks = audioTracks.getTrack();
					for (int i = 0; i < tracks.size(); i++) {
						Track track = tracks.get(i);
						track.getTrackNumber();
						track.getTrackName().toString();
						track.getTrackEncoding().toString();
					}*/
					
					if (!attributesValid) {
						returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
					}
					
					AttributeMap result;
					try {
						result = client.updateAsset(attributes.getAttributes());
						if (result == null) {
							returnCode = MayamClientErrorCode.TITLE_UPDATE_FAILED;
						}
					} catch (RemoteException e) {
						e.printStackTrace();
						e.printRemoteMessages(System.err);
						returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
					}
				}
				else {
					returnCode = MayamClientErrorCode.TITLE_FIND_FAILED;	
				}
			
		}
		else {
			returnCode = MayamClientErrorCode.TITLE_UNAVAILABLE;	
		}
		return returnCode;
	}
	
	//TODO: Refactor Asset AttributeMap creation (duplication with create function)
	public MayamClientErrorCode updateTitle(CreateOrUpdateTitle title)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		boolean attributesValid = true;
		
		if (title != null) {
			TitleDescriptionType titleDescription = title.getTitleDescription();
			
			if (titleDescription != null) {
				AttributeMap assetAttributes = null;
				MayamAttributeController attributes = null;
				
				try {
					assetAttributes = client.getAsset(AssetType.SER, title.getTitleID());
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
				
				if (assetAttributes != null) {
					attributes = new MayamAttributeController(assetAttributes);
					
					//	TODO: Rights management - Mayam to add a new complex type to allow Rights attributes to be added
					//	RightsType titleRights = title.getRights();		
					//	List<License> licenses = titleRights.getLicense();
					//	License license = licenses.get(0);
					//	LicenseHolderType holder = license.getLicenseHolder();
					//	holder.getOrganisationID();
					//	holder.getOrganisationName();
					//	LicensePeriodType period = license.getLicensePeriod();
					//	period.getEndDate();
					//	period.getStartDate();
					//	Channels channels = license.getChannels();
					//	List<ChannelType> channelList = channels.getChannel();
					//	ChannelType channel = channelList.get(0);
					//	channel.getChannelName();
					//	channel.getChannelTag();
					
					attributesValid = attributesValid && attributes.setAttribute(Attribute.AUX_SRC, titleDescription.getShow());
					attributesValid = attributesValid && attributes.setAttribute(Attribute.SERIES_TITLE, titleDescription.getProgrammeTitle());
					attributesValid = attributesValid && attributes.setAttribute(Attribute.SEASON_NUMBER, titleDescription.getSeriesNumber());
					
					attributesValid = attributesValid && attributes.setAttribute(Attribute.EPISODE_TITLE, titleDescription.getEpisodeTitle());
					attributesValid = attributesValid && attributes.setAttribute(Attribute.EPISODE_NUMBER, titleDescription.getEpisodeNumber());
					
					attributesValid = attributesValid && attributes.setAttribute(Attribute.AUX_ID, titleDescription.getProductionNumber());
					attributesValid = attributesValid && attributes.setAttribute(Attribute.CONT_CATEGORY, titleDescription.getStyle());
					attributesValid = attributesValid && attributes.setAttribute(Attribute.SERIES_YEAR, titleDescription.getYearOfProduction());
					attributesValid = attributesValid && attributes.setAttribute(Attribute.AUX_VAL, titleDescription.getCountryOfProduction());
					
					attributesValid = attributesValid && attributes.setAttribute(Attribute.APP_FLAG, title.isRestrictAccess());
					attributesValid = attributesValid && attributes.setAttribute(Attribute.AUX_FLAG, title.isPurgeProtect());
					
					if (!attributesValid) {
						returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
					}
					
					AttributeMap result;
					try {
						result = client.updateAsset(attributes.getAttributes());
						if (result == null) {
							returnCode = MayamClientErrorCode.TITLE_UPDATE_FAILED;
						}
					} catch (RemoteException e) {
						e.printStackTrace();
						e.printRemoteMessages(System.err);
						returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
					}
				}
				else {
					returnCode = MayamClientErrorCode.TITLE_FIND_FAILED;	
				}
			}
			else {
				returnCode = MayamClientErrorCode.TITLE_METADATA_UNAVAILABLE;
			}
		}
		else {
			returnCode = MayamClientErrorCode.TITLE_UNAVAILABLE;	
		}
		return returnCode;
	}
	
	public MayamClientErrorCode purgeTitle(PurgeTitle title)
	{
		//TODO: How to delete an asset in Mayam?
		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}

	public boolean titleExists(String titleID) {
		boolean titleFound = false;
		try {
			AttributeMap assetAttributes = client.getAsset(AssetType.SER, titleID);
			if (assetAttributes != null) {
				titleFound = true;
			}
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return titleFound;
	}
	
	public AttributeMap getTitle(String titleID) {
		AttributeMap assetAttributes = null;
		try {
			assetAttributes = client.getAsset(AssetType.SER, titleID);
		} catch (RemoteException e1) {
			//TODO: Error Handling
			e1.printStackTrace();
		}
		return assetAttributes;
	}
}
