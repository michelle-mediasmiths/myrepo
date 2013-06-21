package com.mediasmiths.mule.worflows;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.wf.adapter.model.InvokeExport;
import com.mediasmiths.foxtel.wf.adapter.model.InvokeIntalioQCFlow;
import com.mediasmiths.foxtel.wf.adapter.model.InvokeIntalioTXFlow;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mq.MediasmithsDestinations;
import com.mediasmiths.mule.IMuleClient;
import com.mediasmiths.mule.MuleClientImpl;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.log4j.Logger;
import org.mule.api.MuleException;

import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;

public class MuleWorkflowController {

	private IMuleClient client;
	private final static Logger log = Logger.getLogger(MayamMaterialController.class);
	
	@Inject
	private MediasmithsDestinations destinations;
	
	@Inject
	@Named("wfe.serialiser")
	private JAXBSerialiser serialiser;
	
	public MuleWorkflowController() 
	{
		try {
			client = new MuleClientImpl();
		} catch (MuleException e) {
			log.error("Exception while initialising MuleClient : ", e);
		}
		
	}
	
	public void initiateQcWorkflow(String assetID, boolean isTx, long taskID, String title) throws UnsupportedEncodingException, JAXBException, MuleException
	{
		InvokeIntalioQCFlow invokeQc= new InvokeIntalioQCFlow();
		invokeQc.setAssetId(assetID);
		invokeQc.setForTXDelivery(isTx);
		invokeQc.setTaskID(taskID);
		invokeQc.setTitle(title);
		
		String payload = getSerialisationOf(invokeQc);
		log.info("About to send message to Mule to initiate QC workflow. Destination : " + destinations.getMule_qc_destination() + " Payload: " + payload);
		
		client.dispatch(destinations.getMule_qc_destination(), payload, null);
		
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
	
	public void initiateTxDeliveryWorkflow(InvokeIntalioTXFlow startMessage) throws UnsupportedEncodingException, JAXBException, MuleException{
		String payload = getSerialisationOf(startMessage);
	
		log.info("About to send message to Mule to initiate TX workflow. Destination : " + destinations.getMuleExportDestination() + " Payload: " + payload);
		client.dispatch(destinations.getMule_tx_destination(), payload, null);
	}
	
	public void initiateExportWorkflow(InvokeExport ie) throws UnsupportedEncodingException, JAXBException, MuleException
	{
		String payload = getSerialisationOf(ie);
		log.info("About to send message to Mule to initiate Export workflow. Destination : " + destinations.getMuleExportDestination() + " Payload: " + payload);
		client.dispatch(destinations.getMuleExportDestination(), payload, null);
	}
	
	protected String getSerialisationOf(Object payload) throws JAXBException, UnsupportedEncodingException
	{
		return serialiser.serialise(payload);
		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		wfeMarshaller.marshal(payload, baos);
//		return baos.toString("UTF-8");
	}
	
}
