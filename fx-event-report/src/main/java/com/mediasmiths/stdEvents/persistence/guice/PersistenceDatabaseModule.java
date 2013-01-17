package com.mediasmiths.stdEvents.persistence.guice;

import org.hibernate.cfg.Configuration;

import com.mediasmiths.std.guice.hibernate.module.HibernateModule;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.db.entity.ContentPickup;
import com.mediasmiths.stdEvents.events.db.entity.Delivery;
import com.mediasmiths.stdEvents.events.db.entity.EventingEntity;
import com.mediasmiths.stdEvents.events.db.entity.IPEvent;
import com.mediasmiths.stdEvents.events.db.entity.LogEntity;
import com.mediasmiths.stdEvents.events.db.entity.PayloadEntity;
import com.mediasmiths.stdEvents.events.db.entity.PlaceholderMessage;
import com.mediasmiths.stdEvents.events.db.entity.QC;
import com.mediasmiths.stdEvents.events.db.entity.Request;
import com.mediasmiths.stdEvents.events.db.entity.Transcode;
import com.mediasmiths.stdEvents.events.db.entity.nagios.NagiosReportEntity;

public class PersistenceDatabaseModule extends HibernateModule
{

	@Override
	protected void configure(Configuration config)
	{
		config.addAnnotatedClass(EventEntity.class);
		config.addAnnotatedClass(PayloadEntity.class);
		config.addAnnotatedClass(LogEntity.class);
		config.addAnnotatedClass(NagiosReportEntity.class);

		config.addAnnotatedClass(PlaceholderMessage.class);
		config.addAnnotatedClass(ContentPickup.class);
		config.addAnnotatedClass(Transcode.class);
		config.addAnnotatedClass(QC.class);
		config.addAnnotatedClass(Delivery.class);
		config.addAnnotatedClass(EventingEntity.class);
		config.addAnnotatedClass(IPEvent.class);

		config.addAnnotatedClass(Request.class);
	}

}
