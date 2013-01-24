package com.mediasmiths.foxtel.tc.service;

import com.mediasmiths.carbon.type.mutable.CarbonDestination;
import com.mediasmiths.carbon.type.mutable.CarbonProject;
import com.mediasmiths.foxtel.tc.CarbonBaseProfile;
import com.mediasmiths.foxtel.tc.rest.api.TCBugOptions;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCTimecodeOptions;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

public class CarbonProjectBuilder
{
	public CarbonProject build(TCJobParameters parameters)
	{
		CarbonProject project = loadProfile(pickProfile(parameters));

		customise(project, parameters);

		return project;
	}

	private void customise(final CarbonProject project, final TCJobParameters parameters)
	{

		if (parameters.timecode != null)
			addTimecode(project, parameters.timecode);

		if (parameters.bug != null)
			addBug(project, parameters.bug);

		setOutputFile(project, parameters.outputFolder, parameters.outputFileBasename);
	}

	private void addBug(final CarbonProject project, final TCBugOptions parameters)
	{
		// Load bug filter XML
	}


	private void addTimecode(final CarbonProject project, TCTimecodeOptions parameters)
	{
		// Load timecode filter XML
	}

	private void setOutputFile(final CarbonProject project, final String outputFolder, final String outputFileBasename)
	{
		CarbonDestination destination = project.getDestinations().get(0);

		destination.setDestinationUNC(outputFolder);
		destination.setDestinationBaseFilename(outputFileBasename);
	}

	protected CarbonBaseProfile pickProfile(TCJobParameters parameters)
	{
		for (CarbonBaseProfile profile : CarbonBaseProfile.values())
		{
			if (profile.suitableFor(parameters.purpose, parameters.sourceType, parameters.audioType))
			{
				return profile;
			}
		}

		throw new IllegalArgumentException("No suitable Carbon base project found!");
	}

	protected CarbonProject loadProfile(CarbonBaseProfile profile)
	{
		// TODO read the file and parse it as a Document, then wrap it in a CarbonProject element
		return null;
	}


	public static void main(String[] args) throws Exception
	{
		JAXBSerialiser ser = JAXBSerialiser.getInstance(TCJobParameters.class);
		ser.setPrettyOutput(true);

		System.out.println(ser.serialise(new TCJobParameters()));
	}
}
