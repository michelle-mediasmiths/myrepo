package com.mediasmiths.mayam.validation;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.guice.MayamClientModule;

public class MayamValidator {
	private TasksClient client;
	
	@Inject
	public MayamValidator(@Named(MayamClientModule.SETUP_TASKS_CLIENT) TasksClient mayamClient) 
	{
		client = mayamClient;
	}
		
	public boolean validateMaterialBroadcastDate(XMLGregorianCalendar targetDate, String materialID) throws MayamClientException 
	{
		boolean isValid = true;
		AttributeMap material = null;
		try {
			material = client.getAsset(AssetType.valueOf(MayamAssetType.MATERIAL.getText()), materialID);
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
				title = client.getAsset(AssetType.valueOf(MayamAssetType.TITLE.getText()), parentID);
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
	
	public boolean validateTitleBroadcastDate(String titleID, XMLGregorianCalendar licenseStartDate, XMLGregorianCalendar licenseEndDate) throws MayamClientException 
	{
		boolean isValid = true;

		List<AttributeMap> materials = null;
		try {
			materials = client.getAssetChildren(AssetType.valueOf(MayamAssetType.TITLE.getText()), titleID, AssetType.valueOf(MayamAssetType.MATERIAL.getText()));
		} catch (RemoteException e) {
			isValid = false;
			throw new MayamClientException(MayamClientErrorCode.MAYAM_EXCEPTION);
		}
		for (AttributeMap material: materials) 
		{
			String materialID = material.getAttribute(Attribute.ASSET_ID);
			
			List<AttributeMap> packages = null;
			try {
				packages = client.getAssetChildren(AssetType.valueOf(MayamAssetType.MATERIAL.getText()), materialID, AssetType.valueOf(MayamAssetType.PACKAGE.getText()));
			} catch (RemoteException e) {
				isValid = false;
				throw new MayamClientException(MayamClientErrorCode.MAYAM_EXCEPTION);
			}
			
			for (AttributeMap pack: packages) {
				XMLGregorianCalendar targetDate = pack.getAttribute(Attribute.TX_NEXT);
				if (targetDate != null) {
					isValid = isValid && (licenseStartDate.compare(targetDate) == DatatypeConstants.LESSER) && (licenseEndDate.compare(targetDate) == DatatypeConstants.GREATER);
				}	
			}
		}
		return isValid;
	}
}
