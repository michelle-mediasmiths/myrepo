package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.report.TaskList;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;

public class TaskListRpt
{
	public static final transient Logger logger = Logger.getLogger(TaskListRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject 
	private QueryAPI queryApi;
	
	public void writeTaskList(List<AttributeMap> tasks, DateTime startDate, DateTime endDate, String reportName)
	{
		List<TaskList> tasks = getReportList(tasks, startDate, endDate);
		createCsv(tasks, reportName);		
	}
		
	public List<TaskList> getReportList(List<AttributeMap> mayamTasks, DateTime startDate, DateTime endDate)
	{
		List<TaskList> tasks = new ArrayList<TaskList>();
		for (AttributeMap mayamTask : mayamTasks)
		{
			TaskList task = new TaskList();
			task.setDateRange(startDate + " - " + endDate);	
				
			task.setTaskType(mayamTask.getAttributeAsString(Attribute.TASK_LIST_ID));
			task.setTaskStatus(mayamTask.getAttributeAsString(Attribute.TASK_STATE));
			task.setTaskStart(mayamTask.getAttributeAsString(Attribute.TASK_CREATED));
			if (mayamTask.getAttribute(Attribute.TASK_UPDATED_BY) != null)
			{
				task.setOperator(mayamTask.getAttributeAsString(Attribute.TASK_UPDATED_BY));
			}
			if (mayamTask.getAttribute(Attribute.OP_DATE) != null)
			{
				GregorianCalendar c = new GregorianCalendar();
				c.setTime((Date) mayamTask.getAttribute(Attribute.OP_DATE));
				task.setRequiredBy(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
			}
			if (mayamTask.getAttribute(Attribute.CLOSED) != null)
			{
				GregorianCalendar c = new GregorianCalendar();
				c.setTime((Date) mayamTask.getAttribute(Attribute.CLOSED));
				task.setTaskEnd(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
			}
			
			List<String> channels = mayamTask.getAttribute(Attribute.CHANNELS);
			if (channels != null)
			{
				String concatChannels = "";
				for (String channel: channels)
				{
					concatChannels += (channel + " ");
				}
				task.setChannels(concatChannels);
			}
			
			List<String> channelGroups = mayamTask.getAttribute(Attribute.CHANNEL_GROUPS);
			if (channelGroups != null)
			{
				String concatGroups = "";
				for (String group: channelGroups)
				{
					concatGroups += (group + " ");
				}
				task.setDepartment(concatGroups);
			}
			
			tasks.add(task);
		}
		
		return tasks;
	}
	
	private void createCsv(List<TaskList> titles, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = {"dateRange", "taskType", "channels", "taskStatus", "requiredBy", "operator", "department", "taskStart", "taskFinish"};
			final CellProcessor[] processors = getProcessor();
			beanWriter.writeHeader(header);
				
			
			for (TaskList title : titles)
			{
				beanWriter.write(title, header, processors);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally {
			if (beanWriter != null)
			{
				try
				{
					beanWriter.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private CellProcessor[] getProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[] {
				new Optional(),
				new Optional(), 
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional()
		};
		return processors;
	}
}

