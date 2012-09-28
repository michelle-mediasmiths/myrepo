/**
 * GetSystemStatusResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

public class GetSystemStatusResponse  implements java.io.Serializable {
    /* The number of Jobs shown as
     *                                     "Waiting" in the Web UI */
    private java.math.BigInteger totalJobsPending;

    /* The number of MTUs in the cluster */
    private java.math.BigInteger totalMtusAvailable;

    /* File capacity per MTU multiplied by
     *                                     the number of MTUs */
    private java.math.BigInteger totalFileCapacity;

    public GetSystemStatusResponse() {
    }

    public GetSystemStatusResponse(
           java.math.BigInteger totalJobsPending,
           java.math.BigInteger totalMtusAvailable,
           java.math.BigInteger totalFileCapacity) {
           this.totalJobsPending = totalJobsPending;
           this.totalMtusAvailable = totalMtusAvailable;
           this.totalFileCapacity = totalFileCapacity;
    }


    /**
     * Gets the totalJobsPending value for this GetSystemStatusResponse.
     * 
     * @return totalJobsPending   * The number of Jobs shown as
     *                                     "Waiting" in the Web UI
     */
    public java.math.BigInteger getTotalJobsPending() {
        return totalJobsPending;
    }


    /**
     * Sets the totalJobsPending value for this GetSystemStatusResponse.
     * 
     * @param totalJobsPending   * The number of Jobs shown as
     *                                     "Waiting" in the Web UI
     */
    public void setTotalJobsPending(java.math.BigInteger totalJobsPending) {
        this.totalJobsPending = totalJobsPending;
    }


    /**
     * Gets the totalMtusAvailable value for this GetSystemStatusResponse.
     * 
     * @return totalMtusAvailable   * The number of MTUs in the cluster
     */
    public java.math.BigInteger getTotalMtusAvailable() {
        return totalMtusAvailable;
    }


    /**
     * Sets the totalMtusAvailable value for this GetSystemStatusResponse.
     * 
     * @param totalMtusAvailable   * The number of MTUs in the cluster
     */
    public void setTotalMtusAvailable(java.math.BigInteger totalMtusAvailable) {
        this.totalMtusAvailable = totalMtusAvailable;
    }


    /**
     * Gets the totalFileCapacity value for this GetSystemStatusResponse.
     * 
     * @return totalFileCapacity   * File capacity per MTU multiplied by
     *                                     the number of MTUs
     */
    public java.math.BigInteger getTotalFileCapacity() {
        return totalFileCapacity;
    }


    /**
     * Sets the totalFileCapacity value for this GetSystemStatusResponse.
     * 
     * @param totalFileCapacity   * File capacity per MTU multiplied by
     *                                     the number of MTUs
     */
    public void setTotalFileCapacity(java.math.BigInteger totalFileCapacity) {
        this.totalFileCapacity = totalFileCapacity;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetSystemStatusResponse)) return false;
        GetSystemStatusResponse other = (GetSystemStatusResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.totalJobsPending==null && other.getTotalJobsPending()==null) || 
             (this.totalJobsPending!=null &&
              this.totalJobsPending.equals(other.getTotalJobsPending()))) &&
            ((this.totalMtusAvailable==null && other.getTotalMtusAvailable()==null) || 
             (this.totalMtusAvailable!=null &&
              this.totalMtusAvailable.equals(other.getTotalMtusAvailable()))) &&
            ((this.totalFileCapacity==null && other.getTotalFileCapacity()==null) || 
             (this.totalFileCapacity!=null &&
              this.totalFileCapacity.equals(other.getTotalFileCapacity())));
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
        if (getTotalJobsPending() != null) {
            _hashCode += getTotalJobsPending().hashCode();
        }
        if (getTotalMtusAvailable() != null) {
            _hashCode += getTotalMtusAvailable().hashCode();
        }
        if (getTotalFileCapacity() != null) {
            _hashCode += getTotalFileCapacity().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetSystemStatusResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetSystemStatusResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalJobsPending");
        elemField.setXmlName(new javax.xml.namespace.QName("", "totalJobsPending"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "integer"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalMtusAvailable");
        elemField.setXmlName(new javax.xml.namespace.QName("", "totalMtusAvailable"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "integer"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalFileCapacity");
        elemField.setXmlName(new javax.xml.namespace.QName("", "totalFileCapacity"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "integer"));
        elemField.setNillable(false);
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
