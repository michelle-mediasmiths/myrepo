package com.mediasmiths.mq.handlers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.controllers.MayamPackageController;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.controllers.MayamTitleController;
import com.mediasmiths.mayam.guice.MayamClientModule;

public abstract class AttributeHandler implements Handler
{
	@Named(MayamClientModule.SETUP_TASKS_CLIENT)
	@Inject
	protected TasksClient tasksClient;
	
	@Inject
	protected MayamTaskController taskController;
	
	@Inject
	protected MayamMaterialController materialController;
	
	@Inject
	protected MayamPackageController packageController;
	
	@Inject
	protected MayamTitleController titlecontroller;
	
	
	public abstract void process(AttributeMap messageAttributes) ;

	public abstract String getName();
}
