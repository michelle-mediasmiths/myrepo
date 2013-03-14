package com.mediasmiths.foxtel.ip.mayam.pdp;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.server.AttributeMapMapper;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.FilterResult;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.util.AssetProperties;
import org.apache.log4j.Logger;

import javax.xml.ws.WebServiceException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MayamPDPImpl implements MayamPDP
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
	@Named("message.segment.mismatch.notoverride")
	String messageSegmentMismatchRefuseOverride;

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

	private Logger logger = Logger.getLogger(MayamPDP.class);

	private final TasksClient client;
	private final MayamTaskController taskController;

	@Inject
	@Named("foxtel.groups.nonao")
	Map<PrivilegedOperations, Set<FoxtelGroups>> permissionsNonAO;

	@Inject
	@Named("foxtel.groups.ao")
	Map<PrivilegedOperations, Set<FoxtelGroups>> permissionsAO;

	private String okStatus;

	private  AttributeMapMapper mapper;
	
	@Inject
	public MayamPDPImpl(@Named(MayamClientModule.SETUP_TASKS_CLIENT) TasksClient client,
	                    MayamTaskController taskController,
	                    AttributeMapMapper mapper)
	{
		this.client = client;
		this.taskController = taskController;

		AttributeMap okStatusMap = new AttributeMap();
		okStatusMap.setAttribute(Attribute.OP_STAT, "ok");
		this.mapper=mapper;
		okStatus =  mapper.serialize(okStatusMap);

	}

	@Override
	public String ping()
	{
		return "<html>Ping</html>";
	}

	@Override
	public String segmentationComplete(final String attributeMapStr)
	{
		try
		{
			String warnings="";

			logger.info("Segment Mismatch.");

			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);

			defaultValidation(attributeMap);
			dumpPayload(attributeMap);

			//Segmentation check
			String requestedNumber = attributeMap.getAttributeAsString(Attribute.REQ_NUMBER);
			int numberOfSegmentsRequested = Integer.parseInt(requestedNumber);
			String presentationID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);

			SegmentList segmentList = null;
			try {
				segmentList = client.segmentApi().getSegmentListBySiteId(presentationID);
			}
			catch (Throwable e) {
				logger.error("Caught throwable: ", e);
				segmentList = null;
			}

			if (segmentList != null && segmentList.getEntries() != null)
			{
				int segmentsSize = segmentList.getEntries().size();

				logger.debug("Mayam PDP - Segment Mismatch Check - Number of Segments: " + segmentsSize + ", Number Requested: " + numberOfSegmentsRequested);


				if (numberOfSegmentsRequested != segmentsSize)
				{
					logger.info("Number of Segments Mismatch discovered. For Presentation ID : " + presentationID);

					boolean permission = userCanPerformOperation(PrivilegedOperations.SEGMENT_MISMATCH_OVERRIDE, attributeMap);

					if (permission)
					{
						logger.debug("User can override a segment mismatch for " + presentationID);

						warnings = messageSegmentMismatchOverride;
					}
					else
					{
						logger.debug("User cannot override a segment mismatch for " + presentationID);

						return getErrorStatus(messageSegmentMismatchRefuseOverride);
					}
				}
			}
			else
			{
				logger.info("Unable to retrieve Segment List. Return Map for  " + presentationID);

				return getErrorStatus("A technical fault has occurred while retrieving segemnt list");

			}

			// Classification Check
			String classification = attributeMap.getAttributeAsString(Attribute.CONT_CLASSIFICATION);
			if (classification == null || classification.equals(""))
			{
				logger.debug("The TX Package has not been classified. Please contact the channel owner and ensure that this is provided");

				return getErrorStatus("The TX Package has not been classified. Please contact the channel owner and ensure that this is provided");
			}

			// QC Parallel Check
			// if QC parallel has been enabled, and the QC status has not been set, then it will inform the user that the content has not 
			// finished auto QC yet and allow them to continue if they wish.

			AttributeMap parentAsset = client.assetApi().getAsset(MayamAssetType.MATERIAL.getAssetType(), attributeMap.getAttributeAsString(Attribute.ASSET_PARENT_ID));
			boolean isQcPassed = AssetProperties.isQCPassed(parentAsset);

			if (isQcPassed)
			{
				logger.info("QC is Passed on Presentation Id " + presentationID);

				return getConfirmStatus(warnings+"Do you wish to send to Tx?");
			}

			String qcParallelAttribute = parentAsset.getAttribute(Attribute.QC_PARALLEL_ALLOWED);

			if (qcParallelAttribute != null && Boolean.parseBoolean(qcParallelAttribute))
			{

				logger.info("Qc Parallel is set but Qc Status has not yet been passed, warning the user: " + presentationID);

				return getConfirmStatus(warnings+"QC Parallel has been set but Auto QC has not yet been passed. Are you sure you wish to proceed?");

			}
			
			return  getErrorStatus("Item is not QC passed. QC Parallel is not set  - you cannot proceed to Tx.");
		}
		catch (Exception e)
		{
			return getErrorStatus("A technical fault has occurred while verifying the segemntation complete rules");
		}
	}

	@Override
	public String segmentation(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);


			defaultValidation(attributeMap);

			AttributeMap returnMap = client.createAttributeMap();
			returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.OK.toString());

			String assetId = attributeMap.getAttributeAsString(Attribute.ASSET_ID);
			AttributeMap existingTask = taskController.getOnlyOpenTaskForAssetByAssetID(MayamTaskListType.SEGMENTATION, assetId);

			if (existingTask != null)
			{
				String presentationID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);

				returnMap.clear();
				returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR.toString());
				returnMap.setAttribute(Attribute.ERROR_MSG, "An open segmentation task already exists for asset :" + presentationID);
			}

			return  mapper.serialize(returnMap);
		}
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String uningest(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			defaultValidation(attributeMap);
			PrivilegedOperations operation = PrivilegedOperations.UNINGEST;

			boolean permission = userCanPerformOperation(operation, attributeMap);

			if (permission)
			{
				String protectedString = attributeMap.getAttributeAsString(Attribute.PURGE_PROTECTED);
				if (Boolean.parseBoolean(protectedString))
				{
					return getErrorStatus(messageItemUningestProtected);
				}
				else
				{
					return okStatus;
				}
			}
			else
			{
				return getActionPermissionsErrorStatus(operation);
			}
		}
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String uningestProtected(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			defaultValidation(attributeMap);
			dumpPayload(attributeMap);
			return okStatus;
		}
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String protect(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
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
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String unprotect(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
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
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String delete(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			PrivilegedOperations operation = PrivilegedOperations.DELETE;
			dumpPayload(attributeMap);
			defaultValidation(attributeMap);
			String houseID =attributeMap.getAttributeAsString(Attribute.HOUSE_ID);

			boolean permission = userCanPerformOperation(operation, attributeMap);

			if (permission)
			{
				return getConfirmStatus(String.format("Are you sure you wish to delete item %s",houseID));
			}
			else
			{
				return getActionPermissionsErrorStatus(operation);
			}
		}
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}



	@Override
	public String proxyfileCheck(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			dumpPayload(attributeMap);
			defaultValidation(attributeMap);
			return okStatus;
		}
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String ingest(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			PrivilegedOperations operation = PrivilegedOperations.INGEST;
			dumpPayload(attributeMap);
			defaultValidation(attributeMap);

			boolean permission = userCanPerformOperation(operation, attributeMap);

			if (permission)
			{
				String houseID = attributeMap.getAttribute(Attribute.HOUSE_ID).toString();
				return mapper.serialize(doesTaskExist(houseID, MayamTaskListType.INGEST));
			}
			else
			{
				return getTaskPermissionErrorStatus(operation);
			}
		}
		catch (RemoteException e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String complianceEdit(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			PrivilegedOperations operation = PrivilegedOperations.COMPLIANCE_EDITING;
			dumpPayload(attributeMap);
			defaultValidation(attributeMap);

			boolean permission = userCanPerformOperation(operation, attributeMap);

			if (permission)
			{
				String houseID = attributeMap.getAttribute(Attribute.HOUSE_ID).toString();
				String sourceHouseID = (String) attributeMap.getAttribute(Attribute.SOURCE_HOUSE_ID);

				AttributeMap returnMap = doesTaskExist(houseID, MayamTaskListType.COMPLIANCE_EDIT);
				if (returnMap != null)
				{
					String status = returnMap.getAttribute(Attribute.OP_STAT).toString();
					if (status.equals(StatusCodes.OK.toString()))
					{
						if (sourceHouseID == null || sourceHouseID.equals(""))
						{
							returnMap.clear();
							returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR.toString());
							returnMap.setAttribute(
									Attribute.ERROR_MSG,
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

				return mapper.serialize(returnMap);
			}
			else
			{
				return getTaskPermissionErrorStatus(operation);
			}
		}
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String complianceLogging(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			PrivilegedOperations operation = PrivilegedOperations.COMPLIANCE_LOGGING;
			dumpPayload(attributeMap);
			defaultValidation(attributeMap);

			boolean permission = userCanPerformOperation(operation, attributeMap);

			if (permission)
			{
				String houseID = attributeMap.getAttribute(Attribute.HOUSE_ID).toString();
				String sourceHouseID = (String) attributeMap.getAttribute(Attribute.SOURCE_HOUSE_ID);

				AttributeMap returnMap = doesTaskExist(houseID, MayamTaskListType.COMPLIANCE_LOGGING);
				if (returnMap != null)
				{
					String status = returnMap.getAttributeAsString(Attribute.OP_STAT);
					if (status.equals(StatusCodes.OK.toString()))
					{
						if (sourceHouseID == null || sourceHouseID.equals(""))
						{
							returnMap.clear();
							returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR.toString());
							returnMap.setAttribute(
									Attribute.ERROR_MSG,
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

				return  mapper.serialize(returnMap);
			}
			else
			{
				return getTaskPermissionErrorStatus(operation);
			}
		}
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String unmatched(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			PrivilegedOperations operation = PrivilegedOperations.MATCHING;
			dumpPayload(attributeMap);
			defaultValidation(attributeMap);

			boolean permission = userCanPerformOperation(operation, attributeMap);

			if (permission)
			{
				String houseID = attributeMap.getAttribute(Attribute.HOUSE_ID).toString();
				return mapper.serialize(doesTaskExist(houseID, MayamTaskListType.UNMATCHED_MEDIA));
			}
			else
			{
				return getTaskPermissionErrorStatus(operation);
			}
		}
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String preview(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			PrivilegedOperations operation = PrivilegedOperations.PREVIEW;
			dumpPayload(attributeMap);
			defaultValidation(attributeMap);

			boolean permission = userCanPerformOperation(operation, attributeMap);

			if (permission)
			{
				String houseID = attributeMap.getAttribute(Attribute.HOUSE_ID).toString();
				return mapper.serialize(doesTaskExist(houseID, MayamTaskListType.PREVIEW));
			}
			else
			{
				return getTaskPermissionErrorStatus(operation);
			}
		}
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String autoQC(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			PrivilegedOperations operation = PrivilegedOperations.AUTOQC;
			dumpPayload(attributeMap);
			defaultValidation(attributeMap);

			boolean permission = userCanPerformOperation(operation, attributeMap);

			if (permission)
			{
				String houseID = attributeMap.getAttribute(Attribute.HOUSE_ID).toString();
				return mapper.serialize(doesTaskExist(houseID, MayamTaskListType.QC_VIEW));
			}
			else
			{
				return getTaskPermissionErrorStatus(operation);
			}
		}
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}
	

	@Override
	public String qcParallel(String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			PrivilegedOperations operation = PrivilegedOperations.QCPARALLEL;
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
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String fileHeaderVerifyOverride(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			PrivilegedOperations operation = PrivilegedOperations.FILEVERIFYOVERRIDE;
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
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}

	}

	@Override
	public String txDelivery(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			PrivilegedOperations operation = PrivilegedOperations.TX_DELIVERY;
			dumpPayload(attributeMap);
			defaultValidation(attributeMap);

			boolean permission = userCanPerformOperation(operation, attributeMap);


			if (permission)
			{
				String houseID = attributeMap.getAttribute(Attribute.HOUSE_ID).toString();

				AttributeMap txTask = doesTaskExist(houseID, MayamTaskListType.TX_DELIVERY);
				String txExists = txTask.getAttributeAsString(Attribute.OP_STAT);

				if (txExists.equals(StatusCodes.ERROR.toString()))
				{ // there is a task already.  Don't create another.

					return mapper.serialize(txTask);

				}

				QcStatus qcStatus = attributeMap.getAttribute(Attribute.QC_STATUS);

				if (qcStatus == QcStatus.PASS || qcStatus == QcStatus.PASS_MANUAL)
				{
					return getConfirmStatus("Send to TX?");
				}

				boolean qcParallel = Boolean.getBoolean(attributeMap.getAttribute(Attribute.QC_PARALLEL_ALLOWED).toString());

				if (qcParallel)
				{
					return getConfirmStatus("QC Parallel is set. QC is not Passed. Do you wish to create a Tx task?");
				}

				boolean qcRequired = Boolean.getBoolean(attributeMap.getAttribute(Attribute.QC_STATUS).toString());

				if (qcRequired)
				{
					return getErrorStatus("Asset has not passed QC.");
				}
				else
				{
					return getConfirmStatus("Send to TX?");
				}

			}
			else
			{
				return getTaskPermissionErrorStatus(operation);
			}
		}
		catch (RemoteException e)
		{
			return getErrorStatus(e.getMessage());
		}
	}



	@Override
	public String exportMarkers(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			PrivilegedOperations operation = PrivilegedOperations.EXPORT_MARKERS;
			dumpPayload(attributeMap);
			defaultValidation(attributeMap);

			boolean permission = userCanPerformOperation(operation, attributeMap);

			if (permission)
			{
				return  okStatus;
			}
			else
			{
				return getTaskPermissionErrorStatus(operation);
			}
		}
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String complianceProxy(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
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
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String captionsProxy(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
			PrivilegedOperations operation = PrivilegedOperations.CAPTIONS_PROXY;
			dumpPayload(attributeMap);
			defaultValidation(attributeMap);

			boolean permission = userCanPerformOperation(operation, attributeMap);

			if (permission)
			{
				return  okStatus;
			}
			else
			{
				return getTaskPermissionErrorStatus(operation);
			}
		}
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	@Override
	public String publicityProxy(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);
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
		catch (Exception e)
		{
			return getErrorStatus(e.getMessage());
		}
	}

	private String getTaskPermissionErrorStatus(PrivilegedOperations operation)
	{
		AttributeMap permissionErrorStatus =client.createAttributeMap();

		permissionErrorStatus.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR.toString());
		permissionErrorStatus.setAttribute(Attribute.ERROR_MSG, String.format(permissionErrorTaskMessage, operation.toString()));

		return  mapper.serialize(permissionErrorStatus);
	}
	
	private String getActionPermissionsErrorStatus(PrivilegedOperations operation)
	{
		AttributeMap permissionErrorStatus =client.createAttributeMap();

		permissionErrorStatus.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR.toString());
		permissionErrorStatus.setAttribute(Attribute.ERROR_MSG, String.format(permissionErrorActionMessage, operation.toString()));

		return  mapper.serialize(permissionErrorStatus);
	}

	private AttributeMap doesTaskExist(String houseID, MayamTaskListType task)
	{
		AttributeMap returnMap =client.createAttributeMap();
		
		returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.OK.toString());

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
			returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR.toString());
			returnMap.setAttribute(Attribute.ERROR_MSG, String.format(messageTaskTechnical, houseID));
		}

		if (result != null && result.getTotalMatches() > 0)
		{
			logger.debug("found tasks");
			returnMap.clear();
			returnMap.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR.toString());
			returnMap.setAttribute(Attribute.ERROR_MSG, String.format(messageTaskMultiple, task.getText()));
		}

		logger.debug("found no tasks");
		return returnMap;
	}

	final EnumSet<TaskState> END_STATES = EnumSet.of(
			TaskState.FINISHED,
			TaskState.FINISHED_FAILED,
			TaskState.REJECTED,
			TaskState.REMOVED);

	private void dumpPayload(final AttributeMap attributeMap)
	{
		if (attributeMap == null)
		{
			logger.info("Payload is null");
		}
		else if (attributeMap.getAttributeSet().isEmpty())
		{
			logger.info("There is not data in the received map");
		}
		else
		{
			for (Attribute key : attributeMap.getAttributeSet())
			{
				logger.info(key + "<->" + attributeMap.getAttributeAsString(key));
			}
		}
	}

	private String getUser(AttributeMap attributeMap)
	{
		return attributeMap.getAttributeAsString(Attribute.TASK_UPDATED_BY);
	}

	private boolean isAo(AttributeMap attributeMap)
	{

		try
		{
			Object o = attributeMap.getAttribute(Attribute.CONT_RESTRICTED_MATERIAL);

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

	private void defaultValidation(AttributeMap attributeMap)
	{
		validateAttributeMap(
				attributeMap,
				Attribute.HOUSE_ID,
				Attribute.ASSET_TYPE,
				Attribute.TASK_UPDATED_BY);
	}

	/**
	 * 
	 * @param attributeMap
	 *            the incoming Mayam Attribute map
	 * @param attributesRequired
	 *            the required fields that must be set in this attribute map.
	 * @return true if all the attribute names in attributeNamesRequired are in the asset map.
	 */
	private boolean validateAttributeMap(AttributeMap attributeMap, Attribute... attributesRequired)
	{
		if (attributeMap == null || attributeMap.getAttributeSet().isEmpty())
			throw new WebServiceException("MAYAM Attribute Map is EMPTY");

		if (attributesRequired == null || attributesRequired.length == 0)
			throw new IllegalArgumentException("MayamPDPSetUp Internal Error: There must be some input map names from Mayam");

		for (Attribute a : attributesRequired)
		{
			if (!attributeMap.getAttributeSet().contains(a))
				throw new WebServiceException("Mayam Attribute " + a + " must be set in this call to the PDP");
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

	private boolean userCanPerformOperation(final PrivilegedOperations operation, final AttributeMap attributeMap)
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

	private String getErrorStatus(String msg)
	{

		AttributeMap errorStatus = new AttributeMap();
		errorStatus.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR.toString());
		errorStatus.setAttribute(Attribute.ERROR_MSG, msg);

		logger.info("Return Map : " + mapper.serialize(errorStatus));
		return mapper.serialize(errorStatus);

	}


	private String getConfirmStatus(String message)
	{
		AttributeMap attributeMap = client.createAttributeMap();
		attributeMap.setAttribute(Attribute.OP_STAT, StatusCodes.CONFIRM.toString());
		attributeMap.setAttribute(Attribute.FORM_MSG_NOTE, message);
		return mapper.serialize(attributeMap);
	}





}
