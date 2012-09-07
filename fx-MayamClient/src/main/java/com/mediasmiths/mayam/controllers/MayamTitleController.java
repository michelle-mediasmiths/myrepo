package com.mediasmiths.mayam.controllers;

import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import au.com.foxtel.cf.mam.pms.TitleDescriptionType;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.RemoteException;
import com.mediasmiths.foxtel.generated.MediaExchange.Programme;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MqClient;

public class MayamTitleController {
	private final TasksClient client;
	private final MqClient mq;
	
	public MayamTitleController(TasksClient mayamClient, MqClient mqClient) {
		client = mayamClient;
		mq = mqClient;
	}
	
	public MayamClientErrorCode createTitle(Programme.Detail title)
	{
		
		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	public MayamClientErrorCode createTitle(CreateOrUpdateTitle title)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		if (title != null)
		{
			TitleDescriptionType titleDescription = title.getTitleDescription();
			
			if (titleDescription != null) {
				final AttributeMap assetAttributes = client.createAttributeMap();
				
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
				
				assetAttributes.setAttribute(Attribute.ASSET_TYPE, AssetType.SER);
		
				assetAttributes.setAttribute(Attribute.ASSET_ID, title.getTitleID());	
				assetAttributes.setAttribute(Attribute.AUX_SRC, titleDescription.getShow());
				
				assetAttributes.setAttribute(Attribute.SERIES_TITLE, titleDescription.getProgrammeTitle());
				assetAttributes.setAttribute(Attribute.SEASON_NUMBER, titleDescription.getSeriesNumber());
				
				assetAttributes.setAttribute(Attribute.EPISODE_TITLE, titleDescription.getEpisodeTitle());
				assetAttributes.setAttribute(Attribute.EPISODE_NUMBER, titleDescription.getEpisodeNumber());
				
				assetAttributes.setAttribute(Attribute.AUX_ID, titleDescription.getProductionNumber());
				assetAttributes.setAttribute(Attribute.CONT_CATEGORY, titleDescription.getStyle());
				assetAttributes.setAttribute(Attribute.SERIES_YEAR, titleDescription.getYearOfProduction());
				assetAttributes.setAttribute(Attribute.AUX_VAL, titleDescription.getCountryOfProduction());
				
				assetAttributes.setAttribute(Attribute.APP_FLAG, title.isRestrictAccess());
				assetAttributes.setAttribute(Attribute.AUX_FLAG, title.isPurgeProtect());
				
				AttributeMap result;
				try {
					result = client.createAsset(assetAttributes);
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

	public MayamClientErrorCode updateTitle(Programme.Detail programme)
	{
		AttributeMap asset;
		try {
			//TODO: Determine id to be used from programme object
			asset = client.getAsset(AssetType.SER, programme.getTitle());
			
			asset.setAttribute(Attribute.SERIES_TITLE, programme.getTitle());
			client.updateAsset(asset);

		} catch (RemoteException e) {
			e.printStackTrace();
			e.printRemoteMessages(System.err);
		}
		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	//TODO: Refactor Asset AttributeMap creation (duplication with create function)
	public MayamClientErrorCode updateTitle(CreateOrUpdateTitle title)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		if (title != null) {
			TitleDescriptionType titleDescription = title.getTitleDescription();
			
			if (titleDescription != null) {
				AttributeMap assetAttributes = null;
				
				try {
					assetAttributes = client.getAsset(AssetType.SER, title.getTitleID());
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
				
				if (assetAttributes != null) {
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
					
					assetAttributes.setAttribute(Attribute.AUX_SRC, titleDescription.getShow());
					assetAttributes.setAttribute(Attribute.SERIES_TITLE, titleDescription.getProgrammeTitle());
					assetAttributes.setAttribute(Attribute.SEASON_NUMBER, titleDescription.getSeriesNumber());
					
					assetAttributes.setAttribute(Attribute.EPISODE_TITLE, titleDescription.getEpisodeTitle());
					assetAttributes.setAttribute(Attribute.EPISODE_NUMBER, titleDescription.getEpisodeNumber());
					
					assetAttributes.setAttribute(Attribute.AUX_ID, titleDescription.getProductionNumber());
					assetAttributes.setAttribute(Attribute.CONT_CATEGORY, titleDescription.getStyle());
					assetAttributes.setAttribute(Attribute.SERIES_YEAR, titleDescription.getYearOfProduction());
					assetAttributes.setAttribute(Attribute.AUX_VAL, titleDescription.getCountryOfProduction());
					
					assetAttributes.setAttribute(Attribute.APP_FLAG, title.isRestrictAccess());
					assetAttributes.setAttribute(Attribute.AUX_FLAG, title.isPurgeProtect());
					
					AttributeMap result;
					try {
						result = client.updateAsset(assetAttributes);
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
		//TODO: try to fetch title
		return true;
	}
}
