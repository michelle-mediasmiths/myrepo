package com.mediasmiths.foxtel.mpa;

import java.io.File;

import com.mediasmiths.foxtel.agent.MessageEnvelope;
import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;

public class MediaEnvelope<T> extends MessageEnvelope<T>
{

	private String masterID;

	private final boolean quarrentineOnMatch;
	private final boolean failOnMatch;

	public MediaEnvelope(File file, T message)
	{
		this(file, message, false,false);
	}

	public MediaEnvelope(File file, T message, boolean quarrentineOnMatch, boolean failOnMatch)
	{
		super(file, message);
		this.quarrentineOnMatch = quarrentineOnMatch;
		this.failOnMatch = failOnMatch;
	}

	public MediaEnvelope(File file, T message, String masterID)
	{
		this(file, message, masterID, false, false);
	}

	public MediaEnvelope(File file, T message, String masterID, boolean quarrentineOnMatch, boolean failOnMatch)
	{
		this(file, message, quarrentineOnMatch,failOnMatch);
		setMasterID(masterID);
	}

	public MediaEnvelope(MessageEnvelope<T> envelope, String masterID)
	{
		this(envelope.getFile(), envelope.getMessage(), masterID, false,false);
	}

	public MediaEnvelope(MessageEnvelope<T> envelope, String masterID, boolean quarrentineOnMatch, boolean failOnMatch)
	{
		this(envelope.getFile(), envelope.getMessage(), masterID, quarrentineOnMatch, failOnMatch);
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
	
	public boolean isFailOnMatch(){
		return failOnMatch;
	}
}
