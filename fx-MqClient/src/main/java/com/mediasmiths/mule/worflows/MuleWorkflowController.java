package com.mediasmiths.mule.worflows;

import org.apache.log4j.Logger;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;

import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mq.MediasmithsDestinations;
import com.mediasmiths.mule.IMuleClient;
import com.mediasmiths.mule.MuleClientImpl;

public class MuleWorkflowController {

	private IMuleClient client;
	private final static Logger log = Logger.getLogger(MayamMaterialController.class);
	
	public MuleWorkflowController() 
	{
		try {
			client = new MuleClientImpl();
		} catch (MuleException e) {
			log.error("Exception while initialising MuleClient : "+ e);
			e.printStackTrace();
		}
		
	}
	
	public void initiateQcWorkflow(String assetID, boolean isTx)
	{
		String payload = "<?xml version='1.0'><invokeIntalioQCFlow><assetId>";
		payload += assetID;
		payload += "</assetId><forTXDelivery>";
		payload += isTx;
		payload += "</forTXDelivery></invokeIntalioQCFlow>";
		client.dispatch(MediasmithsDestinations.MULE_QC_DESTINATION, payload, null);
	}
	
	public void generateReport(String reportType, String namespace, Object payload)
	{
		String request = "<eventEntity>";
		request += "<time>" + new java.util.Date().toString() + "</time>";
		request += "<eventName>" + reportType + "</eventName>";
		request += "<namespace>" + namespace + "</namespace>";
		request += "<payload>" + payload.toString() + "</payload>";
		request += "</eventEntity>";
		client.dispatch(MediasmithsDestinations.MULE_REPORTING_DESTINATION, request, null);
	}
	
	public MuleMessage initiateTxDeliveryWorkflow(long assetID)
	{
		String payload = "<?xml version=1.0><invokeIntalioQCFlow><assetId>";
		//TODO: Implement payload for TxDelivery
		return client.send("http://localhost:9085/qc", payload, null);
	}
	
}
