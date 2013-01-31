package com.mediasmiths.mq;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mayam.wf.mq.MqDestination;

@Singleton
public class MediasmithsDestinations
{
	private final MqDestination tasks;
	private final MqDestination assets;
	private final MqDestination incoming;
	
	@Inject
	@Named("mule.qc.dest")
	private String mule_qc_destination;
	@Inject
	@Named("mule.reporting.dest")
	private String mule_reporting_destination;
	@Inject
	@Named("mule.tc.dest")
	private String mule_tc_destination;
	@Inject
	@Named("mule.tx.dest")
	private String muleTxDestinaion;
	@Inject
	@Named("mule.export.dest")
	private String muleExportDestination;

	
	@Inject
	public MediasmithsDestinations(
			@Named("mq.destination.incoming") String incomingDest,
			@Named("mq.destination.assets") String assetsDest,
			@Named("mq.destination.tasks") String tasksDest)
	{
		tasks = MqDestination.of(tasksDest);
		assets = MqDestination.of(assetsDest);
		incoming = MqDestination.of(incomingDest);
	}
	
	public MqDestination getTasks()
	{
		return tasks;
	}


	public MqDestination getAssets()
	{
		return assets;
	}


	public MqDestination getIncoming()
	{
		return incoming;
	}

	public String getMule_qc_destination()
	{
		return mule_qc_destination;
	}

	public String getMule_reporting_destination()
	{
		return mule_reporting_destination;
	}

	public String getMule_tc_destination()
	{
		return mule_tc_destination;
	}

	public String getMuleExportDestination()
	{
		return muleExportDestination;
	}

	public String getMule_tx_destination()
	{
		return muleTxDestinaion;
	}

}
