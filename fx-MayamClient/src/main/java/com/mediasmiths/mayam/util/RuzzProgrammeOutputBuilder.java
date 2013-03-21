package com.mediasmiths.mayam.util;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AudioTrackList;
import com.mayam.wf.attributes.shared.type.Segment;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mediasmiths.foxtel.generated.ruzz.AudioListType;
import com.mediasmiths.foxtel.generated.ruzz.BarcodeCommandType;
import com.mediasmiths.foxtel.generated.ruzz.BarcodeRequestType;
import com.mediasmiths.foxtel.generated.ruzz.ClassificationType;
import com.mediasmiths.foxtel.generated.ruzz.ColourType;
import com.mediasmiths.foxtel.generated.ruzz.Op56ProgrammeType;
import com.mediasmiths.foxtel.generated.ruzz.Op56ProgrammeType.Detail;
import com.mediasmiths.foxtel.generated.ruzz.Op56ProgrammeType.Media;
import com.mediasmiths.foxtel.generated.ruzz.Op56ProgrammeType.Media.Segments;
import com.mediasmiths.foxtel.generated.ruzz.RequestMessageType;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIF;
import com.mediasmiths.foxtel.generated.ruzz.SegmentListType;
import com.mediasmiths.mayam.FullProgrammePackageInfo;
import com.mediasmiths.mayam.controllers.MayamTitleController;
import org.apache.log4j.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class RuzzProgrammeOutputBuilder
{

	private final static Logger log = Logger.getLogger(RuzzProgrammeOutputBuilder.class);

	public static RuzzIF buildProgramme(FullProgrammePackageInfo pack, MayamTitleController titleController)
	{
		RuzzIF ret = new RuzzIF();
		ret.setRequest(buildRequest(pack, titleController));
		return ret;
	}

	private static RequestMessageType buildRequest(FullProgrammePackageInfo pack, MayamTitleController titleController)
	{
		RequestMessageType ret = new RequestMessageType();
		ret.setTransactionId(UUID.randomUUID().toString());
		ret.setBarcode(buildBarCodeRequest(pack, titleController));
		return ret;
	}

	private static BarcodeRequestType buildBarCodeRequest(FullProgrammePackageInfo pack, MayamTitleController titleController)
	{
		BarcodeRequestType ret = new BarcodeRequestType();
		ret.setType(BarcodeCommandType.CREATE);
		ret.setKey(pack.getPackageId());
		ret.setOP56Programme(buildOP56Programme(pack, titleController));
		return ret;
	}

	private static Op56ProgrammeType buildOP56Programme(FullProgrammePackageInfo pack, MayamTitleController titleController)
	{
		Op56ProgrammeType ret = new Op56ProgrammeType();
		ret.setDetail(buildDetail(pack, titleController));
		ret.setMedia(buildMedia(pack));
		return ret;
	}

	private static Media buildMedia(FullProgrammePackageInfo pack)
	{
		Media ret = new Media();

		Segments s = new Segments();
		s.setAutoId(pack.getPackageId());
		s.setFilename(pack.getPackageId() + ".gxf");

		List<Segment> segments = pack.getSegments();

		for (Segment segment : segments)
		{
			SegmentListType.Segment rzSeg = SegmentUtil.convertMayamSegmentToRuzzSegment(segment);
			s.getSegment().add(rzSeg);
		}

		ret.getSegments().add(s);

		return ret;
	}



	private static Detail buildDetail(FullProgrammePackageInfo pack, MayamTitleController titleController)
	{
		Detail ret = new Detail();

		AttributeMap titleAttributes = pack.getTitleAttributes();

		ret.setEXTCLIPUMID("");
		ret.setCATALOGCODE("");

		String title = titleAttributes.getAttributeAsString(Attribute.EPISODE_TITLE);
		if (title == null)
			ret.setTitle("");
		else
			ret.setTitle(title);

		ret.setEpisodeNumber(titleAttributes.getAttributeAsString(Attribute.EPISODE_NUMBER));

		String desc = titleAttributes.getAttributeAsString(Attribute.EPISODE_DESC);
		if (desc == null)
		{
			ret.setDescription("");
		}
		else
		{
			ret.setDescription(desc);
		}

		try
		{
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(new Date());
			XMLGregorianCalendar xmlDate;
			xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			ret.setCreationDate(xmlDate);

		}
		catch (DatatypeConfigurationException e)
		{
			log.error("error setting target date on programme detail for package " + pack.getPackageId(), e);
		}

		if (!pack.getSegments().isEmpty())
		{
			ret.setSOM(pack.getSegmentList().getEntries().get(0).getIn().toSmpte());
			ret.setDuration(SegmentUtil.totalDuration(pack.getSegments()));
		}
		else
		{
			log.warn("Producing Ruzz Programme type for empty segment list! package" + pack.getPackageId());
		}

		StringList channels = pack.getTitleAttributes().getAttribute(Attribute.CHANNELS);

		if (channels != null && channels.size() > 0)
		{
			String channel = channels.get(0);
			ret.setSUPPLIER(channel);
			ret.setCOPYRIGHT(channel);
		}
		else
		{
			log.warn("Producing Ruzz type for package with empty channels list! package " + pack.getPackageId());
		}

		ret.setMARKET("Online");

		ret.setCensorshipSystem("");

		try
		{
			ret.setClassification(ClassificationType.fromValue(((String) pack.getPackageAttributes().getAttributeAsString(
					Attribute.CONT_CLASSIFICATION)).toUpperCase()));
		}
		catch (Exception e)
		{
			log.error("error setting classification on programme detail for package " + pack.getPackageId(), e);
		}

		ret.setCaptioned(false);

		ret.setColour(ColourType.COLOUR);

		ret.setAspectRatio(getAspectRatio(pack.getPackageAttributes().getAttributeAsString(Attribute.CONT_ASPECT_RATIO)));

		AudioTrackList audioTracks = pack.getPackageAttributes().getAttribute(Attribute.AUDIO_TRACKS);

		if (audioTracks == null)
			log.error("No audio track list returned from Mayam");

		ret.setAudioType(getAudioEncoding(audioTracks));

		ret.setMARKET("Online");

		ret.setRightsStartDate(getDateFromXMLString(pack.getPackageAttributes().getAttributeAsString(Attribute.LICENSE_START)));
		ret.setRightsEndDate(getDateFromXMLString(pack.getPackageAttributes().getAttributeAsString(Attribute.LICENSE_END)));


		return ret;
	}

	private static XMLGregorianCalendar getDateFromXMLString(String xml)
	{

		try
		{
			if (xml == null)
			{
				log.error("Null XML field as date - using today" + xml);

				return DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
			}
			else
			{
				return DatatypeFactory.newInstance().newXMLGregorianCalendar(xml);
			}
		}
		catch (Throwable e)
		{
			log.error("error setting target date on programme detail for package " + xml, e);
		}
		return null;
	}

	private static AudioListType getAudioEncoding(final AudioTrackList audioTracks)
	{
		if (audioTracks == null)
		{
            return AudioListType.STEREO; // it does not matter..there are none.
		}
		else if (audioTracks.size() == 1)
		{
			return AudioListType.MONO;
		}
		else if (audioTracks.size() == 2)
		{
			return AudioListType.STEREO;
		}
		else
		{
			return AudioListType.SURROUND;
		}


	}

	private static String getAspectRatio(final String aspectRatioStr)
	{
		if (aspectRatioStr == null || aspectRatioStr.length() == 0)
		{
			log.error("Aspect ratio is set to null - defaulting to 16x9");
			return "16x9";
		}
		else if (aspectRatioStr.equalsIgnoreCase("ff"))
		{
			return "16x9";
		}
		else if (aspectRatioStr.equals("pb"))
		{
			return "16x9";

		}
		else if (aspectRatioStr.equals("rz"))
		{
			return "16x9";

		} else if (aspectRatioStr.equals("cc"))
		{
			return "16x9";

		} else if (aspectRatioStr.equals("lb"))
		{
			return "16x9";
		}
		else
		{
			log.warn("Setting aspect ration to default to 16x9");
			return "16x9";
		}

	}


}
