package com.mediasmiths.foxtel.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.processing.MessageProcessor;

public class ReceiptWriter {

	private static Logger logger = Logger.getLogger(ReceiptWriter.class);
	
	private final static String receiptFolderName = "completed";
	
	@Inject
	public ReceiptWriter(){
		
	}
	
	protected String getReceiptPathForFile(String messagePath) {
		
		String pathToFile = messagePath;	
		
		String receiptPath = FilenameUtils.getFullPath(pathToFile) + receiptFolderName + IOUtils.DIR_SEPARATOR;
		
		logger.debug(String.format("returning receipt folder %s for file %s ", receiptPath,pathToFile));
		
		return receiptPath;
	}

	
	public String receiptPathForMessageID(String messagePath,String messageID){
		
		String path = getReceiptPathForFile(messagePath) + IOUtils.DIR_SEPARATOR + messageID+ ".txt";
		return path;
	}
	
	public void writeRecipet(String filePath, String receiptText) throws IOException {
		
		String path = receiptPathForMessageID(filePath,receiptText);
		
		logger.info(String.format("Writing reciept for messageID %s (%s) to %s", receiptText,filePath,path));
		
		IOUtils.write(receiptText, new FileOutputStream(new File(path)));
	}

}
