/**
 * GetJobStatusResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

public class GetJobStatusResponse  implements java.io.Serializable {
    private java.lang.String name;  // attribute

    private com.tektronix.www.cerify.soap.client.PriorityType priority;  // attribute

    private java.math.BigInteger progress;  // attribute

    private com.tektronix.www.cerify.soap.client.ResultType result;  // attribute

    private java.util.Calendar started;  // attribute

    private com.tektronix.www.cerify.soap.client.JobStatusType status;  // attribute

    private java.math.BigInteger alertcount;  // attribute

    private org.apache.axis.types.URI url;  // attribute

    private boolean archived;  // attribute

    public GetJobStatusResponse() {
    }

    public GetJobStatusResponse(
           java.lang.String name,
           com.tektronix.www.cerify.soap.client.PriorityType priority,
           java.math.BigInteger progress,
           com.tektronix.www.cerify.soap.client.ResultType result,
           java.util.Calendar started,
           com.tektronix.www.cerify.soap.client.JobStatusType status,
           java.math.BigInteger alertcount,
           org.apache.axis.types.URI url,
           boolean archived) {
           this.name = name;
           this.priority = priority;
           this.progress = progress;
           this.result = result;
           this.started = started;
           this.status = status;
           this.alertcount = alertcount;
           this.url = url;
           this.archived = archived;
    }


    /**
     * Gets the name value for this GetJobStatusResponse.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this GetJobStatusResponse.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the priority value for this GetJobStatusResponse.
     * 
     * @return priority
     */
    public com.tektronix.www.cerify.soap.client.PriorityType getPriority() {
        return priority;
    }


    /**
     * Sets the priority value for this GetJobStatusResponse.
     * 
     * @param priority
     */
    public void setPriority(com.tektronix.www.cerify.soap.client.PriorityType priority) {
        this.priority = priority;
    }


    /**
     * Gets the progress value for this GetJobStatusResponse.
     * 
     * @return progress
     */
    public java.math.BigInteger getProgress() {
        return progress;
    }


    /**
     * Sets the progress value for this GetJobStatusResponse.
     * 
     * @param progress
     */
    public void setProgress(java.math.BigInteger progress) {
        this.progress = progress;
    }


    /**
     * Gets the result value for this GetJobStatusResponse.
     * 
     * @return result
     */
    public com.tektronix.www.cerify.soap.client.ResultType getResult() {
        return result;
    }


    /**
     * Sets the result value for this GetJobStatusResponse.
     * 
     * @param result
     */
    public void setResult(com.tektronix.www.cerify.soap.client.ResultType result) {
        this.result = result;
    }


    /**
     * Gets the started value for this GetJobStatusResponse.
     * 
     * @return started
     */
    public java.util.Calendar getStarted() {
        return started;
    }


    /**
     * Sets the started value for this GetJobStatusResponse.
     * 
     * @param started
     */
    public void setStarted(java.util.Calendar started) {
        this.started = started;
    }


    /**
     * Gets the status value for this GetJobStatusResponse.
     * 
     * @return status
     */
    public com.tektronix.www.cerify.soap.client.JobStatusType getStatus() {
        return status;
    }


    /**
     * Sets the status value for this GetJobStatusResponse.
     * 
     * @param status
     */
    public void setStatus(com.tektronix.www.cerify.soap.client.JobStatusType status) {
        this.status = status;
    }


    /**
     * Gets the alertcount value for this GetJobStatusResponse.
     * 
     * @return alertcount
     */
    public java.math.BigInteger getAlertcount() {
        return alertcount;
    }


    /**
     * Sets the alertcount value for this GetJobStatusResponse.
     * 
     * @param alertcount
     */
    public void setAlertcount(java.math.BigInteger alertcount) {
        this.alertcount = alertcount;
    }


    /**
     * Gets the url value for this GetJobStatusResponse.
     * 
     * @return url
     */
    public org.apache.axis.types.URI getUrl() {
        return url;
    }


    /**
     * Sets the url value for this GetJobStatusResponse.
     * 
     * @param url
     */
    public void setUrl(org.apache.axis.types.URI url) {
        this.url = url;
    }


    /**
     * Gets the archived value for this GetJobStatusResponse.
     * 
     * @return archived
     */
    public boolean isArchived() {
        return archived;
    }


    /**
     * Sets the archived value for this GetJobStatusResponse.
     * 
     * @param archived
     */
    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetJobStatusResponse)) return false;
        GetJobStatusResponse other = (GetJobStatusResponse) obj;
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
            ((this.priority==null && other.getPriority()==null) || 
             (this.priority!=null &&
              this.priority.equals(other.getPriority()))) &&
            ((this.progress==null && other.getProgress()==null) || 
             (this.progress!=null &&
              this.progress.equals(other.getProgress()))) &&
            ((this.result==null && other.getResult()==null) || 
             (this.result!=null &&
              this.result.equals(other.getResult()))) &&
            ((this.started==null && other.getStarted()==null) || 
             (this.started!=null &&
              this.started.equals(other.getStarted()))) &&
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.alertcount==null && other.getAlertcount()==null) || 
             (this.alertcount!=null &&
              this.alertcount.equals(other.getAlertcount()))) &&
            ((this.url==null && other.getUrl()==null) || 
             (this.url!=null &&
              this.url.equals(other.getUrl()))) &&
            this.archived == other.isArchived();
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
        if (getPriority() != null) {
            _hashCode += getPriority().hashCode();
        }
        if (getProgress() != null) {
            _hashCode += getProgress().hashCode();
        }
        if (getResult() != null) {
            _hashCode += getResult().hashCode();
        }
        if (getStarted() != null) {
            _hashCode += getStarted().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getAlertcount() != null) {
            _hashCode += getAlertcount().hashCode();
        }
        if (getUrl() != null) {
            _hashCode += getUrl().hashCode();
        }
        _hashCode += (isArchived() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetJobStatusResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobStatusResponse"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("name");
        attrField.setXmlName(new javax.xml.namespace.QName("", "name"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("priority");
        attrField.setXmlName(new javax.xml.namespace.QName("", "priority"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "priorityType"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("progress");
        attrField.setXmlName(new javax.xml.namespace.QName("", "progress"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "progress"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("result");
        attrField.setXmlName(new javax.xml.namespace.QName("", "result"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "resultType"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("started");
        attrField.setXmlName(new javax.xml.namespace.QName("", "started"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("status");
        attrField.setXmlName(new javax.xml.namespace.QName("", "status"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "jobStatusType"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("alertcount");
        attrField.setXmlName(new javax.xml.namespace.QName("", "alertcount"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "integer"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("url");
        attrField.setXmlName(new javax.xml.namespace.QName("", "url"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "cerifyUrl"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("archived");
        attrField.setXmlName(new javax.xml.namespace.QName("", "archived"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
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
