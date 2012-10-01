/**
 * GetMediaResultsFromAllRuns.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client._20100523;

import javax.annotation.Generated;

@Generated(value = {"axis"})public class GetMediaResultsFromAllRuns  implements java.io.Serializable {
    /* The name of the Job the media file
     * 	                                    is associated with */
    private java.lang.String jobName;

    /* Location (URL) of the media file */
    private org.apache.axis.types.URI url;

    public GetMediaResultsFromAllRuns() {
    }

    public GetMediaResultsFromAllRuns(
           java.lang.String jobName,
           org.apache.axis.types.URI url) {
           this.jobName = jobName;
           this.url = url;
    }


    /**
     * Gets the jobName value for this GetMediaResultsFromAllRuns.
     * 
     * @return jobName   * The name of the Job the media file
     * 	                                    is associated with
     */
    public java.lang.String getJobName() {
        return jobName;
    }


    /**
     * Sets the jobName value for this GetMediaResultsFromAllRuns.
     * 
     * @param jobName   * The name of the Job the media file
     * 	                                    is associated with
     */
    public void setJobName(java.lang.String jobName) {
        this.jobName = jobName;
    }


    /**
     * Gets the url value for this GetMediaResultsFromAllRuns.
     * 
     * @return url   * Location (URL) of the media file
     */
    public org.apache.axis.types.URI getUrl() {
        return url;
    }


    /**
     * Sets the url value for this GetMediaResultsFromAllRuns.
     * 
     * @param url   * Location (URL) of the media file
     */
    public void setUrl(org.apache.axis.types.URI url) {
        this.url = url;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetMediaResultsFromAllRuns)) return false;
        GetMediaResultsFromAllRuns other = (GetMediaResultsFromAllRuns) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.jobName==null && other.getJobName()==null) || 
             (this.jobName!=null &&
              this.jobName.equals(other.getJobName()))) &&
            ((this.url==null && other.getUrl()==null) || 
             (this.url!=null &&
              this.url.equals(other.getUrl())));
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
        if (getJobName() != null) {
            _hashCode += getJobName().hashCode();
        }
        if (getUrl() != null) {
            _hashCode += getUrl().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetMediaResultsFromAllRuns.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/05/23/reports/wsdl", ">GetMediaResultsFromAllRuns"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "jobName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("url");
        elemField.setXmlName(new javax.xml.namespace.QName("", "url"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
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
