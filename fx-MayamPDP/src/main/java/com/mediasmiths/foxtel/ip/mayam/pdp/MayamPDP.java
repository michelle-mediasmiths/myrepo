package com.mediasmiths.foxtel.ip.mayam.pdp;

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
	public String segmentMismatch(Map<String, String> attributeMap);

	String segmentClassificationCheck(Map<String, String> attributeMap);

}
