package com.mediasmiths.stdEvents.persistence.guice;

import org.hibernate.cfg.Configuration;

import com.mediasmiths.std.guice.hibernate.module.HibernateModule;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.db.entity.EventingEntity;
import com.mediasmiths.stdEvents.events.db.entity.nagios.NagiosReportEntity;

public class PersistenceDatabaseModule extends HibernateModule
{

	@Override
	protected void configure(Configuration config)
	{
		config.addAnnotatedClass(EventEntity.class);

		config.addAnnotatedClass(NagiosReportEntity.class);

		config.addAnnotatedClass(EventingEntity.class);
	}

}
