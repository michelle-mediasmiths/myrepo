package com.mediasmiths.foxtel.agent.validation;

public class MessageValidationException extends Exception
{
	public MessageValidationResult result;

	public MessageValidationException(MessageValidationResult result)
	{
		this.result = result;
	}
}
