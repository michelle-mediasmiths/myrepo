package com.mediasmiths.stdEvents.events.db.entity.placeholder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
* <p>Java class for anonymous complex type.
* 
* <p>The following schema fragment specifies the expected content contained within this class.
* 
* <pre>
* &lt;complexType>
*   &lt;complexContent>
*     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
*       &lt;attribute name="titleID" use="required" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}titleIdType" />
*     &lt;/restriction>
*   &lt;/complexContent>
* &lt;/complexType>
* </pre>
* 
* 
*/
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class PurgeTitle {

 @XmlAttribute(name = "titleID", required = true)
 protected String titleID;

 /**
  * Gets the value of the titleID property.
  * 
  * @return
  *     possible object is
  *     {@link String }
  *     
  */
 public String getTitleID() {
     return titleID;
 }

 /**
  * Sets the value of the titleID property.
  * 
  * @param value
  *     allowed object is
  *     {@link String }
  *     
  */
 public void setTitleID(String value) {
     this.titleID = value;
 }

 public PurgeTitle withTitleID(String value) {
     setTitleID(value);
     return this;
 }

}
