package com.mediasmiths.mayam.controllers;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.std.types.Framerate;
import com.mediasmiths.std.types.Timecode;
import org.apache.log4j.Logger;

public abstract class MayamController
{

	protected final static Logger log = Logger.getLogger(MayamController.class);
	
	protected String toTimecodeString(Integer i)
	{
		return Timecode.getInstance((long) i.intValue(), false, new Framerate(1, 25), false).toString();
	}

	protected boolean checkAttributeValid(AttributeMap map, Attribute attr, String assetID, String description, Class<?> type)
	{
		Object o = map.getAttribute(attr);
		return checkNotNull(o, assetID, description) && checkType(o, type, assetID, description);
	}

	protected boolean checkNotNull(Object o, String assetID, String description)
	{
		if (o == null)
		{
			log.warn(String.format("%s attribute of asset %s is null", description, assetID));
		}
		return o != null;
	}

	protected boolean checkType(Object o, Class<?> type, String assetID, String description)
	{
		if (!type.isAssignableFrom(o.getClass()))
		{
			log.warn(String.format(
					"%s attribute of material %s (%s) is not of the expected type %s",
					description,
					assetID,
					o.toString(),
					type.toString()));
			return false;
		}
		return true;
	}
}
