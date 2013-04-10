package com.mediasmiths.foxtel.mpa;

import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.MEDIA_COMPANION_TIMEOUT;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;
import com.mediasmiths.foxtel.generated.MaterialExchange.AudioEncodingEnumType;
import com.mediasmiths.foxtel.generated.MaterialExchange.AudioTrackEnumType;
import com.mediasmiths.foxtel.generated.MaterialExchange.FileFormatEnumType;
import com.mediasmiths.foxtel.generated.MaterialExchange.FileMediaType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details.Supplier;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks.Track;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package.Segmentation;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment;
import com.mediasmiths.foxtel.mpa.delivery.DoNothingImporter;
import com.mediasmiths.foxtel.mpa.delivery.Importer;
import com.mediasmiths.foxtel.mpa.guice.MediaPickupModule;
import com.mediasmiths.foxtel.mpa.processing.DoNothingUnmatchedMaterial;
import com.mediasmiths.foxtel.mpa.processing.SingleMessageProcessor;
import com.mediasmiths.foxtel.mpa.processing.UnmatchedMaterialProcessor;
import com.mediasmiths.foxtel.mpa.validation.MediaPickupAgentConfigValidator;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.io.PropertyFile;

public class ProgrammeMaterialTest {

	protected MayamClient mayamClient;

	private static Logger logger = Logger
			.getLogger(ProgrammeMaterialTest.class);

	@Before
	public void before() {
		mayamClient = mock(MayamClient.class);
	}

	@Test
	@Ignore
	public void testProgrammeMaterialWithMxf() throws Exception {

		String incomingPath = TestUtil.prepareTempFolder("INCOMING");
		String receiptPath = TestUtil.prepareTempFolder("RECEIPT");
		String failurePath = TestUtil.prepareTempFolder("FAILURE");
		String archivePath = TestUtil.prepareTempFolder("ARCHIVE");
		String ardomeImportPath = TestUtil.prepareTempFolder("ARDOMEIMPORT");
		String ardomeEmergencyImportPath = TestUtil
				.prepareTempFolder("ARDOMEEMERGENCY");

		String messageName = RandomStringUtils.randomAlphabetic(10);

		String titleID = RandomStringUtils.randomAlphabetic(10);
		String materialID = RandomStringUtils.randomAlphabetic(10);
		
		Material material = getMaterialNoPackages(titleID,materialID);
		String materialPath =  incomingPath + IOUtils.DIR_SEPARATOR
				+ messageName + FilenameUtils.EXTENSION_SEPARATOR + "xml";
		logger.debug(String.format("Material path %s", materialPath));
		
		// write material message
		TestUtil.writeMaterialToFile(material,materialPath);
		// write 'media' file
		IOUtils.write(new byte[1000], new FileOutputStream(new File(
				incomingPath + IOUtils.DIR_SEPARATOR + messageName
						+ FilenameUtils.EXTENSION_SEPARATOR + "mxf")));

		when(mayamClient.titleExists(titleID)).thenReturn(true);
		when(mayamClient.materialExists(materialID)).thenReturn(true);
		when(mayamClient.isMaterialPlaceholder(materialID)).thenReturn(true);
		
		when(mayamClient.updateTitle((Title) anyObject())).thenReturn(MayamClientErrorCode.SUCCESS);
		when(mayamClient.updateMaterial((ProgrammeMaterialType) anyObject(), any(Material.Details.class), any(Material.Title.class))).thenReturn(true);
		
		startMediaPickupAgent(incomingPath, receiptPath, failurePath,
				archivePath, ardomeImportPath, ardomeEmergencyImportPath,
				"md5", "10000");

		// expect media file in ardome incoming
		File mediaInIncoming = new File(incomingPath + IOUtils.DIR_SEPARATOR
				+ messageName + FilenameUtils.EXTENSION_SEPARATOR + "mxf");
		logger.info("Looking for " + mediaInIncoming.getAbsolutePath());
		assertTrue(mediaInIncoming.exists());
		// expect material message in archive folder
		File archiveFile = new File(ardomeImportPath + IOUtils.DIR_SEPARATOR
				+ messageName + FilenameUtils.EXTENSION_SEPARATOR + "xml");
		logger.info("Looking for " + archiveFile.getAbsolutePath());
		assertTrue(archiveFile.exists());
	}

	private void startMediaPickupAgent(String messagePath, String receiptPath,
			String failurePath, String archivePath, String ardomeImportFolder,
			String ardomeEmergencyImportFolder, String digestAlgorithm,
			String companionTimeout) throws InterruptedException {

		// override some properties to use temp folders
		PropertyFile propertyFile = new PropertyFile();
		propertyFile.merge(PropertyFile.find("service.properties"));

		Properties overridenProperties = new Properties();
		overridenProperties.put(MEDIA_COMPANION_TIMEOUT, companionTimeout);
		propertyFile.merge(overridenProperties);

		// setup guice injector
		final List<Module> moduleList = Collections
				.<Module> singletonList(new TestMediaPickupModule(mayamClient));
		Injector injector = GuiceInjectorBootstrap.createInjector(propertyFile,
				new GuiceSetup() {

					@Override
					public void registerModules(List<Module> modules,
							PropertyFile config) {
						modules.addAll(moduleList);
					}

					@Override
					public void injectorCreated(Injector injector) {
					}
				});
		// run pickup agent
		MediaPickupAgent mpa = injector.getInstance(MediaPickupAgent.class);		
	}

	class TestMediaPickupModule extends MediaPickupModule {

		private final MayamClient mc;

		public TestMediaPickupModule(MayamClient mc) {
			this.mc = mc;
		}

		@Override
		protected void configure() {
			//we dont call super.configure as we want to be trying to bind the same thing twice
			bind(MayamClient.class).toInstance(mc);
			bind(MESSAGEPROCESSOR_LITERAL).to(SingleMessageProcessor.class);
			bind(ConfigValidator.class).to(MediaPickupAgentConfigValidator.class);
			
			bind(Importer.class).to(DoNothingImporter.class);
			bind(UnmatchedMaterialProcessor.class).to(DoNothingUnmatchedMaterial.class);
		}
	
	}
	
	public static void main(String[] args) throws DatatypeConfigurationException, FileNotFoundException, JAXBException, SAXException{
		
		List<String> packageids = new ArrayList<String>();
		packageids.add("package1");
		packageids.add("package2");
		packageids.add("package3");
		Material materialWithPackages = getMaterialWithPackages("titleid", "materialid",packageids);
		TestUtil.writeMaterialToFile(materialWithPackages,"/tmp/materialxml.xml");
	}
	
	public static Material getMaterialWithPackages(String titleID, String materialID, List<String> packageIds) throws DatatypeConfigurationException {
		
		Material m = getMaterialNoPackages(titleID, materialID);
		Presentation presentation = new Presentation();
		
		for(String packageID : packageIds){
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
			presentation.getPackage().add(p);
			
		}
				
		m.getTitle().getProgrammeMaterial().setOriginalConform(null); //clear existing segments
		m.getTitle().getProgrammeMaterial().setPresentation(presentation);
		return m;
	}

	public static Material getMaterialNoPackages(String titleID, String materialID) throws DatatypeConfigurationException {

		Supplier supplier = new Supplier();
		supplier.setSupplierID(RandomStringUtils.randomAlphabetic(10));

		Details details = new Details();
		details.setSupplier(supplier);
		details.setDateOfDelivery(DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(new GregorianCalendar()));
		details.setDeliveryVersion(new BigInteger("1"));

		FileMediaType fmt = new FileMediaType();
		fmt.setFilename("myfile.mxf");
		fmt.setFormat(FileFormatEnumType.MXF_OP_1_A_IMX_D_10_50);

		Track t = new Track();
		t.setTrackName(AudioTrackEnumType.CENTER);
		t.setTrackEncoding(AudioEncodingEnumType.PCM);
		t.setTrackNumber(1);

		AudioTracks at = new AudioTracks();
		at.getTrack().add(t);

		Segment s = new Segment();
		s.setSegmentNumber(1);
		s.setSegmentTitle("Segment title");
		s.setSOM("00:00:00:00");
		s.setEOM("00:00:00:00");

		SegmentationType st = new SegmentationType();
		st.getSegment().add(s);

		ProgrammeMaterialType pmaterial = new ProgrammeMaterialType();
		pmaterial.setMaterialID(materialID);
		pmaterial.setFormat("HD");
		pmaterial.setAspectRatio("16F16");
		pmaterial.setFirstFrameTimecode("00:00:00:00");
		pmaterial.setLastFrameTimecode("00:00:00:00");
		pmaterial.setDuration("00:00:00:00");
		pmaterial.setMedia(fmt);
		pmaterial.setAudioTracks(at);
		pmaterial.setOriginalConform(st);

		Title title = new Title();
		title.setTitleID(titleID);
		title.setProgrammeMaterial(pmaterial);

		Material material = new Material();
		material.setDetails(details);
		material.setTitle(title);
				
		return material;
	}
}
