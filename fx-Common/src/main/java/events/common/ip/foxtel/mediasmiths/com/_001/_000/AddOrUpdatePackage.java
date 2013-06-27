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
 *     &lt;extension base="{http://com.mediasmiths.foxtel.ip.common.events/001/000}IPEvent">
 *       &lt;sequence>
 *         &lt;element name="titleID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="materialID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="packageID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="requiredBy" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
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
    "titleID",
    "materialID",
    "packageID",
    "requiredBy"
})
@XmlRootElement(name = "AddOrUpdatePackage")
public class AddOrUpdatePackage
    extends IPEvent
{

    @XmlElement(required = true)
    protected String titleID;
    @XmlElement(required = true)
    protected String materialID;
    @XmlElement(required = true)
    protected String packageID;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar requiredBy;

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
     * Gets the value of the packageID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPackageID() {
        return packageID;
    }

    /**
     * Sets the value of the packageID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPackageID(String value) {
        this.packageID = value;
    }

    /**
     * Gets the value of the requiredBy property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRequiredBy() {
        return requiredBy;
    }

    /**
     * Sets the value of the requiredBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRequiredBy(XMLGregorianCalendar value) {
        this.requiredBy = value;
    }

}
