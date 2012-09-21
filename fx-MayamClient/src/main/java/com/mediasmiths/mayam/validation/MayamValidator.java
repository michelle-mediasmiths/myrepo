package com.mediasmiths.mayam.validation;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.RemoteException;
import com.mediasmiths.foxtel.placeholder.validation.channels.ChannelValidator;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

public class MayamValidator {
	private TasksClient client;
	
	public MayamValidator(TasksClient mayamClient) 
	{
		client = mayamClient;
	}
	
	public boolean validatePackageFormat(String presentationFormat, String materialID, ChannelValidator channelValidator) throws MayamClientException 
	{
		boolean isValid = true;
		AttributeMap material = null;
		try {
			material = client.getAsset(AssetType.valueOf(MayamAssetType.MATERIAL.toString()), materialID);
		} catch (RemoteException e) {
			isValid = false;
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}
		if (material != null) {
			String parentID = "";
			// TODO: retrieve parentID from material
			// parentID = material.getAttribute(Attribute.);
			AttributeMap title = null;
			
			try {
				title = client.getAsset(AssetType.valueOf(MayamAssetType.TITLE.toString()), parentID);
			} catch (RemoteException e) {
				isValid = false;
				throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
			}
			
			if (title != null) {
				//TODO: Retrieve the channel tag information from the title attributes
				String channelTag = "";
				isValid = channelValidator.isValidFormatForTag(channelTag, presentationFormat);
			}
		}
		return isValid;
	}
	
	public boolean validateBroadcastDate(XMLGregorianCalendar targetDate, String materialID) throws MayamClientException 
	{
		boolean isValid = true;
		AttributeMap material = null;
		try {
			material = client.getAsset(AssetType.valueOf(MayamAssetType.MATERIAL.toString()), materialID);
		} catch (RemoteException e) {
			isValid = false;
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}
		if (material != null) {
			String parentID = "";
			// TODO: retrieve parentID from material
			// parentID = material.getAttribute(Attribute.);
			AttributeMap title = null;
			
			try {
				title = client.getAsset(AssetType.valueOf(MayamAssetType.TITLE.toString()), parentID);
			} catch (RemoteException e) {
				isValid = false;
				throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
			}
			
			if (title != null) {
				//TODO: Retrieve the channel license date information from the title attributes
				GregorianCalendar calendar = new GregorianCalendar();
				XMLGregorianCalendar licenseStartDate = null;
				XMLGregorianCalendar licenseEndDate = null;
				try {
					licenseStartDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
					licenseEndDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
				} catch (DatatypeConfigurationException e) {
					return false;
				}
				isValid = (licenseStartDate.compare(targetDate) == DatatypeConstants.LESSER) && (licenseEndDate.compare(targetDate) == DatatypeConstants.GREATER);
			}
		}
		return isValid;
	}
}
