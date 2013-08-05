package com.mediasmiths.stdEvents.persistence.db.impl;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.common.events.AddOrUpdateMaterial;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus.TaskType;
import com.mediasmiths.stdEvents.persistence.db.dao.OrderDao;
import com.mediasmiths.stdEvents.persistence.db.dao.TitleDao;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

public class OrderDaoImpl extends HibernateDao<OrderStatus, String> implements OrderDao
{

	protected JAXBSerialiser serialiser = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);

	private final static Logger log = Logger.getLogger(OrderDaoImpl.class);
	
	@Inject
	protected TitleDao titleDao;
	
	public OrderDaoImpl()
	{
		super(OrderStatus.class);
	}

	@Override
	@Transactional
	public void addOrUpdateMaterial(EventEntity event)
	{
		AddOrUpdateMaterial aoum = (AddOrUpdateMaterial) serialiser.deserialise(event.getPayload());

		String materialID = aoum.getMaterialID();
		String orderReference = aoum.getOrderReference();

		String titleID = aoum.getTitleID();
		
		Long fileSize = aoum.getFilesize();
		Integer titleLength = aoum.getTitleLength();
		String format = aoum.getFormat();
		
		Date requiredBy = null;

		if (aoum.getRequiredBy() != null)
		{
			requiredBy = aoum.getRequiredBy().toGregorianCalendar().getTime();
		}

		Date completionDate = null;

		if (aoum.getCompletionDate() != null)
		{
			completionDate = aoum.getCompletionDate().toGregorianCalendar().getTime();
		}

		String aggregatorID = aoum.getAggregatorID();

		

		OrderStatus order = getById(materialID);

		if (order == null)
		{
			log.debug(String.format("New order entry material %s title %s",materialID,titleID));
			order = new OrderStatus();
			order.setMaterialid(materialID);
			if (titleID != null)
			{
				order.setTitle(titleDao.getById(titleID));
			}
			order.setCreated(new Date());
		}
		else
		{
			if (order.getTitle() == null || (order.getTitle().getTitleId() != titleID))
			{
				log.debug("setting title");
				if (titleID != null)
				{
					order.setTitle(titleDao.getById(titleID));
				}
			}			
		}

		if (orderReference != null)
		{
			order.setOrderReference(orderReference);
		}

		if (requiredBy != null)
		{
			order.setRequiredBy(requiredBy);
		}

		if (completionDate != null)
		{
			order.setCompleted(completionDate);
		}

		if (aggregatorID != null)
		{
			order.setAggregatorID(aggregatorID);
		}
		
		if (fileSize != null)
		{
			order.setFileSize(fileSize);
		}
		
		if (titleLength != null)
		{
			order.setTitleLength(titleLength);
		}

		if (format != null)
		{
			order.setFormat(format);
		}

		TaskType currentTaskType = order.getTaskType();
		TaskType taskType = TaskType.INGEST;

		if (aoum.getTaskType() != null)
		{
			taskType = OrderStatus.TaskType.valueOf(aoum.getTaskType());
			
			if(! taskType.equals(currentTaskType)){
				order.setTaskType(taskType);
			}
		}

		saveOrUpdate(order);
	}

	@Override
	@Transactional
	public List<OrderStatus> getOrdersInDateRange(DateTime start, DateTime end)
	{
		Criteria criteria = createCriteria();

		criteria.add(Restrictions.or(
				Restrictions.between("created", start.toDate(), end.toDate()),
				Restrictions.between("completed", start.toDate(), end.toDate())));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		return getList(criteria);
	}

	@Override
	public List<OrderStatus> getCompletedOrdersInDateRange(final DateTime start, final DateTime end)
	{
		Criteria criteria = createCriteria();

		criteria.add(Restrictions.between("completed", start.toDate(), end.toDate()));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		return getList(criteria);
	}
}
