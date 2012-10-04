package com.mediasmiths.foxtel.carbonwfs;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.datacontract.schemas._2004._07.rhozet.Job;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Content.CType;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.inject.Inject;
import com.rhozet.rhozet_services_iwfcjmservices.IWfcJmServices;

public class Client
{
	private final IWfcJmServices service;

	private final static Logger log = Logger.getLogger(Client.class);
	
	@Inject
	public Client(IWfcJmServices service)
	{
		this.service = service;

	}

	public UUID transcode(String inputFile, String outputFile, UUID preset, String jobTitle) throws JDOMException, IOException, EncoderException
	{

		String workFlow = buildWorkFlowForSimpleTranscode(inputFile, jobTitle, preset);
		
		log.info(String.format("sending workflow xml %s", workFlow));
		
		Job j = service.queueJobByWorkflow(workFlow, inputFile, outputFile);
		
		log.info(String.format("job %s created", j.getGuid()));
		
		return UUID.fromString(j.getGuid());
	}

	private final static String transcodeTargetxp = "/WorkflowTasksSet/WorkflowTasks/TransformTaskSet/TranscodeTargetSet/Target";
	private final static String transcodeTargetPathxp = transcodeTargetxp + "/Path";
	private final static String transcodeTargetFilexp = transcodeTargetxp + "/Filename";
	private final static String transcodeTargetTitlexp = transcodeTargetxp + "/Title";
	private final static String transcodePresetxp = transcodeTargetxp + "/PresetGuid";

	public String buildWorkFlowForSimpleTranscode(String outputFile, String jobTitle, UUID presetUid)
			throws JDOMException,
			IOException, EncoderException
	{
		SAXBuilder parser = new SAXBuilder();
		Document doc = parser.build(getClass().getClassLoader().getResourceAsStream("TranscodeWFtemplate.xml"));

		// set the target folder
		setSingleElementText(transcodeTargetPathxp, doc, FilenameUtils.getFullPath(outputFile));

		// set the target filename
		setSingleElementText(transcodeTargetFilexp, doc, FilenameUtils.getName(outputFile));

		// set the jobs title
		setSingleElementText(transcodeTargetTitlexp, doc, jobTitle);

		// set the transcode preset
		setSingleElementText(transcodePresetxp, doc, String.format("{%s}", presetUid.toString()));

		return new XMLOutputter().outputString(doc);
	}

	/**
	 * Finds the first instance of the element described by xpath in doc and sets its text to the specified value
	 * 
	 * @param xpath
	 * @param doc
	 * @param text
	 * @throws EncoderException 
	 */
	private void setSingleElementText(String xpathStr, Document doc, String text) throws EncoderException
	{

		XPathExpression<Element> xpath = XPathFactory.instance().compile(xpathStr, Filters.element());
		Element emt = xpath.evaluateFirst(doc);
		if(emt == null){
			System.out.println("xpath " + xpathStr + " failed");
		}
		emt.setText(text);
	}

}
