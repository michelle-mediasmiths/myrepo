package com.mediasmiths.mq.listeners;

import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;

public class TaskListener
{
	public static Listener getInstance() 
	{
		return new Listener() 
		{
			public void onMessage(MqMessage msg) throws Throwable 
			{
				if (msg.getType().equals(ContentTypes.ATTRIBUTES)) 
				{
					//TODO: IMPLEMENT

				}
			}
		};
	}
}
