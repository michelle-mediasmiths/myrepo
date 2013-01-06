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
import com.mayam.wf.attributes.shared.type.StringList;
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
import com.mediasmiths.mayam.MayamAudioEncoding;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamTaskListType;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamTitleController extends MayamController{
	private static final String TITLE_AGL_NAME = "episode";
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
		
		if (title != null && title.getTitleID() != null && !title.getTitleID().equals(""))
		{
			attributesValid &= attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.TITLE.getAssetType());
			attributesValid &= attributes.setAttribute(Attribute.METADATA_FORM, TITLE_AGL_NAME);
			attributesValid &= attributes.setAttribute(Attribute.HOUSE_ID, title.getTitleID());
			
			attributesValid &= attributes.setAttribute(Attribute.SERIES_TITLE, title.getProgrammeTitle()); 
			
			if(title.getSeriesNumber() != null)
			attributesValid &= attributes.setAttribute(Attribute.SEASON_NUMBER, title.getSeriesNumber().intValue());
			
			if(title.getEpisodeTitle() != null)
			attributesValid &= attributes.setAttribute(Attribute.EPISODE_TITLE, title.getEpisodeTitle());
			if(title.getEpisodeNumber() != null)
			attributesValid &= attributes.setAttribute(Attribute.EPISODE_NUMBER, title.getEpisodeNumber().intValue());
			
			attributesValid &= attributes.setAttribute(Attribute.PRODUCTION_NUMBER, title.getProductionNumber());
			
			String assetTitle = title.getProgrammeTitle() + " - " + title.getSeriesNumber() + " - " + title.getEpisodeNumber() + " - " + title.getEpisodeTitle();
			attributesValid &= attributes.setAttribute(Attribute.ASSET_TITLE, assetTitle);
			
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
			if (marketingMaterial != null)
			{
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
					audioTrack.setEncoding(AudioTrack.EncodingType.valueOf(MayamAudioEncoding.mayamAudioEncodings.get(track.getTrackEncoding().toString())));
					audioTrackList.add(audioTrack);
				}
				attributesValid &= attributes.setAttribute(Attribute.AUDIO_TRACKS, audioTrackList); 
			}
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
			log.warn("Null title object or no title Id available, unable to create asset");
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
				
				StringList channelStringList = new StringList();
				
				if (titleRights != null) {
					List<String> columnNames = new ArrayList<String>();
					columnNames.add("Organization ID");
					columnNames.add("Organization Name");
					columnNames.add("Start Date");
					columnNames.add("End Date");
					columnNames.add("Channel Name");
					
					GenericTable rightsTable = new GenericTable(columnNames);

					List<License> licenses = titleRights.getLicense();
					
					int rowCounter = 0;
					for(License license : licenses){
						LicenseHolderType holder = license.getLicenseHolder();
						LicensePeriodType period = license.getLicensePeriod();
						
						Channels channels = license.getChannels();
						if (channels != null) {
							List<ChannelType> channelList = channels.getChannel();
							if (channelList != null) {
								for(ChannelType channel : channelList)
								{
									rightsTable.setCellValue(rowCounter, 0, holder.getOrganisationID());
									rightsTable.setCellValue(rowCounter, 1, holder.getOrganisationName());
									rightsTable.setCellValue(rowCounter, 2, period.getStartDate().toString());
									rightsTable.setCellValue(rowCounter, 3, period.getEndDate().toString());
									rightsTable.setCellValue(rowCounter, 4, channel.getChannelName());
									String channelTag = channel.getChannelTag();
									channelStringList.add(channelTag);
									rowCounter ++;
								}
							}
						}
					}
					attributesValid = attributesValid && attributes.setAttribute(Attribute.MEDIA_RIGHTS, rightsTable);
					attributesValid = attributesValid && attributes.setAttribute(Attribute.CHANNELS, channelStringList);
				}
				
				attributesValid &= attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.TITLE.getAssetType());
				attributesValid &= attributes.setAttribute(Attribute.METADATA_FORM, TITLE_AGL_NAME);
				
				attributesValid &= attributes.setAttribute(Attribute.HOUSE_ID, title.getTitleID());	
				attributesValid &= attributes.setAttribute(Attribute.SHOW, titleDescription.getShow());
				
				attributesValid &= attributes.setAttribute(Attribute.SERIES_TITLE, titleDescription.getProgrammeTitle());
				if(titleDescription.getSeriesNumber() != null)
				attributesValid &= attributes.setAttribute(Attribute.SEASON_NUMBER, titleDescription.getSeriesNumber().intValue());
				
				attributesValid &= attributes.setAttribute(Attribute.EPISODE_TITLE, titleDescription.getEpisodeTitle());
				if(titleDescription.getEpisodeNumber() != null)
				attributesValid &= attributes.setAttribute(Attribute.EPISODE_NUMBER, titleDescription.getEpisodeNumber().intValue());
				
				String assetTitle = titleDescription.getProgrammeTitle() + " - " + titleDescription.getSeriesNumber() + " - " + titleDescription.getEpisodeNumber() + " - " + titleDescription.getEpisodeTitle();
				attributesValid &= attributes.setAttribute(Attribute.ASSET_TITLE, assetTitle);
				
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
					} else {
						log.debug("asset id is " + result.getAttributeAsString(Attribute.ASSET_ID));
						
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
					assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), title.getTitleID());
				} catch (RemoteException e1) {
					log.error("Exception thrown by Mayam while retrieving asset attributes for updated title : " + title.getTitleID());
					e1.printStackTrace();
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
				
				if (assetAttributes != null) {
					attributes = new MayamAttributeController(client.createAttributeMap());
					attributes.setAttribute(Attribute.ASSET_ID, assetAttributes.getAttribute(Attribute.ASSET_ID));
					attributes.setAttribute(Attribute.ASSET_TYPE, assetAttributes.getAttribute(Attribute.ASSET_TYPE));
					
					attributesValid &= attributes.setAttribute(Attribute.SERIES_TITLE, title.getProgrammeTitle());
					if(title.getSeriesNumber() != null)
					attributesValid &= attributes.setAttribute(Attribute.SEASON_NUMBER, title.getSeriesNumber().intValue());
					
					attributesValid &= attributes.setAttribute(Attribute.EPISODE_TITLE, title.getEpisodeTitle());
					if(title.getEpisodeNumber() != null)
					attributesValid &= attributes.setAttribute(Attribute.EPISODE_NUMBER, title.getEpisodeNumber().intValue());
					
					String assetTitle = title.getProgrammeTitle() + " - " + title.getSeriesNumber() + " - " + title.getEpisodeNumber() + " - " + title.getEpisodeTitle();
					attributesValid &= attributes.setAttribute(Attribute.ASSET_TITLE, assetTitle);
					
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
					if (marketingMaterial != null) 
					{
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
							audioTrack.setEncoding(AudioTrack.EncodingType.valueOf(MayamAudioEncoding.mayamAudioEncodings.get(track.getTrackEncoding().toString())));
							audioTrackList.add(audioTrack);
						}
						attributesValid &= attributes.setAttribute(Attribute.AUDIO_TRACKS, audioTrackList); 
					}
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
		log.debug("Updating title");
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		boolean attributesValid = true;
		
		if (title != null) {
			TitleDescriptionType titleDescription = title.getTitleDescription();
			
			if (titleDescription != null) {
				AttributeMap assetAttributes = null;
				MayamAttributeController attributes = null;
				log.debug("title ID is " + title.getTitleID());
				try {
					assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), title.getTitleID());
					log.debug("getting attributes for updating ...");
				} catch (RemoteException e1) {
					e1.printStackTrace();
					log.error("Exception thrown by Mayam while retrieving asset: " + title.getTitleID());
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
				
				if (assetAttributes != null) {
					
					final String assetID = assetAttributes.getAttribute(Attribute.ASSET_ID); 
					
					attributes = new MayamAttributeController(client.createAttributeMap());
					attributes.setAttribute(Attribute.ASSET_ID, assetAttributes.getAttribute(Attribute.ASSET_ID));
					attributes.setAttribute(Attribute.ASSET_TYPE, assetAttributes.getAttribute(Attribute.ASSET_TYPE));
					
					RightsType titleRights = title.getRights();
					if (titleRights != null) {
						List<String> columnNames = new ArrayList<String>();
						columnNames.add("Organization ID");
						columnNames.add("Organization Name");
						columnNames.add("Start Date");
						columnNames.add("End Date");
						columnNames.add("Channel Name");
							
						GenericTable rightsTable = new GenericTable(columnNames);
						StringList channelStringList = new StringList();
						List<License> licenses = titleRights.getLicense();
						
						int rowCounter = 0;
						for(License license : licenses){
							LicenseHolderType holder = license.getLicenseHolder();
							LicensePeriodType period = license.getLicensePeriod();
							
							Channels channels = license.getChannels();
							if (channels != null) {
								List<ChannelType> channelList = channels.getChannel();
								if (channelList != null) {
									for(ChannelType channel : channelList)
									{
										rightsTable.setCellValue(rowCounter, 0, holder.getOrganisationID());
										rightsTable.setCellValue(rowCounter, 1, holder.getOrganisationName());
										rightsTable.setCellValue(rowCounter, 2, period.getStartDate().toString());
										rightsTable.setCellValue(rowCounter, 3, period.getEndDate().toString());
										rightsTable.setCellValue(rowCounter, 4, channel.getChannelName());
										
										String channelTag = channel.getChannelTag();
										channelStringList.add(channelTag);
										
										rowCounter ++;
									}
								}
							}
						}
						attributesValid = attributesValid && attributes.setAttribute(Attribute.MEDIA_RIGHTS, rightsTable);
						attributesValid = attributesValid && attributes.setAttribute(Attribute.CHANNELS, channelStringList);
					}

					attributesValid &=attributes.setAttribute(Attribute.SHOW, titleDescription.getShow());
					attributesValid &= attributes.setAttribute(Attribute.SERIES_TITLE, titleDescription.getProgrammeTitle());
					if(titleDescription.getSeriesNumber() != null)
					attributesValid &= attributes.setAttribute(Attribute.SEASON_NUMBER, titleDescription.getSeriesNumber().intValue());
					
					attributesValid &= attributes.setAttribute(Attribute.EPISODE_TITLE, titleDescription.getEpisodeTitle());
					if(titleDescription.getEpisodeNumber() != null)
					attributesValid &= attributes.setAttribute(Attribute.EPISODE_NUMBER, titleDescription.getEpisodeNumber().intValue());
					
					String assetTitle = titleDescription.getProgrammeTitle() + " - " + titleDescription.getSeriesNumber() + " - " + titleDescription.getEpisodeNumber() + " - " + titleDescription.getEpisodeTitle();
					attributesValid &= attributes.setAttribute(Attribute.ASSET_TITLE, assetTitle);
					
					attributesValid &= attributes.setAttribute(Attribute.PRODUCTION_NUMBER, titleDescription.getProductionNumber());
					attributesValid &= attributes.setAttribute(Attribute.CONT_CATEGORY, titleDescription.getStyle());
					if(titleDescription.getYearOfProduction() != null)
					attributesValid &= attributes.setAttribute(Attribute.SERIES_YEAR, titleDescription.getYearOfProduction().toString());
					attributesValid &= attributes.setAttribute(Attribute.LOCATION, titleDescription.getCountryOfProduction());
					
					attributesValid &= attributes.setAttribute(Attribute.CONT_RESTRICTED_ACCESS, title.isRestrictAccess());
					
					boolean isProtected =false;
					log.debug("updating protected attribute ...");
					if(attributes.getAttributes().getAttribute(Attribute.AUX_FLAG) != null){
						 isProtected = attributes.getAttributes().getAttribute(Attribute.AUX_FLAG);
					}
					
					boolean titleIsPurgeProtected = false;
					
					if(title.isPurgeProtect() != null){
						titleIsPurgeProtected = title.isPurgeProtect().booleanValue();
 					}
					
					if (isProtected != titleIsPurgeProtected){
						attributesValid &= attributes.setAttribute(Attribute.PURGE_PROTECTED, title.isPurgeProtect());
						try {
							List<AttributeMap> materials = client.assetApi().getAssetChildren(MayamAssetType.TITLE.getAssetType(), assetID, MayamAssetType.MATERIAL.getAssetType());
							List<AttributeMap> packages = client.assetApi().getAssetChildren(MayamAssetType.TITLE.getAssetType(), assetID, MayamAssetType.PACKAGE.getAssetType());
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
				AttributeMap assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), title.getTitleID());
				
				AttributeMap taskAttributes = client.createAttributeMap();
				taskAttributes.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.GENERIC_TASK_ERROR);
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
				//fetch asset by house id to get its assetid
				AttributeMap assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), title.getTitleID());
				String assetID = assetAttributes.getAttribute(Attribute.ASSET_ID);
				
				client.assetApi().deleteAsset(MayamAssetType.TITLE.getAssetType(), assetID);
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
			AttributeMap assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), titleID);
			if (assetAttributes != null) {
				titleFound = true;
			}
		} catch (RemoteException e1) {
			log.info("Exception thrown by Mayam while attempting to retrieve asset" + titleID+" , assuming it doesnt exist");
			log.trace(e1);
		}
		return titleFound;
	}
	
	public AttributeMap getTitle(String titleID) {
		AttributeMap assetAttributes = null;
		try {
			assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), titleID);
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
			title = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), titleID);
			if (title != null) {
				isProtected = title.getAttribute(Attribute.PURGE_PROTECTED);
			}
		} catch (RemoteException e) {
			log.error("Exception thrown by Mayam while checking Protected status of Title : " + titleID,e);
		}
		return isProtected;
	}

}
