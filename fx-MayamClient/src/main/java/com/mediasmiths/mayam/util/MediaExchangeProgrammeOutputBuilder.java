package com.mediasmiths.mayam.util;

import com.google.inject.Inject;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.type.AudioTrack;
import com.mayam.wf.attributes.shared.type.AudioTrackList;
import com.mayam.wf.attributes.shared.type.Segment;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mediasmiths.foxtel.generated.mediaexchange.AudioListType;
import com.mediasmiths.foxtel.generated.mediaexchange.AudioTrackType;
import com.mediasmiths.foxtel.generated.mediaexchange.ClassificationType;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme.Media;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme.Media.AudioTracks;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme.Media.Segments;
import com.mediasmiths.foxtel.generated.mediaexchange.ResolutionType;
import com.mediasmiths.mayam.FullProgrammePackageInfo;
import com.mediasmiths.std.types.Framerate;
import com.mediasmiths.std.types.Timecode;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

public class MediaExchangeProgrammeOutputBuilder
{
	private static Logger log = Logger.getLogger(MediaExchangeProgrammeOutputBuilder.class);

	@Inject
	ChannelPropertiesConfiguration channelProperties;

	public Programme buildProgramme(FullProgrammePackageInfo pack)
	{
		Programme ret = new Programme();

		// programme detail
		Programme.Detail programmeDetail = getProgrammeDetail(pack);

		ret.setDetail(programmeDetail);

		// programme media
		Programme.Media programmeMedia = getProgrammeMedia(pack);

		ret.setMedia(programmeMedia);

		return ret;
	}

	private Media getProgrammeMedia(FullProgrammePackageInfo pack)
	{
		Programme.Media programmeMedia = new Programme.Media();
		Segments mexSegments = SegmentUtil.convertMayamSegmentListToMediaExchangeSegments(pack);
		AudioTracks pmat = getProgrammeMediaAudioTracks(pack);

		programmeMedia.setSegments(mexSegments);
		programmeMedia.setAudioTracks(pmat);

		programmeMedia.setId(pack.getPackageId());
		programmeMedia.setFilename(pack.getPackageId() + ".gxf");

		return programmeMedia;
	}

	private  Programme.Media.AudioTracks getProgrammeMediaAudioTracks(FullProgrammePackageInfo pack)
	{
		AudioTrackList audioTracks = pack.getMaterialAttributes().getAttribute(Attribute.AUDIO_TRACKS);
		
		if (audioTracks == null || audioTracks.size() == 0)
		{
			log.error("No audio tracks found when processing package " + pack.getPackageId());
			return new Programme.Media.AudioTracks();
		}
		else
		{
			AudioListType audioListType = getAudioEncoding(pack);
			
			Programme.Media.AudioTracks ats = new Programme.Media.AudioTracks();

			log.warn("Tracks defined: " + audioTracks.size());
			
			if (audioListType == AudioListType.MONO)
			{
				Programme.Media.AudioTracks.Track pmat = mappingAudioTrack(audioTracks, 0);
				
				ats.getTrack().add(pmat);
				
			}
			else if (audioListType == AudioListType.STEREO)
			{
				Programme.Media.AudioTracks.Track pmat = mappingAudioTrack(audioTracks, 0);
				ats.getTrack().add(pmat);
				pmat = mappingAudioTrack(audioTracks, 1);
				ats.getTrack().add(pmat);
				
			}
			else if (audioListType == AudioListType.SURROUND)
			{
				for (AudioTrack track : audioTracks)
				{
					if (track.getNumber() <= channelProperties.audioTrackLayout.length)
					{
						Programme.Media.AudioTracks.Track pmat = mappingAudioTrack(audioTracks, track.getNumber() - 1);
						if (pmat != null)
						{
							ats.getTrack().add(pmat);
						}
					}
					else
					{
						log.warn("Ignoring track number: " + track.getNumber());
					}
				}
				
			}
			return ats;
		}

	}


	
	private  Programme.Media.AudioTracks.Track mappingAudioTrack(AudioTrackList audioTracks, int i)
	{
		AudioTrack track = audioTracks.get(i);
		if (log.isDebugEnabled())
			log.debug("Setting the number of channels: " + track.getNumber());
		
		Programme.Media.AudioTracks.Track pmat = new Programme.Media.AudioTracks.Track();
		if (channelProperties.audioTrackLayout[i] != null)
		{
			AudioTrackType t = channelProperties.audioTrackLayout[i];
			pmat.setNumber(track.getNumber());
			pmat.setType(t);
			log.debug(String.format("Track %d set to %s.", track.getNumber(), t.toString()));
			return pmat;
		}
		else
		{
			log.warn("Ignoring track number: " + track.getNumber());
			return null;
		}
	}


	private  Programme.Detail getProgrammeDetail(FullProgrammePackageInfo pack)
	{
		try
		{
			Programme.Detail programmeDetail = new Programme.Detail();

			programmeDetail.setEXTCLIPUMID(pack.getPackageId());

			setTitle(programmeDetail, pack);

			setEpisodeNumber(programmeDetail, pack);

			setDescription(programmeDetail, pack);

			setCreationDate(programmeDetail, pack);

			setMarket(programmeDetail, pack);

		    setAspectRatio(programmeDetail, pack);

		    setSupplier(programmeDetail, pack);

			setAudioType(programmeDetail, pack);

		    setSOMAndEOM(programmeDetail, pack);

			setResolution(programmeDetail, pack);

			setDueAndPurgeDate(programmeDetail, pack);

			programmeDetail.setClassification(getClassification(pack));


			return programmeDetail;
		}
		catch (Exception e)
		{
			log.error("Problem marshall mayam data to XML.", e);
			throw e;
		}
	}


	private  ClassificationType getClassification(final FullProgrammePackageInfo pack)
	{
		try
		{
			return ClassificationType.fromValue(((String) pack.getPackageAttributes()
			                                                  .getAttributeAsString(Attribute.CONT_CLASSIFICATION)).toUpperCase());
		}
		catch (Exception e)
		{
			log.error("Cannot set classification: " + pack.getPackageAttributes().getAttributeAsString(Attribute.CONT_CLASSIFICATION), e);
			return ClassificationType.NC;
		}
	}


	private  void setDueAndPurgeDate(final Programme.Detail programmeDetail, final FullProgrammePackageInfo pack)
	{
         java.util.Date targetDate = pack.getPackageAttributes().getAttribute(Attribute.TX_FIRST);
		 DateTime txDate;
		 if (targetDate == null)
		 {
			 txDate = new DateTime(); //MAM-506 if tx first date missing then use the current date
		 }
	     else
		 {
			 try
			 {
				 txDate = new DateTime(targetDate);
			 }
			 catch (Exception e)
			 {
				 log.error("Unable to parse date " + targetDate);
				 txDate = new DateTime();
			 }
		 }
		programmeDetail.setDueDate(getGregorian(txDate));
		programmeDetail.setPurgeDate(getPurgeDateFor(txDate));
	}

	private  void setCreationDate(Programme.Detail programmeDetail, final FullProgrammePackageInfo pack)
	{
		programmeDetail.setCreationDate(getNowDate());
	}

	private  void setSOMAndEOM(Programme.Detail programmeDetail, final FullProgrammePackageInfo pack)
	{
		if (pack.getSegments() != null && !pack.getSegments().isEmpty())
		{
			programmeDetail.setSOM(pack.getSegmentList().getEntries().get(0).getIn().toSmpte());
			Segment lastSegment = pack.getSegmentList().getEntries().get(pack.getSegmentList().getEntries().size() - 1);
			programmeDetail.setEOM(SegmentUtil.calculateEOM(lastSegment.getDuration().toSmpte(),
			                                                Timecode.getInstance(lastSegment.getIn().toSmpte(), Framerate.HZ_25)));

			programmeDetail.setDuration(SegmentUtil.totalDuration(pack.getSegments()));

			log.debug("Segment Timings: (in,out,duration) " + programmeDetail.getSOM() + "," +
			           programmeDetail.getEOM() + ", " + programmeDetail.getDuration());
		}
		else
		{
			programmeDetail.setSOM("0:0:0:0");
			programmeDetail.setEOM("0:0:0:0");
			programmeDetail.setDuration("0:0:0:0");
			log.warn("Producing Programme type for empty segment list! package" + pack.getPackageId());
		}
	}


	private  AudioListType getAudioEncoding(final FullProgrammePackageInfo pack)
	{
		AudioListType audioListType = null;
		AudioTrackList audioTracks = pack.getMaterialAttributes().getAttribute(Attribute.AUDIO_TRACKS);
		if (audioTracks == null || audioTracks.size() == 0)
		{
			log.error("no audio tracks found when processing package " + pack.getPackageId());
		}
		else
		{
			audioListType = getAudioEncoding(audioTracks);

	        log.debug("Audio encoding: " + audioListType.toString());
		}
		return audioListType;
	}

	private  void setSupplier(Programme.Detail programmeDetail, final FullProgrammePackageInfo pack)
	{
		String supplier = pack.getMaterialAttributes().getAttributeAsString(Attribute.AGGREGATOR);
		if (supplier == null)
			programmeDetail.setSUPPLIER("No Supplier set in metadata.");
		else
			programmeDetail.setSUPPLIER(supplier);

	}

	private  void setMarket(Programme.Detail programmeDetail, final FullProgrammePackageInfo pack)
	{
		StringList channels = pack.getTitleAttributes().getAttribute(Attribute.CHANNELS);

		if (channels != null && channels.size() > 0)
		{
            String channel = channels.get(0);
			if (channel == null)
				programmeDetail.setMARKET("No Market is set in metadata");
			else
				programmeDetail.setMARKET(channel);
		}
		else
		{
			programmeDetail.setMARKET("No Market is set in metadata");

		}
	}

	private  void setAudioType(Programme.Detail programmeDetail, FullProgrammePackageInfo pack)
    {

	    AudioTrackList audioTracks = pack.getMaterialAttributes().getAttribute(Attribute.AUDIO_TRACKS);
	    if (audioTracks == null || audioTracks.size() == 0)
	    {
		    log.error("No audio tracks found when processing package " + pack.getPackageId());
	    }
	    else
	    {
		    programmeDetail.setAudioType(getAudioEncoding(audioTracks));
	    }
    }

	private  void setDescription(Programme.Detail programmeDetail, final FullProgrammePackageInfo pack)
	{
		String desc = pack.getTitleAttributes().getAttributeAsString(Attribute.CONT_DESC);
		if (desc == null)
			programmeDetail.setDescription("No description supplied.");
		else
			programmeDetail.setDescription(StringUtils.left(desc, 127));
	}

	private  void setEpisodeNumber(Programme.Detail programmeDetail, final FullProgrammePackageInfo pack)
	{
		String episode = pack.getTitleAttributes().getAttributeAsString(Attribute.EPISODE_NUMBER);
		if (episode == null)
			programmeDetail.setEpisodeNumber("No Episode set in metadata");
		else
			programmeDetail.setEpisodeNumber(StringUtils.left(episode, 32));
	}

	private  void setTitle(Programme.Detail programmeDetail, final FullProgrammePackageInfo pack)
	{
		String title = pack.getTitleAttributes().getAttributeAsString(Attribute.SERIES_TITLE);
		if (title == null)
		{
			//
 		}
		else
		{
			programmeDetail.setTitle(StringUtils.left(title, 127));
		}
	}

	private  void setAspectRatio(Programme.Detail programmeDetail, FullProgrammePackageInfo pack)
	{
		String aspectRatioStr = pack.getMaterialAttributes().getAttributeAsString(Attribute.CONT_ASPECT_RATIO);
		String setAR;

		if (aspectRatioStr == null || aspectRatioStr.length() == 0)
		{
			log.error("Aspect ratio is set to null - defaulting to 16x9");
			setAR =  "16x9";
		}
	    else if (aspectRatioStr.equalsIgnoreCase("ff"))
		{
			setAR =  "16x9";
		}
		else if (aspectRatioStr.equals("pb"))
		{
			setAR =  "16x9";

		}
		else if (aspectRatioStr.equals("rz"))
		{
			setAR =  "16x9";

		} else if (aspectRatioStr.equals("cc"))
		{
			setAR =  "16x9";

		} else if (aspectRatioStr.equals("lb"))
		{
			setAR =  "16x9";
		}
		else
		{
			log.warn("Setting aspect ration to default to 16x9");
			setAR =  "16x9";
		}

		programmeDetail.setAspectRatio(setAR);

    }

	private  void setResolution(Programme.Detail programmeDetail, final FullProgrammePackageInfo pack)
	{
		String resolutionStr = pack.getPackageAttributes().getAttribute(Attribute.REQ_FMT);
		if (resolutionStr == null)
		{
			log.error("Error: resolution is null, setting to " + ResolutionType.SD);
			programmeDetail.setResolution(ResolutionType.SD);
		}
		else
		{
			try
			{
				programmeDetail.setResolution(ResolutionType.fromValue(resolutionStr));
			}
			catch (Exception e)
			{
				log.error("Error: resolution is unknown, " +  resolutionStr + " setting to " + ResolutionType.SD);
				programmeDetail.setResolution(ResolutionType.SD);
			}
		}
	}


	private  AudioListType getAudioEncoding(final AudioTrackList audioTracks)
	{
		if (audioTracks.size() == 1)
		{
			return AudioListType.MONO;
		}
		else if (audioTracks.size() >= 8)
		{
			return AudioListType.SURROUND;
		}
		else
		{
			return AudioListType.STEREO;
		}

	}


	private  XMLGregorianCalendar getGregorian(org.joda.time.DateTime dateTime)
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

	private  XMLGregorianCalendar getNowDate()
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

	private  XMLGregorianCalendar getPurgeDateFor(org.joda.time.DateTime dateTime)
	{

		org.joda.time.DateTime pTime = new org.joda.time.DateTime(dateTime);

		return getGregorian(pTime.plusYears(3));

	}


	private static final DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");

}
