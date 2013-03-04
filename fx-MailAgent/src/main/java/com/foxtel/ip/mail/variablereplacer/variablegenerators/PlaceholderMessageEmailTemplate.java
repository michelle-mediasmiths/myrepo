package com.foxtel.ip.mail.variablereplacer.variablegenerators;


import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import com.foxtel.ip.mail.variablereplacer.EmailTemplateGenerator;
import com.mediasmiths.foxtel.ip.common.events.MailTemplate;

public class PlaceholderMessageEmailTemplate implements EmailTemplateGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(PlaceholderMessage.class);
	}

	@Override
	public void customiseTemplate(MailTemplate template, Object obj, String comment)
	{
		customise(template, (PlaceholderMessage) obj, comment);
	}

	void customise(MailTemplate template, PlaceholderMessage obj, String comment)
	{

		String materialID = "Could not find materialID, messageID used " + obj.getMessageID();

		Object Action = obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		if (Action instanceof DeleteMaterial)
		{
			DeleteMaterial deleteMaterial = new DeleteMaterial();
			deleteMaterial = (DeleteMaterial) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			materialID = deleteMaterial.getMaterial().getMaterialID();
			template.setSubject(template.getSubject() + "MaterialID" + materialID);
		}
		else if (Action instanceof CreateOrUpdateTitle)
		{
			CreateOrUpdateTitle temp = new CreateOrUpdateTitle();
			temp = (CreateOrUpdateTitle) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			materialID = "MasterID not found, TitleID used:" + temp.getTitleID() + ", ";
			template.setSubject(template.getSubject() + materialID);
		}
		else if (Action instanceof PurgeTitle)
		{
			PurgeTitle temp = new PurgeTitle();
			temp = (PurgeTitle) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			materialID = "MasterID not found, TitleID used:" + temp.getTitleID() + ", ";
			template.setSubject(template.getSubject() + materialID);
		}

		else if (Action instanceof AddOrUpdateMaterial)
		{
			AddOrUpdateMaterial temp = new AddOrUpdateMaterial();
			temp = (AddOrUpdateMaterial) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			materialID = temp.getMaterial().getMaterialID();
			template.setSubject(template.getSubject() + materialID);
		}

		else if (Action instanceof AddOrUpdatePackage)
		{
			AddOrUpdatePackage temp = new AddOrUpdatePackage();
			temp = (AddOrUpdatePackage) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			materialID = "MasterID not found, TitleID used:" + temp.getTitleID() + ", ";
			template.setSubject(template.getSubject() + materialID);
		}

		else if (Action instanceof DeletePackage)
		{
			DeletePackage temp = new DeletePackage();
			temp = (DeletePackage) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			materialID = "MasterID not found, TitleID used:" + temp.getTitleID() + ", ";
			template.setSubject(template.getSubject() + materialID);
		}
	}

}
