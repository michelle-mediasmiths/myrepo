package com.mediasmiths.foxtel.carbon;

import static com.mediasmiths.foxtel.carbon.CarbonConfig.CARBON_HOST;
import static com.mediasmiths.foxtel.carbon.CarbonConfig.CARBON_PORT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.name.Named;

public class CarbonClientImpl implements CarbonClient
{
	private static final Logger log = Logger.getLogger(CarbonClientImpl.class);
	
	private static final String MESSAGE_PREFIX = "CarbonAPIXML1";
	
	private final String host;
	private final int port;
	private final static Charset UTF8 = Charset.forName("UTF-8");

	public CarbonClientImpl(@Named(CARBON_HOST) String host, @Named(CARBON_PORT) Integer port)
	{
		this.host = host;
		this.port = port.intValue();
	}
	
	private String sendToCarbon(String data) throws UnknownHostException, IOException{
		
		log.debug(String.format("Sending data to carbon"));
		log.trace("data : " + data);		
		String message = buildMessageForData(data);
		log.trace("message : " + message);
		
		log.trace("Constructing carbon socket");
		Socket carbonSocket = new Socket(host, port);

		log.trace("Writing message");		
		IOUtils.write(message, carbonSocket.getOutputStream(),UTF8);
		
		log.trace("Reading response");
		StringWriter writer = new StringWriter();
		IOUtils.copy(carbonSocket.getInputStream(), writer,UTF8);
		String response = writer.toString();
	
		log.trace("Response: "+response);
		
		log.trace("Closing socket");
		carbonSocket.close();
		
		return response;
	}

	private String buildMessageForData(String data)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(MESSAGE_PREFIX);
		sb.append(' ');
		sb.append(data.length());
		sb.append(' ');
		sb.append(data);
		
		return sb.toString();
	}

}
