package com.mediasmiths.mayam;

import java.util.List;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.Segment;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.controllers.MayamPackageController;
import com.mediasmiths.mayam.controllers.MayamTitleController;

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

	public FullProgrammePackageInfo(
			String packageId,
			MayamPackageController packageController,
			MayamMaterialController materialController,
			MayamTitleController titleController) throws MayamClientException
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

		if (!contentType.equals(MayamMaterialController.PROGRAMME_MATERIAL_CONTENT_TYPE))
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
		titleID = materialAttributes.getAttribute(Attribute.PARENT_HOUSE_ID);
		titleAttributes = titleController.getTitle(titleID);
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
}
