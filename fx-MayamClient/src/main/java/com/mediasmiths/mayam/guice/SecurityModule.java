package com.mediasmiths.mayam.guice;

import org.hibernate.cfg.Configuration;

import com.mediasmiths.mayam.accessrights.MayamAccessRights;
import com.mediasmiths.std.guice.hibernate.module.HibernateModule;

public class SecurityModule extends HibernateModule
{

	@Override
	protected void configure(Configuration config)
	{
		config.addAnnotatedClass(MayamAccessRights.class);
	}

}
