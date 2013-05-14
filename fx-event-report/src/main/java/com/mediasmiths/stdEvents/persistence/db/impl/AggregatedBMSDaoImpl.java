package com.mediasmiths.stdEvents.persistence.db.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import com.mediasmiths.foxtel.ip.common.events.AddOrUpdateMaterial;
import com.mediasmiths.foxtel.ip.common.events.AddOrUpdatePackage;
import com.mediasmiths.foxtel.ip.common.events.CreateOrUpdateTitle;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AggregatedBMS;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.persistence.db.dao.AggregatedBMSDao;

public class AggregatedBMSDaoImpl extends HibernateDao<AggregatedBMS, Long> implements AggregatedBMSDao
{
	private static final transient Logger logger = Logger.getLogger(AggregatedBMSDaoImpl.class);
	
	public AggregatedBMSDaoImpl()
	{
		super(AggregatedBMS.class);
	}

	@Transactional
	public List<AggregatedBMS> findByAnId(String column, String id)
	{
		logger.info("Finding BMS...");
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq(column, id));
		return getList(criteria);
	}
	
	@Transactional
	public List<AggregatedBMS> completionDateNotNull()
	{
		logger.debug(">>>completionDateNotNull");
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.isNotNull("completionDate"));
		logger.debug("<<<completionDateNotNull");
		return getList(criteria);
	}
	
	@Transactional 
	public List<AggregatedBMS> withinDate(DateTime start, DateTime end){
		
		Criteria criteria = createCriteria();
		Long startL = start.getMillis();
		Long endL = end.getMillis();
		
		criteria.add(Restrictions.ge("time", startL));
		criteria.add(Restrictions.lt("time", endL));
		
		return getList(criteria);
	}

	@Transactional
	public Object unmarshall(String payload)
	{
		Object bms = null;
		logger.info("Unmarshalling payload");
		try {
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);
			bms = JAXB_SERIALISER.deserialise(payload);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bms;
	}

	@Transactional
	public void updateBMS(EventEntity event)
	{
		logger.info("updateBMS() eventName: " + event.getEventName());

		if (event.getEventName().equals("CreateorUpdateTitle")) {
			CreateOrUpdateTitle title = (CreateOrUpdateTitle) unmarshall(event.getPayload());
			AggregatedBMS bms = new AggregatedBMS();
			bms.setTime(event.getTime());
			bms.setTitleID(title.getTitleID());
			bms.setTitle(title.getTitle());
			bms.setChannels(title.getChannels());
			save(bms);
			logger.info("SAVED" + bms.getTitleID() + " " + bms.getTitle());
		}

		if (event.getEventName().equals("AddOrUpdatePackage")) {
			AddOrUpdatePackage pack = (AddOrUpdatePackage) unmarshall(event.getPayload());
			List<AggregatedBMS> titles = findByAnId("titleID", pack.getTitleID());
			for (AggregatedBMS title : titles)
			{
				if (title.getPackageID() == null) {
					title.setPackageID(pack.getPackageID());
					title.setMaterialID(pack.getMaterialID());
					if (pack.getRequiredBy() != null)
						title.setRequiredBy(pack.getRequiredBy().toString());
					saveOrUpdate(title);
					logger.info("SAVED: " + title.getPackageID() + " " + title.getMaterialID() + " " + title.getRequiredBy());
				} else {
					AggregatedBMS bms = new AggregatedBMS();
					bms.setTitle(title.getTitle());
					bms.setTitleID(title.getTitleID());
					bms.setChannels(title.getChannels());
					bms.setPackageID(pack.getPackageID());
					bms.setMaterialID(pack.getMaterialID());
					if (pack.getRequiredBy() != null)
						bms.setRequiredBy(pack.getRequiredBy().toString());
					save(bms);
					logger.info("SAVED: " + title.getPackageID() + " " + title.getMaterialID() + " " + title.getRequiredBy());
				}
			}
		}

		if (event.getEventName().equals("AddOrUpdateMaterial")) {
			AddOrUpdateMaterial material = (AddOrUpdateMaterial) unmarshall(event.getPayload());
			List<AggregatedBMS> packages = findByAnId("materialID", material.getMaterialID());
			for (AggregatedBMS pack : packages) {
				if (pack.getCompletionDate() == null) {
					logger.info("new material");
					if (material.getCompletionDate() != null)
						pack.setCompletionDate(material.getCompletionDate().toString());
					pack.setAggregatorID(material.getAggregatorID());
					saveOrUpdate(pack);
					logger.info("SAVED: " + pack.getCompletionDate() + " " + pack.getAggregatorID());
				} else {
					AggregatedBMS bms = new AggregatedBMS();
					bms.setTitle(pack.getTitle());
					bms.setTitleID(pack.getTitleID());
					bms.setChannels(pack.getChannels());
					bms.setPackageID(pack.getPackageID());
					bms.setMaterialID(pack.getMaterialID());
					if (pack.getRequiredBy() != null)
						bms.setRequiredBy(pack.getRequiredBy().toString());
					bms.setAggregatorID(material.getAggregatorID());
					if (pack.getCompletionDate() != null)
						bms.setCompletionDate(material.getCompletionDate().toString());
					save(bms);
					logger.info("SAVED: " + bms.getCompletionDate() + " " + bms.getAggregatorID());
				}
			}
		}
	}
}
