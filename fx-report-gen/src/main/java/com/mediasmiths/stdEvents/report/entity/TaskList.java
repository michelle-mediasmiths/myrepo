package com.mediasmiths.stdEvents.report.entity;

public class TaskList
{
	String dateRange;
	String channel;
	String process;
	String department;
	String operator;
	String taskStart;
	String taskFinish;
	
	public TaskList()
	{
	}
	
	public TaskList(String channel, String process)
	{
		super();
		this.channel = channel;
		this.process = process;
	}

	public TaskList(
			String dateRange,
			String channel,
			String process,
			String department,
			String operator,
			String taskStart,
			String taskFinish)
	{
		super();
		this.dateRange = dateRange;
		this.channel = channel;
		this.process = process;
		this.department = department;
		this.operator = operator;
		this.taskStart = taskStart;
		this.taskFinish = taskFinish;
	}

	public String getDateRange()
	{
		return dateRange;
	}

	public void setDateRange(String dateRange)
	{
		this.dateRange = dateRange;
	}

	public String getChannel()
	{
		return channel;
	}

	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	public String getProcess()
	{
		return process;
	}

	public void setProcess(String process)
	{
		this.process = process;
	}

	public String getDepartment()
	{
		return department;
	}

	public void setDepartment(String department)
	{
		this.department = department;
	}

	public String getOperator()
	{
		return operator;
	}

	public void setOperator(String operator)
	{
		this.operator = operator;
	}

	public String getTaskStart()
	{
		return taskStart;
	}

	public void setTaskStart(String taskStart)
	{
		this.taskStart = taskStart;
	}

	public String getTaskFinish()
	{
		return taskFinish;
	}

	public void setTaskFinish(String taskFinish)
	{
		this.taskFinish = taskFinish;
	}
	
	
}
