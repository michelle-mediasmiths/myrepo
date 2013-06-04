package com.mediasmiths.mayam.retrying;


import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
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
		requestInjection(interceptor);
		bindInterceptor(Matchers.subclassesOf(TasksClientVeneer.class), Matchers.annotatedWith(TasksWSRetryable.class), interceptor);
		bindInterceptor(Matchers.subclassesOf(TaskApiVeneer.class),  Matchers.annotatedWith(TasksWSRetryable.class), interceptor);
		bindInterceptor(Matchers.subclassesOf(AssetApiVeneer.class),  Matchers.annotatedWith(TasksWSRetryable.class), interceptor);
		bindInterceptor(Matchers.subclassesOf(SegmentApiVeneer.class), Matchers.annotatedWith(TasksWSRetryable.class), interceptor);
		bindInterceptor(Matchers.subclassesOf(UserApiVeneer.class),  Matchers.annotatedWith(TasksWSRetryable.class), interceptor);
	}

}
