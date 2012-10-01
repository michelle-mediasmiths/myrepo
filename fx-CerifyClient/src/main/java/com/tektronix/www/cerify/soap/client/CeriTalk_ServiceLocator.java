/**
 * CeriTalk_ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

import javax.annotation.Generated;

@Generated(value = {"axis"})public class CeriTalk_ServiceLocator extends org.apache.axis.client.Service implements com.tektronix.www.cerify.soap.client.CeriTalk_Service {

    public CeriTalk_ServiceLocator() {
    }


    public CeriTalk_ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public CeriTalk_ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for CeriTalkSOAP
    private java.lang.String CeriTalkSOAP_address = "http://REPLACE_WITH_ACTUAL_URL";

    public java.lang.String getCeriTalkSOAPAddress() {
        return CeriTalkSOAP_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String CeriTalkSOAPWSDDServiceName = "CeriTalkSOAP";

    public java.lang.String getCeriTalkSOAPWSDDServiceName() {
        return CeriTalkSOAPWSDDServiceName;
    }

    public void setCeriTalkSOAPWSDDServiceName(java.lang.String name) {
        CeriTalkSOAPWSDDServiceName = name;
    }

    public com.tektronix.www.cerify.soap.client.CeriTalk_PortType getCeriTalkSOAP() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(CeriTalkSOAP_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getCeriTalkSOAP(endpoint);
    }

    public com.tektronix.www.cerify.soap.client.CeriTalk_PortType getCeriTalkSOAP(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.tektronix.www.cerify.soap.client.CeriTalkSOAPStub _stub = new com.tektronix.www.cerify.soap.client.CeriTalkSOAPStub(portAddress, this);
            _stub.setPortName(getCeriTalkSOAPWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setCeriTalkSOAPEndpointAddress(java.lang.String address) {
        CeriTalkSOAP_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.tektronix.www.cerify.soap.client.CeriTalk_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.tektronix.www.cerify.soap.client.CeriTalkSOAPStub _stub = new com.tektronix.www.cerify.soap.client.CeriTalkSOAPStub(new java.net.URL(CeriTalkSOAP_address), this);
                _stub.setPortName(getCeriTalkSOAPWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("CeriTalkSOAP".equals(inputPortName)) {
            return getCeriTalkSOAP();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "CeriTalk");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "CeriTalkSOAP"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("CeriTalkSOAP".equals(portName)) {
            setCeriTalkSOAPEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
