package com.mediasmiths.foxtel.mpa;

import java.io.File;

import com.mediasmiths.foxtel.agent.MessageEnvelope;

public class MediaEnvelope<T> extends MessageEnvelope<T>
{

	private String masterID;

	private final boolean quarrentineOnMatch;

	public MediaEnvelope(File file, T message)
	{
		this(file, message, false);
	}

	public MediaEnvelope(File file, T message, boolean quarrentineOnMatch)
	{
		super(file, message);
		this.quarrentineOnMatch = quarrentineOnMatch;
	}

	public MediaEnvelope(File file, T message, String masterID)
	{
		this(file, message, masterID, false);
	}

	public MediaEnvelope(File file, T message, String masterID, boolean quarrentineOnMatch)
	{
		this(file, message, quarrentineOnMatch);
		setMasterID(masterID);
	}

	public MediaEnvelope(MessageEnvelope<T> envelope, String masterID)
	{
		this(envelope.getFile(), envelope.getMessage(), masterID, false);
	}

	public MediaEnvelope(MessageEnvelope<T> envelope, String masterID, boolean quarrentineOnMatch)
	{
		this(envelope.getFile(), envelope.getMessage(), masterID, quarrentineOnMatch);
	}

	public String getMasterID()
	{
		return masterID;
	}

	public final void setMasterID(String masterID)
	{
		this.masterID = masterID;
	}

	public boolean isQuarrentineOnMatch()
	{
		return quarrentineOnMatch;
	}

}
