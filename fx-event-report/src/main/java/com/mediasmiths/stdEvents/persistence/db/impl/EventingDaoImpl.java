package com.mediasmiths.stdEvents.persistence.db.impl;

import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.stdEvents.events.db.entity.EventingEntity;
import com.mediasmiths.stdEvents.persistence.db.dao.EventingDao;

public class EventingDaoImpl extends HibernateDao<EventingEntity, Long> implements EventingDao
{
}
