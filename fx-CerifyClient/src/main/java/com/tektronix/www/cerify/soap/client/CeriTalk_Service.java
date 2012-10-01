/**
 * CeriTalk_Service.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

import javax.annotation.Generated;

@Generated(value = {"axis"})public interface CeriTalk_Service extends javax.xml.rpc.Service {
    public java.lang.String getCeriTalkSOAPAddress();

    public com.tektronix.www.cerify.soap.client.CeriTalk_PortType getCeriTalkSOAP() throws javax.xml.rpc.ServiceException;

    public com.tektronix.www.cerify.soap.client.CeriTalk_PortType getCeriTalkSOAP(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
