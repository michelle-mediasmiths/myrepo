/**
 * CreateMediaSet.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

public class CreateMediaSet  implements java.io.Serializable {
    /* The name of the MediaSet to be
     *                                     created */
    private java.lang.String mediaSetName;

    /* The name of the MediaLocation
     *                                     containing the media file to be
     * added to the MediaSet */
    private java.lang.String mediaLocationName;

    /* Location (URL) of the media file to
     *                                     be added to the MediaSet */
    private org.apache.axis.types.URI url;

    public CreateMediaSet() {
    }

    public CreateMediaSet(
           java.lang.String mediaSetName,
           java.lang.String mediaLocationName,
           org.apache.axis.types.URI url) {
           this.mediaSetName = mediaSetName;
           this.mediaLocationName = mediaLocationName;
           this.url = url;
    }


    /**
     * Gets the mediaSetName value for this CreateMediaSet.
     * 
     * @return mediaSetName   * The name of the MediaSet to be
     *                                     created
     */
    public java.lang.String getMediaSetName() {
        return mediaSetName;
    }


    /**
     * Sets the mediaSetName value for this CreateMediaSet.
     * 
     * @param mediaSetName   * The name of the MediaSet to be
     *                                     created
     */
    public void setMediaSetName(java.lang.String mediaSetName) {
        this.mediaSetName = mediaSetName;
    }


    /**
     * Gets the mediaLocationName value for this CreateMediaSet.
     * 
     * @return mediaLocationName   * The name of the MediaLocation
     *                                     containing the media file to be
     * added to the MediaSet
     */
    public java.lang.String getMediaLocationName() {
        return mediaLocationName;
    }


    /**
     * Sets the mediaLocationName value for this CreateMediaSet.
     * 
     * @param mediaLocationName   * The name of the MediaLocation
     *                                     containing the media file to be
     * added to the MediaSet
     */
    public void setMediaLocationName(java.lang.String mediaLocationName) {
        this.mediaLocationName = mediaLocationName;
    }


    /**
     * Gets the url value for this CreateMediaSet.
     * 
     * @return url   * Location (URL) of the media file to
     *                                     be added to the MediaSet
     */
    public org.apache.axis.types.URI getUrl() {
        return url;
    }


    /**
     * Sets the url value for this CreateMediaSet.
     * 
     * @param url   * Location (URL) of the media file to
     *                                     be added to the MediaSet
     */
    public void setUrl(org.apache.axis.types.URI url) {
        this.url = url;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CreateMediaSet)) return false;
        CreateMediaSet other = (CreateMediaSet) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.mediaSetName==null && other.getMediaSetName()==null) || 
             (this.mediaSetName!=null &&
              this.mediaSetName.equals(other.getMediaSetName()))) &&
            ((this.mediaLocationName==null && other.getMediaLocationName()==null) || 
             (this.mediaLocationName!=null &&
              this.mediaLocationName.equals(other.getMediaLocationName()))) &&
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
        if (getMediaSetName() != null) {
            _hashCode += getMediaSetName().hashCode();
        }
        if (getMediaLocationName() != null) {
            _hashCode += getMediaLocationName().hashCode();
        }
        if (getUrl() != null) {
            _hashCode += getUrl().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CreateMediaSet.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">CreateMediaSet"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mediaSetName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "mediaSetName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mediaLocationName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "mediaLocationName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("url");
        elemField.setXmlName(new javax.xml.namespace.QName("", "url"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
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
