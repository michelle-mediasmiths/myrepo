package com.mediasmiths.mq.listeners;

import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class ItemCreationListener 
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
				    //If the Item was created and the Master message had the Compile flag set, 
					//the WFE will add the Item to the Compliance Logging WorklistIf the Item 
					//was created and the Master message had the Compile flag set, the WFE will add the Item to the Compliance Logging Worklist
				}
			}
		};
	}
}
