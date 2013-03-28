package com.mediasmiths.foxtel.agent.validation;

import com.mediasmiths.foxtel.agent.queue.PickupPackage;

public class MessageValidationResultPackage<T>
{

	private final T message;
	private final PickupPackage pp;
	private final MessageValidationResult result;
	
	public MessageValidationResultPackage(PickupPackage pp, T message, MessageValidationResult result){
		this.message=message;
		this.pp=pp;
		this.result=result;
	}
	
	public MessageValidationResultPackage(PickupPackage pp, MessageValidationResult result){
		this.message=null;
		this.pp=pp;
		this.result=result;
	}

	public boolean hasMessage(){
		return message!=null;
	}
	
	public T getMessage()
	{
		return message;
	}

	public PickupPackage getPp()
	{
		return pp;
	}

	public MessageValidationResult getResult()
	{
		return result;
	}
	
}
