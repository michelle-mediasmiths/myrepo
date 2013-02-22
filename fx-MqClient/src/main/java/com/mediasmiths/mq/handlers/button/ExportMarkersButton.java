package com.mediasmiths.mq.handlers.button;

import java.io.StringWriter;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.Marker;
import com.mayam.wf.attributes.shared.type.MarkerList;
import com.mayam.wf.attributes.shared.type.Timecode;
import com.mayam.wf.attributes.shared.type.Marker.Type;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation;
import com.mediasmiths.foxtel.ip.common.events.ComplianceLoggingMarkerType;
import com.mediasmiths.foxtel.ip.common.events.Emailaddresses;
import com.mediasmiths.foxtel.ip.common.events.ObjectFactory;
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
			
			MarkerList markers = materialController.getMarkers(messageAttributes);
			
			if(markers == null){
				log.warn(String.format("no markers found for item %s", messageAttributes.getAttributeAsString(Attribute.HOUSE_ID)));
				return;
			}
			
			StringBuilder sb = new StringBuilder();
			
			for (Marker marker : markers)
			{
				String id = marker.getId();
				String mediaId = marker.getMediaId();
				Timecode in = marker.getIn();
				Timecode duration = marker.getDuration();
				String title = marker.getTitle();
				Type type = marker.getType();

				String str = String.format(
						"Marker id {%s} mediaID {%s} In: {%s} Duration: {%s} Title: {%s} Type : {%s} Requested by : {%s}\n",
						id,
						mediaId,
						in.toSmpte(),
						duration.toSmpte(),
						title,
						type.toString(),user);
				
				log.debug(str);
				sb.append(str);
			}
			
			
			ComplianceLoggingMarkerType clm = new ComplianceLoggingMarkerType();
			clm.setLoggerdetails(sb.toString());
			clm.setMasterID(messageAttributes.getAttributeAsString(Attribute.HOUSE_ID));
			clm.setTitleField(messageAttributes.getAttributeAsString(Attribute.PARENT_HOUSE_ID));
			Emailaddresses emails = new Emailaddresses();
			emails.getEmailaddress().add(email);
			clm.setEmailaddresses(emails);
			
			String eventName = COMPLIANCE_LOGGING_MARKER;
			String event = compLoggingMarkerInfoToString(clm);
			String namespace = systemEventNamespace;

			log.warn("not sending event for export markers email");
			
//			eventsService.saveEvent(eventName, event, namespace);
			
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

	private String compLoggingMarkerInfoToString(ComplianceLoggingMarkerType clm) throws JAXBException
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
