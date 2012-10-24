package com.mediasmiths.mayam.controllers;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.ClassificationEnumType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.mayam.DateUtil;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamPackageController
{
	private final TasksClient client;

	private final Logger log = Logger.getLogger(MayamPackageController.class);

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

			// TODO: Confirm Asset Type with Mayam
			attributesValid = attributesValid
					&& attributes.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.PACKAGE.getAssetType());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_ID, txPackage.getPresentationID());

			// TODO: Asset Parent ID to be added by Mayam shortly
			// attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_PARENT_ID, txPackage.getMaterialID());

			// TODO: Any need to store number of segments?
			// attributesValid = attributesValid && attributes.setAttribute(Attribute, txPackage.getNumberOfSegments()));

			attributesValid = attributesValid
					&& attributes.setAttribute(Attribute.AUX_VAL, txPackage.getClassification().toString());
			attributesValid = attributesValid
					&& attributes.setAttribute(Attribute.COMPLIANCE_NOTES, txPackage.getConsumerAdvice());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.ESC_NOTES, txPackage.getNotes());
			attributesValid = attributesValid
					&& attributes.setAttribute(Attribute.CONT_FMT, txPackage.getPresentationFormat().toString());
			attributesValid = attributesValid && attributes.setAttribute(Attribute.TX_NEXT, txPackage.getTargetDate());

			if (!attributesValid)
			{
				returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
			}

			AttributeMap result;
			try
			{
				result = client.createAsset(attributes.getAttributes());
				if (result == null)
				{
					returnCode = MayamClientErrorCode.PACKAGE_CREATION_FAILED;
				}
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}
		}
		else
		{
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
				assetAttributes = client.getAsset(MayamAssetType.PACKAGE.getAssetType(), txPackage.getPresentationID());
			}
			catch (RemoteException e1)
			{
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				e1.printStackTrace();
			}

			if (assetAttributes != null)
			{
				attributes = new MayamAttributeController(assetAttributes);

				// TODO: Asset Parent ID to be added by Mayam shortly
				// attributesValid = attributesValid && attributes.setAttribute(Attribute.ASSET_PARENT_ID, txPackage.getMaterialID());

				// TODO: Any need to store number of segments?
				// attributesValid = attributesValid && attributes.setAttribute(Attribute, txPackage.getNumberOfSegments()));

				attributesValid = attributesValid
						&& attributes.setAttribute(Attribute.AUX_VAL, txPackage.getClassification().toString());
				attributesValid = attributesValid
						&& attributes.setAttribute(Attribute.COMPLIANCE_NOTES, txPackage.getConsumerAdvice());
				attributesValid = attributesValid && attributes.setAttribute(Attribute.ESC_NOTES, txPackage.getNotes());
				attributesValid = attributesValid
						&& attributes.setAttribute(Attribute.CONT_FMT, txPackage.getPresentationFormat().toString());
				attributesValid = attributesValid && attributes.setAttribute(Attribute.TX_NEXT, txPackage.getTargetDate());

				if (!attributesValid)
				{
					returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
				}

				AttributeMap result;
				try
				{
					result = client.updateAsset(attributes.getAttributes());
					if (result == null)
					{
						returnCode = MayamClientErrorCode.PACKAGE_UPDATE_FAILED;
					}
				}
				catch (RemoteException e)
				{
					e.printStackTrace();
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
			}
			else
			{
				returnCode = MayamClientErrorCode.PACKAGE_FIND_FAILED;
			}
		}
		else
		{
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
				assetAttributes = client.getAsset(MayamAssetType.PACKAGE.getAssetType(), txPackage.getPresentationID());
			}
			catch (RemoteException e1)
			{
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				e1.printStackTrace();
			}

			if (assetAttributes != null)
			{
				attributes = new MayamAttributeController(assetAttributes);

				// TODO: How to map segmentation data?
				/*
				 * Segmentation segmentation = txPackage.getSegmentation(); List<Segment> segments = segmentation.getSegment(); for (int j = 0; j< segments.size(); j++) { Segment segment =
				 * segments.get(j); segment.getDuration(); segment.getEOM(); segment.getSegmentNumber(); segment.getSegmentTitle(); segment.getSOM(); }
				 */

				if (!attributesValid)
				{
					returnCode = MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES;
				}

				AttributeMap result;
				try
				{
					result = client.updateAsset(attributes.getAttributes());
					if (result == null)
					{
						returnCode = MayamClientErrorCode.PACKAGE_UPDATE_FAILED;
					}
				}
				catch (RemoteException e)
				{
					e.printStackTrace();
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
			}
			else
			{
				returnCode = MayamClientErrorCode.PACKAGE_FIND_FAILED;
			}
		}
		else
		{
			returnCode = MayamClientErrorCode.PACKAGE_UNAVAILABLE;
		}
		return returnCode;
	}

	public MayamClientErrorCode deletePackage(String presentationID)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		try {
			client.deleteAsset(MayamAssetType.PACKAGE.getAssetType(), presentationID);
		} catch (RemoteException e) {
			log.error("Error deleting package : "+ presentationID);
			returnCode = MayamClientErrorCode.PACKAGE_DELETE_FAILED;
		}
		return returnCode;
	}

	public boolean packageExists(String presentationID)
	{
		boolean packageFound = false;
		try
		{
			AttributeMap assetAttributes = client.getAsset(MayamAssetType.PACKAGE.getAssetType(), presentationID);
			if (assetAttributes != null)
			{
				packageFound = true;
			}
		}
		catch (RemoteException e1)
		{
			// TODO: Error Handling
			e1.printStackTrace();
		}
		return packageFound;
	}

	public AttributeMap getPackageAttributes(String presentationID)
	{
		AttributeMap assetAttributes = null;
		try
		{
			assetAttributes = client.getAsset(MayamAssetType.PACKAGE.getAssetType(), presentationID);
		}
		catch (RemoteException e1)
		{
			// TODO: Error Handling
			e1.printStackTrace();
		}
		return assetAttributes;
	}

	public PackageType getPackage(String packageID)
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

		// TODO: Asset Parent ID to be added by Mayam shortly
		// TODO: fetch segment information
		// TODO : pt.setNotes ?

		GregorianCalendar c = new GregorianCalendar();
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
