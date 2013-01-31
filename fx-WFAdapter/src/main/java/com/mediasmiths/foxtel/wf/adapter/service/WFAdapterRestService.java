package com.mediasmiths.foxtel.wf.adapter.service;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBException;

import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCErrorNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCPassNotification;
import com.mediasmiths.foxtel.wf.adapter.model.GetQCProfileResponse;
import com.mediasmiths.foxtel.wf.adapter.model.InvokeIntalioTXFlow;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.TCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCPassedNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCTotalFailure;
import com.mediasmiths.foxtel.wf.adapter.model.TXDeliveryFailure;
import com.mediasmiths.mayam.MayamClientException;

@Path("/wf")
public interface WFAdapterRestService
{

   /**
    * Test method to check if service is online
    * @return
    */
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping();
	
	/**
	 * called to transfer material to the configured location for qc of material arriving from partners
	 * 
	 * @param req
	 * @return
	 * @throws MayamClientException 
	 */
	@PUT
	@Path("/qc/transferforqc")
	@Produces("application/xml")
	public AssetTransferForQCResponse transferMaterialForQC(AssetTransferForQCRequest req) throws MayamClientException;
	
	/**
	 * Called to transfer material to the the configure location for transcoding and return that location
	 * 
	 * If transcoding can be performed direct from the materials current location then that location will be returned with no move taking place
	 * 
	 * @param materialID
	 * @return
	 * @throws MayamClientException
	 */
	@PUT
	@Path("/tc/transferfortc")
	@Produces("application/xml")
	public MaterialTransferForTCResponse transferMaterialForTC(MaterialTransferForTCRequest materialID) throws MayamClientException;
	
	/**
	 * called to lookup the name of the qc profile that should be used for a given material 
	 * 
	 * @param materialID
	 * @param isForTXDelivery - indicats that autoqc is being performed as part of tx delivery and different profiles will be selected as apropriate
	 * @return
	 * @throws MayamClientException 
	 */
	@GET
	@Path("/qc/profile")
	@Produces("application/xml")
	public GetQCProfileResponse getProfileForQc(@QueryParam("assetID") String assetID, @QueryParam("isForTX") boolean isForTXDelivery) throws MayamClientException;


	/**
	 * Called to notify that autoqc has failed for an asset
	 * @param notification
	 * @throws MayamClientException
	 */
	@PUT
	@Path("/qc/autoQcFailed")
	public void notifyAutoQCFailed(AutoQCFailureNotification notification) throws MayamClientException;
	
	/**
	 * called to notify that autoqc has succeeded for an asset
	 * @param notification
	 * @throws MayamClientException 
	 */
	@PUT
	@Path("/qc/autoQcPassed")
	public void notifyAutoQCPassed(AutoQCPassNotification notification) throws MayamClientException;
	
	/**
	 * called to notify about a persistent error performing autoqc that will require investigation
	 * @param notification
	 * @throws MayamClientException
	 */
	@PUT
	@Path("/qc/autoQcError")
	public void notifyAutoQCError(AutoQCErrorNotification notification) throws MayamClientException;
	
	/**
	 * indates an error transcoding
	 * @param notification
	 * @return 
	 * @throws MayamClientException
	 */
	
	@PUT
	@Path("/tc/tcFailed")
	@Consumes("application/xml")
	public void notifyTCFailed(TCFailureNotification notification) throws MayamClientException;
	
	/**
	 * indicates there have been multiple errors transcoding and the workflow has given up
	 * @param notification
	 * @throws MayamClientException
	 */
	@PUT
	@Path("/tc/tcFailedTotal")
	@Consumes("application/xml")
	public void notifyTCFailedTotal(TCTotalFailure notification) throws MayamClientException;
	
	
	/**
	 * indates an error transcoding
	 * @param notification
	 * @throws MayamClientException
	 */
	@PUT
	@Path("/tc/tcPassed")
	@Consumes("application/xml")
	public void notifyTCPassed(TCPassedNotification notification) throws MayamClientException;
	
	/**
	 * Returns the output location for transcode of materials to tx packages
	 * @param packageID
	 * @return
	 */
	@GET
	@Path("/tx/transcodeOutputLocation")
	@Produces("text/plain")
	public String transcodeOutputLocationForPackage(@QueryParam("packageID") String packageID);
	
	/**
	 * Returns the location that tx packages to be delivered to
	 * @param packageID
	 * @return
	 */
	@GET
	@Path("/tx/deliveryLocation")
	@Produces("text/plain")
	public String deliveryLocationForPackage(@QueryParam("packageID") String packageID);
	
	/**
	 * @param packageID
	 * @return
	 * @throws MayamClientException 
	 * @throws JAXBException 
	 * @throws IOException 
	 */
	@GET
	@Path("/tx/delivery/writeSegmentXML")
	@Produces("text/plain")
	public boolean writeSegmentXML(@QueryParam("packageID") String packageID) throws MayamClientException, IOException, JAXBException;
	
	/**
	 * Used to query if autoqc is required for a given package
	 * @param taskID
	 * @return
	 * @throws MayamClientException 
	 */
	@GET
	@Path("/tx/autoQCRequired")
	@Produces("text/plain")
	public Boolean autoQCRequiredForTxTask(@QueryParam("taskID") Long taskID) throws MayamClientException;

	/**
	 * Used to indicate there has been a failure in a tx delivery workflow
	 * @param notification
	 */
	@PUT
	@Path("/tx/failed")
	@Consumes("application/xml")
	public void notifyTXDeliveryFailed(TXDeliveryFailure notification);

	@GET
	@Path("/tx/delivery/getAOSegmentXML")
	@Produces("application/xml")
	public String getAOSegmentXML(@QueryParam("packageID") String packageID) throws MayamClientException, JAXBException;
	
	@GET
	@Path("/tx/delivery/getSegmentXML")
	@Produces("application/xml")
	public String getSegmentXML(@QueryParam("packageID") String packageID) throws MayamClientException, JAXBException;
}

