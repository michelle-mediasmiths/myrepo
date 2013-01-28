package com.mediasmiths.stdEvents.report.jasper;

import java.util.HashMap;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;

@Path("/jasper")
public interface JasperAPI
{
	@GET
	@Path("/create")
	@Produces("text/plain")
	public void createReport(JRBeanCollectionDataSource beanCol, HashMap<String, Object> param, String rptName);
	
	@GET
	@Path("/create_order_status")
	@Produces("text/plain")
	public void createOrderStatus(List<EventEntity> delivered, List<EventEntity> notDelivered, List<EventEntity> outstanding, List<EventEntity> unmatched,
			String deliveredQ, String notDeliveredQ, String overdueQ, String unmatchedQ);
	
	@GET
	@Path("/create_late_order_status")
	@Produces("text/plain")
	public void createLateOrderStatus(List<EventEntity> outstanding, List<EventEntity> unmatched, String outstandingQ, String unmatchedQ);
	
	@GET
	@Path("/create_acquisition")
	@Produces("text/plain")
	public void createAcquisition(List<EventEntity> events, String noTape, String noFile, String perTape,  String perFile);
	
	@GET
	@Path("/create_file_tape_ingest")
	@Produces("text/plain")
	public void createFileTapeIngest(List<EventEntity> events);
	
	@GET
	@Path("/create_auto_qc")
	@Produces("text/plain")
	public void createAutoQc(List<EventEntity> events, String totalQCd, String QCFailed);
	
	@GET
	@Path("/create_purge_content")
	@Produces("text/plain")
	public void createPurgeContent(List<EventEntity> events, String amtPurged, String amtExpired, String amtProtected);
}
