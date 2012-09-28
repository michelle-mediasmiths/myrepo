/**
 * TemplatesType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.templateInfo._2008._06._06;

public class TemplatesType  implements java.io.Serializable {
    private com.tektronix.www.cerify.templateInfo._2008._06._06.VersionType version;

    private com.tektronix.www.cerify.templateInfo._2008._06._06.TemplateType[] template;

    public TemplatesType() {
    }

    public TemplatesType(
           com.tektronix.www.cerify.templateInfo._2008._06._06.VersionType version,
           com.tektronix.www.cerify.templateInfo._2008._06._06.TemplateType[] template) {
           this.version = version;
           this.template = template;
    }


    /**
     * Gets the version value for this TemplatesType.
     * 
     * @return version
     */
    public com.tektronix.www.cerify.templateInfo._2008._06._06.VersionType getVersion() {
        return version;
    }


    /**
     * Sets the version value for this TemplatesType.
     * 
     * @param version
     */
    public void setVersion(com.tektronix.www.cerify.templateInfo._2008._06._06.VersionType version) {
        this.version = version;
    }


    /**
     * Gets the template value for this TemplatesType.
     * 
     * @return template
     */
    public com.tektronix.www.cerify.templateInfo._2008._06._06.TemplateType[] getTemplate() {
        return template;
    }


    /**
     * Sets the template value for this TemplatesType.
     * 
     * @param template
     */
    public void setTemplate(com.tektronix.www.cerify.templateInfo._2008._06._06.TemplateType[] template) {
        this.template = template;
    }

    public com.tektronix.www.cerify.templateInfo._2008._06._06.TemplateType getTemplate(int i) {
        return this.template[i];
    }

    public void setTemplate(int i, com.tektronix.www.cerify.templateInfo._2008._06._06.TemplateType _value) {
        this.template[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TemplatesType)) return false;
        TemplatesType other = (TemplatesType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.version==null && other.getVersion()==null) || 
             (this.version!=null &&
              this.version.equals(other.getVersion()))) &&
            ((this.template==null && other.getTemplate()==null) || 
             (this.template!=null &&
              java.util.Arrays.equals(this.template, other.getTemplate())));
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
        if (getVersion() != null) {
            _hashCode += getVersion().hashCode();
        }
        if (getTemplate() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTemplate());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTemplate(), i);
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
        new org.apache.axis.description.TypeDesc(TemplatesType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "templatesType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("version");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "version"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "versionType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("template");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "template"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "templateType"));
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
