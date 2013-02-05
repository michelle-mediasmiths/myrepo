package com.mediasmiths.mayam;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.Aggregation;
import au.com.foxtel.cf.mam.pms.Aggregator;
import au.com.foxtel.cf.mam.pms.ClassificationEnumType;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.Library;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.Order;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import au.com.foxtel.cf.mam.pms.QualityCheckEnumType;
import au.com.foxtel.cf.mam.pms.Source;
import au.com.foxtel.cf.mam.pms.TapeType;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details.Supplier;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package.Segmentation;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme;
import com.mediasmiths.foxtel.generated.ruzz.DetailType;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIF;
import com.mediasmiths.mayam.validation.MayamValidator;
 
public class MayamClientStub implements MayamClient
{

	private final static Logger log = Logger.getLogger(MayamClientStub.class);

	public final static String NEW_TITLE_ID = "NEW_TITLE";
	public final static String EXISTING_TITLE_ID = "EXISTING_TITLE";
	public final static String ERROR_TITLE_ID = "ERROR_TITLE";
	public final static String PROTECTED_TITLE_ID = "PROTECTED_TITLE";

	public final static String NEW_MATERIAL_ID = "NEW_MATERIAL";
	public final static String EXISTING_MATERIAL_ID = "EXISTING_MATERIAL";
	public final static String ERROR_MATERIAL_ID = "ERROR_MATERIAL";

	public final static String PLACEHOLDER_MATERIAL = "PLACEHOLDER_MATERIAL";

	public final static String NEW_PACKAGE_ID = "NEW_PACKAGE";
	public final static String EXISTING_PACKAGE_ID = "EXISTING_PACKAGE";
	public final static String ERROR_PACKAGE_ID = "ERROR_PACKAGE";
	public final static String PROTECTED_PACKAGE_ID = "PROTECTED_PACKAGE";

	private final static String [] CHANNELS = new String[] {"D3F","ARN","BIO","HIT","COM","CIN","FOX","HST","LHO","LST","LSY","FBO","AO","MEV","FKC","FOD","AED","SOH", "MO1", "MO2", "MO3", "MO5", "MO6", "MO7", "SHF", "SHH"};

	@Override
	public void shutdown()
	{
		log.warn("unimplementented method in mayam client stub");

	}

	@Override
	public MayamClientErrorCode createTitle(CreateOrUpdateTitle title)
	{
		return createTitle(title.getTitleID());
	}

	@Override
	public MayamClientErrorCode createTitle(Title title)
	{
		return createTitle(title.getTitleID());
	}

	private MayamClientErrorCode createTitle(String titleID)
	{
		if (titleID.equals(NEW_TITLE_ID))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (titleID.equals(EXISTING_TITLE_ID) || titleID.equals(PROTECTED_TITLE_ID))
		{
			return MayamClientErrorCode.TITLE_CREATION_FAILED;
		}
		else if (titleID.equals(ERROR_TITLE_ID))
		{
			return MayamClientErrorCode.FAILURE;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	@Override
	public MayamClientErrorCode updateTitle(Title title)
	{
		return updateTitle(title.getTitleID());
	}

	@Override
	public MayamClientErrorCode updateTitle(CreateOrUpdateTitle title)
	{
		return updateTitle(title.getTitleID());
	}

	private MayamClientErrorCode updateTitle(String titleID)
	{

		if (titleID.equals(NEW_TITLE_ID))
		{
			return MayamClientErrorCode.TITLE_FIND_FAILED;
		}
		else if (titleID.equals(EXISTING_TITLE_ID) || titleID.equals(PROTECTED_TITLE_ID))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (titleID.equals(ERROR_TITLE_ID))
		{
			return MayamClientErrorCode.TASK_UPDATE_FAILED;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	@Override
	public MayamClientErrorCode purgeTitle(PurgeTitle title)
	{

		String titleID = title.getTitleID();

		if (titleID.equals(NEW_TITLE_ID))
		{
			return MayamClientErrorCode.TITLE_FIND_FAILED;
		}
		else if (titleID.equals(EXISTING_TITLE_ID) || titleID.equals(PROTECTED_TITLE_ID))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (titleID.equals(PROTECTED_TITLE_ID))
		{
			return MayamClientErrorCode.TITLE_DELETE_FAILED;
		}
		else if (titleID.equals(ERROR_TITLE_ID))
		{
			return MayamClientErrorCode.TITLE_DELETE_FAILED;
		}

		return MayamClientErrorCode.SUCCESS;

	}

	@Override
	public boolean titleExists(String titleID) throws MayamClientException
	{
		if (titleID.equals(NEW_TITLE_ID))
		{
			return false;
		}
		else if (titleID.equals(EXISTING_TITLE_ID) || titleID.equals(PROTECTED_TITLE_ID))
		{
			return true;
		}
		else if (titleID.equals(ERROR_TITLE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
		}

		return true;
	}

	private MayamClientErrorCode createMaterial(String materialID)
	{
		if (materialID.equals(NEW_MATERIAL_ID))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (materialID.equals(EXISTING_MATERIAL_ID) || materialID.startsWith(PLACEHOLDER_MATERIAL))
		{
			return MayamClientErrorCode.MATERIAL_CREATION_FAILED;
		}
		else if (materialID.equals(ERROR_MATERIAL_ID))
		{
			return MayamClientErrorCode.MATERIAL_CREATION_FAILED;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	@Override
	public MayamClientErrorCode createMaterial(MaterialType material, String titleID)
	{
		return createMaterial(material.getMaterialID());
	}

	@Override
	public String createMaterial(String titleID, MarketingMaterialType material, Material.Details details, Material.Title title) throws MayamClientException
	{

		if (titleID.equals(NEW_TITLE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
		}
		else if (titleID.equals(EXISTING_TITLE_ID) || titleID.equals(PROTECTED_TITLE_ID))
		{
			return EXISTING_MATERIAL_ID;
		}
		else if (titleID.equals(ERROR_TITLE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
		}

		return EXISTING_MATERIAL_ID;
	}

	private MayamClientErrorCode updateMaterial(String materialID)
	{
		if (materialID.equals(NEW_MATERIAL_ID))
		{
			return MayamClientErrorCode.MATERIAL_FIND_FAILED;
		}
		else if (materialID.equals(EXISTING_MATERIAL_ID) || materialID.startsWith(PLACEHOLDER_MATERIAL))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (materialID.equals(ERROR_MATERIAL_ID))
		{
			return MayamClientErrorCode.MATERIAL_UPDATE_FAILED;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	@Override
	public boolean updateMaterial(ProgrammeMaterialType material, Material.Details details, Material.Title title) throws MayamClientException
	{
		updateMaterial(material.getMaterialID());
		return isMaterialPlaceholder(material.getMaterialID());
	}

	@Override
	public MayamClientErrorCode updateMaterial(MaterialType material)
	{
		return updateMaterial(material.getMaterialID());
	}

	@Override
	public MayamClientErrorCode deleteMaterial(DeleteMaterial deleteMaterial)
	{
		String materialID = deleteMaterial.getMaterial().getMaterialID();

		if (materialID.equals(NEW_MATERIAL_ID))
		{
			return MayamClientErrorCode.MATERIAL_FIND_FAILED;
		}
		else if (materialID.equals(EXISTING_MATERIAL_ID) || materialID.startsWith(PLACEHOLDER_MATERIAL))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (materialID.equals(ERROR_MATERIAL_ID))
		{
			return MayamClientErrorCode.FAILURE;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	@Override
	public boolean materialExists(String materialID) throws MayamClientException
	{
		if (materialID.equals(NEW_MATERIAL_ID))
		{
			return false;
		}
		else if (materialID.equals(EXISTING_MATERIAL_ID) || materialID.startsWith(PLACEHOLDER_MATERIAL))
		{
			return true;
		}
		else if (materialID.equals(ERROR_MATERIAL_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}

		return true;
	}

	@Override
	public boolean isMaterialPlaceholder(String materialID) throws MayamClientException
	{
		if (materialID.startsWith(PLACEHOLDER_MATERIAL))
		{
			return true;
		}
		else if (materialID.equals(NEW_MATERIAL_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}
		else if (materialID.equals(ERROR_MATERIAL_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.FAILURE);
		}

		return false;
	}

	private MayamClientErrorCode createPackage(String packageID)
	{
		if (packageID.equals(NEW_PACKAGE_ID))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (packageID.equals(EXISTING_PACKAGE_ID) || packageID.equals(PROTECTED_PACKAGE_ID))
		{
			return MayamClientErrorCode.PACKAGE_CREATION_FAILED;
		}
		else if (packageID.equals(ERROR_PACKAGE_ID))
		{
			return MayamClientErrorCode.FAILURE;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	private MayamClientErrorCode updatePackage(String packageID)
	{
		if (packageID.equals(NEW_PACKAGE_ID))
		{
			return MayamClientErrorCode.PACKAGE_FIND_FAILED;
		}
		else if (packageID.equals(EXISTING_PACKAGE_ID) || packageID.equals(PROTECTED_PACKAGE_ID))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (packageID.equals(ERROR_PACKAGE_ID))
		{
			return MayamClientErrorCode.FAILURE;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	@Override
	public MayamClientErrorCode createPackage(PackageType txPackage)
	{
		return createPackage(txPackage.getPresentationID());
	}

	@Override
	public MayamClientErrorCode updatePackage(PackageType txPackage)
	{
		return updatePackage(txPackage.getPresentationID());
	}

	@Override
	public MayamClientErrorCode updatePackage(Package txPackage)
	{
		return updatePackage(txPackage.getPresentationID());
	}

	@Override
	public MayamClientErrorCode deletePackage(DeletePackage deletePackage)
	{
		String packageID = deletePackage.getPackage().getPresentationID();

		if (packageID.equals(NEW_PACKAGE_ID))
		{
			return MayamClientErrorCode.PACKAGE_FIND_FAILED;
		}
		else if (packageID.equals(EXISTING_PACKAGE_ID) || packageID.equals(PROTECTED_PACKAGE_ID))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (packageID.equals(PROTECTED_PACKAGE_ID))
		{
			return MayamClientErrorCode.PACKAGE_DELETE_FAILED;
		}
		else if (packageID.equals(ERROR_PACKAGE_ID))
		{
			return MayamClientErrorCode.FAILURE;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	public boolean packageExists(String presentationID) throws MayamClientException
	{
		if (presentationID.equals(NEW_PACKAGE_ID))
		{
			return false;
		}
		else if (presentationID.equals(EXISTING_PACKAGE_ID) || presentationID.equals(PROTECTED_PACKAGE_ID))
		{
			return true;
		}
		else if (presentationID.equals(ERROR_PACKAGE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.PACKAGE_FIND_FAILED);
		}

		return true;
	}

	@Override
	public boolean isMaterialForPackageProtected(String packageID) throws MayamClientException
	{
		if (packageID.equals(PROTECTED_PACKAGE_ID))
		{
			return true;
		}
		else
		{
			return false;
		}

	}

	@Override
	public boolean isTitleOrDescendentsProtected(String titleID) throws MayamClientException
	{
		if (titleID.equals(PROTECTED_TITLE_ID))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public MayamValidator getValidator()
	{
		return new MayamValidatorStub();
	}

	@Override
	public ArrayList<String> getChannelLicenseTagsForMaterial(String materialID) throws MayamClientException
	{
		if (materialExists(materialID))
		{
			ArrayList<String> list = new ArrayList<String>();
			list.add(CHANNELS[0]);
			return list;
		}
		else
		{
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}

	}


	@Override
	public String pathToMaterial(String materialID) throws MayamClientException
	{
		// TODO implement!
		
		return "/storage/mam/hires01/mediasmithstemp/input/TestMedia.mxf";
	}
	
	@Override
	public PackageType getPackage(String packageID) throws MayamClientException
	{

		if (packageID.equals(EXISTING_PACKAGE_ID)|| packageID.equals(PROTECTED_PACKAGE_ID))
		{
			PackageType pack = new PackageType();
			pack.setMaterialID(EXISTING_MATERIAL_ID);
			PresentationFormatType format = buildPresentationFormat("SD");
			pack.setPresentationFormat(format);
			ClassificationEnumType classification = buildClassification("PG");
			pack.setClassification(classification);
			pack.setConsumerAdvice("Generally suitable for all");
			pack.setNumberOfSegments(new BigInteger("4"));
			XMLGregorianCalendar xmlCal;

			try
			{
				xmlCal = getValidDate();
				pack.setTargetDate(xmlCal);
			}
			catch (DatatypeConfigurationException e)
			{
				log.error("error in mayam client stub", e);
			}

			pack.setNotes("none");
			pack.setPresentationID(packageID);

			return pack;
		}
		else if (packageID.equals(ERROR_PACKAGE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.FAILURE);
		}
		else
		{
			throw new MayamClientException(MayamClientErrorCode.PACKAGE_FIND_FAILED);
		}
	}

	private PresentationFormatType buildPresentationFormat(String v)
	{
		PresentationFormatType format = null;
		format = format.fromValue(v);
		return format;
	}

	private ClassificationEnumType buildClassification(String v)
	{
		ClassificationEnumType classification = null;
		classification = ClassificationEnumType.fromValue(v);
		return classification;
	}

	private XMLGregorianCalendar getValidDate() throws DatatypeConfigurationException
	{

		Calendar calDate = new GregorianCalendar();
		int numberOfDaysToAdd = (int) (Math.random() * 730 + 1);
		calDate.add(Calendar.DAY_OF_YEAR, numberOfDaysToAdd);

		GregorianCalendar gregDate = new GregorianCalendar();
		gregDate.setTimeInMillis(calDate.getTimeInMillis());

		XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregDate);
		return xmlDate;
	}

	@Override
	public MaterialType getMaterial(String materialID) throws MayamClientException
	{
		if (materialID.equals(EXISTING_MATERIAL_ID) || materialID.startsWith(PLACEHOLDER_MATERIAL))
		{

			MaterialType m = new MaterialType();
			Order order = new Order();
			// build request
			try
			{
				XMLGregorianCalendar orderCreated = DatatypeFactory.newInstance().newXMLGregorianCalendar(
						new GregorianCalendar(2013, 1, 1, 0, 0, 1));
				XMLGregorianCalendar requiredBy = DatatypeFactory.newInstance().newXMLGregorianCalendar(
						new GregorianCalendar(2013, 1, 10, 0, 0, 1));
				order.setOrderCreated(orderCreated);
				m.setRequiredBy(requiredBy);

			}
			catch (DatatypeConfigurationException e)
			{
				log.error("error in mayam client stub", e);
			}

			order.setOrderReference("abc123");

			Aggregator aggregator = new Aggregator();
			aggregator.setAggregatorID("def456");
			aggregator.setAggregatorName("aggregator");

			Aggregation aggregation = new Aggregation();
			aggregation.setOrder(order);
			aggregation.setAggregator(aggregator);

			TapeType tape = new TapeType();
			tape.setPresentationID("def456");
			tape.setLibraryID("ghi789");

			Library library = new Library();
			library.getTape().add(tape);

			Source s = new Source();
			s.setAggregation(aggregation);

			m.setMaterialID(materialID);
			m.setRequiredFormat("SD");
			QualityCheckEnumType qualityCheck = null;
			qualityCheck = qualityCheck.fromValue("AutomaticOnIngest");
			m.setQualityCheckTask(qualityCheck);
			m.setSource(s);

			return m;
		}
		else if (materialID.equals(ERROR_MATERIAL_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.FAILURE);
		}
		else
		{
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}
	}

	@Override
	public AttributeMap getTaskForAsset(MayamTaskListType type, String id) throws MayamClientException
	{
		log.info("returning empty attriburte map from mayam client stub");
		return new AttributeMap();
	}

	@Override
	public void saveTask(AttributeMap task) throws MayamClientException
	{
		log.info("unimplemented save task in mayam client stub");
	}

	@Override
	public void failTaskForAsset(MayamTaskListType txDelivery, String id) throws MayamClientException
	{
		log.info("unimplemented failTaskForAsset in mayam client stub");
	}

//	public ProgrammeMaterialType getProgrammeType(String materialID) throws MayamClientException
//	{
//		if (materialID.equals(EXISTING_MATERIAL_ID) || materialID.startsWith(PLACEHOLDER_MATERIAL))
//		{
//		
//			FileMediaType fmt = new FileMediaType();
//			fmt.setFilename("myfile.mxf");
//			fmt.setFormat(FileFormatEnumType.MXF_OP_1_A_IMX_D_10_50);
//
//			Track t = new Track();
//			t.setTrackName(AudioTrackEnumType.CENTER);
//			t.setTrackEncoding(AudioEncodingEnumType.PCM);
//			t.setTrackNumber(1);
//
//			AudioTracks at = new AudioTracks();
//			at.getTrack().add(t);
//
//			Segment sg = new Segment();
//			sg.setSegmentNumber(1);
//			sg.setSegmentTitle("Segment title");
//			sg.setSOM("00:00:00:00");
//			sg.setEOM("00:00:00:00");
//
//			SegmentationType st = new SegmentationType();
//			st.getSegment().add(sg);
//
//			ProgrammeMaterialType pmaterial = new ProgrammeMaterialType();
//			pmaterial.setMaterialID(materialID);
//			pmaterial.setFormat("HD");
//			pmaterial.setAspectRatio("16F16");
//			pmaterial.setFirstFrameTimecode("00:00:00:00");
//			pmaterial.setLastFrameTimecode("00:00:01:00");
//			pmaterial.setDuration("00:00:01:00");
//			pmaterial.setMedia(fmt);
//			pmaterial.setAudioTracks(at);
//			pmaterial.setOriginalConform(st);
//			pmaterial.setAdditionalProgrammeDetail("foo");
//			pmaterial.setAdultMaterial(false);
//									
//			Presentation presentation = new Presentation();
//			pmaterial.setPresentation(presentation);
//
//			String[] packageIds = new String[] { EXISTING_PACKAGE_ID };
//
//			for (String packageID : packageIds)
//			{
//				Package p = getPresentationPackage(packageID);
//				presentation.getPackage().add(p);
//
//			}
//
//			return pmaterial;
//
//		}
//		else if (materialID.equals(ERROR_MATERIAL_ID))
//		{
//			throw new MayamClientException(MayamClientErrorCode.FAILURE);
//		}
//		else
//		{
//			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
//		}
//	}

	@Override
	public Package getPresentationPackage(String packageID) throws MayamClientException
	{

		if (packageID.equals(EXISTING_PACKAGE_ID)|| packageID.equals(PROTECTED_PACKAGE_ID))
		{
			Package p = new Package();
			Segment s = new Segment();
			s.setSegmentNumber(1);
			s.setSegmentTitle("Segment title");
			s.setSOM("00:00:00:00");
			s.setEOM("00:00:00:00");

			Segmentation seg = new Segmentation();
			seg.getSegment().add(s);

			p.setPresentationID(packageID);
			p.setSegmentation(seg);
			return p;

		}
		else if (packageID.equals(ERROR_PACKAGE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.FAILURE);
		}
		else
		{
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}

	}

	@Override
	public String getMaterialIDofPackageID(String packageID) throws MayamClientException
	{
		if (packageID.equals(ERROR_PACKAGE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.FAILURE);
		}
		else if (packageID.equals(NEW_PACKAGE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.PACKAGE_FIND_FAILED);
		}

		return EXISTING_MATERIAL_ID;
	}

	@Override
	public void createTxDeliveryFailureTask(String packageID, String failureReason) throws MayamClientException
	{
		log.info("unimplemented createTxDeliveryFailureTask in mayam client stub");
	}

	@Override
	public Title getTitle(String titleID, boolean includePackages) throws MayamClientException
	{
		if (titleID.equals(NEW_TITLE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
		}
		else if (titleID.equals(EXISTING_TITLE_ID) || titleID.equals(PROTECTED_TITLE_ID))
		{
			Title t = new Title();
			t.setTitleID(titleID);
			
			return t;
		}
		else
		{
			throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
		}
		
	
	}

	@Override
	public String getTitleOfPackage(String packageID) throws MayamClientException
	{
		if (packageID.equals(EXISTING_PACKAGE_ID)|| packageID.equals(PROTECTED_PACKAGE_ID))
		{
			return EXISTING_TITLE_ID;

		}
		else if (packageID.equals(ERROR_PACKAGE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.FAILURE);
		}
		else
		{
			throw new MayamClientException(MayamClientErrorCode.PACKAGE_FIND_FAILED);
		}
	}

	@Override
	public Details getSupplierDetails(String materialID) throws MayamClientException
	{
		if (materialID.equals(EXISTING_MATERIAL_ID) || materialID.startsWith(PLACEHOLDER_MATERIAL))
		{
			Supplier supplier = new Supplier();
			supplier.setSupplierID(RandomStringUtils.randomAlphabetic(10));

			Details details = new Details();
			details.setSupplier(supplier);
			try
			{
				details.setDateOfDelivery(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
			}
			catch (DatatypeConfigurationException e)
			{
				log.error("error in mayam client stub", e);
			}
			details.setDeliveryVersion(new BigInteger("1"));

			return details;

		}
		else if (materialID.equals(ERROR_MATERIAL_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.FAILURE);
		}
		else
		{
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}
	}

	@Override
	public void updateMaterial(DetailType details, String materialID)
	{
		updateMaterial(materialID);
	}

	@Override
	public long createWFEErrorTaskNoAsset(String id, String title, String message) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Programme getProgramme(String packageID) throws MayamClientException
	{
		//TODO bulk out stub
		return new Programme();
	}

	@Override
	public RuzzIF getRuzzProgramme(String packageID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTitleAO(String titleID) throws MayamClientException
	{
			// TODO Auto-generated method stub
			return false;
	}

	@Override
	public boolean isPackageAO(String packageID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void autoQcFailedForMaterial(String assetId, long taskID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void autoQcPassedForMaterial(String assetId, long taskID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attachFileToMaterial(String materialID, String absolutePath, String serviceHandle) throws MayamClientException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public AttributeMap getMaterialAttributes(String materialID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AttributeMap getPackageAttributes(String packageID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLastDeliveryVersionForMaterial(String materialID)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean materialHasPassedPreview(String materialID)
	{
		return false;
	}

	@Override
	public long createWFEErrorTaskForPackage(String packageID, String message)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long createWFEErrorTaskForMaterial(String materialID, String message)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long createWFEErrorTaskForTitle(String titleID, String message)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean attemptAutoMatch(String siteID, String fileName)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean autoQcRequiredForTXTask(Long taskID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPackageSD(String packageID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void txDeliveryCompleted(String packageID, long taskID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void txDeliveryFailed(String packageID, long taskID, String stage) throws MayamClientException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public long createWFEErrorTaskForUnmatched(String aggregator,
			String fileName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TaskState getTaskState(long taskid) throws MayamClientException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean titleIsAO(String titleID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return false;
	}

}
