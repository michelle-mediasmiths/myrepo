/**
 * AlertType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

public class AlertType implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected AlertType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _value1 = "system";
    public static final java.lang.String _value2 = "audio";
    public static final java.lang.String _value3 = "video";
    public static final java.lang.String _value4 = "container";
    public static final java.lang.String _value5 = "parameter";
    public static final java.lang.String _value6 = "container parameter";
    public static final java.lang.String _value7 = "audio parameter";
    public static final java.lang.String _value8 = "video parameter";
    public static final AlertType value1 = new AlertType(_value1);
    public static final AlertType value2 = new AlertType(_value2);
    public static final AlertType value3 = new AlertType(_value3);
    public static final AlertType value4 = new AlertType(_value4);
    public static final AlertType value5 = new AlertType(_value5);
    public static final AlertType value6 = new AlertType(_value6);
    public static final AlertType value7 = new AlertType(_value7);
    public static final AlertType value8 = new AlertType(_value8);
    public java.lang.String getValue() { return _value_;}
    public static AlertType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        AlertType enumeration = (AlertType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static AlertType fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AlertType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "alertType"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
