/**
 * TemplateType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.templateInfo._2008._06._06;

public class TemplateType  implements java.io.Serializable {
    private com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionType[] testdefinition;

    private java.lang.String name;  // attribute

    private int version;  // attribute

    private com.tektronix.www.cerify.templateInfo._2008._06._06.TemplateTypeType type;  // attribute

    private java.lang.String description;  // attribute

    private java.lang.String standard;  // attribute

    public TemplateType() {
    }

    public TemplateType(
           com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionType[] testdefinition,
           java.lang.String name,
           int version,
           com.tektronix.www.cerify.templateInfo._2008._06._06.TemplateTypeType type,
           java.lang.String description,
           java.lang.String standard) {
           this.testdefinition = testdefinition;
           this.name = name;
           this.version = version;
           this.type = type;
           this.description = description;
           this.standard = standard;
    }


    /**
     * Gets the testdefinition value for this TemplateType.
     * 
     * @return testdefinition
     */
    public com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionType[] getTestdefinition() {
        return testdefinition;
    }


    /**
     * Sets the testdefinition value for this TemplateType.
     * 
     * @param testdefinition
     */
    public void setTestdefinition(com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionType[] testdefinition) {
        this.testdefinition = testdefinition;
    }

    public com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionType getTestdefinition(int i) {
        return this.testdefinition[i];
    }

    public void setTestdefinition(int i, com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionType _value) {
        this.testdefinition[i] = _value;
    }


    /**
     * Gets the name value for this TemplateType.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this TemplateType.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the version value for this TemplateType.
     * 
     * @return version
     */
    public int getVersion() {
        return version;
    }


    /**
     * Sets the version value for this TemplateType.
     * 
     * @param version
     */
    public void setVersion(int version) {
        this.version = version;
    }


    /**
     * Gets the type value for this TemplateType.
     * 
     * @return type
     */
    public com.tektronix.www.cerify.templateInfo._2008._06._06.TemplateTypeType getType() {
        return type;
    }


    /**
     * Sets the type value for this TemplateType.
     * 
     * @param type
     */
    public void setType(com.tektronix.www.cerify.templateInfo._2008._06._06.TemplateTypeType type) {
        this.type = type;
    }


    /**
     * Gets the description value for this TemplateType.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this TemplateType.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the standard value for this TemplateType.
     * 
     * @return standard
     */
    public java.lang.String getStandard() {
        return standard;
    }


    /**
     * Sets the standard value for this TemplateType.
     * 
     * @param standard
     */
    public void setStandard(java.lang.String standard) {
        this.standard = standard;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TemplateType)) return false;
        TemplateType other = (TemplateType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.testdefinition==null && other.getTestdefinition()==null) || 
             (this.testdefinition!=null &&
              java.util.Arrays.equals(this.testdefinition, other.getTestdefinition()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            this.version == other.getVersion() &&
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this.standard==null && other.getStandard()==null) || 
             (this.standard!=null &&
              this.standard.equals(other.getStandard())));
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
        if (getTestdefinition() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTestdefinition());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTestdefinition(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        _hashCode += getVersion();
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getStandard() != null) {
            _hashCode += getStandard().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(TemplateType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "templateType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("name");
        attrField.setXmlName(new javax.xml.namespace.QName("", "name"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("version");
        attrField.setXmlName(new javax.xml.namespace.QName("", "version"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("type");
        attrField.setXmlName(new javax.xml.namespace.QName("", "type"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", ">templateType>type"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("description");
        attrField.setXmlName(new javax.xml.namespace.QName("", "description"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("standard");
        attrField.setXmlName(new javax.xml.namespace.QName("", "standard"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("testdefinition");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "testdefinition"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "testdefinitionType"));
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
