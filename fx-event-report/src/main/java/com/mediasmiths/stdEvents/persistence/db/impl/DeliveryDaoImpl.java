package com.mediasmiths.stdEvents.persistence.db.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.events.db.entity.Delivery;
import com.mediasmiths.stdEvents.persistence.db.dao.QueryDao;

public class DeliveryDaoImpl extends HibernateDao<Delivery, Long> implements EventMarshaller, QueryDao<Delivery>
{
	public static final transient Logger logger = Logger.getLogger(DeliveryDaoImpl.class);
	
	public DeliveryDaoImpl()
	{
		super(Delivery.class);
	}

	@Transactional
	public void save(EventEntity event)
	{
		Delivery delivery = get(event);
		delivery.event = event;
		logger.info("Saving delivery");
		saveOrUpdate(delivery);
	}
	
	@Transactional
	public Delivery get(EventEntity event)
	{
		logger.info("Setting delivery for event");
		Delivery delivery = new Delivery();
		String deliveryString = event.getPayload();
		logger.info(deliveryString);
		if (deliveryString.contains("TitleID"))
			delivery.setTitleID(deliveryString.substring(deliveryString.indexOf("TitleID") +8, deliveryString.indexOf("</TitleID")));
		return delivery;
	}
	
	@Transactional
	public List<Delivery> getAll()
	{
		logger.info("Getting all delivery");
		List<Delivery> delivery = super.getAll();
		logger.info("Found all delivery");
		return delivery;
	}
	
	@Override
	public String getNamespace(EventEntity event)
	{
		String namespace = event.getNamespace();
		return namespace;
	}

	@Override
	public String getInfo(EventEntity event)
	{
		String namespace = event.getNamespace().substring(25);
		String info = "Translating from EventEntity to " + namespace;
		return info;
	}
	
	
}
