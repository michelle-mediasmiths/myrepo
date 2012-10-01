/**
 * GetMediaFileResultsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

import javax.annotation.Generated;

@Generated(value = {"axis"})public class GetMediaFileResultsResponse  implements java.io.Serializable {
    private com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponseStreaminfo streaminfo;

    private com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponseAlert[] alert;

    private java.lang.String name;  // attribute

    private java.lang.String path;  // attribute

    private java.math.BigInteger progress;  // attribute

    private com.tektronix.www.cerify.soap.client.ResultType result;  // attribute

    private java.math.BigInteger size;  // attribute

    private java.util.Calendar lastModified;  // attribute

    private java.lang.Long runNumber;  // attribute

    private java.lang.String owner;  // attribute

    private java.util.Calendar started;  // attribute

    private com.tektronix.www.cerify.soap.client.FileStatusType status;  // attribute

    private org.apache.axis.types.URI url;  // attribute

    private org.apache.axis.types.URI posterframe;  // attribute

    public GetMediaFileResultsResponse() {
    }

    public GetMediaFileResultsResponse(
           com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponseStreaminfo streaminfo,
           com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponseAlert[] alert,
           java.lang.String name,
           java.lang.String path,
           java.math.BigInteger progress,
           com.tektronix.www.cerify.soap.client.ResultType result,
           java.math.BigInteger size,
           java.util.Calendar lastModified,
           java.lang.Long runNumber,
           java.lang.String owner,
           java.util.Calendar started,
           com.tektronix.www.cerify.soap.client.FileStatusType status,
           org.apache.axis.types.URI url,
           org.apache.axis.types.URI posterframe) {
           this.streaminfo = streaminfo;
           this.alert = alert;
           this.name = name;
           this.path = path;
           this.progress = progress;
           this.result = result;
           this.size = size;
           this.lastModified = lastModified;
           this.runNumber = runNumber;
           this.owner = owner;
           this.started = started;
           this.status = status;
           this.url = url;
           this.posterframe = posterframe;
    }


    /**
     * Gets the streaminfo value for this GetMediaFileResultsResponse.
     * 
     * @return streaminfo
     */
    public com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponseStreaminfo getStreaminfo() {
        return streaminfo;
    }


    /**
     * Sets the streaminfo value for this GetMediaFileResultsResponse.
     * 
     * @param streaminfo
     */
    public void setStreaminfo(com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponseStreaminfo streaminfo) {
        this.streaminfo = streaminfo;
    }


    /**
     * Gets the alert value for this GetMediaFileResultsResponse.
     * 
     * @return alert
     */
    public com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponseAlert[] getAlert() {
        return alert;
    }


    /**
     * Sets the alert value for this GetMediaFileResultsResponse.
     * 
     * @param alert
     */
    public void setAlert(com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponseAlert[] alert) {
        this.alert = alert;
    }

    public com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponseAlert getAlert(int i) {
        return this.alert[i];
    }

    public void setAlert(int i, com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponseAlert _value) {
        this.alert[i] = _value;
    }


    /**
     * Gets the name value for this GetMediaFileResultsResponse.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this GetMediaFileResultsResponse.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the path value for this GetMediaFileResultsResponse.
     * 
     * @return path
     */
    public java.lang.String getPath() {
        return path;
    }


    /**
     * Sets the path value for this GetMediaFileResultsResponse.
     * 
     * @param path
     */
    public void setPath(java.lang.String path) {
        this.path = path;
    }


    /**
     * Gets the progress value for this GetMediaFileResultsResponse.
     * 
     * @return progress
     */
    public java.math.BigInteger getProgress() {
        return progress;
    }


    /**
     * Sets the progress value for this GetMediaFileResultsResponse.
     * 
     * @param progress
     */
    public void setProgress(java.math.BigInteger progress) {
        this.progress = progress;
    }


    /**
     * Gets the result value for this GetMediaFileResultsResponse.
     * 
     * @return result
     */
    public com.tektronix.www.cerify.soap.client.ResultType getResult() {
        return result;
    }


    /**
     * Sets the result value for this GetMediaFileResultsResponse.
     * 
     * @param result
     */
    public void setResult(com.tektronix.www.cerify.soap.client.ResultType result) {
        this.result = result;
    }


    /**
     * Gets the size value for this GetMediaFileResultsResponse.
     * 
     * @return size
     */
    public java.math.BigInteger getSize() {
        return size;
    }


    /**
     * Sets the size value for this GetMediaFileResultsResponse.
     * 
     * @param size
     */
    public void setSize(java.math.BigInteger size) {
        this.size = size;
    }


    /**
     * Gets the lastModified value for this GetMediaFileResultsResponse.
     * 
     * @return lastModified
     */
    public java.util.Calendar getLastModified() {
        return lastModified;
    }


    /**
     * Sets the lastModified value for this GetMediaFileResultsResponse.
     * 
     * @param lastModified
     */
    public void setLastModified(java.util.Calendar lastModified) {
        this.lastModified = lastModified;
    }


    /**
     * Gets the runNumber value for this GetMediaFileResultsResponse.
     * 
     * @return runNumber
     */
    public java.lang.Long getRunNumber() {
        return runNumber;
    }


    /**
     * Sets the runNumber value for this GetMediaFileResultsResponse.
     * 
     * @param runNumber
     */
    public void setRunNumber(java.lang.Long runNumber) {
        this.runNumber = runNumber;
    }


    /**
     * Gets the owner value for this GetMediaFileResultsResponse.
     * 
     * @return owner
     */
    public java.lang.String getOwner() {
        return owner;
    }


    /**
     * Sets the owner value for this GetMediaFileResultsResponse.
     * 
     * @param owner
     */
    public void setOwner(java.lang.String owner) {
        this.owner = owner;
    }


    /**
     * Gets the started value for this GetMediaFileResultsResponse.
     * 
     * @return started
     */
    public java.util.Calendar getStarted() {
        return started;
    }


    /**
     * Sets the started value for this GetMediaFileResultsResponse.
     * 
     * @param started
     */
    public void setStarted(java.util.Calendar started) {
        this.started = started;
    }


    /**
     * Gets the status value for this GetMediaFileResultsResponse.
     * 
     * @return status
     */
    public com.tektronix.www.cerify.soap.client.FileStatusType getStatus() {
        return status;
    }


    /**
     * Sets the status value for this GetMediaFileResultsResponse.
     * 
     * @param status
     */
    public void setStatus(com.tektronix.www.cerify.soap.client.FileStatusType status) {
        this.status = status;
    }


    /**
     * Gets the url value for this GetMediaFileResultsResponse.
     * 
     * @return url
     */
    public org.apache.axis.types.URI getUrl() {
        return url;
    }


    /**
     * Sets the url value for this GetMediaFileResultsResponse.
     * 
     * @param url
     */
    public void setUrl(org.apache.axis.types.URI url) {
        this.url = url;
    }


    /**
     * Gets the posterframe value for this GetMediaFileResultsResponse.
     * 
     * @return posterframe
     */
    public org.apache.axis.types.URI getPosterframe() {
        return posterframe;
    }


    /**
     * Sets the posterframe value for this GetMediaFileResultsResponse.
     * 
     * @param posterframe
     */
    public void setPosterframe(org.apache.axis.types.URI posterframe) {
        this.posterframe = posterframe;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetMediaFileResultsResponse)) return false;
        GetMediaFileResultsResponse other = (GetMediaFileResultsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.streaminfo==null && other.getStreaminfo()==null) || 
             (this.streaminfo!=null &&
              this.streaminfo.equals(other.getStreaminfo()))) &&
            ((this.alert==null && other.getAlert()==null) || 
             (this.alert!=null &&
              java.util.Arrays.equals(this.alert, other.getAlert()))) &&
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
            ((this.lastModified==null && other.getLastModified()==null) || 
             (this.lastModified!=null &&
              this.lastModified.equals(other.getLastModified()))) &&
            ((this.runNumber==null && other.getRunNumber()==null) || 
             (this.runNumber!=null &&
              this.runNumber.equals(other.getRunNumber()))) &&
            ((this.owner==null && other.getOwner()==null) || 
             (this.owner!=null &&
              this.owner.equals(other.getOwner()))) &&
            ((this.started==null && other.getStarted()==null) || 
             (this.started!=null &&
              this.started.equals(other.getStarted()))) &&
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.url==null && other.getUrl()==null) || 
             (this.url!=null &&
              this.url.equals(other.getUrl()))) &&
            ((this.posterframe==null && other.getPosterframe()==null) || 
             (this.posterframe!=null &&
              this.posterframe.equals(other.getPosterframe())));
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
        if (getStreaminfo() != null) {
            _hashCode += getStreaminfo().hashCode();
        }
        if (getAlert() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAlert());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAlert(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
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
        if (getLastModified() != null) {
            _hashCode += getLastModified().hashCode();
        }
        if (getRunNumber() != null) {
            _hashCode += getRunNumber().hashCode();
        }
        if (getOwner() != null) {
            _hashCode += getOwner().hashCode();
        }
        if (getStarted() != null) {
            _hashCode += getStarted().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getUrl() != null) {
            _hashCode += getUrl().hashCode();
        }
        if (getPosterframe() != null) {
            _hashCode += getPosterframe().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetMediaFileResultsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaFileResultsResponse"));
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
        attrField.setFieldName("lastModified");
        attrField.setXmlName(new javax.xml.namespace.QName("", "lastModified"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("runNumber");
        attrField.setXmlName(new javax.xml.namespace.QName("", "runNumber"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("owner");
        attrField.setXmlName(new javax.xml.namespace.QName("", "owner"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
        attrField.setFieldName("url");
        attrField.setXmlName(new javax.xml.namespace.QName("", "url"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "cerifyUrl"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("posterframe");
        attrField.setXmlName(new javax.xml.namespace.QName("", "posterframe"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "cerifyUrl"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("streaminfo");
        elemField.setXmlName(new javax.xml.namespace.QName("", "streaminfo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>GetMediaFileResultsResponse>streaminfo"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alert");
        elemField.setXmlName(new javax.xml.namespace.QName("", "alert"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>GetMediaFileResultsResponse>alert"));
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
