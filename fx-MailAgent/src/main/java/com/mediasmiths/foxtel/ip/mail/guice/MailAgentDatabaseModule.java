package com.mediasmiths.foxtel.ip.mail.guice;

import com.mediasmiths.foxtel.ip.mail.data.db.entity.EventTableEntity;
import com.mediasmiths.foxtel.ip.mail.data.db.entity.EventingTableEntity;
import com.mediasmiths.std.guice.hibernate.module.HibernateModule;
import org.hibernate.cfg.Configuration;

public class MailAgentDatabaseModule extends HibernateModule
{
	@Override
	protected void configure(Configuration config)
	{
		config.addAnnotatedClass(EventingTableEntity.class);
		config.addAnnotatedClass(EventTableEntity.class);

	}
}
