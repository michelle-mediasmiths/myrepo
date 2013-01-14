package com.mediasmiths.mayam.controllers;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.Aggregation;
import au.com.foxtel.cf.mam.pms.Aggregator;
import au.com.foxtel.cf.mam.pms.Compile;
import au.com.foxtel.cf.mam.pms.Library;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.Order;
import au.com.foxtel.cf.mam.pms.QualityCheckEnumType;

import au.com.foxtel.cf.mam.pms.Source;
import au.com.foxtel.cf.mam.pms.TapeType;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.*;
import com.mayam.wf.attributes.shared.type.SegmentList.SegmentListBuilder;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.AudioEncodingEnumType;
import com.mediasmiths.foxtel.generated.MaterialExchange.AudioTrackEnumType;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks.Track;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType;
import com.mediasmiths.foxtel.generated.ruzz.DetailType;
import com.mediasmiths.mayam.MayamAspectRatios;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamAudioEncoding;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.util.SegmentUtil;
import com.mediasmiths.std.types.Framerate;
import com.mediasmiths.std.types.SampleCount;
import com.mediasmiths.std.types.Timecode;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamMaterialController extends MayamController
{

	public static final String PROGRAMME_MATERIAL_AGL_NAME = "programme";
	public static final String PROGRAMME_MATERIAL_CONTENT_TYPE = "PG";

	private static final String ASSOCIATED_MATERIAL_AGL_NAME = "associated";
	private static final String ASSOCIATED_MATERIAL_CONTENT_TYPE = "PE";

	private final TasksClient client;
	private final MayamTaskController taskController;
	private final static Logger log = Logger.getLogger(MayamMaterialController.class);

	@Inject
	public MayamMaterialController(@Named(SETUP_TASKS_CLIENT) TasksClient mayamClient, MayamTaskController mayamTaskController)
	{
		client = mayamClient;
		taskController = mayamTaskController;
	}

	public MayamClientErrorCode createMaterial(MaterialType material, String titleID)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;
		boolean createCompLoggingTask = false;

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
						attributesValid &= attributes.setAttribute(Attribute.PURGE_PROTECTED, isProtected.booleanValue());

					String assetId = title.getAttribute(Attribute.ASSET_ID);
					attributesValid &= attributes.setAttribute(Attribute.ASSET_PARENT_ID, assetId);
					
					updateMaterialAttributesFromTitle(attributes, title);
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

			AttributeMap result;
			try
			{
				result = client.assetApi().createAsset(attributes.getAttributes());
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

							long taskID = taskController.createComplianceLoggingTaskForMaterial(material.getMaterialID(), requiredBy);
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

							long taskID = taskController.createIngestTaskForMaterial(material.getMaterialID(), requiredBy);
							log.debug("created task with id : " + taskID);
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

	private void updateMaterialAttributesFromTitle(MayamAttributeController attributes, AttributeMap title)
	{
		//copy metadata from title onto material
		
		//Asset Title
		attributes.copyAttribute(Attribute.ASSET_TITLE, title);
		
		//AdultContent
		//TODO which attribute is for this
		//attributes.copyAttribute(Attribute.??????, title);
		
		//ProgrammeTitle
		attributes.copyAttribute(Attribute.SERIES_TITLE, title);
		
		//Show
		
		//Series/Season Number
		attributes.copyAttribute(Attribute.SEASON_NUMBER, title);
		//Episode Number
		attributes.copyAttribute(Attribute.EPISODE_NUMBER, title);
		//Episode Title
		attributes.copyAttribute(Attribute.EPISODE_TITLE, title);
		//Production Country
		attributes.copyAttribute(Attribute.LOCATION, title);
		//Production Number
		attributes.copyAttribute(Attribute.PRODUCTION_NUMBER, title);
		//Production Year
		attributes.copyAttribute(Attribute.SERIES_YEAR, title);
		//Style
		attributes.copyAttribute(Attribute.CONT_CATEGORY, title);
		//Channels
		attributes.copyAttribute(Attribute.CHANNELS, title);
		
		//Channel Groups
		//TODO which attribute is for this
		//attributes.copyAttribute(Attribute.????, title);
	}

	/**
	 * Creates a material, returns the id of the created material
	 * 
	 * @param material
	 * @return
	 * @throws MayamClientException
	 */
	public String createMaterial(MarketingMaterialType material, String titleID) throws MayamClientException
	{
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;

		if (material != null)
		{

			// setting parent_house_id is an unsupported operation
			// attributesValid &= attributes.setAttribute(Attribute.PARENT_HOUSE_ID, titleID);
			try
			{
				AttributeMap title = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), titleID);
				if (title != null)
				{
					String assetId = title.getAttribute(Attribute.ASSET_ID);
					attributesValid &= attributes.setAttribute(Attribute.ASSET_PARENT_ID, assetId);
					
					updateMaterialAttributesFromTitle(attributes, title);
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

			if (!attributesValid)
			{
				log.error("Invalid attributes on material create request");
				throw new MayamClientException(MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES);
			}

			AttributeMap result;
			try
			{
				result = client.assetApi().createAsset(attributes.getAttributes());
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
				long taskID = taskController.createIngestTaskForMaterial(siteID, null);
				log.debug("created task with id : " + taskID);
			}
			catch (Exception e)
			{
				log.error("error creating ingest task for asset " + siteID, e);
			}

			return siteID;
			// AttributeMap item;
			// try
			// {
			// item = client.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), assetID);
			// String siteID = item.getAttribute(Attribute.ASSET_SITE_ID);
			// log.info(String.format("item id %s returned after fetching created item", siteID));
			//
			// if (siteID == null)
			// {
			// throw new MayamClientException(MayamClientErrorCode.MATERIAL_CREATION_FAILED);
			// }
			//
			// try {
			// long taskID = taskController.createTask(siteID, MayamAssetType.MATERIAL, MayamTaskListType.INGEST);
			// log.debug("created task with id : "+taskID);
			// AttributeMap newTask = taskController.getTask(taskID);
			// newTask.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
			// taskController.saveTask(newTask);
			// }
			// catch (RemoteException e) {
			// log.error("Exception thrown in Mayam while creating Ingest task for Material : " + siteID, e);
			// }
			//
			//
			// return siteID;
			// }
			// catch (RemoteException e)
			// {
			// log.error("Exception thrown by Mayam while fetch newly create Material", e);
			// throw new MayamClientException(MayamClientErrorCode.MATERIAL_CREATION_FAILED, e);
			// }

		}
		else
		{
			log.warn("Null material object, unable to create asset");
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_UNAVAILABLE);
		}
	}

	// Material - Updating a media asset in Mayam
	public MayamClientErrorCode updateMaterial(ProgrammeMaterialType material)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		boolean attributesValid = true;

		if (material != null)
		{
			MayamAttributeController attributes = null;
			AttributeMap assetAttributes = null;

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
				attributes.setAttribute(Attribute.ASSET_ID, assetAttributes.getAttribute(Attribute.ASSET_ID));
				attributes.setAttribute(Attribute.ASSET_TYPE, assetAttributes.getAttribute(Attribute.ASSET_TYPE));

				attributesValid &= attributes.setAttribute(
						Attribute.CONT_ASPECT_RATIO,
						MayamAspectRatios.mayamAspectRatioMappings.get(material.getAspectRatio()));
				attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, material.getFormat());

				String houseID = material.getMaterialID();

				AttributeMap asset;
				try
				{
					asset = client.assetApi().getAssetBySiteId(AssetType.ITEM, houseID);
				}
				catch (RemoteException e1)
				{
					log.error("Error thrown by Mayam while getting asset data for material ID: " + houseID, e1);
					return MayamClientErrorCode.MATERIAL_FIND_FAILED;
				}
				String assetID = asset.getAttributeAsString(Attribute.ASSET_ID);

				
				  
				// FX-113 - original conform information should be converted to a human readable format for the natural breaks structure

				if(material.getOriginalConform() != null){
					
					log.debug("programme material message contains original conform information");
					SegmentationType originalConform = material.getOriginalConform();
					log.debug(String.format("%d segments in original conform",originalConform.getSegment().size()));
					StringBuilder sb = new StringBuilder();
					
					for(com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment s : originalConform.getSegment()){
						
						s = SegmentUtil.fillEomAndDurationOfSegment(s);
						String segmentAsString = SegmentUtil.segmentToString(s);
						sb.append(segmentAsString);						
					}
					
					String originalConformInfo = sb.toString();
					attributesValid &= attributes.setAttribute(Attribute.AUX_VAL, originalConformInfo);
					
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



	

	public MayamClientErrorCode updateMaterial(MaterialType material)
	{
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
							AttributeMap compLoggingTask = taskController.getTaskForAssetBySiteID(
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

							AttributeMap ingestTask = taskController.getTaskForAssetBySiteID(
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
			log.debug("Exception thrown by Mayam while attempting to retrieve asset :" + materialID +" , assuming it doesnt exist");
			log.trace(e1);
		}

		log.debug("Material found: " + materialFound);
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
			log.error("Exception thrown by Mayam while attempting to retrieve asset :" + materialID);
			e1.printStackTrace();
		}
		return assetAttributes;
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
		if (isProtected(materialID))
		{
			try
			{
				AttributeMap assetAttributes = client.assetApi().getAssetBySiteId(
						MayamAssetType.MATERIAL.getAssetType(),
						materialID);

				AttributeMap taskAttributes = client.createAttributeMap();
				taskAttributes.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.GENERIC_TASK_ERROR);
				taskAttributes.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);

				taskAttributes.setAttribute(Attribute.HOUSE_ID, materialID);
				taskAttributes.putAll(assetAttributes);
				client.taskApi().createTask(taskAttributes);
			}
			catch (RemoteException e)
			{
				log.error("Error creating Purge By BMS task for protected material : " + materialID);
				returnCode = MayamClientErrorCode.MATERIAL_DELETE_FAILED;
			}
		}
		else
		{
			try
			{
				AttributeMap assetAttributes = client.assetApi().getAssetBySiteId(
						MayamAssetType.MATERIAL.getAssetType(),
						materialID);

				client.assetApi().deleteAsset(
						MayamAssetType.MATERIAL.getAssetType(),
						assetAttributes.getAttributeAsString(Attribute.ASSET_ID));
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
		// TODO : perform update
		log.warn("no attempt made to update material " + materialID);
		//
		// AttributeMap itemAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.MATERIAL.getAssetType(), materialID);
		// String assetID = itemAttributes.getAttribute(Attribute.ASSET_ID);
		//
		// AttributeMap updateMap = client.createAttributeMap();
		// updateMap.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.MATERIAL.getAssetType());
		// updateMap.setAttribute(Attribute.ASSET_ID, assetID);
		//
		// if(details.getTitle() != null){
		// updateMap.setAttribute(Attribute.ASSET_TITLE, details.getTitle());
		// }
		//
		// if(details.getFormat() != null){
		//
		// }

	}
	
	public List<AttributeMap> getChildren(String assetId, AssetType childAssetType)
	{
		try {
			return client.assetApi().getAssetChildren(MayamAssetType.MATERIAL.getAssetType(), assetId, childAssetType);
		} catch (RemoteException e) {
			log.error("Exception thrown by Mayam while attempting to retrieve children of asset : " + assetId, e);
			e.printStackTrace();
			return null;
		}
	}
}
