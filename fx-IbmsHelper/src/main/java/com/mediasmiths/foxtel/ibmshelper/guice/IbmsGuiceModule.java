package com.mediasmiths.foxtel.ibmshelper.guice;

import com.google.inject.AbstractModule;
import com.mediasmiths.foxtel.ibmshelper.data.dao.CreateUpdateTitleDao;
import com.mediasmiths.foxtel.ibmshelper.data.daoIpml.CreateUpdateTitleDaoImpl;

public class IbmsGuiceModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(CreateUpdateTitleDao.class).to(CreateUpdateTitleDaoImpl.class);

	}

}
