package com.mediasmiths.mayam.veneer;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.FilterResult;
import com.mayam.wf.ws.client.TaskApi;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.retrying.TasksWSRetryable;

public class TaskApiVeneerImpl implements TaskApiVeneer
{
	protected final TaskApi tasksApi;

	@Inject
	public TaskApiVeneerImpl(@Named(MayamClientModule.SETUP_TASKS_CLIENT) TasksClient tasksClient)
	{
		this.tasksApi = tasksClient.taskApi();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.TasksApiVeneer#createTask(com.mayam.wf.attributes.shared.AttributeMap)
	 */
	@Override
	@TasksWSRetryable
	public AttributeMap createTask(AttributeMap task) throws RemoteException
	{
		return tasksApi.createTask(task);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.TasksApiVeneer#getTask(long)
	 */
	@Override
	@TasksWSRetryable
	public AttributeMap getTask(long taskId) throws RemoteException
	{
		return tasksApi.getTask(taskId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.TasksApiVeneer#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return tasksApi.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.TasksApiVeneer#getTasks(com.mayam.wf.attributes.shared.type.FilterCriteria, int, int)
	 */
	@Override
	@TasksWSRetryable
	public FilterResult getTasks(FilterCriteria filterCritera, int pageSize, int rowOffset) throws RemoteException
	{
		return tasksApi.getTasks(filterCritera, pageSize, rowOffset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.TasksApiVeneer#updateTask(com.mayam.wf.attributes.shared.AttributeMap)
	 */
	@Override
	@TasksWSRetryable
	public AttributeMap updateTask(AttributeMap task) throws RemoteException
	{
		return tasksApi.updateTask(task);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.TasksApiVeneer#deleteTask(long)
	 */
	@Override
	@TasksWSRetryable
	public void deleteTask(long taskId) throws RemoteException
	{
		tasksApi.deleteTask(taskId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.TasksApiVeneer#createFilterCriteria()
	 */
	@Override
	public FilterCriteria createFilterCriteria()
	{
		return tasksApi.createFilterCriteria();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.TasksApiVeneer#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		return tasksApi.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.veneer.TasksApiVeneer#toString()
	 */
	@Override
	public String toString()
	{
		return tasksApi.toString();
	}
}
