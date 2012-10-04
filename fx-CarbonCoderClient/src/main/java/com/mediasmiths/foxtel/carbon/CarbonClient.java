package com.mediasmiths.foxtel.carbon;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.mediasmiths.foxtel.carbon.jaxb.Job;

public interface CarbonClient
{
	public String jobQueueRequest(String jobName, List<String> sources, List<String> destinations, List<UUID> profiles) throws TransformerException, ParserConfigurationException, UnknownHostException, IOException;

	public List<String> listProfiles() throws TransformerException, ParserConfigurationException, UnknownHostException, IOException;

	public List<Job> listJobs() throws TransformerException, ParserConfigurationException, UnknownHostException, IOException, JAXBException;
}
