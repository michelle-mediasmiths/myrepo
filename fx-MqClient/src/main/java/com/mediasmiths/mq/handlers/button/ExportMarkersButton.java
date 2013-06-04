package com.mediasmiths.mq.handlers.button;


import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.ip.common.events.ComplianceLoggingMarker;
import com.mediasmiths.foxtel.ip.common.events.Emailaddresses;
import com.mediasmiths.mayam.MayamButtonType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;


public class ExportMarkersButton extends ButtonClickHandler
{

	private static final String COMPLIANCE_LOGGING_MARKER = "ComplianceLoggingMarker";
	private final static Logger log = Logger.getLogger(ExportMarkersButton.class);

	@Inject(optional=false)
	@Named("system.events.namespace")
	private String systemEventNamespace;
	
	@Inject
	@Named("fxcommon.serialiser")
	private JAXBSerialiser commonSerialiser;
	
	@Override
	protected void buttonClicked(AttributeMap messageAttributes)
	{
		try
		{
			String user = messageAttributes.getAttributeAsString(Attribute.TASK_CREATED_BY);
			String email = String.format("%s@foxtel.com.au",user);
			
			log.info(String.format("using email address %s for user %s",email, user));
			
			final String markers = materialController.getMarkersString(messageAttributes, user);
			
			ComplianceLoggingMarker clm = new ComplianceLoggingMarker();
			clm.setLoggerdetails(markers);
			clm.setMasterID(messageAttributes.getAttributeAsString(Attribute.HOUSE_ID));
			clm.setTitleField(messageAttributes.getAttributeAsString(Attribute.PARENT_HOUSE_ID));
			Emailaddresses emails = new Emailaddresses();
			emails.getEmailaddress().add(email);
			clm.setEmailaddresses(emails);
			
			String eventName = COMPLIANCE_LOGGING_MARKER;
			String event = compLoggingMarkerInfoToString(clm);
			String namespace = systemEventNamespace;

			eventsService.saveEvent(eventName, event, namespace);
			
		}
		catch (MayamClientException e)
		{
			log.error("error exporting markers",e);
		}
		catch(Exception e)
		{
			log.error("error exporting markers",e);
		}
	}

	private String compLoggingMarkerInfoToString(ComplianceLoggingMarker clm) throws JAXBException
	{
		return commonSerialiser.serialise(clm);
	}

	@Override
	public MayamButtonType getButtonType()
	{
		return MayamButtonType.EXPORT_MARKERS;
	}

	@Override
	public String getName()
	{
		return "Export Markers Button";
	}

}
