package com.mediasmiths.mq;

import com.mayam.wf.mq.MqDestination;

public class MediasmithsDestinations {
	public static final MqDestination TASKS = MqDestination.of("queue://mediasmiths.tasks");
	public static final MqDestination ASSETS = MqDestination.of("queue://mediasmiths.assets");
	public static final String MULE_QC_DESTINATION = "http://localhost:9085/qc";
	public static final String MULE_REPORTING_DESTINATION = "http://localhost:9085/reporting";
	public static final String TRANSCODE_INPUT_FILE = "f:\tcinput\test.mxf";
	public static final String TRANSCODE_OUTPUT_DIR = "f:\tcoutput";
	public static final String MULE_TRANSCODE_DESTINATION = "http://localhost:9085/tc";
}
