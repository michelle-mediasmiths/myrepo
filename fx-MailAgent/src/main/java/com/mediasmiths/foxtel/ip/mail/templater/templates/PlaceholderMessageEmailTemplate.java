package com.mediasmiths.foxtel.ip.mail.templater.templates;


import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.email.MailTemplate;

public class PlaceholderMessageEmailTemplate extends MailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(PlaceholderMessage.class);
	}

	@Override
	public MailTemplate customiseTemplate(Object obj, String comment)
	{
		return customise((PlaceholderMessage) obj, comment);
	}

	MailTemplate customise(PlaceholderMessage obj, String comment)
	{
        MailTemplate m = new MailTemplate();

		m.setEmailaddresses(getEmailaddresses());
		m.setBody(getBody());

		Object Action = obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		if (Action instanceof DeleteMaterial)
		{
			DeleteMaterial deleteMaterial;
			deleteMaterial = (DeleteMaterial) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			m.setSubject(String.format(getSubject(), deleteMaterial.getMaterial().getMaterialID()));
		}
		else if (Action instanceof CreateOrUpdateTitle)
		{
			CreateOrUpdateTitle temp;
			temp = (CreateOrUpdateTitle) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			m.setSubject(String.format(getSubject(), "TitleID:" + temp.getTitleID()));
		}
		else if (Action instanceof PurgeTitle)
		{
			PurgeTitle temp = new PurgeTitle();
			temp = (PurgeTitle) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			m.setSubject(String.format(getSubject(), "TitleID:" + temp.getTitleID()));
		}

		else if (Action instanceof AddOrUpdateMaterial)
		{
			AddOrUpdateMaterial temp;
			temp = (AddOrUpdateMaterial) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			m.setSubject(String.format(getSubject(), temp.getMaterial().getMaterialID()));
		}

		else if (Action instanceof AddOrUpdatePackage)
		{
			AddOrUpdatePackage temp;
			temp = (AddOrUpdatePackage) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			m.setSubject(String.format(getSubject(),"TitleID:" + temp.getTitleID()));
		}

		else if (Action instanceof DeletePackage)
		{
			DeletePackage temp;
			temp = (DeletePackage) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			m.setSubject(String.format(getSubject(), "TitleID:" + temp.getTitleID()));
		}
		else
		{
			m.setSubject(getSubject());
		}
		return m;
	}

}
