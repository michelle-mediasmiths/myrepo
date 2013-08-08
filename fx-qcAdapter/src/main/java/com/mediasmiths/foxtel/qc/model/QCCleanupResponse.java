package com.mediasmiths.foxtel.qc.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QCCleanupResponse
{
	private Boolean success = Boolean.FALSE;


	public QCCleanupResponse(final boolean success)
	{
		this.success = success;
	}


	@XmlElement(name = "success")
	public Boolean getSuccess()
	{
		return success;
	}


	public void setSuccess(final Boolean success)
	{
		this.success = success;
	}
}
