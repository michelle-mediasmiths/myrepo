package com.mediasmiths.foxtel.carbon.message;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TransformerTest
{

	@Test
	public void testTransformer(){
		
		assertEquals("CarbonAPIXML1 3 foo", new Transformer().buildMessageForData("foo"));
		
	}
	
}
