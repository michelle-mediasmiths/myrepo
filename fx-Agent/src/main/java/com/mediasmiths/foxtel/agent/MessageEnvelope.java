package com.mediasmiths.foxtel.agent;

import com.mediasmiths.foxtel.agent.queue.PickupPackage;

public class MessageEnvelope<T>
{

	private final PickupPackage pickupPackage;
	private final T message;

	public MessageEnvelope(PickupPackage pickupPackage, T message)
	{
		this.pickupPackage = pickupPackage;
		this.message = message;
	}

	public MessageEnvelope<T> getInstance(PickupPackage pickupPackage, T Message)
	{
		return new MessageEnvelope<T>(pickupPackage, message);
	}

	public T getMessage()
	{
		return message;
	}

	public PickupPackage getPickupPackage()
	{
		return pickupPackage;
	}

}
