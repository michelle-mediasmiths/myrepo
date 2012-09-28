/**
 * TestdefinitionrowType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.templateInfo._2008._06._06;

public class TestdefinitionrowType  implements java.io.Serializable {
    private com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionrowinputType[] input;

    public TestdefinitionrowType() {
    }

    public TestdefinitionrowType(
           com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionrowinputType[] input) {
           this.input = input;
    }


    /**
     * Gets the input value for this TestdefinitionrowType.
     * 
     * @return input
     */
    public com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionrowinputType[] getInput() {
        return input;
    }


    /**
     * Sets the input value for this TestdefinitionrowType.
     * 
     * @param input
     */
    public void setInput(com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionrowinputType[] input) {
        this.input = input;
    }

    public com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionrowinputType getInput(int i) {
        return this.input[i];
    }

    public void setInput(int i, com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionrowinputType _value) {
        this.input[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TestdefinitionrowType)) return false;
        TestdefinitionrowType other = (TestdefinitionrowType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.input==null && other.getInput()==null) || 
             (this.input!=null &&
              java.util.Arrays.equals(this.input, other.getInput())));
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
        if (getInput() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getInput());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getInput(), i);
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
        new org.apache.axis.description.TypeDesc(TestdefinitionrowType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "testdefinitionrowType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("input");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "input"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "testdefinitionrowinputType"));
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
