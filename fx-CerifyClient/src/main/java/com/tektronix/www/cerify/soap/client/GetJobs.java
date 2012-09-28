/**
 * GetJobs.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

public class GetJobs  implements java.io.Serializable {
    /* If populated, then all active Jobs
     *                                     created on or after this date
     * will
     *                                     be returned */
    private java.util.Calendar createTimeRangeFrom;

    /* If populated, then all active Jobs
     *                                     created on or before this date
     * will
     *                                     be returned */
    private java.util.Calendar createTimeRangeTo;

    /* If populated, then all active Jobs
     *                                     having this status will be returned */
    private com.tektronix.www.cerify.soap.client.JobStatusType status;

    public GetJobs() {
    }

    public GetJobs(
           java.util.Calendar createTimeRangeFrom,
           java.util.Calendar createTimeRangeTo,
           com.tektronix.www.cerify.soap.client.JobStatusType status) {
           this.createTimeRangeFrom = createTimeRangeFrom;
           this.createTimeRangeTo = createTimeRangeTo;
           this.status = status;
    }


    /**
     * Gets the createTimeRangeFrom value for this GetJobs.
     * 
     * @return createTimeRangeFrom   * If populated, then all active Jobs
     *                                     created on or after this date
     * will
     *                                     be returned
     */
    public java.util.Calendar getCreateTimeRangeFrom() {
        return createTimeRangeFrom;
    }


    /**
     * Sets the createTimeRangeFrom value for this GetJobs.
     * 
     * @param createTimeRangeFrom   * If populated, then all active Jobs
     *                                     created on or after this date
     * will
     *                                     be returned
     */
    public void setCreateTimeRangeFrom(java.util.Calendar createTimeRangeFrom) {
        this.createTimeRangeFrom = createTimeRangeFrom;
    }


    /**
     * Gets the createTimeRangeTo value for this GetJobs.
     * 
     * @return createTimeRangeTo   * If populated, then all active Jobs
     *                                     created on or before this date
     * will
     *                                     be returned
     */
    public java.util.Calendar getCreateTimeRangeTo() {
        return createTimeRangeTo;
    }


    /**
     * Sets the createTimeRangeTo value for this GetJobs.
     * 
     * @param createTimeRangeTo   * If populated, then all active Jobs
     *                                     created on or before this date
     * will
     *                                     be returned
     */
    public void setCreateTimeRangeTo(java.util.Calendar createTimeRangeTo) {
        this.createTimeRangeTo = createTimeRangeTo;
    }


    /**
     * Gets the status value for this GetJobs.
     * 
     * @return status   * If populated, then all active Jobs
     *                                     having this status will be returned
     */
    public com.tektronix.www.cerify.soap.client.JobStatusType getStatus() {
        return status;
    }


    /**
     * Sets the status value for this GetJobs.
     * 
     * @param status   * If populated, then all active Jobs
     *                                     having this status will be returned
     */
    public void setStatus(com.tektronix.www.cerify.soap.client.JobStatusType status) {
        this.status = status;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetJobs)) return false;
        GetJobs other = (GetJobs) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.createTimeRangeFrom==null && other.getCreateTimeRangeFrom()==null) || 
             (this.createTimeRangeFrom!=null &&
              this.createTimeRangeFrom.equals(other.getCreateTimeRangeFrom()))) &&
            ((this.createTimeRangeTo==null && other.getCreateTimeRangeTo()==null) || 
             (this.createTimeRangeTo!=null &&
              this.createTimeRangeTo.equals(other.getCreateTimeRangeTo()))) &&
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus())));
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
        if (getCreateTimeRangeFrom() != null) {
            _hashCode += getCreateTimeRangeFrom().hashCode();
        }
        if (getCreateTimeRangeTo() != null) {
            _hashCode += getCreateTimeRangeTo().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetJobs.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobs"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("createTimeRangeFrom");
        elemField.setXmlName(new javax.xml.namespace.QName("", "createTimeRangeFrom"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("createTimeRangeTo");
        elemField.setXmlName(new javax.xml.namespace.QName("", "createTimeRangeTo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("", "status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "jobStatusType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
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
