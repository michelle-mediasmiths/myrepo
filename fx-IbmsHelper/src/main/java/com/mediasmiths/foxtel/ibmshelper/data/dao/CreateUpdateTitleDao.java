package com.mediasmiths.foxtel.ibmshelper.data.dao;

import java.util.List;

import com.mediasmiths.foxtel.ibmshelper.data.entity.createUpdateTitle.GetCreateTitleEntity;
import com.mediasmiths.foxtel.ibmshelper.data.entity.createUpdateTitle.GetLicenseRightsEntity;
import com.mediasmiths.foxtel.ibmshelper.data.entity.createUpdateTitle.GetMaterialEntity;
import com.mediasmiths.foxtel.ibmshelper.data.entity.createUpdateTitle.GetTitleInfoEntity;
import com.mediasmiths.foxtel.ibmshelper.data.entity.createUpdateTitle.GetUpdateTitleEntity;
import com.mediasmiths.foxtel.ibmshelper.data.entity.createUpdateTitle.GetUpdatedTitleEntity;
import com.mediasmiths.std.guice.database.dao.Dao;

//TODO Change extend section?
public interface CreateUpdateTitleDao extends Dao<String, Long>
{
	public List<GetCreateTitleEntity> getCreateTitle();

	public List<GetUpdateTitleEntity> getUpdateTitle();

	public List<GetTitleInfoEntity> getTitleInfo(Long versionID);

	public List<GetLicenseRightsEntity> getLicenseRights(Long versionID);

	public List<GetUpdatedTitleEntity> getUpdatedTitle();

	public List<GetMaterialEntity> getMaterial(Long versionId);

}