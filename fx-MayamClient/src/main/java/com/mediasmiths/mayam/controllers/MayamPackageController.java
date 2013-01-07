package com.mediasmiths.mayam.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.ClassificationEnumType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.Segment.SegmentBuilder;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.attributes.shared.type.Timecode;
import com.mayam.wf.attributes.shared.type.Timecode.InvalidTimecodeException;
import com.mayam.wf.attributes.shared.type.ValueList;
import com.mayam.wf.attributes.shared.type.SegmentList.SegmentListBuilder;
import com.mayam.wf.attributes.shared.type.ValueList.Entry;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package.Segmentation;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment;
import com.mediasmiths.mayam.DateUtil;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.PackageNotFoundException;
import com.mediasmiths.std.types.Framerate;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamPackageController extends MayamController
{
	private static final String VERSION_AGL_NAME = "version";

	private final TasksClient client;

	protected final static Logger log = Logger.getLogger(MayamPackageController.class);

	private final DateUtil dateUtil;
	private final MayamMaterialController materialController;

	@Inject
	public MayamPackageController(@Named(SETUP_TASKS_CLIENT) TasksClient mayamClient, DateUtil dateUtil, MayamMaterialController materialController)
	{
		client = mayamClient;
		this.dateUtil = dateUtil;
		this.materialController = materialController;
	}

	public MayamClientErrorCode createPackage(PackageType txPackage)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;

		if (txPackage != null)
		{
			attributesValid &= attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.PACKAGE.getAssetType());
			attributesValid &= attributes.setAttribute(Attribute.HOUSE_ID, txPackage.getPresentationID());
			
			//ASSET_TITLE is a required attribute
			attributesValid &= attributes.setAttribute(Attribute.ASSET_TITLE, txPackage.getPresentationID());
			attributesValid &= attributes.setAttribute(Attribute.METADATA_FORM, VERSION_AGL_NAME);

			attributesValid &= attributes.setAttribute(Attribute.PARENT_HOUSE_ID, txPackage.getMaterialID());
			
			AttributeMap material;
			try
			{
				material = client.assetApi().getAssetBySiteId(MayamAssetType.MATERIAL.getAssetType(), txPackage.getMaterialID());
				if (material != null)
				{
					if(txPackage.getClassification() != null)
					material.setAttribute(Attribute.CONT_CLASSIFICATION, txPackage.getClassification().toString());
					client.assetApi().updateAsset(material);
				}
			}
			catch (RemoteException e1)
			{
				log.error("Exception thrown by Mayam while attempting to retrieve asset : " + txPackage.getMaterialID(),e1);				
			}

			if (!attributesValid)
			{
				log.warn("PAckage created but one or more attributes was invalid");
				returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
			}
			
			try
			{
					attributesValid &= attributes.setAttribute(Attribute.HOUSE_ID, txPackage.getPresentationID());
					attributesValid &= attributes.setAttribute(Attribute.METADATA_FORM, VERSION_AGL_NAME);
					
					if (txPackage.getClassification() != null)
						attributesValid &= attributes.setAttribute(Attribute.CONT_CLASSIFICATION, txPackage.getClassification().toString());
					if (txPackage.getConsumerAdvice() != null)
						attributesValid &= attributes.setAttribute(Attribute.REQ_NOTES, txPackage.getConsumerAdvice());
					// map.setAttribute(Attribute.?????????, txPackage.getNotes());
					if (txPackage.getPresentationFormat() != null)
						attributesValid &= attributes.setAttribute(Attribute.REQ_FMT, txPackage.getPresentationFormat().toString());
					if (txPackage.getNumberOfSegments() != null)
						attributesValid &= attributes.setAttribute(Attribute.REQ_NUMBER, txPackage.getNumberOfSegments().intValue());
					if (txPackage.getTargetDate() != null)
						attributesValid &= attributes.setAttribute(Attribute.TX_FIRST, txPackage.getTargetDate().toString());
					
				
					
					final SegmentListBuilder listbuilder = SegmentList.create();
					listbuilder.attributeMap(attributes.getAttributes());
					SegmentList segmentList = listbuilder.build();

					log.debug("Getting materials asset id");
					String materialAssetID = materialController.getMaterialAttributes(txPackage.getMaterialID()).getAttributeAsString(Attribute.ASSET_ID);
					String revisionID = findHighestRevision(materialAssetID);
					log.debug("creating segment for material "+ materialAssetID+" revision:"+revisionID);
					SegmentList newSegmentList = client.segmentApi().createSegmentList(AssetType.REVISION, revisionID,	segmentList);
					log.info("Created SegmentList with id :"+newSegmentList.getId());
			
					return MayamClientErrorCode.SUCCESS;
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
				log.error("Exception thrown by Mayam while attempting to create Package");
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}
		}
		else
		{
			log.warn("Null package object, unable to create asset");
			returnCode = MayamClientErrorCode.PACKAGE_UNAVAILABLE;
		}
		return returnCode;
	}
	

	private String findHighestRevision(String itemId) throws RemoteException
	{
		List<AttributeMap> maps = client.assetApi().getAssetChildren(AssetType.ITEM, itemId, AssetType.REVISION);
		int maxno = -1;
		String retId = null;
		for (AttributeMap map : maps)
		{
			Integer no = map.getAttribute(Attribute.REVISION_NUMBER);
			if (no > maxno)
			{
				retId = map.getAttribute(Attribute.ASSET_ID);
				maxno = no;
			}
		}
		return retId;
	}


	public MayamClientErrorCode updatePackage(PackageType txPackage)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;

		if (txPackage != null)
		{
			SegmentList segmentList = null;
			AttributeMap assetAttributes = null;
			MayamAttributeController attributes = null;
	
			try
			{
				String materialAssetID = materialController.getMaterialAttributes(txPackage.getMaterialID()).getAttribute(Attribute.ASSET_ID);
				segmentList = getPackageForMaterial(txPackage.getPresentationID(), materialAssetID);
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
				boolean attributesValid = true;

				try
				{
					attributesValid &= attributes.setAttribute(Attribute.HOUSE_ID, txPackage.getPresentationID());
					attributesValid &= attributes.setAttribute(Attribute.METADATA_FORM, VERSION_AGL_NAME);

					if (txPackage.getClassification() != null)
						attributesValid &= attributes.setAttribute(
								Attribute.CONT_CLASSIFICATION,
								txPackage.getClassification().toString());
					if (txPackage.getConsumerAdvice() != null)
						attributesValid &= attributes.setAttribute(Attribute.REQ_NOTES, txPackage.getConsumerAdvice());
					// map.setAttribute(Attribute.?????????, txPackage.getNotes());
					if (txPackage.getPresentationFormat() != null)
						attributesValid &= attributes.setAttribute(
								Attribute.REQ_FMT,
								txPackage.getPresentationFormat().toString());
					if (txPackage.getNumberOfSegments() != null)
						attributesValid &= attributes.setAttribute(
								Attribute.REQ_NUMBER,
								txPackage.getNumberOfSegments().intValue());
					if (txPackage.getTargetDate() != null)
						attributesValid &= attributes.setAttribute(Attribute.TX_FIRST, txPackage.getTargetDate().toString());

					if (!attributesValid)
					{
						log.warn(String.format(
								"some attributes did not validate for update of package %s",
								txPackage.getPresentationID()));
					}

					log.info("updating SegmentList with id :" + segmentList.getId());
					client.segmentApi().updateSegmentList(segmentList.getId(), segmentList);
					log.debug("updated SegmentList with id :" + segmentList.getId());

				}
				catch (RemoteException e)
				{
					log.error(
							"Error thrown by Mayam while updating Segmentation data for package ID: "
									+ txPackage.getPresentationID(),
							e);
				}

				attributes.setAttribute(Attribute.PARENT_HOUSE_ID, txPackage.getMaterialID());
				returnCode = MayamClientErrorCode.SUCCESS;

			}
			else
			{
				log.warn("Mayam was unable to locate Package:" + txPackage.getPresentationID());
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

	public MayamClientErrorCode updatePackage(ProgrammeMaterialType.Presentation.Package txPackage, String materialID)
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
				String materialAssetID = materialController.getMaterialAttributes(materialID).getAttribute(Attribute.ASSET_ID);
				segmentList = getPackageForMaterial(txPackage.getPresentationID(), materialAssetID);
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
							
								com.mediasmiths.std.types.Timecode startTime = com.mediasmiths.std.types.Timecode.getInstance( segment.getSOM(), Framerate.HZ_25);
								com.mediasmiths.std.types.Timecode duration = com.mediasmiths.std.types.Timecode.getInstance( segment.getDuration(), Framerate.HZ_25);
								//TODO handle EOM if duration is null
								
								com.mayam.wf.attributes.shared.type.Segment mamSegment = com.mayam.wf.attributes.shared.type.Segment.create()
																.in(new Timecode(startTime.getDurationInFrames()))
																.duration(new Timecode(duration.getDurationInFrames()))
																.number(segment.getSegmentNumber())
																.title(segment.getSegmentTitle()).build();
																
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

	public MayamClientErrorCode deletePackage(String presentationID)
	{
	
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		if (isProtected(presentationID))
		{
			try
			{
				AttributeMap assetAttributes = client.assetApi().getAssetBySiteId(
						MayamAssetType.PACKAGE.getAssetType(),
						presentationID);

				AttributeMap taskAttributes = client.createAttributeMap();
				taskAttributes.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PURGE_BY_BMS);
				taskAttributes.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);

				taskAttributes.setAttribute(Attribute.HOUSE_ID, presentationID);
				taskAttributes.putAll(assetAttributes);
				client.taskApi().createTask(taskAttributes);
			}
			catch (RemoteException e)
			{
				log.error("Error creating Purge By BMS task for protected material : " + presentationID);
				returnCode = MayamClientErrorCode.MATERIAL_DELETE_FAILED;
			}
		}
		else
		{
			try
			{
				client.assetApi().deleteAsset(MayamAssetType.PACKAGE.getAssetType(), presentationID);
			}
			catch (RemoteException e)
			{
				log.error("Error deleting package : " + presentationID);
				returnCode = MayamClientErrorCode.PACKAGE_DELETE_FAILED;
			}
		}
		return returnCode;
	}

	public boolean packageExists(String presentationID, String ancestorAssetID, AssetType ancestorAssetAssetType)
	{
		// ancestorAssetID - an ASSET_ID
		if (ancestorAssetAssetType == MayamAssetType.MATERIAL.getAssetType())
		{
			return packageExistsForMaterial(presentationID, ancestorAssetID);
		}
		else if (ancestorAssetAssetType == MayamAssetType.TITLE.getAssetType())
		{
			return packageExistsForTitle(presentationID, ancestorAssetID);
		}
		else
		{
			throw new IllegalArgumentException("Ancestor Type not applicable to this method, should be on of "
					+ MayamAssetType.MATERIAL.getAssetType().toString() + " or " + MayamAssetType.TITLE.getAssetType().toString());
		}

	}

	private SegmentList getPackageForMaterial(String presentationID, String materialAssetID) throws PackageNotFoundException
	{
		log.debug(String.format("looking for package %s under material with asset id", presentationID, materialAssetID));

		try
		{
			final SegmentListList lists = client.segmentApi().getSegmentListsForAsset(
					MayamAssetType.MATERIAL.getAssetType(),
					materialAssetID);

			log.debug("found " + lists.size() + " segment lists for asset");

			for (SegmentList segmentList : lists)
			{
				if (segmentList.getAttributeMap() != null)
				{
					String segmentHouseID = segmentList.getAttributeMap().getAttribute(Attribute.HOUSE_ID);

					if (segmentHouseID != null)
					{
						if (segmentHouseID.equals(presentationID))
						{
							return segmentList;
						}
					}
					else
					{
						log.debug("found a segmentList with no house id : " + segmentList.getId());
					}
				}
			}

			throw new PackageNotFoundException();
		}
		catch (RemoteException e)
		{
			// TODO clarify which:
			log.info(
					"Exception fetching segment lists for material, this may just mean there is no segments or could be an error",
					e);
			throw new PackageNotFoundException();
		}

	}
	
	private SegmentList getPackageForTitle(String presentationID, String titleAssetID) throws PackageNotFoundException
	{
		log.debug(String.format("looking for package %s under material with asset id", presentationID, titleAssetID));

		try
		{
			final List<AttributeMap> maps = client.assetApi().getAssetChildren(
					MayamAssetType.TITLE.getAssetType(),
					titleAssetID,
					AssetType.ITEM);

			log.debug("found " + maps.size() + " items lists for title");

			for (AttributeMap map : maps)
			{
					try
					{
						SegmentList packageForMaterial = getPackageForMaterial(
								presentationID,
								map.getAttributeAsString(Attribute.ASSET_ID));
						
						return packageForMaterial; 
							
					}
					catch (PackageNotFoundException e)
					{
						//..not doing anything here, with throw a packagenotfound exception if it is not found for any of this titles items
					}
			}
			
			//not found in any of the items
			throw new PackageNotFoundException();
		}
		catch (RemoteException e){
			log.info("Exception fetching titles children, this may just mean there is are no children or could be an error", e);
			throw new PackageNotFoundException();
		}
	}

	private boolean packageExistsForMaterial(String presentationID, String materialAssetID)
	{
	
		try
		{
			getPackageForMaterial(presentationID,materialAssetID);
			return true;
		}
		catch (PackageNotFoundException e)
		{
			return false;
		}
	
	}

	private boolean packageExistsForTitle(String presentationID, String titleAssetID)
	{
		try
		{
			getPackageForTitle(presentationID, titleAssetID);
			return true;
		}
		catch (PackageNotFoundException e)
		{
			return false;
		}
	

	}
	
	public AttributeMap getPackageAttributes(String presentationID, String materialID) throws MayamClientException
	{
		AttributeMap assetAttributes = null;
		try
		{
			assetAttributes = client.assetApi().getAssetBySiteId(MayamAssetType.PACKAGE.getAssetType(), presentationID);
		}
		catch (RemoteException e1)
		{
			log.error("Exception thrown by Mayam while attempting to retrieve asset :" + presentationID, e1);
			throw new MayamClientException(MayamClientErrorCode.PACKAGE_FIND_FAILED, e1);
		}
		return assetAttributes;
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
		log.error("code missing! TODO: finish getPresentationPackage  once we can get package by siteid!!");
		return p;
//		AttributeMap pack = getPackageAttributes(packageID);
//		if (pack != null)
//		{
//			
//		
//			
//			p.setPresentationID(packageID);
//
//			String revisionId = pack.getAttribute(Attribute.REVISION_ID);
//			SegmentList segList = null;
//
//			try
//			{
//				segList = client.segmentApi().getSegmentList(revisionId);
//			}
//			catch (RemoteException e)
//			{
//				log.error("Remote exception", e);
//				throw new MayamClientException(MayamClientErrorCode.MAYAM_EXCEPTION, e);
//			}
//
//			if (segList != null)
//			{
//				List<Segment> segs = new ArrayList<Segment>();
//				ValueList metaData = segList.getMetadata();
//				Segment seg = new Segment();
//
//				for (int i = 0; i < metaData.size(); i++)
//				{
//					Entry segment = metaData.get(i);
//
//				}
//
//				Segmentation newSegmentation = new Segmentation();
//				p.setSegmentation(newSegmentation);
//			}
			// TODO segmentation;
			// p.setSegmentation(value);
//		}
//		return p;
	}

	public PackageType getPackage(String packageID) throws MayamClientException
	{

		log.error("code missing! TODO: finish getPackage  once we can get package by siteid!!");
		
		PackageType pt = new PackageType();
//		AttributeMap attributes = getPackageAttributes(packageID);
//
//		pt.setPresentationID((String) attributes.getAttribute(Attribute.HOUSE_ID));
//		String classfication = (String) attributes.getAttribute(Attribute.CONT_CLASSIFICATION);
//		if (classfication != null)
//		{
//			pt.setClassification(ClassificationEnumType.valueOf(classfication));
//		}
//		else
//		{
//			log.warn(String.format("package %s has null classification", packageID));
//		}
//		pt.setConsumerAdvice((String) attributes.getAttribute(Attribute.COMPLIANCE_NOTES));
//
//		String presentationFormat = (String) attributes.getAttribute(Attribute.CONT_FMT);
//
//		if (presentationFormat != null)
//		{
//			pt.setPresentationFormat(PresentationFormatType.valueOf(presentationFormat));
//		}
//		else
//		{
//			log.error(String.format("package %s has null presentationFormat", packageID));
//		}
//
//		pt.setMaterialID("" + attributes.getAttribute(Attribute.PARENT_HOUSE_ID));
//
//		// TODO: fetch segment information
//		// TODO : pt.setNotes ?
//
//		Date txNext = (Date) attributes.getAttribute(Attribute.TX_NEXT);
//
//		if (txNext != null)
//		{
//			pt.setTargetDate(dateUtil.fromDate(txNext));
//		}
//		else
//		{
//			log.error(String.format("package %s has null target tx date", packageID));
//		}

		return pt;
	}

	public boolean isProtected(String presentationID)
	{
		boolean isProtected = false;
		AttributeMap packageMap;
		try
		{
			packageMap = client.assetApi().getAssetBySiteId(MayamAssetType.PACKAGE.getAssetType(), presentationID);
			if (packageMap != null)
			{
				isProtected = packageMap.getAttribute(Attribute.PURGE_PROTECTED);
			}
		}
		catch (RemoteException e)
		{
			log.error("Exception thrown by Mayam while checking Protected status of Package : " + presentationID);
			e.printStackTrace();
		}
		return isProtected;
	}

}
