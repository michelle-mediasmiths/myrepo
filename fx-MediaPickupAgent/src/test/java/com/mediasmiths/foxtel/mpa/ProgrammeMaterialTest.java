package com.mediasmiths.foxtel.mpa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import com.mediasmiths.foxtel.generated.MaterialExchange.AudioEncodingEnumType;
import com.mediasmiths.foxtel.generated.MaterialExchange.AudioTrackEnumType;
import com.mediasmiths.foxtel.generated.MaterialExchange.FileFormatEnumType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details.Supplier;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.FileMediaType;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks.Track;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType;
import com.mediasmiths.foxtel.generated.MaterialExchange.SegmentationType.Segment;

public class ProgrammeMaterialTest {

	@Test
	public void testProgrammeMaterialWithMxf() throws Exception{
		
		String incomingPath = Util.prepareTempFolder("INCOMING");
		String ardomeImportPath = Util.prepareTempFolder("ARDOMEIMPORT");
		
		String messageName = RandomStringUtils.randomAlphabetic(10);
				
		Material material = getMaterial();
		//write material message
		Util.writeMaterialToFile(material, incomingPath+IOUtils.DIR_SEPARATOR+messageName+FilenameUtils.EXTENSION_SEPARATOR+".xml");
		//write 'media' file
		IOUtils.write(new byte[1000], new FileOutputStream(new File(incomingPath+IOUtils.DIR_SEPARATOR+messageName+FilenameUtils.EXTENSION_SEPARATOR+".mxf")));
		
		
		
		//expect media file in ardome incoming
		
		//expect material message in archive folder
	}

	private Material getMaterial() throws DatatypeConfigurationException {
		
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
		pmaterial.setMaterialID(RandomStringUtils.randomAlphabetic(10));
		pmaterial.setFormat("HD");
		pmaterial.setAspectRatio("16F16");
		pmaterial.setFirstFrameTimecode("00:00:00:00");
		pmaterial.setLastFrameTimecode("00:00:00:00");
		pmaterial.setDuration("00:00:00:00");
		pmaterial.setMedia(fmt);
		pmaterial.setAudioTracks(at);
		pmaterial.setOriginalConform(st);
		
		Title title = new Title();
		title.setTitleID(RandomStringUtils.randomAlphabetic(10));
		title.setProgrammeMaterial(pmaterial);
		
		Material material = new Material();
		material.setDetails(details);
		material.setTitle(title);
		
		return material;
	}
}
