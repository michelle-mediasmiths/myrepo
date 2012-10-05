package com.mediasmiths.foxtel.carbon.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;

import org.apache.xerces.dom.ElementNSImpl;

public class JobList
{
	private List<ElementNSImpl> jobs;

	public List<ElementNSImpl> getJobs()
	{
		return jobs;
	}
	@XmlAnyElement(lax = true)
	public void setJobs(List<ElementNSImpl> jobs)
	{
		this.jobs = jobs;

	}
}