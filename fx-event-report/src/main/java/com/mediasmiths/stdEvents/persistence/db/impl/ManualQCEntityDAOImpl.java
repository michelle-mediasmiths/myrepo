package com.mediasmiths.stdEvents.persistence.db.impl;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.common.events.ManualQANotification;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ManualQAEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.persistence.db.dao.ManualQAEntityDAO;
import com.mediasmiths.stdEvents.persistence.db.dao.OrderDao;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

public class ManualQCEntityDAOImpl extends HibernateDao<ManualQAEntity, String> implements ManualQAEntityDAO
{

	private final JAXBSerialiser serialiser = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);
	private final static Logger log = Logger.getLogger(ManualQCEntityDAOImpl.class);

	@Inject
	private OrderDao orderDao;


	public ManualQCEntityDAOImpl()
	{
		super(ManualQAEntity.class);
	}


	@Override
	@Transactional
	public void manualQCMessage(final EventEntity event)
	{
		ManualQANotification mqa = (ManualQANotification) serialiser.deserialise(event.getPayload());

		final String materialID = mqa.getMaterialID();
		final String operator = mqa.getOperator();
		final String taskStatus = mqa.getTaskStatus();
		final String previewStatus = mqa.getPreviewStatus();
		//note: ManualQANotification is Stringly typed, hrPreview ought to be a boolean but it stays a string until old preview data is imported, then it can be changed to a boolean
		final String hrPreview = mqa.getHrPreview();
		final String userWhoRequestedHrPreview = mqa.getHrPreviewRequested();
		//note: ManualQANotification is Stringly typed, isEscalated ought to be an integer but it stays a string until old preview data is imported, then it can be changed to a integer
		final String escalation = mqa.getEscalated();
		//note: ManualQANotification is Stringly typed, reordered ought to be a boolean but it stays a string until old preview data is imported, then it can be changed to a boolean
		final String reordered = mqa.getReordered();

		Date eventTime = new Date(event.getTime());

		if (materialID == null)
		{
			log.error("cannot use a manual qa notification that has a null material id, ignoring");
			return;
		}

		ManualQAEntity m = getById(materialID);

		if (m == null)
		{
			log.debug(String.format("New manual qa entry for material %s", materialID));
			m = new ManualQAEntity();
			m.setMaterialid(materialID);
			m.setTaskCreated(eventTime);

			OrderStatus orderStatus = orderDao.getById(materialID);
			if (orderStatus == null)
			{
				log.info("no order status information for this material");
			}
			m.setOrderStatus(orderStatus);
		}

		m.setTaskUpdated(eventTime); //should this be part of the manualqa notification so it is exactly the same as on task lists?

		if (operator != null)
		{
			m.setOperator(operator);
		}
		else
		{
			log.debug("Null operator in ManualQA notification");
		}

		if (taskStatus != null)
		{
			m.setTaskStatus(taskStatus);
		}
		else
		{
			log.debug("Null task status in ManualQA notification");
		}

		if (previewStatus != null)
		{
			m.setPreviewStatus(previewStatus);
		}
		else
		{
			log.debug("Null preview status in ManualQA notification");
		}


		if (hrPreview != null)
		{
			if ("1".equals(hrPreview))
			{
				m.setHrPreview(Boolean.TRUE);
			}
			else
			{
				log.error("Unexpected value for hrPreview in ManualQA notification");
			}
		}
		else
		{
			log.debug("Null hrPreview in ManualQA notification");
		}

		if (userWhoRequestedHrPreview != null)
		{
			m.setHrPreviewRequestedBy(userWhoRequestedHrPreview);
		}
		else
		{
			log.debug("Null hrPreviewRequested in ManualQA notification");
		}

		if (escalation != null)
		{

			try
			{
				Integer esc = Integer.parseInt(escalation);

				if (m.getEscalated().intValue() == 0 && esc.intValue() != 0)
				{
					log.info("Preview task has been escalated");
					m.setEverEscalated(Boolean.TRUE);
					m.setTimeEscalatedSet(eventTime);
				}

				if (m.getEscalated().intValue() != 0 && esc.intValue() == 0)
				{
					log.info("Preview task is no longer escalated");
					m.setTimeEscalatedCleared(eventTime);
				}

				m.setEscalated(esc);
			}
			catch (NumberFormatException e)
			{
				log.error("Unexpected value for escalation " + escalation);
			}
		}

		if (reordered != null)
		{

			if ("1".equals(reordered))
			{
				m.setReordered(Boolean.TRUE);
			}
			else if (!"0".equals(reordered))
			{
				log.error("Unexpected value for reordered in ManualQA notification");
			}
		}
		else
		{
			log.debug("Null reordered in ManualQA notification");
		}

		save(m);
	}


	@Override
	public List<ManualQAEntity> getManualQAInDateRange(final DateTime start, final DateTime end)
	{
		Criteria criteria = createCriteria();

		criteria.add(Restrictions.or(Restrictions.between("taskCreated", start.toDate(), end.plusDays(1).toDate()),
		                             Restrictions.between("taskUpdated", start.toDate(), end.plusDays(1).toDate()),
		                             Restrictions.between("timeEscalatedSet", start.toDate(), end.plusDays(1).toDate()),
		                             Restrictions.between("timeEscalatedCleared", start.toDate(), end.plusDays(1).toDate())));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		return getList(criteria);
	}
}
