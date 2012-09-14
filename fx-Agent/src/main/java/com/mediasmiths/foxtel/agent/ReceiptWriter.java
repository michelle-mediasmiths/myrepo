package com.mediasmiths.foxtel.agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ReceiptWriter {

	private static Logger logger = Logger.getLogger(ReceiptWriter.class);
	private final String receiptPath;
	
	@Inject
	public ReceiptWriter(@Named("agent.path.receipt") String receiptPath){
		this.receiptPath=receiptPath;
	}
	
	public String receiptPathForMessageID(String messageID){
		String path = receiptPath + IOUtils.DIR_SEPARATOR + messageID+ ".txt";
		return path;
	}
	
	public void writeRecipet(String filePath, String receiptText) throws FileNotFoundException, IOException {
		
		String path = receiptPathForMessageID(receiptText);
		
		logger.info(String.format("Writing reciept for messageID %s (%s) to %s", receiptText,filePath,path));
		
		IOUtils.write(receiptText, new FileOutputStream(new File(path)));
	}

}
