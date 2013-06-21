package com.mediasmiths.foxtel.ibmshelper.ibmsfunction;

import au.com.foxtel.cf.mam.pms.Actions;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import com.google.inject.Inject;
import com.mediasmiths.foxtel.ibmshelper.data.dao.CreateUpdateTitleDao;

import java.util.ArrayList;
import java.util.List;

public class CreateUpdateTitleFunction
{
	public final CreateUpdateTitleDao createUpdateTitleDao;

	@Inject
	public CreateUpdateTitleFunction(CreateUpdateTitleDao createUpdateTitleDao)
	{
		this.createUpdateTitleDao = createUpdateTitleDao;
	}

	public List<PlaceholderMessage> GetCreateUpdateTitle()
	{
		List<PlaceholderMessage> placeholderMessageList = new ArrayList<PlaceholderMessage>();
		PlaceholderMessage placeholderMessage = new PlaceholderMessage();
		Actions actions = new Actions();
		CreateOrUpdateTitle createOrUpdateTitleFINAl = new CreateOrUpdateTitle();

		List<CreateOrUpdateTitle> CreateOrUpdateTitleList = createUpdateTitleDao.getCreateTitle();

		for (CreateOrUpdateTitle createOrUpdateTitle : CreateOrUpdateTitleList)
		{

			String versionID;
			createOrUpdateTitleFINAl.setTitleID(createOrUpdateTitle.getTitleID());
			// TODO versionID??
			createOrUpdateTitleFINAl.setTitleDescription(createUpdateTitleDao.getTitleInfo(1278356163L).get(0));
			// TODO versionID??
			// TODO multiple channel logic for rights
			createOrUpdateTitleFINAl.setRights(createUpdateTitleDao.getLicenseRights(1278356163L).get(0));

			actions.getCreateOrUpdateTitleOrPurgeTitleOrAddOrUpdateMaterial().add(createOrUpdateTitleFINAl);
			placeholderMessage.setActions(actions);
			placeholderMessageList.add(placeholderMessage);
			// TODO remove items(s) from list so get(0) works?

			// TODO addorupdate material(version ID)

		}
		return placeholderMessageList;
	}
	
	//GetUpdateTitle
	//GetUpdatedTitle?
}
