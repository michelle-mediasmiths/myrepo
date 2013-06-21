package com.mediasmiths.stdEvents.persistence.db.dao;

import com.mediasmiths.stdEvents.events.db.entity.HibernateEventingMessage;

import java.util.List;

public interface QueryDao<T extends HibernateEventingMessage>
{
	public List<T> getAll();
}
