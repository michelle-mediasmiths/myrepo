package com.mediasmiths.foxtel.mpa;

import java.math.BigInteger;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Ignore;

import com.mediasmiths.foxtel.generated.MaterialExchange.AudioEncodingEnumType;
import com.mediasmiths.foxtel.generated.MaterialExchange.AudioTrackEnumType;
import com.mediasmiths.foxtel.generated.MaterialExchange.FileFormatEnumType;
import com.mediasmiths.foxtel.generated.MaterialExchange.FileMediaType;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details.Supplier;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType.AudioTracks.Track;

@Ignore  //this is not a junit test class but because it has Test in the name maven will run it as such
public class MarketingMaterialTest {

	public static Material getMaterial(String titleID)
			throws DatatypeConfigurationException {

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

		MarketingMaterialType mmaterial = new MarketingMaterialType();
		mmaterial.setFormat("HD");
		mmaterial.setAspectRatio("16F16");
		mmaterial.setFirstFrameTimecode("00:00:00:00");
		mmaterial.setLastFrameTimecode("00:00:00:00");
		mmaterial.setDuration("00:00:00:00");
		mmaterial.setMedia(fmt);
		mmaterial.setAudioTracks(at);

		Title title = new Title();
		title.setTitleID(titleID);
		title.setMarketingMaterial(mmaterial);

		Material material = new Material();
		material.setDetails(details);
		material.setTitle(title);

		return material;

	}
}
