package com.mediasmiths.mayamintegration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.mayam.wf.attributes.server.AttributesModule;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.FilterCriteria.SortOrder;
import com.mayam.wf.attributes.shared.type.MediaStatus;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.mq.AttributeMessageBuilder;
import com.mayam.wf.mq.Mq;
import com.mayam.wf.mq.Mq.Detachable;
import com.mayam.wf.mq.Mq.ListenIntensity;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.MqException;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.MqModule;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.mq.common.Queues;
import com.mayam.wf.ws.client.FilterResult;
import com.mayam.wf.ws.client.TasksClient;

import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;

/**
 * Hello world!
 * 
 */
public class ExampleApp {

	final URL url;
	final String token = "someuser:somepassword";
	final Injector injector;
	final TasksClient client;
	final Provider<AttributeMessageBuilder> ambp;

	public ExampleApp() throws MalformedURLException {
		url = new URL("http://localhost:8084/tasks-ws");
		injector = Guice.createInjector(new AttributesModule(), new MqModule(
				"mycoolapp"));
		client = injector.getInstance(TasksClient.class).setup(url, token);
		ambp = injector.getProvider(AttributeMessageBuilder.class);

	}

	public void run() throws MalformedURLException {

		getAssetChildrenExample(client);
		
		clientTestMethods(client);
	//	filterCriteriaExample(client);
//		updateTaskExample();
//		mQExample(injector);

//		createAssetExample();
		createTaskExample();

//		filterCriteriaExample(client);

	}

	private void createAssetExample() {

		final AttributeMap assetAttributes = client.createAttributeMap();
		final AssetType assetType = AssetType.ITEM;

		assetAttributes.setAttribute(Attribute.ASSET_TYPE, assetType);
		assetAttributes.setAttribute(Attribute.ASSET_TITLE, "1x02 - Flavour country");
		assetAttributes.setAttribute(Attribute.SERIES_TITLE, "trocadero");

		AttributeMap result;
		try {
			// wont work in mocked mam, just create the task and add the mam
			// data to it
			result = client.createAsset(assetAttributes);
			final Long assetId = result.getAttribute(Attribute.ASSET_ID);
			final String assetGuid = result.getAttribute(Attribute.ASSET_ID);
			System.out.println("Created asset with id " + assetId + " and guid " + assetGuid);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	private void createTaskExample() {

		final AttributeMap task = client.createAttributeMap();
		final AssetType assetType = AssetType.ITEM;
		final Long assetId = new Long(1234L);

		final String taskListId = "ingest";
		final TaskState state = TaskState.PENDING;

		task.setAttribute(Attribute.ASSET_TYPE, assetType);
		task.setAttribute(Attribute.ASSET_ID, assetId);
		task.setAttribute(Attribute.ASSET_TITLE, "morning latte");
		task.setAttribute(Attribute.ASSIGNED_USER, "david");
		task.setAttribute(Attribute.SERIES_TITLE, "clements");

		task.setAttribute(Attribute.TASK_LIST_ID, taskListId);
		task.setAttribute(Attribute.TASK_STATE, state);

		AttributeMap result;
		try {
			result = client.createTask(task);
			final Long taskId = result.getAttribute(Attribute.TASK_ID);
			System.out.println("Created task with id " + taskId.longValue());
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void updateTaskExample() {
		final Long taskId = new Long(3002L);
		AttributeMap task;
		try {
			task = client.getTask(taskId);
			final TaskState state = task.getAttribute(Attribute.TASK_STATE);

			Long assetId = task.getAttribute(Attribute.ASSET_ID);
			System.out.println("Got asset id " + assetId + " for task "
					+ taskId);
			task.setAttribute(Attribute.SERIES_TITLE, "aaaaa");
			task.setAttribute(Attribute.MEDST_HR, MediaStatus.ACTIVE);
			client.updateTask(task);
			System.out.println("updated task");

		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	private void mQExample(final Injector injector) {

		Mq mq = injector.getInstance(Mq.class);

		Detachable someListener = mq.attachListener(Queues.MAM_INCOMING,
				new Listener() {

					public void onMessage(MqMessage msg) throws Throwable {
						System.out.println(msg.getContent());
						if (msg.getType().equals(ContentTypes.ATTRIBUTES)) {

							AttributeMap messageAttributes = msg.getSubject();

							String assetTitle = messageAttributes
									.getAttribute(Attribute.ASSET_TITLE);

							if (assetTitle != null) {
								System.out
										.println("Asset title: " + assetTitle);
							}
						}

					}
				});

		// send a message then listen for it
		final AttributeMap task = client.createAttributeMap();
		task.setAttribute(Attribute.ASSET_TITLE, "Hello");

		try {
			mq.send(Queues.MAM_INCOMING, ambp.get().subject(task).build());
		} catch (MqException e) {
			e.printStackTrace();
		}

		// while (true) {
		mq.listen(ListenIntensity.NORMAL);
		// }

		// detatch and shutdown
		someListener.detach();
		mq.shutdownConsumers();
		mq.shutdownProducers();
	}

	private void filterCriteriaExample(final TasksClient client) {
		final FilterCriteria criteria = client.createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(Attribute.TASK_LIST_ID,
				"ingest");
		criteria.getSortOrders()
				.add(new SortOrder(Attribute.TASK_CREATED,
						SortOrder.Direction.DESC));
		FilterResult result;
		try {
			result = client.getTasks(criteria, 40, 0);
			System.out.println("Total matches: " + result.getTotalMatches());
			for (final AttributeMap match : result.getMatches()) {
				System.out.println(" - "
						+ match.getAttributeAsString(Attribute.ASSET_TITLE)
						+ " " + match.getAttributeAsString(Attribute.MEDST_HR));
			}
		} catch (RemoteException e) {

			e.printStackTrace();
		}
	}

	private void clientTestMethods(final TasksClient client) {
		client.testAlwaysReturnTask();
		client.testAlwaysThrowException();
	}

	private void getAssetChildrenExample(final TasksClient client){
		String assetId = "12345678";
		
		try {
			List<AttributeMap> assetChildren = client.getAssetChildren(MayamAssetType.TITLE.getAssetType(), assetId, AssetType.ITEM);
			
			System.out.println(String.format("Found %s children",""+assetChildren.size()));
			
			for(AttributeMap child : assetChildren){
				System.out.println(String.format("\t Child: Title %s GUID %s", child.getAttribute(Attribute.ASSET_TITLE), child.getAttribute(Attribute.ASSET_ID)));
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) throws MalformedURLException {

		System.out.println("Starting");
		new ExampleApp().run();
		System.out.println("Stopping");

	}
}
