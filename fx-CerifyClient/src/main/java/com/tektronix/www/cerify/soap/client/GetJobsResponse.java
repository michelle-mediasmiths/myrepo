/**
 * GetJobsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

public class GetJobsResponse  implements java.io.Serializable {
    /* A list of active (non-archived) Job
     *                                     names meeting the criteria of
     * the
     *                                     request */
    private java.lang.String[] listOfJobs;

    public GetJobsResponse() {
    }

    public GetJobsResponse(
           java.lang.String[] listOfJobs) {
           this.listOfJobs = listOfJobs;
    }


    /**
     * Gets the listOfJobs value for this GetJobsResponse.
     * 
     * @return listOfJobs   * A list of active (non-archived) Job
     *                                     names meeting the criteria of
     * the
     *                                     request
     */
    public java.lang.String[] getListOfJobs() {
        return listOfJobs;
    }


    /**
     * Sets the listOfJobs value for this GetJobsResponse.
     * 
     * @param listOfJobs   * A list of active (non-archived) Job
     *                                     names meeting the criteria of
     * the
     *                                     request
     */
    public void setListOfJobs(java.lang.String[] listOfJobs) {
        this.listOfJobs = listOfJobs;
    }

    public java.lang.String getListOfJobs(int i) {
        return this.listOfJobs[i];
    }

    public void setListOfJobs(int i, java.lang.String _value) {
        this.listOfJobs[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetJobsResponse)) return false;
        GetJobsResponse other = (GetJobsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.listOfJobs==null && other.getListOfJobs()==null) || 
             (this.listOfJobs!=null &&
              java.util.Arrays.equals(this.listOfJobs, other.getListOfJobs())));
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
        if (getListOfJobs() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getListOfJobs());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getListOfJobs(), i);
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
        new org.apache.axis.description.TypeDesc(GetJobsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobsResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("listOfJobs");
        elemField.setXmlName(new javax.xml.namespace.QName("", "listOfJobs"));
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
