package com.foxtel.ip.mail.data;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class EmailProperties
{

	@Inject
	@Named("mail.agent.from.address")
	public String emailAddress;
	@Inject
	@Named("mail.agent.from.password")
	public String password;
	@Inject
	@Named("mail.agent.smtp.host")
	public String hostName;
	@Inject
	@Named("mail.agent.smtp.port")
	public int smtpPort;

}
