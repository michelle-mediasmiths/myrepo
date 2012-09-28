/**
 * GetMediaFileStatusResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

public class GetMediaFileStatusResponse  implements java.io.Serializable {
    private java.lang.String name;  // attribute

    private java.lang.String path;  // attribute

    private java.math.BigInteger progress;  // attribute

    private com.tektronix.www.cerify.soap.client.ResultType result;  // attribute

    private java.math.BigInteger size;  // attribute

    private java.util.Calendar started;  // attribute

    private com.tektronix.www.cerify.soap.client.FileStatusType status;  // attribute

    private java.math.BigInteger alertcount;  // attribute

    private org.apache.axis.types.URI url;  // attribute

    public GetMediaFileStatusResponse() {
    }

    public GetMediaFileStatusResponse(
           java.lang.String name,
           java.lang.String path,
           java.math.BigInteger progress,
           com.tektronix.www.cerify.soap.client.ResultType result,
           java.math.BigInteger size,
           java.util.Calendar started,
           com.tektronix.www.cerify.soap.client.FileStatusType status,
           java.math.BigInteger alertcount,
           org.apache.axis.types.URI url) {
           this.name = name;
           this.path = path;
           this.progress = progress;
           this.result = result;
           this.size = size;
           this.started = started;
           this.status = status;
           this.alertcount = alertcount;
           this.url = url;
    }


    /**
     * Gets the name value for this GetMediaFileStatusResponse.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this GetMediaFileStatusResponse.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the path value for this GetMediaFileStatusResponse.
     * 
     * @return path
     */
    public java.lang.String getPath() {
        return path;
    }


    /**
     * Sets the path value for this GetMediaFileStatusResponse.
     * 
     * @param path
     */
    public void setPath(java.lang.String path) {
        this.path = path;
    }


    /**
     * Gets the progress value for this GetMediaFileStatusResponse.
     * 
     * @return progress
     */
    public java.math.BigInteger getProgress() {
        return progress;
    }


    /**
     * Sets the progress value for this GetMediaFileStatusResponse.
     * 
     * @param progress
     */
    public void setProgress(java.math.BigInteger progress) {
        this.progress = progress;
    }


    /**
     * Gets the result value for this GetMediaFileStatusResponse.
     * 
     * @return result
     */
    public com.tektronix.www.cerify.soap.client.ResultType getResult() {
        return result;
    }


    /**
     * Sets the result value for this GetMediaFileStatusResponse.
     * 
     * @param result
     */
    public void setResult(com.tektronix.www.cerify.soap.client.ResultType result) {
        this.result = result;
    }


    /**
     * Gets the size value for this GetMediaFileStatusResponse.
     * 
     * @return size
     */
    public java.math.BigInteger getSize() {
        return size;
    }


    /**
     * Sets the size value for this GetMediaFileStatusResponse.
     * 
     * @param size
     */
    public void setSize(java.math.BigInteger size) {
        this.size = size;
    }


    /**
     * Gets the started value for this GetMediaFileStatusResponse.
     * 
     * @return started
     */
    public java.util.Calendar getStarted() {
        return started;
    }


    /**
     * Sets the started value for this GetMediaFileStatusResponse.
     * 
     * @param started
     */
    public void setStarted(java.util.Calendar started) {
        this.started = started;
    }


    /**
     * Gets the status value for this GetMediaFileStatusResponse.
     * 
     * @return status
     */
    public com.tektronix.www.cerify.soap.client.FileStatusType getStatus() {
        return status;
    }


    /**
     * Sets the status value for this GetMediaFileStatusResponse.
     * 
     * @param status
     */
    public void setStatus(com.tektronix.www.cerify.soap.client.FileStatusType status) {
        this.status = status;
    }


    /**
     * Gets the alertcount value for this GetMediaFileStatusResponse.
     * 
     * @return alertcount
     */
    public java.math.BigInteger getAlertcount() {
        return alertcount;
    }


    /**
     * Sets the alertcount value for this GetMediaFileStatusResponse.
     * 
     * @param alertcount
     */
    public void setAlertcount(java.math.BigInteger alertcount) {
        this.alertcount = alertcount;
    }


    /**
     * Gets the url value for this GetMediaFileStatusResponse.
     * 
     * @return url
     */
    public org.apache.axis.types.URI getUrl() {
        return url;
    }


    /**
     * Sets the url value for this GetMediaFileStatusResponse.
     * 
     * @param url
     */
    public void setUrl(org.apache.axis.types.URI url) {
        this.url = url;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetMediaFileStatusResponse)) return false;
        GetMediaFileStatusResponse other = (GetMediaFileStatusResponse) obj;
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
            ((this.path==null && other.getPath()==null) || 
             (this.path!=null &&
              this.path.equals(other.getPath()))) &&
            ((this.progress==null && other.getProgress()==null) || 
             (this.progress!=null &&
              this.progress.equals(other.getProgress()))) &&
            ((this.result==null && other.getResult()==null) || 
             (this.result!=null &&
              this.result.equals(other.getResult()))) &&
            ((this.size==null && other.getSize()==null) || 
             (this.size!=null &&
              this.size.equals(other.getSize()))) &&
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getPath() != null) {
            _hashCode += getPath().hashCode();
        }
        if (getProgress() != null) {
            _hashCode += getProgress().hashCode();
        }
        if (getResult() != null) {
            _hashCode += getResult().hashCode();
        }
        if (getSize() != null) {
            _hashCode += getSize().hashCode();
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
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetMediaFileStatusResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaFileStatusResponse"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("name");
        attrField.setXmlName(new javax.xml.namespace.QName("", "name"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("path");
        attrField.setXmlName(new javax.xml.namespace.QName("", "path"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
        attrField.setFieldName("size");
        attrField.setXmlName(new javax.xml.namespace.QName("", "size"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "integer"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("started");
        attrField.setXmlName(new javax.xml.namespace.QName("", "started"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("status");
        attrField.setXmlName(new javax.xml.namespace.QName("", "status"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "fileStatusType"));
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
