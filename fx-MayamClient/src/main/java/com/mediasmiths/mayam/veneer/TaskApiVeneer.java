package com.mediasmiths.mayam.veneer;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.FilterResult;

public interface TaskApiVeneer
{

	public abstract AttributeMap createTask(AttributeMap task) throws RemoteException;

	public abstract AttributeMap getTask(long taskId) throws RemoteException;

	public abstract int hashCode();

	public abstract FilterResult getTasks(FilterCriteria filterCritera, int pageSize, int rowOffset) throws RemoteException;

	public abstract AttributeMap updateTask(AttributeMap task) throws RemoteException;

	public abstract void deleteTask(long taskId) throws RemoteException;

	public abstract FilterCriteria createFilterCriteria();

	public abstract boolean equals(Object obj);

	public abstract String toString();

}