package com.mediasmiths.foxtel.ibmshelper.data.daoIpml;

import java.util.List;

import com.mediasmiths.foxtel.ibmshelper.data.dao.CreateUpdateTitleDao;
import com.mediasmiths.foxtel.ibmshelper.data.entity.createUpdateTitle.GetCreateTitleEntity;
import com.mediasmiths.foxtel.ibmshelper.data.entity.createUpdateTitle.GetLicenseRightsEntity;
import com.mediasmiths.foxtel.ibmshelper.data.entity.createUpdateTitle.GetTitleInfoEntity;
import com.mediasmiths.foxtel.ibmshelper.data.entity.createUpdateTitle.GetUpdateTitleEntity;
import com.mediasmiths.foxtel.ibmshelper.data.entity.createUpdateTitle.GetUpdatedTitleEntity;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;

public class CreateUpdateTitleDaoImpl extends HibernateDao<String, Long> implements CreateUpdateTitleDao
{

	public CreateUpdateTitleDaoImpl(Class<String> clazz)
	{
		super(clazz);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Class<String> getEntityType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAll()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getById(Long id)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteById(Long id)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(String obj)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void saveOrUpdate(String obj)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Long save(String obj)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(String obj)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public List<GetCreateTitleEntity> getCreateTitle()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GetUpdateTitleEntity> getUpdateTitle()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GetTitleInfoEntity> getTitleInfo(Long versionID)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GetLicenseRightsEntity> getLicenseRights(Long versionID)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GetUpdatedTitleEntity> getUpdatedTitle()
	{
		// TODO Auto-generated method stub
		return null;
	}

}