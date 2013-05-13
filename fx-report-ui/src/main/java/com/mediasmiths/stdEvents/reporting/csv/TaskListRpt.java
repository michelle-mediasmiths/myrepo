package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.report.OrderStatus;
import com.mediasmiths.foxtel.ip.common.events.report.TaskList;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.report.entity.TaskListRT;

public class TaskListRpt
{
	public static final transient Logger logger = Logger.getLogger(TaskListRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject 
	private QueryAPI queryApi;
	
	public void writeTaskList(List<EventEntity> events, DateTime startDate, DateTime endDate, String reportName)
	{
		List<TaskList> tasks = getReportList(events, startDate, endDate);
		createCsv(tasks, reportName);		
	}
	
	private TaskList unmarshall(EventEntity event)
	{
		Object title = new TaskList();
		String payload = event.getPayload();
		logger.info("Unmarshalling payload " + payload);

		try
		{
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.report.ObjectFactory.class);
			logger.info("Deserialising payload");
			title = JAXB_SERIALISER.deserialise(payload);
			logger.info("Object created");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return (TaskList)title;
	}
	
	public List<TaskList> getReportList(List<EventEntity> events, DateTime startDate, DateTime endDate)
	{
		List<TaskList> tasks = new ArrayList<TaskList>();
		for (EventEntity event : events)
		{
			TaskList task = unmarshall(event);
			task.setDateRange(startDate + " - " + endDate);	
				
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
//	ICsvBeanWriter beanWriter = null;
//	try {
//		beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
//		final String[] header = {"dateRange", "taskType", "channel", "taskStatus", "requiredBy", "operator", "department", "taskStart", "taskFinish"};
//		final CellProcessor[] processors = getTaskListProcessor();
//		beanWriter.writeHeader(header);
//		
//		TaskListRT total = new TaskListRT("Total Tasks", Integer.toString(tasks.size()));
//		TaskListRT outstanding = new TaskListRT("Outstanding", Integer.toString(queryApi.getOutstandingTasks(events).size()));
//		TaskListRT completed = new TaskListRT ("Completed Tasks", Integer.toString(queryApi.getCompletedTasks(events).size()));
//		TaskListRT overdue = new TaskListRT ("OverdueTasks", Integer.toString(queryApi.getOverdue(events).size()));
//		TaskListRT average = new TaskListRT ("Average Completion Time", queryApi.getAvCompletionTime(events));
//		tasks.add(total);
//		tasks.add(outstanding);
//		tasks.add(completed);
//		tasks.add(average);
//		tasks.add(overdue);
//		
//		logger.info(tasks);
//		
//		for (TaskListRT task : tasks)
//		{
//			beanWriter.write(task, header, processors);
//		}
//	} 
//	catch (IOException e)
//	{
//		e.printStackTrace();
//	}
//	finally {
//		if (beanWriter != null)
//		{
//			try
//			{
//				beanWriter.close();
//			}
//			catch(IOException e)
//			{
//				e.printStackTrace();
//			}
//		}
//	}
//	
//	String payload = event.getPayload();
//	TaskListRT task = new TaskListRT();
//	
//	task.setDateRange(startDate + " - " + endDate);
//	if (payload.contains("taskType"))
//		task.setTaskType(payload.substring(payload.indexOf("taskType")+9, payload.indexOf("</taskType")));
//	if (payload.contains("channel"))
//		task.setChannel(payload.substring(payload.indexOf("channel")+8, payload.indexOf("</channel")));
//	if (payload.contains("taskStatus"))
//		task.setTaskStatus(payload.substring(payload.indexOf("taskStatus")+11, payload.indexOf("</taskStatus")));
//	if (payload.contains("requiredBy"))
//		task.setRequiredBy(payload.substring(payload.indexOf("requiredBy")+11, payload.indexOf("</requiredBy")));
//	if (payload.contains("operator"))
//		task.setOperator(payload.substring(payload.indexOf("operator")+9, payload.indexOf("</operator")));
//	if (payload.contains("department"))
//		task.setDepartment(payload.substring(payload.indexOf("department")+11, payload.indexOf("</department")));
//	if (payload.contains("taskStart"))
//		task.setTaskStart(payload.substring(payload.indexOf("taskStart")+10, payload.indexOf("</taskStart")));
//	if (payload.contains("taskFinish"))
//		task.setTaskFinish(payload.substring(payload.indexOf("taskFinish")+11, payload.indexOf("</taskFinish")));
//}
