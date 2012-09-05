package com.mediasmiths.mayamintegration.placeholdermanagement;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import com.google.inject.Injector;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetAccess;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.attributes.shared.type.Approval.Entry;
import com.mayam.wf.attributes.shared.type.AssetAccess.EntityType;
import com.mayam.wf.attributes.shared.type.FilterCriteria.SortOrder;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.FilterResult;
import com.mayam.wf.ws.client.TasksClient.RemoteException;

class PlaceHolderCreator implements Runnable {

	private final Injector injector;
	private final TasksClient client;
	private static Logger logger = Logger.getLogger(PlaceHolderCreator.class);
	private final Random random = new Random(System.currentTimeMillis());

	public PlaceHolderCreator(Injector injector, TasksClient client) {
		this.injector = injector;
		this.client = client;
	}


	public void run() {
		logger.trace("PlaceHolderCreator.run() enter");

		createTitle("2001fsfsdfsdfsdfsdfds", "Trocadero", "someuser",
				new Boolean(true), Collections.<String> emptyList());
		createMaster("3002", "2001", "1x01 Trocadero begins", "someuser");
		createMaster("3003", "2001", "1x02 Flavour country", "someuser");
		createMaster("3004", "2001", "1x03 The Juice is loose", "someuser");
		createMaster("3005", "2001", "1x04 Apples and oranges", "someuser");

		logger.trace("PlaceHolderCreator.run() return");
	}

	/**
	 * Title ID: Unique ID to identify the Title and map it to a Subprogram
	 * (Episode) in Viz Ardome Owner: A channel or channel group that “own” the
	 * Title Restricted: A flag that restricts access to the content to a select
	 * user group within the Channel (TBD in FOXTEL’s Security Model). This flag
	 * will default to “false”. Channel(s): A list of channels that can/will air
	 * the Title
	 */
	public void createTitle(String titleID, String name, String owner,
			Boolean restricted, List<String> channels) {

		AttributeMap title = getAsset(AssetType.SER, titleID);

		if (title == null) {

			logger.info("Title does not already exist");

			AttributeMap assetAttr = client.createAttributeMap();

			assetAttr.setAttribute(Attribute.ASSIGNED_USER, owner);
			assetAttr.setAttribute(Attribute.ASSET_TYPE, AssetType.SER);
			assetAttr.setAttribute(Attribute.SERIES_TITLE, name);
			assetAttr.setAttribute(Attribute.ASSET_GUID, titleID);

			AttributeMap result = null;
			try {
				result = client.createAsset(assetAttr);
				logger.info("Created sub program with id " + titleID);
			} catch (RemoteException e) {
				logger.error("RemoteException when creating asset\n\t"
						+ StringUtils.join(e.getRemoteMessages(), "\t\n"), e);
			}

			if(result != null){
				assetAttr = result;
			}
			

			if (restricted.booleanValue()) {
			//	update acls
				
				AssetAccess access = new AssetAccess();
				AssetAccess.ControlList.Entry entry = new AssetAccess.ControlList.Entry(EntityType.USER,owner, true,true,true);
				access.getStandard().add(entry);
				assetAttr.setAttribute(Attribute.ASSET_ACCESS, access);
				
				logger.info("updating asset access controls");
				try {
					client.updateAsset(assetAttr);
				} catch (RemoteException e) {
					logger.error("RemoteException when updating asset\n\t"
							+ StringUtils.join(e.getRemoteMessages(), "\t\n"), e);
				}
				
			}
		} else {
			logger.info("Title already exists");
		}
	}

	/**
	 * Master messages map directly to metadata for an Item in Viz Ardome.
	 */
	public void createMaster(String masterId, String titleId, String name,
			String owner) {

		logger.trace(String.format("createMaster(%s,%s,%s,%s)", masterId,
				titleId, name, owner));

		AttributeMap title = getAsset(AssetType.SER, titleId);

		if (title != null) {

			AttributeMap thismaster = getAsset(AssetType.ITEM, masterId);

			if (thismaster == null) {

				logger.info("Master does not already exist");

				final AttributeMap assetAttr = client.createAttributeMap();
				assetAttr.setAttribute(Attribute.ASSET_TYPE, AssetType.ITEM);
				assetAttr.setAttribute(Attribute.ASSET_TITLE, name);
				assetAttr.setAttribute(Attribute.ASSIGNED_USER, owner);

				// set some metadata from the title
				assetAttr.setAttribute(Attribute.SERIES_TITLE,
						title.getAttribute(Attribute.SERIES_TITLE));
			

				// how do we specify what subprogram an item should be in?
				assetAttr.setAttribute(Attribute.ASSET_GUID, masterId);

				AttributeMap result = null;
				try {
					assetAttr.setAttribute(Attribute.TASK_STATE,
							TaskState.PENDING);
					result = client.createAsset(assetAttr);

					logger.info("Created item with id " + masterId);
				} catch (RemoteException e) {
					logger.error("RemoteException when creating asset\n\t"
							+ StringUtils.join(e.getRemoteMessages(), "\t\n"),
							e);
				}

				if (result == null) {
					logger.error("error creating item");
					return;
				}

				// TODO: update acls
				
				

				// TODO: create PENDING ingest task
				final AttributeMap task = result.copy();
				task.setAttribute(Attribute.TASK_LIST_ID, "ingest");
//				task.setAttribute(Attribute.TASK_ID, random.nextLong());
				AttributeMap taskResult;
				try {
					taskResult = client.createTask(task);
					final Long taskId = taskResult.getAttribute(Attribute.TASK_ID);
					System.out.println("Created task with id "
							+ taskId);
				} catch (RemoteException e) {
					e.printStackTrace();
					e.printRemoteMessages(System.err);
				}
			} else {
				logger.info("Master already exists");
			}
		} else {
			logger.info("Title does not exist");
		}
	}

	/**
	 * Version messages map directly to metadata for a TX-package in Viz Ardome.
	 */
	public void createVersion() {

		// TODO: query if version exists

		// TODO: create or update version

	}

	/**
	 * randomly returns the specified asset or null
	 * @param assetType
	 * @param titleID
	 * @return
	 */
	private AttributeMap getAsset(AssetType assetType, String titleID) {
		if (random.nextBoolean()) {

			AttributeMap title = null;
			try {
				title = client.getAsset(assetType, titleID);
			} catch (RemoteException e1) {
				logger.error(
						"RemoteException when getting asset "
								+ titleID
								+ "\n\t"
								+ StringUtils.join(e1.getRemoteMessages(),
										"\t\n"), e1);
			}
			return title;
		} else {
			return null;
		}
	}

}