package com.mediasmiths.foxtel.ip.mayam.pdp;

import javax.xml.ws.WebServiceException;
import java.util.HashMap;
import java.util.Map;


/**
 * Mayam sends JSON that looks like this. A collection of mapped variables.
 *
 * {
  "aggregator" : "SIT",
  "asset_access" : {
    "standard" : [ {
      "entityType" : "GROUP",
      "entity" : "GS-MAM_FULL_BOPS",
      "read" : true,
      "write" : true,
      "admin" : false
    } ],
    "media" : [ {
      "entityType" : "GROUP",
      "entity" : "GS-MAM_FULL_BOPS",
      "read" : true,
      "write" : true,
      "admin" : false
    } ]
  }
 *
 */
public class MayamPDPImpl implements MayamPDP
{



	@Override
	public String segmentMismatch(final Map<String, String> attributeMap)
	{
	    validateAttributeMap(attributeMap, null, null);


		return null;
	}


	/**
	 *
	 * @param attributeMap the incoming Mayam Attribute map
	 * @param attributeNamesRequired the required fields that must be set in this attribute map.
	 * @return true if all the attribute names in attributeNamesRequired are in the asset map.
	 */
	private boolean validateAttributeMap(Map<String, String> attributeMap, String... attributeNamesRequired)
	{
		if (attributeMap == null || attributeMap.keySet().isEmpty())
			throw new WebServiceException("MAYAM Attribute Map is EMPTY");

		if (attributeNamesRequired == null || attributeNamesRequired.length == 0)
			throw new IllegalArgumentException("MayamPDP Internal Error: There must be some input map names from Mayam");

		for (String mapItemName: attributeNamesRequired)
		{
			if (!attributeMap.keySet().contains(mapItemName))
				throw new WebServiceException("Mayam Attribute " + mapItemName + " must be set in this call to the PDP");
		}

		return true;
	}


	/**
	 *
	 * @param success permit the result
	 * @param feedBackMessage message (can be null) to be displayed
	 *
	 * @return the standard attribute map result for Mayam.
	 */
	private Map<String, String> formResult(boolean success, String feedBackMessage)
	{
		Map<String, String> result = new HashMap<>();


		return result;
	}


}
