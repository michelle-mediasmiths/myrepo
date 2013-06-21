package com.mediasmiths.foxtel.wf.adapter.model;

import com.mediasmiths.foxtel.tc.rest.api.TCFTPUpload;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TCPassedNotification extends TCNotification
{

	private TCFTPUpload ftpupload;

    @XmlElement(required=false)
    public TCFTPUpload getFtpupload()
    {
            return this.ftpupload;
    }

    public void setFtpupload(TCFTPUpload upload)
    {
            this.ftpupload = upload;
    }

	
}
