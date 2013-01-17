package com.mediasmiths.stdEvents.persistence.db.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.mediasmiths.std.guice.hibernate.dao.HibernateDao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.events.db.entity.QC;
import com.mediasmiths.stdEvents.persistence.db.dao.QueryDao;
import com.mediasmiths.std.guice.database.annotation.Transactional;

public class QCDaoImpl extends HibernateDao<QC, Long> implements EventMarshaller, QueryDao<QC>
{
	public static final transient Logger logger = Logger.getLogger(QCDaoImpl.class);
	
	public QCDaoImpl()
	{
		super(QC.class);
	}

	@Transactional
	public void save(EventEntity event)
	{
		QC qc = get(event);
		qc.event = event;
		logger.info("Saving qc");
		saveOrUpdate(qc);
	}
	
	@Transactional
	public List<QC> getAll()
	{
		logger.info("Getting all QC...");
		List<QC> qc = super.getAll();
		logger.info("Found all QC");
		return qc;
	}
	
	@Transactional
	public QC get(EventEntity event)
	{
		logger.info("Setting qc for event...");
		QC qc = new QC();
		String qcStr = event.getPayload();
		logger.info("String recived: " +qcStr);
		if (qcStr.contains("JobName"))
			qc.setJobName(qcStr.substring(qcStr.indexOf("JobName") +8, qcStr.indexOf("</JobName>")));
		
		if (qcStr.contains("AssetID"))
			qc.setAssetId(qcStr.substring(qcStr.indexOf("AssetId") +8, qcStr.indexOf("</AssetId>")));
		if (qcStr.contains("ForTXDelivery")) {
			String bool = qcStr.substring(qcStr.indexOf("ForTXDelivery") +14, qcStr.indexOf("</ForTXDelivery>"));
			if (bool.equals("true")) {
				qc.setForTXDelivery(true);
				logger.info("Tx delivery = true");
			}
			else{
				qc.setForTXDelivery(false);
				logger.info("Tx delivery = false");
			}
		}
		
		logger.info("QC constructed");
		
		return qc;	
	}
	
	@Override
	public String getNamespace(EventEntity event)
	{
		String namespace = event.getNamespace();
		return namespace;
	}

	@Override
	public String getInfo(EventEntity event)
	{
		String namespace = event.getNamespace().substring(25);
		String info = "Translating from EventEntity to " + namespace;
		return info;
	}

}
