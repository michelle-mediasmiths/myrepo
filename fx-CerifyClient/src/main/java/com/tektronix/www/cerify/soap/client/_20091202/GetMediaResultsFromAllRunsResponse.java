/**
 * GetMediaResultsFromAllRunsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client._20091202;

public class GetMediaResultsFromAllRunsResponse  implements java.io.Serializable {
    private com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponseJobmediafile[] jobmediafile;

    public GetMediaResultsFromAllRunsResponse() {
    }

    public GetMediaResultsFromAllRunsResponse(
           com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponseJobmediafile[] jobmediafile) {
           this.jobmediafile = jobmediafile;
    }


    /**
     * Gets the jobmediafile value for this GetMediaResultsFromAllRunsResponse.
     * 
     * @return jobmediafile
     */
    public com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponseJobmediafile[] getJobmediafile() {
        return jobmediafile;
    }


    /**
     * Sets the jobmediafile value for this GetMediaResultsFromAllRunsResponse.
     * 
     * @param jobmediafile
     */
    public void setJobmediafile(com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponseJobmediafile[] jobmediafile) {
        this.jobmediafile = jobmediafile;
    }

    public com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponseJobmediafile getJobmediafile(int i) {
        return this.jobmediafile[i];
    }

    public void setJobmediafile(int i, com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponseJobmediafile _value) {
        this.jobmediafile[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetMediaResultsFromAllRunsResponse)) return false;
        GetMediaResultsFromAllRunsResponse other = (GetMediaResultsFromAllRunsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.jobmediafile==null && other.getJobmediafile()==null) || 
             (this.jobmediafile!=null &&
              java.util.Arrays.equals(this.jobmediafile, other.getJobmediafile())));
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
        if (getJobmediafile() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getJobmediafile());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getJobmediafile(), i);
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
        new org.apache.axis.description.TypeDesc(GetMediaResultsFromAllRunsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">GetMediaResultsFromAllRunsResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobmediafile");
        elemField.setXmlName(new javax.xml.namespace.QName("", "jobmediafile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">>GetMediaResultsFromAllRunsResponse>jobmediafile"));
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
