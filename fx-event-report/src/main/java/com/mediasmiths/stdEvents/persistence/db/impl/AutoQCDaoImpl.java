package com.mediasmiths.stdEvents.persistence.db.impl;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.common.events.AutoQCEvent;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AutoQC;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.persistence.db.dao.AutoQCDao;
import com.mediasmiths.stdEvents.persistence.db.dao.OrderDao;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.List;

public class AutoQCDaoImpl extends HibernateDao<AutoQC, String> implements AutoQCDao
{
	private final static Logger log = Logger.getLogger(AutoQCDaoImpl.class);

	protected JAXBSerialiser serialiser = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);

	@Inject
	protected OrderDao orderDao;



	@Override
	@Transactional
	public void autoQCMessage(EventEntity event)
	{
		AutoQCEvent aqce = (AutoQCEvent) serialiser.deserialise(event.getPayload());

		String materialID = aqce.getMaterialID();
		String assetTitle = aqce.getAssetTitle();
		String contentType = aqce.getContentType();
		String operator = aqce.getOperator();
		String taskStatus = aqce.getTaskStatus();
		String qcStatus = aqce.getQcStatus();
		XMLGregorianCalendar taskStart = aqce.getTaskStart();
		XMLGregorianCalendar taskFinish = aqce.getTaskFinish();
		XMLGregorianCalendar warningTime = aqce.getWarningTime();
		Boolean overriden = aqce.isOverriden();
		String failureParameter = aqce.getFailureParameter();

		if (materialID == null)
		{
			log.error("cant use an autoqc message that has a null material id, ignoring");
			return;
		}

		AutoQC a = getById(materialID);

		if (a == null)
		{
			log.debug(String.format("New autoqc entry for material %s", materialID));
			a = new AutoQC();
			a.setMaterialid(materialID);
			OrderStatus orderStatus = orderDao.getById(materialID);
			if (orderStatus == null)
			{
				log.info("no order status information for this material");
			}
			a.setOrderStatus(orderStatus);
		}

		a.setAssetTitle(assetTitle);
		a.setContentType(contentType);
		a.setOperator(operator);
		a.setTaskStatus(taskStatus);
		a.setQcStatus(qcStatus);

		if (overriden == Boolean.TRUE)
		{
			a.setOverride(Boolean.TRUE);
		}

		a.setFailureParameter(failureParameter);

		if (taskStart != null)
		{
			Date taskStartDate = taskStart.toGregorianCalendar().getTime();
			a.setTaskCreated(taskStartDate);
		}

		if (taskFinish != null)
		{
			Date taskFinishDate = taskFinish.toGregorianCalendar().getTime();
			a.setTaskFinished(taskFinishDate);
		}

		if (warningTime != null)
		{
			Date warningTimeDate = warningTime.toGregorianCalendar().getTime();
			a.setWarningTime(warningTimeDate);
		}

		saveOrUpdate(a);
	}

	@Override
	public List<AutoQC> getAutoQcInDateRange(DateTime start, DateTime end)
	{
		Criteria criteria = createCriteria();

		criteria.add(Restrictions.or(
				Restrictions.between("taskCreated", start.toDate(), end.plusDays(1).toDate()),
				Restrictions.between("taskFinished", start.toDate(), end.plusDays(1).toDate()),
				Restrictions.between("warningTime", start.toDate(), end.plusDays(1).toDate())));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		return getList(criteria);
	}

}
