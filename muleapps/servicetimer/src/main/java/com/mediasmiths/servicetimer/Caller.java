package com.mediasmiths.servicetimer;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.mediasmiths.foxtel.ip.utils.Heartbeat;
import com.mediasmiths.foxtel.ip.utils.impl.HeartbeatStub;

public class Caller extends QuartzJobBean {

	public static final transient Logger logger = Logger.getLogger(Caller.class);
	
	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {
		
		logger.info("Service called");
		Heartbeat heartbeat = new HeartbeatStub();
		
		heartbeat.beat();
		heartbeat.setStatus(true);
		
		//TO BE USED IF HEARTBEAT WAR IS DEPLOYED AS SERVICE
		/*try {
			callRestService();
		}
		catch (IOException e) {
			e.printStackTrace();
		}*/
		
	}
	
	//TO BE USED IF HEARTBEAT WAR IS DEPLOYED AS SERVICE
	private void callRestService() throws ClientProtocolException, IOException 
	{
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPut httpPut = new HttpPut("http://localhost:8080/<NAME_OF_WAR>/heartbeat/beat");
		HttpResponse httpResponsePut = httpClient.execute(httpPut);
		HttpEntity httpEntityPut = httpResponsePut.getEntity();
		EntityUtils.consume(httpEntityPut);
		
		HttpGet httpGet = new HttpGet("http://localhost:8080/<NAME_OF_WAR>/heartbeat/setStatus/status?status=true");
		HttpResponse httpResponseGet = httpClient.execute(httpGet);
		HttpEntity httpEntityGet = httpResponseGet.getEntity();
		EntityUtils.consume(httpEntityGet);
	}

} 
