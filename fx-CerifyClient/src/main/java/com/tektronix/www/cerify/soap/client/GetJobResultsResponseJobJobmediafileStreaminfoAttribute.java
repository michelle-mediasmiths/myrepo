/**
 * GetJobResultsResponseJobJobmediafileStreaminfoAttribute.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

import javax.annotation.Generated;

@Generated(value = {"axis"})public class GetJobResultsResponseJobJobmediafileStreaminfoAttribute  implements java.io.Serializable {
    private java.lang.String name;  // attribute

    private java.lang.String value;  // attribute

    public GetJobResultsResponseJobJobmediafileStreaminfoAttribute() {
    }

    public GetJobResultsResponseJobJobmediafileStreaminfoAttribute(
           java.lang.String name,
           java.lang.String value) {
           this.name = name;
           this.value = value;
    }


    /**
     * Gets the name value for this GetJobResultsResponseJobJobmediafileStreaminfoAttribute.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this GetJobResultsResponseJobJobmediafileStreaminfoAttribute.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the value value for this GetJobResultsResponseJobJobmediafileStreaminfoAttribute.
     * 
     * @return value
     */
    public java.lang.String getValue() {
        return value;
    }


    /**
     * Sets the value value for this GetJobResultsResponseJobJobmediafileStreaminfoAttribute.
     * 
     * @param value
     */
    public void setValue(java.lang.String value) {
        this.value = value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetJobResultsResponseJobJobmediafileStreaminfoAttribute)) return false;
        GetJobResultsResponseJobJobmediafileStreaminfoAttribute other = (GetJobResultsResponseJobJobmediafileStreaminfoAttribute) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.value==null && other.getValue()==null) || 
             (this.value!=null &&
              this.value.equals(other.getValue())));
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getValue() != null) {
            _hashCode += getValue().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetJobResultsResponseJobJobmediafileStreaminfoAttribute.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>>>>GetJobResultsResponse>job>jobmediafile>streaminfo>attribute"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("name");
        attrField.setXmlName(new javax.xml.namespace.QName("", "name"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("value");
        attrField.setXmlName(new javax.xml.namespace.QName("", "value"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
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
