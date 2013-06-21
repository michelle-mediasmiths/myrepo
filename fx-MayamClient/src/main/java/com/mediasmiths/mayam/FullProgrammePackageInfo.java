package com.mediasmiths.mayam;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.Segment;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.AssetApi;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.controllers.MayamPackageController;
import com.mediasmiths.mayam.controllers.MayamTitleController;
import com.mediasmiths.mayam.veneer.AssetApiVeneer;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

/**
 * Given a package id, loads all the info for that package, parent material and grandparent title
 *
 */
public class FullProgrammePackageInfo
{

	private static Logger log = Logger.getLogger(FullProgrammePackageInfo.class);

	SegmentList segmentList;
	String packageId;
	List<Segment> segments;
	AttributeMap packageAttributes;
	String materialID;
	AttributeMap materialAttributes;
	String titleID;
	AttributeMap titleAttributes;

	public FullProgrammePackageInfo(String packageId,
	                                MayamPackageController packageController,
	                                MayamMaterialController materialController,
	                                MayamTitleController titleController,
	                                final AssetApiVeneer assetApi) throws MayamClientException
	{

		this.packageId = packageId;

		segmentList = packageController.getSegmentList(packageId);
		packageAttributes = segmentList.getAttributeMap();
		segments = segmentList.getEntries();

		// find the parent material
		materialID = segmentList.getAttributeMap().getAttributeAsString(Attribute.PARENT_HOUSE_ID);
		if (materialID == null)
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);

		// get the parent materials attributes
		materialAttributes = materialController.getMaterialAttributes(materialID);

		// check that the found material is indeed programme material
		String contentType = materialAttributes.getAttributeAsString(Attribute.CONT_MAT_TYPE);

		if (!MayamMaterialController.PROGRAMME_MATERIAL_CONTENT_TYPE.equals(contentType))
		{
			String error = String.format(
					"CONT_MAT_TYPE for material %s is %s and not %s",
					materialID,
					contentType,
					MayamMaterialController.PROGRAMME_MATERIAL_AGL_NAME);
			log.error(error);
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}


		// get the title
		titleID = materialAttributes.getAttributeAsString(Attribute.PARENT_HOUSE_ID);
		String titleAssetID = materialAttributes.getAttribute(Attribute.ASSET_PARENT_ID);
		try
		{
			titleAttributes = assetApi.getAsset(AssetType.ITEM, titleAssetID);
		}
		catch (RemoteException e)
		{
			log.error("Unable to locate asset with ID: " + titleAssetID, e);

			titleAttributes = titleController.getTitle(titleID);

			log.error("Using the parent house id title as a fall back: " + titleID);
		}
	}
	
	public SegmentList getSegmentList()
	{
		return segmentList;
	}
	
	public String getPackageId()
	{
		return packageId;
	}

	public List<Segment> getSegments()
	{
		return segments;
	}

	public AttributeMap getPackageAttributes()
	{
		return packageAttributes;
	}
	
	public String getMaterialID()
	{
		return materialID;
	}

	public AttributeMap getMaterialAttributes()
	{
		return materialAttributes;
	}

	public String getTitleID()
	{
		return titleID;
	}

	public AttributeMap getTitleAttributes()
	{
		return titleAttributes;
	}

	public String getLocation()
	{
		return (String) titleAttributes.getAttribute(Attribute.LOCATION);
	}

	public Integer getEpisodeNumber()
	{
		return (Integer) titleAttributes.getAttribute(Attribute.EPISODE_NUMBER);
	}

	public String getEpisodeTitle()
	{
		return (String) titleAttributes.getAttribute(Attribute.EPISODE_TITLE);
	}
	
	public String getProductionNumber()
	{
		return (String) titleAttributes.getAttribute(Attribute.PRODUCTION_NUMBER);
	}
	
	public String getProgrammeTitle()
	{
		return (String) titleAttributes.getAttribute(Attribute.SERIES_TITLE);
	}
	
	public Integer getSeriesNumber()
	{
		return (Integer) titleAttributes.getAttribute(Attribute.SEASON_NUMBER);
	}
	
	public String getSeriesYear()
	{
		return (String) titleAttributes.getAttribute(Attribute.SERIES_YEAR);
	}
	
	public Date getFirstTX()
	{
		return (Date) packageAttributes.getAttribute(Attribute.TX_FIRST);
	}

	public List<String> getChannels(){
		return (StringList) titleAttributes.getAttribute(Attribute.CHANNELS);
	}
}
