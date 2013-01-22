package com.mediasmiths.mayam.util;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AudioTrack;
import com.mayam.wf.attributes.shared.type.AudioTrackList;
import com.mediasmiths.mayam.controllers.MayamMaterialController;

public class AssetProperties
{
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
		
		if("sd".equals(reqFMT.toLowerCase())){
			return true;
		}
		else{
			return false;
		}
	}

	public static boolean isProgramme(AttributeMap currentAttributes)
	{
		String contentType = currentAttributes.getAttribute(Attribute.CONT_MAT_TYPE);
		return (contentType != null && MayamMaterialController.PROGRAMME_MATERIAL_CONTENT_TYPE.equals(contentType));
	}

	public static boolean isAssociated(AttributeMap currentAttributes)
	{
		String contentType = currentAttributes.getAttribute(Attribute.CONT_MAT_TYPE);
		return (contentType != null && MayamMaterialController.ASSOCIATED_MATERIAL_CONTENT_TYPE.equals(contentType));
	}
}
