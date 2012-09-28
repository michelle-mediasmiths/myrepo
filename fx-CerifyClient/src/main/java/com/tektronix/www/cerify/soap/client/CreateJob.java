/**
 * CreateJob.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

public class CreateJob  implements java.io.Serializable {
    /* The name of the Job to be created */
    private java.lang.String jobName;

    /* The name of the MediaSet to be
     *                                     associated with the Job */
    private java.lang.String mediaSetName;

    /* The name of the Profile to be
     *                                     associated with the Job */
    private java.lang.String profileName;

    /* The Job's priority */
    private com.tektronix.www.cerify.soap.client.PriorityType priority;

    public CreateJob() {
    }

    public CreateJob(
           java.lang.String jobName,
           java.lang.String mediaSetName,
           java.lang.String profileName,
           com.tektronix.www.cerify.soap.client.PriorityType priority) {
           this.jobName = jobName;
           this.mediaSetName = mediaSetName;
           this.profileName = profileName;
           this.priority = priority;
    }


    /**
     * Gets the jobName value for this CreateJob.
     * 
     * @return jobName   * The name of the Job to be created
     */
    public java.lang.String getJobName() {
        return jobName;
    }


    /**
     * Sets the jobName value for this CreateJob.
     * 
     * @param jobName   * The name of the Job to be created
     */
    public void setJobName(java.lang.String jobName) {
        this.jobName = jobName;
    }


    /**
     * Gets the mediaSetName value for this CreateJob.
     * 
     * @return mediaSetName   * The name of the MediaSet to be
     *                                     associated with the Job
     */
    public java.lang.String getMediaSetName() {
        return mediaSetName;
    }


    /**
     * Sets the mediaSetName value for this CreateJob.
     * 
     * @param mediaSetName   * The name of the MediaSet to be
     *                                     associated with the Job
     */
    public void setMediaSetName(java.lang.String mediaSetName) {
        this.mediaSetName = mediaSetName;
    }


    /**
     * Gets the profileName value for this CreateJob.
     * 
     * @return profileName   * The name of the Profile to be
     *                                     associated with the Job
     */
    public java.lang.String getProfileName() {
        return profileName;
    }


    /**
     * Sets the profileName value for this CreateJob.
     * 
     * @param profileName   * The name of the Profile to be
     *                                     associated with the Job
     */
    public void setProfileName(java.lang.String profileName) {
        this.profileName = profileName;
    }


    /**
     * Gets the priority value for this CreateJob.
     * 
     * @return priority   * The Job's priority
     */
    public com.tektronix.www.cerify.soap.client.PriorityType getPriority() {
        return priority;
    }


    /**
     * Sets the priority value for this CreateJob.
     * 
     * @param priority   * The Job's priority
     */
    public void setPriority(com.tektronix.www.cerify.soap.client.PriorityType priority) {
        this.priority = priority;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CreateJob)) return false;
        CreateJob other = (CreateJob) obj;
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
            ((this.mediaSetName==null && other.getMediaSetName()==null) || 
             (this.mediaSetName!=null &&
              this.mediaSetName.equals(other.getMediaSetName()))) &&
            ((this.profileName==null && other.getProfileName()==null) || 
             (this.profileName!=null &&
              this.profileName.equals(other.getProfileName()))) &&
            ((this.priority==null && other.getPriority()==null) || 
             (this.priority!=null &&
              this.priority.equals(other.getPriority())));
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
        if (getMediaSetName() != null) {
            _hashCode += getMediaSetName().hashCode();
        }
        if (getProfileName() != null) {
            _hashCode += getProfileName().hashCode();
        }
        if (getPriority() != null) {
            _hashCode += getPriority().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CreateJob.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">CreateJob"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "jobName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mediaSetName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "mediaSetName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("profileName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "profileName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("priority");
        elemField.setXmlName(new javax.xml.namespace.QName("", "priority"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "priorityType"));
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
