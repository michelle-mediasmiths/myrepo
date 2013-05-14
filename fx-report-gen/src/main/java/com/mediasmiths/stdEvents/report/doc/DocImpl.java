package com.mediasmiths.stdEvents.report.doc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class DocImpl
{
	public void createDoc() throws FileNotFoundException, IOException
	{
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream("random.doc"));
		HWPFDocument doc = new HWPFDocument(fs);
	}
}
