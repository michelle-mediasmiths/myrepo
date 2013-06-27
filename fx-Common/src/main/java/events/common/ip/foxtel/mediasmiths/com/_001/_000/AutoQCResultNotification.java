//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.06.27 at 01:44:29 PM BST 
//


package events.common.ip.foxtel.mediasmiths.com._001._000;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * A type to represent an ArdomeJobFailure event
 *                 
 * 
 * <p>Java class for autoQCResultNotification complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="autoQCResultNotification">
 *   &lt;complexContent>
 *     &lt;extension base="{http://com.mediasmiths.foxtel.ip.common.events/001/000}IPEvent">
 *       &lt;sequence>
 *         &lt;element name="dateRange" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="materialID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="channels" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="contentType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="operator" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="qcStatus" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="taskStatus" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="taskStart" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="taskFinish" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="warningTime" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="manualOverride" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="failureParameter" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="titleLength" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="assetId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="forTXDelivery" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="qcReportFilePath" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "autoQCResultNotification", propOrder = {
    "dateRange",
    "title",
    "materialID",
    "channels",
    "contentType",
    "operator",
    "qcStatus",
    "taskStatus",
    "taskStart",
    "taskFinish",
    "warningTime",
    "manualOverride",
    "failureParameter",
    "titleLength",
    "assetId",
    "forTXDelivery",
    "qcReportFilePath"
})
@XmlSeeAlso({
    AutoQCPassNotification.class,
    PreviewFailed.class,
    AutoQCErrorNotification.class,
    AutoQCFailureNotification.class
})
public class AutoQCResultNotification
    extends IPEvent
{

    @XmlElement(required = true)
    protected String dateRange;
    @XmlElement(required = true)
    protected String title;
    @XmlElement(required = true)
    protected String materialID;
    @XmlElement(required = true)
    protected String channels;
    @XmlElement(required = true)
    protected String contentType;
    @XmlElement(required = true)
    protected String operator;
    @XmlElement(required = true)
    protected String qcStatus;
    @XmlElement(required = true)
    protected String taskStatus;
    @XmlElement(required = true)
    protected String taskStart;
    @XmlElement(required = true)
    protected String taskFinish;
    @XmlElement(required = true)
    protected String warningTime;
    @XmlElement(required = true)
    protected String manualOverride;
    @XmlElement(required = true)
    protected String failureParameter;
    @XmlElement(required = true)
    protected String titleLength;
    @XmlElement(required = true)
    protected String assetId;
    protected boolean forTXDelivery;
    protected List<String> qcReportFilePath;

    /**
     * Gets the value of the dateRange property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateRange() {
        return dateRange;
    }

    /**
     * Sets the value of the dateRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateRange(String value) {
        this.dateRange = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the materialID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaterialID() {
        return materialID;
    }

    /**
     * Sets the value of the materialID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaterialID(String value) {
        this.materialID = value;
    }

    /**
     * Gets the value of the channels property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChannels() {
        return channels;
    }

    /**
     * Sets the value of the channels property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChannels(String value) {
        this.channels = value;
    }

    /**
     * Gets the value of the contentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the value of the contentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentType(String value) {
        this.contentType = value;
    }

    /**
     * Gets the value of the operator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Sets the value of the operator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOperator(String value) {
        this.operator = value;
    }

    /**
     * Gets the value of the qcStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQcStatus() {
        return qcStatus;
    }

    /**
     * Sets the value of the qcStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQcStatus(String value) {
        this.qcStatus = value;
    }

    /**
     * Gets the value of the taskStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskStatus() {
        return taskStatus;
    }

    /**
     * Sets the value of the taskStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskStatus(String value) {
        this.taskStatus = value;
    }

    /**
     * Gets the value of the taskStart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskStart() {
        return taskStart;
    }

    /**
     * Sets the value of the taskStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskStart(String value) {
        this.taskStart = value;
    }

    /**
     * Gets the value of the taskFinish property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskFinish() {
        return taskFinish;
    }

    /**
     * Sets the value of the taskFinish property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskFinish(String value) {
        this.taskFinish = value;
    }

    /**
     * Gets the value of the warningTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWarningTime() {
        return warningTime;
    }

    /**
     * Sets the value of the warningTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWarningTime(String value) {
        this.warningTime = value;
    }

    /**
     * Gets the value of the manualOverride property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManualOverride() {
        return manualOverride;
    }

    /**
     * Sets the value of the manualOverride property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManualOverride(String value) {
        this.manualOverride = value;
    }

    /**
     * Gets the value of the failureParameter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFailureParameter() {
        return failureParameter;
    }

    /**
     * Sets the value of the failureParameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFailureParameter(String value) {
        this.failureParameter = value;
    }

    /**
     * Gets the value of the titleLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitleLength() {
        return titleLength;
    }

    /**
     * Sets the value of the titleLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitleLength(String value) {
        this.titleLength = value;
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

    /**
     * Gets the value of the qcReportFilePath property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the qcReportFilePath property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQcReportFilePath().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getQcReportFilePath() {
        if (qcReportFilePath == null) {
            qcReportFilePath = new ArrayList<String>();
        }
        return this.qcReportFilePath;
    }

}
