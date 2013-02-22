package com.foxtel.ip.mail.variablereplacer;


public interface EmailVariableGenerator
{
	public boolean handles(Object obj);

	public EmailVariables getVariables(Object obj, String comment);

}
