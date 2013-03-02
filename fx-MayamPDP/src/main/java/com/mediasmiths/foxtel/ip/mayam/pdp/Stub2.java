package com.mediasmiths.foxtel.ip.mayam.pdp;

import com.google.inject.Inject;
import com.mayam.wf.attributes.server.AttributeMapMapper;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.exception.RemoteException;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.ws.WebServiceException;




public class Stub2 implements MayamPDP
{



	// ---------------

	private Logger logger = Logger.getLogger(MayamPDPImpl.class);

	private String okStatus;

	
	private AttributeMapMapper mapper;

	@Inject
	public Stub2(AttributeMapMapper mapper)
	{
		AttributeMap okStatusMap = new AttributeMap();
		okStatusMap.setAttribute(Attribute.OP_STAT, "ok");
		this.mapper=mapper;
		okStatus =  mapper.serialize(okStatusMap);
	}


	@Override
	public String ping()
	{
		return "<html>Ping</html>";
	}

	@Override
	public String segmentMismatch(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}

	@Override
	public String segmentClassificationCheck(final String attributeMapStr)
	{
		return okStatus;
	}

	@Override
	public String uningestProtected(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}

	@Override
	public String protect(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}

	@Override
	public String unprotect(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String delete(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}

	@Override
	public String proxyfileCheck(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}

	@Override
	public String ingest(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}

	@Override
	public String complianceEdit(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}

	@Override
	public String complianceLogging(final String attributeMapStr)
	{
		return okStatus;
	}

	@Override
	public String unmatched(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}

	@Override
	public String preview(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}

	@Override
	public String autoQC(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}

	@Override
	public String txDelivery(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}

	@Override
	public String exportMarkers(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}



	private void dumpPayload(final String attributeMapStr)
	{
		logger.info(attributeMapStr);
//		if (attributeMap == null)
//		{
//			logger.info("Payload is null");
//		}
//		else if (attributeMap.getAttributeSet().isEmpty())
//		{
//			logger.info("There is not data in the received map");
//		}
//		else
//		{
//			for (Attribute key: attributeMap.getAttributeSet())
//			{
//				logger.info(key + "<->" + attributeMap.getAttributeAsString(key));
//			}
//		}
	}


	/**
	 *
	 * @param attributeMap the incoming Mayam Attribute map
	 * @param attributeNamesRequired the required fields that must be set in this attribute map.
	 * @return true if all the attribute names in attributeNamesRequired are in the asset map.
	 */
	private boolean validateAttributeMap(AttributeMap attributeMap, Attribute... attributeNamesRequired)
	{
		if (attributeMap == null || attributeMap.getAttributeSet().isEmpty())
			throw new WebServiceException("MAYAM Attribute Map is EMPTY");

		if (attributeNamesRequired == null || attributeNamesRequired.length == 0)
			throw new IllegalArgumentException("MayamPDPSetUp Internal Error: There must be some input map names from Mayam");

		for (Attribute a: attributeNamesRequired)
		{
			if (!attributeMap.getAttributeSet().contains(a))
				throw new WebServiceException("Mayam Attribute " + a + " must be set in this call to the PDP");
		}

		return true;
	}


	@Override
	@Path("complianceProxy")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public String complianceProxy(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}


	@Override
	@Path("captionsProxy")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public String captionsProxy(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}


	@Override
	@Path("publicityProxy")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public String publicityProxy(final String attributeMapStr)
	{
		dumpPayload(attributeMapStr);
		return okStatus;
	}


	@Override
	@Path("qcParallel")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public String qcParallel(String attributeMapStr) throws RemoteException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String fileHeaderVerifyOverride(final String attributeMapStr) throws RemoteException
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

}

