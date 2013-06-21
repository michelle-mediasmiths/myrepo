package com.mediasmiths.foxtel.ibmshelper.data.dao;

import java.util.List;

import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.RightsType;
import au.com.foxtel.cf.mam.pms.TitleDescriptionType;

import com.mediasmiths.std.guice.database.dao.Dao;

public interface CreateUpdateTitleDao extends Dao<CreateOrUpdateTitle, Long>
{
	public List<CreateOrUpdateTitle> getCreateTitle();

	public List<CreateOrUpdateTitle> getUpdateTitle();

	public List<TitleDescriptionType> getTitleInfo(Long versionID);

	public List<RightsType> getLicenseRights(Long versionID);

	public List<CreateOrUpdateTitle> getUpdatedTitle();

	public List<CreateOrUpdateTitle> getMaterial(Long versionId);

}