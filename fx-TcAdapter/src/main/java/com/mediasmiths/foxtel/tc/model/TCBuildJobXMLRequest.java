package com.mediasmiths.foxtel.tc.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TCBuildJobXMLRequest
{
	String packageID;
	String inputFile;
	String outputFolder;
	
	@XmlElement(name = "inputFile", required = true)
	public String getInputFile()
	{
		return inputFile;
	}

	public void setInputFile(String inputFile)
	{
		this.inputFile = inputFile;
	}

	@XmlElement(name = "outputFolder", required = true)
	public String getOutputFolder()
	{
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder)
	{
		this.outputFolder = outputFolder;
	}

	
	@XmlElement(name="packageID", required = true)
	public String getPackageID()
	{
		return packageID;
	}

	public void setPackageID(String packageID)
	{
		this.packageID = packageID;
	}
}
