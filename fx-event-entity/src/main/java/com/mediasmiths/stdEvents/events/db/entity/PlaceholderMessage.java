//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.10.31 at 09:10:26 AM GMT 
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
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.db.entity.HibernateEventingMessage;



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
 *         &lt;element name="Actions">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice maxOccurs="unbounded">
 *                   &lt;element name="CreateOrUpdateTitle">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="TitleDescription" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}titleDescriptionType"/>
 *                             &lt;element name="Rights" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}rightsType"/>
 *                           &lt;/sequence>
 *                           &lt;attribute name="titleID" use="required" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}titleIdType" />
 *                           &lt;attribute name="restrictAccess" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                           &lt;attribute name="purgeProtect" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="PurgeTitle">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="titleID" use="required" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}titleIdType" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="AddOrUpdateMaterial">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="Material" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}materialType"/>
 *                           &lt;/sequence>
 *                           &lt;attribute name="titleID" use="required" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}titleIdType" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="DeleteMaterial">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="Material">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;attribute name="materialID" use="required" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}materialIdType" />
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                           &lt;attribute name="titleID" use="required" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}titleIdType" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="AddOrUpdatePackage">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="Package" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}packageType"/>
 *                           &lt;/sequence>
 *                           &lt;attribute name="titleID" use="required" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}titleIdType" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="DeletePackage">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="Package">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;attribute name="presentationID" use="required" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}presentationIdType" />
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                           &lt;attribute name="titleID" use="required" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}titleIdType" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="PrivateMessageData" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="senderID" use="required" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}senderIdType" />
 *       &lt;attribute name="messageID" use="required" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}messageIdType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@Entity
@Table(name="bms")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement (name="PlaceholderMessage")
public class PlaceholderMessage extends HibernateEventingMessage
{
	@Id
	@GeneratedValue(generator="gen")
    @GenericGenerator(name="gen", strategy="foreign", parameters=@Parameter(name="property", value="event"))
	Long placeholderMessageId;
	
	@Column(name="ACTIONS", length=4000)
    @XmlElement
    @Type(type="text")
    public String actions;
    
	@Column(name="PRIVATE_MESSAGE_DATA")
    @XmlElement
    public String privateMessageData;
    
	@Column(name="SENDER_ID")
    @XmlElement
    public String senderID;
    
	@Column(name="MESSAGE_ID")
    @XmlElement
    public String messageID;
	
	@OneToOne
	@PrimaryKeyJoinColumn
	public EventEntity event;

    /**
     * Gets the value of the actions property.
     * 
     * @return
     *     possible object is
     *     {@link Actions }
     *     
     */
    public String getActions() {
        return actions;
    }

    /**
     * Sets the value of the actions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Actions }
     *     
     */
    public void setActions(String value) {
        this.actions = value;
    }

    /**
     * Gets the value of the privateMessageData property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public String getPrivateMessageData() {
        return privateMessageData;
    }

    /**
     * Sets the value of the privateMessageData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setPrivateMessageData(String value) {
        this.privateMessageData = value;
    }

    /**
     * Gets the value of the senderID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderID() {
        return senderID;
    }

    /**
     * Sets the value of the senderID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderID(String value) {
        this.senderID = value;
    }

    /**
     * Gets the value of the messageID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageID() {
        return messageID;
    }

    /**
     * Sets the value of the messageID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageID(String value) {
        this.messageID = value;
    }

    public PlaceholderMessage withActions(String value) {
        setActions(value);
        return this;
    }

    public PlaceholderMessage withPrivateMessageData(String value) {
        setPrivateMessageData(value);
        return this;
    }

    public PlaceholderMessage withSenderID(String value) {
        setSenderID(value);
        return this;
    }

    public PlaceholderMessage withMessageID(String value) {
        setMessageID(value);
        return this;
    }

}