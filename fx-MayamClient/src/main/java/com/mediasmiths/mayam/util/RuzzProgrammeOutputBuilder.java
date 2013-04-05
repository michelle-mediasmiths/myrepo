package com.mediasmiths.mayam.util;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AudioTrackList;
import com.mayam.wf.attributes.shared.type.Segment;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mediasmiths.foxtel.generated.outputruzz.AudioListType;
import com.mediasmiths.foxtel.generated.outputruzz.BarcodeCommandType;
import com.mediasmiths.foxtel.generated.outputruzz.BarcodeRequestType;
import com.mediasmiths.foxtel.generated.outputruzz.ClassificationType;
import com.mediasmiths.foxtel.generated.outputruzz.ColourType;
import com.mediasmiths.foxtel.generated.outputruzz.Op56ProgrammeType;
import com.mediasmiths.foxtel.generated.outputruzz.Op56ProgrammeType.Detail;
import com.mediasmiths.foxtel.generated.outputruzz.Op56ProgrammeType.Media;
import com.mediasmiths.foxtel.generated.outputruzz.Op56ProgrammeType.Media.Segments;
import com.mediasmiths.foxtel.generated.outputruzz.RequestMessageType;
import com.mediasmiths.foxtel.generated.outputruzz.RuzzIF;
import com.mediasmiths.foxtel.generated.outputruzz.SegmentListType;
import com.mediasmiths.mayam.FullProgrammePackageInfo;
import com.mediasmiths.mayam.controllers.MayamTitleController;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class RuzzProgrammeOutputBuilder
{

	private final static Logger log = Logger.getLogger(RuzzProgrammeOutputBuilder.class);
	
	@Inject
	@Named("ao.tx.delivery.ftp.xml.supplier")
	private static String supplierInXml = "FX-MAM";

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

		setTitle(ret, pack);

		setEpisodeNumber(ret, pack);

		setDescription(ret, pack);

        setCreationDate(ret, pack);

		setSOMAndDuration(pack, ret);
		
		ret.setSUPPLIER(supplierInXml);

		StringList channels = pack.getTitleAttributes().getAttribute(Attribute.CHANNELS);

		if (channels != null && channels.size() > 0)
		{
			String channel = channels.get(0);
			
			ret.setCOPYRIGHT(channel);
			
			ret.setMARKET(channel);
		}
		else
		{
			log.warn("Producing Ruzz type for package with empty channels list! package " + pack.getPackageId());
			ret.setCOPYRIGHT("No Channel owner set in metadata");
		}

		

		ret.setCensorshipSystem("");

		setClassification(pack, ret);

		ret.setCaptioned(false);

		ret.setColour(ColourType.COLOUR);

		ret.setAspectRatio(getAspectRatio(pack.getPackageAttributes().getAttributeAsString(Attribute.CONT_ASPECT_RATIO)));

		AudioTrackList audioTracks = pack.getPackageAttributes().getAttribute(Attribute.AUDIO_TRACKS);

		if (audioTracks == null)
			log.error("No audio track list returned from Mayam");

		ret.setAudioType(getAudioEncoding(audioTracks));

		ret.setRightsStartDate(getDateFromMayamString((java.util.Date)pack.getTitleAttributes().getAttribute(Attribute.LICENSE_START)));
		ret.setRightsEndDate(getDateFromMayamString((java.util.Date)pack.getTitleAttributes().getAttribute(Attribute.LICENSE_END)));


		return ret;
	}


	private static void setClassification(final FullProgrammePackageInfo pack, final Detail ret)
	{
		try
		{
			ret.setClassification(ClassificationType.fromValue(((String) pack.getPackageAttributes()
			                                                                 .getAttributeAsString(Attribute.CONT_CLASSIFICATION))
					                                                   .toUpperCase()));
		}
		catch (Exception e)
		{
			log.error("error setting classification on programme detail for package " + pack.getPackageId(), e);
			ret.setClassification(ClassificationType.NC);
		}
	}

	private static void setSOMAndDuration(final FullProgrammePackageInfo pack, final Detail ret)
	{
		if (!pack.getSegments().isEmpty())
		{
			ret.setSOM(pack.getSegmentList().getEntries().get(0).getIn().toSmpte());
			ret.setDuration(SegmentUtil.totalDuration(pack.getSegments()));
		}
		else
		{
			ret.setSOM("0:0:0:0");
			ret.setDuration("0:0:0:0");
		}
	}

	private static void setCreationDate(Detail programmeDetail, final FullProgrammePackageInfo pack)
	{
		programmeDetail.setCreationDate(getNowDate());
	}


	private static void setDescription(Detail programmeDetail, final FullProgrammePackageInfo pack)
	{
		String desc = pack.getTitleAttributes().getAttributeAsString(Attribute.CONT_DESC);
		if (desc == null)
			programmeDetail.setDescription("No description supplied.");
		else
			programmeDetail.setDescription(StringUtils.left(desc, 127));
	}

	private static void setEpisodeNumber(Detail programmeDetail, final FullProgrammePackageInfo pack)
	{
		String episode = pack.getTitleAttributes().getAttributeAsString(Attribute.EPISODE_NUMBER);
		if (episode == null)
			programmeDetail.setEpisodeNumber("No Episode set in metadata");
		else
			programmeDetail.setEpisodeNumber(StringUtils.left(episode, 32));
	}

	private static void setTitle(Detail programmeDetail, final FullProgrammePackageInfo pack)
	{
		String title = pack.getTitleAttributes().getAttributeAsString(Attribute.SERIES_TITLE);
		if (title == null)
		{
			programmeDetail.setTitle("No Title set in Metadata.");
		}
		else
		{
			programmeDetail.setTitle(StringUtils.left(title, 127));
		}
	}

	private static XMLGregorianCalendar getDateFromMayamString(java.util.Date mayamValue)
	{

		try
		{
            if (mayamValue == null)
	            return getGregorian(lastCentury);

			return getGregorian(mayamValue);

		}
		catch (Throwable e)
		{
			log.error("error setting target date on programme detail for package " + mayamValue, e);
			return getGregorian(lastCentury);
		}
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


	private static XMLGregorianCalendar getGregorian(java.util.Date dateTime)
	{

		try
		{
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(dateTime);
			XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			cal.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
			cal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			return cal;
		}
		catch (DatatypeConfigurationException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private static XMLGregorianCalendar getNowDate()
	{
		try
		{
			XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
			cal.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
			cal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			return cal;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private static XMLGregorianCalendar getGregorian(org.joda.time.DateTime dateTime)
	{

		try
		{
			XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
			cal.setYear(dateTime.getYear());
			cal.setDay(dateTime.getDayOfMonth());
			cal.setMonth(dateTime.getMonthOfYear());
			cal.setHour(dateTime.getHourOfDay());
			cal.setMinute(dateTime.getMinuteOfHour());
			cal.setSecond(dateTime.getSecondOfMinute());
			cal.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
			cal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			return cal;
		}
		catch (DatatypeConfigurationException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private static XMLGregorianCalendar getPurgeDateFor(java.util.Date dateTime)
	{

		org.joda.time.DateTime pTime = new org.joda.time.DateTime(dateTime);

		return getGregorian(pTime.plusMonths(3));

	}


	private static final DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");

	private static final org.joda.time.DateTime lastCentury = DEFAULT_DATE_FORMAT.parseDateTime("1900-01-01 00:00:00");



}
