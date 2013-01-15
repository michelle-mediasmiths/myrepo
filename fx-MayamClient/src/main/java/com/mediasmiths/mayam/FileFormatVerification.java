package com.mediasmiths.mayam;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.FileFormatInfo;

public class FileFormatVerification
{
	@Inject @Named("ff.sd.wrapper.format")
	private String sdWrapperFormat = "mxf";
	@Inject @Named("ff.sd.wrapper.mime")
	private String sdWrapperMime = "video/x-mxf";
	
	@Inject @Named("ff.sd.video.imagex")
	private int sdVideoX = 720;
	@Inject @Named("ff.sd.video.imagey")
	private int sdVideoY = 608;
	@Inject @Named("ff.sd.video.chroma")
	private String sdVideoChroma = "4:2:2";
	@Inject @Named("ff.sd.video.encoding")
	private String sdVideoEncoding = "mpeg2";
	@Inject @Named("ff.sd.video.bitrate")
	private int sdVideoBitrate = 50000000; //50 Mbps (CBR) 
	@Inject @Named("ff.sd.video.frameratex100")
	private int sdVideoFrameRate100 = 2500;
	
	@Inject @Named("ff.sd.audio.encoding")
	private String sdAudioEncoding = "pcm";
	@Inject @Named("ff.sd.audio.format")
	private String sdAudioFormat = "aes";
	@Inject @Named("ff.sd.audio.mime")
	private String sdAudioMime = "audio/x-aes";
	@Inject @Named("ff.sd.audio.samplerate")
	private int sdAudioSampleRate = 48000;
	@Inject @Named("ff.sd.audio.samplewidth")
	private int sdSampleWidth = 24;
	
	@Inject @Named("ff.hd.wrapper.format")
	private String hdWrapperFormat;
	@Inject @Named("ff.hd.wrapper.mime")
	private String hdWrapperMime;
	
	@Inject @Named("ff.hd.video.imagex")
	private int hdVideoX;
	@Inject @Named("ff.hd.video.imagey")
	private int hdVideoY;
	@Inject @Named("ff.hd.video.chroma")
	private String hdVideoChroma;
	@Inject @Named("ff.hd.video.encoding")
	private String hdVideoEncoding;
	@Inject @Named("ff.hd.video.bitrate")
	private int hdVideoBitrate;
	@Inject @Named("ff.hd.video.frameratex100")
	private int hdVideoFrameRate100;
	
	@Inject @Named("ff.hd.audio.encoding")
	private String hdAudioEncoding;
	@Inject @Named("ff.hd.audio.format")
	private String hdAudioFormat;
	@Inject @Named("ff.hd.audio.mime")
	private String hdAudioMime;
	@Inject @Named("ff.hd.audio.samplerate")
	private int hdAudioSampleRate;
	@Inject @Named("ff.hd.audio.samplewidth")
	private int hdSampleWidth;
	
	//AR???
	
	public boolean verifyFileFormat(FileFormatInfo fileInfo, AttributeMap materialAttributes) throws FileFormatVerificationFailureException{
		
		boolean hd = false;
		hd = isHD(materialAttributes);
		
		String materialID = materialAttributes.getAttributeAsString(Attribute.HOUSE_ID);
		String assetID = materialAttributes.getAttributeAsString(Attribute.ASSET_ID);
		
		//video codec
		String videoChroma = fileInfo.getVideoChroma();
		String videoEncoding = fileInfo.getVideoEncoding();
		Integer videoBitrate = fileInfo.getVideoBitrate();
		Integer imageSizeX = fileInfo.getImageSizeX();
		Integer imageSizeY = fileInfo.getImageSizeY();
		Integer frameRate100 = fileInfo.getFrameRate100();
			
		//TODO: aspect ration + goplength
		
		//aspect ratio
		String videoAspect = fileInfo.getVideoAspect();
				
		//audio codec
		String audioEncoding = fileInfo.getAudioEncoding();
		String audioFormat = fileInfo.getAudioFormat();
		Integer audioSamplerate = fileInfo.getAudioSamplerate();
		Integer audioSamplewidth = fileInfo.getAudioSamplewidth();
		
		//wrapper
		String wrapperFormat = fileInfo.getWrapperFormat();
		String wrapperMime = fileInfo.getWrapperMime();
	

		StringBuilder sb = new StringBuilder();	
		sb.append(String.format("File Format verification for Material %s  (Ardome ID %s)\n", materialID,assetID));
		
		List<FileFormatTest> tests = new ArrayList<FileFormatTest>();
		
		if(hd)
		{
			//video codec
			tests.add(new FileFormatTest(hdVideoChroma, videoChroma, "Chroma"));
			tests.add(new FileFormatTest(hdVideoEncoding, videoEncoding, "Encoding"));			
			tests.add(new FileFormatTest(hdVideoBitrate, videoBitrate, "Video Bitrate"));
			tests.add(new FileFormatTest(hdVideoX, imageSizeX, "Video Image X"));
			tests.add(new FileFormatTest(hdVideoY, imageSizeY, "Video Image Y"));
			tests.add(new FileFormatTest(hdVideoFrameRate100, frameRate100, "Framerate"));
			//audio codec
			tests.add(new FileFormatTest(hdAudioEncoding, audioEncoding, "Audio Encoding"));
			tests.add(new FileFormatTest(hdAudioFormat, audioFormat, "Audio Format"));
			tests.add(new FileFormatTest(hdAudioSampleRate, audioSamplerate, "Audio Sample Rate"));
			tests.add(new FileFormatTest(hdSampleWidth, audioSamplewidth, "Audio Sample Width"));
			//wrapper
			tests.add(new FileFormatTest(hdWrapperFormat, wrapperFormat, "Wrapper format"));
			tests.add(new FileFormatTest(hdWrapperMime, wrapperMime, "Wrapper format mime type"));
			
		}
		else
		{

			//video codec
			tests.add(new FileFormatTest(sdVideoChroma, videoChroma, "Chroma"));
			tests.add(new FileFormatTest(sdVideoEncoding, videoEncoding, "Encoding"));			
			tests.add(new FileFormatTest(sdVideoBitrate, videoBitrate, "Video Bitrate"));
			tests.add(new FileFormatTest(sdVideoX, imageSizeX, "Video Image X"));
			tests.add(new FileFormatTest(sdVideoY, imageSizeY, "Video Image Y"));
			tests.add(new FileFormatTest(sdVideoFrameRate100, frameRate100, "Framerate"));
			//audio codec
			tests.add(new FileFormatTest(sdAudioEncoding, audioEncoding, "Audio Encoding"));
			tests.add(new FileFormatTest(sdAudioFormat, audioFormat, "Audio Format"));
			tests.add(new FileFormatTest(sdAudioSampleRate, audioSamplerate, "Audio Sample Rate"));
			tests.add(new FileFormatTest(sdSampleWidth, audioSamplewidth, "Audio Sample Width"));
			//wrapper
			tests.add(new FileFormatTest(sdWrapperFormat, wrapperFormat, "Wrapper format"));
			tests.add(new FileFormatTest(sdWrapperMime, wrapperMime, "Wrapper format mime type"));
			
		}
		
		boolean anyFailures = false;		
		
		for (FileFormatTest fileFormatTest : tests)
		{
			anyFailures &= !fileFormatTest.check(sb);
		}		
		
		if(anyFailures){
			throw new FileFormatVerificationFailureException(sb.toString());
		}
		
		System.out.println(sb.toString());
		
		return anyFailures;
	}
	
	class FileFormatTest
	{
		Object expected;
		Object actual;
		String description;
		
		public FileFormatTest(int expected,Integer actual,String description){
			this.expected=Integer.valueOf(expected);
			this.actual=actual;
			this.description=description;
		}
		
		public FileFormatTest(Object expected,Object actual,String description){
			this.expected=expected;
			this.actual=actual;
			this.description=description;
		}
		
		public Object getExpected(){
			return expected;
		}
		
		public Object getActual(){
			return actual;
		}
		public String getDescription(){
			return description;
		}
		public boolean check(StringBuilder sb){
			sb.append(String.format("%s : expected={%s} actual={%s}",description,expected,actual));		
			
			if(expected.equals(actual)){
				sb.append("\n");
				return true;
			}
			else{
				sb.append("*\n");
				return false;
			}
		}
	}
	
	private boolean isHD(AttributeMap materialAttributes)
	{
		String reqfmt = materialAttributes.getAttribute(Attribute.REQ_FMT);
		
		if(reqfmt != null && reqfmt.toLowerCase().equals("hd")){
			return true;
		}
		else if(reqfmt != null){
			return false;			
		}
		else{
			return false; //or should we guess becuase there is no REQ_FMT?
		}
	}

}
