package com.mediasmiths.mq.transferqueue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.UUID;

@XmlRootElement(name = "TransferItem")
public class TransferItem
{
	@XmlElement(name = "transferId")
	public String id;
	@XmlElement
	public String assetId;
	@XmlElement
	public String assetHouseId;
	@XmlElement
	public String assetPeerId;
	@XmlElement
	public Date timeout;


	/**
	 * @deprecated for JAXB Serialiser use only!
	 */
	@Deprecated
	public TransferItem()
	{
	}


	public TransferItem(final String assetId,final String assetHouseId,final String assetPeerId, Date timeout)
	{
		this(UUID.randomUUID().toString(), assetId, assetHouseId,assetPeerId, timeout);
	}


	public TransferItem(String id, final String assetId, final String assetHouseId,final String assetPeerId, Date timeout)
	{
		this.id = id;
		this.assetId = assetId;
		this.assetHouseId=assetHouseId;
		this.assetPeerId = assetPeerId;
		this.timeout = timeout;
	}
}
