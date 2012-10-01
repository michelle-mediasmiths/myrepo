/**
 * GetTemplatesForJobResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

import javax.annotation.Generated;

@Generated(value = {"axis"})public class GetTemplatesForJobResponse  implements java.io.Serializable {
    private com.tektronix.www.cerify.templateInfo._2008._06._06.TemplatesType templates;

    public GetTemplatesForJobResponse() {
    }

    public GetTemplatesForJobResponse(
           com.tektronix.www.cerify.templateInfo._2008._06._06.TemplatesType templates) {
           this.templates = templates;
    }


    /**
     * Gets the templates value for this GetTemplatesForJobResponse.
     * 
     * @return templates
     */
    public com.tektronix.www.cerify.templateInfo._2008._06._06.TemplatesType getTemplates() {
        return templates;
    }


    /**
     * Sets the templates value for this GetTemplatesForJobResponse.
     * 
     * @param templates
     */
    public void setTemplates(com.tektronix.www.cerify.templateInfo._2008._06._06.TemplatesType templates) {
        this.templates = templates;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetTemplatesForJobResponse)) return false;
        GetTemplatesForJobResponse other = (GetTemplatesForJobResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.templates==null && other.getTemplates()==null) || 
             (this.templates!=null &&
              this.templates.equals(other.getTemplates())));
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
        if (getTemplates() != null) {
            _hashCode += getTemplates().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetTemplatesForJobResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetTemplatesForJobResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("templates");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "templates"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "templatesType"));
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
