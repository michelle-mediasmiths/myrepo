/**
 * GetMediaLocationsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

import javax.annotation.Generated;

@Generated(value = {"axis"})public class GetMediaLocationsResponse  implements java.io.Serializable {
    /* Each medialocation will have a
     *                                     unique name and url. */
    private com.tektronix.www.cerify.soap.client.GetMediaLocationsResponseMedialocation[] medialocation;

    public GetMediaLocationsResponse() {
    }

    public GetMediaLocationsResponse(
           com.tektronix.www.cerify.soap.client.GetMediaLocationsResponseMedialocation[] medialocation) {
           this.medialocation = medialocation;
    }


    /**
     * Gets the medialocation value for this GetMediaLocationsResponse.
     * 
     * @return medialocation   * Each medialocation will have a
     *                                     unique name and url.
     */
    public com.tektronix.www.cerify.soap.client.GetMediaLocationsResponseMedialocation[] getMedialocation() {
        return medialocation;
    }


    /**
     * Sets the medialocation value for this GetMediaLocationsResponse.
     * 
     * @param medialocation   * Each medialocation will have a
     *                                     unique name and url.
     */
    public void setMedialocation(com.tektronix.www.cerify.soap.client.GetMediaLocationsResponseMedialocation[] medialocation) {
        this.medialocation = medialocation;
    }

    public com.tektronix.www.cerify.soap.client.GetMediaLocationsResponseMedialocation getMedialocation(int i) {
        return this.medialocation[i];
    }

    public void setMedialocation(int i, com.tektronix.www.cerify.soap.client.GetMediaLocationsResponseMedialocation _value) {
        this.medialocation[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetMediaLocationsResponse)) return false;
        GetMediaLocationsResponse other = (GetMediaLocationsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.medialocation==null && other.getMedialocation()==null) || 
             (this.medialocation!=null &&
              java.util.Arrays.equals(this.medialocation, other.getMedialocation())));
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
        if (getMedialocation() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMedialocation());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMedialocation(), i);
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
        new org.apache.axis.description.TypeDesc(GetMediaLocationsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaLocationsResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("medialocation");
        elemField.setXmlName(new javax.xml.namespace.QName("", "medialocation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>GetMediaLocationsResponse>medialocation"));
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
