package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.report.entity.TaskList;

public class TaskListRpt
{
	public static final transient Logger logger = Logger.getLogger(TaskListRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject 
	private QueryAPI queryApi;
	
	public void writeTaskList(List<EventEntity> events)
	{
		List<TaskList> tasks = getTaskList(events);
		
		ICsvBeanWriter beanWriter = null;
		try {
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "taskListCsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "channel", "process", "department", "operator", "taskStart", "taskFinish"};
			final CellProcessor[] processors = getTaskListProcessor();
			beanWriter.writeHeader(header);
			
			TaskList total = new TaskList("Total Tasks", null);
			TaskList outstanding = new TaskList("Outstanding", null);
			TaskList completed = new TaskList ("Completed Tasks", null);
			TaskList overdue = new TaskList ("OverdueTasks", null);
			TaskList average = new TaskList ("Average Completion Time", null);
			tasks.add(total);
			tasks.add(outstanding);
			tasks.add(completed);
			tasks.add(average);
			tasks.add(overdue);
			
			for (TaskList task : tasks)
			{
				beanWriter.write(task, header, processors);
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
	
	public List<TaskList> getTaskList(List<EventEntity> events)
	{
		List<TaskList> tasks = new ArrayList<TaskList>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			TaskList task = new TaskList();
			
			//GET FIELDS FOR REPORT TYPE
			
			tasks.add(task);
		}
		
		return tasks;
	}
	
	public CellProcessor[] getTaskListProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[] {
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
