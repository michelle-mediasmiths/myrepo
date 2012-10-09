package com.mediasmiths.mq.listeners;

import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class InitiateQcListener 
{
	public static Listener getInstance(final TasksClient client, final MayamTaskController taskController) 
	{
		return new Listener() 
		{
			public void onMessage(MqMessage msg) throws Throwable 
			{
				if (msg.getType().equals(ContentTypes.ATTRIBUTES)) 
				{
					//TODO: IMPLEMENT
					//Once the import successfully completes, Viz Ardome shall send a notification to the WFE. The WFE will then perform the following tasks:
					//• If the second, optional, companion XML file containing Blackspot metadata is present, WFE shall upload and attach the file 
					//  to the Item placeholder in Viz Ardome.
					//• The WFE shall initiate a QC flow for the Item in Viz Ardome
					//o 3rd party Auto QC (if the media or workflow has been flagged as requiring Auto QC) o File format verification
					//• The WFE shall send an e-mail notification to a user group to be defined that the media has been imported
					//
					//On content acquisition initiate qc workflows if required
				
				}
			}
		};
	}
}
