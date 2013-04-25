package com.mediasmiths.foxtel.ip.mayam.pdp;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.server.AttributeMapMapper;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.AudioTrackList;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.FilterResult;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.controllers.MayamTitleController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.util.AssetProperties;
import org.apache.log4j.Logger;

import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MayamPDPImpl implements MayamPDP
{

	// Messages

	@Inject
	@Named("message.txsend")
	String messageTXSend;

	@Inject
	@Named("message.qcparallel.warning")
	String messageQCParallelWarning;

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
	@Named("message.segment.no.requested.segments")
	String messageSegmentNoRequestedNumberOfSegments;
	
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
	
	@Inject
	@Named("message.delete.prevention")
	String messageDeletePrevention;

	@Inject
	@Named("message.audio.notracks")
	String messageNoAudioTracks;


	// ---------------

	private Logger logger = Logger.getLogger(MayamPDP.class);

	private final TasksClient client;
	private final MayamTaskController taskController;

	@Inject
	private MayamMaterialController materialController;
	
	@Inject
	private MayamTitleController titleController;
	
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
			List<String> warnings = new ArrayList<String>();
			List<String> errors = new ArrayList<String>();

			logger.debug("Segment Mismatch Check.");

			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);

			defaultValidation(attributeMap);
			dumpPayload(attributeMap);

			//Segmentation check
			Integer requestedNumber = attributeMap.getAttribute(Attribute.REQ_NUMBER);
			
			if(requestedNumber==null){
				errors.add(messageSegmentNoRequestedNumberOfSegments);
			}
			
			int numberOfSegmentsRequested = requestedNumber.intValue();	
			
			String presentationID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);

			SegmentList segmentList = null;
			try 
			{
				segmentList = client.segmentApi().getSegmentListBySiteId(presentationID);
			}
			catch (Throwable e) 
			{
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

						warnings.add(messageSegmentMismatchOverride);
					}
					else
					{
						logger.debug("User cannot override a segment mismatch for " + presentationID);
						errors.add(messageSegmentMismatchRefuseOverride);
					}
				}
			}
			else
			{
				logger.info("Unable to retrieve Segment List. Return Map for  " + presentationID);
				errors.add("A technical fault has occurred while retrieving segment list");
			}

			// Classification Check
			String classification = attributeMap.getAttributeAsString(Attribute.CONT_CLASSIFICATION);
			if (classification == null || classification.equals(""))
			{
				logger.debug("The TX Package has not been classified. Please contact the channel owner and ensure that this is provided");
				errors.add("The TX Package has not been classified. Please contact the channel owner and ensure that this is provided");
			}

			// QC Parallel Check
			// if QC parallel has been enabled, and the QC status has not been set, then it will inform the user that the content has not 
			// finished auto QC yet and allow them to continue if they wish.

			AttributeMap parentAsset = null;
			try
			{
				parentAsset = client.assetApi().getAssetBySiteId(MayamAssetType.MATERIAL.getAssetType(),
				                                                              attributeMap.getAttributeAsString(Attribute.PARENT_HOUSE_ID));
			}
			catch (RemoteException e)
			{
				logger.error("Unable to find: " +  attributeMap.getAttributeAsString(Attribute.PARENT_HOUSE_ID), e);

				throw e;
			}

			AudioTrackList audioTracks = parentAsset.getAttribute(Attribute.AUDIO_TRACKS);

			if (audioTracks == null) // add a no audio track error.
			{
				errors.add(messageNoAudioTracks);
			}

			if (parentAsset != null)
			{
				boolean isQcPassed = AssetProperties.isQCPassed(parentAsset);

				logger.debug("Is QC passed " + isQcPassed);

				if (isQcPassed)
				{
					logger.info("QC is Passed on Presentation Id " + presentationID);
				}
				else
				{
					Boolean qcParallelAttribute = parentAsset.getAttribute(Attribute.QC_PARALLEL_ALLOWED);
					if (qcParallelAttribute != null && qcParallelAttribute)
					{
						logger.info("Qc Parallel is set but Qc Status has not yet been passed, warning the user: "
								+ presentationID);

						warnings.add(messageQCParallelWarning);
					}
					else
					{
						errors.add("Item is not QC passed. QC Parallel is not set  - you cannot proceed to Tx.");
					}
				}
			}
			
			if (errors.size() > 0)
			{
				errors.addAll(warnings); //include warnings in the error message				
				return getErrorStatus(formHTMLList(errors));
			}
			else
			{
				warnings.add(messageTXSend);
				return getConfirmStatus(formHTMLList(warnings));
			}
			
		}
		catch (Exception e)
		{
			logger.error("PDP Comms Error:  unable to retrieve required data to complete Segmentation operation, contact support", e);

			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete Segmentation operation, contact support");
		}
		
		
		
	}


	@Override
	public String segmentation(final String attributeMapStr)
	{
		try
		{
			final AttributeMap attributeMap = mapper.deserialize(attributeMapStr);

			defaultValidation(attributeMap);

			String assetId = attributeMap.getAttributeAsString(Attribute.ASSET_ID);
			AssetType assetType = attributeMap.getAttribute(Attribute.ASSET_TYPE);

			List<AttributeMap> existingTasks = taskController.getAllOpenTasksForAsset(assetType, Attribute.ASSET_ID, assetId);

			if (existingTasks != null && existingTasks.size() > 0)
			{
				String presentationID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);

				logger.error(String.format("%d open tasks for asset Id = %s", existingTasks.size(), assetId));

				return getErrorStatus("An open segmentation task already exists for asset :" + presentationID);
			}
			else
			{
				return okStatus;
			}

		}
		catch (Exception e)
		{
			logger.error("PDP Comms Error:  unable to retrieve required data to complete Segmentation operation", e);

			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete Segmentation operation");
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
			logger.error("PDP Comms Error:  unable to retrieve required data to complete uningest operation", e);

			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete uningest operation");
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
			logger.error("PDP Comms Error:  unable to retrieve required data to complete uningestProtected operation", e);

			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete uningestProtected operation");
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
			logger.error("PDP Comms Error:  unable to retrieve required data to complete protect operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete protect operation");
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
				String assetID=null;

				AssetType assetType = attributeMap.getAttribute(Attribute.ASSET_TYPE);

				if (assetType == MayamAssetType.MATERIAL.getAssetType())
				{

					assetID = attributeMap.getAttribute(Attribute.ASSET_ID);
					String parentAssetID = attributeMap.getAttributeAsString(Attribute.ASSET_PARENT_ID);

					if (parentAssetID != null)
					{
						logger.debug("loading parent asset : "+parentAssetID);
						AttributeMap title = client.assetApi().getAsset(MayamAssetType.TITLE.getAssetType(), parentAssetID);
						if (AssetProperties.isPurgeProtected(title))
						{
							logger.warn("Rejecting unprotect because parent is protected: "
									+ attributeMap.getAttributeAsString(Attribute.HOUSE_ID));
							return getErrorStatus("Parent title " + title.getAttributeAsString(Attribute.HOUSE_ID)
									+ " is Protected");
						}
					}
				}

				return okStatus;
			}
			else
			{
				return getActionPermissionsErrorStatus(operation);
			}
		}
		catch (Exception e)
		{
			logger.error("PDP Comms Error:  unable to retrieve required data to complete unprotect operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete unprotect operation");
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
			String houseID = attributeMap.getAttributeAsString(Attribute.HOUSE_ID);
			AssetType assetType = attributeMap.getAttribute(Attribute.ASSET_TYPE);
			boolean hasTXpackages = false;
			
			logger.debug("asset type is :"+assetType);
			
			AttributeMap assetAttribute = null;
			try
			{
				assetAttribute = client.assetApi().getAssetBySiteId(assetType,
						attributeMap.getAttributeAsString(Attribute.HOUSE_ID));
				
			}
			catch (RemoteException e)
			{
				logger.error("Unable to find: " +  attributeMap.getAttributeAsString(Attribute.HOUSE_ID), e);

				throw e;
			}
			
			String assetID = assetAttribute.getAttributeAsString(Attribute.ASSET_ID);
			
			if (MayamAssetType.MATERIAL.getAssetType().equals(assetType))
			{
				hasTXpackages = materialController.materialHasTXPackages(houseID, assetID);
			}
			else if (MayamAssetType.TITLE.getAssetType().equals(assetType))
			{
				List<AttributeMap> materialsFortitle = titleController.getMaterialsFortitle(houseID, assetID);

				for (AttributeMap material : materialsFortitle)
				{
					String materialID = material.getAttributeAsString(Attribute.HOUSE_ID);
					String materialAssetID = material.getAttributeAsString(Attribute.ASSET_ID);

					if (materialController.materialHasTXPackages(materialID, materialAssetID))
					{
						hasTXpackages = true;
						break;
					}
				}
			}
			else if (MayamAssetType.PACKAGE.getAssetType().equals(assetType))
			{
				hasTXpackages = true; // asset is a tx package
			}
			
			boolean permission = userCanPerformOperation(operation, attributeMap);

			if (permission)
			{
				if (hasTXpackages)
				{
					return getErrorStatus(messageDeletePrevention);
				} else
				{
					return getConfirmStatus(String.format("Are you sure you wish to delete item %s",houseID));
				}
				
			}
			else
			{
				return getActionPermissionsErrorStatus(operation);
			}
			
		}
		catch (Exception e)
		{
			logger.error("PDP Comms Error:  unable to retrieve required data to complete delete operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete delete operation");
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
			logger.error("PDP Comms Error:  unable to retrieve required data to complete proxyfileCheck operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete proxyfileCheck operation");
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
			logger.error("PDP Comms Error:  unable to retrieve required data to complete ingest operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete ingest operation");
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
							return getErrorStatus(String.format(messageComplianceEditNone, houseID));
						}
						else
						{
							logger.debug("parent house id was not null or empty");
							return okStatus;
						}
					}
					else
					{
						logger.debug("returned status was not ok");
						return getErrorStatus("Internal technical error: search fails for houseId: " + houseID);
					}
				}
				else
				{
					return getErrorStatus("Internal technical error: cannot find house id: " + houseID);
				}
			}
			else
			{
				return getTaskPermissionErrorStatus(operation);
			}
		}
		catch (Exception e)
		{
			logger.error("PDP Comms Error:  unable to retrieve required data to complete complianceEdit operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete complianceEdit operation");
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
							return getErrorStatus(String.format(messageComplianceLoggingNone, houseID));
						}
						else
						{
							logger.debug("parent house id was not null or empty");
							return okStatus;
						}
					}
					else
					{
						logger.debug("returned status was not ok: finding " + sourceHouseID);
						return getErrorStatus("Unable to find " + sourceHouseID + " system fault.");
					}
				}
				else
				{
					return okStatus;
				}

			}
			else
			{
				return getTaskPermissionErrorStatus(operation);
			}
		}
		catch (Exception e)
		{
			logger.error("PDP Comms Error:  unable to retrieve required data to complete complianceLogging operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete complianceLogging operation");
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
			logger.error("PDP Comms Error:  unable to retrieve required data to complete unmatched operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete unmatched operation");
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
			logger.error("PDP Comms Error:  unable to retrieve required data to complete preview operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete preview operation");
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
			logger.error("PDP Comms Error:  unable to retrieve required data to complete autoQC operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete autoQC operation");
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
			logger.error("PDP Comms Error:  unable to retrieve required data to complete qcParallel operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete qcParallel operation");
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
			logger.error("PDP Comms Error:  unable to retrieve required data to complete fileHeaderVerify operation", e);

			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete fileheaderVerify operation");
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

				String parentID = attributeMap.getAttribute(Attribute.PARENT_HOUSE_ID).toString();
				AttributeMap parentAsset = client.assetApi().getAssetBySiteId(MayamAssetType.MATERIAL.getAssetType(), parentID);
				
				if (parentAsset != null)
				{
					QcStatus qcStatus = parentAsset.getAttribute(Attribute.QC_STATUS);
	
					if (qcStatus == QcStatus.PASS || qcStatus == QcStatus.PASS_MANUAL)
					{
						return getConfirmStatus("'Pressing CONFIRM will send this media into the TX domain. Please ensure this material is segmented correctly and ready for transmission before proceeding.");
					}
	
					boolean qcParallel = Boolean.getBoolean(parentAsset.getAttribute(Attribute.QC_PARALLEL_ALLOWED).toString());
	
					if (qcParallel)
					{
						return getConfirmStatus("QC Parallel is set. QC is not Passed. Do you wish to create a Tx task?");
					}
				}
				else {
					return getConfirmStatus("Unable to locate parent asset. Do you wish to create a Tx task?");
				}
				
				boolean qcRequired = Boolean.getBoolean(attributeMap.getAttribute(Attribute.QC_REQUIRED).toString());

				if (qcRequired)
				{
					return getErrorStatus("Asset has not passed QC.");
				}
				else
				{
					return getConfirmStatus("'Pressing CONFIRM will send this media into the TX domain. Please ensure this material is segmented correctly and ready for transmission before proceeding.");
				}

			}
			else
			{
				return getTaskPermissionErrorStatus(operation);
			}
		}
		catch (RemoteException e)
		{

			logger.error("PDP Comms Error:  unable to retrieve required data to complete TXDelivery operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete TXDelivery operation");
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
			logger.error("PDP Comms Error:  unable to retrieve required data to complete exportMarkers operation", e);

			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete exportMarkers operation");
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
			logger.error("PDP Comms Error:  unable to retrieve required data to complete complianceProxy operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete complianceProxy operation");
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
			logger.error("PDP Comms Error:  unable to retrieve required data to complete captionsProxy operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete captionsProxy operation");
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
			logger.error("PDP Comms Error:  unable to retrieve required data to complete PublicityProxy operation", e);


			return getErrorStatus("PDP Comms Error:  unable to retrieve required data to complete publicityProxy operation");
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

		Set<String> userGroups = null;
		try
		{
			userGroups = client.userApi().getUserGroups(user);
		}
		catch (RemoteException e)
		{
			logger.error("Mayam failed to return any groups for " + user, e);

			return new HashSet<FoxtelGroups>();
		}

		if (userGroups == null)
		{
			logger.error("Null set of user Groups  for " + user);

			return new HashSet<FoxtelGroups>();

		}

		Set<FoxtelGroups> groups = null;
		try
		{
			groups = new HashSet<FoxtelGroups>();

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
		catch (Exception e)
		{
            logger.error("Problem forming the collection of groups: ", e);

			return new HashSet<FoxtelGroups>();
		}

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

	private String getDeletePreventionMessage(PrivilegedOperations operation)
	{
		AttributeMap permissionErrorStatus = client.createAttributeMap();

		permissionErrorStatus.setAttribute(Attribute.OP_STAT, StatusCodes.ERROR.toString());
		permissionErrorStatus.setAttribute(Attribute.ERROR_MSG, String.format(permissionErrorTaskMessage, operation.toString()));

		return  mapper.serialize(permissionErrorStatus);
		
	}



	private String formHTMLList(final List<String> warnings)
	{
		String composite="<ul>";

		for (String s : warnings)
			if (s != null && s.length() != 0)
				composite += "<li>" + s + "</li>";

		return composite + "</ul>";
	}


}
