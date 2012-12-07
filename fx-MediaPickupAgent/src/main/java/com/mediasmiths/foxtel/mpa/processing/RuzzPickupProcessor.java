//package com.mediasmiths.foxtel.mpa.processing;
//
//import com.mediasmiths.foxtel.agent.MessageEnvelope;
//import com.mediasmiths.foxtel.agent.processing.MessageProcessingFailedException;
//import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
//import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
//import com.mediasmiths.foxtel.generated.ruzz.RuzzIngestRecord;
//
//public class RuzzPickupProcessor extends MediaPickupProcessor<RuzzIngestRecord>
//{
//
//	@Override
//	protected void typeCheck(Object unmarshalled) throws ClassCastException { // NOSONAR
//		// throwing unchecked exception as hint to users of class that this
//		// method is likely to throw ClassCastException
//
//		if (!(unmarshalled instanceof RuzzIngestRecord)) {
//			throw new ClassCastException(String.format(
//					"unmarshalled type %s is not a RuzzIngestRecord",
//					unmarshalled.getClass().toString()));
//		}
//
//	}
//
//	@Override
//	protected String getIDFromMessage(MessageEnvelope<RuzzIngestRecord> envelope)
//	{
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected void messageValidationFailed(String filePath, MessageValidationResult result)
//	{
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	protected void processMessage(MessageEnvelope<RuzzIngestRecord> envelope) throws MessageProcessingFailedException
//	{
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	protected void processNonMessageFile(String filePath)
//	{
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	protected boolean shouldArchiveMessages()
//	{
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//
//}
