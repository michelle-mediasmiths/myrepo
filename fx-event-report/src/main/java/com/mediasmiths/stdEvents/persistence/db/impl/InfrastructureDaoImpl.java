package com.mediasmiths.stdEvents.persistence.db.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.events.db.entity.Infrastructure;
import com.mediasmiths.stdEvents.persistence.db.dao.QueryDao;

public class InfrastructureDaoImpl extends HibernateDao<Infrastructure, Long> implements EventMarshaller
{
	public static final transient Logger logger = Logger.getLogger(InfrastructureDaoImpl.class);

	public InfrastructureDaoImpl()
	{
		super(Infrastructure.class);
	}

	@Transactional
	public void save(EventEntity event)
	{
		Infrastructure infrastructure = get(event);
		infrastructure.event = event;
		logger.info("saving infrastructure");
		saveOrUpdate(infrastructure);
	}

	@Transactional
	public Infrastructure get(EventEntity event)
	{
		logger.info("Setting Infrastructure for event");
		Infrastructure inf = new Infrastructure();
		String str = event.getPayload();
		if (str.contains("FileSystemReference>")) {
			inf.setFileSystemReference(str.substring(str.indexOf("FileSystemReference")+20, str.indexOf("</FileSystemReference")));
		}
		if (str.contains("MegaBytesUsed>")) {
			inf.setMegaBytesUsed(str.substring(str.indexOf("MegaBytesUsed")+13, str.indexOf("</MegaBytesUsed")));
		}
		if (str.contains("MegaBytesFree>")) {
			inf.setMegaBytesFree(str.substring(str.indexOf("MegaBytesFree")+14, str.indexOf("</MegaBytesFree")));
		}
		return inf;
	}

	@Transactional
	public List<Infrastructure> getAll() 
	{
		logger.info("Getting all infrastructure");
		List<Infrastructure> infrastructure = super.getAll();
		logger.info("found all infrastructure");
		return infrastructure;
	}

	@Override
	public String getNamespace(EventEntity event)
	{
		return event.getNamespace();
	}

	@Override
	public String getInfo(EventEntity event)
	{
		return event.getNamespace();
	}
}
