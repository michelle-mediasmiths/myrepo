package com.mediasmiths.foxtel.wf.adapter.service;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBException;

import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.wf.adapter.model.AbortFxpTransferRequest;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.AssetTransferForQCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCErrorNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.AutoQCPassNotification;
import com.mediasmiths.foxtel.wf.adapter.model.ExportFailedRequest;
import com.mediasmiths.foxtel.wf.adapter.model.GetPriorityRequest;
import com.mediasmiths.foxtel.wf.adapter.model.GetPriorityResponse;
import com.mediasmiths.foxtel.wf.adapter.model.GetQCProfileResponse;
import com.mediasmiths.foxtel.wf.adapter.model.InvokeIntalioTXFlow;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCRequest;
import com.mediasmiths.foxtel.wf.adapter.model.MaterialTransferForTCResponse;
import com.mediasmiths.foxtel.wf.adapter.model.RemoveTransferRequest;
import com.mediasmiths.foxtel.wf.adapter.model.StartFxpTransferRequest;
import com.mediasmiths.foxtel.wf.adapter.model.TCFailureNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCPassedNotification;
import com.mediasmiths.foxtel.wf.adapter.model.TCTotalFailure;
import com.mediasmiths.foxtel.wf.adapter.model.TXDeliveryFailure;
import com.mediasmiths.foxtel.wf.adapter.model.TXDeliveryFinished;
import com.mediasmiths.foxtel.wf.adapter.model.WriteExportCompanions;
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
	 * Returns the location that tx packages to be delivered to
	 * @param packageID
	 * @return
	 * @throws MayamClientException 
	 */
	@GET
	@Path("/tx/deliveryLocation")
	@Produces("text/plain")
	public String deliveryLocationForPackage(@QueryParam("packageID") String packageID) throws MayamClientException;
	
	@PUT
	@Path("/tx/startFxpTransfer")
	public boolean startFxpTransfer(StartFxpTransferRequest startTransfer) throws MayamClientException;
	
	@PUT
	@Path("/tx/abortFxpTransfer")
	public void abortFxpTransfer(AbortFxpTransferRequest abort) throws IOException;
	
	@PUT
	@Path("/tx/removeFxpTransfer")
	public void removeTransfer(RemoveTransferRequest remove);
	
	@GET
	@Path("/tx/fxpTransferStatus")
	public String fxpTransferStatus(@QueryParam("taskID") Long taskID);
	
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
	 * @throws MayamClientException 
	 */
	@PUT
	@Path("/tx/failed")
	@Consumes("application/xml")
	public void notifyTXDeliveryFailed(TXDeliveryFailure notification) throws MayamClientException;

	/**
	 * Called once tx delivery is complete
	 */
	@PUT
	@Path("/tx/delivered")
	@Consumes("application/xml")
	public void notifiyTXDelivered(TXDeliveryFinished deliveryFinished)throws MayamClientException;
	
	
	@GET
	@Path("/tx/delivery/getAOSegmentXML")
	@Produces("application/xml")
	public String getAOSegmentXML(@QueryParam("packageID") String packageID) throws MayamClientException, JAXBException;
	
	@GET
	@Path("/tx/delivery/getSegmentXML")
	@Produces("application/xml")
	public String getSegmentXML(@QueryParam("packageID") String packageID) throws MayamClientException, JAXBException;
	
	@GET
	@Path("/task/{taskid}/cancelled")
	@Produces("text/plain")
	public boolean isTaskCancelled(@PathParam("taskid") long taskid) throws MayamClientException;
	
	@GET
	@Path("/mex/{materialid}/deliveryversion")
	@Produces("text/plain")
	public Integer deliveryVersionForMaterial(@PathParam("materialid") String materialID) throws MayamClientException;

    @GET
    @Path("/mex/{materialid}/nextDeliveryVersion")
    @Produces("text/plain")
    public Integer nextDeliveryVersionForMaterial(@PathParam("materialid") String materialID) throws MayamClientException;

	/**
	 * Lists items pending purge
	 * @return
	 * @throws MayamClientException
	 */
	@GET
	@Path("/purge/list")
	@Produces("text/plain")
	public String getPurgePendingList() throws MayamClientException;

	/**
	 * Lists items pending purge that perhaps should not be
	 * @return
	 * @throws MayamClientException
	 */
	@GET
	@Path("/purge/suspectList")
	@Produces("text/plain")
	public String getSuspectPurgePendingList() throws MayamClientException;

	/**
	 * Lists items pending purge that perhaps should not be
	 * @return
	 * @throws MayamClientException
	 */
	@DELETE
	@Path("/purge/suspectList")
	@Produces("text/plain")
	public void deleteSuspectPurgePendingList() throws MayamClientException;

	/**
	 * Performs pending purges
	 * @return
	 * @throws MayamClientException
	 */
	@POST
	@Path("/purge/perform")
	@Produces("text/plain")
	public boolean performPendingPerges() throws MayamClientException;
	
	/**
	 * Returns the transcode priority a job should have
	 * @param request
	 * @return
	 * @throws MayamClientException 
	 */
	@POST
	@Path("/tc/priority")
	@Produces("application/xml")
	@Consumes("application/xml")
	public GetPriorityResponse getTCPriority(GetPriorityRequest request) throws MayamClientException;
	
	/**
	 * writes metadata and scripts if required by export task
	 * @param request
	 * @return
	 * @throws MayamClientException
	 * @throws IOException 
	 */
	@POST
	@Path("/export/writeCompanions")
	@Consumes("application/xml")
	@Produces("text/plain")
	public boolean writeExportCompanions(WriteExportCompanions request) throws MayamClientException, IOException;
	
	@GET
	@Path("materialInfo")
	@Produces("text/plain")
	public String getTextualMaterialInfo(@QueryParam("materialID") String materialID) throws MayamClientException;
	
	@POST
	@Path("/export/failed")
	@Consumes("application/xml")
	public void exportFailed(ExportFailedRequest request) throws MayamClientException;

	@GET
	@Path("/export/previewMetadata")
	public String previewExportMetadata(@QueryParam("taskID") Long taskID) throws MayamClientException;


	/**
	 *
	 * Part of ao tx delivery, transfers companion xml from the tx delivery location to the configured ftp server	 *
	 *
	 * @param packageID
	 * @return
	 * @throws MayamClientException
	 * @throws JAXBException
	 * @throws IOException
	 */
	@GET
	@Path("/tx/delivery/ftpAoSegmentXML")
	@Produces("text/plain")
	void ftpTransferForAoSegmentXML(@QueryParam("packageID") String packageID) throws MayamClientException, IOException;
}

