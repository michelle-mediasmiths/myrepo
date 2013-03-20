package com.mediasmiths.mayam.controllers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.FilterCriteria.SortOrder;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.FilterResult;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.LogUtil;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamPreviewResults;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.accessrights.MayamAccessRightsController;
import com.mediasmiths.mayam.util.AssetProperties;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamTaskController extends MayamController
{
	private final Logger log = Logger.getLogger(MayamTaskController.class);

	public static final EnumSet<TaskState> END_STATES = EnumSet.of(TaskState.FINISHED,
	                                                               TaskState.FINISHED_FAILED,
	                                                               TaskState.REJECTED,
	                                                               TaskState.REMOVED);


	private final TasksClient client;

	public TasksClient getTasksClient()
	{
		return client;
	}

	@Inject
	private final MayamAccessRightsController accessRightsController;

	@Inject
	public MayamTaskController(@Named(SETUP_TASKS_CLIENT) TasksClient mayamClient, MayamAccessRightsController rightsController)
	{
		client = mayamClient;
		accessRightsController = rightsController;
	}
	
	
	public void createIngestTaskForMaterial(String materialID, Date requiredByDate) throws MayamClientException
	{
		log.info(String.format("Creating ingest task for asset " + materialID));

		List<AttributeMap> openTasksForAsset = getOpenTasksForAsset(
				MayamTaskListType.INGEST,
				MayamAssetType.MATERIAL.getAssetType(),
				Attribute.HOUSE_ID,
				materialID);
		if (openTasksForAsset.isEmpty())
		{
			log.debug("no unclosed ingest tasks for asset");

			AttributeMap initialAttributes = client.createAttributeMap();
			initialAttributes.setAttribute(Attribute.COMPLETE_BY_DATE, requiredByDate);
			initialAttributes.setAttribute(Attribute.QC_STATUS, QcStatus.TBD);
			createTask(materialID, MayamAssetType.MATERIAL, MayamTaskListType.INGEST, initialAttributes);
		}
		else
		{
			log.info("There is already at least one unclosed ingest task for asset, will not create another");

			if (requiredByDate != null)
			{
				log.info("updating requiredby date on existing task(s)");
				for (AttributeMap task : openTasksForAsset)
				{
					AttributeMap updateMapForTask = updateMapForTask(task);
					updateMapForTask.setAttribute(Attribute.COMPLETE_BY_DATE, requiredByDate);
					saveTask(updateMapForTask);
				}
			}
		}
	}
	
	public void createQCTaskForMaterial(String materialID, Date requiredByDate, String previewStatus, AttributeMap material) throws MayamClientException
	{
		if(!MayamPreviewResults.isPreviewPass(previewStatus) && ! AssetProperties.isQCPassed(material)){
		
			log.info(String.format("Creating qc task for asset "+materialID));
		
			AttributeMap initialAttributes = client.createAttributeMap();
			initialAttributes.setAttribute(Attribute.QC_SUBSTATUS1, QcStatus.TBD);
			initialAttributes.setAttribute(Attribute.QC_SUBSTATUS1_NOTES, "");
			initialAttributes.setAttribute(Attribute.QC_SUBSTATUS2, QcStatus.TBD);
			initialAttributes.setAttribute(Attribute.QC_SUBSTATUS2_NOTES, "");
			initialAttributes.setAttribute(Attribute.QC_SUBSTATUS3, QcStatus.TBD);
			initialAttributes.setAttribute(Attribute.QC_SUBSTATUS3_NOTES, "");
			initialAttributes.setAttribute(Attribute.QC_STATUS, QcStatus.TBD); //reset qc status when creating new task
			initialAttributes.setAttribute(Attribute.COMPLETE_BY_DATE, requiredByDate);
			createTask(materialID, MayamAssetType.MATERIAL, MayamTaskListType.QC_VIEW, initialAttributes);
			
//			if(AssetProperties.isQCParallel(material)){
//				log.info("Item was marked as qc parallel, creating preview task");
//				createPreviewTaskForMaterial(materialID);
//			}
//			
		}
		else{
			log.info(String.format("Did not create qc task for asset %s as preview already passed or qc already passed",materialID));
		}
	}
	
	public long createComplianceLoggingTaskForMaterial(String materialID,String parentAssetID, Date requiredByDate) throws MayamClientException
	{
		
		log.info(String.format("Creating compliance logging task for asset "+materialID+" whose source material has asset id "+parentAssetID));
		
		AttributeMap initialAttributes = client.createAttributeMap();
		initialAttributes.setAttribute(Attribute.ASSET_PEER_ID, parentAssetID);
		initialAttributes.setAttribute(Attribute.COMPLETE_BY_DATE, requiredByDate);
		return createTask(materialID, MayamAssetType.MATERIAL, MayamTaskListType.COMPLIANCE_LOGGING, initialAttributes);

	}
	
	public long createPreviewTaskForMaterial(String materialID, Date requiredByDate) throws MayamClientException{
		
		log.info(String.format("Creating preview task for asset "+materialID));		
		AttributeMap initialAttributes = client.createAttributeMap();
		initialAttributes.setAttribute(Attribute.QC_PREVIEW_RESULT, MayamPreviewResults.PREVIEW_NOT_DONE);
		
		if(requiredByDate != null){
			initialAttributes.setAttribute(Attribute.COMPLETE_BY_DATE, requiredByDate);
		}
		
		return createTask(materialID, MayamAssetType.MATERIAL, MayamTaskListType.PREVIEW,initialAttributes);
	}
	
	public long createFixStictchTaskForMaterial(String materialID, Date requiredByDate) throws MayamClientException{
		
		log.info(String.format("Creating preview task for asset "+materialID));		
		AttributeMap initialAttributes = client.createAttributeMap();
		
		if(requiredByDate != null){
			initialAttributes.setAttribute(Attribute.COMPLETE_BY_DATE, requiredByDate);
		}
		
		return createTask(materialID, MayamAssetType.MATERIAL, MayamTaskListType.FIX_STITCH_EDIT,initialAttributes);
	}
	
	public void createOrUpdatePurgeCandidateTaskForAsset(MayamAssetType assetType, String siteID, int numberOfDays) throws MayamClientException{
		
		log.info(String.format("Creating purge candidate task for asset "+siteID));
		
		List<AttributeMap> openTasksForAsset = getOpenTasksForAsset(MayamTaskListType.PURGE_CANDIDATE_LIST, assetType.getAssetType(), Attribute.HOUSE_ID, siteID);
		
		Calendar date = Calendar.getInstance();
		date.add(Calendar.DAY_OF_MONTH, numberOfDays);
		
		if(openTasksForAsset.isEmpty()){
			
			log.info("no existing purge candidate tasks found, creating");
			AttributeMap initialAttributes = client.createAttributeMap();
			initialAttributes.setAttribute(Attribute.OP_DATE, date.getTime());
			initialAttributes.setAttribute(Attribute.TASK_STATE, TaskState.PENDING);
			
			createTask(siteID,assetType,MayamTaskListType.PURGE_CANDIDATE_LIST,initialAttributes);
		}
		else{
			log.info("existing tasks found, updating");
			
			if(openTasksForAsset.size() > 1){
				log.warn("more than the expected number of purge candidate tasks for asset "+siteID);				
			}
			
			//update
			for (AttributeMap task : openTasksForAsset)
			{
				AttributeMap updateMap = updateMapForTask(task);
				updateMap.setAttribute(Attribute.OP_DATE, date.getTime());
				updateMap.setAttribute(Attribute.TASK_STATE, TaskState.PENDING);
				saveTask(updateMap);
			}				
		}			
	}

	
	public long createUnmatchedTaskForMaterial(String assetID) throws MayamClientException{
		//Add to Unmatched worklist
		log.info(String.format("Creating unmatched task for asset "+assetID));
		return createTask(assetID, MayamAssetType.MATERIAL, MayamTaskListType.UNMATCHED_MEDIA);
	}
	
	public long createErrorTXDeliveryTaskForPackage(String presentationID, String errorMessage) throws MayamClientException{
		log.info(String.format("Creating tx delivery task task for package "+presentationID));
		AttributeMap initialAttributes = client.createAttributeMap();
		initialAttributes.setAttribute(Attribute.TX_READY, Boolean.FALSE);
		initialAttributes.setAttribute(Attribute.ERROR_MSG, errorMessage);	
		initialAttributes.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
		
		return createTask(presentationID, MayamAssetType.PACKAGE, MayamTaskListType.TX_DELIVERY, initialAttributes);		
	}
			
	public long createTXDeliveryTaskForPackage(String presentationID, boolean requireAutoQC) throws MayamClientException{
		 		
		log.info(String.format("Creating tx delivery task task for package "+presentationID+" qcrequired: "+requireAutoQC));

		 AttributeMap initialAttributes = client.createAttributeMap();
		 initialAttributes.setAttribute(Attribute.TX_READY, Boolean.TRUE);
		initialAttributes.setAttribute(Attribute.QC_REQUIRED, requireAutoQC);
		 return createTask(presentationID, MayamAssetType.PACKAGE, MayamTaskListType.TX_DELIVERY, initialAttributes);	
	}

	
	public long createExportTask(String materialID,AttributeMap exportButtonAttributes, AttributeMap materialAttributes, String jobType) throws MayamClientException
	{
			
		AttributeMap initialAttributes = client.createAttributeMap();
		initialAttributes.setAttribute(Attribute.OP_FILENAME,exportButtonAttributes.getAttribute(Attribute.OP_FILENAME));
		initialAttributes.setAttribute(Attribute.VISUAL_BUG,exportButtonAttributes.getAttribute(Attribute.VISUAL_BUG));
		initialAttributes.setAttribute(Attribute.VISUAL_TIMECODE_POSITION,exportButtonAttributes.getAttribute(Attribute.VISUAL_TIMECODE_POSITION));
		initialAttributes.setAttribute(Attribute.VISUAL_TIMECODE_COLOR,exportButtonAttributes.getAttribute(Attribute.VISUAL_TIMECODE_COLOR));
		initialAttributes.setAttribute(Attribute.OP_TYPE, jobType);
		initialAttributes.setAttribute(Attribute.TASK_CREATED_BY,exportButtonAttributes.getAttribute(Attribute.TASK_CREATED_BY));
		
		MayamAssetType assetType = MayamAssetType.MATERIAL;
		return createTask(materialID,assetType, MayamTaskListType.EXTENDED_PUBLISHING,initialAttributes);
	}
	

	public void createPendingTxPackage(String materialID,String presentationID, Date firstTx, SegmentList txpackage) throws MayamClientException{
		
		log.info(String.format("Creating pending tx package task for package %s material %s", presentationID,materialID));
		
		 AttributeMap initialAttributes = client.createAttributeMap();
		 initialAttributes.setAttribute(Attribute.AUX_EXTIDSTR, presentationID);
		 initialAttributes.setAttribute(Attribute.EVENT_DATE, firstTx);
		 initialAttributes.setAttribute(Attribute.SEGMENTATION_LIST, txpackage);
		
		 createTask(materialID, MayamAssetType.MATERIAL, MayamTaskListType.PENDING_TX_PACKAGE, initialAttributes);
	}
	
	public long createWFEErorTask(MayamAssetType type, String siteId, String message)throws MayamClientException
	{
		AttributeMap asset = null;
		try
		{
			asset = client.assetApi().getAssetBySiteId(type.getAssetType(), siteId);
		}
		catch (RemoteException e1)
		{
			log.error("error fetching asset",e1);
			throw new MayamClientException(MayamClientErrorCode.ASSET_FIND_FAILED,e1);
		}
		
		if (asset == null)
		{
			log.error("error fetching asset " + siteId);
			throw new MayamClientException(MayamClientErrorCode.ASSET_FIND_FAILED);
		}
		
		String assetID = asset.getAttribute(Attribute.ASSET_ID);
		
		AttributeMap initialAttributes = client.createAttributeMap();
		initialAttributes.setAttribute(Attribute.ASSET_SITE_ID, siteId);
		initialAttributes.setAttribute(Attribute.ASSET_ID, assetID);
		initialAttributes.setAttribute(Attribute.ERROR_MSG, message);
		initialAttributes.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
		initialAttributes.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.WFE_ERROR.getText());
		AttributeMap createTask;
		try
		{
			createTask = client.taskApi().createTask(initialAttributes);
		}
		catch (RemoteException e)
		{
			log.error(String.format("failed to create wfe error task ID {%s} message {%s}", siteId, message),e);
			throw new MayamClientException(MayamClientErrorCode.TASK_CREATE_FAILED,e);
		}
		return createTask.getAttribute(Attribute.TASK_ID);
	}
	
	// creates an error task for some situation where there is no underly asset to create the task for
	public long createWFEErrorTaskNoAsset(String id, String title, String message) throws MayamClientException{
		
		AttributeMap initialAttributes = client.createAttributeMap();
		initialAttributes.setAttribute(Attribute.ASSET_SITE_ID, id);
		initialAttributes.setAttribute(Attribute.ASSET_TITLE, title);
		initialAttributes.setAttribute(Attribute.ERROR_MSG, message);
		initialAttributes.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
		initialAttributes.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.WFE_ERROR.getText());
		AttributeMap createTask;
		try
		{
			createTask = client.taskApi().createTask(initialAttributes);
		}
		catch (RemoteException e)
		{
			log.error(String.format("failed to create wfe error task ID {%s} Title {%s} message {%s}", id, title,message),e);
			throw new MayamClientException(MayamClientErrorCode.TASK_CREATE_FAILED,e);
		}
		return createTask.getAttribute(Attribute.TASK_ID);
	}

	public long createTask(String houseID, MayamAssetType assetType, MayamTaskListType taskList, AttributeMap initialAttributes)
			throws MayamClientException
	{

		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;

		AttributeMap assetAttributes = null;
		try
		{
			if (assetType.equals(MayamAssetType.PACKAGE))
			{
				SegmentList segment = client.segmentApi().getSegmentListBySiteId(houseID);
				assetAttributes=segment.getAttributeMap();
			}
			else
			{
				assetAttributes = client.assetApi().getAssetBySiteId(assetType.getAssetType(), houseID);
			}
		}
		catch (RemoteException e)
		{
			log.error("Exception thrown by Mayam while attempting to find asset with ID: " + houseID, e);
			throw new MayamClientException(MayamClientErrorCode.MAYAM_EXCEPTION, e);
		}

		if (assetAttributes != null)
		{

			String assetID = assetAttributes.getAttributeAsString(Attribute.ASSET_ID);

			attributesValid &= attributes.setAttribute(Attribute.ASSET_TYPE, assetType.getAssetType());
			attributesValid &= attributes.setAttribute(Attribute.ASSET_ID, assetID);
			attributesValid &= attributes.setAttribute(Attribute.HOUSE_ID, houseID);
			attributesValid &= attributes.setAttribute(Attribute.TASK_LIST_ID, taskList.getText());
			attributesValid &= attributes.setAttribute(Attribute.TASK_STATE, TaskState.OPEN);
			attributes.copyAttributes(initialAttributes);

			if (!attributesValid)
			{
				log.warn("Task created but one or more attributes was invalid");
			}
			try
			{
				AttributeMap newTask = attributes.getAttributes();

				log.debug("Creating task :" + LogUtil.mapToString(newTask));

				AttributeMap task = client.taskApi().createTask(newTask);

				Long taskID = task.getAttribute(Attribute.TASK_ID);

				if (taskID == null)
				{
					log.error("created task had null TASK_ID");
					throw new MayamClientException(MayamClientErrorCode.FAILURE);
				}

				return taskID.longValue();

			}
			catch (RemoteException e)
			{
				log.error("Exception thrown by Mayam while attempting to create task", e);
				throw new MayamClientException(MayamClientErrorCode.MAYAM_EXCEPTION, e);
			}

		}
		else
		{
			log.warn("Failed to find asset with ID: " + houseID);
			throw new MayamClientException(MayamClientErrorCode.ASSET_FIND_FAILED);
		}
	}

	public long createTask(String siteID, MayamAssetType assetType, MayamTaskListType taskList) throws MayamClientException
	{
		return createTask(siteID, assetType, taskList, new AttributeMap());
	}

	public MayamClientErrorCode deleteTask(long taskID) throws MayamClientException
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		try
		{
			client.taskApi().deleteTask(taskID);
		}
		catch (RemoteException e)
		{
			log.error("Error deleting task : " + taskID, e);
			returnCode = MayamClientErrorCode.TASK_DELETE_FAILED;
		}
		return returnCode;
	}

	public AttributeMap getOnlyTaskForAssetBySiteID(MayamTaskListType type, String siteid) throws MayamClientException
	{
		return getTaskForAssetByID(type, siteid, Attribute.HOUSE_ID,1);

	}
	
	public AttributeMap getOnlyOpenTaskForAssetBySiteID(MayamTaskListType type, String siteid) throws MayamClientException
	{
		return getOpenTaskForAssetByID(type, siteid, Attribute.HOUSE_ID,1);

	}

	public AttributeMap getOnlyTaskForAssetByAssetID(MayamTaskListType type, String assetId) throws MayamClientException
	{
		return getOpenTaskForAssetByID(type, assetId, Attribute.ASSET_ID,1);

	}
	
	public AttributeMap getOnlyOpenTaskForAssetByAssetID(MayamTaskListType type, String assetId) throws MayamClientException
	{
		return getOpenTaskForAssetByID(type, assetId, Attribute.ASSET_ID,1);

	}

	private AttributeMap getTaskForAssetByID(MayamTaskListType type, String id, Attribute idattribute, int expectedResultCount)
			throws MayamClientException
	{
		log.info(String.format(
				"Searching for task of type %s for asset %s using id attribute %s",
				type.getText(),
				id,
				idattribute.toString()));

		final FilterCriteria criteria = client.taskApi().createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(Attribute.TASK_LIST_ID, type.getText());
		criteria.getFilterEqualities().setAttribute(idattribute, id);
		criteria.getSortOrders().add(new SortOrder(Attribute.TASK_CREATED, SortOrder.Direction.DESC));
		FilterResult result;
		try
		{
			result = client.taskApi().getTasks(criteria, 100, 0);
			log.info("Total matches: " + result.getTotalMatches());

			if (result.getTotalMatches() != expectedResultCount)
			{
				log.error("unexpected number of results for search expected "+ expectedResultCount + " got " + result.getTotalMatches());
				throw new MayamClientException(MayamClientErrorCode.UNEXPECTED_NUMBER_OF_TASKS);
			}

			return result.getMatches().get(0);
		}
		catch (RemoteException e)
		{
			log.error("remote expcetion searching for task", e);
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED);
		}

	}
	
	private AttributeMap getOpenTaskForAssetByID(MayamTaskListType type, String id, Attribute idattribute, int expectedResultCount)
			throws MayamClientException
	{
		log.info(String.format(
				"Searching for task of type %s for asset %s using id attribute %s",
				type.getText(),
				id,
				idattribute.toString()));

		final FilterCriteria criteria = client.taskApi().createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(Attribute.TASK_LIST_ID, type.getText());
		criteria.getFilterEqualities().setAttribute(idattribute, id);
		criteria.getFilterAlternatives().addAsExclusions(Attribute.TASK_STATE, END_STATES);
		criteria.getSortOrders().add(new SortOrder(Attribute.TASK_CREATED, SortOrder.Direction.DESC));
		FilterResult result;
		try
		{
			result = client.taskApi().getTasks(criteria, 100, 0);
			log.info("Total matches: " + result.getTotalMatches());

			if (result.getTotalMatches() != expectedResultCount)
			{
				log.error("unexpected number of results for search expected "+ expectedResultCount + " got " + result.getTotalMatches());
				throw new MayamClientException(MayamClientErrorCode.UNEXPECTED_NUMBER_OF_TASKS);
			}

			if(result.getTotalMatches() > 0){
				return result.getMatches().get(0);
			}
			else{
				return null;
			}
		}
		catch (RemoteException e)
		{
			log.error("remote expcetion searching for task", e);
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED);
		}

	}

	public void saveTask(AttributeMap task) throws MayamClientException
	{
		try
		{
			client.taskApi().updateTask(task);
		}
		catch (RemoteException e)
		{
			log.error("remote expcetion saving task", e);
			throw new MayamClientException(MayamClientErrorCode.TASK_UPDATE_FAILED);
		}
	}

	public AttributeMap getTask(long taskId) throws RemoteException
	{
		return client.taskApi().getTask(taskId);
	}

	public void autoQcFailedForMaterial(final String materialId, final long taskID) throws MayamClientException
	{
		log.debug(String.format("Auto QC task failed for materialId %s; setting task state to fail", materialId));
		AttributeMap qcTask;
		try
		{
			qcTask = getTask(taskID);
		}
		catch (RemoteException e)
		{
			log.error(String.format("Error fetching qc task %d for material %s ",taskID,materialId,e));
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED);
		}
		
		qcTask.setAttribute(Attribute.ERROR_MSG, String.format("Auto QC failed for material with id %s", materialId));
		qcTask.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
		qcTask.setAttribute(Attribute.QC_SUBSTATUS2, QcStatus.FAIL);
		
		try
		{
			log.debug("Saving failedQcTask");
			saveTask(qcTask);
		}
		catch (MayamClientException e)
		{
			log.error("Error updating qc task for material "+ materialId, e);
			throw e;
		}		
	}


	public void autoQcPassedForMaterial(String materialId, long taskID) throws MayamClientException
	{
		setAutoQcStatus(QcStatus.PASS, materialId, taskID);		
	}
	
	private void setAutoQcStatus(QcStatus newStatus, String materialId, long taskID) throws MayamClientException
	{
		AttributeMap qcTask;
		try
		{
			qcTask = getTask(taskID);
		}
		catch (RemoteException e)
		{
			log.error(String.format("Error fetching qc task %d for material %s ",taskID,materialId,e));
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED);
		}

		qcTask.setAttribute(Attribute.QC_SUBSTATUS2, newStatus);
		
		try
		{
			saveTask(qcTask);
		}
		catch (MayamClientException e)
		{
			log.error("error updating qc task for material "+materialId,e);
			throw e;
		}		
	}


	public AttributeMap updateMapForAsset(AttributeMap assetAttributes){
		AttributeMap updateMap = client.createAttributeMap();
		updateMap.setAttribute(Attribute.ASSET_TYPE, assetAttributes.getAttribute(Attribute.ASSET_TYPE));
		updateMap.setAttribute(Attribute.ASSET_ID, assetAttributes.getAttribute(Attribute.ASSET_ID));
		return updateMap;
	}	
	
	public AttributeMap updateMapForTask(AttributeMap taskAttributes){
		AttributeMap updateMap = client.createAttributeMap();
		updateMap.setAttribute(Attribute.ASSET_TYPE, taskAttributes.getAttribute(Attribute.ASSET_TYPE));
		updateMap.setAttribute(Attribute.ASSET_ID, taskAttributes.getAttribute(Attribute.ASSET_ID));
		updateMap.setAttribute(Attribute.TASK_ID, taskAttributes.getAttribute(Attribute.TASK_ID));
		return updateMap;
	}


	public void setTaskToErrorWithMessage(long taskID, String message)
	{
		log.info(String.format("Failing task %d with error message {%s}",taskID,message));
		
		AttributeMap task;
		try
		{
			task = getTask(taskID);
		}
		catch (RemoteException e)
		{
			log.error("error fetching tasks to with intent of marking it as failed",e);
			return;
		}
		AttributeMap updateMapForTask = updateMapForTask(task);
		updateMapForTask.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
		updateMapForTask.setAttribute(Attribute.ERROR_MSG, message);
		try
		{
			saveTask(updateMapForTask);
		}
		catch (MayamClientException e)
		{
			log.error("error setting task to failed state",e);
			return;
		}
	}	
	
	/**
	 * Returns tasks for item
	 * @param taskList
	 * @param assetType
	 * @param idAttribute
	 * @param id
	 * @return
	 * @throws MayamClientException
	 */
	public List<AttributeMap> getTasksForAsset(MayamTaskListType taskList, AssetType assetType, Attribute idAttribute, String id) throws MayamClientException{
		
		log.info(String.format(
				"Searching for task of type %s for asset %s using id attribute %s",
				taskList.getText(),
				id,
				idAttribute.toString()));

		final FilterCriteria criteria = client.taskApi().createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(Attribute.TASK_LIST_ID, taskList.getText());
		criteria.getFilterEqualities().setAttribute(idAttribute, id);
		criteria.getSortOrders().add(new SortOrder(Attribute.TASK_CREATED, SortOrder.Direction.DESC));
		
		FilterResult result;
		try
		{
			result = client.taskApi().getTasks(criteria, 100, 0);
			log.debug("Total matches: " + result.getTotalMatches());
			return result.getMatches();
		}
		catch (RemoteException e)
		{
			log.error("remote exception searching for task", e);
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED);
		}
	}
	
	/**
	 * Returns all tasks of the specified type that are not in end states
	 * @param taskList
	 * @param assetType
	 * @param idAttribute
	 * @param id
	 * @return
	 * @throws MayamClientException
	 */
	public List<AttributeMap> getOpenTasksForAsset(MayamTaskListType taskList, AssetType assetType, Attribute idAttribute, String id) throws MayamClientException{
		
		log.info(String.format(
				"Searching for task of type %s for asset %s using id attribute %s",
				taskList.getText(),
				id,
				idAttribute.toString()));

		final FilterCriteria criteria = client.taskApi().createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(Attribute.TASK_LIST_ID, taskList.getText());
		criteria.getFilterEqualities().setAttribute(idAttribute, id);
		criteria.getFilterAlternatives()
	   		.addAsExclusions(Attribute.TASK_STATE, END_STATES);
		criteria.getSortOrders().add(new SortOrder(Attribute.TASK_CREATED, SortOrder.Direction.DESC));
		
		FilterResult result;
		try
		{
			result = client.taskApi().getTasks(criteria, 100, 0);
			log.debug("Total matches: " + result.getTotalMatches());
			return result.getMatches();
		}
		catch (RemoteException e)
		{
			log.error("remote exception searching for task", e);
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED);
		}
	}

	/**
	 * returns all tasks for an asset that are not in end states
	 * @return
	 */
	public List<AttributeMap> getAllOpenTasksForAsset( AssetType assetType, Attribute idAttribute, String id) throws MayamClientException{
		
		log.info(String.format(
				"Searching for non closed tasks of for asset %s using id attribute %s",
				id,
				idAttribute.toString()));

		final FilterCriteria criteria = client.taskApi().createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(idAttribute, id);
		criteria.getFilterAlternatives()
	   		.addAsExclusions(Attribute.TASK_STATE, END_STATES);
		
		criteria.getSortOrders().add(new SortOrder(Attribute.TASK_CREATED, SortOrder.Direction.DESC));
		
		FilterResult result;
		try
		{
			result = client.taskApi().getTasks(criteria, 100, 0);
			log.debug("Total matches: " + result.getTotalMatches());
			return result.getMatches();
		}
		catch (RemoteException e)
		{
			log.error("remote exception searching for task", e);
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED);
		}
	}
	
	public void cancelAllOpenTasksForAsset(AssetType assetType, Attribute idAttribute, String id) throws MayamClientException
	{

		List<AttributeMap> tasksForAsset = getAllOpenTasksForAsset(assetType, idAttribute, id);

		for (AttributeMap task : tasksForAsset)
		{
			log.debug(String.format("removing task %s which has state %s", task.getAttribute(Attribute.TASK_ID), task.getAttribute(Attribute.TASK_STATE)));
			task.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
			try
			{
				client.taskApi().updateTask(task);
			}
			catch (RemoteException e)
			{
				log.error("exception removing task");
				throw new MayamClientException(MayamClientErrorCode.TASK_UPDATE_FAILED);
			}
		}
	}
	
	public boolean fixAndStitchTaskExistsForItem(String itemAssetID) throws MayamClientException{
		
		List<AttributeMap> tasksForAsset = getTasksForAsset(
				MayamTaskListType.FIX_STITCH_EDIT, AssetType.ITEM,
				Attribute.ASSET_ID, itemAssetID);

		if (tasksForAsset.size() > 0) {
			return true;
		}
		return false;
	}
	
}
