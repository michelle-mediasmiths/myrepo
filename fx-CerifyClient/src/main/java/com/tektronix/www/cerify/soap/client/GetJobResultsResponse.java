/**
 * GetJobResultsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

public class GetJobResultsResponse  implements java.io.Serializable {
    private com.tektronix.www.cerify.soap.client.GetJobResultsResponseJob job;

    private java.util.Calendar completed;  // attribute

    private java.lang.String mediaset;  // attribute

    private java.lang.String name;  // attribute

    private com.tektronix.www.cerify.soap.client.PriorityType priority;  // attribute

    private java.lang.String profile;  // attribute

    private com.tektronix.www.cerify.soap.client.ResultType result;  // attribute

    private java.util.Calendar started;  // attribute

    private org.apache.axis.types.URI url;  // attribute

    public GetJobResultsResponse() {
    }

    public GetJobResultsResponse(
           com.tektronix.www.cerify.soap.client.GetJobResultsResponseJob job,
           java.util.Calendar completed,
           java.lang.String mediaset,
           java.lang.String name,
           com.tektronix.www.cerify.soap.client.PriorityType priority,
           java.lang.String profile,
           com.tektronix.www.cerify.soap.client.ResultType result,
           java.util.Calendar started,
           org.apache.axis.types.URI url) {
           this.job = job;
           this.completed = completed;
           this.mediaset = mediaset;
           this.name = name;
           this.priority = priority;
           this.profile = profile;
           this.result = result;
           this.started = started;
           this.url = url;
    }


    /**
     * Gets the job value for this GetJobResultsResponse.
     * 
     * @return job
     */
    public com.tektronix.www.cerify.soap.client.GetJobResultsResponseJob getJob() {
        return job;
    }


    /**
     * Sets the job value for this GetJobResultsResponse.
     * 
     * @param job
     */
    public void setJob(com.tektronix.www.cerify.soap.client.GetJobResultsResponseJob job) {
        this.job = job;
    }


    /**
     * Gets the completed value for this GetJobResultsResponse.
     * 
     * @return completed
     */
    public java.util.Calendar getCompleted() {
        return completed;
    }


    /**
     * Sets the completed value for this GetJobResultsResponse.
     * 
     * @param completed
     */
    public void setCompleted(java.util.Calendar completed) {
        this.completed = completed;
    }


    /**
     * Gets the mediaset value for this GetJobResultsResponse.
     * 
     * @return mediaset
     */
    public java.lang.String getMediaset() {
        return mediaset;
    }


    /**
     * Sets the mediaset value for this GetJobResultsResponse.
     * 
     * @param mediaset
     */
    public void setMediaset(java.lang.String mediaset) {
        this.mediaset = mediaset;
    }


    /**
     * Gets the name value for this GetJobResultsResponse.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this GetJobResultsResponse.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the priority value for this GetJobResultsResponse.
     * 
     * @return priority
     */
    public com.tektronix.www.cerify.soap.client.PriorityType getPriority() {
        return priority;
    }


    /**
     * Sets the priority value for this GetJobResultsResponse.
     * 
     * @param priority
     */
    public void setPriority(com.tektronix.www.cerify.soap.client.PriorityType priority) {
        this.priority = priority;
    }


    /**
     * Gets the profile value for this GetJobResultsResponse.
     * 
     * @return profile
     */
    public java.lang.String getProfile() {
        return profile;
    }


    /**
     * Sets the profile value for this GetJobResultsResponse.
     * 
     * @param profile
     */
    public void setProfile(java.lang.String profile) {
        this.profile = profile;
    }


    /**
     * Gets the result value for this GetJobResultsResponse.
     * 
     * @return result
     */
    public com.tektronix.www.cerify.soap.client.ResultType getResult() {
        return result;
    }


    /**
     * Sets the result value for this GetJobResultsResponse.
     * 
     * @param result
     */
    public void setResult(com.tektronix.www.cerify.soap.client.ResultType result) {
        this.result = result;
    }


    /**
     * Gets the started value for this GetJobResultsResponse.
     * 
     * @return started
     */
    public java.util.Calendar getStarted() {
        return started;
    }


    /**
     * Sets the started value for this GetJobResultsResponse.
     * 
     * @param started
     */
    public void setStarted(java.util.Calendar started) {
        this.started = started;
    }


    /**
     * Gets the url value for this GetJobResultsResponse.
     * 
     * @return url
     */
    public org.apache.axis.types.URI getUrl() {
        return url;
    }


    /**
     * Sets the url value for this GetJobResultsResponse.
     * 
     * @param url
     */
    public void setUrl(org.apache.axis.types.URI url) {
        this.url = url;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetJobResultsResponse)) return false;
        GetJobResultsResponse other = (GetJobResultsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.job==null && other.getJob()==null) || 
             (this.job!=null &&
              this.job.equals(other.getJob()))) &&
            ((this.completed==null && other.getCompleted()==null) || 
             (this.completed!=null &&
              this.completed.equals(other.getCompleted()))) &&
            ((this.mediaset==null && other.getMediaset()==null) || 
             (this.mediaset!=null &&
              this.mediaset.equals(other.getMediaset()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.priority==null && other.getPriority()==null) || 
             (this.priority!=null &&
              this.priority.equals(other.getPriority()))) &&
            ((this.profile==null && other.getProfile()==null) || 
             (this.profile!=null &&
              this.profile.equals(other.getProfile()))) &&
            ((this.result==null && other.getResult()==null) || 
             (this.result!=null &&
              this.result.equals(other.getResult()))) &&
            ((this.started==null && other.getStarted()==null) || 
             (this.started!=null &&
              this.started.equals(other.getStarted()))) &&
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
        if (getJob() != null) {
            _hashCode += getJob().hashCode();
        }
        if (getCompleted() != null) {
            _hashCode += getCompleted().hashCode();
        }
        if (getMediaset() != null) {
            _hashCode += getMediaset().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getPriority() != null) {
            _hashCode += getPriority().hashCode();
        }
        if (getProfile() != null) {
            _hashCode += getProfile().hashCode();
        }
        if (getResult() != null) {
            _hashCode += getResult().hashCode();
        }
        if (getStarted() != null) {
            _hashCode += getStarted().hashCode();
        }
        if (getUrl() != null) {
            _hashCode += getUrl().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetJobResultsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobResultsResponse"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("completed");
        attrField.setXmlName(new javax.xml.namespace.QName("", "completed"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("mediaset");
        attrField.setXmlName(new javax.xml.namespace.QName("", "mediaset"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
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
        attrField.setFieldName("profile");
        attrField.setXmlName(new javax.xml.namespace.QName("", "profile"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
        attrField.setFieldName("url");
        attrField.setXmlName(new javax.xml.namespace.QName("", "url"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "cerifyUrl"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("job");
        elemField.setXmlName(new javax.xml.namespace.QName("", "job"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>GetJobResultsResponse>job"));
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
