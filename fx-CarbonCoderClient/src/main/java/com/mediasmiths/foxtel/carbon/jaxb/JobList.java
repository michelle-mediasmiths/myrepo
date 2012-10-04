package com.mediasmiths.foxtel.carbon.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;

import org.apache.xerces.dom.ElementNSImpl;

public class JobList
{
	private List<ElementNSImpl> jobs;

	@XmlAnyElement(lax = true)
	public List<ElementNSImpl> getJobs()
	{
		return jobs;
	}

	public void setJobs(List<ElementNSImpl> jobs)
	{
		this.jobs = jobs;

	}
}
