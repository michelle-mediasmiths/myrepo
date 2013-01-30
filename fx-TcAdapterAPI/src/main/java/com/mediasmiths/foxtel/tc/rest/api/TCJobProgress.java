package com.mediasmiths.foxtel.tc.rest.api;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum TCJobProgress
{
	/**
	 * A non-terminal state indicating that the job has not yet completed
	 */
	INCOMPLETE,
	/**
	 * A terminal state indicating that the job has completed successfully
	 */
	SUCCESS,
	/**
	 * A terminal state indicating that the job has completed with error(s)
	 */
	FAILURE;
}
