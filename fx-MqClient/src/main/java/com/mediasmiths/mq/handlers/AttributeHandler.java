package com.mediasmiths.mq.handlers;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.mayam.MayamClientImpl;
import com.mediasmiths.mayam.accessrights.MayamAccessRightsController;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.controllers.MayamPackageController;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.controllers.MayamTitleController;
import com.mediasmiths.mayam.veneer.TasksClientVeneer;

public abstract class AttributeHandler implements Handler
{
	@Inject
	protected TasksClientVeneer tasksClient;
	
	@Inject
	protected MayamClientImpl mayamClient;
	
	@Inject
	protected MayamTaskController taskController;
	
	@Inject
	protected MayamMaterialController materialController;
	
	@Inject
	protected MayamPackageController packageController;
	
	@Inject
	protected MayamTitleController titlecontroller;
	
	@Inject
	protected MayamAccessRightsController accessRightsController;
	
	@Inject
	protected EventService eventsService;
	
	@Inject
	protected ChannelProperties channelProperties;
	
	public abstract void process(AttributeMap messageAttributes) ;

	public abstract String getName();
}
