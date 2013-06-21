package com.mediasmiths.mayam.guice;

import com.mediasmiths.mayam.accessrights.MayamAccessRights;
import com.mediasmiths.std.guice.hibernate.module.HibernateModule;
import org.hibernate.cfg.Configuration;

public class SecurityModule extends HibernateModule
{

	@Override
	protected void configure(Configuration config)
	{
		config.addAnnotatedClass(MayamAccessRights.class);
	}

}
