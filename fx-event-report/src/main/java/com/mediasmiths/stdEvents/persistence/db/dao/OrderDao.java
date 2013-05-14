package com.mediasmiths.stdEvents.persistence.db.dao;

import java.util.List;

import org.joda.time.DateTime;

import com.google.inject.ImplementedBy;
import com.mediasmiths.std.guice.database.dao.Dao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;

@ImplementedBy(com.mediasmiths.stdEvents.persistence.db.impl.OrderDaoImpl.class)
public interface OrderDao extends Dao<OrderStatus, String>
{
	void addOrUpdateMaterial(EventEntity event);

	List<OrderStatus> getOrdersInDateRange(DateTime start, DateTime end);
}
