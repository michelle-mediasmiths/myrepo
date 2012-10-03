package com.mediasmiths.foxtel.carbon;

import static com.mediasmiths.foxtel.carbon.CarbonConfig.CARBON_HOST;
import static com.mediasmiths.foxtel.carbon.CarbonConfig.CARBON_PORT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.dom.ElementNSImpl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.carbon.jaxb.CarbonReply;
import com.mediasmiths.foxtel.carbon.jaxb.Job;
import com.mediasmiths.foxtel.carbon.message.Builder;
import com.mediasmiths.foxtel.carbon.message.Transformer;
import com.mediasmiths.foxtel.carbon.profile.ProfileType;

public class CarbonClientImpl implements CarbonClient
{
	private static final Logger log = Logger.getLogger(CarbonClientImpl.class);

	@Inject
	private Transformer transformer;
	@Inject
	private Unmarshaller unmarshaller;

	private final String host;
	private final int port;
	private final static Charset UTF8 = Charset.forName("UTF-8");

	@Inject
	public CarbonClientImpl(@Named(CARBON_HOST) String host, @Named(CARBON_PORT) Integer port)
	{
		this.host = host;
		this.port = port.intValue();
	}

	private String sendToCarbon(String data) throws UnknownHostException, IOException
	{

		log.debug(String.format("Sending data to carbon"));
		log.trace("data : " + data);
		String message = transformer.buildMessageForData(data);
		log.trace("message : " + message);

		log.trace("Constructing carbon socket");
		Socket carbonSocket = new Socket(host, port);

		log.trace("Writing message");
		IOUtils.write(message, carbonSocket.getOutputStream(), UTF8);

		log.trace("Reading response");
		StringWriter writer = new StringWriter();
		IOUtils.copy(carbonSocket.getInputStream(), writer, UTF8);
		String response = writer.toString();

		log.trace("Response: " + response);

		log.trace("Closing socket");
		carbonSocket.close();

		return response;
	}

	@Override
	public String jobQueueRequest(String jobName, List<String> sources, List<String> destinations, List<UUID> profiles)
			throws TransformerException,
			ParserConfigurationException,
			UnknownHostException,
			IOException
	{
		String message = new Builder().getJobQueueRequest(jobName, sources, destinations, profiles);
		return sendToCarbon(message);
	}

	@Override
	public List<String> listProfiles()
			throws TransformerException,
			ParserConfigurationException,
			UnknownHostException,
			IOException
	{

		String message = new Builder().getProfileListRequest(ProfileType.Connection);
		sendToCarbon(message);

		return null;
	}

	@Override
	public List<Job> listJobs()
			throws TransformerException,
			ParserConfigurationException,
			UnknownHostException,
			IOException,
			JAXBException
	{
		String message = new Builder().getJobListRequest();
		String reply = sendToCarbon(message);
		String replyXML = transformer.getMessageFromData(reply);
		CarbonReply cr = (CarbonReply) unmarshaller.unmarshal(new ByteArrayInputStream(replyXML.getBytes()));

		log.debug(cr.getSuccess());

		List<Job> jobs = new ArrayList<Job>();
		
		for (ElementNSImpl e : cr.getJobList().getJobs())
		{
			Job job = new Job();

			job.setName(e.getAttribute("Name"));
			job.setGuid(UUID.fromString(e.getAttribute("GUID").substring(1,37))); //guids from carbon wrapped with {}
			jobs.add(job);
			
			log.trace(job.toString());
		}

		return jobs;
	}

}
