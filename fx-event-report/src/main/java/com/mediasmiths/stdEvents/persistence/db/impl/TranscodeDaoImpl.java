package com.mediasmiths.stdEvents.persistence.db.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.events.db.entity.Transcode;
import com.mediasmiths.stdEvents.persistence.db.dao.QueryDao;

public class TranscodeDaoImpl extends HibernateDao<Transcode, Long> implements EventMarshaller, QueryDao<Transcode>
{
	public static final transient Logger logger = Logger.getLogger(TranscodeDaoImpl.class);

	public TranscodeDaoImpl()
	{
		super(Transcode.class);
	}

	@Transactional
	public void save(EventEntity event)
	{
		logger.info("Setting tc passed notification...");
		Transcode notification = new Transcode();
		String noteStr = event.getPayload();
		logger.trace(noteStr);
		if (noteStr.contains("PackageID>"))
			notification.setPackageID(noteStr.substring(noteStr.indexOf("PackageID")+10, noteStr.indexOf("</PackageID")));
		
		logger.info("notification constructed");
		notification.event = event;
		saveOrUpdate(notification);
	}
	
	@Transactional
	public List<Transcode> getAll()
	{
		logger.info("Getting all transcode...");
		List<Transcode> notification = super.getAll();
		logger.info("Found all transcode");
		return notification;
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
