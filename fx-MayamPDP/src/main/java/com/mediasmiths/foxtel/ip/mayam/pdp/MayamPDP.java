package com.mediasmiths.foxtel.ip.mayam.pdp;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.exception.RemoteException;

import java.util.Map;

/**
 * PDP Service interface for Mayam
 *
 * Provides a client interface for Mayam to Query operational permissions for UI operations.
 *
 * Author: Harmer
 */

@Path("/pdp/")
public interface MayamPDP
{

	@Path("ping")
	@Produces(MediaType.TEXT_HTML)
	public  String ping();

	/**
	 *
	 * @param attributeMap the MAYAM Attribute Map.
	 * @return a result packet that indicates that a user can overide the number of defined segments.
	 *
	 */
	@Path("segmentationComplete")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String segmentationComplete(String attributeMapStr);

	@Path("segmentation")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String segmentation(String attributeMapStr);

	@Path("uningest")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String uningest(String attributeMapStr);

	@Path("uningestProtected")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String uningestProtected(String attributeMapStr);

	@Path("protect")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String protect(String attributeMapStr) throws RemoteException;

	@Path("unprotect")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String unprotect(String attributeMapStr) throws RemoteException;

	@Path("delete")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String delete(String attributeMapStr) throws RemoteException;

	@Path("proxyFileCheck")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String proxyfileCheck(String attributeMapStr);

	@Path("ingest")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String ingest(String attributeMapStr) throws RemoteException;

	@Path("complianceLogging")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String complianceLogging(String attributeMapStr) throws RemoteException;

	@Path("complianceEdit")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String complianceEdit(String attributeMapStr) throws RemoteException;

	@Path("unmatched")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String unmatched(String attributeMapStr) throws RemoteException;

	@Path("preview")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String preview(String attributeMapStr) throws RemoteException;

	@Path("autoQC")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String autoQC(String attributeMapStr) throws RemoteException;

	@Path("txDelivery")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String txDelivery(String attributeMapStr) throws RemoteException;

	@Path("exportMarkers")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String exportMarkers(String attributeMapStr) throws RemoteException;


	@Path("complianceProxy")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String complianceProxy(String attributeMapStr) throws RemoteException;

	@Path("captionsProxy")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String captionsProxy(String attributeMapStr) throws RemoteException;

	@Path("publicityProxy")
	@POST()
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String publicityProxy(String attributeMapStr) throws RemoteException;


	@Path("qcParallel")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public String qcParallel(String attributeMapStr) throws RemoteException;

	@Path("fileHeaderVerifyOverride")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public String fileHeaderVerifyOverride(String attributeMapStr) throws RemoteException;
	
	@Path("matchallowed")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public String matchAllowed(String attributeMapStr) throws RemoteException;


}
