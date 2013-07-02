package com.mediasmiths.stdEvents.persistence.guice;

import com.mediasmiths.std.guice.hibernate.module.HibernateModule;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AutoQC;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ComplianceLogging;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ExtendedPublishing;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ManualQAEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Purge;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Title;
import com.mediasmiths.stdEvents.coreEntity.db.entity.TranscodeJob;
import com.mediasmiths.stdEvents.events.db.entity.EventingEntity;
import com.mediasmiths.stdEvents.events.db.entity.nagios.NagiosReportEntity;
import org.hibernate.cfg.Configuration;

public class PersistenceDatabaseModule extends HibernateModule
{

	@Override
	protected void configure(Configuration config)
	{
		config.addAnnotatedClass(EventEntity.class);

		config.addAnnotatedClass(NagiosReportEntity.class);

		config.addAnnotatedClass(EventingEntity.class);
		
		config.addAnnotatedClass(OrderStatus.class);
		
		config.addAnnotatedClass(Title.class);
		
		config.addAnnotatedClass(AutoQC.class);

		config.addAnnotatedClass(ManualQAEntity.class);

		config.addAnnotatedClass(Purge.class);

		config.addAnnotatedClass(ExtendedPublishing.class);

		config.addAnnotatedClass(ComplianceLogging.class);

		config.addAnnotatedClass(TranscodeJob.class);

	}

}
