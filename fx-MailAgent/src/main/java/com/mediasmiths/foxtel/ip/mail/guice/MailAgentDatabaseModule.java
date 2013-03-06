package com.mediasmiths.foxtel.ip.mail.guice;

import org.hibernate.cfg.Configuration;

import com.mediasmiths.foxtel.ip.mail.data.db.entity.EventTableEntity;
import com.mediasmiths.foxtel.ip.mail.data.db.entity.EventingTableEntity;
import com.mediasmiths.std.guice.hibernate.module.HibernateModule;

public class MailAgentDatabaseModule extends HibernateModule
{
	@Override
	protected void configure(Configuration config)
	{
		config.addAnnotatedClass(EventingTableEntity.class);
		config.addAnnotatedClass(EventTableEntity.class);

	}
}
