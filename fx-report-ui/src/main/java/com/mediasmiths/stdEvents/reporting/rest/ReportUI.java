package com.mediasmiths.stdEvents.reporting.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Path("/query")
public interface ReportUI
{
	@GET
	@Path("/report")
	public String getReport();
	
	@GET
	@Path("/popup")
	public String getPopup();
	
	@GET
	@Path("/parent")
	public String getParent();
	
	@GET
	@Path("/search_namespace/namespace")
	@Produces("text/html")
	public String getByNamespace(@QueryParam("namespace")String namespace);
	
	@GET
	@Path("/search_namespace_window/namespace")
	@Produces("text/html")
	public String getByNamespaceWindow(@QueryParam("namespace")String namespace);
	
	@GET
	@Path("/search_eventname/eventname")
	@Produces("text/html")
	public String getByEventName(@QueryParam("eventname")String eventname);
	
	@GET
	@Path("/search_namespace_eventname/namespace/eventname")
	@Produces("text/html")
	public String getByNamespaceEventname(@QueryParam("namespace")String namespace, @QueryParam("eventname")String eventname);
	
	@GET
	@Path("/search_id/id")
	@Produces("text/html")
	public String getById(@QueryParam("id")Long id);
	
	@GET
	@Path("/start_date/date/month/year")
	@Produces("text/html")
	public void saveStartDate(@QueryParam("date")String date, @QueryParam("month")String month, @QueryParam("year")String year);
	
	@GET
	@Path("/end_date/date/month/year")
	@Produces("text/html")
	public void saveEndDate(@QueryParam("date")String date, @QueryParam("month")String month, @QueryParam("year")String year);
	
	@GET
	@Path("/report_name/name")
	@Produces("text/html")
	public void saveReportName(@QueryParam("name")String name);
	
	@GET
	@Path("/order_status_csv")
	@Produces("text/html")
	public void getOrderStatusCSV();
	
	@GET
	@Path("/order_status_ui")
	@Produces("text/html")
	public String getOrderStatusUI();
	
	@GET
	@Path("/acquisition_report_csv")
	@Produces("text/html")
	public void getAquisitionReportCSV();

	@GET
	@Path("/manual_qa_csv")
	@Produces("text/html")
	public void getManualQACSV();
	
	@GET
	@Path("/auto_qc_csv")
	@Produces("text/html")
	public void getAutoQCCSV();

	@GET
	@Path("/task_list_csv")
	@Produces("text/html")
	public void getTaskListCSV();
	
	@GET
	@Path("/purge_content_csv")
	@Produces("text/html")
	public void getPurgeContentCSV();
	
	@GET
	@Path("/purge_content_ui")
	@Produces("text/html")
	public String getPurgeContentUI();

	@GET
	@Path("/compliance_edit_csv")
	@Produces("text/html")
	public void getComplianceEditCSV();
	
	@GET
	@Path("/export_csv")
	@Produces("text/html")
	public void getExportCSV();
	
	@GET
	@Path("/watch_folder_storage_csv")
	@Produces("text/html")
	public void getWatchFolderStorageCSV();
	
	@GET
	@Path("/transcoder_load_csv")
	@Produces("text/html")
	public void getTranscoderLoadCSV();
	
	@POST
	@Path("/display_path/path")
	@Produces("text/html")
	public String displayPath(@QueryParam("path")String path);
	

//	
//	@GET
//	@Path("/order_status_pdf")
//	@Produces("text/html")
//	public void getOrderStatusPDF();
//	
//	@GET
//	@Path("/late_order_status_ui")
//	@Produces("text/html")
//	public String getLateOrderStatusUI();
//	
//	
//	@GET
//	@Path("/late_order_status_csv")
//	@Produces("text/html")
//	public void getLateOrderStatusCSV();
//	
//	@GET
//	@Path("/late_order_status_pdf")
//	@Produces("text/html")
//	public void getLateOrderStatusPDF();
//	
//	@GET
//	@Path("/acquisition_report_ui")
//	@Produces("text/html")
//	public String getAquisitionReportUI();
//	
//	@GET
//	@Path("/acquisition_report_pdf")
//	@Produces("text/html")
//	public void getAquisitionReportPDF();
//
//	@GET
//	@Path("/file_tape_ingest_ui")
//	@Produces("text/html")
//	public String getFileTapeIngestUI();
//	
//	@GET
//	@Path("/file_tape_ingest_csv")
//	@Produces("text/html")
//	public void getFileTapeIngestCSV();
//	
//	@GET
//	@Path("/file_tape_ingest_pdf")
//	@Produces("text/html")
//	public void getFileTapeIngestPDF();	
//	
//	@GET
//	@Path("/auto_qc_ui")
//	@Produces("text/html")
//	public String getAutoQCUI();
//	
//	@GET
//	@Path("/auto_qc_pdf")
//	@Produces("text/html")
//	public void getAutoQCPDF();
//	
//	
//	@GET
//	@Path("/purge_content_pdf")
//	@Produces("text/html")
//	public void getPurgeContentPDF();
//	
//	@GET
//	@Path("/compliance_edits_ui")
//	@Produces("text/html")
//	public String getComplianceEditsUI();
//	
}
