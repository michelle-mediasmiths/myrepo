package com.foxtel.ip.mail.variablereplacer;

import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.DeliveryDetailsEmailTemplate;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.EmailConfigGenerator;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.PlaceholderMessageEmailTemplate;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.content.ArdomeFailureEmailTemplate;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.material.MarketingMaterialTypeNotificationEmailTemplate;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.material.MaterialNotificationEmailTemplate;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.material.MaterialTypeNotificationEmailTemplate;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.material.ProgrammeMaterialTypeNotificationEmailTemplate;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.preview.PreviewEventDetailEmailTemplate;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.qcdata.AutoQcErrorNotificationEmailTemplate;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.qcdata.AutoQcFailureNotificationEmailTemplate;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.qcdata.AutoQcPassedNotificationEmailTemplate;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.system.ComplianceLoggingMarkerEmailTemplate;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.tcdata.TcFailedNotificationEmailTemplate;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.tcdata.TcPassedNotificationEmailTemplate;
import com.foxtel.ip.mail.variablereplacer.variablegenerators.tcdata.TcTotalFailureNotificationEmailTemplate;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.ip.common.events.ArdomeJobFailure;
import com.mediasmiths.foxtel.ip.common.events.AutoQCErrorNotification;
import com.mediasmiths.foxtel.ip.common.events.AutoQCFailureNotification;
import com.mediasmiths.foxtel.ip.common.events.AutoQCPassNotification;
import com.mediasmiths.foxtel.ip.common.events.ComplianceLoggingMarker;
import com.mediasmiths.foxtel.ip.common.events.DeliveryDetails;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.PreviewEventDetail;
import com.mediasmiths.foxtel.ip.common.events.TcFailureNotification;
import com.mediasmiths.foxtel.ip.common.events.TcPassedNotification;
import com.mediasmiths.foxtel.ip.common.events.TcTotalFailure;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBElement;
import java.util.HashMap;
import java.util.Map;





public class EMailTemplater
{
	private static final transient Logger logger = Logger.getLogger(EMailTemplater.class);

	public static final JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance("com.mediasmiths.foxtel.ip.common.events:com.mediasmiths.foxtel.generated.MaterialExchange:au.com.foxtel.cf.mam.pms");

	private Map<Class, EmailTemplateGenerator> generators = new HashMap<>();

	EmailConfigGenerator emailConfigGenerator = new EmailConfigGenerator();

	public EMailTemplater()
	{

		PlaceholderMessageEmailTemplate phT = new PlaceholderMessageEmailTemplate();
		generators.put(DeleteMaterial.class, phT);
		generators.put(CreateOrUpdateTitle.class, phT);
		generators.put(PurgeTitle.class, phT);
		generators.put(AddOrUpdateMaterial.class, phT);
		generators.put(AddOrUpdatePackage.class, phT);
		generators.put(DeletePackage.class, phT);

		generators.put(TcPassedNotification.class, new TcPassedNotificationEmailTemplate());
		generators.put(TcFailureNotification.class, new TcFailedNotificationEmailTemplate());
		generators.put(TcTotalFailure.class, new TcTotalFailureNotificationEmailTemplate());

		generators.put(AutoQCErrorNotification.class, new AutoQcErrorNotificationEmailTemplate());
		generators.put(AutoQCFailureNotification.class, new AutoQcFailureNotificationEmailTemplate());
		generators.put(AutoQCPassNotification.class, new AutoQcPassedNotificationEmailTemplate());

		generators.put(Material.class, new MaterialNotificationEmailTemplate());
		generators.put(MarketingMaterialType.class, new MarketingMaterialTypeNotificationEmailTemplate());
		generators.put(MaterialType.class, new MaterialTypeNotificationEmailTemplate());
		generators.put(ProgrammeMaterialType.class, new ProgrammeMaterialTypeNotificationEmailTemplate());

		generators.put(PreviewEventDetail.class, new PreviewEventDetailEmailTemplate());
		generators.put(DeliveryDetails.class, new DeliveryDetailsEmailTemplate());
		generators.put(ComplianceLoggingMarker.class, new ComplianceLoggingMarkerEmailTemplate());

		generators.put(ArdomeJobFailure.class, new ArdomeFailureEmailTemplate());
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
			logger.info("EMailTemplater Called");

		if (logger.isTraceEnabled())
			logger.info("Payload Received : " + payload);

		final Object obj = unmarshal(payload);
		if (obj != null)
		{
			EmailTemplateGenerator generator = generators.get(obj.getClass());

			if (generator != null)
			{
				logger.info("Generator to handle object: " + generator.getClass().toString());

				generator.customiseTemplate(mailTemplate,  obj, comment);

			}
			else
			{
				mailTemplate.setBody("There was a problem Template processing the following payload, :\n" + payload);
			}
		}
		else
		{
			if (logger.isTraceEnabled())
				logger.error("Could not find generator, will now generate generic message and send to specified email");

			mailTemplate.setBody("There was a unmarshalling processing the following payload, :\n" + payload);
		}

		return mailTemplate;


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
