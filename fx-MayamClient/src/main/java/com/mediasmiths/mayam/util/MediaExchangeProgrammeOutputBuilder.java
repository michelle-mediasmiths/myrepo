package com.mediasmiths.mayam.util;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.type.AudioTrack;
import com.mayam.wf.attributes.shared.type.AudioTrack.EncodingType;
import com.mayam.wf.attributes.shared.type.AudioTrackList;
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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MediaExchangeProgrammeOutputBuilder
{
	private static Logger log = Logger.getLogger(MediaExchangeProgrammeOutputBuilder.class);

	public static Programme buildProgramme(FullProgrammePackageInfo pack)
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

	private static Media getProgrammeMedia(FullProgrammePackageInfo pack)
	{
		Programme.Media programmeMedia = new Programme.Media();
		Segments mexSegments = SegmentUtil.convertMayamSegmentListToMediaExchangeSegments(pack.getSegmentList());
		AudioTracks pmat = getProgrammeMediaAudioTracks(pack);

		programmeMedia.setSegments(mexSegments);
		programmeMedia.setAudioTracks(pmat);

		programmeMedia.setId(pack.getPackageId());
		programmeMedia.setFilename(pack.getPackageId() + ".gxf");

		return programmeMedia;
	}

	private static Programme.Media.AudioTracks getProgrammeMediaAudioTracks(FullProgrammePackageInfo pack)
	{
		AudioTrackList audioTracks = pack.getMaterialAttributes().getAttribute(Attribute.AUDIO_TRACKS);
		if (audioTracks == null || audioTracks.size() == 0)
		{
			log.error("no audio tracks found when processing package " + pack.getPackageId());
			audioTracks = new AudioTrackList();
		}
		Programme.Media.AudioTracks ats = new Programme.Media.AudioTracks();

		log.warn("Tracks defined: " + audioTracks.size());

		for (AudioTrack track : audioTracks)
		{
			if (log.isDebugEnabled()) log.debug("Setting the number of channels: " + track.getNumber());

			if (track.getNumber() <= TRACK_MAX)
			{
				Programme.Media.AudioTracks.Track pmat = new Programme.Media.AudioTracks.Track();

				AudioTrackType t = mayamChannelMapping[track.getNumber()-1];

				if (t != null) // there is a valid track name mapping.
				{
					pmat.setNumber(track.getNumber());

					pmat.setType(t);

					ats.getTrack().add(pmat);

					log.debug(String.format("Track %d set to %s.", track.getNumber(), t.toString()));
				}
				else
				{
					log.warn("Ignoring track number: " + track.getNumber());
				}
			}
			else
			{
				log.warn("Ignoring track number: " + track.getNumber());
			}

		}

		return ats;
	}


	/** for simplicity create a MAP from internal Ardome names to the external exchange names. */
	private static final int TRACK_MAX = 8;

	private static  AudioTrackType[] mayamChannelMapping = new AudioTrackType[TRACK_MAX];


	/**
	 *
	 * As Per MAM-202 Ben wants a mapping from channels to track type.
	 *
	 */
	static
	{

		mayamChannelMapping[0] = AudioTrackType.LEFT;
		mayamChannelMapping[1] = AudioTrackType.RIGHT;
		mayamChannelMapping[2] = AudioTrackType.DOLBY_E;
		mayamChannelMapping[3] = AudioTrackType.DOLBY_E;
		mayamChannelMapping[4] = null;
		mayamChannelMapping[5] = null;
		mayamChannelMapping[6] = null;
		mayamChannelMapping[7] = null;



	}


	private static Programme.Detail getProgrammeDetail(FullProgrammePackageInfo pack)
	{
		Programme.Detail programmeDetail = new Programme.Detail();

		programmeDetail.setEXTCLIPUMID(pack.getPackageId());

		log.debug("Setting PackageId: " + pack.getPackageId());

		if (pack.getTitleAttributes() != null)
		{
			programmeDetail.setTitle(StringUtils.left(pack.getTitleAttributes().getAttributeAsString(Attribute.EPISODE_TITLE), 127));
			programmeDetail.setEpisodeNumber(StringUtils.left(pack.getTitleAttributes().getAttributeAsString(Attribute.EPISODE_NUMBER), 32));
			programmeDetail.setDescription(StringUtils.left(pack.getTitleAttributes().getAttributeAsString(Attribute.SERIES_TITLE), 127));

			log.debug("Title: " + programmeDetail.getTitle() + " : " + programmeDetail.getEpisodeNumber());

			StringList channels = pack.getTitleAttributes().getAttribute(Attribute.CHANNELS);

			if (channels != null && channels.size() > 0)
			{
				log.debug("Market: " + channels.get(0));
				programmeDetail.setMARKET(channels.get(0));
			}
			else
			{
				log.warn("Producing Programme type for package with empty channels list! package " +pack.getPackageId());
			}
		}
		else
		{
			log.warn("Producing Programme output with empty title details. Package : " + pack.getPackageId());
		}
		
		if (pack.getMaterialAttributes() != null)
		{
			programmeDetail.setAspectRatio(pack.getMaterialAttributes().getAttributeAsString(Attribute.ASPECT_RATIO));

			log.debug("Aspect Ratio: " + pack.getMaterialAttributes().getAttributeAsString(Attribute.ASPECT_RATIO));
			
			String supplierId = pack.getMaterialAttributes().getAttribute(Attribute.AGGREGATOR);
			programmeDetail.setSUPPLIER(supplierId);

			log.debug("Supplier id: " + supplierId);
			
			AudioTrackList audioTracks = pack.getMaterialAttributes().getAttribute(Attribute.AUDIO_TRACKS);
			if (audioTracks == null || audioTracks.size() == 0)
			{
				log.error("no audio tracks found when processing package " + pack.getPackageId());
			}
			else
			{
				EncodingType encoding = audioTracks.get(0).getEncoding();

		        log.debug("Audio encoding: " + encoding);

				if (encoding != null)
				{
					switch (encoding)
					{
						case DOLBY_E:
							programmeDetail.setAudioType(AudioListType.DIGITAL);
							break;
						case LINEAR:
							programmeDetail.setAudioType(AudioListType.STEREO);
							break;
						default:
							log.error("Encoding is not known:  " + encoding);
							break;
		
					}
					log.debug("Audio set to: " + programmeDetail.getAudioType());
				}
				else
				{
					log.error("null encoding on audio track information!");
				}
			}
		}
		else
		{
			log.warn("Producing Programme output with empty material details. Package : " + pack.getPackageId());
		}
		
		if (pack.getSegments() != null && !pack.getSegments().isEmpty())
		{
			programmeDetail.setSOM(pack.getSegmentList().getEntries().get(0).getIn().toSmpte());
			programmeDetail.setEOM(SegmentUtil.calculateEOM(pack.getSegmentList().getEntries().get(0).getDuration().toSmpte(), Timecode.getInstance(pack.getSegmentList().getEntries().get(0).getIn().toSmpte(), Framerate.HZ_25)));
			programmeDetail.setDuration(SegmentUtil.totalDuration(pack.getSegments()));

			log.debug("Segment Timings: (in,out,duration) " + programmeDetail.getSOM() + "," + programmeDetail.getEOM() + ", " + programmeDetail.getDuration());
		}
		else
		{
			log.warn("Producing Programme type for empty segment list! package" + pack.getPackageId());
		}

		programmeDetail.setResolution(getResolution(pack));

		log.debug("Resolution: " + programmeDetail.getResolution());

		Date targetDate = pack.getPackageAttributes().getAttribute(Attribute.TX_NEXT);

		log.debug(" Date set to: " + targetDate);

		if (targetDate != null)
		{

			try
			{
				GregorianCalendar c = new GregorianCalendar();
				c.setTime(targetDate);
				XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
				
				programmeDetail.setDueDate(xmlDate);
				xmlDate.setYear(xmlDate.getYear() + 3);
				programmeDetail.setPurgeDate(xmlDate);
			}
			catch (DatatypeConfigurationException e)
			{
				log.error("error setting target date on programme detail for package " + pack.getPackageId(), e);
			}
		}
		else
		{
			log.warn("Producing Programme type for package with no target date! pacakge " + pack.getPackageId());
		}

		try
		{
			programmeDetail.setClassification(ClassificationType.fromValue(((String) pack.getPackageAttributes().getAttributeAsString(Attribute.CONT_CLASSIFICATION)).toUpperCase()));
		}
		catch (Exception e)
		{
			log.error("error setting classification on programme detail for package " + pack.getPackageId(), e);
		}
		return programmeDetail;
	}

	private static ResolutionType getResolution(final FullProgrammePackageInfo pack)
	{
		String resolutionStr = pack.getPackageAttributes().getAttribute(Attribute.REQ_FMT);
		if (resolutionStr == null)
		{
			log.error("Error: resolution is null, setting to " + ResolutionType.SD);
			return ResolutionType.SD;
		}
		else
		{
			try
			{
				return ResolutionType.fromValue(resolutionStr);
			}
			catch (Exception e)
			{
				log.error("Error: resolution is unknown, " +  resolutionStr + " setting to " + ResolutionType.SD);
				return ResolutionType.SD;
			}
		}
	}
}
