package com.mediasmiths.foxtel.tc.model;

import java.util.UUID;

import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TCStartRequest
{
	@XmlElement(name="JobName")
	public String getJobName()
	{
		return jobName;
	}
	public void setJobName(String jobName)
	{
		this.jobName = jobName;
	}
	
	public String getInput()
	{
		return input;
	}
	public void setInput(String input)
	{
		this.input = input;
	}
	
	public String getOutput()
	{
		return output;
	}
	public void setOutput(String output)
	{
		this.output = output;
	}
	
	public UUID getPreset()
	{
		return preset;
	}
	public void setPresent(UUID preset)
	{
		this.preset = preset;
	}
	String jobName;
	String input;
	String output;
	UUID preset;

}
