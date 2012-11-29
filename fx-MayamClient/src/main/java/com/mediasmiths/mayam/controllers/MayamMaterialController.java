package com.mediasmiths.mayam.controllers;

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
import com.mediasmiths.mayam.MayamAspectRatios;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamMaterialController extends MayamController
{

	private static final String PROGRAMME_MATERIAL_AGL_NAME = "programme";

	private final TasksClient client;

	private final static Logger log = Logger.getLogger(MayamMaterialController.class);

	@Inject
	public MayamMaterialController(@Named(SETUP_TASKS_CLIENT) TasksClient mayamClient)
	{
		client = mayamClient;
	}

	public MayamClientErrorCode createMaterial(MaterialType material, String titleID)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;

		if (material != null)
		{

//			attributesValid &= attributes.setAttribute(Attribute.PARENT_HOUSE_ID, titleID);
			
			attributesValid &= attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.MATERIAL.getAssetType());
			attributesValid &= attributes.setAttribute(Attribute.METADATA_FORM, PROGRAMME_MATERIAL_AGL_NAME);
			attributesValid &= attributes.setAttribute(Attribute.HOUSE_ID, material.getMaterialID());
			
			if(material.getQualityCheckTask() != null)			
			attributesValid &= attributes.setAttribute(Attribute.QC_REQUIRED, material.getQualityCheckTask().toString());
			attributesValid &= attributes.setAttribute(Attribute.REQ_FMT, material.getRequiredFormat());

			//Mayam and Foxtel have agreed that Required by is not required in the AGL
			//attributesValid &= attributes.setAttribute(Attribute.TX_NEXT, material.getRequiredBy());
			
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
						//attributesValid &= attributes.setAttribute(Attribute.APP_SRC, aggregator.getAggregatorName());
					}
					if (order != null)
					{
						// Order Created field not required
						//attributesValid &= attributes.setAttribute(Attribute.OP_DATE, order.getOrderCreated());
						attributesValid &= attributes.setAttribute(Attribute.REQ_REFERENCE, order.getOrderReference());
					}
				}

				Compile compile = source.getCompile();
				if (compile != null)
				{
					
					//is parent_house_id suitable for this, as parent in this context is a material rather than a title
					attributesValid &= attributes.setAttribute(Attribute.PARENT_HOUSE_ID, compile.getParentMaterialID());
					try {
						AttributeMap title = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), compile.getParentMaterialID());
						if (title != null) {
							boolean isProtected = title.getAttribute(Attribute.PURGE_PROTECTED);
							attributesValid &= attributes.setAttribute(Attribute.PURGE_PROTECTED, isProtected);
						}
					} catch (RemoteException e) {
						log.error("MayamException while trying to retrieve title : " + compile.getParentMaterialID(),e);						
					}
				}

				Library library = source.getLibrary();
				if (library != null)
				{
					List<TapeType> tapeList = library.getTape();
					if (tapeList != null)
					{
						IdSet tapeIds = new IdSet();
						for (int i = 0; i < tapeList.size(); i++)
						{
							TapeType tape = tapeList.get(i);
							if (tape != null)
							{
								tapeIds.add(tape.getPresentationID());
							}
						}
						attributesValid &= attributes.setAttribute(Attribute.SOURCE_IDS, tapeIds);
					}
				}
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
			log.warn("Null material object, unable to create asset");
			return MayamClientErrorCode.MATERIAL_UNAVAILABLE;
		}
		return returnCode;
	}

	/**
	 * Creates a material, returns the id of the created material
	 * @param material
	 * @return
	 * @throws MayamClientException 
	 */
	public String createMaterial(MarketingMaterialType material) throws MayamClientException
	{
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;

		if (material != null)
		{
			attributesValid &= attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.MATERIAL.getAssetType());
			attributesValid &= attributes.setAttribute(Attribute.METADATA_FORM, PROGRAMME_MATERIAL_AGL_NAME);

			attributesValid &= attributes.setAttribute(Attribute.CONT_ASPECT_RATIO, MayamAspectRatios.mayamAspectRatioMappings.get(material.getAspectRatio()));
			attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, material.getFormat());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.CONT_RESTRICTED_MATERIAL, material.isAdultMaterial());
			
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
				audioTrack.setEncoding(AudioTrack.EncodingType.valueOf(track.getTrackEncoding().toString())); 
				audioTrack.setName(track.getTrackName().toString()); 
				audioTrack.setNumber(track.getTrackNumber());
				audioTrackList.set(i, audioTrack);
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
				log.error("Exception thrown by Mayam while trying to create Material");
				throw new MayamClientException(MayamClientErrorCode.MAYAM_EXCEPTION);
			}
			
			String materialID = result.getAttribute(Attribute.HOUSE_ID);
			return materialID;
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
				assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.MATERIAL.getAssetType(), material.getMaterialID());
			}
			catch (RemoteException e1)
			{
				log.error("Exception thrown by Mayam while attempting to retrieve asset :" + material.getMaterialID());
				e1.printStackTrace();
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}

			if (assetAttributes != null)
			{
				attributes = new MayamAttributeController(assetAttributes);

				attributesValid &= attributes.setAttribute(Attribute.CONT_ASPECT_RATIO, MayamAspectRatios.mayamAspectRatioMappings.get(material.getAspectRatio()));
				attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, material.getFormat());

				// As per Foxtel and Mayam decision, duration and timecodes will be detected in Ardome, no need to store
				// material.getLastFrameTimecode();
				// material.getFirstFrameTimecode();
				// attributesValid &= attributes.setAttribute(Attribute.ASSET_DURATION, material.getDuration());

				String assetID = material.getMaterialID();
				try {
					AttributeMap asset = client.assetApi().getAssetBySiteId(AssetType.ITEM, assetID);
					String revisionID = asset.getAttribute(Attribute.REVISION_ID);
			
					SegmentationType segmentation = material.getOriginalConform(); 
					if (segmentation != null)
					{
						List<SegmentationType.Segment> segments = segmentation.getSegment(); 
						for (int i = 0; i < segments.size(); i++) 
						{ 
							SegmentationType.Segment segment = segments.get(i); 
							if (segment != null) 
							{
								ValueList metadata = new ValueList();
								metadata.add(new ValueList.Entry("DURATION", segment.getDuration())); 
								metadata.add(new ValueList.Entry("EOM", segment.getEOM())); 
								metadata.add(new ValueList.Entry("SOM", segment.getSOM())); 
								metadata.add(new ValueList.Entry("SEGMENT_NUMBER", "" + segment.getSegmentNumber())); 
								metadata.add(new ValueList.Entry("SEGMENT_TITLE", segment.getSegmentTitle())); 
								
								SegmentListBuilder listBuilder = SegmentList.create("Asset " + assetID + " Segment " + segment.getSegmentNumber());
								listBuilder = listBuilder.metadataForm(PROGRAMME_MATERIAL_AGL_NAME); 
								listBuilder = listBuilder.metadata(metadata);
								SegmentList list = listBuilder.build();
								client.segmentApi().updateSegmentList(revisionID, list);
							}
							else {
								log.error("Segment data is null for asset ID: " + assetID);
							}
						}
					}
				}
				catch(RemoteException e)
				{
					log.error("Error thrown by Mayam while updating Segmentation data for asset ID: " + assetID,e);
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
							audioTrack.setEncoding(AudioTrack.EncodingType.valueOf(track.getTrackEncoding().toString())); 
							audioTrack.setName(track.getTrackName().toString()); 
							audioTrack.setNumber(track.getTrackNumber());
							audioTrackList.set(i, audioTrack);
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
					log.debug("updating material "+material.getMaterialID());
					result = client.assetApi().updateAsset(attributes.getAttributes());
					if (result == null)
					{
						log.warn("Mayam failed to update Material");
						returnCode = MayamClientErrorCode.MATERIAL_UPDATE_FAILED;
					}
				}
				catch (RemoteException e)
				{
					log.error("Exception thrown by Mayam while trying to update Material",e);
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
				assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.MATERIAL.getAssetType(), material.getMaterialID());
			}
			catch (RemoteException e1)
			{
				log.error("Exception thrown by Mayam while attempting to retrieve asset :" + material.getMaterialID());
				e1.printStackTrace();
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}

			if (assetAttributes != null)
			{
				attributes = new MayamAttributeController(assetAttributes);

				if(material.getQualityCheckTask() != null)
				attributesValid &= attributes.setAttribute(Attribute.QC_REQUIRED, material.getQualityCheckTask().toString());
				attributesValid &= attributes.setAttribute(Attribute.REQ_FMT, material.getRequiredFormat());
				
				// Required By does not need to be stored in AGL
				//if (material.getRequiredBy() != null && material.getRequiredBy().toGregorianCalendar() != null)
				//{
					//attributesValid &= attributes.setAttribute(Attribute.TX_NEXT, material.getRequiredBy().toGregorianCalendar().getTime());
				//}
				
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
							//attributesValid &= attributes.setAttribute(Attribute.APP_SRC, aggregator.getAggregatorName());
						}
						if (order != null)
						{
							// TODO: Order Created date still to be added by Mayam
							//attributesValid &= attributes.setAttribute(Attribute.OP_DATE, order.getOrderCreated().toGregorianCalendar().getTime());
							attributesValid &= attributes.setAttribute(Attribute.REQ_REFERENCE, order.getOrderReference());
						}
					}

					Compile compile = source.getCompile();
					if (compile != null)
					{
						attributesValid &= attributes.setAttribute(Attribute.PARENT_HOUSE_ID, compile.getParentMaterialID());
					}

					Library library = source.getLibrary();
					if (library != null)
					{
						List<TapeType> tapeList = library.getTape();
						if (tapeList != null)
						{
							IdSet tapeIds = new IdSet();
							for (int i = 0; i < tapeList.size(); i++)
							{
								TapeType tape = tapeList.get(i);
								if (tape != null)
								{
									tapeIds.add(tape.getPresentationID());
								}
							}
							attributesValid &= attributes.setAttribute(Attribute.SOURCE_IDS, tapeIds);
						}
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
					log.debug("updating material "+material.getMaterialID());
					result = client.assetApi().updateAsset(attributes.getAttributes());
					if (result == null)
					{
						log.warn("Mayam failed to update Material");
						returnCode = MayamClientErrorCode.MATERIAL_UPDATE_FAILED;
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
			log.debug("Exception thrown by Mayam while attempting to retrieve asset :" + materialID,e1);			
		}
		
		log.debug("Material found: "+materialFound);
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
		String qc = (String) attributes.getAttribute(Attribute.QC_REQUIRED);

		if (qc != null)
		{
			material.setQualityCheckTask(QualityCheckEnumType.valueOf(qc));
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

		Library library = new Library();
		IdSet tapeIds = attributes.getAttribute(Attribute.SOURCE_IDS);
		String[] tapeIdsArrays = (String[]) tapeIds.toArray();
		ArrayList<TapeType> tapeList = new ArrayList<TapeType>();
		for (int i = 0; i < tapeIdsArrays.length; i++)
		{
			TapeType tape = new TapeType();
			tape.setLibraryID(tapeIdsArrays[i]);
			tapeList.add(tape);
		}
		library.withTape(tapeList);
		source.setLibrary(library);

		return material;
	}
	
	public com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType getMaterialType(String materialID){
		AttributeMap attributes = getMaterialAttributes(materialID);
		
		//IF revision ID exists then the asset has associated Segment data making it Programme material rather than Marketing Material
		String revisionID = attributes.getAttribute(Attribute.REVISION_ID);
		if (revisionID == null || revisionID.equals(""))
		{
			return getMarketingMaterial(materialID);
		}
		else {
			return getProgrammeMaterial(materialID);
		}
	}
	/**
	 * Returns the MarketingMaterialType representation of a material, does not include media type or packages
	 * @param materialID
	 * @return
	 */
	public MarketingMaterialType getMarketingMaterial(String materialID)
	{
		AttributeMap attributes = getMaterialAttributes(materialID);
		MarketingMaterialType mmt = new MarketingMaterialType();

		if (checkAttributeValid(attributes, Attribute.CONT_RESTRICTED_MATERIAL, materialID, "Adult only", Boolean.class))
		{
			mmt.setAdultMaterial((Boolean) attributes.getAttribute(Attribute.CONT_RESTRICTED_MATERIAL));
		}

		if (checkAttributeValid(attributes, Attribute.CONT_ASPECT_RATIO, materialID, "Aspect ratio", AspectRatio.class))
		{
			mmt.setAspectRatio(((AspectRatio) attributes.getAttribute(Attribute.CONT_ASPECT_RATIO)).toString());
		}

		AudioTrackList audioTrackList = attributes.getAttribute(Attribute.AUDIO_TRACKS); 
		if (audioTrackList != null) 
		{
			AudioTracks audioTracks = new AudioTracks();
			for (int i = 0; i < audioTrackList.size(); i++)
			{
				AudioTrack track = audioTrackList.get(i);
				Track newTrack = new Track();
				newTrack.setTrackEncoding(AudioEncodingEnumType.valueOf(track.getEncoding().toString()));
				newTrack.setTrackName(AudioTrackEnumType.valueOf(track.getName()));
				newTrack.setTrackNumber(track.getNumber());
				audioTracks.getTrack().add(newTrack);
			}
			mmt.setAudioTracks(audioTracks);
		}

		if (checkAttributeValid(attributes, Attribute.CONT_FMT, materialID, "Content format", String.class))
		{
			mmt.setFormat((String) attributes.getAttribute(Attribute.CONT_FMT));
		}

		if (!attributes.getAttribute(Attribute.HOUSE_ID).equals(materialID))
		{
			log.error("unexpected asset id for material " + materialID);
		}
		
		return mmt;
	}
	
	/**
	 * Returns the ProgrammeMaterialType representation of a material, does not include media type or packages
	 * @param materialID
	 * @return
	 */
	public ProgrammeMaterialType getProgrammeMaterial(String materialID)
	{

		AttributeMap attributes = getMaterialAttributes(materialID);

		ProgrammeMaterialType pmt = new ProgrammeMaterialType();

		if (checkAttributeValid(attributes, Attribute.CONT_RESTRICTED_MATERIAL, materialID, "Adult only", Boolean.class))
		{
			pmt.setAdultMaterial((Boolean) attributes.getAttribute(Attribute.CONT_RESTRICTED_MATERIAL));
		}

		if (checkAttributeValid(attributes, Attribute.CONT_ASPECT_RATIO, materialID, "Aspect ratio", AspectRatio.class))
		{
			pmt.setAspectRatio(((AspectRatio) attributes.getAttribute(Attribute.CONT_ASPECT_RATIO)).toString());
		}

		AudioTrackList audioTrackList = attributes.getAttribute(Attribute.AUDIO_TRACKS); 
		if (audioTrackList != null) 
		{
			AudioTracks audioTracks = new AudioTracks();
			for (int i = 0; i < audioTrackList.size(); i++)
			{
				AudioTrack track = audioTrackList.get(i);
				Track newTrack = new Track();
				newTrack.setTrackEncoding(AudioEncodingEnumType.valueOf(track.getEncoding().toString()));
				newTrack.setTrackName(AudioTrackEnumType.valueOf(track.getName()));
				newTrack.setTrackNumber(track.getNumber());
				audioTracks.getTrack().add(newTrack);
			}
			pmt.setAudioTracks(audioTracks);
		}

		if (checkAttributeValid(attributes, Attribute.CONT_FMT, materialID, "Content format", String.class))
		{
			pmt.setFormat((String) attributes.getAttribute(Attribute.CONT_FMT));
		}

		if (!attributes.getAttribute(Attribute.HOUSE_ID).equals(materialID))
		{
			log.error("unexpected asset id for material " + materialID);
		}

		pmt.setMaterialID(materialID);

		try {
			String revisionID = attributes.getAttribute(Attribute.REVISION_ID);
			SegmentList list = client.segmentApi().getSegmentList(revisionID);

			List<Segment> segList = list.getEntries();
			SegmentationType segmentation = new SegmentationType();
			for (int i = 0; i < segList.size(); i++)
			{
				Segment segment = segList.get(i);
				SegmentationType.Segment newSegment = new SegmentationType.Segment();
				newSegment.setDuration(segment.getDuration().toString());
				newSegment.setSegmentNumber(segment.getNumber());
				newSegment.setSegmentTitle(segment.getTitle());
				segmentation.getSegment().add(newSegment);
			}
			pmt.setOriginalConform(segmentation);
		} catch (RemoteException e) {
			log.error("Exception thrown by Mayam while retrieving Segmentation data for Material : " + materialID);
			e.printStackTrace();
		}
		
		return pmt;
	}

	public MayamClientErrorCode deleteMaterial(String materialID)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		if (isProtected(materialID)) {
			try
			{
				AttributeMap assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.MATERIAL.getAssetType(), materialID);
				
				AttributeMap taskAttributes = client.createAttributeMap();
				taskAttributes.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PURGE_BY_BMS);
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
		else {
			try
			{
				client.assetApi().deleteAsset(MayamAssetType.MATERIAL.getAssetType(), materialID);
			}
			catch (RemoteException e)
			{
				log.error("Error deleting material : " + materialID);
				returnCode = MayamClientErrorCode.MATERIAL_DELETE_FAILED;
			}
		}
		return returnCode;
	}
	
	public boolean isProtected(String materialID)
	{
		boolean isProtected = false;
		AttributeMap material;
		try {
			material = client.assetApi().getAssetBySiteId(MayamAssetType.MATERIAL.getAssetType(), materialID);
			if (material != null) {
				isProtected = material.getAttribute(Attribute.PURGE_PROTECTED);
			}
		} catch (RemoteException e) {
			log.error("Exception thrown by Mayam while checking Protected status of Material : " + materialID);
			e.printStackTrace();
		}
		return isProtected;
	}
}
