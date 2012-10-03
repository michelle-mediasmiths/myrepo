package com.mediasmiths.foxtel.carbon.message;



/**
 * Takes an xml message for carbon and translates into string to be sent down socket connections with carbon
 * @author bmcleod
 *
 */
public class Transformer
{
	private static final String MESSAGE_PREFIX = "CarbonAPIXML1";
	
	public String buildMessageForData(String data)
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
