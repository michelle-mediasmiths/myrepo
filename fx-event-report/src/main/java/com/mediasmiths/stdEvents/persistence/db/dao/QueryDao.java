package com.mediasmiths.stdEvents.persistence.db.dao;

import java.util.List;

import com.mediasmiths.stdEvents.events.db.entity.HibernateEventingMessage;

public interface QueryDao<T extends HibernateEventingMessage>
{
	public List<T> getAll();
}
