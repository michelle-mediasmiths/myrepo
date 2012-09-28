/**
 * TestdefinitionType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.templateInfo._2008._06._06;

public class TestdefinitionType  implements java.io.Serializable {
    private com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionrowType[] testdefinitionrow;

    private java.lang.String ruleid;  // attribute

    private java.lang.String testid;  // attribute

    public TestdefinitionType() {
    }

    public TestdefinitionType(
           com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionrowType[] testdefinitionrow,
           java.lang.String ruleid,
           java.lang.String testid) {
           this.testdefinitionrow = testdefinitionrow;
           this.ruleid = ruleid;
           this.testid = testid;
    }


    /**
     * Gets the testdefinitionrow value for this TestdefinitionType.
     * 
     * @return testdefinitionrow
     */
    public com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionrowType[] getTestdefinitionrow() {
        return testdefinitionrow;
    }


    /**
     * Sets the testdefinitionrow value for this TestdefinitionType.
     * 
     * @param testdefinitionrow
     */
    public void setTestdefinitionrow(com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionrowType[] testdefinitionrow) {
        this.testdefinitionrow = testdefinitionrow;
    }

    public com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionrowType getTestdefinitionrow(int i) {
        return this.testdefinitionrow[i];
    }

    public void setTestdefinitionrow(int i, com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionrowType _value) {
        this.testdefinitionrow[i] = _value;
    }


    /**
     * Gets the ruleid value for this TestdefinitionType.
     * 
     * @return ruleid
     */
    public java.lang.String getRuleid() {
        return ruleid;
    }


    /**
     * Sets the ruleid value for this TestdefinitionType.
     * 
     * @param ruleid
     */
    public void setRuleid(java.lang.String ruleid) {
        this.ruleid = ruleid;
    }


    /**
     * Gets the testid value for this TestdefinitionType.
     * 
     * @return testid
     */
    public java.lang.String getTestid() {
        return testid;
    }


    /**
     * Sets the testid value for this TestdefinitionType.
     * 
     * @param testid
     */
    public void setTestid(java.lang.String testid) {
        this.testid = testid;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TestdefinitionType)) return false;
        TestdefinitionType other = (TestdefinitionType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.testdefinitionrow==null && other.getTestdefinitionrow()==null) || 
             (this.testdefinitionrow!=null &&
              java.util.Arrays.equals(this.testdefinitionrow, other.getTestdefinitionrow()))) &&
            ((this.ruleid==null && other.getRuleid()==null) || 
             (this.ruleid!=null &&
              this.ruleid.equals(other.getRuleid()))) &&
            ((this.testid==null && other.getTestid()==null) || 
             (this.testid!=null &&
              this.testid.equals(other.getTestid())));
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
        if (getTestdefinitionrow() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTestdefinitionrow());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTestdefinitionrow(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRuleid() != null) {
            _hashCode += getRuleid().hashCode();
        }
        if (getTestid() != null) {
            _hashCode += getTestid().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(TestdefinitionType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "testdefinitionType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("ruleid");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ruleid"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("testid");
        attrField.setXmlName(new javax.xml.namespace.QName("", "testid"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("testdefinitionrow");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "testdefinitionrow"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "testdefinitionrowType"));
        elemField.setMinOccurs(0);
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
