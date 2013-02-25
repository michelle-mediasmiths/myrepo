package com.mediasmiths.foxtel.ip.mayam.pdp;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Map;

/**
 * PDP Service interface for Mayam
 *
 * Provides a client interface for Mayam to Query operational permissions for UI operations.
 *
 * Author: Harmer
 */

@Path("/pdp")
public interface MayamPDP
{

	/**
	 *
	 * @param attributeMap the MAYAM Attribute Map.
	 * @return a result packet that indicates that a user can overide the number of defined segments.
	 *
	 */
	@Path("segmentMismatch")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public  Map<String, String> segmentMismatch(Map<String, String> attributeMap);

	@Path("segmentClassificationCheck")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public  Map<String, String> segmentClassificationCheck(Map<String, String> attributeMap);

	@Path("uningestProtected")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public  Map<String, String> uningestProtected(Map<String, String> attributeMap);

	@Path("deleteProtected")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public  Map<String, String> deleteProtected(Map<String, String> attributeMap);

	@Path("protect")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public  Map<String, String> protect(Map<String, String> attributeMap);

	@Path("unprotect")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public  Map<String, String> unprotect(Map<String, String> attributeMap);

	@Path("delete")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public  Map<String, String> delete(Map<String, String> attributeMap);

	@Path("proxyFileCheck")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public  Map<String, String> proxyfileCheck(Map<String, String> attributeMap);

	@Path("ingest")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public Map<String, String> ingest(Map<String, String> attributeMap);

	@Path("complianceLogging")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public Map<String, String> complianceLogging(Map<String, String> attributeMap);

	@Path("complianceEdit")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public Map<String, String> complianceEdit(Map<String, String> attributeMap);

	@Path("unmatched")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public Map<String, String> unmatched(Map<String, String> attributeMap);

	@Path("preview")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public Map<String, String> preview(Map<String, String> attributeMap);

	@Path("autoQC")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public Map<String, String> autoQC(Map<String, String> attributeMap);

	@Path("txDelivery")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public Map<String, String> txDelivery(Map<String, String> attributeMap);

	@Path("exportMarkers")
	@GET()
	@Produces("application/json")
	@Consumes("application/json")
	public Map<String, String> exportMarkers(Map<String, String> attributeMap);



}
