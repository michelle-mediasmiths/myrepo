package com.mediasmiths.mayam;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.FileFormatInfo;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileFormatVerification
{
	@Inject
	@Named("ff.sd.wrapper.format")
	private String sdWrapperFormat;
	@Inject
	@Named("ff.sd.wrapper.mime")
	private String sdWrapperMime;
	@Inject
	@Named("ff.sd.video.aspect")
	private String sdVideoAspect;
	@Inject
	@Named("ff.sd.video.imagex")
	private String sdVideoX;
	@Inject
	@Named("ff.sd.video.imagey")
	private String sdVideoY;
	@Inject
	@Named("ff.sd.video.chroma")
	private String sdVideoChroma;
	@Inject
	@Named("ff.sd.video.encoding")
	private String sdVideoEncoding;
	@Inject
	@Named("ff.sd.video.gopL")
	private String sdVideoGopL;
	@Inject
	@Named("ff.sd.video.bitrate")
	private String sdVideoBitrate;
	@Inject
	@Named("ff.sd.video.frameratex100")
	private String sdVideoFrameRate100;

	@Inject
	@Named("ff.sd.audio.encoding")
	private String sdAudioEncoding = "pcm";
	@Inject
	@Named("ff.sd.audio.format")
	private String sdAudioFormat = "aes";
	@Inject
	@Named("ff.sd.audio.mime")
	private String sdAudioMime = "audio/x-aes";
	@Inject
	@Named("ff.sd.audio.samplerate")
	private String sdAudioSampleRate;
	@Inject
	@Named("ff.sd.audio.samplewidth")
	private String sdSampleWidth;
	@Inject
	@Named("ff.hd.wrapper.format")
	private String hdWrapperFormat;
	@Inject
	@Named("ff.hd.wrapper.mime")
	private String hdWrapperMime;

	@Inject
	@Named("ff.hd.video.aspect")
	private String hdVideoAspect;
	@Inject
	@Named("ff.hd.video.imagex")
	private String hdVideoX;
	@Inject
	@Named("ff.hd.video.imagey")
	private String hdVideoY;
	@Inject
	@Named("ff.hd.video.chroma")
	private String hdVideoChroma;
	@Inject
	@Named("ff.hd.video.encoding")
	private String hdVideoEncoding;
	@Inject
	@Named("ff.hd.video.gopL")
	private String hdVideoGopL;
	@Inject
	@Named("ff.hd.video.bitrate")
	private String hdVideoBitrate;
	@Inject
	@Named("ff.hd.video.frameratex100")
	private String hdVideoFrameRate100;

	@Inject
	@Named("ff.hd.audio.encoding")
	private String hdAudioEncoding;
	@Inject
	@Named("ff.hd.audio.format")
	private String hdAudioFormat;
	@Inject
	@Named("ff.hd.audio.mime")
	private String hdAudioMime;
	@Inject
	@Named("ff.hd.audio.samplerate")
	private String hdAudioSampleRate;
	@Inject
	@Named("ff.hd.audio.samplewidth")
	private String hdSampleWidth;

	private final static Logger log = Logger.getLogger(FileFormatVerification.class);

	public FileFormatVerificationResult verifyFileFormat(FileFormatInfo fileInfo, AttributeMap materialAttributes)
	{
		log.info(String.format(
				"File format verification for asset %s",
				materialAttributes.getAttributeAsString(Attribute.ASSET_ID)));

		String reqfmt = materialAttributes.getAttribute(Attribute.REQ_FMT);
		boolean requiredFormatKnown = false;
		boolean requiredFormatSD = false;

		if (reqfmt != null)
		{
			requiredFormatKnown = true;
			if (reqfmt.toLowerCase().equals("sd"))
			{
				requiredFormatSD = true;
			}
		}

		String materialID = materialAttributes.getAttributeAsString(Attribute.HOUSE_ID);
		String assetID = materialAttributes.getAttributeAsString(Attribute.ASSET_ID);

		// video codec
		String videoChroma = fileInfo.getVideoChroma();
		String videoEncoding = fileInfo.getVideoEncoding();
		Integer videoBitrate = fileInfo.getVideoBitrate();
		Integer imageSizeX = fileInfo.getImageSizeX();
		Integer imageSizeY = fileInfo.getImageSizeY();
		Integer frameRate100 = fileInfo.getFrameRate100();
		Integer videoGopL = fileInfo.getVideoGopsize();

		// aspect ratio
		String videoAspect = fileInfo.getVideoAspect();

		// audio codec
		String audioEncoding = fileInfo.getAudioEncoding();
		String audioFormat = fileInfo.getAudioFormat();
		Integer audioSamplerate = fileInfo.getAudioSamplerate();
		Integer audioSamplewidth = fileInfo.getAudioSamplewidth();

		// wrapper
		String wrapperFormat = fileInfo.getWrapperFormat();
		String wrapperMime = fileInfo.getWrapperMime();

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("File Format verification for Material %s  (Ardome ID %s)\n", materialID, assetID));

		List<FileFormatTest> sdTests = new ArrayList<FileFormatTest>();
		List<FileFormatTest> hdTests = new ArrayList<FileFormatTest>();

		// hd tests

		// video codec
		hdTests.add(new FileFormatTest(hdVideoChroma, videoChroma, "Chroma"));
		hdTests.add(new FileFormatTest(hdVideoEncoding, videoEncoding, "Encoding"));
		hdTests.add(new FileFormatTest(hdVideoBitrate, videoBitrate, "Video Bitrate"));
		hdTests.add(new FileFormatTest(hdVideoX, imageSizeX, "Video Image X"));
		hdTests.add(new FileFormatTest(hdVideoY, imageSizeY, "Video Image Y"));
		hdTests.add(new FileFormatTest(hdVideoFrameRate100, frameRate100, "Framerate"));
		hdTests.add(new FileFormatTest(hdVideoGopL, videoGopL, "Gop Size (L)"));
		// aspect ratio
		hdTests.add(new FileFormatTest(hdVideoAspect, videoAspect, "Aspect Ratio"));
		// audio codec
		hdTests.add(new FileFormatTest(hdAudioEncoding, audioEncoding, "Audio Encoding"));
		hdTests.add(new FileFormatTest(hdAudioFormat, audioFormat, "Audio Format"));
		hdTests.add(new FileFormatTest(hdAudioSampleRate, audioSamplerate, "Audio Sample Rate"));
		hdTests.add(new FileFormatTest(hdSampleWidth, audioSamplewidth, "Audio Sample Width"));
		// wrapper
		hdTests.add(new FileFormatTest(hdWrapperFormat, wrapperFormat, "Wrapper format"));
		hdTests.add(new FileFormatTest(hdWrapperMime, wrapperMime, "Wrapper format mime type"));

		// sd tests

		// video codec
		sdTests.add(new FileFormatTest(sdVideoChroma, videoChroma, "Chroma"));
		sdTests.add(new FileFormatTest(sdVideoEncoding, videoEncoding, "Encoding"));
		sdTests.add(new FileFormatTest(sdVideoBitrate, videoBitrate, "Video Bitrate"));
		sdTests.add(new FileFormatTest(sdVideoX, imageSizeX, "Video Image X"));
		sdTests.add(new FileFormatTest(sdVideoY, imageSizeY, "Video Image Y"));
		sdTests.add(new FileFormatTest(sdVideoFrameRate100, frameRate100, "Framerate"));
		sdTests.add(new FileFormatTest(sdVideoGopL, videoGopL, "Gop Size (L)"));
		// aspect ratio
		sdTests.add(new FileFormatTest(sdVideoAspect, videoAspect, "Aspect Ratio"));
		// audio codec
		sdTests.add(new FileFormatTest(sdAudioEncoding, audioEncoding, "Audio Encoding"));
		sdTests.add(new FileFormatTest(sdAudioFormat, audioFormat, "Audio Format"));
		sdTests.add(new FileFormatTest(sdAudioSampleRate, audioSamplerate, "Audio Sample Rate"));
		sdTests.add(new FileFormatTest(sdSampleWidth, audioSamplewidth, "Audio Sample Width"));
		// wrapper
		sdTests.add(new FileFormatTest(sdWrapperFormat, wrapperFormat, "Wrapper format"));
		sdTests.add(new FileFormatTest(sdWrapperMime, wrapperMime, "Wrapper format mime type"));

		// Finding the format type been delivered

		log.debug("Performing file format verification tests against both SD and HD requirements");

		Boolean sdTestsPass = performTests(sb,sdTests,"SD Requirements");
		Boolean hdTestsPass = performTests(sb, hdTests, "HD Requirements");
		Boolean eitherPass = sdTestsPass || hdTestsPass;

		if (sdTestsPass)
		{
			//Delivered format is SD
			log.debug("format validated as sd");
			String detail = sb.toString();
			log.info(detail);
		}
		else
		{
			log.debug("sd test format failed");
		}

		if (hdTestsPass)
		{
			//Delivered Format is HD
			log.debug("format validated as hd");
			String detail = sb.toString();
			log.info(detail);
		}
		else
		{
			log.debug("hd test format failed");
		}

		if (!eitherPass)
		{
			//file format verification failed
			log.debug("Tests failed for both asset as both hd and sd");
			return new FileFormatVerificationResult(false, sb.toString());
		}
		else
		{
			if (requiredFormatKnown && ((!requiredFormatSD) && sdTestsPass)) //require format is known to not be sd but sd content has arrived
			{
				log.info("Required format is HD and SD is arrived. So failing the file format verification");
				return new FileFormatVerificationResult(false, sb.toString());
			}
			else
			{
				log.debug("File format verification passed");
				return new FileFormatVerificationResult(true,sb.toString());
			}
		}

	}

	private boolean performTests(StringBuilder sb, List<FileFormatTest> tests, String testListName)
	{
		boolean allPass = true;

		sb.append(testListName+"\n");

		for (FileFormatTest fileFormatTest : tests)
		{
			if (!(fileFormatTest.check(sb)))
			{
				allPass = false;
			}
		}

		return allPass;
	}

	class FileFormatTest
	{
		String expected;
		String actual;
		String description;

		public FileFormatTest(String expected, Integer actual, String description)
		{
			this.expected = expected;
			this.actual = actual.toString();
			this.description = description;
		}

		public FileFormatTest(String expected, String actual, String description)
		{
			this.expected = expected;
			this.actual = actual;
			this.description = description;
		}

		public Object getExpected()
		{
			return expected;
		}

		public Object getActual()
		{
			return actual;
		}

		public String getDescription()
		{
			return description;
		}

		public boolean check(StringBuilder sb)
		{
			sb.append(String.format("%s : expected={%s} actual={%s}", description, expected, actual));

			if (expected.equals(actual))
			{
				sb.append("\n");
				return true;
			}
			else if (expected instanceof String)
			{ // allows multiple comma separated values to be specified for string values

				String exp = (String) expected;
				String[] split = exp.split(",");
				List<String> asList = Arrays.asList(split);

				if (asList.contains(actual))
				{
					sb.append("\n");
					return true;
				}
				else
				{
					sb.append("*\n");
					return false;
				}
			}
			else
			{
				sb.append("*\n");
				return false;
			}
		}
	}

	public class FileFormatVerificationResult
	{

		private boolean pass;
		private String detail;

		public FileFormatVerificationResult(boolean pass, String detail)
		{
			this.pass = pass;
			this.detail = detail;
		}

		public boolean isPass()
		{
			return pass;
		}

		public String getDetail()
		{
			return detail;
		}

	};

}
