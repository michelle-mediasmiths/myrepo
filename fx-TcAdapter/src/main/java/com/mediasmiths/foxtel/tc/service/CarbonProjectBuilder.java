package com.mediasmiths.foxtel.tc.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.carbon.type.mutable.CarbonDestination;
import com.mediasmiths.carbon.type.mutable.CarbonModule;
import com.mediasmiths.carbon.type.mutable.CarbonMultisourceModuleDataSimple;
import com.mediasmiths.carbon.type.mutable.CarbonProject;
import com.mediasmiths.carbon.type.mutable.CarbonSource;
import com.mediasmiths.foxtel.pathresolver.PathResolver;
import com.mediasmiths.foxtel.tc.CarbonBaseProfile;
import com.mediasmiths.foxtel.tc.rest.api.TCBugOptions;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCResolution;
import com.mediasmiths.foxtel.tc.rest.api.TCTimecodeOptions;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;

@Singleton
public class CarbonProjectBuilder
{
	private static final Logger log = Logger.getLogger(CarbonProjectBuilder.class);

	/**
	 * The base folder where all prototype Carbon Projects and Filter XMLs will be loaded from
	 */
	@Inject
	@Named("carbon.pcp.folder")
	String pcpResourceBasePath;

	/**
	 * Filepath underneath pcpResourceBasePath to find the prototype timecode display filter XML
	 */
	@Inject(optional = true)
	@Named("carbon.timecode-filter.filename")
	String timecodeFilterResource = "timecode-filter.xml";

	/**
	 * Filepath underneath pcpResourceBasePath to find the prototype bug filter XML
	 */
	@Inject(optional = true)
	@Named("carbon.bug-filter.filename")
	String bugFilterResource = "bug-filter.xml";

	/**
	 * The folder where Carbon bugs are placed. We expect the bugs to be named:
	 * <pre><b>carbon.bug.folder.nix</b>/<b>CHANNEL</b>/bugs/<b>CHANNEL</b>_<b>HD/SD</b>_<b>TL/TR/BL/BR</b>.tga</pre>
	 */
	@Inject
	@Named("carbon.bug.folder.nix")
	String bugBasePath;

	@Inject
	PathResolver paths;

	//
	// Timecode bug values
	//
	private static final String TIMECODE_FILTER_BG_BLACK = "0";
	private static final String TIMECODE_FILTER_BG_WHITE = "1";
	private static final String TIMECODE_FILTER_FG_BLACK = "4";
	private static final String TIMECODE_FILTER_FG_WHITE = "0";
	private static final String TIMECODE_FILTER_TEXT_TOP = "0"; // Topmost
	private static final String TIMECODE_FILTER_TEXT_BOTTOM = "4"; // Bottommost
	private static final String TIMECODE_FILTER_TEXTSIZE_MEDIUM = "2";
	private static final String TIMECODE_FILTER_TEXTSIZE_LARGE = "3";


	public CarbonProject build(TCJobParameters parameters)
	{
		CarbonProject project = loadProject(pickProfile(parameters));

		customise(project, parameters);

		return project;
	}

	private void customise(final CarbonProject project, final TCJobParameters parameters)
	{
		if (parameters.timecode != null)
			addTimecode(project, parameters.timecode);

		if (parameters.bug != null)
			addBug(project, parameters.bug, parameters.resolution);

		setInputFile(project, parameters.inputFile);

		setOutputFile(project, parameters.outputFolder, parameters.outputFileBasename);
	}

	private void addBug(final CarbonProject project, final TCBugOptions parameters, final TCResolution resolution)
	{
		// Load bug filter resource template
		final Element xml = loadXML(bugFilterResource);
		final CarbonModule module = new CarbonModule(xml);

		// Customise the module
		final String bugFilename = getBugFilepath(parameters, resolution);

		// Set the bug filepath
		setAttribute(module, "Filename", bugFilename);

		// Set the opacity
		setAttribute(module, "Opacity.DBL", Double.toString(parameters.opacity));

		applyOutputFilter(project, module);
	}

	private String getBugFilepath(final TCBugOptions parameters, final TCResolution resolution)
	{
		// Bug filepath is "(channel)_(HD/SD)_(BL/BR/TL/TR).tga"
		final File folder = new File(new File(new File(bugBasePath), parameters.channel) + "bugs");

		final String filename = getBugFilename(parameters, resolution);

		final File file = new File(folder, filename);

		if (file.exists())
		{
			// TODO Convert to UNC
			return file.getAbsolutePath();
		}
		else
		{
			throw new IllegalArgumentException("Cannot find bug for requested channel, resolution and placement: " +
			                                   file.getAbsolutePath());
		}
	}

	String getBugFilename(final TCBugOptions parameters, final TCResolution resolution)
	{
		// Convert BOTTOM_LEFT to "BL", etc. for the filename
		final String positionString;
		switch (parameters.position)
		{
			case BOTTOM_LEFT:
				positionString = "BL";
				break;
			case BOTTOM_RIGHT:
				positionString = "BR";
				break;
			case TOP_LEFT:
				positionString = "TL";
				break;
			case TOP_RIGHT:
				positionString = "TR";
				break;
			default:
				throw new IllegalArgumentException("Invalid bug position: " + parameters.position);
		}

		return parameters.channel + "_" + resolution + "_" + positionString + ".tga";
	}

	private void addTimecode(final CarbonProject project, TCTimecodeOptions parameters)
	{
		// Load timecode filter resource template
		final Element xml = loadXML(timecodeFilterResource);
		final CarbonModule module = new CarbonModule(xml);

		// Customise the module

		// Position
		switch (parameters.location)
		{
			case TOP:
				setAttribute(module, "TextPlacement.DWD", TIMECODE_FILTER_TEXT_TOP);
				break;
			case BOTTOM:
				setAttribute(module, "TextPlacement.DWD", TIMECODE_FILTER_TEXT_BOTTOM);
				break;
			default:
				throw new IllegalArgumentException("Unsupported timecode location: " + parameters.location);
		}

		// Foreground colour
		switch (parameters.foreground)
		{
			case BLACK:
				setAttribute(module, "TextColor.DWD", TIMECODE_FILTER_FG_BLACK);
				break;
			case WHITE:
				setAttribute(module, "TextColor.DWD", TIMECODE_FILTER_FG_WHITE);
				break;
			default:
				throw new IllegalArgumentException("Unsupported timecode foreground colour: " + parameters.foreground);
		}

		// Background colour
		switch (parameters.background)
		{
			case TRANSPARENT:
				setAttribute(module, "UseBkgColor.DWD", "0");
				setAttribute(module, "MakeBkgTransp.DWD", "1");
				break;
			case BLACK:
				setAttribute(module, "UseBkgColor.DWD", "1");
				setAttribute(module, "BkgColor.DWD", TIMECODE_FILTER_BG_BLACK);
				setAttribute(module, "MakeBkgTransp.DWD", "0");
				break;
			case WHITE:
				setAttribute(module, "UseBkgColor.DWD", "1");
				setAttribute(module, "BkgColor.DWD", TIMECODE_FILTER_BG_WHITE);
				setAttribute(module, "MakeBkgTransp.DWD", "0");
				break;
			default:
				throw new IllegalArgumentException("Unsupported timecode background colour: " + parameters.background);
		}

		// Text size
		switch (parameters.size)
		{
			case LARGE:
				setAttribute(module, "TextSize.DWD", TIMECODE_FILTER_TEXTSIZE_LARGE);
				break;
			case MEDIUM:
				setAttribute(module, "TextSize.DWD", TIMECODE_FILTER_TEXTSIZE_MEDIUM);
				break;
			default:
				throw new IllegalArgumentException("Unsupported timecode text size: " + parameters.size);
		}

		applyOutputFilter(project, module);
	}

	private void setAttribute(CarbonModule module, String name, String value)
	{
		module.getModuleData().setAttribute(name, value);
	}

	private void applyOutputFilter(final CarbonProject project, final CarbonModule module)
	{
		CarbonDestination destination = project.getDestinations().get(0);

		destination.addVideoFilter(module);
	}

	private void setInputFile(final CarbonProject project, final String inputFile)
	{
		final String filepath = getExistingFileAsUNC(inputFile);

		final CarbonSource src = project.getSources().get(0);

		if (src.isMultiSource())
		{
			// We need to interpret the CarbonSource as a Multi Source input containing another CarbonSource
			CarbonMultisourceModuleDataSimple multiSourceData = new CarbonMultisourceModuleDataSimple(src.getModuleData()
			                                                                                             .getElement());

			final CarbonSource realSource = multiSourceData.getSource();

			// Change the source of the inner input
			realSource.setFullUNCFilename(filepath);
		}
		else
		{
			// Change the filepath for the source
			src.setFullUNCFilename(filepath);
		}
	}

	/**
	 * Translate a UNIX Filepath to UNC, also asserting that it exists and can be accessed by this host (using the NIX path)
	 *
	 * @param nixFile
	 *
	 * @return
	 */
	private String getExistingFileAsUNC(String nixFile)
	{
		File file = new File(nixFile);

		if (file.exists())
			return nixtoUNC(nixFile);
		else
			throw new IllegalArgumentException("File does not exist (but it is expected to): " + nixFile);
	}

	/**
	 * Convert a UNIX path to a UNC
	 *
	 * @param nixFile
	 *
	 * @return
	 */
	private String nixtoUNC(String nixFile)
	{
		return paths.uncPath(PathResolver.PathType.NIX, nixFile);
	}

	private void setOutputFile(final CarbonProject project, final String outputFolder, final String outputFileBasename)
	{
		final String filepath = nixtoUNC(outputFolder);

		CarbonDestination destination = project.getDestinations().get(0);

		destination.setDestinationUNC(filepath);
		destination.setDestinationBaseFilename(outputFileBasename);
	}

	protected CarbonBaseProfile pickProfile(TCJobParameters parameters)
	{
		for (CarbonBaseProfile profile : CarbonBaseProfile.values())
		{
			if (profile.suitableFor(parameters.purpose, parameters.resolution, parameters.audioType))
			{
				return profile;
			}
		}

		throw new IllegalArgumentException("No suitable Carbon base project found!");
	}

	protected CarbonProject loadProject(CarbonBaseProfile profile)
	{
		final Element xml = loadXML(profile.name() + ".xml");

		return new CarbonProject(xml.getDocument());
	}

	/**
	 * @param name
	 *
	 * @return
	 */
	protected Element loadXML(String name)
	{
		final File folder = new File(pcpResourceBasePath);
		final File pcp = new File(folder, name);

		try
		{
			final Document document = new SAXBuilder().build(pcp);

			return document.getRootElement();
		}
		catch (JDOMException e)
		{
			log.error("Error loading PCP resource " + pcp, e);
			throw new RuntimeException("Error loading PCP " + pcp, e);
		}
		catch (IOException e)
		{
			log.error("Error loading PCP resource " + pcp, e);
			throw new RuntimeException("Error loading PCP " + pcp, e);
		}
	}


	public static void main(String[] args) throws Exception
	{
		JAXBSerialiser ser = JAXBSerialiser.getInstance(TCJobParameters.class);
		ser.setPrettyOutput(true);

		System.out.println(ser.serialise(new TCJobParameters()));
	}
}
