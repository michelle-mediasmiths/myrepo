/**
 * GetJobResultsResponseJobJobmediafileStreaminfo.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

public class GetJobResultsResponseJobJobmediafileStreaminfo  implements java.io.Serializable {
    /* An attribute of a stream within the media file, espressed as
     * a name/value pair */
    private com.tektronix.www.cerify.soap.client.GetJobResultsResponseJobJobmediafileStreaminfoAttribute[] attribute;

    public GetJobResultsResponseJobJobmediafileStreaminfo() {
    }

    public GetJobResultsResponseJobJobmediafileStreaminfo(
           com.tektronix.www.cerify.soap.client.GetJobResultsResponseJobJobmediafileStreaminfoAttribute[] attribute) {
           this.attribute = attribute;
    }


    /**
     * Gets the attribute value for this GetJobResultsResponseJobJobmediafileStreaminfo.
     * 
     * @return attribute   * An attribute of a stream within the media file, espressed as
     * a name/value pair
     */
    public com.tektronix.www.cerify.soap.client.GetJobResultsResponseJobJobmediafileStreaminfoAttribute[] getAttribute() {
        return attribute;
    }


    /**
     * Sets the attribute value for this GetJobResultsResponseJobJobmediafileStreaminfo.
     * 
     * @param attribute   * An attribute of a stream within the media file, espressed as
     * a name/value pair
     */
    public void setAttribute(com.tektronix.www.cerify.soap.client.GetJobResultsResponseJobJobmediafileStreaminfoAttribute[] attribute) {
        this.attribute = attribute;
    }

    public com.tektronix.www.cerify.soap.client.GetJobResultsResponseJobJobmediafileStreaminfoAttribute getAttribute(int i) {
        return this.attribute[i];
    }

    public void setAttribute(int i, com.tektronix.www.cerify.soap.client.GetJobResultsResponseJobJobmediafileStreaminfoAttribute _value) {
        this.attribute[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetJobResultsResponseJobJobmediafileStreaminfo)) return false;
        GetJobResultsResponseJobJobmediafileStreaminfo other = (GetJobResultsResponseJobJobmediafileStreaminfo) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.attribute==null && other.getAttribute()==null) || 
             (this.attribute!=null &&
              java.util.Arrays.equals(this.attribute, other.getAttribute())));
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
        if (getAttribute() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAttribute());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAttribute(), i);
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
        new org.apache.axis.description.TypeDesc(GetJobResultsResponseJobJobmediafileStreaminfo.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>>>GetJobResultsResponse>job>jobmediafile>streaminfo"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attribute");
        elemField.setXmlName(new javax.xml.namespace.QName("", "attribute"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>>>>GetJobResultsResponse>job>jobmediafile>streaminfo>attribute"));
        elemField.setNillable(false);
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
