/**
 * ControlMediaSet.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

import javax.annotation.Generated;

@Generated(value = {"axis"})public class ControlMediaSet  implements java.io.Serializable {
    /* The name of the MediaSet to be
     *                                     controlled */
    private java.lang.String mediaSetName;

    /* The action to be performed upon the
     *                                     MediaSet */
    private com.tektronix.www.cerify.soap.client.MediaSetActionType action;

    public ControlMediaSet() {
    }

    public ControlMediaSet(
           java.lang.String mediaSetName,
           com.tektronix.www.cerify.soap.client.MediaSetActionType action) {
           this.mediaSetName = mediaSetName;
           this.action = action;
    }


    /**
     * Gets the mediaSetName value for this ControlMediaSet.
     * 
     * @return mediaSetName   * The name of the MediaSet to be
     *                                     controlled
     */
    public java.lang.String getMediaSetName() {
        return mediaSetName;
    }


    /**
     * Sets the mediaSetName value for this ControlMediaSet.
     * 
     * @param mediaSetName   * The name of the MediaSet to be
     *                                     controlled
     */
    public void setMediaSetName(java.lang.String mediaSetName) {
        this.mediaSetName = mediaSetName;
    }


    /**
     * Gets the action value for this ControlMediaSet.
     * 
     * @return action   * The action to be performed upon the
     *                                     MediaSet
     */
    public com.tektronix.www.cerify.soap.client.MediaSetActionType getAction() {
        return action;
    }


    /**
     * Sets the action value for this ControlMediaSet.
     * 
     * @param action   * The action to be performed upon the
     *                                     MediaSet
     */
    public void setAction(com.tektronix.www.cerify.soap.client.MediaSetActionType action) {
        this.action = action;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ControlMediaSet)) return false;
        ControlMediaSet other = (ControlMediaSet) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.mediaSetName==null && other.getMediaSetName()==null) || 
             (this.mediaSetName!=null &&
              this.mediaSetName.equals(other.getMediaSetName()))) &&
            ((this.action==null && other.getAction()==null) || 
             (this.action!=null &&
              this.action.equals(other.getAction())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getMediaSetName() != null) {
            _hashCode += getMediaSetName().hashCode();
        }
        if (getAction() != null) {
            _hashCode += getAction().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ControlMediaSet.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">ControlMediaSet"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mediaSetName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "mediaSetName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("action");
        elemField.setXmlName(new javax.xml.namespace.QName("", "action"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "mediaSetActionType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
