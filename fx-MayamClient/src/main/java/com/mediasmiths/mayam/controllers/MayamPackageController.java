package com.mediasmiths.mayam.controllers;

import java.util.List;

import au.com.foxtel.cf.mam.pms.PackageType;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package.Segmentation;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MqClient;

public class MayamPackageController {
	private final TasksClient client;
	private final MqClient mq;
	
	public MayamPackageController(TasksClient mayamClient, MqClient mqClient) {
		client = mayamClient;
		mq = mqClient;
	}
	
	public MayamClientErrorCode createPackage(PackageType txPackage)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		if (txPackage != null) {
			AttributeMap assetAttributes = client.createAttributeMap();
			
			//TODO: Confirm Asset Type with Mayam
			assetAttributes.setAttribute(Attribute.ASSET_TYPE, AssetType.PACK);	
			assetAttributes.setAttribute(Attribute.ASSET_ID, txPackage.getPresentationID());
			
			//TODO: Asset Parent ID to be added by Mayam shortly
			//assetAttributes.setAttribute(Attribute.ASSET_PARENT_ID, txPackage.getMaterialID());
			
			//TODO: Any need to store number of segments?
			//assetAttributes.setAttribute(Attribute, txPackage.getNumberOfSegments()));
			
			assetAttributes.setAttribute(Attribute.AUX_VAL, txPackage.getClassification().toString());
			assetAttributes.setAttribute(Attribute.COMPLIANCE_NOTES, txPackage.getConsumerAdvice());
			assetAttributes.setAttribute(Attribute.ESC_NOTES, txPackage.getNotes());
			assetAttributes.setAttribute(Attribute.CONT_FMT, txPackage.getPresentationFormat().toString());
			assetAttributes.setAttribute(Attribute.TX_NEXT, txPackage.getTargetDate());
			
			AttributeMap result;
			try {
				result = client.createAsset(assetAttributes);
				if (result == null) {
					returnCode = MayamClientErrorCode.PACKAGE_CREATION_FAILED;
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				e.printRemoteMessages(System.err);
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
			}
		}
		else {
			returnCode = MayamClientErrorCode.PACKAGE_UNAVAILABLE;
		}
		return returnCode;
	}
	
	public MayamClientErrorCode updatePackage(PackageType txPackage)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		if (txPackage != null) {
			AttributeMap assetAttributes = null;
			try {
				assetAttributes = client.getAsset(AssetType.PACK, txPackage.getPresentationID());
			} catch (RemoteException e1) {
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				e1.printStackTrace();
			}
			
			if (assetAttributes != null) {
				//TODO: Asset Parent ID to be added by Mayam shortly
				//assetAttributes.setAttribute(Attribute.ASSET_PARENT_ID, txPackage.getMaterialID());
				
				//TODO: Any need to store number of segments?
				//assetAttributes.setAttribute(Attribute, txPackage.getNumberOfSegments()));
				
				assetAttributes.setAttribute(Attribute.AUX_VAL, txPackage.getClassification().toString());
				assetAttributes.setAttribute(Attribute.COMPLIANCE_NOTES, txPackage.getConsumerAdvice());
				assetAttributes.setAttribute(Attribute.ESC_NOTES, txPackage.getNotes());
				assetAttributes.setAttribute(Attribute.CONT_FMT, txPackage.getPresentationFormat().toString());
				assetAttributes.setAttribute(Attribute.TX_NEXT, txPackage.getTargetDate());
				
				AttributeMap result;
				try {
					result = client.updateAsset(assetAttributes);
					if (result == null) {
						returnCode = MayamClientErrorCode.PACKAGE_UPDATE_FAILED;
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					e.printRemoteMessages(System.err);
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
			}
			else {
				returnCode = MayamClientErrorCode.PACKAGE_FIND_FAILED;	
			}
		}
		else {
			returnCode = MayamClientErrorCode.PACKAGE_UNAVAILABLE;	
		}
		return returnCode;
	}
	
	public MayamClientErrorCode updatePackage(ProgrammeMaterialType.Presentation.Package txPackage)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		if (txPackage != null) {
			AttributeMap assetAttributes = null;
			try {
				assetAttributes = client.getAsset(AssetType.PACK, txPackage.getPresentationID());
			} catch (RemoteException e1) {
				returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				e1.printStackTrace();
			}
			
			if (assetAttributes != null) {
				//TODO: How to map segmentation data?
/*				Segmentation segmentation = txPackage.getSegmentation();
				List<Segment> segments = segmentation.getSegment();
				for (int j = 0; j< segments.size(); j++) {
					Segment segment = segments.get(j);
					segment.getDuration();
					segment.getEOM();
					segment.getSegmentNumber();
					segment.getSegmentTitle();
					segment.getSOM();
				}*/
				
				AttributeMap result;
				try {
					result = client.updateAsset(assetAttributes);
					if (result == null) {
						returnCode = MayamClientErrorCode.PACKAGE_UPDATE_FAILED;
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					e.printRemoteMessages(System.err);
					returnCode = MayamClientErrorCode.MAYAM_EXCEPTION;
				}
			}
			else {
				returnCode = MayamClientErrorCode.PACKAGE_FIND_FAILED;	
			}
		}
		else {
			returnCode = MayamClientErrorCode.PACKAGE_UNAVAILABLE;	
		}
		return returnCode;
	}
	
	public MayamClientErrorCode deletePackage(String presentationID)
	{
		//TODO: How to delete an asset from Mayam?
		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
}
