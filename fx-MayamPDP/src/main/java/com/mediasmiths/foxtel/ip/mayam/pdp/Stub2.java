package com.mediasmiths.foxtel.ip.mayam.pdp;

import com.google.inject.Inject;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.ws.WebServiceException;
import java.util.HashMap;
import java.util.Map;




public class Stub2 implements MayamPDP
{



	// ---------------

	private Logger logger = Logger.getLogger(MayamPDPImpl.class);

	private final Map<String,Object> okStatus = new HashMap<String,Object>();


	@Inject
	public Stub2()
	{
	}


	@Override
	public String ping()
	{
		return "<html>Ping</html>";
	}

	@Override
	public Map<String, Object> segmentMismatch(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> segmentClassificationCheck(final Map<String, Object> attributeMap)
	{
		return okStatus;
	}

	@Override
	public Map<String, Object> uningestProtected(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> protect(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> unprotect(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Map<String, Object> delete(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> proxyfileCheck(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> ingest(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> complianceEdit(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> complianceLogging(final Map<String, Object> attributeMap)
	{
		return okStatus;
	}

	@Override
	public Map<String, Object> unmatched(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> preview(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> autoQC(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> txDelivery(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> exportMarkers(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}



	private void dumpPayload(final Map<String, Object> attributeMap)
	{
		if (attributeMap == null)
		{
			logger.info("Payload is null");
		}
		else if (attributeMap.keySet().isEmpty())
		{
			logger.info("There is not data in the received map");
		}
		else
		{
			for (String key: attributeMap.keySet())
			{
				logger.info(key + "<->" + attributeMap.get(key).toString());
			}
		}
	}


	/**
	 *
	 * @param attributeMap the incoming Mayam Attribute map
	 * @param attributeNamesRequired the required fields that must be set in this attribute map.
	 * @return true if all the attribute names in attributeNamesRequired are in the asset map.
	 */
	private boolean validateAttributeMap(Map<String, Object> attributeMap, String... attributeNamesRequired)
	{
		if (attributeMap == null || attributeMap.keySet().isEmpty())
			throw new WebServiceException("MAYAM Attribute Map is EMPTY");

		if (attributeNamesRequired == null || attributeNamesRequired.length == 0)
			throw new IllegalArgumentException("MayamPDPSetUp Internal Error: There must be some input map names from Mayam");

		for (String mapItemName: attributeNamesRequired)
		{
			if (!attributeMap.keySet().contains(mapItemName))
				throw new WebServiceException("Mayam Attribute " + mapItemName + " must be set in this call to the PDP");
		}

		return true;
	}


	@Override
	@Path("complianceProxy")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public Map<String, Object> complianceProxy(Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}


	@Override
	@Path("captionsProxy")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public Map<String, Object> captionsProxy(Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}


	@Override
	@Path("publicityProxy")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public Map<String, Object> publicityProxy(Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

}

