package com.mediasmiths.foxtel.ip.mayam.pdp;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.FilterResult;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import org.apache.log4j.Logger;

import javax.xml.ws.WebServiceException;
import java.util.HashMap;
import java.util.Map;




public class Stub implements MayamPDP
{

	// Error Messages

	@Inject
	@Named("message.task.technical")
	String messageTaskTechnical;

	@Inject
	@Named("message.task.multiple")
	String messageTaskMultiple;

	@Inject
	@Named("message.task.permission")
	String messageTaskPermission;

	@Inject
	@Named("message.segment.mismatch.override")
	String messageSegmentMismatchOverride;

	@Inject
	@Named("message.seqment.mismatch.technical")
	String messageSeqmentMismatchTechnical;

	@Inject
	@Named("message.package.classification")
	String messagePackageClassification;

	@Inject
	@Named("message.item.uningest.protected")
	String messageItemUningestProtected;

	@Inject
	@Named("message.item.uningest.override")
	String messageItemUningestOverride;

	@Inject
	@Named("message.item.delete.protected")
	String messageItemDeleteProtected;

	@Inject
	@Named("message.item.delete.override")
	String messageItemDeleteOverride;

	@Inject
	@Named("message.markers.none")
	String messageMarkersNone;

	@Inject
	@Named("message.markers.technical")
	String messageMarkersTechnical;

	@Inject
	@Named("message.compliance.edit.none")
	String messageComplianceEditNone;

	@Inject
	@Named("message.compliance.logging.none")
	String messageComplianceLoggingNone;

	@Inject
	@Named("message.proxy.filename.exists")
	String messageProxyFilenameExists;

	@Inject
	@Named("message.proxy.filename.technical")
	String messageProxyFilenameTechnical;

	// ---------------

	private Logger logger = Logger.getLogger(MayamPDPImpl.class);

	private final Map<String,Object> okStatus = new HashMap<String,Object>();

	private final TasksClient client;
	private final MayamTaskController taskController;

	@Inject
	public Stub(@Named(MayamClientModule.SETUP_TASKS_CLIENT)TasksClient client, MayamTaskController taskController)
	{
		this.client=client;
		this.taskController=taskController;
	}


	@Override
	public String ping()
	{
		return "<html>Ping</html>";
	}

	@Override
	public Map<String, Object> segmentMismatch(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> segmentClassificationCheck(final Map<String, Object> attributeMap)
	{
		return okStatus;
	}

	@Override
	public Map<String, Object> uningestProtected(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> protect(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> unprotect(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Map<String, Object> delete(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> proxyfileCheck(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> ingest(final Map<String, Object> attributeMap)
	{
		validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
		String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();

		return doesTaskExist(houseID, MayamTaskListType.INGEST);
	}

	@Override
	public Map<String, Object> complianceEdit(final Map<String, Object> attributeMap)
	{
		validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
		String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();
		String parentHouseID = attributeMap.get(Attribute.PARENT_HOUSE_ID.toString()).toString();

		Map<String, Object> returnMap = doesTaskExist(houseID, MayamTaskListType.COMPLIANCE_EDIT);
		if (returnMap != null)
		{
			Object status = returnMap.get(PDPAttributes.OP_STAT).toString();
			if (status.equals(StatusCodes.CONFIRM))
			{
				if (parentHouseID == null || parentHouseID.equals(""))
				{
					returnMap.clear();
					returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
					returnMap.put(PDPAttributes.ERROR_MSG.toString(), String.format(messageComplianceEditNone, houseID));
				}
			}
		}

		return returnMap;
	}

	@Override
	public Map<String, Object> complianceLogging(final Map<String, Object> attributeMap)
	{
		return okStatus;
	}

	@Override
	public Map<String, Object> unmatched(final Map<String, Object> attributeMap)
	{
		validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
		String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();

		return doesTaskExist(houseID, MayamTaskListType.UNMATCHED_MEDIA);
	}

	@Override
	public Map<String, Object> preview(final Map<String, Object> attributeMap)
	{
		validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
		String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();

		return doesTaskExist(houseID, MayamTaskListType.PREVIEW);
	}

	@Override
	public Map<String, Object> autoQC(final Map<String, Object> attributeMap)
	{
		validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
		String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();

		return doesTaskExist(houseID, MayamTaskListType.QC_VIEW);
	}

	@Override
	public Map<String, Object> txDelivery(final Map<String, Object> attributeMap)
	{
		validateAttributeMap(attributeMap, Attribute.HOUSE_ID.toString());
		String houseID = attributeMap.get(Attribute.HOUSE_ID.toString()).toString();

		return doesTaskExist(houseID, MayamTaskListType.TX_DELIVERY);
	}

	@Override
	public Map<String, Object> exportMarkers(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		return okStatus;
	}

	private Map<String, Object> doesTaskExist(String houseID, MayamTaskListType task)
	{

		Map<String, Object> returnMap = new HashMap<String,Object>();
		returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.OK);

		final FilterCriteria criteria = client.taskApi().createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(Attribute.TASK_LIST_ID, task.getText());
		criteria.getFilterEqualities().setAttribute(Attribute.HOUSE_ID, houseID);
		FilterResult result = null;

		try
		{
			result = client.taskApi().getTasks(criteria, 10, 0);
		}
		catch(RemoteException e) {
			returnMap.clear();
			returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
			returnMap.put(PDPAttributes.ERROR_MSG.toString(), String.format(messageTaskPermission, houseID));
		}

		if (result != null && result.getTotalMatches() > 0)
		{
			returnMap.clear();
			returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
			returnMap.put(PDPAttributes.ERROR_MSG.toString(), String.format(messageTaskMultiple,task.getText()));
		}

		return returnMap;
	}

	private void dumpPayload(final Map<String, Object> attributeMap)
	{
		if (attributeMap == null)
		{
			logger.info("Payload is null");
		}
		else if (attributeMap.keySet().isEmpty())
		{
             logger.info("There is not data in the received map");
		}
		else
		{
			for (String key: attributeMap.keySet())
			{
				logger.info(key + "<->" + attributeMap.get(key).toString());
			}
		}
	}


	/**
	 *
	 * @param attributeMap the incoming Mayam Attribute map
	 * @param attributeNamesRequired the required fields that must be set in this attribute map.
	 * @return true if all the attribute names in attributeNamesRequired are in the asset map.
	 */
	private boolean validateAttributeMap(Map<String, Object> attributeMap, String... attributeNamesRequired)
	{
		if (attributeMap == null || attributeMap.keySet().isEmpty())
			throw new WebServiceException("MAYAM Attribute Map is EMPTY");

		if (attributeNamesRequired == null || attributeNamesRequired.length == 0)
			throw new IllegalArgumentException("MayamPDPSetUp Internal Error: There must be some input map names from Mayam");

		for (String mapItemName: attributeNamesRequired)
		{
			if (!attributeMap.keySet().contains(mapItemName))
				throw new WebServiceException("Mayam Attribute " + mapItemName + " must be set in this call to the PDP");
		}

		return true;
	}

}
