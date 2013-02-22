package com.foxtel.ip.mail.variablereplacer.variablegenerators;


import au.com.foxtel.cf.mam.pms.AddOrUpdateMaterial;
import au.com.foxtel.cf.mam.pms.AddOrUpdatePackage;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;

public class PlaceholderMessageEmailVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(PlaceholderMessage.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{
		return getVariableReplacer((PlaceholderMessage) obj, comment);
	}

	EmailVariables getVariableReplacer(PlaceholderMessage obj, String comment)
	{

		String materialID = "Could not find materialID, messageID used " + obj.getMessageID();

		Object Action = obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
		if (Action instanceof DeleteMaterial)
		{
			DeleteMaterial temp = new DeleteMaterial();
			temp = (DeleteMaterial) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			materialID = temp.getMaterial().getMaterialID();
		}
		else if (Action instanceof CreateOrUpdateTitle)
		{
			CreateOrUpdateTitle temp = new CreateOrUpdateTitle();
			temp = (CreateOrUpdateTitle) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			materialID = "MasterID not found, TitleID used:" + temp.getTitleID() + ", ";
		}
		else if (Action instanceof PurgeTitle)
		{
			PurgeTitle temp = new PurgeTitle();
			temp = (PurgeTitle) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			materialID = "MasterID not found, TitleID used:" + temp.getTitleID() + ", ";
		}

		else if (Action instanceof AddOrUpdateMaterial)
		{
			AddOrUpdateMaterial temp = new AddOrUpdateMaterial();
			temp = (AddOrUpdateMaterial) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			materialID = temp.getMaterial().getMaterialID();
		}

		else if (Action instanceof AddOrUpdatePackage)
		{
			AddOrUpdatePackage temp = new AddOrUpdatePackage();
			temp = (AddOrUpdatePackage) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			materialID = "MasterID not found, TitleID used:" + temp.getTitleID() + ", ";
		}

		else if (Action instanceof DeletePackage)
		{
			DeletePackage temp = new DeletePackage();
			temp = (DeletePackage) obj.getActions().getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().get(0);
			materialID = "MasterID not found, TitleID used:" + temp.getTitleID() + ", ";
		}

		EmailVariables replacer = new EmailVariables();

		replacer.setVariable("MasterID", materialID);
		replacer.setVariable("TitleField", "Mediasmiths placeholder: placeholder");
		if (comment != null)
		{
			replacer.setVariable("XX", comment);
		}
		else
		{
			replacer.setVariable("XX", "Error comment field was null! Please check event table");

		}
		return replacer;
	}

}
