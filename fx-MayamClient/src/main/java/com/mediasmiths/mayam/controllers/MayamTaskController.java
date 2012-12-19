package com.mediasmiths.mayam.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetAccess;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.AssetAccess.EntityType;
import com.mayam.wf.attributes.shared.type.FilterCriteria.SortOrder;
import com.mayam.wf.attributes.shared.type.GenericTable;
import com.mayam.wf.attributes.shared.type.GenericTable.Row;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.FilterResult;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mchange.v2.log.LogUtils;
import com.mediasmiths.mayam.LogUtil;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mayam.accessrights.MayamAccessRights;
import com.mediasmiths.mayam.accessrights.MayamAccessRightsController;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamTaskController extends MayamController
{
	private final TasksClient client;

	public TasksClient getTasksClient()
	{
		return client;
	}

	@Inject
	private final MayamAccessRightsController accessRightsController;

	private final Logger log = Logger.getLogger(MayamTaskController.class);

	@Inject
	public MayamTaskController(@Named(SETUP_TASKS_CLIENT) TasksClient mayamClient, MayamAccessRightsController rightsController)
	{
		client = mayamClient;
		accessRightsController = rightsController;
	}

	public long createIngestTaskForMaterial(String materialID, Date requiredByDate) throws MayamClientException
	{
		AttributeMap initialAttributes = client.createAttributeMap();
		initialAttributes.setAttribute(Attribute.COMPLETE_BY_DATE, requiredByDate);
		return createTask(materialID, MayamAssetType.MATERIAL, MayamTaskListType.INGEST, initialAttributes);

	}
	
	public long createComplianceLoggingTaskForMaterial(String materialID, Date requiredByDate) throws MayamClientException
	{
		AttributeMap initialAttributes = client.createAttributeMap();
		initialAttributes.setAttribute(Attribute.COMPLETE_BY_DATE, requiredByDate);
		return createTask(materialID, MayamAssetType.MATERIAL, MayamTaskListType.COMPLIANCE_LOGGING, initialAttributes);

	}

	public long createTask(String houseID, MayamAssetType assetType, MayamTaskListType taskList, AttributeMap initialAttributes)
			throws MayamClientException
	{

		MayamAttributeController attributes = new MayamAttributeController(client);
		boolean attributesValid = true;

		AttributeMap assetAttributes = null;
		try
		{
			assetAttributes = client.assetApi().getAssetBySiteId(assetType.getAssetType(), houseID);
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
				AttributeMap newTask;

				try
				{
					newTask = updateAccessRights(attributes.getAttributes());
				}
				catch (Exception e)
				{
					log.error("Error setting access rights on new task", e);
					newTask = attributes.getAttributes();
				}

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

	public AttributeMap getTaskForAssetBySiteID(MayamTaskListType type, String siteid) throws MayamClientException
	{
		return getTaskForAssetByID(type, siteid, Attribute.HOUSE_ID);

	}

	public AttributeMap getTaskForAssetByAssetID(MayamTaskListType type, String assetId) throws MayamClientException
	{
		return getTaskForAssetByID(type, assetId, Attribute.ASSET_ID);

	}

	private AttributeMap getTaskForAssetByID(MayamTaskListType type, String id, Attribute idattribute)
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
			result = client.taskApi().getTasks(criteria, 10, 0);
			log.info("Total matches: " + result.getTotalMatches());

			if (result.getTotalMatches() != 1)
			{
				log.error("unexpected number of results for search expected 1 got " + result.getTotalMatches());
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

	public void saveTask(AttributeMap task) throws MayamClientException
	{
		try
		{
			try
			{
				task = updateAccessRights(task);
			}
			catch (Exception e)
			{
				log.error("Error updating access rights before task save", e);
			}
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

	private AttributeMap updateAccessRights(AttributeMap task)
	{
		AssetType assetType = task.getAttribute(Attribute.ASSET_TYPE);
		String assetId = task.getAttribute(Attribute.HOUSE_ID);

		boolean purgeProtected = false;

		if (task.getAttribute(Attribute.PURGE_PROTECTED) != null)
		{
			purgeProtected = ((Boolean) task.getAttribute(Attribute.PURGE_PROTECTED)).booleanValue();
		}

		GenericTable mediaRights = task.getAttribute(Attribute.MEDIA_RIGHTS);

		ArrayList<String> channelList = new ArrayList<String>();
		if (mediaRights != null)
		{
			List<Row> rows = mediaRights.getRows();
			if (rows != null)
			{
				for (int i = 0; i < rows.size(); i++)
				{
					String channel = rows.get(i).get(5);
					channelList.add(channel);
				}
			}
		}

		String contentFormat = task.getAttributeAsString(Attribute.CONT_FMT);
		String contentCategory = task.getAttributeAsString(Attribute.CONT_CATEGORY);

		log.warn(String.format(
				"content format is %s , category is %s for asset with id %s",
				contentFormat,
				contentCategory,
				assetId));

		String contentType = null;
		if (assetType != null && assetType.equals(MayamAssetType.TITLE.getAssetType()))
		{
			contentType = "Title";
		}
		else if (contentFormat != null && contentFormat.toUpperCase().equals("UNMATCHED"))
		{
			contentType = "Unmatched";
		}
		else if (contentCategory != null)
		{
			if (contentCategory.toUpperCase().equals("PROGRAMME"))
			{
				contentType = "Programme";
			}
			else if (contentCategory.toUpperCase().equals("ASSOCIATED"))
			{
				contentType = "EPK";
			}
			else if (contentCategory.toUpperCase().equals("EDIT CLIPS"))
			{
				contentType = "Edit Clip";
			}
			else if (contentCategory.toUpperCase().equals("PUBLICITY"))
			{
				contentType = "Publicity";
			}
		}

		Boolean adultOnly = task.getAttribute(Attribute.CONT_RESTRICTED_MATERIAL);
		Boolean qcParallel = task.getAttribute(Attribute.QC_PARALLEL_ALLOWED);
		QcStatus qcStatus = task.getAttribute(Attribute.QC_STATUS);

		TaskState qaStatus = null;

		AttributeMap filterEqualities = client.createAttributeMap();
		filterEqualities.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PREVIEW.toString());
		filterEqualities.setAttribute(Attribute.HOUSE_ID, assetId);
		FilterCriteria criteria = new FilterCriteria();
		criteria.setFilterEqualities(filterEqualities);
		FilterResult existingTasks;
		try
		{
			existingTasks = client.taskApi().getTasks(criteria, 10, 0);

			if (existingTasks.getTotalMatches() > 0)
			{
				List<AttributeMap> tasks = existingTasks.getMatches();
				AttributeMap qaTask = tasks.get(0);
				qaStatus = task.getAttribute(Attribute.TASK_STATE);

				if (existingTasks.getTotalMatches() > 0)
				{
					log.warn("More than 1 Preview task found for assset : " + assetId
							+ ". Access Righst will be set based on status of first task : "
							+ qaTask.getAttribute(Attribute.TASK_ID));
				}
			}
		}
		catch (RemoteException e)
		{
			log.error("Exception thrown by Mayam while attempting to retrieve any Preview tasks for asset : " + assetId, e);
		}

		List<MayamAccessRights> allRights = accessRightsController.retrieve(
				qcStatus.toString(),
				qaStatus.toString(),
				qcParallel,
				contentType,
				channelList,
				purgeProtected,
				adultOnly);

		AssetAccess accessRights = new AssetAccess();
		for (int i = 0; i < allRights.size(); i++)
		{
			AssetAccess.ControlList.Entry entry = new AssetAccess.ControlList.Entry();
			entry.setEntityType(EntityType.GROUP);
			entry.setEntity(allRights.get(i).getGroupName());
			entry.setRead(allRights.get(i).getReadAccess());
			entry.setWrite(allRights.get(i).getWriteAccess());
			entry.setAdmin(allRights.get(i).getAdminAccess());
			accessRights.getStandard().add(entry);
		}
		task.setAttribute(Attribute.ASSET_ACCESS, accessRights);
		return task;
	}
}