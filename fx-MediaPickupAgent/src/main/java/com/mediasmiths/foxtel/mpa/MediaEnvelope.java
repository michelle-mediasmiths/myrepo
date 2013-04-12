package com.mediasmiths.foxtel.mpa;

import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.queue.PickupPackage;

public class MediaEnvelope<T> extends MessageEnvelope<T>
{

	private String masterID;

	public MediaEnvelope(PickupPackage p, T message)
	{
		super(p, message);
	}

	public MediaEnvelope(MessageEnvelope<T> envelope, String masterID)
	{
		this(envelope.getPickupPackage(), envelope.getMessage(), masterID);
	}

	public MediaEnvelope(PickupPackage p, T message, String masterID)
	{
		this(p, message);
		setMasterID(masterID);
	}

	public String getMasterID()
	{
		return masterID;
	}

	public void setMasterID(String masterid)
	{
		this.masterID = masterid;
	}
}
