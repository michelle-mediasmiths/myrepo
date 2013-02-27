package com.mediasmiths.mayam.controllers;


import java.io.File;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.Aggregation;
import au.com.foxtel.cf.mam.pms.Aggregator;
import au.com.foxtel.cf.mam.pms.Compile;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.Order;
import au.com.foxtel.cf.mam.pms.QualityCheckEnumType;

import au.com.foxtel.cf.mam.pms.Source;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.*;
import com.mayam.wf.attributes.shared.type.Marker.Type;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details.Supplier;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title.Distributor;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks.Track;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType;
import com.mediasmiths.foxtel.generated.ruzz.DetailType;
import com.mediasmiths.foxtel.pathresolver.PathResolver;
import com.mediasmiths.foxtel.pathresolver.UnknownPathException;
import com.mediasmiths.foxtel.pathresolver.PathResolver.PathType;
import com.mediasmiths.mayam.FileFormatVerification;
import com.mediasmiths.mayam.FileFormatVerificationFailureException;
import com.mediasmiths.mayam.MayamAspectRatios;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamAudioEncoding;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamClientImpl;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.accessrights.MayamAccessRightsController;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mayam.util.RevisionUtil;
import com.mediasmiths.mayam.util.SegmentUtil;
import com.mediasmiths.std.io.PropertyFile;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamMaterialController extends MayamController
{

	public static final String PROGRAMME_MATERIAL_AGL_NAME = "programme";
	public static final String PROGRAMME_MATERIAL_CONTENT_TYPE = "PG";

	public static final String ASSOCIATED_MATERIAL_AGL_NAME = "associated";
	public static final String ASSOCIATED_MATERIAL_CONTENT_TYPE = "PE";

	private final TasksClient client;
	private final MayamTaskController taskController;
	private final static Logger log = Logger.getLogger(MayamMaterialController.class);

	@Inject
	@Named("material.exchange.marshaller")
	private Marshaller materialExchangeMarshaller;

	@Inject
	private FileFormatVerification fileformatVerification;

	@Inject
	private MayamAccessRightsController accessRightsController;
	
	@Inject
	private PathResolver pathResolver;
	
	@Inject
	public MayamMaterialController(@Named(SETUP_TASKS_CLIENT) TasksClient mayamClient, MayamTaskController mayamTaskController)
	{
		client = mayamClient;
		taskController = mayamTaskController;
	}

	public MayamClientErrorCode createMaterial(MaterialType material, String titleID)
	{
		log.info(String.format("Creating Material %s for title %s", material.getMaterialID(),titleID));
		
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;
		boolean createCompLoggingTask = false;
		String parentAssetID = null; //parent asset id to use if a compliance logging task is to be created

		attributes.setAttribute(Attribute.QC_STATUS, QcStatus.TBD);
		attributes.setAttribute(Attribute.QC_PREVIEW_RESULT, MayamPreviewResults.PREVIEW_NOT_DONE);
		attributes.setAttribute(Attribute.QC_PARALLEL_ALLOWED, Boolean.FALSE);
		
		if (material != null && material.getMaterialID() != null && !material.getMaterialID().equals(""))
		{
			// setting parent_house_id is an unsupported operation
			// attributesValid &= attributes.setAttribute(Attribute.PARENT_HOUSE_ID, titleID);
			try
			{
				AttributeMap title = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), titleID);
				if (title != null)
				{

					Boolean isProtected = title.getAttribute(Attribute.PURGE_PROTECTED);

					if (isProtected != null)
					{
						attributesValid &= attributes.setAttribute(Attribute.PURGE_PROTECTED, isProtected.booleanValue());
						
						if (isProtected)
						{
							attributesValid &= attributes.setAttribute(Attribute.ARCHIVE_POLICY, "2");
						}
					}

					String assetId = title.getAttribute(Attribute.ASSET_ID);
					attributesValid &= attributes.setAttribute(Attribute.ASSET_PARENT_ID, assetId);

					updateMaterialAttributesFromTitle(attributes, title);
					
					Boolean isAO = title.getAttribute(Attribute.CONT_RESTRICTED_MATERIAL);

					if (isAO != null && isAO.equals(Boolean.TRUE))
					{
						attributesValid = attributesValid
								&& attributes.setAttribute(Attribute.ARCHIVE_POLICY, "R");
					}
				}
			}
			catch (RemoteException e)
			{
				log.error("MayamException while trying to retrieve title : " + titleID, e);
				return MayamClientErrorCode.TASK_SEARCH_FAILED;
			}

			// attributesValid &= attributes.setAttribute(Attribute.PARENT_HOUSE_ID, titleID);

			attributesValid &= attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.MATERIAL.getAssetType());
			attributesValid &= attributes.setAttribute(Attribute.METADATA_FORM, PROGRAMME_MATERIAL_AGL_NAME);
			attributesValid &= attributes.setAttribute(Attribute.HOUSE_ID, material.getMaterialID());
			attributesValid &= attributes.setAttribute(Attribute.CONT_MAT_TYPE, PROGRAMME_MATERIAL_CONTENT_TYPE);
			attributesValid &= attributes.setAttribute(Attribute.QC_STATUS, QcStatus.TBD);
			attributesValid &= attributes.setAttribute(Attribute.QC_PREVIEW_STATUS, QcStatus.TBD);

			if (material.getQualityCheckTask() != null)
			{
				if (material.getQualityCheckTask().equals(QualityCheckEnumType.AUTOMATIC_ON_INGEST))
				{
					attributesValid &= attributes.setAttribute(Attribute.QC_REQUIRED, new Boolean(true));
				}
				else
				{
					attributesValid &= attributes.setAttribute(Attribute.QC_REQUIRED, new Boolean(false));
				}

			}
			else
			{
				attributesValid &= attributes.setAttribute(Attribute.QC_REQUIRED, new Boolean(false));
			}

			attributesValid &= attributes.setAttribute(Attribute.REQ_FMT, material.getRequiredFormat());

			Source source = material.getSource();
			if (source != null)
			{
				Aggregation aggregation = source.getAggregation();
				if (aggregation != null)
				{
					Aggregator aggregator = aggregation.getAggregator();
					Order order = aggregation.getOrder();
					if (aggregator != null)
					{
						// As per Foxtel and Mayam decision, Aggregator Name will not be stored in AGL
						attributesValid &= attributes.setAttribute(Attribute.AGGREGATOR, aggregator.getAggregatorID());
						// attributesValid &= attributes.setAttribute(Attribute.APP_SRC, aggregator.getAggregatorName());
					}
					if (order != null)
					{
						// Order Created field not required
						// attributesValid &= attributes.setAttribute(Attribute.OP_DATE, order.getOrderCreated());
						attributesValid &= attributes.setAttribute(Attribute.REQ_REFERENCE, order.getOrderReference());
					}
				}


				Compile compile = source.getCompile();
				if (compile != null)
				{
					try
					{
						AttributeMap parentMaterial = client.assetApi().getAssetBySiteId(
								MayamAssetType.MATERIAL.getAssetType(),
								compile.getParentMaterialID());

						createCompLoggingTask = true;
						parentAssetID = parentMaterial.getAttributeAsString(Attribute.ASSET_ID);

					}
					catch (RemoteException e)
					{
						log.error("MayamException while trying to retrieve title : " + compile.getParentMaterialID(), e);
					}

					attributesValid &= attributes.setAttribute(Attribute.SOURCE_HOUSE_ID, compile.getParentMaterialID());
				}

				/*
				 * No longer supported Library library = source.getLibrary(); if (library != null) { List<TapeType> tapeList = library.getTape(); if (tapeList != null) { IdSet tapeIds = new IdSet();
				 * for (int i = 0; i < tapeList.size(); i++) { TapeType tape = tapeList.get(i); if (tape != null) { tapeIds.add(tape.getPresentationID()); } } attributesValid &=
				 * attributes.setAttribute(Attribute.SOURCE_IDS, tapeIds); } }
				 */
			}

			if (!attributesValid)
			{
				log.warn("Material created but one or more attributes was invalid");
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
			try
			{
				result = client.assetApi().createAsset(newAsset);
				if (result == null)
				{
					log.warn("Mayam failed to create Material");
					returnCode = MayamClientErrorCode.MATERIAL_CREATION_FAILED;
				}
				else
				{
					if (createCompLoggingTask)
					{
						try
						{
							Date requiredBy = null;

							if (material.getRequiredBy() != null && material.getRequiredBy().toGregorianCalendar() != null)
							{
								requiredBy = material.getRequiredBy().toGregorianCalendar().getTime();
							}

							long taskID = taskController.createComplianceLoggingTaskForMaterial(
									material.getMaterialID(),parentAssetID,
									requiredBy);
							log.debug("created task with id : " + taskID);
						}
						catch (MayamClientException e)
						{
							log.error("Exception thrown in Mayam while creating Compliance Logging task for Material : "
									+ material.getMaterialID(), e);
						}
					}
					else
					{
						try
						{
							Date requiredBy = null;

							if (material.getRequiredBy() != null && material.getRequiredBy().toGregorianCalendar() != null)
							{
								requiredBy = material.getRequiredBy().toGregorianCalendar().getTime();
							}

							taskController.createIngestTaskForMaterial(material.getMaterialID(), requiredBy);
							log.debug("created ingest task");
						}
						catch (MayamClientException e)
						{
							log.error(
									"Exception thrown in Mayam while creating Ingest task for Material : "
											+ material.getMaterialID(),
									e);
						}
					}
				}
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
				log.error("Exception thrown by Mayam while trying to create Material");
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}
		}
		else
		{
			log.warn("Null material object or no house ID, unable to create asset");
			return MayamClientErrorCode.MATERIAL_UNAVAILABLE;
		}
		return returnCode;
	}

	// Asset Title
	// AdultContent
	// ProgrammeTitle
	// Show
	// Series/Season Number
	// Episode Number
	// Episode Title
	// Production Country
	// Production Number
	// Production Year
	// Style
	// Channels
	public static Attribute[] materialsAttributesInheritedFromTitle = new Attribute[] { Attribute.ASSET_TITLE,
			Attribute.CONT_RESTRICTED_MATERIAL,Attribute.CONT_RESTRICTED_ACCESS, Attribute.SERIES_TITLE, Attribute.SERIES_TITLE, Attribute.SHOW,
			Attribute.SEASON_NUMBER, Attribute.EPISODE_NUMBER, Attribute.EPISODE_TITLE, Attribute.LOCATION,
			Attribute.PRODUCTION_NUMBER, Attribute.SERIES_YEAR, Attribute.CONT_CATEGORY, Attribute.CHANNELS, Attribute.CHANNEL_GROUPS, Attribute.PURGE_PROTECTED };

	private void updateMaterialAttributesFromTitle(MayamAttributeController attributes, AttributeMap title)
	{
		// copy metadata from title onto material
		for (Attribute a : materialsAttributesInheritedFromTitle)
		{
			attributes.copyAttribute(a, title);
		}

	}

	public void updateMaterialAttributesFromTitle(AttributeMap title) throws MayamClientException
	{
		// copy metadata from title onto its material
		List<AttributeMap> materials;
		try
		{
			materials = client.assetApi().getAssetChildren(
					MayamAssetType.TITLE.getAssetType(),
					(String) title.getAttribute(Attribute.ASSET_ID),
					MayamAssetType.MATERIAL.getAssetType());
			log.debug(""+materials.size() +" materials returned for title");
		}
		catch (RemoteException e1)
		{
			log.error("error getting materials for title " + title.getAttributeAsString(Attribute.ASSET_ID), e1);
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED, e1);
		}
		
		
	
		for (AttributeMap material : materials)
		{

			AttributeMap update = taskController.updateMapForAsset(material);
			
			for (Attribute a : materialsAttributesInheritedFromTitle)
			{
				update.setAttribute(a, title.getAttribute(a));
			}

			try
			{
				client.assetApi().updateAsset(update);
			}
			catch (RemoteException e)
			{
				log.error("error updating material" + material.getAttributeAsString(Attribute.HOUSE_ID), e);
			}
		}

	}

	/**
	 * Creates a material, returns the id of the created material
	 * 
	 * @param material
	 * @param title2
	 * @param details
	 * @return
	 * @throws MayamClientException
	 */
	public String createMaterial(MarketingMaterialType material, String titleID, Details details, Title title)
			throws MayamClientException
	{
		log.info(String.format("Creating Marketing Material for title %s",titleID));
		
		MayamAttributeController attributes = new MayamAttributeController(client);
		
		attributes.setAttribute(Attribute.QC_STATUS, QcStatus.TBD);
		attributes.setAttribute(Attribute.QC_PREVIEW_RESULT, MayamPreviewResults.PREVIEW_NOT_DONE);
		attributes.setAttribute(Attribute.QC_PARALLEL_ALLOWED, Boolean.FALSE);
		
		boolean attributesValid = true;

		if (material != null)
		{

			try
			{
				AttributeMap titleAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), titleID);
				if (titleAttributes != null)
				{
					String assetId = titleAttributes.getAttribute(Attribute.ASSET_ID);
					attributesValid &= attributes.setAttribute(Attribute.ASSET_PARENT_ID, assetId);

					updateMaterialAttributesFromTitle(attributes, titleAttributes);
				}
			}
			catch (RemoteException e)
			{
				log.error("MayamException while trying to retrieve title : " + titleID, e);
				throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED, e);
			}

			attributesValid &= attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.MATERIAL.getAssetType());
			attributesValid &= attributes.setAttribute(Attribute.METADATA_FORM, ASSOCIATED_MATERIAL_AGL_NAME);
			attributesValid &= attributes.setAttribute(Attribute.CONT_MAT_TYPE, ASSOCIATED_MATERIAL_CONTENT_TYPE);
			attributesValid &= attributes.setAttribute(Attribute.QC_STATUS, QcStatus.TBD);
			attributesValid &= attributes.setAttribute(Attribute.QC_PREVIEW_STATUS, QcStatus.TBD);

			attributesValid &= attributes.setAttribute(
					Attribute.CONT_ASPECT_RATIO,
					MayamAspectRatios.mayamAspectRatioMappings.get(material.getAspectRatio()));
			attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, material.getFormat());
			attributesValid = attributesValid
					&& attributes.setAttribute(Attribute.CONT_RESTRICTED_MATERIAL, material.isAdultMaterial());

			if (material.isAdultMaterial())
			{
				attributesValid = attributesValid
						&& attributes.setAttribute(Attribute.ARCHIVE_POLICY, "R");
			}
			
			// As per Foxtel and Mayam decision, duration and timecodes will be detected in Ardome, no need to store
			// attributesValid &= attributes.setAttribute(Attribute., material.getFirstFrameTimecode());
			// attributesValid &= attributes.setAttribute(Attribute., material.getLastFrameTimecode());
			// attributesValid &= attributes.setAttribute(Attribute.ASSET_DURATION, material.getDuration());

			AudioTracks audioTracks = material.getAudioTracks();
			List<Track> tracks = audioTracks.getTrack();
			AudioTrackList audioTrackList = new AudioTrackList();
			for (int i = 0; i < tracks.size(); i++)
			{
				AudioTrack audioTrack = new AudioTrack();
				Track track = tracks.get(i);
				audioTrack.setEncoding(AudioTrack.EncodingType.valueOf(MayamAudioEncoding.mayamAudioEncodings.get(track.getTrackEncoding().toString())));
				audioTrack.setName(track.getTrackName().toString());
				audioTrack.setNumber(track.getTrackNumber());
				audioTrackList.add(audioTrack);
			}

			attributesValid &= attributes.setAttribute(Attribute.AUDIO_TRACKS, audioTrackList);

			Distributor distributor = title.getDistributor();
			if (distributor != null)
			{
				attributesValid &= attributes.setAttribute(Attribute.DIST_NAME, title.getDistributor().getDistributorName());
			}

			Supplier supplier = details.getSupplier();
			if (supplier != null)
			{
				attributesValid &= attributes.setAttribute(Attribute.AGGREGATOR, supplier.getSupplierID());
			}

			if (material.getFormat() != null)
			{
				attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, material.getFormat());
			}

			if (title.getProgrammeTitle() != null)
			{
				attributesValid &= attributes.setAttribute(Attribute.SERIES_TITLE, title.getProgrammeTitle());
			}

			if (title.getSeriesNumber() != null)
			{
				attributesValid &= attributes.setAttribute(
						Attribute.SEASON_NUMBER,
						Integer.valueOf(title.getSeriesNumber().intValue()));
			}

			if (title.getEpisodeNumber() != null)
			{
				attributesValid &= attributes.setAttribute(
						Attribute.EPISODE_NUMBER,
						Integer.valueOf(title.getEpisodeNumber().intValue()));
			}

			if (title.getEpisodeTitle() != null)
			{
				attributesValid &= attributes.setAttribute(Attribute.SERIES_TITLE, title.getEpisodeTitle());
			}

			if (title.getCountryOfProduction() != null)
			{
				attributesValid &= attributes.setAttribute(Attribute.LOCATION, title.getCountryOfProduction());
			}

			if (title.getProductionNumber() != null)
			{
				attributesValid &= attributes.setAttribute(Attribute.PRODUCTION_NUMBER, title.getProductionNumber());
			}

			if (title.getProductionNumber() != null)
			{
				attributesValid &= attributes.setAttribute(Attribute.SERIES_YEAR, title.getProductionNumber());
			}

			if (!attributesValid)
			{
				log.error("Invalid attributes on material create request");
				throw new MayamClientException(MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES);
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
			try
			{
				result = client.assetApi().createAsset(newAsset);
				if (result == null)
				{
					log.warn("Mayam failed to create Material");
					throw new MayamClientException(MayamClientErrorCode.MATERIAL_CREATION_FAILED);
				}
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
				log.error("Exception thrown by Mayam while trying to create Material", e);
				throw new MayamClientException(MayamClientErrorCode.MAYAM_EXCEPTION, e);
			}

			String assetID = result.getAttribute(Attribute.ASSET_ID);
			log.info(String.format("assetId %s returned when creating marketing material", assetID));

			String siteID = result.getAttribute(Attribute.ASSET_SITE_ID);
			log.info(String.format("siteID %s returned when creating marketing material", siteID));

			if (siteID == null)
			{
				throw new MayamClientException(MayamClientErrorCode.CREATED_ASSOCIATED_CONTENT_HAS_NULL_SITE_ID);
			}

			// create ingest task
			try
			{
				log.debug("trying to create ingest task");
				taskController.createIngestTaskForMaterial(siteID, null);
			}
			catch (Exception e)
			{
				log.error("error creating ingest task for asset " + siteID, e);
			}

			return siteID;
		}
		else
		{
			log.warn("Null material object, unable to create asset");
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_UNAVAILABLE);
		}
	}


	@Inject
	@Named("segmentation.data.stash.folder")
	private String segmentationDataStashPath;
	
	public String segdataFilePathForMaterial(String materialId)
	{
		return segmentationDataStashPath+IOUtils.DIR_SEPARATOR+materialId+".xml";
	}
	
	
	// Material - Updating a media asset in Mayam
	public boolean updateMaterial(ProgrammeMaterialType material, Details details, Title title) throws MayamClientException
	{
		log.info(String.format("Updating material %s for title %s",material.getMaterialID(), title.getTitleID()));
		
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		boolean attributesValid = true;
		boolean isPlaceholder = false;

		if (material != null)
		{
			MayamAttributeController attributes = null;
			AttributeMap assetAttributes = null;

			try
			{
				assetAttributes = client.assetApi().getAssetBySiteId(
						MayamAssetType.MATERIAL.getAssetType(),
						material.getMaterialID());
				
				isPlaceholder = AssetProperties.isMaterialPlaceholder(assetAttributes);
			}
			catch (RemoteException e1)
			{
				log.error("Exception thrown by Mayam while attempting to retrieve asset :" + material.getMaterialID());
				throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED,e1);
			}

			if (assetAttributes != null)
			{
				attributes = new MayamAttributeController(client.createAttributeMap());
				attributes.setAttribute(Attribute.ASSET_ID, assetAttributes.getAttribute(Attribute.ASSET_ID));
				attributes.setAttribute(Attribute.ASSET_TYPE, assetAttributes.getAttribute(Attribute.ASSET_TYPE));

				attributesValid &= attributes.setAttribute(
						Attribute.CONT_ASPECT_RATIO,
						MayamAspectRatios.mayamAspectRatioMappings.get(material.getAspectRatio()));
				attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, material.getFormat());


				// FX-113 - original conform information should be converted to a human readable format for the natural breaks structure
				if (material.getOriginalConform() != null)
				{

					log.debug("programme material message contains original conform information");
					SegmentationType originalConform = material.getOriginalConform();

					try
					{
						String presentationString = SegmentUtil.originalConformToHumanString(originalConform);
						attributesValid &= attributes.setAttribute(Attribute.SEGMENTATION_NOTES, presentationString);
					}
					catch (Exception e)
					{
						log.error("error setting original conform information in SEGMENTATION_NOTES", e);
					}

				}
				else if (material.getPresentation() != null)
				{

					Presentation presentation = material.getPresentation();
				
					//stash to file for populating segmentation information later on
					
					//stashing to file will be replaced by stashing in pending tx pacakge list
					try
					{
						String presentationString = presentationToString(presentation);
						FileUtils.writeStringToFile(new File(segdataFilePathForMaterial(material.getMaterialID())),presentationString);
					}
					catch (JAXBException e)
					{
						log.error("Error marshalling presentation for material " + material.getMaterialID(), e);
					}
					catch(Exception e){
						log.error("error saving presentation info for material",e);
					}
					
					// save the segmentation information to the natural breaks string
					try
					{
						String presentationString = SegmentUtil.presentationToHumanString(presentation);
						attributesValid &= attributes.setAttribute(Attribute.SEGMENTATION_NOTES, presentationString);
					}
					catch (Exception e)
					{
						log.error("error converting segmentation informatio to human string for natual breaks field", e);
					}
					
				}

				AudioTracks audioTracks = material.getAudioTracks();
				if (audioTracks != null)
				{
					List<Track> tracks = audioTracks.getTrack();
					if (tracks != null)
					{
						AudioTrackList audioTrackList = new AudioTrackList();
						for (int i = 0; i < tracks.size(); i++)
						{
							AudioTrack audioTrack = new AudioTrack();
							Track track = tracks.get(i);
							audioTrack.setEncoding(AudioTrack.EncodingType.valueOf(MayamAudioEncoding.mayamAudioEncodings.get(track.getTrackEncoding().toString())));
							audioTrack.setName(track.getTrackName().toString());
							audioTrack.setNumber(track.getTrackNumber());
							audioTrackList.add(audioTrack);
						}
						attributesValid &= attributes.setAttribute(Attribute.AUDIO_TRACKS, audioTrackList);
					}
				}

				Distributor distributor = title.getDistributor();
				if (distributor != null)
				{
					attributesValid &= attributes.setAttribute(Attribute.DIST_NAME, title.getDistributor().getDistributorName());
				}

				Supplier supplier = details.getSupplier();
				if (supplier != null)
				{
					attributesValid &= attributes.setAttribute(Attribute.AGGREGATOR, supplier.getSupplierID());
				}

				if (material.getFormat() != null)
				{
					attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, material.getFormat());
				}

				if(details.getDeliveryVersion() != null){
					int deliveryVersion = details.getDeliveryVersion().intValue();
					attributesValid &= attributes.setAttribute(Attribute.VERSION_NUMBER, Integer.valueOf(deliveryVersion));
				}
				
				if (!attributesValid)
				{
					log.warn("Material updated but one or more attributes was invalid");
					returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
				}

				AttributeMap result;
				try
				{
					log.debug("updating material " + material.getMaterialID());
					result = client.assetApi().updateAsset(attributes.getAttributes());
					if (result == null)
					{
						log.warn("Mayam failed to update Material");
						returnCode = MayamClientErrorCode.MATERIAL_UPDATE_FAILED;
					}
				}
				catch (RemoteException e)
				{
					log.error("Exception thrown by Mayam while trying to update Material", e);
					throw new MayamClientException(MayamClientErrorCode.MATERIAL_UPDATE_FAILED,e);
				}
			}
			else
			{
				log.warn("Unable to retrieve asset :" + material.getMaterialID());
				returnCode = MayamClientErrorCode.MATERIAL_FIND_FAILED;
			}
		}
		else
		{
			log.warn("Null material object, unable to update asset");
			returnCode = MayamClientErrorCode.MATERIAL_UNAVAILABLE;
		}	
		
		if(returnCode!= MayamClientErrorCode.SUCCESS){
			throw new MayamClientException(returnCode);
		}
		
		return isPlaceholder;
	}

	private String presentationToString(Presentation p) throws JAXBException
	{
		JAXBElement<Presentation> j = new JAXBElement<ProgrammeMaterialType.Presentation>(
				new QName("", "Presentation"),
				Presentation.class,
				p);
		StringWriter sw = new StringWriter();
		materialExchangeMarshaller.marshal(j, sw);
		return sw.toString();
	}

	public MayamClientErrorCode updateMaterial(MaterialType material)
	{
		log.info(String.format("Updating material %s",material.getMaterialID()));
		
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		boolean attributesValid = true;
		if (material != null)
		{
			AttributeMap assetAttributes = null;
			MayamAttributeController attributes = null;

			try
			{
				assetAttributes = client.assetApi().getAssetBySiteId(
						MayamAssetType.MATERIAL.getAssetType(),
						material.getMaterialID());
			}
			catch (RemoteException e1)
			{
				log.error("Exception thrown by Mayam while attempting to retrieve asset :" + material.getMaterialID());
				e1.printStackTrace();
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}

			if (assetAttributes != null)
			{

				attributes = new MayamAttributeController(client.createAttributeMap());

				String titleID = assetAttributes.getAttribute(Attribute.PARENT_HOUSE_ID);
				try
				{

					AttributeMap titleAttributes = client.assetApi().getAssetBySiteId(
							MayamAssetType.TITLE.getAssetType(),
							titleID);
					if (titleAttributes != null)
					{
						updateMaterialAttributesFromTitle(attributes, titleAttributes);
					}
				}
				catch (RemoteException e)
				{
					log.error("MayamException while trying to retrieve title : " + titleID, e);
					// let the method continue to try and update what we can, though if the title for this material doesnt exist something has gone wrong
				}

				attributes.setAttribute(Attribute.ASSET_ID, assetAttributes.getAttribute(Attribute.ASSET_ID));
				attributes.setAttribute(Attribute.ASSET_TYPE, assetAttributes.getAttribute(Attribute.ASSET_TYPE));

				if (material.getQualityCheckTask() != null)
				{
					if (material.getQualityCheckTask().equals(QualityCheckEnumType.AUTOMATIC_ON_INGEST))
					{
						attributesValid &= attributes.setAttribute(Attribute.QC_REQUIRED, new Boolean(true));
					}
					else
					{
						attributesValid &= attributes.setAttribute(Attribute.QC_REQUIRED, new Boolean(false));
					}

				}

				attributesValid &= attributes.setAttribute(Attribute.REQ_FMT, material.getRequiredFormat());

				Source source = material.getSource();
				if (source != null)
				{
					Aggregation aggregation = source.getAggregation();
					if (aggregation != null)
					{
						Aggregator aggregator = aggregation.getAggregator();
						Order order = aggregation.getOrder();

						if (aggregator != null)
						{
							// Aggregator Name will not be stored, only ID
							attributesValid &= attributes.setAttribute(Attribute.AGGREGATOR, aggregator.getAggregatorID());
							// attributesValid &= attributes.setAttribute(Attribute.APP_SRC, aggregator.getAggregatorName());
						}
						if (order != null)
						{
							// attributesValid &= attributes.setAttribute(Attribute.OP_DATE, order.getOrderCreated().toGregorianCalendar().getTime());
							attributesValid &= attributes.setAttribute(Attribute.REQ_REFERENCE, order.getOrderReference());
						}
					}

					Compile compile = source.getCompile();
					if (compile != null)
					{
						try
						{
							AttributeMap parentMaterial = client.assetApi().getAssetBySiteId(
									MayamAssetType.MATERIAL.getAssetType(),
									compile.getParentMaterialID());
							if (parentMaterial != null)
							{
								// String assetId = parentMaterial.getAttribute(Attribute.ASSET_ID);
								// attributesValid &= attributes.setAttribute(Attribute.ASSET_PARENT_ID, assetId);
							}
						}
						catch (RemoteException e)
						{
							log.error(
									"Exception thrown by Mayam while trying to retrieve parent Material : "
											+ compile.getParentMaterialID(),
									e);
						}

						attributesValid &= attributes.setAttribute(Attribute.SOURCE_HOUSE_ID, compile.getParentMaterialID());
					}

					/*
					 * No longer supported Library library = source.getLibrary(); if (library != null) { List<TapeType> tapeList = library.getTape(); if (tapeList != null) { IdSet tapeIds = new
					 * IdSet(); for (int i = 0; i < tapeList.size(); i++) { TapeType tape = tapeList.get(i); if (tape != null) { tapeIds.add(tape.getPresentationID()); } } attributesValid &=
					 * attributes.setAttribute(Attribute.SOURCE_IDS, tapeIds); } }
					 */
				}

				if (!attributesValid)
				{
					log.warn("Material updated but one or more attributes was invalid");
					returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
				}

				AttributeMap result;
				try
				{
					log.debug("updating material " + material.getMaterialID());
					result = client.assetApi().updateAsset(attributes.getAttributes());
					if (result == null)
					{
						log.warn("Mayam failed to update Material");
						returnCode = MayamClientErrorCode.MATERIAL_UPDATE_FAILED;
					}
					else
					{
						try
						{
							AttributeMap compLoggingTask = taskController.getOnlyTaskForAssetBySiteID(
									MayamTaskListType.COMPLIANCE_LOGGING,
									material.getMaterialID());
							if (compLoggingTask != null)
							{
								if (material.getRequiredBy() != null && material.getRequiredBy().toGregorianCalendar() != null)
								{
									compLoggingTask.setAttribute(
											Attribute.REQ_BY,
											material.getRequiredBy().toGregorianCalendar().getTime());
									taskController.saveTask(compLoggingTask);
								}
							}

							AttributeMap ingestTask = taskController.getOnlyTaskForAssetBySiteID(
									MayamTaskListType.INGEST,
									material.getMaterialID());
							if (ingestTask != null)
							{
								if (material.getRequiredBy() != null && material.getRequiredBy().toGregorianCalendar() != null)
								{
									ingestTask.setAttribute(
											Attribute.REQ_BY,
											material.getRequiredBy().toGregorianCalendar().getTime());
									taskController.saveTask(ingestTask);
								}
							}
						}
						catch (MayamClientException e)
						{
							log.error(
									"Exception thrown by Mayam while updating tasks for Material : " + material.getMaterialID(),
									e);
						}
					}
				}
				catch (RemoteException e)
				{
					e.printStackTrace();
					log.error("Exception thrown by Mayam while trying to update Material");
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
			}
			else
			{
				log.warn("Unable to retrieve asset :" + material.getMaterialID());
				returnCode = MayamClientErrorCode.MATERIAL_FIND_FAILED;
			}
		}
		else
		{
			log.warn("Null material object, unable to update asset");
			returnCode = MayamClientErrorCode.MATERIAL_UNAVAILABLE;
		}

		return returnCode;
	}

	public boolean materialExists(String materialID)
	{
		boolean materialFound = false;
		try
		{
			AttributeMap assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.MATERIAL.getAssetType(), materialID);
			if (assetAttributes != null)
			{
				materialFound = true;
			}
		}
		catch (RemoteException e1)
		{
			log.debug("Exception thrown by Mayam while attempting to retrieve asset :" + materialID
					+ " , assuming it doesnt exist");
			log.trace(e1);
		}

		log.info(String.format("Material %s found: %b ",materialID, materialFound));
		return materialFound;
	}

	public AttributeMap getMaterialAttributes(String materialID)
	{
		AttributeMap assetAttributes = null;
		try
		{
			assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.MATERIAL.getAssetType(), materialID);
		}
		catch (RemoteException e1)
		{
			log.error("Exception thrown by Mayam while attempting to retrieve asset :" + materialID, e1);
		}
		return assetAttributes;
	}

	public AttributeMap getMaterialByAssetId(String assetID) throws MayamClientException
	{

		try
		{
			AttributeMap asset = client.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), assetID);
			
			if(asset==null) throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
			
			return asset;
		}
		catch (RemoteException e)
		{
			log.debug(String.format("remote expcetion getting material by asset id", e));
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED, e);
		}
	}

	public MaterialType getPHMaterialType(String materialID)
	{ // this is the placeholder material type, we also need the material exchange one to get info about audio tracks etc
		AttributeMap attributes = getMaterialAttributes(materialID);
		MaterialType material = new MaterialType();

		material.setMaterialID((String) attributes.getAttribute(Attribute.HOUSE_ID));
		Boolean qc = (Boolean) attributes.getAttribute(Attribute.QC_REQUIRED);

		if (qc != null)
		{
			if (qc.booleanValue())
			{
				material.setQualityCheckTask(QualityCheckEnumType.AUTOMATIC_ON_INGEST);
			}

		}
		else
		{
			log.warn(String.format("material %s had null QualityCheck attribute", materialID));
		}

		material.setRequiredFormat((String) attributes.getAttribute(Attribute.CONT_FMT));

		Source source = new Source();
		material.setSource(source);
		Aggregation aggregation = new Aggregation();
		source.setAggregation(aggregation);
		Aggregator aggregator = new Aggregator();
		aggregation.setAggregator(aggregator);
		Order order = new Order();
		aggregation.setOrder(order);

		aggregator.setAggregatorID("" + attributes.getAttribute(Attribute.AGGREGATOR));
		order.setOrderReference("" + attributes.getAttribute(Attribute.OP_ID));

		Compile compile = new Compile();
		source.setCompile(compile);
		compile.setParentMaterialID("" + attributes.getAttribute(Attribute.PARENT_HOUSE_ID));

		/*
		 * Library library = new Library(); IdSet tapeIds = attributes.getAttribute(Attribute.SOURCE_IDS); String[] tapeIdsArrays = (String[]) tapeIds.toArray(); ArrayList<TapeType> tapeList = new
		 * ArrayList<TapeType>(); for (int i = 0; i < tapeIdsArrays.length; i++) { TapeType tape = new TapeType(); tape.setLibraryID(tapeIdsArrays[i]); tapeList.add(tape); }
		 * library.withTape(tapeList); source.setLibrary(library);
		 */

		return material;
	}

	public MayamClientErrorCode deleteMaterial(String materialID)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		if (! isProtected(materialID))
		{
			try
			{
				AttributeMap assetAttributes = client.assetApi().getAssetBySiteId(
						MayamAssetType.MATERIAL.getAssetType(),
						materialID);

				if(assetAttributes==null){
					log.warn("delete request for material that doesnt exist "+materialID);
					return MayamClientErrorCode.MATERIAL_FIND_FAILED;
				}
				
				deleteAssetsPackages(MayamAssetType.MATERIAL.getAssetType(),(String)assetAttributes.getAttributeAsString(Attribute.ASSET_ID),materialID);
				
				String parentId = assetAttributes.getAttribute(Attribute.PARENT_HOUSE_ID);
				
				try
				{
					taskController.cancelAllOpenTasksForAsset(MayamAssetType.MATERIAL.getAssetType(), Attribute.HOUSE_ID, materialID);
				}
				catch (MayamClientException e)
				{
					log.error("error cancelling open tasks for asset during delete",e);
				}
				
				client.assetApi().deleteAsset(
						MayamAssetType.MATERIAL.getAssetType(),
						assetAttributes.getAttributeAsString(Attribute.ASSET_ID));
				
				if (parentId != null) {
					AttributeMap title = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), parentId);
					if (title != null) {
						String assetId = title.getAttribute(Attribute.ASSET_ID);
						List<AttributeMap> childAssets = client.assetApi().getAssetChildren(MayamAssetType.TITLE.getAssetType(), assetId, MayamAssetType.MATERIAL.getAssetType());
						if (childAssets == null || childAssets.isEmpty())
						{
							client.assetApi().deleteAsset(MayamAssetType.TITLE.getAssetType(), assetId);
							log.info("Orphaned title " + assetId + " deleted after purge of material " + materialID);
						}
						else if (childAssets.size() == 1)
						{
							AttributeMap childAsset = childAssets.get(0);
							String childId = childAsset.getAttribute(Attribute.ASSET_ID);
							if (childId != null && childId.equals(assetAttributes.getAttributeAsString(Attribute.ASSET_ID)))
							{
								client.assetApi().deleteAsset(MayamAssetType.TITLE.getAssetType(), assetId);
								log.info("Orphaned title " + assetId + " deleted after purge of material " + materialID);
							}
						}
					}
				}
				
			}
			catch (RemoteException e)
			{
				log.error("Error deleting material : " + materialID, e);
				returnCode = MayamClientErrorCode.MATERIAL_DELETE_FAILED;
			}
		}
		return returnCode;
	}

	public boolean isProtected(String materialID)
	{
		boolean isProtected = false;
		AttributeMap material;
		try
		{
			material = client.assetApi().getAssetBySiteId(MayamAssetType.MATERIAL.getAssetType(), materialID);
			if (material != null)
			{
				isProtected = material.getAttribute(Attribute.PURGE_PROTECTED);
			}
		}
		catch (RemoteException e)
		{
			log.error("Exception thrown by Mayam while checking Protected status of Material : " + materialID, e);
		}
		return isProtected;
	}

	public void updateMaterial(DetailType details, String materialID) throws MayamClientException
	{
		log.warn("no attempt made to update material " + materialID);
		
		AttributeMap materialAttributes = getMaterialAttributes(materialID);
		AttributeMap updateMap = taskController.updateMapForAsset(materialAttributes);
		
	
		 if(details.getTitle() != null){
			 updateMap.setAttribute(Attribute.ASSET_TITLE, details.getTitle());
		 }
		
		 if(details.getFormat() != null){
			 log.warn("have not updated format");
		 }
		 
		 //TODO : update any other metadata
		 
		try
		{
			client.assetApi().updateAsset(updateMap);
		}
		catch (RemoteException e)
		{
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_UPDATE_FAILED, e);
		}
	}

	public List<AttributeMap> getChildren(String assetId, AssetType childAssetType)
	{
		try
		{
			return client.assetApi().getAssetChildren(MayamAssetType.MATERIAL.getAssetType(), assetId, childAssetType);
		}
		catch (RemoteException e)
		{
			log.error("Exception thrown by Mayam while attempting to retrieve children of asset : " + assetId, e);
			e.printStackTrace();
			return null;
		}
	}

	public void verifyFileMaterialFileFormat(AttributeMap qcTaskAttributes) throws MayamClientException
	{
		log.info("Starting File format verification for asset " + qcTaskAttributes.getAttribute(Attribute.HOUSE_ID));

		FileFormatInfo formatInfo = null;

		try
		{
			formatInfo = client.assetApi().getFormatInfo(
					MayamAssetType.MATERIAL.getAssetType(),
					(String) qcTaskAttributes.getAttribute(Attribute.ASSET_ID));
		}
		catch (RemoteException e1)
		{
			log.error("error fetching format info", e1);
			throw new MayamClientException(MayamClientErrorCode.FILE_FORMAT_QUERY_FAILED, e1);
		}

		AttributeMap update = taskController.updateMapForTask(qcTaskAttributes);
		
		try
		{
			fileformatVerification.verifyFileFormat(formatInfo, qcTaskAttributes);
			update.setAttribute(Attribute.QC_SUBSTATUS1, QcStatus.PASS);
			update.setAttribute(Attribute.QC_SUBSTATUS2, QcStatus.TBD);
			update.setAttribute(Attribute.QC_SUBSTATUS3, QcStatus.TBD);
		}
		catch (FileFormatVerificationFailureException ffve)
		{
			log.warn("file format verification failed", ffve);
			update.setAttribute(Attribute.QC_SUBSTATUS1, QcStatus.FAIL);
			update.setAttribute(Attribute.QC_SUBSTATUS2, QcStatus.TBD);
			update.setAttribute(Attribute.QC_SUBSTATUS3, QcStatus.TBD);
			update.setAttribute(Attribute.QC_STATUS, QcStatus.FAIL);
			update.setAttribute(Attribute.QC_SUBSTATUS1_NOTES, ffve.getMessage());
		}

		taskController.saveTask(update);
	}

	public boolean isFileFormatVerificationRequiredForMaterial(AttributeMap messageAttributes)
	{
		return true;
	}

	public boolean isFileFormatVerificationRunForMaterial(AttributeMap messageAttributes)
	{
		QcStatus fileFormatVerification = messageAttributes.getAttribute(Attribute.QC_SUBSTATUS1);
		if (fileFormatVerification == null || fileFormatVerification.equals(QcStatus.TBD))
		{
			return false;
		}
		return true;
	}

	@Inject
	@Named("service.properties")
	protected PropertyFile serviceProperties;

	public boolean isAutoQcRequiredForMaterial(AttributeMap messageAttributes)
	{
		Boolean autoQcRequired = messageAttributes.getAttribute(Attribute.QC_REQUIRED);

		if (autoQcRequired != null && autoQcRequired.booleanValue() == true)
		{
			return true;
		}

		String aggregator = messageAttributes.getAttribute(Attribute.AGGREGATOR);
		if (aggregator != null)
		{
			boolean aggregatorRequiresAutoQc = serviceProperties.getBoolean("aggregators." + aggregator.toLowerCase()
					+ ".requiresQC", false);
			if(aggregatorRequiresAutoQc){
				return true;
			}
		}
		
		if(AssetProperties.isQCParallel(messageAttributes)){
			return true;
		}

		return false;
	}

	public boolean isAutoQcRunOrRunningForMaterial(AttributeMap messageAttributes)
	{

		boolean run = false;
		QcStatus autoQcResult = messageAttributes.getAttribute(Attribute.QC_SUBSTATUS2);
		if (autoQcResult == null || autoQcResult.equals(QcStatus.TBD))
		{
			run = false;
		}
		else
		{
			run = true;
		}

		boolean running = false;

		TaskState taskState = messageAttributes.getAttribute(Attribute.TASK_STATE);
		if (taskState == TaskState.ACTIVE)
		{
			running = true;
		}
		else
		{
			running = false;
		}

		return run || running;
	}

	public void uningest(AttributeMap materialAttributes) throws MayamClientException
	{
		AssetType assetType = materialAttributes.getAttribute(Attribute.ASSET_TYPE);
		String assetID = materialAttributes.getAttribute(Attribute.ASSET_ID);
		String houseID = materialAttributes.getAttributeAsString(Attribute.HOUSE_ID);
		
		String previewStatus = (String) materialAttributes.getAttribute(Attribute.QC_PREVIEW_RESULT);
		
		if (MayamPreviewResults.isPreviewPass(previewStatus))
		{
			log.info("Ignoring uningest request for material, material has passed preview");
		}
		else
		{
			log.info(String.format("Requesting uningest for asset %s (%s)", houseID, assetID));
			try
			{
				client.assetApi().deleteAssetMedia(assetType, assetID);
			}
			catch (RemoteException e)
			{
				log.error("Uningest request failed", e);
				throw new MayamClientException(MayamClientErrorCode.UNINGEST_FAILED, e);
			}

			deleteAssetsPackages(assetType, assetID, houseID);
			
			try
			{
				taskController.cancelAllOpenTasksForAsset(assetType, Attribute.ASSET_ID, assetID);
			}
			catch (MayamClientException e)
			{
				log.error("error cancelling open tasks for assset", e);
			}
			
			// create ingest task
			log.debug("creating new ingest task");
			taskController.createIngestTaskForMaterial(houseID,
					(Date) materialAttributes.getAttribute(Attribute.COMPLETE_BY_DATE));
		}
	}

	private void deleteAssetsPackages(AssetType assetType, String assetID, String houseID)
	{
		try
		{
			log.info(String.format("Searching for packages of asset %s (%s) for deletion", houseID, assetID));
			List<SegmentList> packages = client.segmentApi().getSegmentListsForAsset(assetType, assetID);
			log.info(String.format("Found %d packages for asset %s",packages.size(), houseID));
			for (SegmentList segmentList : packages)
			{
				
				log.debug(String.format("Removing tasks of segment %s", segmentList.getId()));
				try
				{
					taskController.cancelAllOpenTasksForAsset(
							MayamAssetType.PACKAGE.getAssetType(),
							Attribute.ASSET_ID,
							segmentList.getId());
				}
				catch (MayamClientException e)
				{
					log.error("error closing open tasks for asset", e);
					// dont rethrow, we still want the delete to go ahead;
				}
				
				log.debug(String.format("Deleting segment %s", segmentList.getId()));
				try
				{
					client.segmentApi().deleteSegmentList(segmentList.getId());
				}
				catch (RemoteException e)
				{
					log.error(String.format("error deleting segment %s", segmentList.getId()), e);
				}
			}

		}
		catch (RemoteException e)
		{
			log.error(String.format("error fetching packages for asset %s (%s)", houseID, assetID), e);
		}
	}

	public MarkerList getMarkers(AttributeMap messageAttributes) throws MayamClientException
	{
		
		String assetID = messageAttributes.getAttributeAsString(Attribute.ASSET_ID);
		String user = messageAttributes.getAttributeAsString(Attribute.TASK_UPDATED_BY);
		String revisionId = null;
		try
		{
			revisionId = RevisionUtil.findHighestRevision(assetID, client);
		}
		catch (RemoteException e)
		{
			log.error("Error finding highest reivsion for asset "+ assetID,e);		
			throw new MayamClientException(MayamClientErrorCode.REVISION_FIND_FAILED,e);
		}
		
		MarkerList markers = null;
		
		try
		{
			markers = client.assetApi().getMarkers(AssetType.ITEM, assetID, revisionId);
		}
		catch (RemoteException e)
		{
			log.error(String.format("Error fetching markerts for revision %s asset %s",revisionId,assetID));
		}
		
		if (markers == null)
		{
			log.info(String.format("No markers found for revision %s asset %s", revisionId, assetID));
			return null;
		}
		else
		{
			log.info(String.format("found %d markers  ",markers.size()));
			
			return markers;
		}		
	}
	
	public String getAssetPath(String assetID) throws MayamClientException{

		FileFormatInfo fileinfo;
		try
		{
			fileinfo = client.assetApi().getFormatInfo(MayamAssetType.MATERIAL.getAssetType(), assetID);
		}
		catch (RemoteException e)
		{
			log.error("error getting file format info for asset "+assetID,e);
			throw new MayamClientException(MayamClientErrorCode.FILE_FORMAT_QUERY_FAILED,e);
		}
		
		
		List<String> urls = fileinfo.getUrls();
		
		if(urls == null || urls.size()==0){
			log.error("no urls for media found!");
			throw new MayamClientException(MayamClientErrorCode.FILE_LOCATON_QUERY_FAILED);
		}
		
		String url = urls.get(0);
		String nixPath;
		try
		{
			nixPath = pathResolver.nixPath(PathType.FTP, url);
			return nixPath;
		}
		catch (UnknownPathException e)
		{
			log.error(String.format("Unable to resolve storage path for ftp location %s",url),e);
			throw new MayamClientException(MayamClientErrorCode.FILE_LOCATON_QUERY_FAILED,e);
		}
	}
	
}
