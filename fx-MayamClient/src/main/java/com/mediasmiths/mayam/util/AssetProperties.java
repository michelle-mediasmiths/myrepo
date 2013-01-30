package com.mediasmiths.mayam.util;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.AudioTrack;
import com.mayam.wf.attributes.shared.type.AudioTrackList;
import com.mayam.wf.attributes.shared.type.MediaStatus;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.controllers.MayamMaterialController;

public class AssetProperties
{
	private final static Logger log = Logger.getLogger(AssetProperties.class);

	public static boolean isMaterialSD(AttributeMap map)
	{

		String format = map.getAttribute(Attribute.CONT_FMT);

		if (format.equals("SD"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static boolean isMaterialSurround(AttributeMap map)
	{

		AudioTrackList audioTracks = map.getAttribute(Attribute.AUDIO_TRACKS);

		if (audioTracks.size() > 2)
		{
			return true;
		}

		return false;
	}

	public static boolean isPackageSD(AttributeMap packageAttributes)
	{

		String reqFMT = packageAttributes.getAttributeAsString(Attribute.REQ_FMT);

		if ("sd".equals(reqFMT.toLowerCase()))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public static boolean isMaterialProgramme(AttributeMap currentAttributes)
	{
		String contentType = currentAttributes.getAttribute(Attribute.CONT_MAT_TYPE);
		return (contentType != null && MayamMaterialController.PROGRAMME_MATERIAL_CONTENT_TYPE.equals(contentType));
	}

	public static boolean isMaterialAssociated(AttributeMap currentAttributes)
	{
		String contentType = currentAttributes.getAttribute(Attribute.CONT_MAT_TYPE);
		return (contentType != null && MayamMaterialController.ASSOCIATED_MATERIAL_CONTENT_TYPE.equals(contentType));
	}

	public static boolean isMaterialPlaceholder(AttributeMap materialAttributes)
	{

		MediaStatus mediaStatus = materialAttributes.getAttribute(Attribute.MEDST_HR);
		if (mediaStatus != null)
		{
			log.debug("Media status is " + mediaStatus.toString());
			if (mediaStatus == MediaStatus.MISSING)
			{
				return true;
			}
		}
		else
		{
			log.error("Media status is null");
		}

		return false;

	}

	public static boolean isClassificationSet(AttributeMap packageAttributes)
	{
		String classification = packageAttributes.getAttribute(Attribute.CONT_CLASSIFICATION);
		return classification != null;
	}

	public static boolean isQCPassed(AttributeMap materialAttributes)
	{

		QcStatus qcStatus = materialAttributes.getAttribute(Attribute.QC_STATUS);

		if (qcStatus != null)
		{
			return qcStatus.equals(QcStatus.PASS) || qcStatus.equals(QcStatus.PASS_MANUAL);
		}

		return false;

	}

	public static boolean isPurgeProtected(AttributeMap titleAttributes)
	{

		Boolean purgeProtected = titleAttributes.getAttribute(Attribute.PURGE_PROTECTED);

		if (purgeProtected == null)
		{
			return false;
		}
		else
		{
			return purgeProtected.booleanValue();
		}
	}

	public static boolean isProtected(AttributeMap attributes)
	{
		Boolean p = attributes.getAttribute(Attribute.PURGE_PROTECTED);

		if (p == null)
		{
			return false;
		}
		else
		{
			return p.booleanValue();
		}
	}

	public static boolean isAO(AttributeMap attributes)
	{
		Boolean p = attributes.getAttribute(Attribute.CONT_RESTRICTED_MATERIAL);

		if (p == null)
		{
			return false;
		}
		else
		{
			return p.booleanValue();
		}
	}

}
