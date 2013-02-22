package com.foxtel.ip.mail.data;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class FileAndFolderLocations
{
	@Inject
	@Named("mail.agent.mailbodies.location")
	public String bodyFolderLocation;
	@Inject
	@Named("mail.agent.configuration.location")
	public String configurationLocation;
}
