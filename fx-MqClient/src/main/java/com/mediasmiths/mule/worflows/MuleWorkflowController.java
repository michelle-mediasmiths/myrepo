package com.mediasmiths.mule.worflows;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.wf.adapter.model.InvokeExport;
import com.mediasmiths.foxtel.wf.adapter.model.InvokeIntalioTXFlow;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mq.MediasmithsDestinations;
import com.mediasmiths.mule.IMuleClient;
import com.mediasmiths.mule.MuleClientImpl;

public class MuleWorkflowController {

	private IMuleClient client;
	private final static Logger log = Logger.getLogger(MayamMaterialController.class);
	
	@Inject
	private MediasmithsDestinations destinations;
	
	@Inject
	@Named("wfe.marshaller")
	private Marshaller wfeMarshaller;
	
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
	
	public void initiateTxDeliveryWorkflow(InvokeIntalioTXFlow startMessage) throws UnsupportedEncodingException, JAXBException{
		String payload = getSerialisationOf(startMessage);
		client.dispatch(destinations.getMule_tx_destination(), payload, null);
		log.info("Message sent to Mule to initiate TX workflow. Destination : " + destinations.getMuleExportDestination() + " Payload: " + payload);
	}
	
	public void initiateExportWorkflow(InvokeExport ie) throws UnsupportedEncodingException, JAXBException
	{
		String payload = getSerialisationOf(ie);
		client.dispatch(destinations.getMule_qc_destination(), payload, null);
		log.info("Message sent to Mule to initiate Export workflow. Destination : " + destinations.getMuleExportDestination() + " Payload: " + payload);
	}
	
	protected String getSerialisationOf(Object payload) throws JAXBException, UnsupportedEncodingException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wfeMarshaller.marshal(payload, baos);
		return baos.toString("UTF-8");
	}
	
}
