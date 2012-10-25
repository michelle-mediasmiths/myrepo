package com.mediasmiths.mayam.controllers;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.FilterCriteria.SortOrder;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.FilterResult;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.std.auth.yubikey.YubicoClient;

import static com.mediasmiths.mayam.guice.MayamClientModule.SETUP_TASKS_CLIENT;

public class MayamTaskController {
	private final TasksClient client;
	private final Logger log = Logger.getLogger(MayamPackageController.class);

	@Inject
	public MayamTaskController(
			@Named(SETUP_TASKS_CLIENT) TasksClient mayamClient) {
		client = mayamClient;
	}

	public long createTask(String assetID, MayamAssetType assetType,
			MayamTaskListType taskList) throws MayamClientException {
		long taskID = 0;
		MayamAttributeController attributes = new MayamAttributeController(
				client);
		boolean attributesValid = true;

		AttributeMap assetAttributes = null;
		try {
			assetAttributes = client.getAsset(
					AssetType.valueOf(assetType.toString()), assetID);
		} catch (RemoteException e) {
			throw new MayamClientException(MayamClientErrorCode.MAYAM_EXCEPTION);
		}

		if (assetAttributes != null) {
			// TODO: Generate Task ID
			// taskID = 0;
			// attributesValid = attributesValid &&
			// attributes.setAttribute(Attribute.TASK_ID, taskID);

			attributesValid = attributesValid
					&& attributes.setAttribute(Attribute.TASK_LIST_ID,
							taskList.toString());
			attributesValid = attributesValid
					&& attributes.setAttribute(Attribute.TASK_STATE,
							TaskState.OPEN);

			attributesValid = attributesValid
					&& attributes.setAttribute(Attribute.ASSET_ID,
							taskList.toString());
			attributes.copyAttributes(assetAttributes);

			try {
				client.createTask(attributes.getAttributes());
			} catch (RemoteException e) {
				throw new MayamClientException(
						MayamClientErrorCode.MAYAM_EXCEPTION);
			}

		} else {
			throw new MayamClientException(
					MayamClientErrorCode.ASSET_FIND_FAILED);
		}
		return taskID;
	}

	public MayamClientErrorCode deleteTask(long taskID)
			throws MayamClientException {
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		try {
			client.deleteTask(taskID);
		} catch (RemoteException e) {
			log.error("Error deleting task : " + taskID);
			returnCode = MayamClientErrorCode.TASK_DELETE_FAILED;
		}
		return returnCode;
	}

	public AttributeMap getTaskForAsset(MayamTaskListType type, String id)
			throws MayamClientException {

		log.info(String.format("Searching for task of type %s for asset %s",
				type.getText(), id));

		final FilterCriteria criteria = client.createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(Attribute.TASK_LIST_ID,
				type.getText());
		criteria.getFilterEqualities().setAttribute(Attribute.ASSET_ID, id);
		criteria.getSortOrders()
				.add(new SortOrder(Attribute.TASK_CREATED,
						SortOrder.Direction.DESC));
		FilterResult result;
		try {
			result = client.getTasks(criteria, 10, 0);
			log.info("Total matches: " + result.getTotalMatches());

			if (result.getTotalMatches() != 1) {
				log.error("unexpected number of results for search");
				throw new MayamClientException(
						MayamClientErrorCode.UNEXPECTED_NUMBER_OF_TASKS);
			}

			return result.getMatches().get(0);
		} catch (RemoteException e) {
			log.error("remote expcetion searching for task", e);
			throw new MayamClientException(
					MayamClientErrorCode.TASK_SEARCH_FAILED);
		}

	}

	public void saveTask(AttributeMap task) throws MayamClientException {
		
		try {
			client.updateTask(task);
		} catch (RemoteException e) {
			log.error("remote expcetion saving task", e);
			throw new MayamClientException(
					MayamClientErrorCode.TASK_UPDATE_FAILED);
		}
		
	}

}
