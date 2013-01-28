package com.mediasmiths.stdEvents.report.jasper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.db.entity.ContentPickup;
import com.mediasmiths.stdEvents.events.db.entity.PlaceholderMessage;
import com.mediasmiths.stdEvents.events.db.entity.QC;
import com.mediasmiths.stdEvents.events.db.marshaller.PlaceholderMarshaller;
import com.mediasmiths.stdEvents.persistence.db.impl.AddOrUpdateMaterialImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.AddOrUpdatePackageImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.ContentPickupDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.CreateOrUpdateTitleImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.PlaceholderMessageDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.QCDaoImpl;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class JasperImpl implements JasperAPI
{
	public static final transient Logger logger = Logger.getLogger(JasperImpl.class);
	
	@Inject
	private ContentPickupDaoImpl contentPickupDao;
	@Inject
	private PlaceholderMessageDaoImpl placeholderDao;
	@Inject
	private QCDaoImpl qcDao;
	
	@Inject
	protected Injector injector;
	
	List<PlaceholderMessage> createTitle = new ArrayList<PlaceholderMessage>();
	List<PlaceholderMessage> addMaterial = new ArrayList<PlaceholderMessage>();
	List<PlaceholderMessage> addPackage = new ArrayList<PlaceholderMessage>();
	
	Map<String, PlaceholderMarshaller<?>> placeholderType = createPlaceholderMap();
	
	private Map<String, PlaceholderMarshaller<?>> createPlaceholderMap()
	{
		Map<String, PlaceholderMarshaller<?>> placeholderType = new HashMap<String,PlaceholderMarshaller<?>>();
		placeholderType.put("CreateOrUpdateTitle", new CreateOrUpdateTitleImpl());
		placeholderType.put("AddOrUpdateMaterial", new AddOrUpdateMaterialImpl());
		placeholderType.put("AddOrUpdatePackage", new AddOrUpdatePackageImpl());
		
		return placeholderType;
	}
	
	public void getPlaceholderLists(List<EventEntity> events)	
	{
		createTitle.clear();
		addMaterial.clear();
		addPackage.clear();
		
		for (EventEntity event : events)
		{
			PlaceholderMessage message = placeholderDao.get(event);
			String action = event.getEventName();
			logger.info("EventName: " + action);
			
			if (action.equals("CreateOrUpdateTitle"))
			{
				createTitle.add(message);
			}
			else if(action.equals("AddOrUpdateMaterial"))
			{
				addMaterial.add(message);
			}		
			else if (action.equals("AddOrUpdatePackage"));
			{
				addPackage.add(message);
			}
		}
	}

	public <T> ArrayList<T> toBeanArray(List<T> items)
	{	
		logger.info("Conveting list to bean array...");
		ArrayList<T> arrayList = new ArrayList<T>();
		for (T item : items)
		{
			logger.info("Adding item to array...");
			arrayList.add(item);
		}
		System.out.println(arrayList);
		return arrayList;
	}
	
	@SuppressWarnings("unchecked")
	public <T> JRBeanCollectionDataSource getPlaceholderBeanCol (List<PlaceholderMessage> messages, String action)
	{
		List<T> placeholderList = new ArrayList<T>();
		logger.info("Creating placeholder list...");
		logger.info("Incoming messages: " + messages);
		for (PlaceholderMessage message : messages)
		{
			logger.info("Actions " + action);
			
			PlaceholderMarshaller<?> daoClass = placeholderType.get(action);
			if (daoClass != null) {
				Object placeholder = daoClass.get(message);
				placeholderList.add((T) placeholder);
			}
		}
		logger.info("PlaceholderList: " + placeholderList.toString());
		logger.info("Creating bean array...");
		ArrayList<T> rptArray = toBeanArray(placeholderList);
		logger.info("RptArray: " + rptArray.toString());
		JRBeanCollectionDataSource beanCol = new JRBeanCollectionDataSource(rptArray);
		
		return beanCol;
	}

	@Override
	public void createReport(JRBeanCollectionDataSource beanCol, HashMap<String,Object> param, String rptName)
	{
		logger.info("Creating report...");
		JasperReport jasperReport;
		JasperPrint jasperPrint;
		
		try {
			logger.info("Compiling report...");
			jasperReport = JasperCompileManager.compileReport("/Users/alisonboal/Documents/PersistenceInterface/Templates/" + rptName + ".jrxml");
			jasperPrint = JasperFillManager.fillReport(jasperReport, param, beanCol);
			logger.info("Exporting to PDF...");
			JasperExportManager.exportReportToPdfFile(jasperPrint, "/Users/alisonboal/Documents/PersistenceInterface/Reports/" + rptName + ".pdf");
			logger.info("Report saved");
		}
		catch (JRException e) {
			e.printStackTrace();
		}
	}
	
	private void mergePDF(List<String> pdfs, String destination)
	{
		PDFMergerUtility merger = new PDFMergerUtility();
		
		for (String pdf : pdfs)
		{
			merger.addSource(new File("/Users/alisonboal/Documents/PersistenceInterface/Reports/" + pdf + ".pdf"));
		}
	
		merger.setDestinationFileName("/Users/alisonboal/Documents/PersistenceInterface/Reports/" + destination + ".pdf");
		
		try
		{
			merger.mergeDocuments();
		}
		catch (COSVisitorException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void createOrderStatus(List<EventEntity> delivered, List<EventEntity> notDelivered, List<EventEntity> outstanding, List<EventEntity> unmatched, 
			String deliveredQ, String notDeliveredQ, String overdueQ, String unmatchedQ)
	{			
		logger.info("Adding order status parameters...");
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("deliveredQ", "0");
		param.put("notDeliveredQ", "0");
		param.put("overdueQ", "0");
		param.put("unmatchedQ", "0");
		
		getPlaceholderLists(delivered);

		JRBeanCollectionDataSource deliveredTitleBeanCol = getPlaceholderBeanCol(createTitle, "CreateOrUpdateTitle");
		JRBeanCollectionDataSource deliveredMaterialBeanCol = getPlaceholderBeanCol(addMaterial, "AddOrUpdateMaterial");
		JRBeanCollectionDataSource deliveredPackageBeanCol = getPlaceholderBeanCol(addPackage, "AddOrUpdatePackage");

		createReport(deliveredTitleBeanCol, new HashMap(), "order_status_delivered_title");
		createReport(deliveredMaterialBeanCol, new HashMap(), "order_status_delivered_material");
		createReport(deliveredPackageBeanCol, new HashMap(), "order_status_delivered_package");
	
		getPlaceholderLists(notDelivered);
		
		JRBeanCollectionDataSource notDeliveredTitleBeanCol = getPlaceholderBeanCol(createTitle, "CreateOrUpdateTitle");
		JRBeanCollectionDataSource notDeliveredMaterialBeanCol = getPlaceholderBeanCol(addMaterial, "AddOrUpdateMaterial");
		JRBeanCollectionDataSource notDeliveredPackageBeanCol = getPlaceholderBeanCol(addPackage, "AddOrUpdatePackage");
		
		createReport(notDeliveredTitleBeanCol, new HashMap(), "order_status_not_delivered_title");
		createReport(notDeliveredMaterialBeanCol, new HashMap(), "order_status_not_delivered_material");
		createReport(notDeliveredPackageBeanCol, new HashMap(), "order_status_not_delivered_package");
		
		getPlaceholderLists(outstanding);
		
		JRBeanCollectionDataSource outstandingTitleBeanCol = getPlaceholderBeanCol(createTitle, "CreateOrUpdateTitle");
		JRBeanCollectionDataSource outstandingMaterialBeanCol = getPlaceholderBeanCol(addMaterial, "AddOrUpdateMaterial");
		JRBeanCollectionDataSource outstandingPackageBeanCol = getPlaceholderBeanCol(addPackage, "AddOrUpdatePackage");
		
		createReport(outstandingTitleBeanCol, new HashMap(), "order_status_outstanding_title");
		createReport(outstandingMaterialBeanCol, new HashMap(), "order_status_outstanding_material");
		createReport(outstandingPackageBeanCol, new HashMap(), "order_status_outstanding_package");
		
		List<ContentPickup> contents = new ArrayList<ContentPickup>();
		for (EventEntity un : unmatched)
		{
			ContentPickup content = contentPickupDao.get(un);
			contents.add(content);
		}
		ArrayList<ContentPickup> rptArray = toBeanArray(contents);
		JRBeanCollectionDataSource unmatchedBeanCol = new JRBeanCollectionDataSource(rptArray);
		createReport(unmatchedBeanCol, param, "order_status_unmatched");
				
		List<String> pdfs = Arrays.asList("order_status_delivered_title", "order_status_delivered_material", "order_status_delivered_package", 
				"order_status_not_delivered_title", "order_status_not_delivered_material", "order_status_not_delivered_package",
				"order_status_outstanding_title", "order_status_outstanding_material", "order_status_outstanding_package",
				"order_status_unmatched");
		mergePDF(pdfs, "order_status_merged");
	}
	
	@Override
	public void createLateOrderStatus(List<EventEntity> outstanding, List<EventEntity> unmatched, String outstandingQ, String unmatchedQ)
	{
		logger.info("Adding late order status parameters...");
		List<PlaceholderMessage> createTitle = new ArrayList<PlaceholderMessage>();
		List<PlaceholderMessage> addMaterial = new ArrayList<PlaceholderMessage>();
		List<PlaceholderMessage> addPackage = new ArrayList<PlaceholderMessage>();
		String action = null;

		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("outstandingQ", outstandingQ);
		param.put("unmatchedQ", unmatchedQ);
		
		getPlaceholderLists(outstanding);
		
		JRBeanCollectionDataSource outstandingTitleBeanCol = getPlaceholderBeanCol(createTitle, "CreateOrUpdateTitle");
		JRBeanCollectionDataSource outstandingMaterialBeanCol = getPlaceholderBeanCol(addMaterial, "AddOrUpdateMaterial");
		JRBeanCollectionDataSource outstandingPackageBeanCol = getPlaceholderBeanCol(addPackage, "AddOrUpdatePackage");
		
		createReport(outstandingTitleBeanCol, new HashMap(), "late_order_status_outstanding_title");
		createReport(outstandingMaterialBeanCol, new HashMap(), "late_order_status_outstanding_material");
		createReport(outstandingPackageBeanCol, new HashMap(), "late_order_status_outstanding_package");
		
		List<ContentPickup> contents = new ArrayList<ContentPickup>();
		for (EventEntity un : unmatched)
		{
			ContentPickup content = contentPickupDao.get(un);
			contents.add(content);
		}
		ArrayList<ContentPickup> rptArray = toBeanArray(contents);
		JRBeanCollectionDataSource unmatchedBeanCol = new JRBeanCollectionDataSource(rptArray);
		createReport(unmatchedBeanCol, param, "late_order_status_unmatched");
		
		List<String> pdfs = Arrays.asList("late_order_status_outstanding_title", "late_order_status_outstanding_material", "late_order_status_outstanding_package", "late_order_status_unmatched");
		mergePDF(pdfs, "late_order_status_merged");

	}

	@Override
	public void createAcquisition(List<EventEntity> events, String noTape, String noFile, String perTape, String perFile)
	{
		logger.info("Adding acquisition parameters...");
		
		List<ContentPickup> materialList = new ArrayList<ContentPickup>();
		for (EventEntity event : events)
		{
			ContentPickup material = contentPickupDao.get(event);
			materialList.add(material);
		}
		
		ArrayList<ContentPickup> rptArray = toBeanArray(materialList);
		JRBeanCollectionDataSource beanCol = new JRBeanCollectionDataSource(rptArray);
		
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("noTape", noTape);
		param.put("noFile", noFile);
		param.put("perTape", perTape);
		param.put("perFile", perFile);
		
		createReport(beanCol, param, "acquisition_delivery");
	}

	@Override
	public void createFileTapeIngest(List<EventEntity> events)
	{
		logger.info("No file tape ingest parameters");
		
		List<ContentPickup> materialList = new ArrayList<ContentPickup>();
		for (EventEntity event : events)
		{
			ContentPickup material = contentPickupDao.get(event);
			materialList.add(material);
		}
		
		ArrayList<ContentPickup> rptArray = toBeanArray(materialList);
		JRBeanCollectionDataSource beanCol = new JRBeanCollectionDataSource(rptArray);
		
		createReport(beanCol, new HashMap<String, Object>(), "file_tape_ingest");
	}

	@Override
	public void createAutoQc(List<EventEntity> events, String totalQCd, String QCFailed)
	{
		logger.info("Adding parameters for quto qc...");
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("totalQCd", totalQCd);
		param.put("QCFailed", QCFailed);
		
		List<QC> qcList = new ArrayList<QC>();
		for (EventEntity event : events)
		{
			QC qc = qcDao.get(event);
			qcList.add(qc);
		}
		
		ArrayList<QC> rptArray = toBeanArray(qcList);
		JRBeanCollectionDataSource beanCol = new JRBeanCollectionDataSource(rptArray);
		
		createReport(beanCol, param, "auto_qc");
	}

	@Override
	public void createPurgeContent(List<EventEntity> events, String amtPurged, String amtExpired, String amtProtected)
	{
		logger.info("Adding parameters for purge content...");
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("amtPurged", amtPurged);
		param.put("amtExpired", amtExpired);
		param.put("amyProtected", amtProtected);
		
		List<PlaceholderMessage> purged = new ArrayList<PlaceholderMessage>();
		for (EventEntity event : events)
		{
			PlaceholderMessage message = placeholderDao.get(event);
			purged.add(message);
		}
		JRBeanCollectionDataSource purgedBeanCol = getPlaceholderBeanCol(purged, "PurgeTitle");
		createReport(purgedBeanCol, param, "purge_content");
	}
	
	
}
