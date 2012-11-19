package com.mediasmiths.mayam.controllers;

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
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.ValueList;
import com.mayam.wf.attributes.shared.type.SegmentList.SegmentListBuilder;
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

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamPackageController extends MayamController
{
	private final TasksClient client;

	protected final static Logger log = Logger.getLogger(MayamPackageController.class);

	private final DateUtil dateUtil;

	@Inject
	public MayamPackageController(@Named(SETUP_TASKS_CLIENT) TasksClient mayamClient, DateUtil dateUtil)
	{
		client = mayamClient;
		this.dateUtil = dateUtil;
	}

	public MayamClientErrorCode createPackage(PackageType txPackage)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;

		if (txPackage != null)
		{
			attributesValid &= attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.PACKAGE.getAssetType());
			attributesValid &= attributes.setAttribute(Attribute.ASSET_ID, txPackage.getPresentationID());

			attributesValid &= attributes.setAttribute(Attribute.ASSET_PARENT_ID, txPackage.getMaterialID());

			// TODO: Any need to store number of segments?
			// attributesValid &= attributes.setAttribute(Attribute, txPackage.getNumberOfSegments()));

			attributesValid &= attributes.setAttribute(Attribute.AUX_VAL, txPackage.getClassification().toString());
			attributesValid &= attributes.setAttribute(Attribute.COMPLIANCE_NOTES, txPackage.getConsumerAdvice());
			attributesValid &= attributes.setAttribute(Attribute.ESC_NOTES, txPackage.getNotes());
			attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, txPackage.getPresentationFormat().toString());
			attributesValid &= attributes.setAttribute(Attribute.TX_NEXT, txPackage.getTargetDate());

			if (!attributesValid)
			{
				log.warn("PAckage created but one or more attributes was invalid");
				returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
			}

			AttributeMap result;
			try
			{
				result = client.assetApi().createAsset(attributes.getAttributes());
				if (result == null)
				{
					log.warn("Mayam failed to create Package");
					returnCode = MayamClientErrorCode.PACKAGE_CREATION_FAILED;
				}
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
		boolean attributesValid = true;

		if (txPackage != null)
		{
			AttributeMap assetAttributes = null;
			MayamAttributeController attributes = null;
			try
			{
				assetAttributes = client.assetApi().getAsset(MayamAssetType.PACKAGE.getAssetType(), txPackage.getPresentationID());
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

				attributesValid &= attributes.setAttribute(Attribute.ASSET_PARENT_ID, txPackage.getMaterialID());

				// TODO: Any need to store number of segments?
				// attributesValid &= attributes.setAttribute(Attribute, txPackage.getNumberOfSegments()));

				attributesValid &= attributes.setAttribute(Attribute.AUX_VAL, txPackage.getClassification().toString());
				attributesValid &= attributes.setAttribute(Attribute.COMPLIANCE_NOTES, txPackage.getConsumerAdvice());
				attributesValid &= attributes.setAttribute(Attribute.ESC_NOTES, txPackage.getNotes());
				attributesValid &= attributes.setAttribute(Attribute.CONT_FMT, txPackage.getPresentationFormat().toString());
				attributesValid &= attributes.setAttribute(Attribute.TX_NEXT, txPackage.getTargetDate());

				if (!attributesValid)
				{
					log.warn("Package updated but one or more attributes was invalid");
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
				assetAttributes = client.assetApi().getAsset(MayamAssetType.PACKAGE.getAssetType(), txPackage.getPresentationID());
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
				try {
					AttributeMap asset = client.assetApi().getAsset(MayamAssetType.PACKAGE.getAssetType(), assetID);
					String revisionID = asset.getAttribute(Attribute.REVISION_ID);
			
					Segmentation segmentation = txPackage.getSegmentation(); 
					List<Segment> segments = segmentation.getSegment(); 
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
				catch(RemoteException e)
				{
					log.error("Error thrown by Mayam while updating Segmentation data for asset ID: " + assetID);
					e.printStackTrace();
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
		try
		{
			client.assetApi().deleteAsset(MayamAssetType.PACKAGE.getAssetType(), presentationID);
		}
		catch (RemoteException e)
		{
			log.error("Error deleting package : " + presentationID);
			returnCode = MayamClientErrorCode.PACKAGE_DELETE_FAILED;
		}
		return returnCode;
	}

	public boolean packageExists(String presentationID)
	{
		boolean packageFound = false;
		try
		{
			AttributeMap assetAttributes = client.assetApi().getAsset(MayamAssetType.PACKAGE.getAssetType(), presentationID);
			if (assetAttributes != null)
			{
				packageFound = true;
			}
		}
		catch (RemoteException e1)
		{
			log.error("Exception thrown by Mayam while attempting to retrieve asset :" + presentationID,e1);
		}
		return packageFound;
	}

	public AttributeMap getPackageAttributes(String presentationID) throws MayamClientException
	{
		AttributeMap assetAttributes = null;
		try
		{
			assetAttributes = client.assetApi().getAsset(MayamAssetType.PACKAGE.getAssetType(), presentationID);
		}
		catch (RemoteException e1)
		{
			log.error("Exception thrown by Mayam while attempting to retrieve asset :" + presentationID,e1);
			throw new MayamClientException(MayamClientErrorCode.PACKAGE_FIND_FAILED,e1);
		}
		return assetAttributes;
	}

	/**
	 * returns the package as represented in material exchange
	 * @param packageID
	 * @return
	 * @throws RemoteException 
	 */
	public ProgrammeMaterialType.Presentation.Package getPresentationPackage(String packageID) throws MayamClientException
	{
		ProgrammeMaterialType.Presentation.Package p = new ProgrammeMaterialType.Presentation.Package();
		AttributeMap pack = getPackageAttributes(packageID);
		
		p.setPresentationID(packageID);
		//TODO segmentation;
		//p.setSegmentation(value);

		return p;
	}

	public PackageType getPackage(String packageID) throws MayamClientException
	{

		PackageType pt = new PackageType();
		AttributeMap attributes = getPackageAttributes(packageID);

		pt.setPresentationID((String) attributes.getAttribute(Attribute.ASSET_ID));
		String classfication = (String) attributes.getAttribute(Attribute.AUX_VAL);
		if (classfication != null)
		{
			pt.setClassification(ClassificationEnumType.valueOf(classfication));
		}
		else
		{
			log.warn(String.format("package %s has null classification", packageID));
		}
		pt.setConsumerAdvice((String) attributes.getAttribute(Attribute.COMPLIANCE_NOTES));

		String presentationFormat = (String) attributes.getAttribute(Attribute.CONT_FMT);

		if (presentationFormat != null)
		{
			pt.setPresentationFormat(PresentationFormatType.valueOf(presentationFormat));
		}
		else
		{
			log.error(String.format("package %s has null presentationFormat", packageID));
		}

		pt.setMaterialID("" + attributes.getAttribute(Attribute.ASSET_PARENT_ID));

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

}
