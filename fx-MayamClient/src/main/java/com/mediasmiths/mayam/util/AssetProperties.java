package com.mediasmiths.mayam.util;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AudioTrack;
import com.mayam.wf.attributes.shared.type.AudioTrackList;
import com.mayam.wf.attributes.shared.type.MediaStatus;
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

	public static boolean isMaterialDolbyE(AttributeMap map)
	{

		AudioTrackList audioTracks = map.getAttribute(Attribute.AUDIO_TRACKS);

		for (AudioTrack audioTrack : audioTracks)
		{
			if (audioTrack.getEncoding() != null && audioTrack.getEncoding().equals(AudioTrack.EncodingType.DOLBY_E))
			{
				return true;
			}
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
}
