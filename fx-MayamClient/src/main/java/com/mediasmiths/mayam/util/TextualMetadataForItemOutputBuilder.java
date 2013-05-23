package com.mediasmiths.mayam.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.type.Segment;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.SegmentListList;
import com.mayam.wf.attributes.shared.type.Timecode;
import com.mediasmiths.mayam.FullMaterialInfo;
import com.mediasmiths.std.types.Framerate;

public class TextualMetadataForItemOutputBuilder
{

	public static String buildInfoString(final FullMaterialInfo info)
	{
		final String titleID = info.getTitleID();
		final String materialID = info.getMaterialID();
		final String programmeTitle = info.getProgrammeTitle();
		final Integer seriesNumber = info.getSeriesNumber();
		final String episodeTitle = info.getEpisodetitle();
		final Integer episodeNumber = info.getEpisodeNumber();
		final Date txDate = info.getFirstTXDate();
		final List<String> channels = info.getChannels();
		final String packagesSection = getPackagesSection(info);
		final String previewComment = info.getPreviewComment();
		final String additionalInfo = info.getAdditionalInfo();
		final String markers = info.getMarkers();

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Title ID : %s\n", titleID));
		sb.append(String.format("Material ID : %s\n", materialID));
		sb.append(String.format("Programme Title : %s\n", programmeTitle));

		if (seriesNumber != null)
		{
			sb.append(String.format("Series Number : %d\n", seriesNumber));
		}

		if (episodeTitle != null)
		{
			sb.append(String.format("Episode Title : %s\n", episodeTitle));
		}

		if (episodeNumber != null)
		{
			sb.append(String.format("Episode Number : %d\n", episodeNumber));
		}

		if (txDate != null)
		{
			sb.append(String.format("TX Date : %s\n", txDate));
		}

		sb.append(String.format("Channels : %s\n", StringUtils.join(channels, ',')));
		sb.append(packagesSection);

		if (previewComment != null)
		{
			sb.append(String.format("Preview Comment : %s\n", previewComment));
		}

		if (additionalInfo != null)
		{
			sb.append(String.format("Additional Info : %s\n", previewComment));
		}

		if (!StringUtils.isEmpty(markers))
		{

			sb.append("Markers: \n");
			sb.append(markers);
		}

		return sb.toString();
	}

	protected static String getPackagesSection(final FullMaterialInfo info)
	{
		StringBuilder sb = new StringBuilder();

		SegmentListList packages = info.getPackages();

		if (packages != null && packages.size() > 0)
		{
			sb.append("Packages: \n");

			for (SegmentList segmentList : packages)
			{
				String presentationID = segmentList.getAttributeMap().getAttribute(Attribute.HOUSE_ID);

				if (segmentList.getEntries().size() > 0)
				{

					// sort by segment number
					Collections.sort(segmentList.getEntries(), new Comparator<Segment>()
					{
						@Override
						public int compare(Segment o1, Segment o2)
						{
							return Integer.compare(o1.getNumber(), o2.getNumber());
						}
					});

					Timecode som = segmentList.getEntries().get(0).getIn();
					String somString = som.toSmpte();
					String eomString = SegmentUtil.calculateEOM(
							segmentList.getEntries().get(segmentList.getEntries().size()-1).getDuration().toSmpte(),
							com.mediasmiths.std.types.Timecode.getInstance(somString,Framerate.HZ_25));

					sb.append(String.format(
							"\tPresentation ID : %s Segment Count : %d : SOM %s : EOM %s\n",
							presentationID,
							segmentList.getEntries().size(),
							somString,
							eomString));
				}
				else
				{
					sb.append(String.format("\tPresentation ID : %s Segment Count : %d\n", presentationID, Integer.valueOf(0)));
				}
			}
		}
		return sb.toString();
	}
}
