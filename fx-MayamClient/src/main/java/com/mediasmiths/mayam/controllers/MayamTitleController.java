package com.mediasmiths.mayam.controllers;

import au.com.foxtel.cf.mam.pms.ChannelType;
import au.com.foxtel.cf.mam.pms.Channels;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.License;
import au.com.foxtel.cf.mam.pms.LicenseHolderType;
import au.com.foxtel.cf.mam.pms.LicensePeriodType;
import au.com.foxtel.cf.mam.pms.RightsType;
import au.com.foxtel.cf.mam.pms.TitleDescriptionType;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AudioTrack;
import com.mayam.wf.attributes.shared.type.AudioTrackList;
import com.mayam.wf.attributes.shared.type.GenericTable;
import com.mayam.wf.attributes.shared.type.GenericTable.Row;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title.Distributor;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks.Track;
import com.mediasmiths.mayam.DateUtil;
import com.mediasmiths.mayam.MayamAspectRatios;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamAudioEncoding;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.accessrights.MayamAccessRightsController;
import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamTitleController extends MayamController{
	private static final String TITLE_AGL_NAME = "episode";
	private final TasksClient client;
	private final static Logger log = Logger.getLogger(MayamTitleController.class);
	
	private final static String AO_CHANNEL_TAG = "AO";
	
	@Inject
	private DateUtil dateUtil;
	
	@Inject
	public MayamTitleController(@Named(SETUP_TASKS_CLIENT)TasksClient mayamClient) {
		client = mayamClient;
	}
	
	@Inject
	ChannelProperties channelProperties;
	
	@Inject
	MayamTaskController taskController;
	
	@Inject
	MayamMaterialController materialController;
	
	@Inject
	MayamPackageController packageController;
	
	@Inject
	private MayamAccessRightsController accessRightsController;
	
	public MayamClientErrorCode createTitle(Material.Title title)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;
		
		if (title != null && title.getTitleID() != null && !title.getTitleID().equals(""))
		{
			
			log.info(String.format("Creating title %s",title.getTitleID()));
			
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
			
			String assetTitle = buildAssetTitle(title.getProgrammeTitle(),title.getSeriesNumber(), title.getEpisodeNumber(),title.getEpisodeTitle());
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
			
			AttributeMap newAsset = null;
			try{
				newAsset = accessRightsController.updateAccessRights(attributes.getAttributes());
			}
			catch(Exception e){
				log.error("error determining access rights for new asset",e);
				newAsset = attributes.getAttributes();
			}
			
			AttributeMap result;
			try {
				result = client.assetApi().createAsset(newAsset);
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
			log.info(String.format("Creating title %s",title.getTitleID()));
			
			TitleDescriptionType titleDescription = title.getTitleDescription();
			
			if (titleDescription != null) {				
				RightsType titleRights = title.getRights();
				
				StringList channelStringList = new StringList();
				
				boolean ao = false;
				
				if (titleRights != null) {
					List<String> columnNames = new ArrayList<String>();
					columnNames.add("Organization ID");
					columnNames.add("Organization Name");
					columnNames.add("Start Date");
					columnNames.add("End Date");
					columnNames.add("Channel Name");
					
					GenericTable rightsTable = new GenericTable(columnNames);

					List<License> licenses = titleRights.getLicense();
					
					XMLGregorianCalendar earliestStart = null;
					XMLGregorianCalendar latestEnd = null;
					
					attributesValid = updateLicenceStartAndEnd(attributes, attributesValid, licenses, earliestStart, latestEnd);
						
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
									if (channelProperties.isTagValid(channel.getChannelTag()))
									{
										rightsTable.setCellValue(rowCounter, 0, holder.getOrganisationID());
										rightsTable.setCellValue(rowCounter, 1, holder.getOrganisationName());
										rightsTable.setCellValue(rowCounter, 2, period.getStartDate().toString());
										rightsTable.setCellValue(rowCounter, 3, period.getEndDate().toString());
										rightsTable.setCellValue(rowCounter, 4, channel.getChannelName());
										String channelTag = channel.getChannelTag();
										if (AO_CHANNEL_TAG.equals(channelTag))
										{
											ao = true;
										}
										channelStringList.add(channelTag);
										rowCounter++;
									}
									else
									{
										log.debug("did not add rights entry for channel "+channel.getChannelTag());
									}
								}
							}
						}
					}
					

					attributesValid = attributesValid && attributes.setAttribute(Attribute.MEDIA_RIGHTS, rightsTable);
					
					attributesValid = attributesValid && attributes.setAttribute(Attribute.CHANNELS, channelStringList);
					
					//ao will be true if the ao channel was in the channel list
					attributesValid = attributesValid && attributes.setAttribute(Attribute.CONT_RESTRICTED_MATERIAL, Boolean.valueOf(ao));
					
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
				
				String assetTitle = buildAssetTitle(titleDescription.getProgrammeTitle(),titleDescription.getSeriesNumber() , titleDescription.getEpisodeNumber() ,titleDescription.getEpisodeTitle());
				attributesValid &= attributes.setAttribute(Attribute.ASSET_TITLE, assetTitle);
				
				attributesValid &= attributes.setAttribute(Attribute.PRODUCTION_NUMBER, titleDescription.getProductionNumber());
				attributesValid &= attributes.setAttribute(Attribute.CONT_CATEGORY, titleDescription.getStyle());
				if(titleDescription.getYearOfProduction() != null)
				attributesValid &= attributes.setAttribute(Attribute.SERIES_YEAR, titleDescription.getYearOfProduction().toString());
				attributesValid &= attributes.setAttribute(Attribute.LOCATION, titleDescription.getCountryOfProduction());
				
				attributesValid &= attributes.setAttribute(Attribute.CONT_RESTRICTED_ACCESS, title.isRestrictAccess());
				
				if(title.isPurgeProtect() == null){
					attributesValid &= attributes.setAttribute(Attribute.PURGE_PROTECTED, Boolean.FALSE);	
				}
				else{
					attributesValid &= attributes.setAttribute(Attribute.PURGE_PROTECTED, title.isPurgeProtect());
				}
				
				if (!attributesValid) {
					log.warn("Title created but one or more attributes were invalid");
					returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
				}
			
				AttributeMap newAsset = null;
				try{
					newAsset = accessRightsController.updateAccessRights(attributes.getAttributes());
				}
				catch(Exception e){
					log.error("error determining access rights for new asset",e);
					newAsset = attributes.getAttributes();
				}
				
				AttributeMap result;
				try {
					result = client.assetApi().createAsset(newAsset);
					if (result == null) {
						log.warn("Mayam failed to create new Title asset");
						returnCode = MayamClientErrorCode.TITLE_CREATION_FAILED;
					} else {
						log.info("Created Title with Title ID:"+title.getTitleID());
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

	/**
	 * called when creating or updating titles based on placeholder management messages
	 * @param attributes   - an attribute map we are building (performs validation as items are added to map, not strictly necessary)
	 * @param attributesValid - records if attributes were valid at the start of method call
	 * @param licenses - licence information from placeholder management message
	 * @param earliestStart - value to use as current earliest start (could be null)
	 * @param latestEnd - value to use as current latest end (coult be null)
	 * @return
	 */
	private boolean updateLicenceStartAndEnd(
			MayamAttributeController attributes,
			boolean attributesValid,
			List<License> licenses,
			XMLGregorianCalendar earliestStart,
			XMLGregorianCalendar latestEnd)
	{
		for(License license : licenses){
			LicensePeriodType period = license.getLicensePeriod();
			
			/* Earliest start and latest end date start */
			XMLGregorianCalendar start = period.getStartDate();
			XMLGregorianCalendar end = period.getEndDate();

			if (start != null)
			{
				if (earliestStart == null)
				{
					earliestStart = start;
				}
				else
				{
					int compare = earliestStart.toGregorianCalendar().compareTo(start.toGregorianCalendar());
					if (compare > 0)
					{
						log.debug(start.toString() + " earlier than " + earliestStart.toString());
						earliestStart = start;
					}
				}
			}

			if (end != null)
			{
				if (latestEnd == null)
				{
					latestEnd = end;
				}
				else
				{
					int compare = latestEnd.toGregorianCalendar().compareTo(end.toGregorianCalendar());
					if (compare < 0)
					{
						log.debug(end.toString() + " later than " + latestEnd.toString());
						latestEnd = end;
					}
				}
			}
			/* Earliest start and latest end date end */
		}
		
		Date earliestStartDate = dateUtil.fromXMLGregorianCalendar(earliestStart);
		Date latestEndDate = dateUtil.fromXMLGregorianCalendar(latestEnd);
		
		attributesValid = attributesValid && attributes.setAttribute(Attribute.LICENSE_START, earliestStartDate);
		attributesValid = attributesValid && attributes.setAttribute(Attribute.LICENSE_END, latestEndDate);
		return attributesValid;
	}

	private String buildAssetTitle(String programmeTitle, BigInteger seriesNumber, BigInteger episodeNumber, String episodeTitle)
	{
		StringBuilder builderAssetTitle = new StringBuilder();
		
		if(programmeTitle != null){
			builderAssetTitle.append(programmeTitle);
		}
		
		builderAssetTitle.append(" - ");
		
		if(seriesNumber != null){
			builderAssetTitle.append(seriesNumber);
		}
		
		builderAssetTitle.append(" - ");
		
		if(episodeNumber != null){
			builderAssetTitle.append(episodeNumber);
		}
		
		builderAssetTitle.append(" - ");
		
		if(episodeTitle != null){
			builderAssetTitle.append(episodeTitle);
		}
		
		return builderAssetTitle.toString();
	}

	public MayamClientErrorCode updateTitle(Material.Title title)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		boolean attributesValid = true;
	
		if (title != null) {
			log.info(String.format("updating title %s",title.getTitleID()));
			
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
					
					Distributor distributor = title.getDistributor();
					if (distributor != null) {
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
			log.info(String.format("updating title %s",title.getTitleID()));
			
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
					
					boolean ao = false;
					RightsType titleRights = title.getRights();
					List<License> licenses = titleRights.getLicense();
					
					XMLGregorianCalendar earliestStart = null;
					XMLGregorianCalendar latestEnd = null;
					
					attributesValid = updateLicenceStartAndEnd(attributes, attributesValid, licenses, earliestStart, latestEnd);						
										
					if (titleRights != null) {
						List<String> columnNames = new ArrayList<String>();
						columnNames.add("Organization ID");
						columnNames.add("Organization Name");
						columnNames.add("Start Date");
						columnNames.add("End Date");
						columnNames.add("Channel Name");
							
						GenericTable rightsTable = new GenericTable(columnNames);
						StringList channelStringList = new StringList();

						
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
										if (channelProperties.isTagValid(channel.getChannelTag()))
										{
										
											rightsTable.setCellValue(rowCounter, 0, holder.getOrganisationID());
											rightsTable.setCellValue(rowCounter, 1, holder.getOrganisationName());
											rightsTable.setCellValue(rowCounter, 2, period.getStartDate().toString());
											rightsTable.setCellValue(rowCounter, 3, period.getEndDate().toString());
											rightsTable.setCellValue(rowCounter, 4, channel.getChannelName());
											
											String channelTag = channel.getChannelTag();
											
											if(AO_CHANNEL_TAG.equals(channelTag)){
												ao=true;
											}
											
											channelStringList.add(channelTag);
											
											rowCounter ++;
										}
										else{
											log.debug("did not add rights entry for channel "+channel.getChannelTag());
										}
									}
								}
							}
						}
						attributesValid = attributesValid && attributes.setAttribute(Attribute.MEDIA_RIGHTS, rightsTable);
						attributesValid = attributesValid && attributes.setAttribute(Attribute.CHANNELS, channelStringList);
						
						//ao will be true if the ao channel was in the channel list
						attributesValid = attributesValid && attributes.setAttribute(Attribute.CONT_RESTRICTED_MATERIAL, Boolean.valueOf(ao));
						
					}

					attributesValid &=attributes.setAttribute(Attribute.SHOW, titleDescription.getShow());
					attributesValid &= attributes.setAttribute(Attribute.SERIES_TITLE, titleDescription.getProgrammeTitle());
					if(titleDescription.getSeriesNumber() != null)
					attributesValid &= attributes.setAttribute(Attribute.SEASON_NUMBER, titleDescription.getSeriesNumber().intValue());
					
					attributesValid &= attributes.setAttribute(Attribute.EPISODE_TITLE, titleDescription.getEpisodeTitle());
					if(titleDescription.getEpisodeNumber() != null)
					attributesValid &= attributes.setAttribute(Attribute.EPISODE_NUMBER, titleDescription.getEpisodeNumber().intValue());
					
					String assetTitle = buildAssetTitle(titleDescription.getProgrammeTitle(),titleDescription.getSeriesNumber() , titleDescription.getEpisodeNumber() ,titleDescription.getEpisodeTitle());
					attributesValid &= attributes.setAttribute(Attribute.ASSET_TITLE, assetTitle);
					
					attributesValid &= attributes.setAttribute(Attribute.PRODUCTION_NUMBER, titleDescription.getProductionNumber());
					attributesValid &= attributes.setAttribute(Attribute.CONT_CATEGORY, titleDescription.getStyle());
					if(titleDescription.getYearOfProduction() != null)
					attributesValid &= attributes.setAttribute(Attribute.SERIES_YEAR, titleDescription.getYearOfProduction().toString());
					attributesValid &= attributes.setAttribute(Attribute.LOCATION, titleDescription.getCountryOfProduction());
					
					attributesValid &= attributes.setAttribute(Attribute.CONT_RESTRICTED_ACCESS, title.isRestrictAccess());
					
					boolean isProtected =false;
					log.debug("updating protected attribute ...");
					if(attributes.getAttributes().getAttribute(Attribute.PURGE_PROTECTED) != null){
						 isProtected = attributes.getAttributes().getAttribute(Attribute.PURGE_PROTECTED);
					}
					
					boolean titleIsPurgeProtected = false;
					
					if(title.isPurgeProtect() != null){
						titleIsPurgeProtected = title.isPurgeProtect().booleanValue();
 					}
					
					attributesValid &= attributes.setAttribute(Attribute.PURGE_PROTECTED, Boolean.valueOf(titleIsPurgeProtected));

 					boolean isPreviewPass = MayamPreviewResults.isPreviewPass((String) attributes.getAttributes().getAttribute(Attribute.QC_PREVIEW_RESULT));
					
					if (ao)
					{
						if (isProtected)
						{
							log.warn("AO Material is Protected. This does not affect the Archive policy for: " + title.getTitleID());
						}
						// no classification and so no archive policy can be set.
					}
					else
					{
						if (isProtected != titleIsPurgeProtected)
						{

							if (titleIsPurgeProtected && isPreviewPass)
							{
								attributesValid &= attributes.setAttribute(Attribute.ARCHIVE_POLICY, "2");
							}
							try
							{
								List<AttributeMap> materials = client.assetApi().getAssetChildren(MayamAssetType.TITLE.getAssetType(),
								                                                                  assetID,
								                                                                  MayamAssetType.MATERIAL.getAssetType());
								for (int i = 0; i < materials.size(); i++)
								{
									AttributeMap material = materials.get(i);
									material.setAttribute(Attribute.PURGE_PROTECTED, titleIsPurgeProtected);
									if (ao)
									{
										// material.setAttribute(Attribute.ARCHIVE_POLICY, "R");
									}
									else if (titleIsPurgeProtected && isPreviewPass)
									{
										material.setAttribute(Attribute.ARCHIVE_POLICY, "2");
									}
									client.assetApi().updateAsset(material);
								}
							}
							catch (RemoteException e)
							{
								log.error("Exception thrown by Mayam while retrieving child assets of title : " +
								          title.getTitleID(), e);
							}
						}
					}

					if (!attributesValid) {
						log.warn("Title updated but one or more attributes were invalid");
						returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
					}
					
					AttributeMap result;
					try {
						result = client.assetApi().updateAsset(attributes.getAttributes());
						log.info("Updated Title with TitleID "+title.getTitleID());
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
	
	public MayamClientErrorCode purgeTitle(String titleID, int gracePeriod)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		
		log.info(String.format("purge title %s",titleID));
		
		if (isProtected(titleID)) 
		{
			log.warn("ignoring delete request for protected title "+titleID);
		}
		else {
			try {
				//fetch asset by house id to get its assetid
				AttributeMap assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), titleID);
				String assetID = assetAttributes.getAttribute(Attribute.ASSET_ID);
				
				try
				{
					taskController.cancelAllOpenTasksForAsset(MayamAssetType.TITLE.getAssetType(), Attribute.ASSET_ID,assetID);
				}
				catch (MayamClientException e)
				{
					log.error("error closing open tasks for title before deletion",e);
					//user intent is for title to delete so dont let failure of side effect (removal of open tasks) prevent that
				}
								
				//Delete the asset
				client.assetApi().deleteAsset(MayamAssetType.TITLE.getAssetType(), assetID, gracePeriod);
				
				
				//Delete child materials and close open tasklists
				List<AttributeMap> childAssets = client.assetApi().getAssetChildren(MayamAssetType.TITLE.getAssetType(), assetID, MayamAssetType.MATERIAL.getAssetType());
				for (AttributeMap material: childAssets)
				{
					String materialID = material.getAttributeAsString(Attribute.HOUSE_ID);
					materialController.deleteMaterial(materialID, gracePeriod);
				}
			} catch (RemoteException e) {
				log.error("Error deleting title : "+ titleID,e);
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
		
		log.info(String.format("title %s exists : %b",titleID, titleFound));
		
		return titleFound;
	}
	
	public AttributeMap getTitle(String titleID) throws MayamClientException {
		AttributeMap assetAttributes = null;
		try {
			assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), titleID);
		} catch (RemoteException e1) {
			log.error("Exception thrown by Mayam while attempting to retrieve asset :" + titleID,e1);
			throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
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
		
		log.info(String.format("title %s isprotected : %b",titleID, isProtected));
		
		return isProtected;
	}

	public String getRightsStart(AttributeMap titleAttributes)
	{
		// TODO find very first?
		GenericTable rights = titleAttributes.getAttribute(Attribute.MEDIA_RIGHTS);

		if (rights != null)
		{
			List<Row> rows = rights.getRows();
			if (rows.size() > 0 && rows.get(0).size() > 3)
			{
				return rows.get(0).get(2);
			}
		}

		log.warn("could not find licence for title");
		return "";
	}

	public String getRightsEnd(AttributeMap titleAttributes)
	{
		// TODO find very last?
		GenericTable rights = titleAttributes.getAttribute(Attribute.MEDIA_RIGHTS);
		if (rights != null)
		{
			List<Row> rows = rights.getRows();
			if (rows.size() > 0 && rows.get(0).size() > 3)
			{
				return rows.get(0).get(3);
			}
		}
		log.warn("could not find licence for title");
		return "";
	}

}
