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
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;


/**
 * The details of a component or infrastructure communication failure.
 * 				
 * 
 * <p>Java class for CommFailure complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CommFailure">
 *   &lt;complexContent>
 *     &lt;extension base="{http://com.mediasmiths.foxtel.ip.common.events/001/000}IPEvent">
 *       &lt;sequence>
 *         &lt;element name="source" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="target" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="failureShortDesc" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="failureLongDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
/**
 * The details of a component or infrastructure communication failure.
 * 				
 * 
 * <p>Java class for CommFailure complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CommFailure">
 *   &lt;complexContent>
 *     &lt;extension base="{http://com.mediasmiths.foxtel.ip.common.events/001/000}IPEvent">
 *       &lt;sequence>
 *         &lt;element name="source" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="target" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="failureShortDesc" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="failureLongDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */

@Entity
@Table(name="system")
@XmlAccessorType(XmlAccessType.FIELD)
public class IPEvent extends HibernateEventingMessage
{
	@Id
	@GeneratedValue(generator="gen")
    @GenericGenerator(name="gen", strategy="foreign", parameters=@Parameter(name="property", value="event"))
	Long ipEventTypeId;
	
	@Column(name="pickup_kind")
	@XmlElement
    protected String pickUpKind;
	
	@Column(name="filepath")
    @XmlElement
    protected String filePath;
    
	@Column(name="wait_time")
	@XmlElement
    protected String waitTime;
	
	@Column(name="source")
    @XmlElement
    protected String source;
	
	@Column(name="target")
    @XmlElement
    protected String target;
	
	@Column(name="failure_short_desc")
    @XmlElement
    protected String failureShortDesc;
	
	@Column(name="failure_long_desc")
	@XmlElement
	@Type(type="text")
    protected String failureLongDescription;
	
	@OneToOne
	@PrimaryKeyJoinColumn
	public EventEntity event;
	
	/**
     * Gets the value of the pickUpKind property.
     * 
     * @return
     *     possible object is
     *     {@link FilePickUpKinds }
     *     
     */
    public String getPickUpKind() {
        return pickUpKind;
    }

    /**
     * Sets the value of the pickUpKind property.
     * 
     * @param value
     *     allowed object is
     *     {@link FilePickUpKinds }
     *     
     */
    public void setPickUpKind(String value) {
        this.pickUpKind = value;
    }

    /**
     * Gets the value of the filePath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Sets the value of the filePath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilePath(String value) {
        this.filePath = value;
    }

    /**
     * Gets the value of the waitTime property.
     * 
     */
    public String getWaitTime() {
        return waitTime;
    }

    /**
     * Sets the value of the waitTime property.
     * 
     */
    public void setWaitTime(String value) {
        this.waitTime = value;
    }
    
	 /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSource(String value) {
        this.source = value;
    }

    /**
     * Gets the value of the target property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the value of the target property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTarget(String value) {
        this.target = value;
    }

    /**
     * Gets the value of the failureShortDesc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFailureShortDesc() {
        return failureShortDesc;
    }

    /**
     * Sets the value of the failureShortDesc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFailureShortDesc(String value) {
        this.failureShortDesc = value;
    }

    /**
     * Gets the value of the failureLongDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFailureLongDescription() {
        return failureLongDescription;
    }

    /**
     * Sets the value of the failureLongDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFailureLongDescription(String value) {
        this.failureLongDescription = value;
    }
}
