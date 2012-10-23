package com.mediasmiths.foxtel.carbonwfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.datacontract.schemas._2004._07.rhozet.ArrayOfPreset;
import org.datacontract.schemas._2004._07.rhozet.Dashboard;
import org.datacontract.schemas._2004._07.rhozet.Job;
import org.datacontract.schemas._2004._07.rhozet.JobStatus;
import org.datacontract.schemas._2004._07.rhozet.Preset;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.inject.Inject;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfguid;
import com.rhozet.rhozet_services_iwfcjmservices.IWfcJmServices;

public class WfsClient
{
	@Inject
	private IWfcJmServices service;
	@Inject
	private Unmarshaller unmarshaller;

	private final static Logger log = Logger.getLogger(WfsClient.class);

	public Job getJob(UUID jobid)
	{
		log.info("Requesting status of job " + jobid.toString());
		Job job = service.getJob(jobid.toString(), new Boolean(true));
		
		return job;
	}

	public JobStatus jobStatus(UUID jobid)
	{
		Job job = getJob(jobid);
		return job.getStatus();
	}

	/**
	 * Transcodes a file using the supplied preset/pcp file
	 * 
	 * @param pcpxml - pcp xml describing job
	 * @return
	 * @throws WfsClientException
	 */
	public UUID transcode(String pcpxml) throws WfsClientException
	{
		if (log.isTraceEnabled())
		{
			log.trace(String.format("queuing job by xml %s", pcpxml));
		}
		else
		{
			log.info(String.format("queuing job by xml"));
		}
		
		Job j = service.queueJobXML(pcpxml);
		
		log.info(String.format("job %s created", j.getGuid()));
		return UUID.fromString(j.getGuid());
	}

	/************************************************************
	 * methods beyond this point not currently used for foxtel
	 ************************************************************/

	/**
	 * Performs a transcode using a prexisting preset
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @param preset
	 * @param jobTitle
	 * @return
	 * @throws WfsClientException
	 */
	public UUID transcode(String inputFile, String outputFile, UUID preset, String jobTitle) throws WfsClientException
	{

		String workFlow = buildWorkFlowForSimpleTranscode(inputFile, jobTitle, preset);

		log.info(String.format("sending workflow xml %s for inputfile %s and outputfile %s", workFlow, inputFile, outputFile));

		Job j = service.queueJobByWorkflow(workFlow, inputFile, outputFile);

		log.info(String.format("job %s created", j.getGuid()));

		return UUID.fromString(j.getGuid());
	}

	private final static String transcodeTargetxp = "/WorkflowTasksSet/WorkflowTasks/TransformTaskSet/TranscodeTargetSet/Target";
	private final static String transcodeTargetPathxp = transcodeTargetxp + "/Path";
	private final static String transcodeTargetFilexp = transcodeTargetxp + "/Filename";
	private final static String transcodeTargetTitlexp = transcodeTargetxp + "/Title";
	private final static String transcodePresetxp = transcodeTargetxp + "/PresetGuid";
	private final static String workFlowTasksNamexp = "/WorkflowTasksSet/WorkflowTasks[@name]";

	public String buildWorkFlowForSimpleTranscode(String outputFile, String jobTitle, UUID presetUid) throws WfsClientException
	{
		log.info(String.format(
				"building workflow for outputFile %s jobTitle %s preset %s",
				outputFile,
				jobTitle,
				presetUid.toString()));

		SAXBuilder parser = new SAXBuilder();
		InputStream templateInputStream = getClass().getClassLoader().getResourceAsStream("TranscodeWFtemplate.xml");
		Document doc;
		try
		{
			doc = parser.build(templateInputStream);
		}
		catch (JDOMException e)
		{
			log.error("failed to parse document from workflow template");
			throw new WfsClientException(WfsClientException.reason.WORKFLOW_CONSTRUCTION_FROM_TEMPLATE_FAILED, e);
		}
		catch (IOException e)
		{
			log.error("failed to fetch workflow template");
			throw new WfsClientException(WfsClientException.reason.WORKFLOW_CONSTRUCTION_FROM_TEMPLATE_FAILED, e);
		}

		// set the target folder
		setSingleElementText(transcodeTargetPathxp, doc, FilenameUtils.getFullPath(outputFile));

		// set the target filename
		setSingleElementText(transcodeTargetFilexp, doc, FilenameUtils.getName(outputFile));

		// set the jobs title
		setSingleElementText(transcodeTargetTitlexp, doc, jobTitle);

		// set the transcode preset
		setSingleElementText(transcodePresetxp, doc, String.format("{%s}", presetUid.toString()));

		// set the template name
		// setSingleElementText(workFlowTasksNamexp, doc, jobTitle);

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
	private void setSingleElementText(String xpathStr, Document doc, String text)
	{

		XPathExpression<Element> xpath = XPathFactory.instance().compile(xpathStr, Filters.element());
		Element emt = xpath.evaluateFirst(doc);
		if (emt == null)
		{
			log.error("xpath " + xpathStr + " failed");
		}
		emt.setText(text);
	}

	/**
	 * Creates the supplied preset, returns its id
	 * 
	 * @param presetXML
	 * @return
	 * @throws JAXBException
	 */
	public UUID createPreset(Preset preset) throws JAXBException
	{

		log.info("Saving a preset");

		String id = service.storePreset(preset);

		log.info(String.format("preset %s created"));

		return UUID.fromString(id);
	}

	public ArrayOfPreset listPresets()
	{
		return service.getPresetList(Long.valueOf(0l));
	}

	/**
	 * updates the priority of a job
	 * @param jobid
	 * @param priority
	 */
	public void updatejobPriority(UUID jobid, Integer priority) {
		
		log.info(String.format("updating the priority of job %s to %n", jobid, priority));
		
		if(priority.intValue() < 1 || priority.intValue() > 10){
			log.warn("Priority is outside of the expected range");
		}
		
		ArrayOfguid jobGUids = new ArrayOfguid();
		jobGUids.getGuid().add(jobid.toString());
		service.updateJobPriority(jobGUids, priority);
	}

	public List<Job> listJobs() {
		log.info("listing jobs");
		
		return service.getJobPage(0, 1000, Long.valueOf(0l)).getJob();
	}

	public Dashboard getDashBoard() {
		log.info("getting jobs dashboard");
		return service.getJobDashboard();
			
	}

}
