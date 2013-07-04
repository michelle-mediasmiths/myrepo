package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.foxtel.ip.common.events.report.TaskList;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

public class TaskListRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(TaskListRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject 
	private QueryAPIImpl queryApi;
	
	@Inject
	private ChannelProperties channelProperties;

	public void writeTaskList(List<AttributeMap> tasks, DateTime startDate, DateTime endDate, String reportName)
	{
		List<TaskList> taskList = getReportList(tasks, startDate, endDate);
		createCsv(taskList, reportName);		
	}
		
	public List<TaskList> getReportList(List<AttributeMap> mayamTasks, DateTime startDate, DateTime endDate)
	{
		List<TaskList> tasks = new ArrayList<TaskList>();

		final String start = startDate.toString(dateOnlyFormatString);
		final String end = endDate.toString(dateOnlyFormatString);

		for (AttributeMap mayamTask : mayamTasks)
		{
			TaskList task = new TaskList();
			task.setDateRange(start + " - " + end);
				
			task.setTaskType(mayamTask.getAttributeAsString(Attribute.TASK_LIST_ID));
			task.setTaskStatus(mayamTask.getAttributeAsString(Attribute.TASK_STATE));
			
			Date taskStart = mayamTask.getAttribute(Attribute.TASK_CREATED);
			if (taskStart != null)
			{
				String formattedStart = new SimpleDateFormat(dateAndTimeFormatString).format(taskStart);
				task.setTaskStart(formattedStart);
			}
			if (mayamTask.getAttribute(Attribute.TASK_UPDATED_BY) != null)
			{
				task.setOperator(mayamTask.getAttributeAsString(Attribute.TASK_UPDATED_BY));
			}
			try
			{
				if (mayamTask.getAttribute(Attribute.OP_DATE) != null)
				{
					GregorianCalendar c = new GregorianCalendar();
					c.setTime((Date) mayamTask.getAttribute(Attribute.OP_DATE));
					task.setRequiredBy(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
				}
				if (mayamTask.getAttribute(Attribute.CLOSED) != null)
				{
					Date taskFinish = mayamTask.getAttribute(Attribute.CLOSED);
					if (taskFinish != null)
					{
						String formattedFinish = new SimpleDateFormat(dateAndTimeFormatString).format(taskFinish);
						task.setTaskFinish(formattedFinish);
					}
				}
			} catch (DatatypeConfigurationException e) {
				Log.warn("Error while converting task start and end times to XMLGregorianCalendar");
				e.printStackTrace();
			}
			
			List<String> channels = mayamTask.getAttribute(Attribute.CHANNELS);
			if (channels != null)
			{
				String concatChannels = "";
				Log.info("Number of channels found = " + channels.size());
				for (String channel: channels)
				{
					concatChannels += (channel + " ");
				}
				task.setChannels(concatChannels);
			}
			
			List<String> channelGroups = mayamTask.getAttribute(Attribute.CHANNEL_GROUPS);
			if (channelGroups != null && !channelGroups.isEmpty())
			{
				String concatGroups = "";
				for (String group: channelGroups)
				{
					concatGroups += (group + " ");
				}
				task.setDepartment(concatGroups);
			}
			else {
				Set <String> channelOwnerList = channelProperties.groupsForChannels(channels);
				String concatGroups = "";
				for (String group: channelOwnerList)
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

