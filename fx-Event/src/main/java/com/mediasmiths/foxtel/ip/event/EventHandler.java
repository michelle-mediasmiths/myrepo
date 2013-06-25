package com.mediasmiths.foxtel.ip.event;

/**
 *
 * The common interface that is used to transmit Events (with a common namespace) within the IP Platform.
 *
 * Author: Harmer
 */
public interface EventHandler
{
	/**
	 * @param eventName
	 * @param payload
	 *
	 * Perform the required message transmission for an object that has already been serialised by the caller.
	 *
	 */
	public void saveEvent(String eventName, String payload);

	/**
	 * @param eventName
	 * @param payload
	 * 
	 * Perform the required message transmission for an object that has already been serialised by the caller.
	 * 
	 * Allows the use of a different namespace than inject from service.events.namespace for services that created events for multiple namesapces
	 *
	 */
	public void saveEvent(String eventName, String payload, String namespace);

}
