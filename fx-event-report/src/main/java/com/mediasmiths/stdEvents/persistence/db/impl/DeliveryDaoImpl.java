package com.mediasmiths.stdEvents.persistence.db.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.events.db.entity.DeliveryDetails;
import com.mediasmiths.stdEvents.persistence.db.dao.QueryDao;

public class DeliveryDaoImpl extends HibernateDao<DeliveryDetails, Long> implements EventMarshaller, QueryDao<DeliveryDetails>
{
	public static final transient Logger logger = Logger.getLogger(DeliveryDaoImpl.class);
	
	public DeliveryDaoImpl()
	{
		super(DeliveryDetails.class);
	}

	@Transactional
	public void save(EventEntity event)
	{
		DeliveryDetails delivery = get(event);
		delivery.event = event;
		logger.info("Saving delivery");
		saveOrUpdate(delivery);
	}
	
	@Transactional
	public DeliveryDetails get(EventEntity event)
	{
		logger.info("Setting delivery for event");
		DeliveryDetails delivery = new DeliveryDetails();
		String str = event.getPayload();
		logger.trace(str);
		if (str.contains("MasterID>"))
			delivery.setMasterId(str.substring(str.indexOf("MasterID")+8, str.indexOf("</MasterID")));
		if (str.contains("Title>"))
			delivery.setTitle(str.substring(str.indexOf("Title")+6, str.indexOf("</Title")));
		if (str.contains("FileLocation>"))
			delivery.setFileLocation(str.substring(str.indexOf("FileLocation")+13, str.indexOf("</FileLocation")));
		if (str.contains("Delivered>")) {
			String delivered = str.substring(str.indexOf("Delivered")+10, str.indexOf("</Delivered"));
			if (delivered.equals("true"))
				delivery.setDelivered(true);
			else
				delivery.setDelivered(false);
		}
		return delivery;
	}
	
	@Transactional
	public List<DeliveryDetails> getAll()
	{
		logger.info("Getting all delivery");
		List<DeliveryDetails> delivery = super.getAll();
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
