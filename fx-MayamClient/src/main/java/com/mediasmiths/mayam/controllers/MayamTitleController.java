package com.mediasmiths.mayam.controllers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.ChannelType;
import au.com.foxtel.cf.mam.pms.Channels;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.License;
import au.com.foxtel.cf.mam.pms.LicenseHolderType;
import au.com.foxtel.cf.mam.pms.LicensePeriodType;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import au.com.foxtel.cf.mam.pms.RightsType;
import au.com.foxtel.cf.mam.pms.TitleDescriptionType;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.GenericTable;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title.Distributor;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamTitleController extends MayamController{
	private final TasksClient client;
	private final static Logger log = Logger.getLogger(MayamMaterialController.class);
	
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
			attributesValid &= attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.TITLE.getAssetType());
			attributesValid &= attributes.setAttribute(Attribute.ASSET_ID, title.getTitleID());
			
			attributesValid &= attributes.setAttribute(Attribute.SERIES_TITLE, title.getProgrammeTitle());
			attributesValid &= attributes.setAttribute(Attribute.SEASON_NUMBER, title.getSeriesNumber());
			
			attributesValid &= attributes.setAttribute(Attribute.EPISODE_TITLE, title.getEpisodeTitle());
			attributesValid &= attributes.setAttribute(Attribute.EPISODE_NUMBER, title.getEpisodeNumber());
			
			attributesValid &= attributes.setAttribute(Attribute.AUX_ID, title.getProductionNumber());
			attributesValid &= attributes.setAttribute(Attribute.SERIES_YEAR, title.getYearOfProduction());
			attributesValid &= attributes.setAttribute(Attribute.AUX_VAL, title.getCountryOfProduction());
			
			//TODO: Rights attributes are not the ideal location for distributor values
			Distributor distributor = title.getDistributor();
			if (distributor != null) {
				attributesValid &= attributes.setAttribute(Attribute.RIGHTS_CODE, distributor.getDistributorID());
				attributesValid &= attributes.setAttribute(Attribute.RIGHTS_SUMMARY, title.getDistributor().getDistributorName());
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
				log.warn("Title created but one or more attributes were invalid");
				returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
			}
			
			AttributeMap result;
			try {
				result = client.assetApi().createAsset(attributes.getAttributes());
				if (result == null) {
					log.warn("Mayam failed to create new title asset");
					returnCode = MayamClientErrorCode.TITLE_CREATION_FAILED;
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				log.error("Exception thrown by Mayam while attempting to create new title asset");
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}
		}
		else {
			log.warn("Null title object, unable to create asset");
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
				RightsType titleRights = title.getRights();
				if (titleRights != null) {
					List<String> columnNames = new ArrayList<String>();
					columnNames.add("Organization ID");
					columnNames.add("Organization Name");
					columnNames.add("Start Date");
					columnNames.add("End Date");
					columnNames.add("Channel Name");
					columnNames.add("Channel Tag");
					
					GenericTable rightsTable = new GenericTable(columnNames);

					List<License> licenses = titleRights.getLicense();
					
					int rowCounter = 0;
					for (int i = 0; i < licenses.size(); i++) {
						License license = licenses.get(i);
						LicenseHolderType holder = license.getLicenseHolder();
						LicensePeriodType period = license.getLicensePeriod();
						
						Channels channels = license.getChannels();
						if (channels != null) {
							List<ChannelType> channelList = channels.getChannel();
							if (channelList != null) {
								for (int j = 0; j < channelList.size(); j++)
								{
									ChannelType channel = channelList.get(j);
	
									rightsTable.setCellValue(rowCounter, 0, holder.getOrganisationID());
									rightsTable.setCellValue(rowCounter, 1, holder.getOrganisationName());
									rightsTable.setCellValue(rowCounter, 2, period.getStartDate().toString());
									rightsTable.setCellValue(rowCounter, 3, period.getEndDate().toString());
									rightsTable.setCellValue(rowCounter, 4, channel.getChannelName());
									rightsTable.setCellValue(rowCounter, 5, channel.getChannelTag());
									
									rowCounter ++;
								}
							}
						}
					}
					attributesValid = attributesValid && attributes.setAttribute(Attribute.MEDIA_RIGHTS, rightsTable);
				}
				
				attributesValid &= attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.TITLE.getAssetType());
		
				attributesValid &= attributes.setAttribute(Attribute.ASSET_ID, title.getTitleID());	
				attributesValid &= attributes.setAttribute(Attribute.AUX_SRC, titleDescription.getShow());
				
				attributesValid &= attributes.setAttribute(Attribute.SERIES_TITLE, titleDescription.getProgrammeTitle());
				attributesValid &= attributes.setAttribute(Attribute.SEASON_NUMBER, titleDescription.getSeriesNumber());
				
				attributesValid &= attributes.setAttribute(Attribute.EPISODE_TITLE, titleDescription.getEpisodeTitle());
				attributesValid &= attributes.setAttribute(Attribute.EPISODE_NUMBER, titleDescription.getEpisodeNumber());
				
				attributesValid &= attributes.setAttribute(Attribute.AUX_ID, titleDescription.getProductionNumber());
				attributesValid &= attributes.setAttribute(Attribute.CONT_CATEGORY, titleDescription.getStyle());
				attributesValid &= attributes.setAttribute(Attribute.SERIES_YEAR, titleDescription.getYearOfProduction());
				attributesValid &= attributes.setAttribute(Attribute.AUX_VAL, titleDescription.getCountryOfProduction());
				
				attributesValid &= attributes.setAttribute(Attribute.APP_FLAG, title.isRestrictAccess());
				attributesValid &= attributes.setAttribute(Attribute.AUX_FLAG, title.isPurgeProtect());
				
				if (!attributesValid) {
					log.warn("Title created but one or more attributes were invalid");
					returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
				}
				
				AttributeMap result;
				try {
					result = client.assetApi().createAsset(attributes.getAttributes());
					if (result == null) {
						log.warn("Mayam failed to create new Title asset");
						returnCode = MayamClientErrorCode.TITLE_CREATION_FAILED;
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					log.error("Exception thrown by Mayam while creating new Title assset");
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
			}
			else {
				log.warn("Null title description, unable to create asset");
				returnCode = MayamClientErrorCode.TITLE_METADATA_UNAVAILABLE;
			}
		}
		else {
			log.warn("Null title object, unable to create asset");
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
					assetAttributes = client.assetApi().getAsset(MayamAssetType.TITLE.getAssetType(), title.getTitleID());
				} catch (RemoteException e1) {
					log.error("Exception thrown by Mayam while retrieving asset attributes for updated title : " + title.getTitleID());
					e1.printStackTrace();
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
				
				if (assetAttributes != null) {
					attributes = new MayamAttributeController(assetAttributes);
					
					attributesValid &= attributes.setAttribute(Attribute.SERIES_TITLE, title.getProgrammeTitle());
					attributesValid &= attributes.setAttribute(Attribute.SEASON_NUMBER, title.getSeriesNumber());
					
					attributesValid &= attributes.setAttribute(Attribute.EPISODE_TITLE, title.getEpisodeTitle());
					attributesValid &= attributes.setAttribute(Attribute.EPISODE_NUMBER, title.getEpisodeNumber());
					
					attributesValid &= attributes.setAttribute(Attribute.AUX_ID, title.getProductionNumber());
					attributesValid &= attributes.setAttribute(Attribute.SERIES_YEAR, title.getYearOfProduction());
					attributesValid &= attributes.setAttribute(Attribute.AUX_VAL, title.getCountryOfProduction());
					
					//TODO: Rights attributes are not the ideal location for distributor values
					Distributor distributor = title.getDistributor();
					if (distributor != null) {
						attributesValid &= attributes.setAttribute(Attribute.RIGHTS_CODE, distributor.getDistributorID());
						attributesValid &= attributes.setAttribute(Attribute.RIGHTS_SUMMARY, title.getDistributor().getDistributorName());
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
						result = client.assetApi().updateAsset(attributes.getAttributes());
						if (result == null) {
							returnCode = MayamClientErrorCode.TITLE_UPDATE_FAILED;
						}
					} catch (RemoteException e) {
						e.printStackTrace();
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
					assetAttributes = client.assetApi().getAsset(MayamAssetType.TITLE.getAssetType(), title.getTitleID());
				} catch (RemoteException e1) {
					e1.printStackTrace();
					log.error("Exception thrown by Mayam while retrieving asset: " + title.getTitleID());
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
				
				if (assetAttributes != null) {
					attributes = new MayamAttributeController(assetAttributes);
					
					RightsType titleRights = title.getRights();
					if (titleRights != null) {
						List<String> columnNames = new ArrayList<String>();
						columnNames.add("Organization ID");
						columnNames.add("Organization Name");
						columnNames.add("Start Date");
						columnNames.add("End Date");
						columnNames.add("Channel Name");
						columnNames.add("Channel Tag");
						
						GenericTable rightsTable = new GenericTable(columnNames);

						List<License> licenses = titleRights.getLicense();
						
						int rowCounter = 0;
						for (int i = 0; i < licenses.size(); i++) {
							License license = licenses.get(i);
							LicenseHolderType holder = license.getLicenseHolder();
							LicensePeriodType period = license.getLicensePeriod();
							
							Channels channels = license.getChannels();
							if (channels != null) {
								List<ChannelType> channelList = channels.getChannel();
								if (channelList != null) {
									for (int j = 0; j < channelList.size(); j++)
									{
										ChannelType channel = channelList.get(j);
		
										rightsTable.setCellValue(rowCounter, 0, holder.getOrganisationID());
										rightsTable.setCellValue(rowCounter, 1, holder.getOrganisationName());
										rightsTable.setCellValue(rowCounter, 2, period.getStartDate().toString());
										rightsTable.setCellValue(rowCounter, 3, period.getEndDate().toString());
										rightsTable.setCellValue(rowCounter, 4, channel.getChannelName());
										rightsTable.setCellValue(rowCounter, 5, channel.getChannelTag());
										
										rowCounter ++;
									}
								}
							}
						}
						attributesValid = attributesValid && attributes.setAttribute(Attribute.MEDIA_RIGHTS, rightsTable);
					}

					attributesValid &=attributes.setAttribute(Attribute.AUX_SRC, titleDescription.getShow());
					attributesValid &= attributes.setAttribute(Attribute.SERIES_TITLE, titleDescription.getProgrammeTitle());
					attributesValid &= attributes.setAttribute(Attribute.SEASON_NUMBER, titleDescription.getSeriesNumber());
					
					attributesValid &= attributes.setAttribute(Attribute.EPISODE_TITLE, titleDescription.getEpisodeTitle());
					attributesValid &= attributes.setAttribute(Attribute.EPISODE_NUMBER, titleDescription.getEpisodeNumber());
					
					attributesValid &= attributes.setAttribute(Attribute.AUX_ID, titleDescription.getProductionNumber());
					attributesValid &= attributes.setAttribute(Attribute.CONT_CATEGORY, titleDescription.getStyle());
					attributesValid &= attributes.setAttribute(Attribute.SERIES_YEAR, titleDescription.getYearOfProduction());
					attributesValid &= attributes.setAttribute(Attribute.AUX_VAL, titleDescription.getCountryOfProduction());
					
					attributesValid &= attributes.setAttribute(Attribute.APP_FLAG, title.isRestrictAccess());
					attributesValid &= attributes.setAttribute(Attribute.AUX_FLAG, title.isPurgeProtect());
					
					if (!attributesValid) {
						log.warn("Title updated but one or more attributes were invalid");
						returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
					}
					
					AttributeMap result;
					try {
						result = client.assetApi().updateAsset(attributes.getAttributes());
						if (result == null) {
							log.warn("Mayam failed to update title: " + title.getTitleID());
							returnCode = MayamClientErrorCode.TITLE_UPDATE_FAILED;
						}
					} catch (RemoteException e) {
						e.printStackTrace();
						log.error("Exception thrown by Mayam while updating title: " + title.getTitleID());
						returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
					}
				}
				else {
					log.warn("Mayam was unable to locate Title: " + title.getTitleID());
					returnCode = MayamClientErrorCode.TITLE_FIND_FAILED;	
				}
			}
			else {
				log.warn("Null title description, unable to update asset");
				returnCode = MayamClientErrorCode.TITLE_METADATA_UNAVAILABLE;
			}
		}
		else {
			log.warn("Null title object, unable to update asset");
			returnCode = MayamClientErrorCode.TITLE_UNAVAILABLE;	
		}
		return returnCode;
	}
	
	public MayamClientErrorCode purgeTitle(PurgeTitle title)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		if (title == null) {
			returnCode = MayamClientErrorCode.TITLE_UNAVAILABLE;
		}
		else {
			try {
				client.assetApi().deleteAsset(MayamAssetType.TITLE.getAssetType(), title.getTitleID());
			} catch (RemoteException e) {
				log.error("Error deleting title : "+ title.getTitleID());
				returnCode = MayamClientErrorCode.TITLE_DELETE_FAILED;
			}
		}
		return returnCode;
	}

	public boolean titleExists(String titleID) {
		boolean titleFound = false;
		try {
			AttributeMap assetAttributes = client.assetApi().getAsset(MayamAssetType.TITLE.getAssetType(), titleID);
			if (assetAttributes != null) {
				titleFound = true;
			}
		} catch (RemoteException e1) {
			log.error("Exception thrown by Mayam while attempting to retrieve asset :" + titleID);
			e1.printStackTrace();
		}
		return titleFound;
	}
	
	public AttributeMap getTitle(String titleID) {
		AttributeMap assetAttributes = null;
		try {
			assetAttributes = client.assetApi().getAsset(MayamAssetType.TITLE.getAssetType(), titleID);
		} catch (RemoteException e1) {
			log.error("Exception thrown by Mayam while attempting to retrieve asset :" + titleID);
			e1.printStackTrace();
		}
		return assetAttributes;
	}
}
