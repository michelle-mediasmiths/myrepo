package com.mediasmiths.foxtel.agent;
import static com.mediasmiths.foxtel.agent.Config.RECEIPT_PATH;

import java.io.File;
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
	public ReceiptWriter(@Named(RECEIPT_PATH) String receiptPath){
		this.receiptPath=receiptPath;
	}
	
	public String receiptPathForMessageID(String messageID){
		String path = receiptPath + IOUtils.DIR_SEPARATOR + messageID+ ".txt";
		return path;
	}
	
	public void writeRecipet(String filePath, String receiptText) throws IOException {
		
		String path = receiptPathForMessageID(receiptText);
		
		logger.info(String.format("Writing reciept for messageID %s (%s) to %s", receiptText,filePath,path));
		
		IOUtils.write(receiptText, new FileOutputStream(new File(path)));
	}

}
