package com.mediasmiths.foxtel.wf.adapter.model;

import javax.xml.bind.annotation.XmlRootElement;
import com.mediasmiths.foxtel.tc.rest.api.TCFTPUpload;
import javax.xml.bind.annotation.XmlElement;

@XmlRootElement
public class TCPassedNotification extends TCNotification
{
	private TCFTPUpload ftpupload;

    @XmlElement(required=false)
    public TCFTPUpload getFtpupload()
    {
            return this.ftpupload;
    }

    public void getFtpupload(TCFTPUpload upload)
    {
            this.ftpupload = upload;
    }


}
