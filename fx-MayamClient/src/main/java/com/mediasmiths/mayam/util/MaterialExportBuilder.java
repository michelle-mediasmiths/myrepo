package com.mediasmiths.mayam.util;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.foxtel.generated.materialexport.ChannelType;
import com.mediasmiths.foxtel.generated.materialexport.MaterialExport;
import com.mediasmiths.foxtel.generated.materialexport.PresentationInformationType;
import com.mediasmiths.foxtel.generated.materialexport.PresentationInformationType.LicensedChannels;
import com.mediasmiths.foxtel.generated.materialexport.PresentationInformationType.SegmentationInformation;
import com.mediasmiths.mayam.DateUtil;
import com.mediasmiths.mayam.FullProgrammePackageInfo;
import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public class MaterialExportBuilder
{

	@Inject
	DateUtil dateUtil;
	
	@Inject
	protected ChannelProperties channelProperties;


	private static Logger log = Logger.getLogger(MaterialExportBuilder.class);

	public MaterialExport buildMaterialExport(
			FullProgrammePackageInfo pack,
			String filename)
	{

		final String location = pack.getLocation();
		final Integer episodeNumber = pack.getEpisodeNumber();
		final String episodeTitle = pack.getEpisodeTitle();
		final String productionNumber = pack.getProductionNumber();
		final String programmeTitle = pack.getProgrammeTitle();
		final Integer seriesNumber = pack.getSeriesNumber();
		final String titleId = pack.getTitleID();
		final String seriesYear = pack.getSeriesYear();

		MaterialExport ret = new MaterialExport();
		ret.setCountryOfProduction(location);

		if (episodeNumber != null)
		{
			ret.setEpisodeNumber(BigInteger.valueOf(episodeNumber.longValue()));
		}
		ret.setEpisodeTitle(episodeTitle);
		ret.setExportDate(dateUtil.fromDate(new Date()));
		ret.setMediaFile(filename);

		ret.setProductionNumber(productionNumber);
		ret.setProgrammeTitle(programmeTitle);

		if (seriesNumber != null)
		{
			ret.setSeriesNumber(BigInteger.valueOf(seriesNumber.longValue()));
		}

		ret.setTitleID(titleId);

		if (seriesYear != null)
		{
			log.debug("series year: " + seriesYear);
			try
			{
				Integer year = Integer.parseInt(seriesYear);
				ret.setYearOfProduction(BigInteger.valueOf(year.longValue()));
			}
			catch (NumberFormatException e)
			{
				log.warn("Returned value for SERIES_YEAR could not be parsed as an integer");
			}
		}

		ret.setPresentationInformation(getPresentationInformation(pack));

		return ret;
	}

	private PresentationInformationType getPresentationInformation(FullProgrammePackageInfo pack)
	{

		final String totalDuration = SegmentUtil.totalDuration(pack.getSegments());
		final Date firstTx = pack.getFirstTX();

		PresentationInformationType ret = new PresentationInformationType();
		ret.setPresentationID(pack.getPackageId());
		ret.setDuration(totalDuration);
		
		if (firstTx != null)
		{
			ret.setPresentationStartDate(dateUtil.fromDate(firstTx));
		}
		
		ret.setLicensedChannels(getLicensedChannels(pack));
		ret.setSegmentationInformation(getSegmentationInformation(pack));

		return ret;
	}

	private SegmentationInformation getSegmentationInformation(FullProgrammePackageInfo pack)
	{
		SegmentationInformation ret = SegmentUtil.convertMayamSegmentListToMaterialExportSegments(pack);
		return ret;
	}

	private LicensedChannels getLicensedChannels(FullProgrammePackageInfo pack)
	{
		LicensedChannels ret = new LicensedChannels();
		List<String> channels = pack.getChannels();
		
		for (String tag : channels)
		{
			ChannelType ct = new ChannelType();
			ct.setChannelTag(tag);
			ct.setChannelName(channelProperties.nameForTag(tag));
			ret.getChannel().add(ct);
		}
		
		return ret;
	}
}
