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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * A type to represent a channel conditions found event
 *                 
 * 
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://com.mediasmiths.foxtel.ip.common.events/001/000}IPEvent">
 *       &lt;sequence>
 *         &lt;element name="materialID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "materialID"
})
@XmlRootElement(name = "channelConditionsFound")
public class ChannelConditionsFound
    extends IPEvent
{

    @XmlElement(required = true)
    protected String materialID;

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

}
