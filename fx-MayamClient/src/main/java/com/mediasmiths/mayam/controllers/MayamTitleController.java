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
import com.mayam.wf.attributes.shared.type.AudioTrack;
import com.mayam.wf.attributes.shared.type.AudioTrackList;
import com.mayam.wf.attributes.shared.type.GenericTable;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title.Distributor;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks.Track;
import com.mediasmiths.mayam.MayamAspectRatios;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamTaskListType;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamTitleController extends MayamController{
	private final TasksClient client;
	private final static Logger log = Logger.getLogger(MayamTitleController.class);
	
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
			attributesValid &= attributes.setAttribute(Attribute.HOUSE_ID, title.getTitleID());
			
			
			attributesValid &= attributes.setAttribute(Attribute.SERIES_TITLE, title.getProgrammeTitle()); 
			
			if(title.getSeriesNumber() != null)
			attributesValid &= attributes.setAttribute(Attribute.SEASON_NUMBER, title.getSeriesNumber().intValue());
			
			if(title.getEpisodeTitle() != null)
			attributesValid &= attributes.setAttribute(Attribute.EPISODE_TITLE, title.getEpisodeTitle());
			if(title.getEpisodeNumber() != null)
			attributesValid &= attributes.setAttribute(Attribute.EPISODE_NUMBER, title.getEpisodeNumber().intValue());
			
			attributesValid &= attributes.setAttribute(Attribute.PRODUCTION_NUMBER, title.getProductionNumber());
			
			if (title.getYearOfProduction() != null)
			attributesValid &= attributes.setAttribute(Attribute.SERIES_YEAR,title.getYearOfProduction().toString());
			attributesValid &= attributes.setAttribute(Attribute.LOCATION, title.getCountryOfProduction());
			
			//Foxtel and Mayam have agreed that Distributer ID does not need to be stored as Name is suitable enough
			Distributor distributor = title.getDistributor();
			if (distributor != null) {
				//attributesValid &= attributes.setAttribute(Attribute.RIGHTS_CODE, distributor.getDistributorID());
				attributesValid &= attributes.setAttribute(Attribute.DIST_NAME, title.getDistributor().getDistributorName());
			}
			
			MarketingMaterialType marketingMaterial = title.getMarketingMaterial();
			attributesValid &= attributes.setAttribute(Attribute.CONT_ASPECT_RATIO, MayamAspectRatios.mayamAspectRatioMappings.get(marketingMaterial.getAspectRatio()));
			
			//Feedback from Mayam and Foxtel - Duration and timecodes not required - will be detected by Ardome
			//marketingMaterial.getDuration();
			//marketingMaterial.getFirstFrameTimecode();
			//marketingMaterial.getLastFrameTimecode();
			
			attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, marketingMaterial.getFormat());
			
			AudioTracks audioTracks = marketingMaterial.getAudioTracks();
			List<Track> tracks = audioTracks.getTrack();
			AudioTrackList audioTrackList = new AudioTrackList();
			for (int i = 0; i < tracks.size(); i++) {
				AudioTrack audioTrack = new AudioTrack();
				Track track = tracks.get(i);
				audioTrack.setNumber(track.getTrackNumber());
				audioTrack.setName(track.getTrackName().toString());
				audioTrack.setEncoding(AudioTrack.EncodingType.valueOf(track.getTrackEncoding().toString()));
				audioTrackList.add(audioTrack);
			}
			attributesValid &= attributes.setAttribute(Attribute.AUDIO_TRACKS, audioTrackList); 
			
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
				log.error("Exception thrown by Mayam while attempting to create new title asset",e);
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
		
				attributesValid &= attributes.setAttribute(Attribute.HOUSE_ID, title.getTitleID());	
				attributesValid &= attributes.setAttribute(Attribute.SHOW, titleDescription.getShow());
				
				attributesValid &= attributes.setAttribute(Attribute.SERIES_TITLE, titleDescription.getProgrammeTitle());
				if(titleDescription.getSeriesNumber() != null)
				attributesValid &= attributes.setAttribute(Attribute.SEASON_NUMBER, titleDescription.getSeriesNumber().intValue());
				
				attributesValid &= attributes.setAttribute(Attribute.EPISODE_TITLE, titleDescription.getEpisodeTitle());
				if(titleDescription.getEpisodeNumber() != null)
				attributesValid &= attributes.setAttribute(Attribute.EPISODE_NUMBER, titleDescription.getEpisodeNumber().intValue());
				
				attributesValid &= attributes.setAttribute(Attribute.PRODUCTION_NUMBER, titleDescription.getProductionNumber());
				attributesValid &= attributes.setAttribute(Attribute.CONT_CATEGORY, titleDescription.getStyle());
				if(titleDescription.getYearOfProduction() != null)
				attributesValid &= attributes.setAttribute(Attribute.SERIES_YEAR, titleDescription.getYearOfProduction().toString());
				attributesValid &= attributes.setAttribute(Attribute.LOCATION, titleDescription.getCountryOfProduction());
				
				attributesValid &= attributes.setAttribute(Attribute.CONT_RESTRICTED_ACCESS, title.isRestrictAccess());
				attributesValid &= attributes.setAttribute(Attribute.PURGE_PROTECTED, title.isPurgeProtect());
				
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
					log.error("Exception thrown by Mayam while creating new Title assset",e);
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
					
					attributesValid &= attributes.setAttribute(Attribute.PRODUCTION_NUMBER, title.getProductionNumber());
					if( title.getYearOfProduction() != null)
					attributesValid &= attributes.setAttribute(Attribute.SERIES_YEAR, title.getYearOfProduction().toString());
					attributesValid &= attributes.setAttribute(Attribute.LOCATION, title.getCountryOfProduction());
					
					Distributor distributor = title.getDistributor();
					if (distributor != null) {
						//attributesValid &= attributes.setAttribute(Attribute.RIGHTS_CODE, distributor.getDistributorID());
						attributesValid &= attributes.setAttribute(Attribute.DIST_NAME, title.getDistributor().getDistributorName());
					}
					
					MarketingMaterialType marketingMaterial = title.getMarketingMaterial();
					attributesValid &= attributes.setAttribute(Attribute.CONT_ASPECT_RATIO, MayamAspectRatios.mayamAspectRatioMappings.get(marketingMaterial.getAspectRatio()));
					
					//Feedback from Mayam and Foxtel - Duration and timecodes not required - will be detected by Ardome
					//marketingMaterial.getDuration();
					//marketingMaterial.getFirstFrameTimecode();
					//marketingMaterial.getLastFrameTimecode();
					
					attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, marketingMaterial.getFormat());
					
					AudioTracks audioTracks = marketingMaterial.getAudioTracks();
					List<Track> tracks = audioTracks.getTrack();
					AudioTrackList audioTrackList = new AudioTrackList();
					for (int i = 0; i < tracks.size(); i++) {
						AudioTrack audioTrack = new AudioTrack();
						Track track = tracks.get(i);
						audioTrack.setNumber(track.getTrackNumber());
						audioTrack.setName(track.getTrackName().toString());
						audioTrack.setEncoding(AudioTrack.EncodingType.valueOf(track.getTrackEncoding().toString()));
						audioTrackList.add(audioTrack);
					}
					attributesValid &= attributes.setAttribute(Attribute.AUDIO_TRACKS, audioTrackList); 
					
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
						log.error("Exception thrown by Mayam while updating Title assset",e);
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

					attributesValid &=attributes.setAttribute(Attribute.SHOW, titleDescription.getShow());
					attributesValid &= attributes.setAttribute(Attribute.SERIES_TITLE, titleDescription.getProgrammeTitle());
					attributesValid &= attributes.setAttribute(Attribute.SEASON_NUMBER, titleDescription.getSeriesNumber());
					
					attributesValid &= attributes.setAttribute(Attribute.EPISODE_TITLE, titleDescription.getEpisodeTitle());
					attributesValid &= attributes.setAttribute(Attribute.EPISODE_NUMBER, titleDescription.getEpisodeNumber());
					
					attributesValid &= attributes.setAttribute(Attribute.PRODUCTION_NUMBER, titleDescription.getProductionNumber());
					attributesValid &= attributes.setAttribute(Attribute.CONT_CATEGORY, titleDescription.getStyle());
					if(titleDescription.getYearOfProduction() != null)
					attributesValid &= attributes.setAttribute(Attribute.SERIES_YEAR, titleDescription.getYearOfProduction().toString());
					attributesValid &= attributes.setAttribute(Attribute.LOCATION, titleDescription.getCountryOfProduction());
					
					attributesValid &= attributes.setAttribute(Attribute.CONT_RESTRICTED_ACCESS, title.isRestrictAccess());
					
					boolean isProtected = attributes.getAttributes().getAttribute(Attribute.AUX_FLAG);
					if (isProtected != title.isPurgeProtect()){
						attributesValid &= attributes.setAttribute(Attribute.PURGE_PROTECTED, title.isPurgeProtect());
						try {
							List<AttributeMap> materials = client.assetApi().getAssetChildren(MayamAssetType.TITLE.getAssetType(), title.getTitleID(), MayamAssetType.MATERIAL.getAssetType());
							List<AttributeMap> packages = client.assetApi().getAssetChildren(MayamAssetType.TITLE.getAssetType(), title.getTitleID(), MayamAssetType.PACKAGE.getAssetType());
							for (int i = 0; i < materials.size(); i++) {
								AttributeMap material = materials.get(i);
								material.setAttribute(Attribute.PURGE_PROTECTED, title.isPurgeProtect());
								client.assetApi().updateAsset(material);
							}
							for (int i = 0; i < packages.size(); i++) {
								AttributeMap packageMap = packages.get(i);
								packageMap.setAttribute(Attribute.PURGE_PROTECTED, title.isPurgeProtect());
								client.assetApi().updateAsset(packageMap);
							}
						} catch (RemoteException e) {
							log.error("Exception thrown by Mayam while retrieving child assets of title : " + title.getTitleID(),e);
						}
					}
					
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
						log.error("Exception thrown by Mayam while updating title: " + title.getTitleID(),e);
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
		else if (isProtected(title.getTitleID())) 
		{
			try
			{
				AttributeMap assetAttributes = client.assetApi().getAsset(MayamAssetType.TITLE.getAssetType(), title.getTitleID());
				
				AttributeMap taskAttributes = client.createAttributeMap();
				taskAttributes.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PURGE_BY_BMS);
				taskAttributes.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
	
				taskAttributes.setAttribute(Attribute.HOUSE_ID, title.getTitleID());
				taskAttributes.putAll(assetAttributes);
				client.taskApi().createTask(taskAttributes);
			}
			catch (RemoteException e)
			{
				log.error("Error creating Purge By BMS task for protected material : " + title.getTitleID());
				returnCode = MayamClientErrorCode.MATERIAL_DELETE_FAILED;
			}
		}
		else {
			try {
				client.assetApi().deleteAsset(MayamAssetType.TITLE.getAssetType(), title.getTitleID());
			} catch (RemoteException e) {
				log.error("Error deleting title : "+ title.getTitleID(),e);
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
			log.error("Exception thrown by Mayam while attempting to retrieve asset :" + titleID,e1);
		}
		return titleFound;
	}
	
	public AttributeMap getTitle(String titleID) {
		AttributeMap assetAttributes = null;
		try {
			assetAttributes = client.assetApi().getAsset(MayamAssetType.TITLE.getAssetType(), titleID);
		} catch (RemoteException e1) {
			log.error("Exception thrown by Mayam while attempting to retrieve asset :" + titleID,e1);
		}
		return assetAttributes;
	}
	
	public boolean isProtected(String titleID)
	{
		boolean isProtected = false;
		AttributeMap title;
		try {
			title = client.assetApi().getAsset(MayamAssetType.TITLE.getAssetType(), titleID);
			if (title != null) {
				isProtected = title.getAttribute(Attribute.PURGE_PROTECTED);
			}
		} catch (RemoteException e) {
			log.error("Exception thrown by Mayam while checking Protected status of Title : " + titleID,e);
		}
		return isProtected;
	}

}
