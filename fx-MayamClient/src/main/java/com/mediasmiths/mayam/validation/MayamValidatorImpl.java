package com.mediasmiths.mayam.validation;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.GenericTable;
import com.mayam.wf.attributes.shared.type.GenericTable.Row;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.veneer.TasksClientVeneer;
import org.apache.log4j.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

public class MayamValidatorImpl implements MayamValidator {
	private TasksClientVeneer client;
	
	protected final static Logger log=Logger.getLogger(MayamValidatorImpl.class);
	
	@Inject
	public MayamValidatorImpl(TasksClientVeneer client) 
	{
		this.client=client;
	}
		
	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.validation.MayamValidator#validateMaterialBroadcastDate(javax.xml.datatype.XMLGregorianCalendar, java.lang.String)
	 */
	@Override
	public boolean validateMaterialBroadcastDate(XMLGregorianCalendar targetDate, String materialID, String channelTag) throws MayamClientException 
	{
		boolean isValid = true;
		AttributeMap material = null;
		try {
			material = client.assetApi().getAssetBySiteId(AssetType.valueOf(MayamAssetType.MATERIAL.getText()), materialID);
		} catch (RemoteException e) {
			//TODO material doesnt exist, there for cant be invalid? or do we go with material doesnt exist therefore it *is* invalid
			//either way a message shouldnt be rejected based on this kind of information as it is managed by bms, still log the information
			log.info("unable to validate broadcast date for material"+materialID, e);
			return true;
			
		}
		if (material != null) {
			String parentID = material.getAttribute(Attribute.PARENT_HOUSE_ID);
			AttributeMap title = null;
			
			try {
				title = client.assetApi().getAssetBySiteId(AssetType.valueOf(MayamAssetType.TITLE.getText()), parentID);
			} catch (RemoteException e) {
				isValid = false;
				throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
			}
			
			if (title != null) {
				GenericTable mediaRights = title.getAttribute(Attribute.MEDIA_RIGHTS);
				if (mediaRights != null) {
					List<Row> rows = mediaRights.getRows();
					if (rows != null) {
						for (int i = 0; i < rows.size(); i++) 
						{
							String channel = rows.get(i).get(5);
							if (channel.equals(channelTag))
							{
								XMLGregorianCalendar licenseStartDate = null;
								XMLGregorianCalendar licenseEndDate = null;
								try {
									licenseStartDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(rows.get(i).get(2));
									licenseEndDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(rows.get(i).get(3));
								} catch (DatatypeConfigurationException e) {
									return false;
								}
								isValid = (licenseStartDate.compare(targetDate) == DatatypeConstants.LESSER) && (licenseEndDate.compare(targetDate) == DatatypeConstants.GREATER);
							}
						}
					}
				}
			}
		}
		return isValid;
	}
	
	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.validation.MayamValidator#validateTitleBroadcastDate(java.lang.String, javax.xml.datatype.XMLGregorianCalendar, javax.xml.datatype.XMLGregorianCalendar)
	 */
	@Override
	public boolean validateTitleBroadcastDate(String titleID, XMLGregorianCalendar licenseStartDate, XMLGregorianCalendar licenseEndDate) throws MayamClientException 
	{
		boolean isValid = true;

		List<AttributeMap> materials = null;
		try {
			AttributeMap title = client.assetApi().getAssetBySiteId(AssetType.valueOf(MayamAssetType.TITLE.getText()), titleID);
			if (title != null) {
				String assetID = title.getAttributeAsString(Attribute.ASSET_ID);
				materials = client.assetApi().getAssetChildren(AssetType.valueOf(MayamAssetType.TITLE.getText()), assetID, AssetType.valueOf(MayamAssetType.MATERIAL.getText()));
			}
			else {
				log.info("Could not find matching title asset for : " + titleID);
				return true;	
			}
		} catch (RemoteException e) {
			//could not retrieve children, treat title date as valid
			log.info("No children returned for "+titleID, e);
			return true;
		}
		for (AttributeMap material: materials) 
		{
			String materialAssetID = material.getAttribute(Attribute.ASSET_ID);
			
			List<AttributeMap> packages = null;
			try {
				packages = client.assetApi().getAssetChildren(AssetType.valueOf(MayamAssetType.MATERIAL.getText()), materialAssetID, AssetType.valueOf(MayamAssetType.PACKAGE.getText()));
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
