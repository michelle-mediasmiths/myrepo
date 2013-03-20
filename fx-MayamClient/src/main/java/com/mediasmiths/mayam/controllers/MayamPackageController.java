package com.mediasmiths.mayam.controllers;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.ClassificationEnumType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.attributes.shared.type.Timecode;
import com.mayam.wf.attributes.shared.type.Timecode.InvalidTimecodeException;
import com.mayam.wf.attributes.shared.type.FilterCriteria.SortOrder;
import com.mayam.wf.attributes.shared.type.SegmentList.SegmentListBuilder;
import com.mayam.wf.ws.client.FilterResult;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package.Segmentation;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment;
import com.mediasmiths.mayam.DateUtil;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.PackageNotFoundException;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mayam.util.RevisionUtil;
import com.mediasmiths.mayam.util.SegmentUtil;
import com.mediasmiths.std.types.Framerate;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamPackageController extends MayamController
{
	private static final String VERSION_AGL_NAME = "version";

	private final TasksClient client;

	protected final static Logger log = Logger.getLogger(MayamPackageController.class);

	private final DateUtil dateUtil;
	private final MayamMaterialController materialController;
	private final MayamTaskController taskController;
	
	@Inject @Named("material.exchange.unmarshaller")
	private Unmarshaller materialExchangeUnMarshaller;
	
	@Inject
	public MayamPackageController(@Named(SETUP_TASKS_CLIENT) TasksClient mayamClient, DateUtil dateUtil, MayamMaterialController materialController, MayamTaskController taskController)
	{
		client = mayamClient;
		this.dateUtil = dateUtil;
		this.materialController = materialController;
		this.taskController = taskController;
	}

	/**
	 * called when a package is created by BMS
	 * @param txPackage
	 * @return
	 */
	public MayamClientErrorCode createPackage(PackageType txPackage)
	{
		MayamAttributeController attributes = new MayamAttributeController(client);

		if (txPackage != null)
		{
			attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.PACKAGE.getAssetType());
			attributes.setAttribute(Attribute.HOUSE_ID, txPackage.getPresentationID());
			attributes.setAttribute(Attribute.ASSET_TITLE, txPackage.getPresentationID());
			attributes.setAttribute(Attribute.METADATA_FORM, VERSION_AGL_NAME);
			attributes.setAttribute(Attribute.PARENT_HOUSE_ID, txPackage.getMaterialID());

			AttributeMap material = null;
			Segmentation segmentation = null; // segmentation information that arrived with the media as part of material exchange

			boolean materialHasPreviewPass = false;
			boolean materialHasMedia = false;
			Date firstTx = null;

			try
			{
				material = client.assetApi().getAssetBySiteId(MayamAssetType.MATERIAL.getAssetType(), txPackage.getMaterialID());
				if (material != null)
				{
					if (txPackage.getClassification() != null)
					{
						material.setAttribute(Attribute.CONT_CLASSIFICATION, txPackage.getClassification().toString());
					}

					if (txPackage.getTargetDate() != null)
					{
						material.setAttribute(Attribute.TX_FIRST, dateUtil.fromXMLGregorianCalendar(txPackage.getTargetDate()));
					}

					client.assetApi().updateAsset(material);

					// the following determine if segmentation tasks need created and if package should be created in ardome or saved to the pending tx package list
					materialHasPreviewPass = AssetProperties.isMaterialPreviewPassed(material);
					materialHasMedia = !AssetProperties.isMaterialPlaceholder(material);

					try
					{
						segmentation = findExistingSegmentInfoForTxPackage(txPackage, material);
					}
					catch (Exception e)
					{
						log.error("error finding existing segment info for tx package", e);
					}

					if (segmentation == null)
					{
						log.info("no existing segmentation information for this tx package");
					}

				}
			}
			catch (RemoteException e1)
			{
				log.error("Exception thrown by Mayam while attempting to retrieve asset : " + txPackage.getMaterialID(), e1);
			}

			attributes.setAttribute(Attribute.HOUSE_ID, txPackage.getPresentationID());
			attributes.setAttribute(Attribute.METADATA_FORM, VERSION_AGL_NAME);

			if (txPackage.getClassification() != null)
				attributes.setAttribute(Attribute.CONT_CLASSIFICATION, txPackage.getClassification().toString());
			if (txPackage.getConsumerAdvice() != null)
				attributes.setAttribute(Attribute.REQ_NOTES, txPackage.getConsumerAdvice());
			if (txPackage.getPresentationFormat() != null)
				attributes.setAttribute(Attribute.REQ_FMT, txPackage.getPresentationFormat().toString());
			if (txPackage.getTargetDate() != null)
			{
				firstTx = dateUtil.fromXMLGregorianCalendar(txPackage.getTargetDate());
				attributes.setAttribute(Attribute.TX_FIRST, firstTx);
			}
			if (txPackage.getNumberOfSegments() != null)
				attributes.setAttribute(Attribute.REQ_NUMBER, txPackage.getNumberOfSegments().intValue());

			log.debug("Getting materials asset id");
			String materialAssetID = materialController.getMaterialAttributes(txPackage.getMaterialID()).getAttributeAsString(
					Attribute.ASSET_ID);

			boolean fixAndStitchItem = true;

			try
			{
				fixAndStitchItem = taskController.fixAndStitchTaskExistsForItem(materialAssetID);
			}
			catch (MayamClientException e)
			{
				log.error(
						"error searching for fix and stitch tasks, will not populate segmentation info but will still try to create package",
						e);
			}

			SegmentListBuilder listbuilder = SegmentList.create();
			listbuilder.attributeMap(attributes.getAttributes());

			// attempt to populate segmentlist with information supplied by aggregators only if fix stitch task has never been created for item
			if (!fixAndStitchItem)
			{
				try
				{
					if (segmentation != null)
					{

						for (Segment s : segmentation.getSegment())
						{
							com.mayam.wf.attributes.shared.type.Segment converted = SegmentUtil.convertMaterialExchangeSegmentToMayamSegment(s);
							listbuilder = listbuilder.segment(converted);
						}

					}
				}
				catch (InvalidTimecodeException e)
				{
					log.error("could not convert segmentation info stored against item", e);
				}
				catch (Exception e)
				{
					log.error("could not convert segmentation info stored against item", e);
				}
			}

			SegmentList segmentList = listbuilder.build();

			if (materialHasMedia && materialHasPreviewPass)
			{
				try
				{
					createSegmentList(txPackage.getPresentationID(), materialAssetID, segmentList);
				}
				catch (MayamClientException e)
				{
					log.error("error creating segment list", e);
					return e.getErrorcode();
				}
			}
			else
			{
				// tx package needs stashed on the pending tx package list
				try
				{
					taskController.createPendingTxPackage(
							txPackage.getMaterialID(),
							txPackage.getPresentationID(),
							firstTx,
							segmentList);

				}
				catch (MayamClientException e)
				{
					log.error("error creating pending tx pacakge task", e);
					return e.getErrorcode();
				}
			}
		}
		else
		{
			log.warn("Null package object, unable to create asset");
			return MayamClientErrorCode.PACKAGE_UNAVAILABLE;
		}
		return MayamClientErrorCode.SUCCESS;
	}

	public void createSegmentList(String presentationID, String materialAssetID, SegmentList segmentList) throws MayamClientException
	{
		try
		{
			String revisionID = RevisionUtil.findHighestRevision(materialAssetID, client);
			log.debug("creating segment for material " + materialAssetID + " revision:" + revisionID);
			SegmentList newSegmentList = client.segmentApi().createSegmentList(
					AssetType.REVISION,
					revisionID,
					segmentList);
			log.info("Created SegmentList with id :" + newSegmentList.getId());
		}
		catch (RemoteException e)
		{
			log.error("Exception thrown by Mayam while attempting to create Package", e);
			throw new MayamClientException(MayamClientErrorCode.PACKAGE_CREATION_FAILED,e);
		}

		// create segmentation task
		long taskID = taskController.createTask(presentationID, MayamAssetType.PACKAGE, MayamTaskListType.SEGMENTATION);
		log.info("Segmentation task created with id :" + taskID);
	
	}
	
	/**
	 * This is used to find any segmentation info that might have been saved against a material as natural breaks
	 * 
	 * Once preview has passed and bms is issuing tx package create messages then we fetch this segment info from the material
	 * to populated tx packages being created. (We couldn't just create them when the material comes in for some foxtel operational reason)
	 * 
	 * @param txPackage
	 * @param material
	 * @return
	 */
	private Segmentation findExistingSegmentInfoForTxPackage(PackageType txPackage, AttributeMap material)
	{
		Presentation p = null;
		String materialId = (String) material.getAttribute(Attribute.HOUSE_ID);
		
		try
		{
			final FileReader reader = new FileReader(new File(materialController.segdataFilePathForMaterial(materialId)));
			final StreamSource source = new StreamSource(reader);
			
			JAXBElement<Presentation> j = (JAXBElement<Presentation>) materialExchangeUnMarshaller.unmarshal(source,Presentation.class);
			p = j.getValue();
		}
		catch (JAXBException je)
		{
			log.error("error unmarshalling presentation information from SEGMENTATION_DATA", je);
		}
		catch (Exception e)
		{
			log.error("error unmarshalling presentation information from SEGMENTATION_DATA", e);
		}

		if (p != null)
		{
			List<Package> packages = p.getPackage();
			
			log.debug(String.format("Found %d segment lists for material %s",packages.size(),materialId));
			
			for (Package pc : packages)
			{
				if (pc.getPresentationID().equals(txPackage.getPresentationID()))
				{
					log.debug("Found segmentation info");
					return pc.getSegmentation();
				}
			}
		}
		return null;
	}

	/**
	 * called when bms updates a package
	 * @param txPackage
	 * @return
	 */
	public MayamClientErrorCode updatePackage(PackageType txPackage)
	{
		if (txPackage != null)
		{
			SegmentList segmentList = null;
			AttributeMap assetAttributes = null;
			MayamAttributeController attributes = null;

			AttributeMap material = materialController.getMaterialAttributes(txPackage.getMaterialID());

			try
			{
				segmentList = getTxPackage(txPackage.getPresentationID(), txPackage.getMaterialID(), material);
				assetAttributes = segmentList.getAttributeMap();
			}
			catch (MayamClientException e1)
			{
				log.error("unable to fetch package for update", e1);
				return e1.getErrorcode();
			}

			if (assetAttributes != null)
			{
				attributes = new MayamAttributeController(client.createAttributeMap());

				attributes.setAttribute(Attribute.HOUSE_ID, txPackage.getPresentationID());

				if (txPackage.getClassification() != null)
				{
					attributes.setAttribute(Attribute.CONT_CLASSIFICATION, txPackage.getClassification().toString());
				}

				if (txPackage.getConsumerAdvice() != null)
				{
					attributes.setAttribute(Attribute.REQ_NOTES, txPackage.getConsumerAdvice());
				}

				if (txPackage.getPresentationFormat() != null)
				{
					attributes.setAttribute(Attribute.REQ_FMT, txPackage.getPresentationFormat().toString());
				}

				if (txPackage.getNumberOfSegments() != null)
				{
					attributes.setAttribute(Attribute.REQ_NUMBER, txPackage.getNumberOfSegments().intValue());
				}

				if (txPackage.getTargetDate() != null)
				{
					attributes.setAttribute(Attribute.TX_FIRST, dateUtil.fromXMLGregorianCalendar(txPackage.getTargetDate()));
				}

				attributes.setAttribute(Attribute.PARENT_HOUSE_ID, txPackage.getMaterialID());

				segmentList.getAttributeMap().putAll(attributes.getAttributes());

				try
				{
					log.info("updating tx package with id :" + txPackage.getPresentationID());
					updateTxPackage(segmentList, txPackage.getPresentationID(), txPackage.getMaterialID(), material);
					log.info("updated tx package with id :" + txPackage.getPresentationID());
					return MayamClientErrorCode.SUCCESS;
				}
				catch (MayamClientException e)
				{
					log.error(
							"Error thrown by Mayam while updating Segmentation data for package ID: "
									+ txPackage.getPresentationID(),
							e);
					return e.getErrorcode();
				}
			}
			else
			{
				log.warn("Mayam was unable to locate Package:" + txPackage.getPresentationID());
				return MayamClientErrorCode.PACKAGE_FIND_FAILED;
			}
		}
		else
		{
			log.warn("Null package object, unable to update asset");
			return MayamClientErrorCode.PACKAGE_UNAVAILABLE;
		}
	}

	public MayamClientErrorCode updatePackage(ProgrammeMaterialType.Presentation.Package txPackage)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		boolean attributesValid = true;
		if (txPackage != null)
		{
			SegmentList segmentList = null;
			AttributeMap assetAttributes = null;
			MayamAttributeController attributes = null;

			try
			{
				segmentList = getSegmentList(txPackage.getPresentationID());
				assetAttributes = segmentList.getAttributeMap();
			}
			catch (MayamClientException e1)
			{
				log.error("unable to fetch package for update", e1);
				return e1.getErrorcode();
			}

			if (assetAttributes != null)
			{
				attributes = new MayamAttributeController(assetAttributes);

				String assetID = txPackage.getPresentationID();
				try
				{
					Segmentation segmentation = txPackage.getSegmentation();
					List<com.mayam.wf.attributes.shared.type.Segment> mamSegments = segmentList.getEntries();
					mamSegments.clear();
					
					if (segmentation != null)
					{
						List<Segment> materialExchangeSegments = segmentation.getSegment();
						for (SegmentationType.Segment segment : materialExchangeSegments)
						{
							if (segment != null)
							{
							
								SegmentationType.Segment filled = SegmentUtil.fillEomAndDurationOfSegment(segment);
								
								com.mediasmiths.std.types.Timecode startTime = com.mediasmiths.std.types.Timecode.getInstance( filled.getSOM(), Framerate.HZ_25);
								com.mediasmiths.std.types.Timecode duration = com.mediasmiths.std.types.Timecode.getInstance( filled.getDuration(), Framerate.HZ_25);
								String title = filled.getSegmentTitle();
								
								if(title==null){
									title = "";
								}
								
								com.mayam.wf.attributes.shared.type.Segment mamSegment = com.mayam.wf.attributes.shared.type.Segment.create()
																.in(new Timecode(startTime.getDurationInFrames()))
																.duration(new Timecode(duration.getDurationInFrames()))
																.number(filled.getSegmentNumber())
																.title(title).build();
																
								mamSegments.add(mamSegment);
							}
							else
							{
								log.error("Segment data is null for asset ID: " + assetID);
							}
						}
						
						client.segmentApi().updateSegmentList(segmentList.getId(), segmentList);
						
					}
				}
				catch (RemoteException e)
				{
					log.error("Error thrown by Mayam while updating Segmentation data for asset ID: " + assetID);
					e.printStackTrace();
				}
				catch (InvalidTimecodeException e)
				{
					log.error("invalid timecode",e);
					throw new IllegalArgumentException("invalid timecode",e);
				}

				if (!attributesValid)
				{
					log.warn("Package updated but one or more attributes were invalid");
					returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
				}

				AttributeMap result;
				try
				{
					result = client.assetApi().updateAsset(attributes.getAttributes());
					if (result == null)
					{
						log.warn("Mayam failed to update Package");
						returnCode = MayamClientErrorCode.PACKAGE_UPDATE_FAILED;
					}
				}
				catch (RemoteException e)
				{
					e.printStackTrace();
					log.error("Exception thrown by Mayam while attempting to update asset: " + txPackage.getPresentationID());
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
			}
			else
			{
				log.warn("Mayam was unable to locate Package: " + txPackage.getPresentationID());
				returnCode = MayamClientErrorCode.PACKAGE_FIND_FAILED;
			}
		}
		else
		{
			log.warn("Null package object, unable to update asset");
			returnCode = MayamClientErrorCode.PACKAGE_UNAVAILABLE;
		}
		return returnCode;
	}

	public MayamClientErrorCode deletePackage(String presentationID, int gracePeriod)
	{
		SegmentList segmentList = null;
		try
		{
			segmentList = getSegmentList(presentationID);
		}
		catch (PackageNotFoundException e1)
		{
			log.error(String.format("Failed to find package %s", presentationID));
			return MayamClientErrorCode.PACKAGE_FIND_FAILED;
		}

		if (segmentList != null)
		{
			if ( ! isProtected(segmentList,presentationID))
			{
				
				try
				{
					taskController.cancelAllOpenTasksForAsset(MayamAssetType.PACKAGE.getAssetType(), Attribute.HOUSE_ID, presentationID);
				}
				catch (MayamClientException e)
				{	
					log.error("error cancelling open tasks for asset during delete",e);
				}
				
				try
				{
					log.warn("no grace period for segment lists");
					client.segmentApi().deleteSegmentList(segmentList.getId());
				}
				catch (RemoteException e)
				{
					log.error("Error deleting package : " + presentationID, e);
					return MayamClientErrorCode.PACKAGE_DELETE_FAILED;
				}
			}
			else{
				log.info("Package's parent item is protect, will not perform delete");
			}
		}
		else
		{
			return MayamClientErrorCode.PACKAGE_FIND_FAILED;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	/**
	 * returns the package as represented in material exchange
	 * 
	 * @param packageID
	 * @return
	 * @throws RemoteException
	 */
	public ProgrammeMaterialType.Presentation.Package getPresentationPackage(String packageID) throws MayamClientException
	{
		ProgrammeMaterialType.Presentation.Package p = new ProgrammeMaterialType.Presentation.Package();
		
		SegmentList segmentList = getSegmentList(packageID);
		
		if (segmentList != null)
		{
			p.setPresentationID(packageID);
			
			Segmentation segmentation = new Segmentation();
			
			for(com.mayam.wf.attributes.shared.type.Segment s : segmentList.getEntries()){
			
				Segment out = new Segment();
				
				com.mediasmiths.std.types.Timecode startTime = com.mediasmiths.std.types.Timecode.getInstance(s.getIn().toSmpte(), Framerate.HZ_25);
				com.mediasmiths.std.types.Timecode duration = com.mediasmiths.std.types.Timecode.getInstance(s.getDuration().toSmpte(), Framerate.HZ_25);
				
				out.setSOM(startTime.toSMPTEString());
				out.setDuration(duration.toSMPTEString());
				out.setSegmentNumber(s.getNumber());
				out.setSegmentTitle(s.getTitle());
				segmentation.getSegment().add(out);
			}
			
			p.setSegmentation(segmentation);
		
		}
		return p;
	}

	
	public PackageType getPackageType(String packageID) throws MayamClientException
	{

		PackageType pt = new PackageType();
		AttributeMap attributes = getPackageAttributes(packageID);

		pt.setPresentationID((String) attributes.getAttribute(Attribute.HOUSE_ID));
		String classfication = (String) attributes.getAttribute(Attribute.CONT_CLASSIFICATION);
		if (classfication != null)
		{
			pt.setClassification(ClassificationEnumType.valueOf(classfication));
		}
		else
		{
			log.warn(String.format("package %s has null classification", packageID));
		}
		pt.setConsumerAdvice((String) attributes.getAttribute(Attribute.REQ_NOTES));

		String presentationFormat = (String) attributes.getAttribute(Attribute.REQ_FMT);

		if (presentationFormat != null)
		{
			pt.setPresentationFormat(PresentationFormatType.valueOf(presentationFormat));
		}
		else
		{
			log.error(String.format("package %s has null presentationFormat", packageID));
		}

		Integer numSegments = (Integer) attributes.getAttribute(Attribute.REQ_NUMBER);
		
		if(numSegments!=null){
			pt.setNumberOfSegments(BigInteger.valueOf(numSegments.intValue()));
		}
		
		pt.setMaterialID("" + attributes.getAttribute(Attribute.PARENT_HOUSE_ID));

		// TODO: fetch segment information
		// TODO : pt.setNotes ?

		Date txNext = (Date) attributes.getAttribute(Attribute.TX_NEXT);

		if (txNext != null)
		{
			pt.setTargetDate(dateUtil.fromDate(txNext));
		}
		else
		{
			log.error(String.format("package %s has null target tx date", packageID));
		}

		return pt;
	}

	private AttributeMap getPackageAttributes(String packageID) throws PackageNotFoundException
	{
		return getSegmentList(packageID).getAttributeMap();
	}

	public boolean isProtected(SegmentList segmentList, String packageID)
	{
		boolean isProtected = false;

		String materialID = segmentList.getAttributeMap().getAttribute(Attribute.PARENT_HOUSE_ID);
		log.debug(String.format("material id %s found for package %s", materialID, packageID));
		
		if (materialID != null)
		{
			isProtected = materialController.isProtected(materialID);
		}

		return isProtected;
	}

	public boolean packageExists(String presentationID)
	{
		try
		{
			SegmentList segmentList = client.segmentApi().getSegmentListBySiteId(presentationID);

			if (segmentList != null)
			{
				return true;
			}
			else
			{
				try
				{
					AttributeMap pendingTxPackageTask = getPendingTxPackageTask(presentationID);
					if (pendingTxPackageTask != null)
					{
						return true;
					}
					else
					{
						return false;
					}
				}
				catch (MayamClientException e)
				{
					log.error("error searching for pending tx package task", e);
					return false;
				}
			}
		}
		catch (RemoteException e)
		{
			log.info("Exception fetching package with id " + presentationID
					+ " it may not exists or there could be an error see stack trace", e);
			return false;
		}
	}
	
	public SegmentList getTxPackage(String presentationID, String materialID) throws PackageNotFoundException, MayamClientException{
		
		AttributeMap material = materialController.getMaterialAttributes(materialID);
		return getTxPackage(presentationID, materialID, material);
	}
	
	public SegmentList getTxPackage(String presentationID, String materialID, AttributeMap material) throws PackageNotFoundException, MayamClientException{
	
		boolean pendingPackage = AssetProperties.isMaterialsReadyForPackages(material);
		
		if(pendingPackage){
			log.debug("looking for pending tx package");
			return getPendingTxPackage(presentationID, materialID);
		}
		else{
			log.debug("looking for real tx package");
			return getSegmentList(presentationID);
		}
	}
	private SegmentList getPendingTxPackage(String presentationID, String materialID) throws MayamClientException
	{
		AttributeMap task = getPendingTxPackageTask(presentationID, materialID);
		SegmentList seglist = task.getAttribute(Attribute.SEGMENTATION_LIST);

		if (seglist == null)
		{
			log.warn("pending tx package task with null SEGMENTATION_LIST !");
		}

		return seglist;
	}
	
	public SegmentList getSegmentList(String presentationID) throws PackageNotFoundException{
		try
		{
			SegmentList segmentList = client.segmentApi().getSegmentListBySiteId(presentationID);
			return segmentList;
		}
		catch (RemoteException e)
		{
			log.info("Exception fetching package with id " + presentationID,e);
			throw new PackageNotFoundException(e);
		}
	}
	
	public void updateTxPackage(SegmentList segmentList, String presentationID, String materialID, AttributeMap material) throws MayamClientException{
	
		boolean pendingPackage = AssetProperties.isMaterialsReadyForPackages(material);
		
		if(pendingPackage){
			log.debug("pending tx package");
			AttributeMap pendingTxPackageTask = getPendingTxPackageTask(presentationID,materialID);
			AttributeMap updateMap = taskController.updateMapForTask(pendingTxPackageTask);
			updateMap.setAttribute(Attribute.SEGMENTATION_LIST, segmentList);
			taskController.saveTask(updateMap);
		}
		else{
			log.debug("real tx package");
			try
			{
				client.segmentApi().updateSegmentList(segmentList.getId(), segmentList);
			}
			catch (RemoteException e)
			{
				log.error("error updating tx package",e);
				throw new MayamClientException(MayamClientErrorCode.PACKAGE_UPDATE_FAILED,e);
			}
		}
	}
	

	private AttributeMap getPendingTxPackageTask(String presentationID) throws MayamClientException
	{
		return getPendingTxPackageTask(presentationID,null);		
	}

	
	private AttributeMap getPendingTxPackageTask(String presentationID, String materialID) throws MayamClientException{
		log.info(String.format("searching for pending tx package %s for material %s",presentationID,materialID));
		
		final FilterCriteria criteria = client.taskApi().createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PENDING_TX_PACKAGE.getText());
		if (materialID != null)
		{
			criteria.getFilterEqualities().setAttribute(Attribute.HOUSE_ID, materialID);
		}
		criteria.getFilterEqualities().setAttribute(Attribute.AUX_EXTIDSTR, presentationID);
		criteria.getFilterAlternatives().addAsInclusions(Attribute.TASK_STATE, TaskState.OPEN, TaskState.ERROR);
		criteria.getSortOrders().add(new SortOrder(Attribute.TASK_CREATED, SortOrder.Direction.DESC));
		
		FilterResult result;
		try
		{
			result = client.taskApi().getTasks(criteria, 100, 0);
		
			if(result.getTotalMatches() > 1){
				log.warn("multiple pending tx package tasks for the same package detected, using the first");
			}
			
			if(result.getTotalMatches()==0){
				throw new PackageNotFoundException();
			}
			return result.getMatches().get(0);
		}
		catch (RemoteException e)
		{
			log.error("remote exception searching for task", e);
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED);
		}
		
	}

	public boolean packageHasRequiredNumberOfSegments(String houseID) throws PackageNotFoundException
	{
		SegmentList segmentList = getSegmentList(houseID);
		AttributeMap attributeMap = segmentList.getAttributeMap();
		Integer requiredNumber = attributeMap.getAttribute(Attribute.REQ_NUMBER);
		int segmentCount = segmentList.getEntries().size();
		
		if(requiredNumber != null && segmentCount ==requiredNumber.intValue() ){
			return true;
		}
		
		return false;
	}

}
