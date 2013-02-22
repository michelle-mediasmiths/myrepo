package com.foxtel.ip.mail.variablereplacer;

import com.foxtel.ip.mail.variablereplacer.variablegenerators.DeliveryDetailsVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.EmailConfigGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.PlaceholderMessageEmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.material.MarketingMaterialTypeNotificationVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.material.MaterialNotificationGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.material.MaterialTypeNotificationVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.material.ProgrammeMaterialTypeNotificationVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.preview.PreviewEventDetailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.qcdata.AutoQcErrorNotificationVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.qcdata.AutoQcFailureNotificationVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.qcdata.AutoQcPassedNotificationVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.system.ComplianceLoggingMarkerVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.tcdata.TcFailedNotificationVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.tcdata.TcPassedNotificationVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.tcdata.TcTotalFailureNotificationVariableGenerator;
import com.mediasmiths.foxtel.ip.common.events.ComplianceLoggingMarkerType;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;

public class VariableReplacer
{
	private static final transient Logger logger = Logger.getLogger(VariableReplacer.class);

	public static final JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance("com.mediasmiths.foxtel.ip.common.events:com.mediasmiths.foxtel.generated.MaterialExchange:au.com.foxtel.cf.mam.pms");

	private List<EmailVariableGenerator> generators = new ArrayList<EmailVariableGenerator>();
	EmailConfigGenerator emailConfigGenerator = new EmailConfigGenerator();

	public VariableReplacer()
	{
		generators.add(new PlaceholderMessageEmailVariableGenerator());

		generators.add(new TcPassedNotificationVariableGenerator());
		generators.add(new TcFailedNotificationVariableGenerator());
		generators.add(new TcTotalFailureNotificationVariableGenerator());

		generators.add(new AutoQcErrorNotificationVariableGenerator());
		generators.add(new AutoQcFailureNotificationVariableGenerator());
		generators.add(new AutoQcPassedNotificationVariableGenerator());

		generators.add(new MaterialNotificationGenerator());
		generators.add(new MarketingMaterialTypeNotificationVariableGenerator());
		generators.add(new MaterialTypeNotificationVariableGenerator());
		generators.add(new ProgrammeMaterialTypeNotificationVariableGenerator());

		generators.add(new PreviewEventDetailVariableGenerator());
		generators.add(new DeliveryDetailsVariableGenerator());
		generators.add(new ComplianceLoggingMarkerVariableGenerator());

	}

	/**
	 * Unmarshallers item returned and finds a relevant generator and returns email
	 * 
	 * @param mailTemplate
	 * @param payload
	 * @param comment
	 * @return
	 */
	public MailTemplate generate(MailTemplate mailTemplate, String payload, String comment)
	{
		if (logger.isTraceEnabled())
			logger.info("VariableReplacer Called");

		if (logger.isTraceEnabled())
			logger.info("Payload Recieved : " + payload);
		final Object obj = unmarshal(payload);
		if (obj != null)
		{
			for (EmailVariableGenerator generator : generators)
			{

				if (generator.handles(obj))
				{
					logger.info("Generator to handle object: " + generator.getClass().toString());

					if (generator.getClass() == ComplianceLoggingMarkerVariableGenerator.class)
					{
						logger.info("Matches ComplianceLoggingMarkerVariableGenerator");
						// logger.info("ComplianceLoggingMarkerVariableGenerator payload " + payload);

						ComplianceLoggingMarkerType complianceLoggingMarker = (ComplianceLoggingMarkerType) obj;

						// logger.info("Email logger set " + complianceLoggingMarker.getLoggerdetails());
						logger.info("Assigning mail template email as obj email");

						mailTemplate.setEmailaddresses(complianceLoggingMarker.getEmailaddresses());
					}

					if (logger.isTraceEnabled())
						logger.info("VariableReplacer Found");
					EmailVariables variables = generator.getVariables(obj, comment);

					if (logger.isTraceEnabled())
						logger.info("Before proccessing: " + mailTemplate.getSubject());
					if (mailTemplate.getSubject() != null)
					{
						mailTemplate.setSubject(variables.format(mailTemplate.getSubject()));
					}
					if (logger.isTraceEnabled())
						logger.info("After proccessing: " + mailTemplate.getSubject());

					if (logger.isTraceEnabled())
						logger.info("Before body proccessing: " + mailTemplate.getBody());
					mailTemplate.setBody(variables.format(mailTemplate.getBody()));
					if (logger.isTraceEnabled())
						logger.info("After body proccessing: " + mailTemplate.getBody());

					return mailTemplate;
				}
			}
		}
		if (logger.isTraceEnabled())
			logger.error("Could not find generator, will now generate generic message and send to specified email");

		MailTemplate newmail = new MailTemplate();
		newmail.setEmailaddresses(mailTemplate.getEmailaddresses());
		newmail.setSubject("Email Error: Could not find generator");
		newmail.setBody("There was a problem processing the following payload, :\n" + payload);
		return newmail;

	}

	public Object unmarshal(String xml)
	{
		try
		{
			if (logger.isTraceEnabled())
				logger.info("Xml received by unmarshaller: " + xml);


			Object obj = JAXB_SERIALISER.deserialise(xml);

			if (obj instanceof JAXBElement)
			{
				return ((JAXBElement<?>)obj).getValue();
			}
			else
			{
				return obj;
			}

		}
		catch (Exception e)
		{
			logger.error("Could not validate Against Schema: " + xml);
			logger.error(e);

		}
		return null;
	}

}
