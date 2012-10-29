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
import com.mayam.wf.attributes.shared.type.AspectRatio;
import com.mayam.wf.attributes.shared.type.IdSet;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.mayam.DateUtil;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;

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
			attributesValid = attributesValid
					&& attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.MATERIAL.getAssetType());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_ID, material.getMaterialID());
			attributesValid = attributesValid
					&& attributes.setAttribute(Attribute.QC_NOTES, material.getQualityCheckTask().toString());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.TX_NEXT, material.getRequiredBy());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.CONT_FMT, material.getRequiredFormat());

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
						attributesValid = attributesValid
								&& attributes.setAttribute(Attribute.APP_ID, aggregator.getAggregatorID());
						attributesValid = attributesValid
								&& attributes.setAttribute(Attribute.APP_SRC, aggregator.getAggregatorName());
					}
					if (order != null)
					{
						// TODO: Task operation attributes are not ideal for aggregator values
						attributesValid = attributesValid && attributes.setAttribute(Attribute.OP_DATE, order.getOrderCreated());
						attributesValid = attributesValid && attributes.setAttribute(Attribute.OP_ID, order.getOrderReference());
					}
				}

				Compile compile = source.getCompile();
				if (compile != null)
				{
					attributesValid = attributesValid
							&& attributes.setAttribute(Attribute.ASSET_PARENT_ID, compile.getParentMaterialID());
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
								// TODO: Should we store the tape library ids, the presentation (package) id, or both?
								// If both are required then we will need a new way of storing the Attribute values
								tape.getLibraryID();
								tapeIds.add(tape.getPresentationID());
							}
						}
						attributesValid = attributesValid && attributes.setAttribute(Attribute.SOURCE_IDS, tapeIds);
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
				result = client.createAsset(attributes.getAttributes());
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

	public MayamClientErrorCode createMaterial(MarketingMaterialType material)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;

		if (material != null)
		{
			// TODO: What all data can we get from this 'Object'
			// Object additionalMaterial = material.getAdditionalMarketingDetail();

			attributesValid = attributesValid && attributes.setAttribute(Attribute.ASPECT_RATIO, material.getAspectRatio());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.CONT_FMT, material.getFormat());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_DURATION, material.getDuration());

			attributesValid = attributesValid && attributes.setAttribute(Attribute.AUX_FLAG, material.isAdultMaterial());

			// TODO: Require attributes for timecode
			// attributesValid = attributesValid && attributes.setAttribute(Attribute., material.getFirstFrameTimecode());
			// attributesValid = attributesValid && attributes.setAttribute(Attribute., material.getLastFrameTimecode());

			// TODO: What should Media be set as?
			// MediaType media = material.getMedia();

			// TODO: List of audio data
			/*
			 * AudioTracks audioTracks = material.getAudioTracks(); List<Track> tracks = audioTracks.getTrack(); for (int i = 0; i < tracks.size(); i++) { Track track = tracks.get(i);
			 * track.getTrackEncoding().toString(); track.getTrackName().toString(); track.getTrackNumber(); }
			 */

			if (!attributesValid)
			{
				log.warn("Material created but one or more attributes was invalid");
				returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
			}

			AttributeMap result;
			try
			{
				result = client.createAsset(attributes.getAttributes());
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
				assetAttributes = client.getAsset(MayamAssetType.MATERIAL.getAssetType(), material.getMaterialID());
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

				// TODO: Confirm aspect ratio is in the correct notation, otherwise have conversion method
				attributesValid = attributesValid
						&& attributes.setAttribute(Attribute.ASPECT_RATIO, AspectRatio.valueOf(material.getAspectRatio()));

				attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_DURATION, material.getDuration());
				attributesValid = attributesValid && attributes.setAttribute(Attribute.CONT_FMT, material.getFormat());

				// TODO: No appropriate attributes for start and end frame markers
				// material.getLastFrameTimecode();
				// material.getFirstFrameTimecode();

				// TODO: How to handle multiple audio tracks and segments?
				/*
				 * SegmentationType segmentation = material.getOriginalConform(); List<Segment> segments = segmentation.getSegment(); for (int i = 0; i < segments.size(); i++) { Segment segment =
				 * segments.get(i); segment.getDuration(); segment.getEOM(); segment.getSegmentNumber(); segment.getSegmentTitle(); segment.getSOM(); }
				 * 
				 * AudioTracks audioTracks = material.getAudioTracks(); List<Track> tracks = audioTracks.getTrack(); for (int i = 0; i < tracks.size(); i++) { Track track = tracks.get(i);
				 * track.getTrackEncoding().toString(); track.getTrackName().toString(); track.getTrackNumber(); }
				 */

				if (!attributesValid)
				{
					log.warn("Material updated but one or more attributes was invalid");
					returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
				}

				AttributeMap result;
				try
				{
					result = client.updateAsset(attributes.getAttributes());
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
				assetAttributes = client.getAsset(MayamAssetType.MATERIAL.getAssetType(), material.getMaterialID());
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

				attributesValid = attributesValid
						&& attributes.setAttribute(Attribute.QC_NOTES, material.getQualityCheckTask().toString());
				if (material.getRequiredBy() != null && material.getRequiredBy().toGregorianCalendar() != null)
				{
					attributesValid = attributesValid
							&& attributes.setAttribute(
									Attribute.TX_NEXT,
									material.getRequiredBy().toGregorianCalendar().getTime());
				}
				attributesValid = attributesValid && attributes.setAttribute(Attribute.CONT_FMT, material.getRequiredFormat());

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
							attributesValid = attributesValid
									&& attributes.setAttribute(Attribute.APP_VAL, aggregator.getAggregatorID());
							attributesValid = attributesValid
									&& attributes.setAttribute(Attribute.APP_SRC, aggregator.getAggregatorName());
						}
						if (order != null)
						{
							// TODO: Task operation attributes are not ideal for aggregator values
							attributesValid = attributesValid
									&& attributes.setAttribute(
											Attribute.OP_DATE,
											order.getOrderCreated().toGregorianCalendar().getTime());
							attributesValid = attributesValid
									&& attributes.setAttribute(Attribute.OP_VAL, order.getOrderReference());
						}
					}

					Compile compile = source.getCompile();
					if (compile != null)
					{
						attributesValid = attributesValid
								&& attributes.setAttribute(Attribute.ASSET_PARENT_ID, compile.getParentMaterialID());
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
									// TODO: Should we store the tape library ids, the presentation (package) id, or both?
									// If both are required then we will need a new way of storing the Attribute values
									tape.getLibraryID();
									tapeIds.add(tape.getPresentationID());
								}
							}
							attributesValid = attributesValid && attributes.setAttribute(Attribute.SOURCE_IDS, tapeIds);
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
					result = client.updateAsset(attributes.getAttributes());
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
			AttributeMap assetAttributes = client.getAsset(MayamAssetType.MATERIAL.getAssetType(), materialID);
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
			assetAttributes = client.getAsset(MayamAssetType.MATERIAL.getAssetType(), materialID);
		}
		catch (RemoteException e1)
		{
			log.error("Exception thrown by Mayam while attempting to retrieve asset :" + materialID);
			e1.printStackTrace();
		}
		return assetAttributes;
	}

	public MaterialType getMaterial(String materialID)
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

	/**
	 * Returns the ProgrammeMaterialType representation of a material, does not include media type or packages
	 * @param materialID
	 * @return
	 */
	public ProgrammeMaterialType getProgrammeMaterial(String materialID)
	{

		AttributeMap attributes = getMaterialAttributes(materialID);

		ProgrammeMaterialType pmt = new ProgrammeMaterialType();

		if (checkAttributeValid(attributes, Attribute.AUX_FLAG, materialID, "Adult only", Boolean.class))
		{
			pmt.setAdultMaterial((Boolean) attributes.getAttribute(Attribute.AUX_FLAG));
		}

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
		try
		{
			client.deleteAsset(MayamAssetType.MATERIAL.getAssetType(), materialID);
		}
		catch (RemoteException e)
		{
			log.error("Error deleting material : " + materialID);
			returnCode = MayamClientErrorCode.MATERIAL_DELETE_FAILED;
		}
		return returnCode;
	}
}
