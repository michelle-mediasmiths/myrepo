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
import org.hibernate.cfg.annotations.ListBinder;

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

public class MayamPackageController extends MayamController implements PackageController
{
	private static final String VERSION_AGL_NAME = "version";

	private final TasksClient client;

	protected final static Logger log = Logger.getLogger(MayamPackageController.class);

	private final DateUtil dateUtil;
	private final MayamMaterialController materialController;
	private final MayamTaskController taskController;
	
	public final static String PENDING_TX_PACKAGE_SOURCE_BMS = "BMS";
	public final static String PENDING_TX_PACKAGE_SOURCE_AGGREGATOR = "AGGREGATOR";
	
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

			SegmentListBuilder listbuilder = SegmentList.create();
			listbuilder.attributeMap(attributes.getAttributes());

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
							segmentList,
							PENDING_TX_PACKAGE_SOURCE_BMS);

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
			log.debug("searching for segment list before trying to create it");
			SegmentList s = client.segmentApi().getSegmentListBySiteId(presentationID);

			if (s != null)
			{
				//segment list already exists!
				log.error("segment list with id "+presentationID+" already exits!");
				throw new MayamClientException(MayamClientErrorCode.PACKAGE_ALREADY_EXISTS);
			}
		}
		catch (RemoteException e1)
		{
			log.warn("exception thrown searching for package, assuming it doesnt exist",e1);
		}
		
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
			
			boolean isPending = false;

			if (material != null)
			{
				try
				{
					try
					{
						log.debug("looking for pending tx package");
						segmentList = getPendingTxPackage(txPackage.getPresentationID(), txPackage.getMaterialID());

						if (segmentList != null)
						{
							isPending = true;
						}
					}
					catch (PackageNotFoundException pnfe)
					{
						log.debug("pending tx package not found for package " + txPackage.getPresentationID(), pnfe);
					}

					if (segmentList == null)
					{

						log.debug("looking for real tx package");
						segmentList = getSegmentList(txPackage.getPresentationID());

						if (segmentList != null)
						{
							assetAttributes = segmentList.getAttributeMap();
						}
						else
						{
							log.warn("Unable to locate segment list for package " + txPackage.getPresentationID());
						}
					}
				}
				catch (MayamClientException e1)
				{
					log.error("unable to fetch package for update", e1);
					return e1.getErrorcode();
				}
			}
			else {
				log.warn("Unable to locate material " + txPackage.getMaterialID() + " for package");
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

				AttributeMap existingAttributes = segmentList.getAttributeMap();
				existingAttributes.putAll(attributes.getAttributes());
				segmentList.setAttributeMap(existingAttributes);
				
				try
				{
					log.info("updating tx package with id :" + txPackage.getPresentationID());
					updateTxPackage(segmentList, txPackage.getPresentationID(), txPackage.getMaterialID(), material, PENDING_TX_PACKAGE_SOURCE_BMS,isPending);
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
				log.debug("Segment list returned");
				return true;
			}
			else
			{
				try
				{
					AttributeMap pendingTxPackageTask = getPendingTxPackageTask(presentationID);
					if (pendingTxPackageTask != null)
					{
						log.debug("Pending tx package returned");
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
	public SegmentList getPendingTxPackage(String presentationID, String materialID) throws MayamClientException
	{
		AttributeMap task = getPendingTxPackageTask(presentationID, materialID);
		SegmentList seglist = null;
		
		try {
		//Due to requiring detail fields we must refetch (Mayam searches dont return detail fields)
			if (task != null)
			{
				long taskId = task.getAttribute(Attribute.TASK_ID);
				AttributeMap pendingTxTask = taskController.getTask(taskId);
				
				seglist = pendingTxTask.getAttribute(Attribute.SEGMENTATION_LIST);
		
				if (seglist == null)
				{
					log.warn("pending tx package task with null SEGMENTATION_LIST !");
				}
				
			}
		} catch (RemoteException e) {
			log.error("Exception thrown by Mayam while retrieving pending tx task", e);
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED,e);
		}

		return seglist;
	}
	
	/**
	 * Returns a REAL Segment list, this method will NOT return 'pending' tx packages
	 * @param presentationID
	 * @return
	 * @throws PackageNotFoundException
	 */
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
	
	/**
	 * Updates a tx package
	 * @param segmentList
	 * @param presentationID
	 * @param materialID
	 * @param material
	 * @throws MayamClientException
	 */
	public void updateTxPackage(SegmentList segmentList, String presentationID, String materialID, AttributeMap material, String source, boolean pendingPackage) throws MayamClientException{
	
		if(pendingPackage){
			log.debug("pending tx package");
			AttributeMap pendingTxPackageTask = getPendingTxPackageTask(presentationID,materialID);
			AttributeMap updateMap = taskController.updateMapForTask(pendingTxPackageTask);
			updateMap.setAttribute(Attribute.SEGMENTATION_LIST, segmentList);
			
			if(source.equals(PENDING_TX_PACKAGE_SOURCE_BMS)){
				//only set the source if it is BMS as once package info has been seen coming from BMS then 
				//it is ok to consider creating it once preview passes
				updateMap.setAttribute(Attribute.AUX_SRC, source); 
			}
			
			//update EVENT_DATE on pending task with the first tx date
			if(segmentList.getAttributeMap() != null && segmentList.getAttributeMap().getAttribute(Attribute.TX_FIRST) != null){
				updateMap.setAttribute(Attribute.EVENT_DATE, segmentList.getAttributeMap().getAttribute(Attribute.TX_FIRST));
			}
			
			taskController.saveTask(updateMap);
		}
		else{
			log.debug("real tx package");
			try
			{
				log.debug("Updating segment list with the below attributes:");
				for (Attribute attribute: segmentList.getAttributeMap().getAttributeSet())
				{
					log.debug(attribute.toString() + " : " + segmentList.getAttributeMap().getAttributeAsString(attribute));
				}
				client.assetApi().updateAsset(segmentList.getAttributeMap());
				client.segmentApi().updateSegmentList(segmentList.getId(), segmentList);
			}
			catch (RemoteException e)
			{
				log.error("error updating tx package",e);
				throw new MayamClientException(MayamClientErrorCode.PACKAGE_UPDATE_FAILED,e);
			}
		}
	}
	
	/**
	 * Returns any pending tx package tasks that have the given presentation id
	 * @param presentationID
	 * @return
	 * @throws MayamClientException
	 */
	private AttributeMap getPendingTxPackageTask(String presentationID) throws MayamClientException
	{
		return getPendingTxPackageTask(presentationID,null);		
	}

	/**
	 * Returns pending tx package tasks for the given presentation id and material id
	 * 
	 * if materialid is null then any tx package with the given presentation id will be returned
	 * 
	 * note: there should never be multiple pending tx package tasks for the same presentation id against different material ids
	 * 
	 * @param presentationID
	 * @param materialID
	 * @return
	 * @throws MayamClientException
	 */
	private AttributeMap getPendingTxPackageTask(String presentationID, String materialID) throws MayamClientException{
		log.info(String.format("searching for pending tx package %s for material %s",presentationID,materialID));
		
		final FilterCriteria criteria = client.taskApi().createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PENDING_TX_PACKAGE.getText());
		if (materialID != null)
		{
			criteria.getFilterEqualities().setAttribute(Attribute.HOUSE_ID, materialID);
		}
		criteria.getFilterEqualities().setAttribute(Attribute.AUX_EXTIDSTR, presentationID);
		log.info("Returning all tx packages regardless of what state they are in");
		//criteria.getFilterAlternatives().addAsInclusions(Attribute.TASK_STATE, TaskState.OPEN, TaskState.ERROR);
		criteria.getSortOrders().add(new SortOrder(Attribute.TASK_CREATED, SortOrder.Direction.DESC));
		criteria.getFilterAlternatives().addAsExclusions(Attribute.TASK_STATE, taskController.END_STATES);
		FilterResult result;
		try
		{
			result = client.taskApi().getTasks(criteria, 100, 0);
			log.info("Found " + result.getTotalMatches() + " pending tx-Package tasks");
			if(result.getTotalMatches() > 1){
				log.warn("multiple pending tx package tasks for the same package detected, using the first. Total number of tasks : " + result.getTotalMatches());
				List<AttributeMap> allMatches = result.getMatches();
				for (AttributeMap task: allMatches)
				{
					log.info("task " + task.getAttributeAsString(Attribute.TASK_ID) + " is in state " + task.getAttributeAsString(Attribute.TASK_STATE));
				}
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

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.controllers.PackageController#createOrUpdatePendingTxPackagesSegmentInfo(com.mayam.wf.attributes.shared.AttributeMap, java.lang.String, java.lang.String, com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package.Segmentation)
	 */
	@Override
	public void createOrUpdatePendingTxPackagesSegmentInfo(
			AttributeMap materialAttributes,
			String materialID,
			String packageID,
			Segmentation segmentation) throws MayamClientException
	{
		boolean pendingPackage = AssetProperties.isMaterialsReadyForPackages(materialAttributes);

		if (pendingPackage)
		{
			log.debug("pending tx package");

			SegmentListBuilder listbuilder = SegmentList.create();
			addMaterialExchangeSegmentInfoToSegmentListBuilder(segmentation, listbuilder);

			AttributeMap packageAttributes;
			boolean existingPendingTxPackageTask = false;

			// there may or may not be a pending tx package for this presentation id at the moment
			try
			{
				SegmentList pendingTxPackageTask = getPendingTxPackage(packageID, materialID);
				packageAttributes = pendingTxPackageTask.getAttributeMap();
				existingPendingTxPackageTask = true;
			}
			catch (PackageNotFoundException pnfe)
			{
				log.info("No pending tx package for this presentation id yet, will create one");

				packageAttributes = client.createAttributeMap();
				packageAttributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.PACKAGE.getAssetType());
				packageAttributes.setAttribute(Attribute.HOUSE_ID, packageID);
				packageAttributes.setAttribute(Attribute.ASSET_TITLE, packageID);
				packageAttributes.setAttribute(Attribute.METADATA_FORM, VERSION_AGL_NAME);
				packageAttributes.setAttribute(Attribute.PARENT_HOUSE_ID, materialID);
				packageAttributes.setAttribute(Attribute.HOUSE_ID, packageID);
				packageAttributes.setAttribute(Attribute.METADATA_FORM, VERSION_AGL_NAME);
				existingPendingTxPackageTask = false;
			}

			listbuilder.attributeMap(packageAttributes);
			SegmentList seglist = listbuilder.build();

			if (existingPendingTxPackageTask)
			{
				//if there was already a task then update
				updateTxPackage(seglist, packageID, materialID, materialAttributes, PENDING_TX_PACKAGE_SOURCE_AGGREGATOR,true);
			}
			else
			{
				//create pending tx package if there wasnt already one
				taskController.createPendingTxPackage(materialID, packageID, null, seglist, PENDING_TX_PACKAGE_SOURCE_AGGREGATOR);
			}
		}
		else
		{
			log.error("Expected a pending package, but conditions imply it should be real, will not attempt segmentation info update in case segmentation workflow has already started");
		}
	}

	/**
	 * used when converting segmentation information that arrived as part of material exchange into the mayam segmentation format
	 * @param segmentation
	 * @param listbuilder
	 * @throws MayamClientException
	 */
	private void addMaterialExchangeSegmentInfoToSegmentListBuilder(
			Segmentation segmentation,
			SegmentListBuilder listbuilder) throws MayamClientException
	{
		try
		{
			for (Segment s : segmentation.getSegment())
			{
				com.mayam.wf.attributes.shared.type.Segment converted = SegmentUtil.convertMaterialExchangeSegmentToMayamSegment(s);
				listbuilder = listbuilder.segment(converted);
			}
		}
		catch (InvalidTimecodeException e)
		{
			log.error("could not convert segmentation info", e);
			throw new MayamClientException(MayamClientErrorCode.SEGMENT_INFO_CONVERSION_FAILED);
		}
		catch (Exception e)
		{
			log.error("could not convert segmentation info", e);
			throw new MayamClientException(MayamClientErrorCode.SEGMENT_INFO_CONVERSION_FAILED);
		}
	}

}
