/**
 * VersionType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.templateInfo._2008._06._06;

public class VersionType  implements java.io.Serializable {
    private int major;  // attribute

    private int minor;  // attribute

    private int patch;  // attribute

    public VersionType() {
    }

    public VersionType(
           int major,
           int minor,
           int patch) {
           this.major = major;
           this.minor = minor;
           this.patch = patch;
    }


    /**
     * Gets the major value for this VersionType.
     * 
     * @return major
     */
    public int getMajor() {
        return major;
    }


    /**
     * Sets the major value for this VersionType.
     * 
     * @param major
     */
    public void setMajor(int major) {
        this.major = major;
    }


    /**
     * Gets the minor value for this VersionType.
     * 
     * @return minor
     */
    public int getMinor() {
        return minor;
    }


    /**
     * Sets the minor value for this VersionType.
     * 
     * @param minor
     */
    public void setMinor(int minor) {
        this.minor = minor;
    }


    /**
     * Gets the patch value for this VersionType.
     * 
     * @return patch
     */
    public int getPatch() {
        return patch;
    }


    /**
     * Sets the patch value for this VersionType.
     * 
     * @param patch
     */
    public void setPatch(int patch) {
        this.patch = patch;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof VersionType)) return false;
        VersionType other = (VersionType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.major == other.getMajor() &&
            this.minor == other.getMinor() &&
            this.patch == other.getPatch();
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
        _hashCode += getMajor();
        _hashCode += getMinor();
        _hashCode += getPatch();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(VersionType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "versionType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("major");
        attrField.setXmlName(new javax.xml.namespace.QName("", "major"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("minor");
        attrField.setXmlName(new javax.xml.namespace.QName("", "minor"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("patch");
        attrField.setXmlName(new javax.xml.namespace.QName("", "patch"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
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
