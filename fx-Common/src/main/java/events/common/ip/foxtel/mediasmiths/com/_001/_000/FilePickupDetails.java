//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.06.27 at 01:44:29 PM BST 
//


package events.common.ip.foxtel.mediasmiths.com._001._000;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * The definition for File Pickup Details.
 *                     
 * 
 * <p>Java class for FilePickupDetails complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FilePickupDetails">
 *   &lt;complexContent>
 *     &lt;extension base="{http://com.mediasmiths.foxtel.ip.common.events/001/000}IPEvent">
 *       &lt;sequence>
 *         &lt;element name="filePath" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="filename" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="timeDiscovered" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="timeProcessed" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="aggregator" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FilePickupDetails", propOrder = {
    "filePath",
    "filename",
    "timeDiscovered",
    "timeProcessed",
    "aggregator"
})
public class FilePickupDetails
    extends IPEvent
{

    @XmlElement(required = true)
    protected String filePath;
    @XmlElement(required = true)
    protected String filename;
    protected long timeDiscovered;
    protected long timeProcessed;
    @XmlElement(required = true)
    protected String aggregator;

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
     * Gets the value of the filename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the value of the filename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilename(String value) {
        this.filename = value;
    }

    /**
     * Gets the value of the timeDiscovered property.
     * 
     */
    public long getTimeDiscovered() {
        return timeDiscovered;
    }

    /**
     * Sets the value of the timeDiscovered property.
     * 
     */
    public void setTimeDiscovered(long value) {
        this.timeDiscovered = value;
    }

    /**
     * Gets the value of the timeProcessed property.
     * 
     */
    public long getTimeProcessed() {
        return timeProcessed;
    }

    /**
     * Sets the value of the timeProcessed property.
     * 
     */
    public void setTimeProcessed(long value) {
        this.timeProcessed = value;
    }

    /**
     * Gets the value of the aggregator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAggregator() {
        return aggregator;
    }

    /**
     * Sets the value of the aggregator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAggregator(String value) {
        this.aggregator = value;
    }

}
