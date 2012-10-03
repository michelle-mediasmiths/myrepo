package com.mediasmiths.foxtel.carbon.message;

import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mediasmiths.foxtel.carbon.profile.ProfileType;

public class Builder
{

	private static final String ROOT_ELEMENT = "cnpsXML";
	private static final String API_VERSION = "1.2";

	private DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	private final DocumentBuilder docBuilder;

	public Builder() throws ParserConfigurationException
	{
		docBuilder = docFactory.newDocumentBuilder();
	}

	public String getJobQueueRequest(String jobName, List<String> sources, List<String> destinations, List<UUID> profiles) throws TransformerException
	{

		// check there is a destination for each source
		if (sources.size() != destinations.size())
		{
			throw new IllegalArgumentException("A destination is required for each source");
		}
		
		if(destinations.size() != profiles.size()){
			throw new IllegalArgumentException("A profile is required for each destination");
		}

		Document doc = docBuilder.newDocument();

		Element rootElement = createRootElementForTask("JobQueue", doc);
		rootElement.setAttribute("JobName", jobName);

		addSourcesElement(sources, doc, rootElement);
		addDestinationsElement(destinations,profiles,doc, rootElement);

		return documentToString(doc);
	}
	
	public String getProfileListRequest(ProfileType pt) throws TransformerException{
		
		Document doc = docBuilder.newDocument();
		Element rootElement = createRootElementForTask("ProfileList", doc);
		
		Element profileAttributes = doc.createElement("ProfileAttributes");
		profileAttributes.setAttribute("ProfileType", pt.toString());
		rootElement.appendChild(profileAttributes);
		
		return documentToString(doc);
	}
	
	public String getJobListRequest() throws TransformerException
	{
		Document doc = docBuilder.newDocument();
		Element rootElement = createRootElementForTask("JobList", doc);
		return documentToString(doc);
	}

	
	/**
	 * Creates a root element for the given task type. Added as child to the supplied document
	 * @param taskType
	 * @param doc
	 * @return
	 */
	private Element createRootElementForTask(String taskType, Document doc){
		Element rootElement = doc.createElement(ROOT_ELEMENT);
		rootElement.setAttribute("CarbonAPIVer", API_VERSION);
		rootElement.setAttribute("TaskType", taskType);
		doc.appendChild(rootElement);
		return rootElement;
	}
	

	private void addDestinationsElement(List<String> destinations, List<UUID> profiles, Document doc, Element rootElement)
	{
		Element destinationsElement = doc.createElement("Destinations");

		int moduleId = 0;
		for (String s : destinations)
		{
			Element module = getDestinationModule(doc, moduleId, s, profiles.get(moduleId));
			destinationsElement.appendChild(module);
			moduleId++;
		}

		rootElement.appendChild(destinationsElement);
	}

	private void addSourcesElement(List<String> sources, Document doc, Element rootElement)
	{
		Element sourcesElement = doc.createElement("Sources");

		int moduleId = 0;
		for (String s : sources)
		{
			Element module = getSourceModule(doc, moduleId, s);
			sourcesElement.appendChild(module);
			moduleId++;
		}

		rootElement.appendChild(sourcesElement);
	}

	private Element getSourceModule(Document doc, int suffix, String filename)
	{

		Element module = doc.createElement("Module_" + suffix);
		module.setAttribute("Filename", filename);
		return module;

	}

	private Element getDestinationModule(Document doc, int suffix, String filename, UUID profile)
	{

		Element module = doc.createElement("Module_" + suffix);
		module.setAttribute("DestinationName", filename);
		module.setAttribute("PresetGUID", profile.toString());
		return module;

	}

	private String documentToString(Document doc) throws TransformerException
	{
		// set up a transformer
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		// create string from xml tree
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(doc);
		trans.transform(source, result);
		String xmlString = sw.toString();
		return xmlString;
	}

	
}
