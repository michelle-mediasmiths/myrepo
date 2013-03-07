package com.mediasmiths.mayam.util;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.type.AudioTrack;
import com.mayam.wf.attributes.shared.type.AudioTrackList;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.attributes.shared.type.AudioTrack.EncodingType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details.Supplier;
import com.mediasmiths.foxtel.generated.mediaexchange.AudioTrackType;
import com.mediasmiths.foxtel.generated.mediaexchange.ClassificationType;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme.Media;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme.Media.AudioTracks;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme.Media.Segments;
import com.mediasmiths.foxtel.generated.mediaexchange.ResolutionType;
import com.mediasmiths.foxtel.generated.mediaexchange.AudioListType;
import com.mediasmiths.mayam.FullProgrammePackageInfo;
import com.mediasmiths.std.types.Framerate;
import com.mediasmiths.std.types.Timecode;

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

		for (AudioTrack track : audioTracks)
		{

			Programme.Media.AudioTracks.Track pmat = new Programme.Media.AudioTracks.Track();
			pmat.setNumber(track.getNumber());
			
			if(track.getEncoding() != null){
			AudioTrackType att = mapMayamAudioTrackEncodingtoMediaExhchangeEncoding(track.getEncoding());
			pmat.setType(att);
			}
			else{
				log.warn("null encoding on audio track!");
			}

			ats.getTrack().add(pmat);

		}

		return ats;
	}

	private static AudioTrackType mapMayamAudioTrackEncodingtoMediaExhchangeEncoding(EncodingType encoding)
	{
		switch (encoding)
		{
			case DOLBY_E:
				return AudioTrackType.DOLBY_E;
			case LINEAR:
				return AudioTrackType.STEREO; // no idea if this is right
		}

		log.error("unknown encoding type");

		return AudioTrackType.STEREO;
	}

	private static Programme.Detail getProgrammeDetail(FullProgrammePackageInfo pack)
	{
		Programme.Detail programmeDetail = new Programme.Detail();

		programmeDetail.setEXTCLIPUMID(pack.getPackageId());
		programmeDetail.setTitle(StringUtils.left(pack.getTitleAttributes().getAttributeAsString(Attribute.EPISODE_TITLE), 127));
		programmeDetail.setEpisodeNumber(StringUtils.left(pack.getTitleAttributes().getAttributeAsString(Attribute.EPISODE_NUMBER), 32));
		programmeDetail.setDescription(StringUtils.left(pack.getTitleAttributes().getAttributeAsString(Attribute.SERIES_TITLE), 127));
		
		programmeDetail.setResolution(ResolutionType.fromValue(pack.getPackageAttributes().getAttribute(Attribute.REQ_FMT).toString()));
		programmeDetail.setAspectRatio(pack.getMaterialAttributes().getAttribute(Attribute.ASPECT_RATIO).toString());
		
		String supplierId = pack.getMaterialAttributes().getAttribute(Attribute.AGGREGATOR);
		Supplier supplier = new Supplier();
		supplier.setSupplierID(supplierId);
		programmeDetail.setSUPPLIER(supplier);
		
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
						programmeDetail.setAudioType(AudioListType.DIGITAL);
						break;
					case LINEAR:
						programmeDetail.setAudioType(AudioListType.STEREO);
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
		
		if (pack.getSegments() != null && !pack.getSegments().isEmpty())
		{
			programmeDetail.setSOM(pack.getSegmentList().getEntries().get(0).getIn().toSmpte());
			programmeDetail.setEOM(SegmentUtil.calculateEOM(pack.getSegmentList().getEntries().get(0).getDuration().toSmpte(), Timecode.getInstance(pack.getSegmentList().getEntries().get(0).getIn().toSmpte(), Framerate.HZ_25)));
			programmeDetail.setDuration(SegmentUtil.totalDuration(pack.getSegments()));
		}
		else
		{
			log.warn("Producing Programme type for empty segment list! package" + pack.getPackageId());
		}

		programmeDetail.setSUPPLIER(pack.getMaterialAttributes().getAttributeAsString(Attribute.DIST_NAME));
		StringList channels = pack.getTitleAttributes().getAttribute(Attribute.CHANNELS);

		if (channels != null && channels.size() > 0)
		{
			programmeDetail.setMARKET(channels.get(0));
		}
		else
		{
			log.warn("Producing Programme type for package with empty channels list! package " +pack.getPackageId());
		}

		Date targetDate = pack.getPackageAttributes().getAttribute(Attribute.TX_NEXT);

		if (targetDate != null)
		{
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(targetDate);
			XMLGregorianCalendar xmlDate;
			try
			{
				xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
				programmeDetail.setDueDate(xmlDate);
				xmlDate.setYear(xmlDate.getYear() + 3);
				programmeDetail.setPurgeDate(xmlDate);
			}
			catch (DatatypeConfigurationException e)
			{
				log.error("error settign target date on programme detail for package " + pack.getPackageId(), e);
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
}
