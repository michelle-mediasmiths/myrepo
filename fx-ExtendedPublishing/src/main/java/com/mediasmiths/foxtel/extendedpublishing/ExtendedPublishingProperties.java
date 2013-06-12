package com.mediasmiths.foxtel.extendedpublishing;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import org.apache.log4j.Logger;

public class ExtendedPublishingProperties
{

	private final static Logger log = Logger.getLogger(ExtendedPublishingProperties.class);

	public static boolean isDVD(AttributeMap exportAttributes)
	{
		String requestedFormat = (String) exportAttributes.getAttribute(Attribute.OP_FMT);
		boolean isDVD = false;
		if ("dvd".equals(requestedFormat))
		{
			isDVD = true;
			log.debug("Export requested in dvd format");
		}
		else
		{
			log.debug("Export not requested in dvd format");
		}
		return isDVD;
	}


	public static String requestedFileName(AttributeMap exportAttributes)
	{
		final String filename = (String) exportAttributes.getAttribute(Attribute.OP_FILENAME);
		return filename;
	}


	public static TranscodeJobType jobType(AttributeMap exportAttributes)
	{
		return TranscodeJobType.fromText((String) exportAttributes.getAttribute(Attribute.OP_TYPE));
	}

	public static String channel(AttributeMap exportAttributes){
		return exportAttributes.getAttributeAsString(Attribute.CHANNEL);
	}
}
