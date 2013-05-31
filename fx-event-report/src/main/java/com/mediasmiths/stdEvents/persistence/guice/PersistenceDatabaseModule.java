package com.mediasmiths.stdEvents.persistence.guice;

import org.hibernate.cfg.Configuration;

import com.mediasmiths.std.guice.hibernate.module.HibernateModule;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AggregatedBMS;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AutoQC;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Title;
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
		
		config.addAnnotatedClass(AggregatedBMS.class);
		
		config.addAnnotatedClass(OrderStatus.class);
		
		config.addAnnotatedClass(Title.class);
		
		config.addAnnotatedClass(AutoQC.class);
	}

}
