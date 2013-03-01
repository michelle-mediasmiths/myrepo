package com.foxtel.ip.mail.variablereplacer.variablegenerators.content;

import com.foxtel.ip.mail.variablereplacer.EmailVariableGenerator;
import com.foxtel.ip.mail.variablereplacer.EmailVariables;
import com.mediasmiths.foxtel.ip.common.events.ArdomeJobFailure;

public class ArdomeFailureVariableGenerator implements EmailVariableGenerator
{

	@Override
	public boolean handles(Object obj)
	{
		return obj.getClass().equals(ArdomeJobFailure.class);
	}

	@Override
	public EmailVariables getVariables(Object obj, String comment)
	{
		ArdomeJobFailure ajf = (ArdomeJobFailure) obj;
		
		EmailVariables replacer = new EmailVariables();
		replacer.setVariable("assetID", ajf.getAssetID());
		replacer.setVariable("jobID", ajf.getJobID());
		return replacer;				
	}

}
