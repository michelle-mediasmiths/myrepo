package com.mediasmiths.foxtel.ip.event.email;

/**
 *
 * Communication mechanism neutral means to interact with the email agent that send out email for events
 *
 * Author: Harmer
 */
public interface EmailAgent
{

	/**
	 * Send a request to the email agent informing them of the event.
	 *
	 * @param namespace the namespace of the event that is being sent
	 * @param event the name of the event
	 * @param payload the event specific xml
	 */
	public void eventNotify(String namespace, String event, String payload);


}
