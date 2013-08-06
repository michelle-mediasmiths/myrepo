package com.mediasmiths.stdEvents.persistence.db.impl;

import com.mediasmiths.foxtel.ip.common.events.TranscodeReportData;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.TranscodeJob;
import com.mediasmiths.stdEvents.persistence.db.dao.TranscodeJobDao;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

public class TranscodeJobDaoImpl extends HibernateDao<TranscodeJob, String> implements TranscodeJobDao
{
	private final static Logger log = Logger.getLogger(TranscodeJobDaoImpl.class);

	protected JAXBSerialiser serialiser = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);


	@Override
	@Transactional
	public void transcodeReportMessage(final EventEntity event)
	{
		log.debug("Transcode report data event");

		TranscodeReportData trd = (TranscodeReportData) serialiser.deserialise(event.getPayload());

		final String jobID = trd.getJobID();
		final XMLGregorianCalendar created = trd.getCreated();
		final XMLGregorianCalendar updated = trd.getUpdated();
		final XMLGregorianCalendar started = trd.getStarted();
		final String sourceFormat = trd.getSourceFormat();
		final String destinationFormat = trd.getDestinationFormat();
		final Integer priority = trd.getPriority();
		final String status = trd.getStatus();

		if (jobID == null)
		{
			log.error("Cannot use a TranscodeReportData message that has a null job id, ignoring");
			return;
		}

		TranscodeJob job = getById(jobID);

		if (job == null)
		{
			log.debug(String.format("New transcode job entry with id %s", jobID));

			job = new TranscodeJob();
			job.setJobID(jobID);
		}

		if (sourceFormat != null)
		{
			job.setSourceFormat(sourceFormat);
		}

		if (destinationFormat != null)
		{
			job.setDestinationFormat(destinationFormat);
		}

		if (priority != null)
		{
			job.setPriority(priority);
		}
		if (status != null)
		{
			job.setStatus(status);
		}

		if (created != null)
		{
			job.setCreated(created.toGregorianCalendar().getTime());
		}

		if (updated != null)
		{
			job.setUpdated(updated.toGregorianCalendar().getTime());
		}

		if (started != null)
		{
			job.setStarted(started.toGregorianCalendar().getTime());
		}

		saveOrUpdate(job);
	}


	@Override
	@Transactional
	public List<TranscodeJob> getTranscodeJobsInDateRange(final DateTime start, final DateTime end)
	{
		Criteria criteria = createCriteria();

		criteria.add(Restrictions.or(Restrictions.between("created", start.toDate(), end.plusDays(1).toDate()),
		                             Restrictions.between("updated", start.toDate(), end.plusDays(1).toDate()),
		                             Restrictions.between("started", start.toDate(), end.plusDays(1).toDate())));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.addOrder(Order.asc("created"));

		return getList(criteria);
	}
}
