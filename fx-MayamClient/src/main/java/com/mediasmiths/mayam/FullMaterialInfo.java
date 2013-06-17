package com.mediasmiths.mayam;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.controllers.MayamPackageController;
import com.mediasmiths.mayam.controllers.MayamTitleController;
import com.mediasmiths.mayam.veneer.AssetApiVeneer;
import com.mediasmiths.mayam.veneer.SegmentApiVeneer;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

/**
 * Given a materialid loads the full information for that material, its parent title and its child packages
 */
public class FullMaterialInfo
{
	
	private final static Logger log = Logger.getLogger(FullMaterialInfo.class);

	private String materialID;
	private AttributeMap materialAttributes;
	private SegmentListList packages;
	private String markers;
	String titleId;
	AttributeMap titleAttributes;

	public FullMaterialInfo(
			String materialID,
			MayamPackageController packageController,
			MayamMaterialController materialController,
			MayamTitleController titleController,
			final AssetApiVeneer assetApi, final SegmentApiVeneer segmentApi) throws MayamClientException
	{

		this.materialID = materialID;

		// get the parent materials attributes
		materialAttributes = materialController.getMaterialAttributes(materialID);

		// get the title
		titleId = materialAttributes.getAttributeAsString(Attribute.PARENT_HOUSE_ID);
		String titleAssetID = materialAttributes.getAttribute(Attribute.ASSET_PARENT_ID);

		if (titleAssetID == null)
		{
			log.info("Item has no parent title, metadata will be incomplete");
		}
		else
		{

			try
			{
				titleAttributes = assetApi.getAsset(MayamAssetType.TITLE.getAssetType(), titleAssetID);
			}
			catch (RemoteException e)
			{
				log.error("Unable to locate asset with ID: " + titleAssetID, e);

				titleAttributes = titleController.getTitle(titleId);
				titleId = titleAttributes.getAttribute(Attribute.HOUSE_ID);

				log.error("Using the parent house id title as a fall back: " + titleId);
			}
		}

		// get markers
		markers = materialController.getMarkersString(materialAttributes, null);

		//find child packages
		try
		{
			packages =  segmentApi.getSegmentListsForAsset(AssetType.ITEM, (String) materialAttributes.getAttribute(Attribute.ASSET_ID));
		}
		catch (RemoteException e)
		{
			log.error("remote exception looking for materials packages",e);
			throw new MayamClientException(MayamClientErrorCode.PACKAGE_FIND_FAILED);
		}
	}

	public String getTitleID()
	{
		return titleId;
	}

	public String getMaterialID()
	{
		return  materialID;
	}

	private Object getTitleAttribute(Attribute a)
	{
		if (titleAttributes == null)
		{
			return null;
		}
		else
		{
			return titleAttributes.getAttribute(a);
		}
	}

	public String getProgrammeTitle()
	{
		return (String) getTitleAttribute(Attribute.SERIES_TITLE);
	}

	public Integer getSeriesNumber()
	{
		return (Integer) getTitleAttribute(Attribute.SEASON_NUMBER);
	}

	public String getEpisodetitle()
	{
		return (String) getTitleAttribute(Attribute.EPISODE_TITLE);
	}

	public Integer getEpisodeNumber()
	{
		return (Integer) getTitleAttribute(Attribute.EPISODE_NUMBER);
	}

	public Date getFirstTXDate()
	{
		return (Date) materialAttributes.getAttribute(Attribute.TX_FIRST);
	}

	public List<String> getChannels()
	{
		return (StringList) getTitleAttribute(Attribute.CHANNELS);
	}

	public String getAdditionalInfo()
	{
		return (String) materialAttributes.getAttribute(Attribute.AUX_CATEGORY);
	}

	public String getPreviewComment()
	{
		return (String) materialAttributes.getAttribute(Attribute.QC_PREVIEW_NOTES);
	}

	public String getMarkers()
	{
		return markers;
	}
	
	public SegmentListList getPackages(){
		return packages;
	}
}
