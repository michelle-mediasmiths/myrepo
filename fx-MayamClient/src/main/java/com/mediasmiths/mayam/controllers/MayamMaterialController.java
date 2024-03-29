package com.mediasmiths.mayam.controllers;

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
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.AudioTrack;
import com.mayam.wf.attributes.shared.type.AudioTrackList;
import com.mayam.wf.attributes.shared.type.FileFormatInfo;
import com.mayam.wf.attributes.shared.type.Marker;
import com.mayam.wf.attributes.shared.type.MarkerList;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.attributes.shared.type.Timecode;
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
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package.Segmentation;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType;
import com.mediasmiths.foxtel.generated.ruzz.DetailType;
import com.mediasmiths.foxtel.pathresolver.PathResolver;
import com.mediasmiths.foxtel.pathresolver.PathResolver.PathType;
import com.mediasmiths.foxtel.pathresolver.UnknownPathException;
import com.mediasmiths.mayam.DateUtil;
import com.mediasmiths.mayam.FileFormatVerification;
import com.mediasmiths.mayam.FileFormatVerification.FileFormatVerificationResult;
import com.mediasmiths.mayam.MayamAspectRatios;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamAudioEncoding;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.accessrights.MayamAccessRightsController;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mayam.util.RevisionUtil;
import com.mediasmiths.mayam.util.SegmentUtil;
import com.mediasmiths.mayam.veneer.TasksClientVeneer;
import com.mediasmiths.std.io.PropertyFile;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.xml.bind.Marshaller;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MayamMaterialController extends MayamController
{

	public static final String PROGRAMME_MATERIAL_AGL_NAME = "programme";
	public static final String AO_PROGRAMME_MATERIAL_AGL_NAME = "ao-programme";
	public static final String PROGRAMME_MATERIAL_CONTENT_TYPE = "PG";

	public static final String ASSOCIATED_MATERIAL_AGL_NAME = "associated";
	public static final String AO_ASSOCIATED_MATERIAL_AGL_NAME = "ao-associated";
	public static final String ASSOCIATED_MATERIAL_CONTENT_TYPE = "PE";

	public static final String RUZZ_CHANNEL_CONDITIONS = "Ruzz channel conditions";

	private final TasksClientVeneer client;
	private final MayamTaskController taskController;
	private final static Logger log = Logger.getLogger(MayamMaterialController.class);

	@Inject
	@Named("material.exchange.marshaller")
	private Marshaller materialExchangeMarshaller;

	@Inject
	@Named("preferred.storage.locations")
	private String allPreferredPaths;

	@Inject
	@Named("highres.transfer.location")
	private String highResTransferLocation;

	@Inject
	private FileFormatVerification fileformatVerification;

	@Inject
	private MayamAccessRightsController accessRightsController;

	@Inject
	private PathResolver pathResolver;

	@Inject
	private DateUtil dateUtil;

	@Inject
	private PackageController packageController;
	
	@Inject
	@Named("ff.sd.video.imagex")
	private int sdVideoX;
	
	public static final String UNMATCHED_ASSET_MATCHED_TO_THIS_PLACEHOLDER = "Unmatched asset matched to this placeholder";
	
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
	public static EnumSet<Attribute> materialsAttributesInheritedFromTitle = EnumSet.of(
			Attribute.ASSET_TITLE,
			Attribute.CONT_RESTRICTED_MATERIAL,
			Attribute.CONT_RESTRICTED_ACCESS,
			Attribute.SERIES_TITLE,
			Attribute.SERIES_TITLE,
			Attribute.SHOW,
			Attribute.SEASON_NUMBER,
			Attribute.EPISODE_NUMBER,
			Attribute.EPISODE_TITLE,
			Attribute.LOCATION,
			Attribute.PRODUCTION_NUMBER,
			Attribute.SERIES_YEAR,
			Attribute.CONT_CATEGORY,
			Attribute.CHANNELS,
			Attribute.CHANNEL_GROUPS,
			Attribute.PURGE_PROTECTED,
			Attribute.LICENSE_START,
			Attribute.LICENSE_END);

	public static EnumSet<Attribute> associatedMaterialsAttributesNotInheritedFromTitle = EnumSet.of(Attribute.ASSET_TITLE);

	@Inject
	public MayamMaterialController(TasksClientVeneer client, MayamTaskController mayamTaskController)
	{
		this.client = client;
		this.taskController = mayamTaskController;
	}

	public MayamClientErrorCode createMaterial(MaterialType material, String titleID)
	{
		if (material == null)
		{
			log.warn("Null material object or no house ID, unable to create asset");
			return MayamClientErrorCode.MATERIAL_UNAVAILABLE;
		}

		log.info(String.format("Creating Material %s for title %s", material.getMaterialID(), titleID));

		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;
		boolean createCompLoggingTask = false;
		String parentAssetID = null; // parent asset id to use if a compliance logging task is to be created

		attributes.setAttribute(Attribute.QC_STATUS, QcStatus.TBD);
		attributes.setAttribute(Attribute.QC_PREVIEW_RESULT, MayamPreviewResults.PREVIEW_NOT_DONE);
		attributes.setAttribute(Attribute.QC_PARALLEL_ALLOWED, Boolean.FALSE);

		if (material != null && material.getMaterialID() != null && !material.getMaterialID().equals(""))
		{
			// setting parent_house_id is an unsupported operation
			attributesValid &= attributes.setAttribute(Attribute.PARENT_HOUSE_ID, titleID);
			try
			{
				AttributeMap title = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), titleID);
				if (title != null)
				{
					Boolean isProtected = title.getAttribute(Attribute.PURGE_PROTECTED);

					if (isProtected != null)
					{
						attributesValid &= attributes.setAttribute(Attribute.PURGE_PROTECTED, isProtected.booleanValue());
					}

					String assetId = title.getAttribute(Attribute.ASSET_ID);
					attributesValid &= attributes.setAttribute(Attribute.ASSET_PARENT_ID, assetId);

					updateMaterialAttributesFromTitle(attributes, title, false);

					Boolean isAO = title.getAttribute(Attribute.CONT_RESTRICTED_MATERIAL);

					if (isAO != null && isAO.equals(Boolean.TRUE))
					{
						attributesValid &= attributes.setAttribute(Attribute.METADATA_FORM, AO_PROGRAMME_MATERIAL_AGL_NAME);
					}
					else
					{
						attributesValid &= attributes.setAttribute(Attribute.METADATA_FORM, PROGRAMME_MATERIAL_AGL_NAME);
					}
				}
			}
			catch (RemoteException e)
			{
				log.error("MayamException while trying to retrieve title : " + titleID, e);
				return MayamClientErrorCode.TASK_SEARCH_FAILED;
			}

			attributesValid &= attributes.setAttribute(Attribute.PARENT_HOUSE_ID, titleID);

			attributesValid &= attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.MATERIAL.getAssetType());
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
					}
					if (order != null)
					{
						// Order Created field not required
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

						// If the item is a compliance placeholder then automatically set qc status to true (MAM-316)
						attributesValid &= attributes.setAttribute(Attribute.QC_STATUS, QcStatus.PASS);

					}
					catch (RemoteException e)
					{
						log.error("MayamException while trying to retrieve title : " + compile.getParentMaterialID(), e);
					}

					attributesValid &= attributes.setAttribute(Attribute.SOURCE_HOUSE_ID, compile.getParentMaterialID());
				}
			}

			// required by \ complete by date
			if (material.getRequiredBy() != null)
			{
				Date requiredByDate = dateUtil.fromXMLGregorianCalendar(material.getRequiredBy());
				log.debug("Setting required by date: " + requiredByDate);
				attributesValid &= attributes.setAttribute(Attribute.COMPLETE_BY_DATE, requiredByDate);
			}

			if (!attributesValid)
			{
				log.warn("Material created but one or more attributes was invalid");
				returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
			}

			AttributeMap newAsset = null;
			try
			{
				newAsset = accessRightsController.updateAccessRights(attributes.getAttributes());
			}
			catch (Exception e)
			{
				log.error("error determining access rights for new asset", e);
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
							long taskID = taskController.createComplianceLoggingTaskForMaterial(
									material.getMaterialID(),
									parentAssetID);
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
							taskController.createIngestTaskForMaterial(material.getMaterialID());
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


	private void updateMaterialAttributesFromTitle(MayamAttributeController attributes, AttributeMap title, boolean associated)
	{
		// copy metadata from title onto material
		for (Attribute a : materialsAttributesInheritedFromTitle)
		{
			if ((!associated) || (!associatedMaterialsAttributesNotInheritedFromTitle.contains(a)))
			{
				attributes.copyAttribute(a, title);
			}
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
			log.debug("" + materials.size() + " materials returned for title");
		}
		catch (RemoteException e1)
		{
			log.error("error getting materials for title " + title.getAttributeAsString(Attribute.ASSET_ID), e1);
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED, e1);
		}

		for (AttributeMap material : materials)
		{
			updateMaterialWithAttributesInheritedFromTitle(title, material);
		}

	}


	public void updateMaterialWithAttributesInheritedFromTitle(final AttributeMap title, final AttributeMap material)
	{
		String type = (String) material.getAttribute(Attribute.CONT_MAT_TYPE);
		boolean associated = false;

		if (ASSOCIATED_MATERIAL_CONTENT_TYPE.equals(type))
		{
			associated = true;
		}

		AttributeMap update = taskController.updateMapForAsset(material);

		for (Attribute a : materialsAttributesInheritedFromTitle)
		{
			if ((!associated) || (!associatedMaterialsAttributesNotInheritedFromTitle.contains(a)))
			{
				update.setAttribute(a, title.getAttribute(a));
			}
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


	/**
	 * Creates a material, returns the id of the created material
	 * 
	 * @param material
	 * @param title
	 * @param details
	 * @return
	 * @throws MayamClientException
	 */
	public String createMaterial(MarketingMaterialType material, String titleID, Details details, Title title)
			throws MayamClientException
	{
		log.info(String.format("Creating Marketing Material for title %s", titleID));

		MayamAttributeController attributes = new MayamAttributeController(client);

		attributes.setAttribute(Attribute.QC_STATUS, QcStatus.TBD);
		attributes.setAttribute(Attribute.QC_PREVIEW_RESULT, MayamPreviewResults.PREVIEW_NOT_DONE);
		attributes.setAttribute(Attribute.QC_PARALLEL_ALLOWED, Boolean.FALSE);
		attributes.setAttribute(Attribute.AUX_VAL,titleID);

		boolean attributesValid = true;

		if (material != null)
		{

			try
			{
				AttributeMap titleAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), titleID);
				if (titleAttributes != null)
				{
					// Terry - Update the attribute with the correct parent
					// MAM-196 Jonas states that we must associate with the title's parent and not the title.
					String associatingAssetId = titleAttributes.getAttribute(Attribute.ASSET_ID);
					String associatingHouseId = titleAttributes.getAttribute(Attribute.HOUSE_ID);
					log.debug("Associating asset with id=" + associatingAssetId + ", houseId = " + associatingHouseId);
					attributes.setAttribute(Attribute.ASSET_PARENT_ID, associatingAssetId);
					// attributes.setAttribute(Attribute.PARENT_HOUSE_ID, associatingHouseId);
					updateMaterialAttributesFromTitle(attributes, titleAttributes, true);
				}
				else
				{
					// ok what do we do.
					log.error("Expect to get attributes for title: " + titleID + " Failed.");
					throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
				}
			}
			catch (RemoteException e)
			{
				log.error("MayamException while trying to retrieve title : " + titleID, e);
				throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED, e);
			}

			attributesValid &= attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.MATERIAL.getAssetType());

			attributesValid &= attributes.setAttribute(Attribute.CONT_MAT_TYPE, ASSOCIATED_MATERIAL_CONTENT_TYPE);
			attributesValid &= attributes.setAttribute(Attribute.QC_STATUS, QcStatus.TBD);
			attributesValid &= attributes.setAttribute(Attribute.QC_PREVIEW_STATUS, QcStatus.TBD);

			attributes.setAttribute(Attribute.ASSET_TITLE, title.getProgrammeTitle());

			attributesValid &= attributes.setAttribute(
					Attribute.CONT_ASPECT_RATIO,
					MayamAspectRatios.mayamAspectRatioMappings.get(material.getAspectRatio()));
			attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, material.getFormat());
			attributesValid = attributesValid
					&& attributes.setAttribute(Attribute.CONT_RESTRICTED_MATERIAL, material.isAdultMaterial());

			if (material.isAdultMaterial())
			{
				attributesValid &= attributes.setAttribute(Attribute.METADATA_FORM, AO_ASSOCIATED_MATERIAL_AGL_NAME);
			}
			else
			{
				attributesValid &= attributes.setAttribute(Attribute.METADATA_FORM, ASSOCIATED_MATERIAL_AGL_NAME);
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
			try
			{
				newAsset = accessRightsController.updateAccessRights(attributes.getAttributes());
			}
			catch (Exception e)
			{
				log.error("error determining access rights for new asset", e);
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
				taskController.createIngestTaskForMaterial(siteID);
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

	// Material - Updating a media asset in Mayam
	public boolean updateMaterial(ProgrammeMaterialType material, Details details, Title title) throws MayamClientException
	{
		if (material == null)
		{
			log.warn("Null material object or no house ID, unable to update asset");
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_UNAVAILABLE);
		}

		log.info(String.format("Updating material %s for title %s", material.getMaterialID(), title.getTitleID()));

		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		boolean attributesValid = true;
		boolean isPlaceholder = false;

		MayamAttributeController attributes = null;
		AttributeMap assetAttributes = null;

		try
		{
			assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.MATERIAL.getAssetType(), material.getMaterialID());

			isPlaceholder = AssetProperties.isMaterialPlaceholder(assetAttributes);
		}
		catch (RemoteException e1)
		{
			log.error("Exception thrown by Mayam while attempting to retrieve asset :" + material.getMaterialID());
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED, e1);
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

				List<Package> presentationInfo = presentation.getPackage();

				for (Package pack : presentationInfo)
				{
					String packageID = pack.getPresentationID();
					Segmentation segmentation = pack.getSegmentation();

					try
					{
						packageController.createOrUpdatePendingTxPackagesSegmentInfo(
								assetAttributes,
								material.getMaterialID(),
								packageID,
								segmentation);
					}
					catch (Exception e)
					{
						log.error("error creation or updating pending tx package with segmentation information", e);
						// something has gone wrong populating the pending tx package task allow processing to continue so at least the natural breaks field gets populated
						try
						{
							taskController.createWFEErrorTaskBySiteID(MayamAssetType.MATERIAL,
							                                          material.getMaterialID(),
							                                          "Error processing segmentation information in material exchange message");
						}
						catch (MayamClientException mce)
						{
							log.error("error creating error task", mce);
						}
					}
				}

				// save the segmentation information to the natural breaks string
				try
				{
					String presentationString = SegmentUtil.presentationToHumanString(presentation);
					attributesValid &= attributes.setAttribute(Attribute.SEGMENTATION_NOTES, presentationString);
				}
				catch (Exception e)
				{
					log.error("error converting segmentation information to human string for natual breaks field", e);
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

			if (details.getDeliveryVersion() != null)
			{
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
				throw new MayamClientException(MayamClientErrorCode.MATERIAL_UPDATE_FAILED, e);
			}
		}
		else
		{
			log.warn("Unable to retrieve asset :" + material.getMaterialID());
			returnCode = MayamClientErrorCode.MATERIAL_FIND_FAILED;
		}

		if (returnCode != MayamClientErrorCode.SUCCESS)
		{
			throw new MayamClientException(returnCode);
		}

		return isPlaceholder;
	}

	public MayamClientErrorCode updateMaterial(MaterialType material)
	{
		if (material == null)
		{
			log.warn("Null material object or no house ID, unable to update asset");
			return MayamClientErrorCode.MATERIAL_UNAVAILABLE;
		}

		log.info(String.format("Updating material %s", material.getMaterialID()));

		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		boolean attributesValid = true;

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
			attributes = new MayamAttributeController(client.createAttributeMap());

			String titleID = assetAttributes.getAttribute(Attribute.PARENT_HOUSE_ID);
			try
			{

				AttributeMap titleAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), titleID);
				if (titleAttributes != null)
				{
					updateMaterialAttributesFromTitle(attributes, titleAttributes, false);
				}
			}
			catch (RemoteException e)
			{
				// let the method continue to try and update what we can, though if the title for this material doesnt exist something has gone wrong
				log.error("MayamException while trying to retrieve title : " + titleID, e);
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
					}
					if (order != null)
					{
						attributesValid &= attributes.setAttribute(Attribute.REQ_REFERENCE, order.getOrderReference());
					}
				}

				Compile compile = source.getCompile();
				if (compile != null)
				{
					attributesValid &= attributes.setAttribute(Attribute.SOURCE_HOUSE_ID, compile.getParentMaterialID());
				}

			}

			// required by \ complete by date
			if (material.getRequiredBy() != null)
			{
				Date requiredByDate = dateUtil.fromXMLGregorianCalendar(material.getRequiredBy());
				log.debug("Setting required by date: " + requiredByDate);
				attributesValid &= attributes.setAttribute(Attribute.COMPLETE_BY_DATE, requiredByDate);
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
					log.info("material updated");
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

		log.info(String.format("Material %s found: %b ", materialID, materialFound));
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

			if (asset == null)
				throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);

			return asset;
		}
		catch (RemoteException e)
		{
			log.debug(String.format("remote expcetion getting material by asset id", e));
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED, e);
		}
	}

	public MayamClientErrorCode deleteMaterial(String materialID, int gracePeriod)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		if (!isProtected(materialID))
		{
			try
			{
				AttributeMap assetAttributes = client.assetApi().getAssetBySiteId(
						MayamAssetType.MATERIAL.getAssetType(),
						materialID);

				if (assetAttributes == null)
				{
					log.warn("delete request for material that doesnt exist " + materialID);
					return MayamClientErrorCode.MATERIAL_FIND_FAILED;
				}

				deleteAssetsPackages(
						MayamAssetType.MATERIAL.getAssetType(),
						(String) assetAttributes.getAttributeAsString(Attribute.ASSET_ID),
						materialID,
						gracePeriod);

				String parentId = assetAttributes.getAttribute(Attribute.PARENT_HOUSE_ID);

				try
				{
					taskController.cancelAllOpenTasksForAsset(
							MayamAssetType.MATERIAL.getAssetType(),
							Attribute.HOUSE_ID,
							materialID);
				}
				catch (MayamClientException e)
				{
					log.error("error cancelling open tasks for asset during delete", e);
				}

				client.assetApi().deleteAsset(
						MayamAssetType.MATERIAL.getAssetType(),
						assetAttributes.getAttributeAsString(Attribute.ASSET_ID),
						gracePeriod);

				if (parentId != null)
				{
					AttributeMap title = client.assetApi().getAssetBySiteId(MayamAssetType.TITLE.getAssetType(), parentId);
					if (title != null)
					{
						String assetId = title.getAttribute(Attribute.ASSET_ID);
						List<AttributeMap> childAssets = client.assetApi().getAssetChildren(
								MayamAssetType.TITLE.getAssetType(),
								assetId,
								MayamAssetType.MATERIAL.getAssetType());
						if (childAssets == null || childAssets.isEmpty())
						{
							client.assetApi().deleteAsset(MayamAssetType.TITLE.getAssetType(), assetId, gracePeriod);
							log.info("Orphaned title " + assetId + " deleted after purge of material " + materialID);
						}
						else if (childAssets.size() == 1)
						{
							AttributeMap childAsset = childAssets.get(0);
							String childId = childAsset.getAttribute(Attribute.ASSET_ID);
							if (childId != null && childId.equals(assetAttributes.getAttributeAsString(Attribute.ASSET_ID)))
							{
								client.assetApi().deleteAsset(MayamAssetType.TITLE.getAssetType(), assetId, gracePeriod);
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
				Boolean prot = material.getAttribute(Attribute.PURGE_PROTECTED);
				if (prot != null)
				{
					isProtected = prot.booleanValue();
				}
				else
				{
					isProtected = false;
				}
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

		if (details.getTitle() != null)
		{
			log.debug("have not updated title information");
		}

		if (details.getFormat() != null)
		{
			log.debug("have not updated format information");
		}

		// TODO : update any other metadata

		// set aggregator to Ruzz
		updateMap.setAttribute(Attribute.AGGREGATOR, "Ruzz");

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

		// begin retry specific behavior

		log.debug("Checking existing file format status for asset");

		// if file format verification is being rerun and it previously passed then there will be no 'change' detected from PASS->PASS
		// so if it has already run and this is a retry the state is first set to TBD then once it passes or fails the next step can
		// proceed based on the change from TBD->PASS or TBD->FAIL
		QcStatus currentStatus = qcTaskAttributes.getAttribute(Attribute.QC_SUBSTATUS1);
		if (currentStatus != null && currentStatus != QcStatus.TBD)
		{
			log.debug("FFV has run before, setting substatus 1 to TBD");
			// file format verification has already been run, set sub status to TBD before proceeding
			{
				AttributeMap update = taskController.updateMapForTask(qcTaskAttributes);
				update.setAttribute(Attribute.QC_SUBSTATUS1, QcStatus.TBD);
				update.setAttribute(Attribute.QC_SUBSTATUS1_NOTES, "");
				taskController.saveTask(update);
			}
		}
		// end retry specific behavior

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

		FileFormatVerificationResult ffvResult = fileformatVerification.verifyFileFormat(formatInfo, qcTaskAttributes);

		if (ffvResult.isPass())
		{
			log.info("ffv passed");
			update.setAttribute(Attribute.QC_SUBSTATUS1, QcStatus.PASS);
			update.setAttribute(Attribute.QC_SUBSTATUS2, QcStatus.TBD);
			update.setAttribute(Attribute.QC_SUBSTATUS3, QcStatus.TBD);
		}
		else
		{
			log.warn("file format verification failed");
			update.setAttribute(Attribute.QC_SUBSTATUS1, QcStatus.FAIL);
			update.setAttribute(Attribute.QC_SUBSTATUS2, QcStatus.TBD);
			update.setAttribute(Attribute.QC_SUBSTATUS3, QcStatus.TBD);
			update.setAttribute(Attribute.QC_STATUS, QcStatus.FAIL);
			update.setAttribute(Attribute.QC_SUBSTATUS1_NOTES, ffvResult.getDetail());


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
			if (aggregatorRequiresAutoQc)
			{
				return true;
			}
		}

		if (AssetProperties.isQCParallel(messageAttributes))
		{
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

			deleteAssetsPackages(assetType, assetID, houseID, 0);

			try
			{
				Set<MayamTaskListType> taskTypesToKeepOpen = new HashSet<MayamTaskListType>();
				taskTypesToKeepOpen.add(MayamTaskListType.PENDING_TX_PACKAGE);
				taskController.cancelAllOpenTasksForAsset(assetType, Attribute.ASSET_ID, assetID, taskTypesToKeepOpen);
			}
			catch (MayamClientException e)
			{
				log.error("error cancelling open tasks for asset", e);
			}

			// create ingest task
			log.debug("creating new ingest task");
			taskController.createIngestTaskForMaterial(houseID);

			try
			{
				AttributeMap updateMap = taskController.updateMapForAsset(materialAttributes);
				updateMap.setAttribute(Attribute.SEGMENTATION_NOTES, "");
				log.debug("clearing segmentation notes");
				client.assetApi().updateAsset(updateMap);
			}
			catch (RemoteException e)
			{
				log.error("error clearing segmentation notes during uningest", e);
				// segmentation notes are only for reference, just going to catch this exception
			}

		}
	}

	private void deleteAssetsPackages(AssetType assetType, String assetID, String houseID, int gracePeriod)
	{
		try
		{
			log.info(String.format("Searching for packages of asset %s (%s) for deletion", houseID, assetID));
			List<SegmentList> packages = client.segmentApi().getSegmentListsForAsset(assetType, assetID);
			log.info(String.format("Found %d packages for asset %s", packages.size(), houseID));
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
					log.warn("no grace period for segment lists");
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

	public String getMarkersString(final AttributeMap materialAttributes, final String user) throws MayamClientException
	{
		final MarkerList markers = getMarkers(materialAttributes);

		if (markers == null)
		{
			log.warn(String.format("No markers found for item %s", materialAttributes.getAttributeAsString(Attribute.HOUSE_ID)));
			return "";
		}

		final StringBuilder markerSb = new StringBuilder();

		for (Marker marker : markers)
		{
			final Timecode inTc = marker.getIn();
			if (null != inTc)
			{
				markerSb.append("In: ").append(inTc.toSmpte());
			}

			final Timecode duration = marker.getDuration();
			if(null != duration)
			{
				markerSb.append("; Duration: ").append(duration.toSmpte());
			}

			final String title = marker.getTitle();
			if(StringUtils.isNotBlank(title))
			{
				markerSb.append("; Title: ").append(title);
			}

			final String markerType = marker.getTypeStr();
			if(null != markerType)
			{
				markerSb.append("; Type: ").append(markerType);
			}
			
			if(null != user)
			{
				markerSb.append("; Requested by: ").append(user);
			}
			
			markerSb.append("\n");
		}
		log.debug(String.format("Returning markers: %s", markerSb.toString()));
		return markerSb.toString();
	}

	private MarkerList getMarkers(final AttributeMap messageAttributes) throws MayamClientException
	{
		final String assetID = messageAttributes.getAttributeAsString(Attribute.ASSET_ID);
		String revisionId = null;
		try
		{
			revisionId = RevisionUtil.findHighestRevision(assetID, client);
		}
		catch (RemoteException e)
		{
			log.error("Error finding highest reivsion for asset " + assetID, e);
			throw new MayamClientException(MayamClientErrorCode.REVISION_FIND_FAILED, e);
		}

		MarkerList markers = null;

		try
		{
			markers = client.assetApi().getMarkers(AssetType.ITEM, assetID, revisionId);
		}
		catch (RemoteException e)
		{
			log.error(String.format("Error fetching markerts for revision %s asset %s", revisionId, assetID));
		}

		if (markers == null)
		{
			log.info(String.format("No markers found for revision %s asset %s", revisionId, assetID));
			return null;
		}
		else
		{
			log.info(String.format("Found %d markers ", markers.size()));
			return markers;
		}
	}

	public String getAssetPath(String assetID) throws MayamClientException
	{
		return getAssetPath(assetID, true);
	}

	public List<String> getDataFilesUrls(String materialAssetID) throws MayamClientException
	{
		FileFormatInfo fileinfo;
		try
		{
			fileinfo = client.assetApi().getFormatInfo(MayamAssetType.MATERIAL.getAssetType(), materialAssetID);
		}
		catch (RemoteException e)
		{
			log.error("error getting file format info for asset " + materialAssetID, e);
			throw new MayamClientException(MayamClientErrorCode.FILE_FORMAT_QUERY_FAILED, e);
		}

		List<String> urls = fileinfo.getDataUrls();

		return urls;

	}

	public String getAssetPath(String assetID, boolean acceptNonPreferredLocations) throws MayamClientException
	{

		FileFormatInfo fileinfo;
		try
		{
			fileinfo = client.assetApi().getFormatInfo(MayamAssetType.MATERIAL.getAssetType(), assetID);
		}
		catch (RemoteException e)
		{
			log.error("error getting file format info for asset " + assetID, e);
			throw new MayamClientException(MayamClientErrorCode.FILE_FORMAT_QUERY_FAILED, e);
		}

		List<String> urls = fileinfo.getMediaUrls();

		if (urls == null || urls.size() == 0)
		{
			log.error("no urls for media found!");
			throw new MayamClientException(MayamClientErrorCode.FILE_LOCATON_UNAVAILABLE);
		}

		String nixPath = null;
		List<String> preferredLocations = Arrays.asList(allPreferredPaths.split(","));

		log.info("No of URLs returned is :" + urls.size());

		for (String url : urls)
		{
			log.info("Attempting to resolve path for url :" + url);
			try
			{
				nixPath = pathResolver.nixPath(PathType.FTP, url);
				for (String preferredLoc : preferredLocations)
				{
					if (nixPath.contains(preferredLoc))
					{
						log.info("Preferred location found:" + nixPath);
						return nixPath;
					}
				}
			}
			catch (UnknownPathException e)
			{
				log.error(String.format("Unable to resolve storage path for ftp location %s", url), e);
			}
		}
		if (acceptNonPreferredLocations)
		{
			if (nixPath != null)
			{
				log.info("No preferred location found, using :" + nixPath);
				return nixPath;
			}
			throw new MayamClientException(MayamClientErrorCode.FILE_LOCATON_QUERY_FAILED);
		}
		else
		{
			throw new MayamClientException(MayamClientErrorCode.FILE_NOT_IN_PREFERRED_LOCATION);
		}

	}

	public void setNaturalBreaks(String materialID, String naturalBreaks) throws MayamClientException
	{
		log.info(String.format("Setting natural breaks string {%s} on material {%s}", naturalBreaks, materialID));

		AttributeMap materialAttributes = getMaterialAttributes(materialID);
		if (materialAttributes == null)
		{
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}

		AttributeMap updateMap = taskController.updateMapForAsset(materialAttributes);
		updateMap.setAttribute(Attribute.SEGMENTATION_NOTES, naturalBreaks);

		try
		{
			client.assetApi().updateAsset(updateMap);
			log.info("Natural breaks updated");
		}
		catch (RemoteException e)
		{
			log.error("error setting natual breaks on material", e);
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_UPDATE_FAILED);
		}

	}

	public String getFormat(AttributeMap currentAttributes) throws RemoteException
	{
		String format = "HD";
		try
		{
			FileFormatInfo formatInfo = client.assetApi().getFormatInfo(
					MayamAssetType.MATERIAL.getAssetType(),
					(String) currentAttributes.getAttribute(Attribute.ASSET_ID));

			if (formatInfo != null && formatInfo.getImageSizeX() <= sdVideoX)
			{
				format = "SD";
			}
		}
		catch (Exception e)
		{
			log.error("error determining format for asset", e);
			try
			{
				taskController.createWFEErrorTaskBySiteID(MayamAssetType.MATERIAL,
				                                          currentAttributes.getAttributeAsString(Attribute.ASSET_SITE_ID),
				                                          "Error determining content format");
			}
			catch (Exception e1)
			{
				log.error("error creating error task!", e1);
			}
		}
		return format;
	}

	public boolean materialHasTXPackages(String houseID, String materialAssetID) throws RemoteException, MayamClientException
	{
		log.info(String.format("Searching for packages of asset %s (%s) for deletion", houseID, materialAssetID));
		List<SegmentList> packages = client.segmentApi().getSegmentListsForAsset(AssetType.ITEM, materialAssetID);
		log.info(String.format("Found %d packages for asset %s", packages.size(), houseID));
		if (packages.size() > 0)
		{
			return true;
		}
		else
		{
			log.info("found no segment lists for asset, searching for pending tx package tasks");
			// check if there are any pending tx package tasks with bms information
			List<AttributeMap> pendingTxTasks = taskController.getOpenTasksForAsset(
					MayamTaskListType.PENDING_TX_PACKAGE,
					Attribute.ASSET_ID,
					materialAssetID,
					Collections.<Attribute, Object> singletonMap(
							Attribute.AUX_SRC,
							MayamPackageController.PENDING_TX_PACKAGE_SOURCE_BMS));

			if (!pendingTxTasks.isEmpty())
			{
				log.info("open pending tx package with bms information found");
				return true;
			}
			log.info("no pending tx package tasks with bms info found");
		}

		return false;
	}

	public void initiateHighResTransfer(final AttributeMap taskAttributes)
	{
		try
		{
			final String assetId = taskAttributes.getAttribute(Attribute.ASSET_ID);
			final AssetType assetType = taskAttributes.getAttribute(Attribute.ASSET_TYPE);

			String itemAssetId = null;
			if (MayamAssetType.PACKAGE.getAssetType().equals(assetType))
			{
				// message attributes describe a package
				itemAssetId = taskAttributes.getAttribute(Attribute.ASSET_GRANDPARENT_ID);
			}
			else if (MayamAssetType.MATERIAL.getAssetType().equals(assetType))
			{
				// message attributes describe an item
				itemAssetId = assetId;
			}
			else
			{
				throw new IllegalArgumentException("Unexpected asset type " + assetType);
			}

			final String jobNum = client.assetApi().requestHighresXfer(AssetType.ITEM, itemAssetId, highResTransferLocation);

			log.warn("Requesting High Res Transfer. Job : " + jobNum);
			log.warn("Setting task to Sys Wait state : " + taskAttributes.getAttributeAsString(Attribute.TASK_ID));

			final AttributeMap updateMap = taskController.updateMapForTask(taskAttributes);
			updateMap.setAttribute(Attribute.TASK_STATE, TaskState.SYS_WAIT);
			taskController.saveTask(updateMap);
		}
		catch (RemoteException e)
		{
			log.error("error initiating high res transfer", e);
			taskController.setTaskToErrorWithMessage(taskAttributes, "Error initiating high res transfer");
		}
		catch (MayamClientException e)
		{
			log.error("error saving task to sys wait state", e);
			taskController.setTaskToErrorWithMessage(taskAttributes, "Error saving task to sys wait state");
		}
	}


	public void setDateUtil(final DateUtil dateUtil)
	{
		this.dateUtil = dateUtil;
	}


}
