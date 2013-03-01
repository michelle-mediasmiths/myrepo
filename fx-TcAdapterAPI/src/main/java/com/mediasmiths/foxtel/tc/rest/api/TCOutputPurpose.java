package com.mediasmiths.foxtel.tc.rest.api;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum TCOutputPurpose
{
	TX_HD, TX_SD, DVD, CAPTIONING, MPG4;
}
