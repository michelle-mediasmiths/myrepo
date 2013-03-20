package com.mediasmiths.mayam.util;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AudioTrack.EncodingType;
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
			log.error("error settign target date on programme detail for package " + pack.getPackageId(), e);
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
		ret.setAspectRatio("16x9");
		ret.setColour(ColourType.COLOUR);
		ret.setAudioType(AudioListType.STEREO);
		ret.setMARKET("Online");
		ret.setRightsStartDate(getDateFromXMLString(titleController.getRightsStart(titleAttributes)));
		ret.setRightsEndDate(getDateFromXMLString(titleController.getRightsEnd(titleAttributes)));

		AudioTrackList audioTracks = pack.getMaterialAttributes().getAttribute(Attribute.AUDIO_TRACKS);
		if (audioTracks == null || audioTracks.size() == 0)
		{
			log.error("no audio tracks found when processing package " + pack.getPackageId());
		}
		else
		{

			EncodingType encoding = audioTracks.get(0).getEncoding();

			if (encoding != null)
			{
				switch (encoding)
				{
					case DOLBY_E:
						ret.setAudioType(AudioListType.DIGITAL);
						break;
					case LINEAR:
						ret.setAudioType(AudioListType.STEREO);
						break;
					default:
						break;

				}
			}
			else
			{
				log.error("null encoding on audio track information!");
			}

		}
		return ret;
	}

	private static XMLGregorianCalendar getDateFromXMLString(String xml)
	{
		try
		{
			if (xml == null)
				throw new Exception("Null XML field as date");

			return DatatypeFactory.newInstance().newXMLGregorianCalendar(xml);
		}
		catch (Throwable e)
		{
			log.error("error setting target date on programme detail for package " + xml, e);
		}
		return null;
	}


}
