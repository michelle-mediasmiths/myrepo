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
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.attributes.shared.type.TaskState;
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
					log.debug("creating segment for material "+ materialAssetID);
					SegmentList newSegmentList = client.segmentApi().createSegmentList(AssetType.ITEM, materialAssetID,	segmentList);
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
	


	public MayamClientErrorCode updatePackage(PackageType txPackage)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;

		if (txPackage != null)
		{
			AttributeMap assetAttributes = null;
			MayamAttributeController attributes = null;
			try
			{
				assetAttributes = client.assetApi().getAssetBySiteId(
						MayamAssetType.PACKAGE.getAssetType(),
						txPackage.getPresentationID());
			}
			catch (RemoteException e1)
			{
				log.error("Exception thrown by Mayam while attempting to get asset: " + txPackage.getPresentationID());
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				e1.printStackTrace();
			}

			if (assetAttributes != null)
			{
				attributes = new MayamAttributeController(assetAttributes);

//				try
//				{
////					String revisionID = assetAttributes.getAttribute(Attribute.REVISION_ID);
////
////					ValueList metadata = new ValueList();
////					metadata.add(new ValueList.Entry("CONT_CLASSIFICATION", txPackage.getClassification().toString()));
////					metadata.add(new ValueList.Entry("COMPLIANCE_NOTES", txPackage.getConsumerAdvice()));
////					metadata.add(new ValueList.Entry("ESC_NOTES", txPackage.getNotes()));
////					metadata.add(new ValueList.Entry("CONT_FMT", txPackage.getPresentationFormat().toString()));
////					metadata.add(new ValueList.Entry("TX_NEXT", txPackage.getTargetDate().toString()));
////					metadata.add(new ValueList.Entry("NUMBER_SEGMENTS", txPackage.getNumberOfSegments().toString()));
////
////					SegmentListBuilder listBuilder = SegmentList.create("Package " + txPackage.getPresentationID());
////					listBuilder = listBuilder.metadataForm(VERSION_AGL_NAME);
////					listBuilder = listBuilder.metadata(metadata);
////					SegmentList list = listBuilder.build();
////					client.segmentApi().updateSegmentList(revisionID, list);
//				}
//				catch (RemoteException e)
//				{
//					log.error("Error thrown by Mayam while updating Segmentation data for package ID: "
//							+ txPackage.getPresentationID());
//					e.printStackTrace();
//				}

				attributes.setAttribute(Attribute.PARENT_HOUSE_ID, txPackage.getMaterialID());

				AttributeMap result;
				try
				{
					AttributeMap material = client.assetApi().getAssetBySiteId(
							MayamAssetType.MATERIAL.getAssetType(),
							txPackage.getMaterialID());
					if (material != null)
					{
						String assetId = material.getAttributeAsString(Attribute.ASSET_ID);
						attributes.setAttribute(Attribute.ASSET_PARENT_ID, assetId);
					}

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

	public MayamClientErrorCode updatePackage(ProgrammeMaterialType.Presentation.Package txPackage)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		boolean attributesValid = true;
		if (txPackage != null)
		{
			AttributeMap assetAttributes = null;
			MayamAttributeController attributes = null;
			try
			{
				assetAttributes = client.assetApi().getAssetBySiteId(
						MayamAssetType.PACKAGE.getAssetType(),
						txPackage.getPresentationID());
			}
			catch (RemoteException e1)
			{
				log.error("Exception thrown by Mayam while attempting to get asset: " + txPackage.getPresentationID());
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				e1.printStackTrace();
			}

			if (assetAttributes != null)
			{
				attributes = new MayamAttributeController(assetAttributes);

				String assetID = txPackage.getPresentationID();
//				try
//				{
//					AttributeMap asset = client.assetApi().getAssetBySiteId(MayamAssetType.PACKAGE.getAssetType(), assetID);
//					String revisionID = asset.getAttribute(Attribute.REVISION_ID);
//
//					Segmentation segmentation = txPackage.getSegmentation();
//					if (segmentation != null)
//					{
//						List<Segment> segments = segmentation.getSegment();
//						for (int i = 0; i < segments.size(); i++)
//						{
//							SegmentationType.Segment segment = segments.get(i);
//							if (segment != null)
//							{
//								ValueList metadata = new ValueList();
//								metadata.add(new ValueList.Entry("DURATION", segment.getDuration()));
//								metadata.add(new ValueList.Entry("EOM", segment.getEOM()));
//								metadata.add(new ValueList.Entry("SOM", segment.getSOM()));
//								metadata.add(new ValueList.Entry("SEGMENT_NUMBER", "" + segment.getSegmentNumber()));
//								metadata.add(new ValueList.Entry("SEGMENT_TITLE", segment.getSegmentTitle()));
//
//								SegmentListBuilder listBuilder = SegmentList.create("Asset " + assetID + " Segment "
//										+ segment.getSegmentNumber());
//								listBuilder = listBuilder.metadataForm(VERSION_AGL_NAME);
//								listBuilder = listBuilder.metadata(metadata);
//								SegmentList list = listBuilder.build();
//								client.segmentApi().updateSegmentList(revisionID, list);
//							}
//							else
//							{
//								log.error("Segment data is null for asset ID: " + assetID);
//							}
//						}
//					}
//				}
//				catch (RemoteException e)
//				{
//					log.error("Error thrown by Mayam while updating Segmentation data for asset ID: " + assetID);
//					e.printStackTrace();
//				}

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

		boolean packageFound = false;

		AttributeMap materialAttributes;
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
				if (segmentList.getAttributeMap().getAttribute(Attribute.HOUSE_ID).equals(presentationID))
				{
					return segmentList;
				}
			}

			throw new PackageNotFoundException();
		}
		catch (RemoteException e)
		{
			//TODO clarify which:
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
