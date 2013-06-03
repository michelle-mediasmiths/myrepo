package com.mediasmiths.mq.handlers.qc;

import java.util.Date;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.ip.common.events.AutoQCEvent;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.mayam.DateUtil;

public class QcEvent
{
	@Inject(optional = false)
	@Named("qc.events.namespace")
	private String qcEventNamespace;

	@Inject
	protected EventService eventsService;

	@Inject
	DateUtil dateUtil;

	private final static Logger log = Logger.getLogger(QcEvent.class);

	public void sendAutoQcEvent(AttributeMap a)
	{
		try
		{
			log.info("building AutoQCReport event");
			AutoQCEvent qce = new AutoQCEvent();

			qce.setAssetTitle((String) a.getAttribute(Attribute.ASSET_TITLE));
			qce.setMaterialID((String) a.getAttribute(Attribute.HOUSE_ID));
			qce.setContentType((String) a.getAttribute(Attribute.CONT_MAT_TYPE));
			qce.setOperator((String) a.getAttribute(Attribute.ASSIGNED_USER));
			qce.setTaskStatus((String) a.getAttribute(Attribute.TASK_STATE));

			Date created = a.getAttribute(Attribute.TASK_CREATED);
			if (created != null)
			{
				qce.setTaskStart(dateUtil.fromDate(created));
			}

			TaskState state = a.getAttribute(Attribute.TASK_STATE);

			if (TaskState.WARNING == state)
			{
				Date updated = a.getAttribute(Attribute.TASK_UPDATED);

				if (updated != null)
				{
					qce.setWarningTime(dateUtil.fromDate(updated));
				}
				else
				{
					log.warn("task updated date is null!");
				}
			}

			if (TaskState.FINISHED == state || TaskState.FINISHED_FAILED == state)
			{
				Date updated = a.getAttribute(Attribute.TASK_UPDATED);

				if (updated != null)
				{
					qce.setTaskStart(dateUtil.fromDate(updated));
				}
				else
				{
					log.warn("task updated date is null!");
				}

				QcStatus assetQcStatus = a.getAttribute(Attribute.QC_STATUS);
				QcStatus ffv = a.getAttribute(Attribute.QC_SUBSTATUS1);
				QcStatus cerify = a.getAttribute(Attribute.QC_SUBSTATUS2);

				if (QcStatus.PASS == assetQcStatus)
				{
					if (QcStatus.PASS != ffv)
					{
						qce.setFailureParameter("File Format Verification");
						qce.setOverriden(Boolean.TRUE);
					}
					else if (QcStatus.PASS != cerify)
					{
						qce.setFailureParameter("Cerify");
						qce.setOverriden(Boolean.TRUE);
					}
				}
				else
				{
					if (QcStatus.PASS != ffv)
					{
						qce.setFailureParameter("File Format Verification");
					}
					else if (QcStatus.PASS != cerify)
					{
						qce.setFailureParameter("Cerify");
					}
				}
			}

			eventsService.saveEvent(qcEventNamespace, "AutoQCReport", qce);
		}
		catch (Exception e)
		{
			// this event is for reporting and not part of the actual workflow so not letting the exception go any further

			log.error("Error sending autoqc report event", e);
		}
	}
}