package com.mediasmiths.mayam;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;

public class LogUtil
{

	/**
	 * Returns an attribute map as a string for logging
	 * 
	 * @param map
	 * @return
	 */
	public static String mapToString(AttributeMap map)
	{

		try
		{
			StringBuilder sb = new StringBuilder();

			if (map != null)
				for (Attribute entry : map.getAttributeSet())
				{
					sb.append(entry.name());
					sb.append(":");
					sb.append(map.getAttributeAsString(entry));
					sb.append(",\n");
				}

			return sb.toString();
		}
		catch (Exception e)
		{
			return "exception converting attribute map to log string";
		}
	}
}
