package com.mediasmiths.stdEvents.persistence.db.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.events.db.entity.IPEvent;
import com.mediasmiths.stdEvents.persistence.db.dao.QueryDao;

public class IPEventDaoImpl extends HibernateDao<IPEvent, Long> implements EventMarshaller, QueryDao<IPEvent>
{
	public final static transient Logger logger = Logger.getLogger(IPEventDaoImpl.class);

	public IPEventDaoImpl()
	{
		super(IPEvent.class);
	}
	
	@Transactional
	public void save(EventEntity event)
	{
		IPEvent ip = get(event);
		ip.event = event;
		logger.info("Saving ipEvent");
		saveOrUpdate(ip);
	}
	
	@Transactional
	public List<IPEvent> getAll()
	{
		List<IPEvent> ips= super.getAll();
		return ips;
	}
	
	@Transactional
	public IPEvent get(EventEntity event)
	{
		IPEvent ip = new IPEvent();
		String str = event.getPayload();
		
		if (str.contains("PickUpKind>"))
			ip.setPickUpKind(str.substring(str.indexOf("PickUpKind") +11, str.indexOf("</PickUpKind")));
		if (str.contains("FilePath>"))
			ip.setFilePath(str.substring(str.indexOf("FilePath") +9, str.indexOf("</FilePath")));
		if (str.contains("WaitTime>"))
			ip.setWaitTime(str.substring(str.indexOf("WaitTime") +9, str.indexOf("</WaitTime")));
		if (str.contains("Source>"))
			ip.setSource(str.substring(str.indexOf("Source") +7, str.indexOf("</Source")));
		if (str.contains("Target>"))
			ip.setTarget(str.substring(str.indexOf("Target") +7, str.indexOf("</Target")));
		if (str.contains("FailureShortDesc>"))
			ip.setFailureShortDesc(str.substring(str.indexOf("FailureShortDesc") +17, str.indexOf("</FailureShortDesc")));
		if (str.contains("FailureLongDescription>"))
			ip.setFailureLongDescription(str.substring(str.indexOf("FailureLongDescription") +23, str.indexOf("</FailureLongDescription")));
	
		return ip;
	}

	@Transactional
	public String getNamespace(EventEntity event)
	{
		String namespace = event.getNamespace();
		return namespace;
	}
	
	@Transactional
	public String getInfo(EventEntity arg0)
	{
		return null;
	}
}
