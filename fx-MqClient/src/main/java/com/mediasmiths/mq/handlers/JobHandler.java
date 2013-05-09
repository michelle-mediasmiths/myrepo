package com.mediasmiths.mq.handlers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.veneer.TasksClientVeneer;

public abstract class JobHandler implements Handler
{
	@Inject
	protected TasksClientVeneer tasksClient;
	
	@Inject
	protected MayamTaskController taskController;
	
	@Inject
	protected MayamMaterialController materialController;
	
	@Inject
	protected EventService eventsService;
		
	public abstract void process(Job jobMessage) ; 

	public abstract String getName();
}
