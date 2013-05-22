package com.mediasmiths.foxtel.extendedpublishing;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class OutputPathsConfig
{
	@Inject
	@Named("export.caption.path.prefix")
	private String captionOutputPath;

	@Inject
	@Named("export.caption.path.local")
	private String captionOutputLocalPath;

	@Inject
	@Named("export.caption.extention")
	private String captionOutputExtension;

	@Inject
	@Named("export.compliance.foldername")
	private String complianceOutputFolderName;

	@Inject
	@Named("export.compliance.path.prefix")
	private String complianceOutputPrefix;

	@Inject
	@Named("export.compliance.path.local")
	private String complianceOutputLocalPath;

	@Inject
	@Named("export.compliance.extention")
	private String complianceOutputExtension;

	@Inject
	@Named("export.publicity.foldername")
	private String publicityOutputFolderName;

	@Inject
	@Named("export.publicity.path.prefix")
	private String publicityOutputPrefix;

	@Inject
	@Named("export.publicity.path.local")
	private String publicityOutputLocalPath;

	@Inject
	@Named("export.publicity.extention")
	private String publicityOutputExtension;
	
	@Inject
	@Named("export.channel.generic.output.folder")
	private String genericChannelOutputFolder;

	public String getCaptionOutputPath()
	{
		return captionOutputPath;
	}

	public void setCaptionOutputPath(String captionOutputPath)
	{
		this.captionOutputPath = captionOutputPath;
	}

	public String getCaptionOutputLocalPath()
	{
		return captionOutputLocalPath;
	}

	public void setCaptionOutputLocalPath(String captionOutputLocalPath)
	{
		this.captionOutputLocalPath = captionOutputLocalPath;
	}

	public String getCaptionOutputExtension()
	{
		return captionOutputExtension;
	}

	public void setCaptionOutputExtension(String captionOutputExtension)
	{
		this.captionOutputExtension = captionOutputExtension;
	}

	public String getComplianceOutputFolderName()
	{
		return complianceOutputFolderName;
	}

	public void setComplianceOutputFolderName(String complianceOutputFolderName)
	{
		this.complianceOutputFolderName = complianceOutputFolderName;
	}

	public String getComplianceOutputPrefix()
	{
		return complianceOutputPrefix;
	}

	public void setComplianceOutputPrefix(String complianceOutputPrefix)
	{
		this.complianceOutputPrefix = complianceOutputPrefix;
	}

	public String getComplianceOutputLocalPath()
	{
		return complianceOutputLocalPath;
	}

	public void setComplianceOutputLocalPath(String complianceOutputLocalPath)
	{
		this.complianceOutputLocalPath = complianceOutputLocalPath;
	}

	public String getComplianceOutputExtension()
	{
		return complianceOutputExtension;
	}

	public void setComplianceOutputExtension(String complianceOutputExtension)
	{
		this.complianceOutputExtension = complianceOutputExtension;
	}

	public String getPublicityOutputFolderName()
	{
		return publicityOutputFolderName;
	}

	public void setPublicityOutputFolderName(String publicityOutputFolderName)
	{
		this.publicityOutputFolderName = publicityOutputFolderName;
	}

	public String getPublicityOutputPrefix()
	{
		return publicityOutputPrefix;
	}

	public void setPublicityOutputPrefix(String publicityOutputPrefix)
	{
		this.publicityOutputPrefix = publicityOutputPrefix;
	}

	public String getPublicityOutputLocalPath()
	{
		return publicityOutputLocalPath;
	}

	public void setPublicityOutputLocalPath(String publicityOutputLocalPath)
	{
		this.publicityOutputLocalPath = publicityOutputLocalPath;
	}

	public String getPublicityOutputExtension()
	{
		return publicityOutputExtension;
	}

	public void setPublicityOutputExtension(String publicityOutputExtension)
	{
		this.publicityOutputExtension = publicityOutputExtension;
	}

	public void setGenericChannelOutputFolder(String genericChannelOutputFolder)
	{
		this.genericChannelOutputFolder=genericChannelOutputFolder;
	}
	
	public String getGenericChannelOutputFolder()
	{
		return genericChannelOutputFolder;
	}
}
