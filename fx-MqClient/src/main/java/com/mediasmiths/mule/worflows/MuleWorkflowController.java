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
	
	public void generateReport(String name, String namespace, String reportType)
	{
		String request = "<eventEntity>";
		request += "<time>" + new java.util.Date().toString() + "</time>";
		request += "<eventName>" + name + "</eventName>";
		request += "<namespace>" + namespace + "</namespace>";
		request += "<payload>" + reportType + "</payload>";
		request += "</eventEntity>";
		client.send(MediasmithsDestinations.MULE_REPORTING_DESTINATION, request, null);
	}
	
	public MuleMessage initiateTxDeliveryWorkflow(String assetID)
	{
		String payload = "<?xml version=\"1.0\"?>";
		payload += "<invokeIntalioTCFlow>";
		payload += "<inputFile>" + MediasmithsDestinations.TRANSCODE_INPUT_FILE + "</inputFile>";
		payload += "<outputFolder>" + MediasmithsDestinations.TRANSCODE_OUTPUT_DIR + "</outputFolder>";
		payload += "<packageID>" + assetID + "</packageID>";
		payload += "</invokeIntalioTCFlow>";
		return client.send(MediasmithsDestinations.MULE_TRANSCODE_DESTINATION, payload, null);
	}
	
}
