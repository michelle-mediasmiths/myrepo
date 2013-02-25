package com.mediasmiths.foxtel.ip.mayam.pdp;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public  Map<String, Object> segmentMismatch(Map<String, Object> attributeMap);

	@Path("segmentClassificationCheck")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public  Map<String, Object> segmentClassificationCheck(Map<String, Object> attributeMap);

	@Path("uningestProtected")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public  Map<String, Object> uningestProtected(Map<String, Object> attributeMap);

	@Path("protect")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public  Map<String, Object> protect(Map<String, Object> attributeMap);

	@Path("unprotect")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public  Map<String, Object> unprotect(Map<String, Object> attributeMap);

	@Path("delete")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public  Map<String, Object> delete(Map<String, Object> attributeMap);

	@Path("proxyFileCheck")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public  Map<String, Object> proxyfileCheck(Map<String, Object> attributeMap);

	@Path("ingest")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> ingest(Map<String, Object> attributeMap);

	@Path("complianceLogging")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> complianceLogging(Map<String, Object> attributeMap);

	@Path("complianceEdit")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> complianceEdit(Map<String, Object> attributeMap);

	@Path("unmatched")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> unmatched(Map<String, Object> attributeMap);

	@Path("preview")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> preview(Map<String, Object> attributeMap);

	@Path("autoQC")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> autoQC(Map<String, Object> attributeMap);

	@Path("txDelivery")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> txDelivery(Map<String, Object> attributeMap);

	@Path("exportMarkers")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> exportMarkers(Map<String, Object> attributeMap);



}
