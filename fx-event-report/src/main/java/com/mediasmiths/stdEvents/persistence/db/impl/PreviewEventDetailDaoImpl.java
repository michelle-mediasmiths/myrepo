package com.mediasmiths.stdEvents.persistence.db.impl;

import org.apache.log4j.Logger;

import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.events.db.entity.PreviewEventDetail;

public class PreviewEventDetailDaoImpl extends HibernateDao<PreviewEventDetail, Long> implements EventMarshaller
{

	public static final transient Logger logger = Logger.getLogger(PreviewEventDetail.class);
	
	public PreviewEventDetailDaoImpl()
	{
		super(PreviewEventDetail.class);
	}

	@Transactional
	public void save(EventEntity event)
	{
		PreviewEventDetail preview = get(event);
		preview.event = event;
		logger.info("saving preview");
		saveOrUpdate(preview);
	}
	
	@Transactional
	public PreviewEventDetail get (EventEntity event)
	{
		PreviewEventDetail preview = new PreviewEventDetail();
		String str = event.getPayload();
		logger.trace(str);
		if (str.contains("MasterID>"))
			preview.setMasterId(str.substring(str.indexOf("MasterID")+9, str.indexOf("</MasterID")));
		if (str.contains("Title>"))
			preview.setTitle(str.substring(str.indexOf("Title")+6, str.indexOf("</Title")));
		if (str.contains("PreviewStatus>"))
			preview.setPreviewStatus(str.substring(str.indexOf("PreviewStatus")+14, str.indexOf("</PreviewStatus")));
		return preview;
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
