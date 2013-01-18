//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.11.29 at 12:03:26 PM GMT 
//


package com.mediasmiths.stdEvents.events.db.entity.placeholder;

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
 *         &lt;element name="Material" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}materialType"/>
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
    "material"
})
public class AddOrUpdateMaterial {

    @XmlAttribute(name = "titleID", required = true)
    protected String titleID;
    @XmlAttribute(name = "materialID", required = true)
    protected String materialID;
    @XmlElement(name = "RequiredBy")
    protected String requiredBy;
    @XmlElement(name = "RequiredFormat")
    protected String requiredFormat;
    @XmlElement(name = "QualityCheckTask")
    protected String qualityCheckTask;
    @XmlElement(name = "OrderCreated")
    @XmlSchemaType(name = "dateTime")
    protected String orderCreated;
    @XmlElement(name = "OrderReference")
    protected String orderReference;
    @XmlAttribute(name = "aggregatorID", required = true)
    protected String aggregatorID;
    @XmlAttribute(name = "aggregatorName")
    protected String aggregatorName;
	
    public String getTitleID()
	{
		return titleID;
	}
	public void setTitleID(String titleID)
	{
		this.titleID = titleID;
	}
	public String getMaterialID()
	{
		return materialID;
	}
	public void setMaterialID(String materialID)
	{
		this.materialID = materialID;
	}
	public String getRequiredBy()
	{
		return requiredBy;
	}
	public void setRequiredBy(String requiredBy)
	{
		this.requiredBy = requiredBy;
	}
	public String getRequiredFormat()
	{
		return requiredFormat;
	}
	public void setRequiredFormat(String requiredFormat)
	{
		this.requiredFormat = requiredFormat;
	}
	public String getQualityCheckTask()
	{
		return qualityCheckTask;
	}
	public void setQualityCheckTask(String qualityCheckTask)
	{
		this.qualityCheckTask = qualityCheckTask;
	}
	public String getOrderCreated()
	{
		return orderCreated;
	}
	public void setOrderCreated(String orderCreated)
	{
		this.orderCreated = orderCreated;
	}
	public String getOrderReference()
	{
		return orderReference;
	}
	public void setOrderReference(String orderReference)
	{
		this.orderReference = orderReference;
	}
	public String getAggregatorID()
	{
		return aggregatorID;
	}
	public void setAggregatorID(String aggregatorID)
	{
		this.aggregatorID = aggregatorID;
	}
	public String getAggregatorName()
	{
		return aggregatorName;
	}
	public void setAggregatorName(String aggregatorName)
	{
		this.aggregatorName = aggregatorName;
	}
}