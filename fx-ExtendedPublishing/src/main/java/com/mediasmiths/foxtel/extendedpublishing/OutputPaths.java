package com.mediasmiths.foxtel.extendedpublishing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.foxtel.pathresolver.PathResolver;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;

public class OutputPaths
{

	@Inject
	protected OutputPathsConfig outputPathsConfig;

	@Inject
	protected ChannelProperties channelProperties;

	@Inject
	protected PathResolver pathResolver;

	@Inject
	@Named("export.caption.filename.formatstring")
	private String captionFileNameFormatString;

	@Inject
	@Named("export.caption.filename.non.episodic.formatstring")
	private String captionFileNameNonEpisodicFormatString;

	@Inject
	@Named("export.caption.filename.titlehint.length")
	private Integer captionFileNameTitleHintLength;

	private final static Logger log = Logger.getLogger(OutputPaths.class);

	/**
	 * Returns the ftp path to the destination for the given channel and job type
	 * 
	 * @param channelTag
	 * @param jobType
	 * @return
	 */
	public String getFTPPathToExportDestinationFolder(String channelTag, TranscodeJobType jobType)
	{
		return getOutputPathForExport(channelTag, jobType, true);
	}

	/**
	 * Returns true if a file already exists at the output location for the given export parameters
	 * 
	 * @param channelTag
	 * @param jobType
	 * @param filename
	 * @return
	 */
	public boolean fileExistsAtExportDestination(String channelTag, TranscodeJobType jobType, String filename, boolean isDVD)
	{
		String pathToExportDestination = getLocalPathToExportDestination(channelTag, jobType, filename, isDVD);
		File f = new File(pathToExportDestination);
		return f.exists();
	}

	protected String getLocalPathToExportDestinationFolder(String channelTag, TranscodeJobType jobType)
	{
		return getOutputPathForExport(channelTag, jobType, false);
	}

	public String getLocalPathToExportDestination(String channelTag, TranscodeJobType jobType, String filename, boolean isDVD)
	{

		String extension = getOutputFileExtension(jobType, isDVD);
		return getLocalPathToExportDestination(channelTag, jobType, filename, extension);
	}

	public String getLocalPathToExportDestination(String channelTag, TranscodeJobType jobType, String filename, String extension)
	{

		String folder = getLocalPathToExportDestinationFolder(channelTag, jobType);
		String file = String.format("%s%s", filename, extension);
		String fullPath = String.format("%s%s%s", folder, IOUtils.DIR_SEPARATOR, file);

		return fullPath;
	}

	/**
	 * returns the output path used for the given export + channel tag if ftp is true then this will be the path used in the post ftp transfer if ftp is false this will be the local path to the final
	 * export destination
	 * 
	 * @param channelTag
	 * @param jobType
	 * @param ftp
	 * @return
	 */
	private String getOutputPathForExport(String channelTag, TranscodeJobType jobType, boolean ftp)
	{

		switch (jobType)
		{
			case CAPTION_PROXY:
				{
					if (ftp)
					{
						return outputPathsConfig.getCaptionOutputPath();
					}
					else
					{
						return outputPathsConfig.getCaptionOutputLocalPath();
					}

				}
			case COMPLIANCE_PROXY:
				{
					String exportLocation = getExportLocationForChannel(channelTag);
					String prefix;

					if (ftp)
					{
						prefix = outputPathsConfig.getComplianceOutputPrefix();
					}
					else
					{
						prefix = outputPathsConfig.getComplianceOutputLocalPath();
					}

					return String.format("%s/%s/%s", prefix, exportLocation, outputPathsConfig.getComplianceOutputFolderName());
				}
			case PUBLICITY_PROXY:
				{
					String exportLocation = getExportLocationForChannel(channelTag);

					String prefix;

					if (ftp)
					{
						prefix = outputPathsConfig.getPublicityOutputPrefix();
					}
					else
					{
						prefix = outputPathsConfig.getPublicityOutputLocalPath();
					}

					return String.format("%s/%s/%s", prefix, exportLocation, outputPathsConfig.getPublicityOutputFolderName());
				}
			case TX:
				throw new IllegalArgumentException("Unexpected job type");
			default:
				throw new IllegalArgumentException("Unexpected job type");

		}

	}

	private String getExportLocationForChannel(String channelTag)
	{

		if ("generic".equals(channelTag.toLowerCase()))
		{
			return outputPathsConfig.getGenericChannelOutputFolder();
		}

		String channelGroup = channelProperties.channelGroupForChannel(channelTag);
		String exportLocation = channelProperties.exportPathForChannelGroup(channelGroup);
		return exportLocation;
	}

	public String getOutputFileExtension(TranscodeJobType jobType, boolean isDVD)
	{
		if (isDVD)
		{
			return "";
		}
		else
		{
			switch (jobType)
			{
				case CAPTION_PROXY:
					{
						return String.format(".%s", outputPathsConfig.getCaptionOutputExtension());
					}
				case COMPLIANCE_PROXY:
					{
						return String.format(".%s", outputPathsConfig.getComplianceOutputExtension());
					}
				case PUBLICITY_PROXY:
					{
						return String.format(".%s", outputPathsConfig.getPublicityOutputExtension());
					}
				case TX:
					throw new IllegalArgumentException("Unexpected job type");
				default:
					throw new IllegalArgumentException("Unexpected job type");

			}
		}
	}


	public String getUserDeliveryLocation(String channelTag, TranscodeJobType jobType, String filename, boolean isDVD)
	{
		String nixPath = getLocalPathToExportDestination(channelTag,jobType,filename,isDVD);
		String uncPath = pathResolver.uncPath(PathResolver.PathType.NIX, nixPath);
		uncPath = uncPath.replace(" ",
		                          "%20"); //exports are limited to 0-9 a-z _, space is only character we really need to handle url encoding wise, if more are required then the path will need properly parsed out and fully urlencoded
		return uncPath;
	}

	public String getFileNameForCaptionExport(
			String presentationID,
			String programmeTitle,
			Integer seriesNumber,
			Integer episodeNumber,
			Integer exportVersion)
	{

		if (programmeTitle == null)
		{
			programmeTitle = "";
		}

		programmeTitle = StringUtils.trimToEmpty(programmeTitle).replace(" ", "");
		programmeTitle = StringUtils.left(programmeTitle, captionFileNameTitleHintLength);

		String filename;

		if (episodeNumber == null && seriesNumber == null)
		{
			filename = String.format(captionFileNameNonEpisodicFormatString, presentationID, programmeTitle, exportVersion);
		}
		else
		{
			filename = String.format(
					captionFileNameFormatString,
					presentationID,
					programmeTitle,
					seriesNumber,
					episodeNumber,
					exportVersion);
		}

		log.debug("Caption export filename: " + filename);

		return filename;
	}

}
