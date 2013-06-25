package com.mediasmiths.stdEvents.persistence.db.impl;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.common.events.ComplianceLoggingTaskEvent;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ComplianceLogging;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ExtendedPublishing;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Title;
import com.mediasmiths.stdEvents.persistence.db.dao.ComplianceLoggingDao;
import com.mediasmiths.stdEvents.persistence.db.dao.ExtendedPublishingDao;
import com.mediasmiths.stdEvents.persistence.db.dao.TitleDao;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

public class ComplianceLoggingDaoImpl extends HibernateDao<ComplianceLogging, Long> implements ComplianceLoggingDao
{

	protected JAXBSerialiser serializer = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);

	private final static Logger log = Logger.getLogger(ComplianceLoggingDaoImpl.class);

	@Inject
	private TitleDao titleDao;

	@Inject
	private ExtendedPublishingDao extendedPublishingDao;


	public ComplianceLoggingDaoImpl()
	{
		super(ComplianceLogging.class);
	}


	@Override
	public void complianceEvent(final EventEntity event)
	{

		ComplianceLoggingTaskEvent evt = (ComplianceLoggingTaskEvent) serializer.deserialise(event.getPayload());

		final Long taskID = evt.getTaskID();
		final String materialID = evt.getMaterialID();
		final XMLGregorianCalendar taskUpdated = evt.getTaskUpdated();
		final XMLGregorianCalendar taskCreated = evt.getTaskCreated();
		final String taskStatus = evt.getTaskStatus();
		final String sourceMaterialID = evt.getSourceMaterialID();
		final String titleID = evt.getTitleID();

		ComplianceLogging c = getById(taskID);

		if (c == null)
		{
			log.info("New compliance task " + taskID);
			c = new ComplianceLogging();
			c.setTaskID(taskID);
		}

		attatchTitleIfAvailable(titleID, c);

		c.setTaskStatus(taskStatus);
		c.setMaterialID(materialID);
		c.setSourceMaterialID(sourceMaterialID);

		if (taskCreated != null)
		{
			c.setTaskCreated(taskCreated.toGregorianCalendar().getTime());
		}

		if (taskUpdated != null)
		{
			c.setTaskUpdated(taskUpdated.toGregorianCalendar().getTime());
		}

		if ("FINISHED".equals(taskStatus) ||
		    "FINISHED_FAILED".equals(taskStatus) ||
		    "REMOVED".equals(taskStatus) ||
		    "REJECTED".equals(taskStatus))
		{
			c.setComplete(true);
			if (taskUpdated != null)
			{
				c.setDateCompleted(taskUpdated.toGregorianCalendar().getTime());
			}
		}

		//look for compliance exports for this task
		if (c.getExternalCompliance() != null && !c.getExternalCompliance())
		{
			List<ExtendedPublishing> exportTasks = extendedPublishingDao.getByMaterialIDAndType(sourceMaterialID,
			                                                                                    TranscodeJobType.COMPLIANCE_PROXY);
			if (exportTasks != null && exportTasks.size() > 0)
			{
				c.setExternalCompliance(Boolean.TRUE);
			}
		}

		saveOrUpdate(c);
	}


	private void attatchTitleIfAvailable(final String titleID, final ComplianceLogging c)
	{
		if (c.getTitle() == null && titleID != null)
		{
			Title t = titleDao.getById(titleID);

			if (t != null)
			{
				c.setTitle(t);
			}
			else
			{
				log.info("no title information for this compliance task, titleID "+titleID);
			}
		}
	}


	@Override
	public List<ComplianceLogging> getComplianceByDate(final DateTime start, final DateTime end)
	{
		Criteria criteria = createCriteria();

		criteria.add(Restrictions.or(Restrictions.between("taskCreated", start.toDate(), end.plusDays(1).toDate()),
		                             Restrictions.between("taskUpdated", start.toDate(), end.plusDays(1).toDate()),
		                             Restrictions.between("dateCompleted", start.toDate(), end.plusDays(1).toDate())));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		log.info("Fetching compliance logging tasks in date range");
		final List<ComplianceLogging> list = getList(criteria);
		return list;
	}
}
