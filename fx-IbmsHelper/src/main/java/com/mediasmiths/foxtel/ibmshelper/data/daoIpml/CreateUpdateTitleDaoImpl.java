package com.mediasmiths.foxtel.ibmshelper.data.daoIpml;

import java.util.List;

import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.RightsType;
import au.com.foxtel.cf.mam.pms.TitleDescriptionType;

import com.mediasmiths.foxtel.ibmshelper.data.dao.CreateUpdateTitleDao;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;

public class CreateUpdateTitleDaoImpl extends HibernateDao<CreateOrUpdateTitle, Long> implements CreateUpdateTitleDao
{

	public CreateUpdateTitleDaoImpl(Class<CreateOrUpdateTitle> clazz)
	{
		super(clazz);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void delete(CreateOrUpdateTitle obj)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void saveOrUpdate(CreateOrUpdateTitle obj)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Long save(CreateOrUpdateTitle obj)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(CreateOrUpdateTitle obj)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public List<CreateOrUpdateTitle> getCreateTitle()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CreateOrUpdateTitle> getUpdateTitle()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TitleDescriptionType> getTitleInfo(Long versionID)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RightsType> getLicenseRights(Long versionID)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CreateOrUpdateTitle> getUpdatedTitle()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CreateOrUpdateTitle> getMaterial(Long versionId)
	{
		// TODO Auto-generated method stub
		return null;
	}

}