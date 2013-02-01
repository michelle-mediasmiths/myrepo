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
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.Timecode;
import com.mayam.wf.attributes.shared.type.Timecode.InvalidTimecodeException;
import com.mayam.wf.attributes.shared.type.SegmentList.SegmentListBuilder;
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
			Segmentation segmentation = null; //segmentation information that arrived with the media as part of material exchange
			boolean requiresSegmentationTask = false;
			try
			{
				material = client.assetApi().getAssetBySiteId(MayamAssetType.MATERIAL.getAssetType(), txPackage.getMaterialID());
				if (material != null)
				{
					if(txPackage.getClassification() != null)
					material.setAttribute(Attribute.CONT_CLASSIFICATION, txPackage.getClassification().toString());
					
					if (txPackage.getTargetDate() != null)
					{
						material.setAttribute(Attribute.TX_FIRST, dateUtil.fromXMLGregorianCalendar(txPackage.getTargetDate()).toString());
					}
					
					client.assetApi().updateAsset(material);
					
					//Has Material been set to Preview Pass
					if(material.getAttribute(Attribute.QC_PREVIEW_RESULT).equals(MayamPreviewResults.PREVIEW_PASSED) 
							|| material.getAttribute(Attribute.QC_PREVIEW_RESULT).equals(MayamPreviewResults.PREVIEW_PASSED_BUT_REORDER)) {
								requiresSegmentationTask = true;
					}		
					
					
					segmentation = findExistingSegmentInfoForTxPackage(txPackage, material);
					
					if(segmentation==null){
						log.info("no existing segmentation information for this tx package");
					}
					
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
				
					
					
					
					SegmentListBuilder listbuilder = SegmentList.create();
					listbuilder.attributeMap(attributes.getAttributes());
	
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
						
					
					SegmentList segmentList = listbuilder.build();
				
					log.debug("Getting materials asset id");
					String materialAssetID = materialController.getMaterialAttributes(txPackage.getMaterialID()).getAttributeAsString(Attribute.ASSET_ID);
					String revisionID = findHighestRevision(materialAssetID);
					log.debug("creating segment for material "+ materialAssetID+" revision:"+revisionID);
					SegmentList newSegmentList = client.segmentApi().createSegmentList(AssetType.REVISION, revisionID,	segmentList);
					log.info("Created SegmentList with id :"+newSegmentList.getId());
					
					if (requiresSegmentationTask) {
						String houseID = segmentList.getAttributeMap().getAttribute(Attribute.HOUSE_ID);
						long taskID = taskController.createTask(houseID, MayamAssetType.PACKAGE, MayamTaskListType.SEGMENTATION);
						log.info("Segmentation task created with id :"+taskID);
					}
			
					return MayamClientErrorCode.SUCCESS;
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
				log.error("Exception thrown by Mayam while attempting to create Package",e);
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			} 
			catch (MayamClientException e2) 
			{
				log.error("Exception thrown by Mayam while attempting to retrieve task for asset : " + txPackage.getMaterialID(),e2);	
			}
		}
		else
		{
			log.warn("Null package object, unable to create asset");
			returnCode = MayamClientErrorCode.PACKAGE_UNAVAILABLE;
		}
		return returnCode;
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
			for (Package pc : packages)
			{
				if (pc.getPresentationID().equals(txPackage.getPresentationID()))
				{
					return pc.getSegmentation();
				}
			}
		}
		
		//didnt find anything from presentation information that came with material exchange xml, lets use the natural breaks field
		String segNotes = material.getAttribute(Attribute.SEGMENTATION_NOTES);
		
		if(segNotes != null){
			try{
				List<Segment> originalConform = SegmentUtil.stringToSegmentList(segNotes);
				
				if(txPackage.getNumberOfSegments() != null && txPackage.getNumberOfSegments().intValue() != originalConform.size()){
					
					log.info(String.format("number of required segments for package %s doesnt match the nubmer of segments in the original conform, not prepopulating segmentation info", txPackage.getPresentationID()));
					return null; // the number of segments did not match the required number, not going to return the original conform segmentation info
				}
				
				Segmentation s = new Segmentation();
				s.getSegment().addAll(originalConform);
				return s;
			}
			catch(Exception e){
				log.error("error selecting segmentation info for tx package from natural break fields",e);
			}
		
		}
		else{
			log.info("Segmentation notes are null for "+materialId);
		}
		
		return null;
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
				attributes = new MayamAttributeController(client.createAttributeMap());
				boolean attributesValid = true;

				try
				{
					attributesValid &= attributes.setAttribute(Attribute.HOUSE_ID, txPackage.getPresentationID());

					if (txPackage.getClassification() != null)
					{
						attributesValid &= attributes.setAttribute(
								Attribute.CONT_CLASSIFICATION,
								txPackage.getClassification().toString());
					}
					
					if (txPackage.getConsumerAdvice() != null)
					{
						attributesValid &= attributes.setAttribute(Attribute.REQ_NOTES, txPackage.getConsumerAdvice());
					}
					
					// map.setAttribute(Attribute.?????????, txPackage.getNotes());
					if (txPackage.getPresentationFormat() != null)
					{
						attributesValid &= attributes.setAttribute(
								Attribute.REQ_FMT,
								txPackage.getPresentationFormat().toString());
					}
					
					if (txPackage.getNumberOfSegments() != null)
					{
						attributesValid &= attributes.setAttribute(
								Attribute.REQ_NUMBER,
								txPackage.getNumberOfSegments().intValue());
					}
					
					if (txPackage.getTargetDate() != null)
					{
						attributesValid &= attributes.setAttribute(Attribute.TX_FIRST, dateUtil.fromXMLGregorianCalendar(txPackage.getTargetDate()).toString());
					}
					
					attributes.setAttribute(Attribute.PARENT_HOUSE_ID, txPackage.getMaterialID());
					
					if (!attributesValid)
					{
						log.warn(String.format(
								"some attributes did not validate for update of package %s",
								txPackage.getPresentationID()));
					}

					segmentList.getAttributeMap().putAll(attributes.getAttributes());
					log.info("updating SegmentList metadata with id :" + segmentList.getId());
//					client.segmentApi().updateSegmentList(segmentList.getId(), segmentList);
					client.assetApi().updateAsset(segmentList.getAttributeMap());
					log.debug("updated SegmentList metadata with id :" + segmentList.getId());
					

					returnCode = MayamClientErrorCode.SUCCESS;

				}
				catch (RemoteException e)
				{
					log.error(
							"Error thrown by Mayam while updating Segmentation data for package ID: "
									+ txPackage.getPresentationID(),
							e);
					returnCode = MayamClientErrorCode.PACKAGE_UPDATE_FAILED; 
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
					client.segmentApi().deleteSegmentList(segmentList.getId());
				}
				catch (RemoteException e)
				{
					log.error("Error deleting package : " + presentationID, e);
					return MayamClientErrorCode.PACKAGE_DELETE_FAILED;
				}
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

		log.error("code missing! TODO: finish getPackage  once we can get package by siteid!!");
		
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
			return segmentList != null;
		}
		catch (RemoteException e)
		{
			// TODO clarify which:
			log.info("Exception fetching package with id " + presentationID
					+ " it may not exists or there could be an error see stack trace", e);
			return false;
		}
	}

	public SegmentList getSegmentList(String presentationID) throws PackageNotFoundException{
		try
		{
			SegmentList segmentList = client.segmentApi().getSegmentListBySiteId(presentationID);
			return segmentList;
		}
		catch (RemoteException e)
		{
			log.info("Exception fetching package with id " + presentationID);
			log.debug(e);
			throw new PackageNotFoundException(e);
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
