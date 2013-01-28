package com.mediasmiths.stdEvents.persistence.db.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.events.db.entity.placeholder.ManualPurge;

public class ManualPurgeDaoImpl extends HibernateDao<ManualPurge, Long> implements EventMarshaller
{
	public static final transient Logger logger = Logger.getLogger(ManualPurgeDaoImpl.class);
	public ManualPurgeDaoImpl()
	{
		super(ManualPurge.class);
	}

	@Transactional
	public void save(EventEntity event)
	{
		ManualPurge purge = get(event);
		purge.event = event;
		logger.info("saving manual purge");
		saveOrUpdate(purge);
	}

	@Transactional
	public List<ManualPurge> getAll()
	{
		logger.info("Getting all manual purge");
		List<ManualPurge> purge = super.getAll();
		return purge;
	}

	@Transactional
	public ManualPurge get(EventEntity event)
	{
		logger.info("Setting manual purge for event");
		ManualPurge purge = new ManualPurge();
		String str = event.getPayload();

		if (str.contains("TitleID"))
			purge.setTitleID(str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9))));
		if (str.contains("MasterID"))
			purge.setMasterID(str.substring(str.indexOf("MasterID")+9, str.indexOf("</MasterID")));
		if (str.contains("PurgeStatus"))
			purge.setPurgeStatus(str.substring(str.indexOf("PurgeStatus")+12, str.indexOf("</PurgeStatus")));
		if (str.contains("ProtectedStatus"))
			purge.setProtectedStatus(str.substring(str.indexOf("ProtectedStatus")+16, str.indexOf("</ProtectedStatus")));
		if (str.contains("ExtendedStatus"))
			purge.setExtendedStatus(str.substring(str.indexOf("ExtendedStatus")+15, str.indexOf("</ExtendedStatus")));
		if (str.contains("ExpireDate"))
			purge.setExpireDate(str.substring(str.indexOf("ExpireDate")+11, str.indexOf("</ExpireDate")));
		if (str.contains("DeletedIn"))
			purge.setDeletedIn(str.substring(str.indexOf("DeletedIn")+10, str.indexOf("</DeletedIn")));
		logger.info("Manual purge type constructed");
		return purge;
	}

	@Override
	public String getNamespace(EventEntity event)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInfo(EventEntity event)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
