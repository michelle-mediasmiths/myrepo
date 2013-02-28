package com.mediasmiths.stdEvents.report.entity;

public class TaskListRT
{
	String dateRange;
	String taskType;
	String channel;
	String taskStatus;
	String requiredBy;
	String department;
	String operator;
	String taskStart;
	String taskFinish;
	
	public TaskListRT()
	{
	}

	public TaskListRT(String taskType, String channel)
	{
		super();
		this.taskType = taskType;
		this.channel = channel;
	}

	public TaskListRT(
			String dateRange,
			String taskType,
			String channel,
			String taskStatus,
			String requiredBy,
			String department,
			String operator,
			String taskStart,
			String taskFinish)
	{
		super();
		this.dateRange = dateRange;
		this.taskType = taskType;
		this.channel = channel;
		this.taskStatus = taskStatus;
		this.requiredBy = requiredBy;
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

	public String getTaskType()
	{
		return taskType;
	}

	public void setTaskType(String taskType)
	{
		this.taskType = taskType;
	}

	public String getChannel()
	{
		return channel;
	}

	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	public String getTaskStatus()
	{
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus)
	{
		this.taskStatus = taskStatus;
	}

	public String getRequiredBy()
	{
		return requiredBy;
	}

	public void setRequiredBy(String requiredBy)
	{
		this.requiredBy = requiredBy;
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