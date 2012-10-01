/**
 * GetProfilesResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

import javax.annotation.Generated;

@Generated(value = {"axis"})public class GetProfilesResponse  implements java.io.Serializable {
    /* A list of all active (non-archived)
     *                                     Profile names */
    private java.lang.String[] profileName;

    public GetProfilesResponse() {
    }

    public GetProfilesResponse(
           java.lang.String[] profileName) {
           this.profileName = profileName;
    }


    /**
     * Gets the profileName value for this GetProfilesResponse.
     * 
     * @return profileName   * A list of all active (non-archived)
     *                                     Profile names
     */
    public java.lang.String[] getProfileName() {
        return profileName;
    }


    /**
     * Sets the profileName value for this GetProfilesResponse.
     * 
     * @param profileName   * A list of all active (non-archived)
     *                                     Profile names
     */
    public void setProfileName(java.lang.String[] profileName) {
        this.profileName = profileName;
    }

    public java.lang.String getProfileName(int i) {
        return this.profileName[i];
    }

    public void setProfileName(int i, java.lang.String _value) {
        this.profileName[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetProfilesResponse)) return false;
        GetProfilesResponse other = (GetProfilesResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.profileName==null && other.getProfileName()==null) || 
             (this.profileName!=null &&
              java.util.Arrays.equals(this.profileName, other.getProfileName())));
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
        if (getProfileName() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getProfileName());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getProfileName(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetProfilesResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetProfilesResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("profileName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "profileName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
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
