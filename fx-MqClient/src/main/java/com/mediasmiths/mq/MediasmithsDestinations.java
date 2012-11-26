package com.mediasmiths.mq;

import com.mayam.wf.mq.MqDestination;

public class MediasmithsDestinations {
	public static final MqDestination TASKS = MqDestination.of("queue://ToWFE");
	public static final MqDestination ASSETS = MqDestination.of("queue://ToWFE");
	public static final String MULE_QC_DESTINATION = "http://localhost:8088/qc";
	public static final String MULE_REPORTING_DESTINATION = "http://localhost:8088/reporting";
	public static final String TRANSCODE_INPUT_FILE = "f:\tcinput\test.mxf";
	public static final String TRANSCODE_OUTPUT_DIR = "f:\tcoutput";
	public static final String MULE_TRANSCODE_DESTINATION = "http://localhost:8088/tc";
	public static final String MAYAM_TASKS_SERVER = "http://localhost:8084/tasks-ws";
}
