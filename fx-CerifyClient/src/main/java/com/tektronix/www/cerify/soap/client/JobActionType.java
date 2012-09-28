/**
 * JobActionType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

public class JobActionType implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected JobActionType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _stop = "stop";
    public static final java.lang.String _stopnow = "stopnow";
    public static final java.lang.String _resume = "resume";
    public static final java.lang.String _delete = "delete";
    public static final java.lang.String _priorityhigh = "priorityhigh";
    public static final java.lang.String _prioritymedium = "prioritymedium";
    public static final java.lang.String _prioritylow = "prioritylow";
    public static final JobActionType stop = new JobActionType(_stop);
    public static final JobActionType stopnow = new JobActionType(_stopnow);
    public static final JobActionType resume = new JobActionType(_resume);
    public static final JobActionType delete = new JobActionType(_delete);
    public static final JobActionType priorityhigh = new JobActionType(_priorityhigh);
    public static final JobActionType prioritymedium = new JobActionType(_prioritymedium);
    public static final JobActionType prioritylow = new JobActionType(_prioritylow);
    public java.lang.String getValue() { return _value_;}
    public static JobActionType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        JobActionType enumeration = (JobActionType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static JobActionType fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(JobActionType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "jobActionType"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
