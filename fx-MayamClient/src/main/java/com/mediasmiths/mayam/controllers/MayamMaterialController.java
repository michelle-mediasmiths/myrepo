package com.mediasmiths.mayam.controllers;

import java.util.Date;
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
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks.Track;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType;
import com.mediasmiths.mayam.DateUtil;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamMaterialController extends MayamController
{

	private final TasksClient client;
	private final DateUtil dateUtil;

	private final static Logger log = Logger.getLogger(MayamMaterialController.class);

	@Inject
	public MayamMaterialController(@Named(SETUP_TASKS_CLIENT) TasksClient mayamClient, DateUtil dateUtil)
	{
		client = mayamClient;
		this.dateUtil = dateUtil;
	}

	public MayamClientErrorCode createMaterial(MaterialType material)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;

		if (material != null)
		{
			attributesValid &= attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.MATERIAL.getAssetType());
			attributesValid &= attributes.setAttribute(Attribute.ASSET_ID, material.getMaterialID());
			attributesValid &= attributes.setAttribute(Attribute.QC_NOTES, material.getQualityCheckTask().toString());
			attributesValid &= attributes.setAttribute(Attribute.TX_NEXT, material.getRequiredBy());
			attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, material.getRequiredFormat());

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
						// TODO: Approval attributes are not ideal for aggregator values
						attributesValid &= attributes.setAttribute(Attribute.APP_ID, aggregator.getAggregatorID());
						attributesValid &= attributes.setAttribute(Attribute.APP_SRC, aggregator.getAggregatorName());
					}
					if (order != null)
					{
						// TODO: Task operation attributes are not ideal for aggregator values
						attributesValid &= attributes.setAttribute(Attribute.OP_DATE, order.getOrderCreated());
						attributesValid &= attributes.setAttribute(Attribute.OP_ID, order.getOrderReference());
					}
				}

				Compile compile = source.getCompile();
				if (compile != null)
				{
					attributesValid &= attributes.setAttribute(Attribute.ASSET_PARENT_ID, compile.getParentMaterialID());
					try {
						AttributeMap title = client.assetApi().getAsset(MayamAssetType.TITLE.getAssetType(), compile.getParentMaterialID());
						if (title != null) {
							boolean isProtected = title.getAttribute(Attribute.AUX_FLAG);
							attributesValid &= attributes.setAttribute(Attribute.AUX_FLAG, isProtected);
						}
					} catch (RemoteException e) {
						log.error("MayamException while trying to retrieve title : " + compile.getParentMaterialID());
						e.printStackTrace();
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
			//TODO: Mayam to advise on new attribute for Aspect Ratios
			//attributesValid &= attributes.setAttribute(Attribute.ASPECT_RATIO, MayamAspectRatios.mayamAspectRatioMappings.get(material.getAspectRatio()));
			attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, material.getFormat());
			attributesValid &= attributes.setAttribute(Attribute.ASSET_DURATION, material.getDuration());

			attributesValid = attributesValid && attributes.setAttribute(Attribute.APP_FLAG, material.isAdultMaterial());

			// TODO: Require attributes for timecode
			// attributesValid &= attributes.setAttribute(Attribute., material.getFirstFrameTimecode());
			// attributesValid &= attributes.setAttribute(Attribute., material.getLastFrameTimecode());

			// TODO: What should Media be set as?
			// MediaType media = material.getMedia();

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
			
			String materialID = result.getAttribute(Attribute.ASSET_ID);
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
				assetAttributes = client.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), material.getMaterialID());
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

				//TODO: Mayam to advise on new attribute for Aspect Ratios
				//attributesValid &= attributes.setAttribute(Attribute.ASPECT_RATIO, MayamAspectRatios.mayamAspectRatioMappings.get(material.getAspectRatio()));

				attributesValid &= attributes.setAttribute(Attribute.ASSET_DURATION, material.getDuration());
				attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, material.getFormat());

				// TODO: No appropriate attributes for start and end frame markers
				// material.getLastFrameTimecode();
				// material.getFirstFrameTimecode();

				String assetID = material.getMaterialID();
				try {
					AttributeMap asset = client.assetApi().getAsset(AssetType.ITEM, assetID);
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
								metadata.add(new ValueList.Entry("metadata_field", segment.getDuration())); 
								metadata.add(new ValueList.Entry("metadata_field", segment.getEOM())); 
								metadata.add(new ValueList.Entry("metadata_field", segment.getSOM())); 
								metadata.add(new ValueList.Entry("metadata_field", "" + segment.getSegmentNumber())); 
								metadata.add(new ValueList.Entry("metadata_field", segment.getSegmentTitle())); 
								
								SegmentListBuilder listBuilder = SegmentList.create("Asset " + assetID + " Segment " + segment.getSegmentNumber());
								listBuilder = listBuilder.metadataForm("Material_Segment"); 
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
					log.error("Error thrown by Mayam while updating Segmentation data for asset ID: " + assetID);
					e.printStackTrace();
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
				assetAttributes = client.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), material.getMaterialID());
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

				attributesValid &= attributes.setAttribute(Attribute.QC_NOTES, material.getQualityCheckTask().toString());
				if (material.getRequiredBy() != null && material.getRequiredBy().toGregorianCalendar() != null)
				{
					attributesValid &= attributes.setAttribute(Attribute.TX_NEXT, material.getRequiredBy().toGregorianCalendar().getTime());
				}
				attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, material.getRequiredFormat());

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
							// TODO: Approval attributes are not ideal for aggregator values
							attributesValid &= attributes.setAttribute(Attribute.APP_VAL, aggregator.getAggregatorID());
							attributesValid &= attributes.setAttribute(Attribute.APP_SRC, aggregator.getAggregatorName());
						}
						if (order != null)
						{
							// TODO: Task operation attributes are not ideal for aggregator values
							attributesValid &= attributes.setAttribute(Attribute.OP_DATE, order.getOrderCreated().toGregorianCalendar().getTime());
							attributesValid &= attributes.setAttribute(Attribute.OP_VAL, order.getOrderReference());
						}
					}

					Compile compile = source.getCompile();
					if (compile != null)
					{
						attributesValid &= attributes.setAttribute(Attribute.ASSET_PARENT_ID, compile.getParentMaterialID());
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
			AttributeMap assetAttributes = client.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), materialID);
			if (assetAttributes != null)
			{
				materialFound = true;
			}
		}
		catch (RemoteException e1)
		{
			log.error("Exception thrown by Mayam while attempting to retrieve asset :" + materialID);
			e1.printStackTrace();
		}
		return materialFound;
	}

	public AttributeMap getMaterialAttributes(String materialID)
	{
		AttributeMap assetAttributes = null;
		try
		{
			assetAttributes = client.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), materialID);
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

		material.setMaterialID((String) attributes.getAttribute(Attribute.ASSET_ID));
		String qc = (String) attributes.getAttribute(Attribute.QC_NOTES);

		if (qc != null)
		{
			material.setQualityCheckTask(QualityCheckEnumType.valueOf(qc));
		}
		else
		{
			log.warn(String.format("material %s had null QualityCheck attribute", materialID));
		}

		Date txNext = (Date) attributes.getAttribute(Attribute.TX_NEXT);

		if (txNext != null)
		{
			material.setRequiredBy(dateUtil.fromDate(txNext));
		}
		else
		{
			log.warn(String.format("material %s had null TX_NEXT attribute", materialID));
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

		// TODO: Approval attributes are not ideal for aggregator values
		aggregator.setAggregatorID("" + attributes.getAttribute(Attribute.APP_ID));
		aggregator.setAggregatorName((String) attributes.getAttribute(Attribute.APP_SRC));

		// TODO: Task operation attributes are not ideal for aggregator values
		Date orderCreated = (Date) attributes.getAttribute(Attribute.OP_DATE);

		if (orderCreated != null)
		{
			order.setOrderCreated(dateUtil.fromDate(orderCreated));
		}
		else
		{
			log.warn(String.format("material %s had null order created date", materialID));
		}
		order.setOrderReference("" + attributes.getAttribute(Attribute.OP_ID));

		Compile compile = new Compile();
		source.setCompile(compile);
		compile.setParentMaterialID("" + attributes.getAttribute(Attribute.ASSET_PARENT_ID));

		// TODO : handle source tape info
		// Library library = new Library();
		// source.setLibrary(library);

		return material;

	}
	
	public com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType getMaterialType(String materialID){
		AttributeMap attributes = getMaterialAttributes(materialID);
		
		//TODO get field to check for marketing material / programme material
		boolean isProgrammeMaterial = true;
//		if(isProgrammeMaterial){
//			return getProgrammeMaterial(materialID);
//		}
//		
		
		return getProgrammeMaterial(materialID);
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

		if (checkAttributeValid(attributes, Attribute.APP_FLAG, materialID, "Adult only", Boolean.class))
		{
			pmt.setAdultMaterial((Boolean) attributes.getAttribute(Attribute.APP_FLAG));
		}

		//TODO: Update when new Mayam attribute for Aspect Ratio is added
		if (checkAttributeValid(attributes, Attribute.ASPECT_RATIO, materialID, "Aspect ratio", AspectRatio.class))
		{
			pmt.setAspectRatio(((AspectRatio) attributes.getAttribute(Attribute.ASPECT_RATIO)).toString());
		}

		// TODO audio tracks
		// pmt.setAudioTracks(value);

		if (checkAttributeValid(attributes, Attribute.ASSET_DURATION, materialID, "Asset duration", Integer.class))
		{
			Integer durationInMillis = (Integer) attributes.getAttribute(Attribute.ASSET_DURATION);
			pmt.setDuration(toTimecodeString(durationInMillis));
		}

		// TODO start and end timecodes
		/*
		 * 
		 * if(checkAttributeValid(attributes, Attribute.???, materialID, "First Frame Timecode", Integer.class)){ Integer firstFrameTCinMillis = (Integer)attributes.getAttribute(Attribute.???);
		 * pmt.setFirstFrameTimecode(toTimecodeString(firstFrameTCinMillis)); }
		 * 
		 * if(checkAttributeValid(attributes, Attribute.???, materialID, "Last Frame Timecode", Integer.class)){ Integer firstFrameTCinMillis = (Integer)attributes.getAttribute(Attribute.???);
		 * pmt.setLastFrameTimecode(toTimecodeString(firstFrameTCinMillis)); }
		 */

		if (checkAttributeValid(attributes, Attribute.CONT_FMT, materialID, "Content format", String.class))
		{
			pmt.setFormat((String) attributes.getAttribute(Attribute.CONT_FMT));
		}

		if (!attributes.getAttribute(Attribute.ASSET_ID).equals(materialID))
		{
			log.error("unexpected asset id for material " + materialID);
		}

		pmt.setMaterialID(materialID);

		//TODO segmentation types
		//pmt.setOriginalConform(value);
		
		return pmt;
	}

	public MayamClientErrorCode deleteMaterial(String materialID)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		if (isProtected(materialID)) {
			try
			{
				AttributeMap assetAttributes = client.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), materialID);
				
				AttributeMap taskAttributes = client.createAttributeMap();
				taskAttributes.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PURGE_BY_BMS);
				taskAttributes.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
	
				taskAttributes.setAttribute(Attribute.ASSET_ID, materialID);
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
			material = client.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), materialID);
			if (material != null) {
				isProtected = material.getAttribute(Attribute.AUX_FLAG);
			}
		} catch (RemoteException e) {
			log.error("Exception thrown by Mayam while checking Protected status of Material : " + materialID);
			e.printStackTrace();
		}
		return isProtected;
	}
}
