package com.mediasmiths.foxtel.tc.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InvokeIntalioTCFlow
{
	private String inputFile;
	private String outputFolder;
	private String packageID;

	@XmlElement(required=true)
	public String getInputFile()
	{
		return inputFile;
	}

	public void setInputFile(String inputFile)
	{
		this.inputFile = inputFile;
	}

	@XmlElement(required=true)
	public String getOutputFolder()
	{
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder)
	{
		this.outputFolder = outputFolder;
	}
	
	@XmlElement(required=true)
	public String getPackageID()
	{
		return packageID;
	}

	public void setPackageID(String packageID)
	{
		this.packageID = packageID;
	}
}
