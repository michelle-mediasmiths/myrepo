package com.mediasmiths.mayam.validation;

import javax.xml.datatype.XMLGregorianCalendar;

import com.mayam.wf.ws.client.TasksClient;

public class MayamValidator {
	private TasksClient client;
	
	public MayamValidator(TasksClient mayamClient) 
	{
		client = mayamClient;
	}
	
	public boolean validatePackageFormat(String presentationFormat, String materialID) 
	{
		
		return false;
	}
	
	public boolean validateBroadcastDate(XMLGregorianCalendar targetDate, String materialID) 
	{
		
		return false;
	}
}
