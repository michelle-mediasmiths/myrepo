package com.mediasmiths.foxtel.ip.mayam.pdp;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.FilterResult;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import org.apache.log4j.Logger;

import javax.xml.ws.WebServiceException;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	@Inject
	@Named("message.task.permission")
	String permissionErrorTaskMessage;
	
	@Inject
	@Named("message.action.permission")
	String permissionErrorActionMessage;

	// ---------------

	private Logger logger = Logger.getLogger(MayamPDPImpl.class);

	private final Map<String, Object> okStatus = new HashMap<String, Object>();

	private final TasksClient client;
	private final MayamTaskController taskController;

	@Inject
	@Named("foxtel.groups.nonao")
	Map<PrivilegedOperations, Set<FoxtelGroups>> permissionsNonAO;

	@Inject
	@Named("foxtel.groups.ao")
	Map<PrivilegedOperations, Set<FoxtelGroups>> permissionsAO;

	@Inject
	public Stub(@Named(MayamClientModule.SETUP_TASKS_CLIENT) TasksClient client, MayamTaskController taskController)
	{
		this.client = client;
		this.taskController = taskController;

		okStatus.put(PDPAttributes.OP_STAT.toString(), "ok");

	}

	@Override
	public String ping()
	{
		return "<html>Ping</html>";
	}

	@Override
	public Map<String, Object> segmentMismatch(final Map<String, Object> attributeMap)
	{
		defaultValidation(attributeMap);
		dumpPayload(attributeMap);

		return okStatus;
	}

	@Override
	public Map<String, Object> segmentClassificationCheck(final Map<String, Object> attributeMap)
	{
		defaultValidation(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> uningestProtected(final Map<String, Object> attributeMap)
	{
		defaultValidation(attributeMap);
		dumpPayload(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> protect(final Map<String, Object> attributeMap) throws RemoteException
	{
		PrivilegedOperations operation = PrivilegedOperations.PROTECT;
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);

		boolean permission = userCanPerformOperation(operation, attributeMap);

		if (permission)
		{
			return okStatus;
		}
		else
		{
			return getActionPermissionsErrorStatus(operation);
		}
	}

	@Override
	public Map<String, Object> unprotect(final Map<String, Object> attributeMap) throws RemoteException
	{
		PrivilegedOperations operation = PrivilegedOperations.UNPROTECT;
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);

		boolean permission = userCanPerformOperation(operation, attributeMap);

		if (permission)
		{
			return okStatus;
		}
		else
		{
			return getActionPermissionsErrorStatus(operation);
		}
	}

	@Override
	public Map<String, Object> delete(final Map<String, Object> attributeMap) throws RemoteException
	{
		PrivilegedOperations operation = PrivilegedOperations.DELETE;
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);

		boolean permission = userCanPerformOperation(operation, attributeMap);

		if (permission)
		{
			return okStatus;
		}
		else
		{
			return getActionPermissionsErrorStatus(operation);
		}
	}

	@Override
	public Map<String, Object> proxyfileCheck(final Map<String, Object> attributeMap)
	{
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);
		return okStatus;
	}

	@Override
	public Map<String, Object> ingest(final Map<String, Object> attributeMap) throws RemoteException
	{
		PrivilegedOperations operation = PrivilegedOperations.INGEST;
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);

		boolean permission = userCanPerformOperation(operation, attributeMap);

		if (permission)
		{
			String houseID = attributeMap.get(Attribute.HOUSE_ID.toString().toLowerCase()).toString();
			return doesTaskExist(houseID, MayamTaskListType.INGEST);
		}
		else
		{
			return getTaskPermissionErrorStatus(operation);
		}
	}

	@Override
	public Map<String, Object> complianceEdit(final Map<String, Object> attributeMap) throws RemoteException
	{
		PrivilegedOperations operation = PrivilegedOperations.COMPLIANCE_EDITING;
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);

		boolean permission = userCanPerformOperation(operation, attributeMap);

		if (permission)
		{
			String houseID = attributeMap.get(Attribute.HOUSE_ID.toString().toLowerCase()).toString();
			String sourceHouseID = (String) attributeMap.get(Attribute.SOURCE_HOUSE_ID.toString().toLowerCase());

			Map<String, Object> returnMap = doesTaskExist(houseID, MayamTaskListType.COMPLIANCE_EDIT);
			if (returnMap != null)
			{
				Object status = returnMap.get(PDPAttributes.OP_STAT.toString()).toString();
				if (status.equals(StatusCodes.OK.toString()))
				{
					if (sourceHouseID == null || sourceHouseID.equals(""))
					{
						returnMap.clear();
						returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
						returnMap.put(
								PDPAttributes.ERROR_MSG.toString().toLowerCase(),
								String.format(messageComplianceEditNone, houseID));
					}
					else
					{
						logger.debug("parent house id was not null or empty");
					}
				}
				else
				{
					logger.debug("returned status was not ok");
				}
			}

			return returnMap;
		}
		else
		{
			return getTaskPermissionErrorStatus(operation);
		}
	}

	@Override
	public Map<String, Object> complianceLogging(final Map<String, Object> attributeMap) throws RemoteException
	{
		PrivilegedOperations operation = PrivilegedOperations.COMPLIANCE_LOGGING;
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);

		boolean permission = userCanPerformOperation(operation, attributeMap);

		if (permission)
		{
			String houseID = attributeMap.get(Attribute.HOUSE_ID.toString().toLowerCase()).toString();
			String sourceHouseID = (String) attributeMap.get(Attribute.SOURCE_HOUSE_ID.toString().toLowerCase());

			Map<String, Object> returnMap = doesTaskExist(houseID, MayamTaskListType.COMPLIANCE_LOGGING);
			if (returnMap != null)
			{
				Object status = returnMap.get(PDPAttributes.OP_STAT.toString()).toString();
				if (status.equals(StatusCodes.OK.toString()))
				{
					if (sourceHouseID == null || sourceHouseID.equals(""))
					{
						returnMap.clear();
						returnMap.put(PDPAttributes.OP_STAT.toString(), StatusCodes.ERROR);
						returnMap.put(
								PDPAttributes.ERROR_MSG.toString().toLowerCase(),
								String.format(messageComplianceLoggingNone, houseID));
					}
					else
					{
						logger.debug("parent house id was not null or empty");
					}
				}
				else
				{
					logger.debug("returned status was not ok");
				}
			}

			return returnMap;
		}
		else
		{
			return getTaskPermissionErrorStatus(operation);
		}
	}

	@Override
	public Map<String, Object> unmatched(final Map<String, Object> attributeMap) throws RemoteException
	{
		PrivilegedOperations operation = PrivilegedOperations.MATCHING;
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);

		boolean permission = userCanPerformOperation(operation, attributeMap);

		if (permission)
		{
			String houseID = attributeMap.get(Attribute.HOUSE_ID.toString().toLowerCase()).toString();
			return doesTaskExist(houseID, MayamTaskListType.UNMATCHED_MEDIA);
		}
		else
		{
			return getTaskPermissionErrorStatus(operation);
		}
	}

	@Override
	public Map<String, Object> preview(final Map<String, Object> attributeMap) throws RemoteException
	{
		PrivilegedOperations operation = PrivilegedOperations.PREVIEW;
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);

		boolean permission = userCanPerformOperation(operation, attributeMap);

		if (permission)
		{
			String houseID = attributeMap.get(Attribute.HOUSE_ID.toString().toLowerCase()).toString();
			return doesTaskExist(houseID, MayamTaskListType.PREVIEW);
		}
		else
		{
			return getTaskPermissionErrorStatus(operation);
		}
	}

	@Override
	public Map<String, Object> autoQC(final Map<String, Object> attributeMap) throws RemoteException
	{
		PrivilegedOperations operation = PrivilegedOperations.AUTOQC;
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);

		boolean permission = userCanPerformOperation(operation, attributeMap);

		if (permission)
		{
			String houseID = attributeMap.get(Attribute.HOUSE_ID.toString().toLowerCase()).toString();
			return doesTaskExist(houseID, MayamTaskListType.QC_VIEW);
		}
		else
		{
			return getTaskPermissionErrorStatus(operation);
		}
	}

	@Override
	public Map<String, Object> txDelivery(final Map<String, Object> attributeMap) throws RemoteException
	{
		PrivilegedOperations operation = PrivilegedOperations.TX_DELIVERY;
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);

		boolean permission = userCanPerformOperation(operation, attributeMap);

		if (permission)
		{
			String houseID = attributeMap.get(Attribute.HOUSE_ID.toString().toLowerCase()).toString();
			return doesTaskExist(houseID, MayamTaskListType.TX_DELIVERY);
		}
		else
		{
			return getTaskPermissionErrorStatus(operation);
		}
	}

	@Override
	public Map<String, Object> exportMarkers(final Map<String, Object> attributeMap) throws RemoteException
	{
		PrivilegedOperations operation = PrivilegedOperations.EXPORT_MARKERS;
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);

		boolean permission = userCanPerformOperation(operation, attributeMap);

		if (permission)
		{
			return okStatus;
		}
		else
		{
			return getTaskPermissionErrorStatus(operation);
		}
	}

	@Override
	public Map<String, Object> complianceProxy(final Map<String, Object> attributeMap) throws RemoteException
	{
		PrivilegedOperations operation = PrivilegedOperations.COMPLIANCE_PROXY;
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);

		boolean permission = userCanPerformOperation(operation, attributeMap);

		if (permission)
		{
			return okStatus;
		}
		else
		{
			return getTaskPermissionErrorStatus(operation);
		}
	}

	@Override
	public Map<String, Object> captionsProxy(final Map<String, Object> attributeMap) throws RemoteException
	{
		PrivilegedOperations operation = PrivilegedOperations.CAPTIONS_PROXY;
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);

		boolean permission = userCanPerformOperation(operation, attributeMap);

		if (permission)
		{
			return okStatus;
		}
		else
		{
			return getTaskPermissionErrorStatus(operation);
		}
	}

	@Override
	public Map<String, Object> publicityProxy(final Map<String, Object> attributeMap) throws RemoteException
	{
		PrivilegedOperations operation = PrivilegedOperations.PUBLICITY_PROXY;
		dumpPayload(attributeMap);
		defaultValidation(attributeMap);

		boolean permission = userCanPerformOperation(operation, attributeMap);

		if (permission)
		{
			return okStatus;
		}
		else
		{
			return getTaskPermissionErrorStatus(operation);
		}
	}

	private Map<String, Object> getTaskPermissionErrorStatus(PrivilegedOperations operation)
	{
		Map<String, Object> permissionErrorStatus = new HashMap<String, Object>();

		permissionErrorStatus.put(PDPAttributes.OP_STAT.toString(), "error");
		permissionErrorStatus.put(PDPAttributes.ERROR_MSG.toString(), String.format(permissionErrorTaskMessage, operation.toString()));

		return permissionErrorStatus;
	}
	
	private Map<String, Object> getActionPermissionsErrorStatus(PrivilegedOperations operation)
	{
		Map<String, Object> permissionErrorStatus = new HashMap<String, Object>();

		permissionErrorStatus.put(PDPAttributes.OP_STAT.toString(), "error");
		permissionErrorStatus.put(PDPAttributes.ERROR_MSG.toString(), String.format(permissionErrorActionMessage, operation.toString()));

		return permissionErrorStatus;
	}

	private Map<String, Object> doesTaskExist(String houseID, MayamTaskListType task)
	{

		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put(PDPAttributes.OP_STAT.toString().toLowerCase(), StatusCodes.OK);

		logger.info(String.format("Searching for tasks of type %s for asset %s", task.getText(), houseID));

		final FilterCriteria criteria = client.taskApi().createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(Attribute.TASK_LIST_ID, task.getText());
		criteria.getFilterEqualities().setAttribute(Attribute.HOUSE_ID, houseID);
		criteria.getFilterAlternatives().addAsExclusions(Attribute.TASK_STATE, END_STATES);
		FilterResult result = null;

		try
		{
			result = client.taskApi().getTasks(criteria, 10, 0);
		}
		catch (RemoteException e)
		{
			logger.warn("Exception searching for tasks");
			returnMap.clear();
			returnMap.put(PDPAttributes.OP_STAT.toString().toLowerCase(), StatusCodes.ERROR);
			returnMap.put(PDPAttributes.ERROR_MSG.toString().toLowerCase(), String.format(messageTaskTechnical, houseID));
		}

		if (result != null && result.getTotalMatches() > 0)
		{
			logger.debug("found tasks");
			returnMap.clear();
			returnMap.put(PDPAttributes.OP_STAT.toString().toLowerCase(), StatusCodes.ERROR);
			returnMap.put(PDPAttributes.ERROR_MSG.toString().toLowerCase(), String.format(messageTaskMultiple, task.getText()));
		}

		logger.debug("found no tasks");
		return returnMap;
	}

	final EnumSet<TaskState> END_STATES = EnumSet.of(
			TaskState.FINISHED,
			TaskState.FINISHED_FAILED,
			TaskState.REJECTED,
			TaskState.REMOVED);

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
			for (String key : attributeMap.keySet())
			{
				logger.info(key + "<->" + attributeMap.get(key).toString());
			}
		}
	}

	private String getUser(Map<String, Object> attributeMap)
	{
		return (String) attributeMap.get(Attribute.TASK_CREATED_BY.toString().toLowerCase());
	}

	private boolean isAo(Map<String, Object> attributeMap)
	{

		try
		{
			Object o = attributeMap.get(Attribute.CONT_RESTRICTED_MATERIAL.toString().toLowerCase());

			if (o instanceof Boolean)
			{
				return ((Boolean) o).booleanValue();
			}
			else if (o instanceof String)
			{
				String s = (String) o;

				Boolean b = new Boolean(s);
				return b.booleanValue();
			}
		}
		catch (Exception e)
		{
			logger.error("error checking if asset is ao", e);
		}

		throw new IllegalArgumentException(String.format(
				"Invalid value for %s",
				Attribute.CONT_RESTRICTED_MATERIAL.toString().toLowerCase()));

	}

	private void defaultValidation(Map<String, Object> attributeMap)
	{
		validateAttributeMap(
				attributeMap,
				Attribute.HOUSE_ID.toString().toLowerCase(),
				Attribute.HOUSE_ID.toString().toLowerCase(),
				Attribute.ASSET_TYPE.toString().toLowerCase(),
				Attribute.TASK_CREATED_BY.toString().toLowerCase(),
				Attribute.CONT_RESTRICTED_MATERIAL.toString().toLowerCase());
	}

	/**
	 * 
	 * @param attributeMap
	 *            the incoming Mayam Attribute map
	 * @param attributeNamesRequired
	 *            the required fields that must be set in this attribute map.
	 * @return true if all the attribute names in attributeNamesRequired are in the asset map.
	 */
	private boolean validateAttributeMap(Map<String, Object> attributeMap, String... attributeNamesRequired)
	{
		if (attributeMap == null || attributeMap.keySet().isEmpty())
			throw new WebServiceException("MAYAM Attribute Map is EMPTY");

		if (attributeNamesRequired == null || attributeNamesRequired.length == 0)
			throw new IllegalArgumentException("MayamPDPSetUp Internal Error: There must be some input map names from Mayam");

		for (String mapItemName : attributeNamesRequired)
		{
			if (!attributeMap.keySet().contains(mapItemName))
				throw new WebServiceException("Mayam Attribute " + mapItemName + " must be set in this call to the PDP");
		}

		return true;
	}

	private Set<FoxtelGroups> getUserGroups(String user) throws RemoteException // remote exception will be from mayam, I see no harm in throwing it back to them
	{
		Set<String> userGroups = client.userApi().getUserGroups(user);
		Set<FoxtelGroups> groups = new HashSet<FoxtelGroups>();

		for (String userGroup : userGroups)
		{
			try
			{
				groups.add(FoxtelGroups.fromString(userGroup));
			}
			catch (Exception e)
			{
				logger.debug(String.format("Unknown group %s", userGroup));
			}
		}

		return groups;

	}

	private boolean userCanPerformOperation(final PrivilegedOperations operation, final Map<String, Object> attributeMap)
			throws RemoteException
	{

		String user = getUser(attributeMap);
		boolean ao = isAo(attributeMap);
		Set<FoxtelGroups> userGroups = getUserGroups(user);
		Set<FoxtelGroups> permittedGroups = null;

		if (ao)
		{
			permittedGroups = permissionsAO.get(operation);
		}
		else
		{
			permittedGroups = permissionsNonAO.get(operation);
		}

		if (Collections.disjoint(userGroups, permittedGroups))
		{
			// user does not have permissions
			logger.info(String.format("User %s does not have permission for %s", user, operation.toString()));
			return false;
		}
		else
		{
			// user has permission
			logger.info(String.format("User %s does have permission for %s", user, operation.toString()));
			return true;
		}
	}

}
