package com.mediasmiths.stdEvents.ui.rss;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import org.apache.commons.digester.rss.Channel;
import org.apache.commons.digester.rss.Item;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RSSFeed
{
	public static transient Logger logger = Logger.getLogger(RSSFeed.class);

	public static void createRSS(EventEntity event) 
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
		String today = formatter.format(new Date());

		logger.info("Creating channel...");
		Channel newChannel = new Channel();

		newChannel.setLanguage("en");
		newChannel.setPubDate(today);

		logger.info("Adding item...");
		Item item = new Item();
		item.setTitle(event.getEventName());
		item.setDescription("Time:" + event.getTime() + " Namespace:" + event.getNamespace() + " EventName:" + event.getEventName() + " Payload:" + event.getPayload());


		newChannel.setPubDate(today);
		newChannel.addItem(item);

		try {
			logger.info("Writing xml file...");
			FileOutputStream out = new FileOutputStream("rssFeed.xml");
			newChannel.render(out);
			out.close();
			logger.info("Finished writing");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getRSS () throws SAXException, IOException, JDOMException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//DocumentBuilder builder = factory.newDocumentBuilder();

		SAXBuilder builder = new SAXBuilder();
		File rssFile = new File("rssFeed.xml");

		Document doc = (Document) builder.build(rssFile);
		Element rootNode = doc.getRootElement();

		Element channel = rootNode.getChild("channel");

	}

	public static void main (String [] args) 
	{
		EventEntity event = new EventEntity();
		event.setTime(new Long (1611));
		event.setNamespace("rssTest");
		event.setEventName("rssTest");
		event.setPayload("rssTest");

		createRSS(event);

	}
}
