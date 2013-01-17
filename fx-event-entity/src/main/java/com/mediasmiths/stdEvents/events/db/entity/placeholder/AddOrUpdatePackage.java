//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.11.29 at 12:03:26 PM GMT 
//


package com.mediasmiths.stdEvents.events.db.entity.placeholder;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Package" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}packageType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="titleID" use="required" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}titleIdType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "_package"
})
public class AddOrUpdatePackage {

	@XmlAttribute(name = "titleID", required = true)
    protected String titleID;
	@XmlAttribute(name = "presentationID", required = true)
	protected String presentationID;
	@XmlElement(name = "MaterialID", required = true)
	protected String materialID;
	@XmlElement(name = "PresentationFormat", required = true)
	protected String presentationFormat; 
	@XmlElement(name = "Classification")	 
	protected String classification;
	@XmlElement(name = "ConsumerAdvice")
	protected String consumerAdvice;
	@XmlElement(name = "NumberOfSegments", required = true)
	@XmlSchemaType(name = "positiveInteger")
	protected BigInteger numberOfSegments;
	@XmlElement(name = "TargetDate")
	protected String targetDate;
	@XmlElement(name = "Notes")
	protected String notes;
	public String getTitleID()
	{
		return titleID;
	}
	public void setTitleID(String titleID)
	{
		this.titleID = titleID;
	}
	public String getPresentationID()
	{
		return presentationID;
	}
	public void setPresentationID(String presentationID)
	{
		this.presentationID = presentationID;
	}
	public String getMaterialID()
	{
		return materialID;
	}
	public void setMaterialID(String materialID)
	{
		this.materialID = materialID;
	}
	public String getPresentationFormat()
	{
		return presentationFormat;
	}
	public void setPresentationFormat(String presentationFormat)
	{
		this.presentationFormat = presentationFormat;
	}
	public String getClassification()
	{
		return classification;
	}
	public void setClassification(String classification)
	{
		this.classification = classification;
	}
	public String getConsumerAdvice()
	{
		return consumerAdvice;
	}
	public void setConsumerAdvice(String consumerAdvice)
	{
		this.consumerAdvice = consumerAdvice;
	}
	public BigInteger getNumberOfSegments()
	{
		return numberOfSegments;
	}
	public void setNumberOfSegments(BigInteger numberOfSegments)
	{
		this.numberOfSegments = numberOfSegments;
	}
	public String getTargetDate()
	{
		return targetDate;
	}
	public void setTargetDate(String targetDate)
	{
		this.targetDate = targetDate;
	}
	public String getNotes()
	{
		return notes;
	}
	public void setNotes(String notes)
	{
		this.notes = notes;
	}
	
    

}
