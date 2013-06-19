package com.mediasmiths.stdEvents.persistence.db.impl;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.common.events.PurgeEventNotification;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Purge;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Title;
import com.mediasmiths.stdEvents.persistence.db.dao.OrderDao;
import com.mediasmiths.stdEvents.persistence.db.dao.PurgeDao;
import com.mediasmiths.stdEvents.persistence.db.dao.TitleDao;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.List;

public class PurgeDaoImpl extends HibernateDao<Purge, String> implements PurgeDao
{

	private final JAXBSerialiser serializer = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);
	private final static Logger log = Logger.getLogger(PurgeDaoImpl.class);

	@Inject
	private TitleDao titleDao;

	@Inject
	private OrderDao orderDao;


	public PurgeDaoImpl()
	{
		super(Purge.class);
	}


	@Override
	public void purgeMessage(final EventEntity event)
	{
		PurgeEventNotification pe = (PurgeEventNotification) serializer.deserialise(event.getPayload());

		final String assetType = pe.getAssetType();
		final String materialID = pe.getMaterialId();
		final String titleID = pe.getTitleId();
		final String houseID = pe.getHouseId();
		final Boolean purged = pe.isPurged();
		final Boolean isProtected = pe.isProtected();
		final Boolean extended = pe.isExtended();
		final Boolean hasPurgeCandidateTask = pe.isHasPurgeCandidateTask();
		final XMLGregorianCalendar purgeCandidateCreated = pe.getPurgeCandidateCreated();
		final XMLGregorianCalendar purgeCandidateUpdated = pe.getPurgeCandidateUpdated();
		final XMLGregorianCalendar expires = pe.getExpires();

		log.debug(String.format("PurgetEventNotification Asset Type %s MaterialID %s titleID %s houseId %s",
		                        assetType,
		                        materialID,
		                        titleID,
		                        houseID));

		if (houseID == null)
		{
			log.error("cannot use a PurgetEventNotification that has a null house id, ignoring");
			return;
		}

		Purge p = getById(houseID);

		if (p == null)
		{
			p = initialiseNewPurgeEntity(materialID, titleID, houseID, event);
		}

		p.setAssetType(assetType);

		updatePurgeEntityIfPurged(event, purged, p);
		updatePurgeEntityIfExtended(event, extended, p);
		updatePurgeEntityIfProtected(event, isProtected, p);
		updatePurgeEntityIfHasPurgeCandidateTask(hasPurgeCandidateTask, p);
		updatePurgeEntityWithPurgeCandidateDates(purgeCandidateCreated, purgeCandidateUpdated, expires, p);

		save(p);
	}


	private void updatePurgeEntityIfExtended(final EventEntity event, final Boolean extended, final Purge p)
	{
		if (extended != null)
		{
			if (!p.getExtended() && extended)
			{
				log.info("Assets purge candidate task has been extended");
				p.setExtended(Boolean.TRUE);
				p.setDateExtended(new Date(event.getTime()));
			}
		}
	}


	private void updatePurgeEntityWithPurgeCandidateDates(final XMLGregorianCalendar purgeCandidateCreated,
	                                                      final XMLGregorianCalendar purgeCandidateUpdated,
	                                                      final XMLGregorianCalendar expires,
	                                                      final Purge p)
	{
		if (purgeCandidateCreated != null)
		{
			p.setDatePurgeCandidateTaskCreated(purgeCandidateCreated.toGregorianCalendar().getTime());
		}

		if (purgeCandidateUpdated != null)
		{
			p.setDatePurgeCandidateTaskUpdated(purgeCandidateUpdated.toGregorianCalendar().getTime());
		}

		if (expires != null)
		{
			p.setDateExpires(expires.toGregorianCalendar().getTime());
		}
	}


	private void updatePurgeEntityIfHasPurgeCandidateTask(final Boolean hasPurgeCandidateTask, final Purge p)
	{
		if (hasPurgeCandidateTask != null)
		{
			if (hasPurgeCandidateTask)
			{
				log.debug("Asset has a purge candidate task");
				p.setHasPurgeCandidateTask(Boolean.TRUE);
			}
		}
	}


	private void updatePurgeEntityIfProtected(final EventEntity event, final Boolean isProtected, final Purge p)
	{
		if (isProtected != null)
		{
			if (!p.getPurged() && isProtected)
			{
				log.info("Asset has been protected");
				p.setProtected(Boolean.TRUE);
				p.setEverProtected(Boolean.TRUE);
				p.setDateProtected(new Date(event.getTime()));
			}
			else if (p.getPurged() && !isProtected)
			{
				log.info("Asset has been unprotected");
				p.setProtected(Boolean.FALSE);
				p.setDateUnProtected(new Date(event.getTime()));
			}
		}
	}


	private void updatePurgeEntityIfPurged(final EventEntity event, final Boolean purged, final Purge p)
	{
		if (purged != null)
		{
			if (!p.getPurged() && purged)
			{
				log.info("Asset has been purged");
				p.setPurged(Boolean.TRUE);
				p.setDatePurged(new Date(event.getTime()));
			}
		}
	}


	private Purge initialiseNewPurgeEntity(final String materialID,
	                                       final String titleID,
	                                       final String houseID,
	                                       final EventEntity event)
	{
		Purge p;
		log.info(String.format("New purge entry for asset %s", houseID));
		p = new Purge();
		p.setHouseID(houseID);

		if (titleID != null)
		{
			log.debug("title id present");
			final Title title = titleDao.getById(titleID);
			p.setTitle(title);
		}
		else if (materialID != null)
		{
			final OrderStatus order = orderDao.getById(materialID);
			if (order != null)
			{
				final Title title = order.getTitle();
				p.setTitle(title);
			}
			else
			{
				log.warn("No orderStatus found for material " + materialID);
			}
		}
		else
		{
			log.warn("TitleId and materialID both null for house ID " + houseID);
		}

		if (p.getTitle() == null)
		{
			log.warn("No title information for house ID " + houseID);
		}

		p.setDateEntityCreated(new Date(event.getTime()));

		return p;
	}


	@Override
	public List<Purge> getPurgeInDateRange(final DateTime start, final DateTime end)
	{
		Criteria criteria = createCriteria();

		criteria.add(Restrictions.or(Restrictions.between("dateEntityCreated", start.toDate(), end.plusDays(1).toDate()),
		                             Restrictions.between("dateProtected", start.toDate(), end.plusDays(1).toDate()),
		                             Restrictions.between("dateUnProtected", start.toDate(), end.plusDays(1).toDate()),
		                             Restrictions.between("datePurgeCandidateTaskCreated",
		                                                  start.toDate(),
		                                                  end.plusDays(1).toDate()),
		                             Restrictions.between("datePurgeCandidateTaskUpdated",
		                                                  start.toDate(),
		                                                  end.plusDays(1).toDate()),
		                             Restrictions.between("dateExtended", start.toDate(), end.plusDays(1).toDate()),
		                             Restrictions.between("datePurged", start.toDate(), end.plusDays(1).toDate()),
		                             Restrictions.between("dateExpires", start.toDate(), end.plusDays(1).toDate())));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		return getList(criteria);
	}
}
