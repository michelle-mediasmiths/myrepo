package com.mediasmiths.mule.worflows;

import org.apache.log4j.Logger;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;

import com.google.inject.Inject;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mq.MediasmithsDestinations;
import com.mediasmiths.mule.IMuleClient;
import com.mediasmiths.mule.MuleClientImpl;

public class MuleWorkflowController {

	private IMuleClient client;
	private final static Logger log = Logger.getLogger(MayamMaterialController.class);
	
	@Inject
	private MediasmithsDestinations destinations;
	
	public MuleWorkflowController() 
	{
		try {
			client = new MuleClientImpl();
		} catch (MuleException e) {
			log.error("Exception while initialising MuleClient : ", e);
		}
		
	}
	
	public void initiateQcWorkflow(String assetID, boolean isTx, long taskID, String title)
	{
		String payload = "<?xml version=\"1.0\"?><invokeIntalioQCFlow><assetId>";
		payload += assetID;
		payload += "</assetId><forTXDelivery>";
		payload += isTx;
		payload += "</forTXDelivery>";
		payload += "<taskID>"+taskID+"</taskID>";
		payload += "<title>"+title+"</title>";
		payload+="</invokeIntalioQCFlow>";
		
		if(client==null){
			log.error("mule client is null");	
		}
		if(destinations.getMule_qc_destination() == null){
			log.error("qc destination is null");
		}
		
		client.dispatch(destinations.getMule_qc_destination(), payload, null);
		log.info("Message sent to Mule to initiate QC workflow. Destination : " + destinations.getMule_qc_destination() + " Payload: " + payload);
	}
	
	public void generateReport(String name, String namespace, String reportType)
	{
		String request = "<eventEntity>";
		request += "<time>" + new java.util.Date().toString() + "</time>";
		request += "<eventName>" + name + "</eventName>";
		request += "<namespace>" + namespace + "</namespace>";
		request += "<payload>" + reportType + "</payload>";
		request += "</eventEntity>";
		client.send(destinations.getMule_reporting_destination(), request, null);
		log.info("Message sent to Mule to generate report. Destination : " + destinations.getMule_reporting_destination() + " Payload: " + request);
	}
	
	public MuleMessage initiateTxDeliveryWorkflow(String assetID, long taskID,String title)
	{
		String payload = "<?xml version=\"1.0\"?>";
		payload += "<invokeIntalioTCFlow>";
		payload += "<inputFile>" + MediasmithsDestinations.TRANSCODE_INPUT_FILE + "</inputFile>";
		payload += "<outputFolder>" + MediasmithsDestinations.TRANSCODE_OUTPUT_DIR + "</outputFolder>";
		payload += "<packageID>" + assetID + "</packageID>";
		payload += "<taskID>"+taskID+"</taskID>";
		payload += "<title>"+title+"</title>";
		payload += "</invokeIntalioTCFlow>";
		log.info("Message sent to Mule to initiate Tx Delivery. Destination : " +  destinations.getMule_tc_destination()  + " Payload: " + payload);
		return client.send(destinations.getMule_tc_destination(), payload, null);
	}
	
}
