//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.11.01 at 10:24:51 AM GMT 
//


package com.mediasmiths.stdEvents.events.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.db.entity.HibernateEventingMessage;




/**
 * <p>Java class for autoQCResultNotification complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="autoQCResultNotification">
 *   &lt;complexContent>
 *     &lt;extension base="{http://ns.mediasmiths.com/foxtel/wf/adapter}autoQCRequest">
 *       &lt;sequence>
 *         &lt;element name="jobName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@Entity
@Table(name="qc")
@XmlAccessorType(XmlAccessType.FIELD)
public class QC extends HibernateEventingMessage
{
	@Id
	@GeneratedValue(generator="gen")
    @GenericGenerator(name="gen", strategy="foreign", parameters=@Parameter(name="property", value="event"))
	Long qcId;
	
	@Column(name="job_name")
	@XmlElement
    protected String jobName;
	
	@Column(name="asset_id")
	@XmlElement
    protected String assetId;
	
	@Column(name="for_tx_delivery")
	@XmlElement
    protected boolean forTXDelivery;
	
	@OneToOne
	@PrimaryKeyJoinColumn
	public EventEntity event;

    /**
     * Gets the value of the jobName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * Sets the value of the jobName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJobName(String value) {
        this.jobName = value;
    }
    /**
     * Gets the value of the assetId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssetId() {
        return assetId;
    }

    /**
     * Sets the value of the assetId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssetId(String value) {
        this.assetId = value;
    }

    /**
     * Gets the value of the forTXDelivery property.
     * 
     */
    public boolean isForTXDelivery() {
        return forTXDelivery;
    }

    /**
     * Sets the value of the forTXDelivery property.
     * 
     */
    public void setForTXDelivery(boolean value) {
        this.forTXDelivery = value;
    }

}
