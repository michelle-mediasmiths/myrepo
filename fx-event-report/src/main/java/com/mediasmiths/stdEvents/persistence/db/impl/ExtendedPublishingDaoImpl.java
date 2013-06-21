package com.mediasmiths.stdEvents.persistence.db.impl;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.common.events.ExtendedPublishingTaskEvent;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ExtendedPublishing;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.persistence.db.dao.ExtendedPublishingDao;
import com.mediasmiths.stdEvents.persistence.db.dao.OrderDao;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

public class ExtendedPublishingDaoImpl extends HibernateDao<ExtendedPublishing, Long> implements ExtendedPublishingDao
{
	private final JAXBSerialiser serializer = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);
	private final static Logger log = Logger.getLogger(ExtendedPublishingDaoImpl.class);

	@Inject
	private OrderDao orderDao;


	public ExtendedPublishingDaoImpl()
	{
		super(ExtendedPublishing.class);
	}


	@Override
	@Transactional
	public void extendedPublishingEvent(final EventEntity event)
	{
		ExtendedPublishingTaskEvent evt = (ExtendedPublishingTaskEvent) serializer.deserialise(event.getPayload());

		final long taskID = evt.getTaskID();
		final String materialID = evt.getMaterialID();
		final String exportType = evt.getExportType();
		final String requestedBy = evt.getRequestedBy();
		final String taskStatus = evt.getTaskStatus();
		final XMLGregorianCalendar taskCreated = evt.getTaskCreated();
		final XMLGregorianCalendar taskUpdated = evt.getTaskUpdated();

		ExtendedPublishing ep = getById(taskID);

		if (ep == null)
		{
			log.info("New extended publishing task " + taskID);
			ep = new ExtendedPublishing();
			ep.setTaskID(taskID);
			ep.setMaterialID(materialID);

			final OrderStatus orderStatus = orderDao.getById(materialID);
			if (orderStatus == null)
			{
				log.info("no order status information for this material");
			}
			ep.setOrderStatus(orderStatus);
		}

		if (exportType != null)
		{
			ep.setExportType(exportType);
		}

		if (requestedBy != null)
		{
			ep.setRequestedBy(requestedBy);
		}

		if (taskStatus != null)
		{
			ep.setTaskStatus(taskStatus);
		}

		if (taskCreated != null)
		{
			ep.setTaskCreated(taskCreated.toGregorianCalendar().getTime());
		}

		if (taskUpdated != null)
		{
			ep.setTaskUpdated(taskUpdated.toGregorianCalendar().getTime());
		}

		save(ep);
	}


	@Override
	public List<ExtendedPublishing> getExtendedPublishingInDateRange(final DateTime start, final DateTime end)
	{
		Criteria criteria = createCriteria();

		criteria.add(Restrictions.or(Restrictions.between("taskCreated", start.toDate(), end.plusDays(1).toDate()),
		                             Restrictions.between("taskUpdated", start.toDate(), end.plusDays(1).toDate())));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		return getList(criteria);
	}
}
