package com.mediasmiths.mayam.retrying;

import java.net.MalformedURLException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.veneer.AssetApiVeneer;
import com.mediasmiths.mayam.veneer.SegmentApiVeneer;
import com.mediasmiths.mayam.veneer.TaskApiVeneer;
import com.mediasmiths.mayam.veneer.TasksClientVeneer;
import com.mediasmiths.mayam.veneer.UserApiVeneer;

public class TasksWSRetryModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		TasksWSRetryMethodInterceptor interceptor = new TasksWSRetryMethodInterceptor();
		bindInterceptor(Matchers.subclassesOf(TasksClientVeneer.class), Matchers.annotatedWith(TasksWSRetryable.class), interceptor);
		bindInterceptor(Matchers.subclassesOf(TaskApiVeneer.class),  Matchers.annotatedWith(TasksWSRetryable.class), interceptor);
		bindInterceptor(Matchers.subclassesOf(AssetApiVeneer.class),  Matchers.annotatedWith(TasksWSRetryable.class), interceptor);
		bindInterceptor(Matchers.subclassesOf(SegmentApiVeneer.class), Matchers.annotatedWith(TasksWSRetryable.class), interceptor);
		bindInterceptor(Matchers.subclassesOf(UserApiVeneer.class),  Matchers.annotatedWith(TasksWSRetryable.class), interceptor);
	}

	@Provides
	@Singleton
	@Named(MayamClientModule.SETUP_TASKS_CLIENT)
	public TasksClient getSetupTasksClient() throws MalformedURLException
	{
		return new TasksClient();
	}

}
