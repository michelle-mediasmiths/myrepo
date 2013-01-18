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
 *         &lt;element name="TitleDescription" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}titleDescriptionType"/>
 *         &lt;element name="Rights" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}rightsType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="titleID" use="required" type="{http://foxtel.com.au/schemas/MAM/Placeholders/001/000}titleIdType" />
 *       &lt;attribute name="restrictAccess" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="purgeProtect" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "titleDescription",
    "rights"
})
public class CreateOrUpdateTitle {

	@XmlAttribute(name = "titleID", required = true)
    protected String titleID;
	//TitleDescription
	@XmlElement(name = "ProgrammeTitle", required = true)
    protected String programmeTitle;
    @XmlElement(name = "ProductionNumber")
    protected String productionNumber;
    @XmlElement(name = "EpisodeTitle")
    protected String episodeTitle;
    @XmlElement(name = "SeriesNumber")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger seriesNumber;
    @XmlElement(name = "EpisodeNumber")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger episodeNumber;
    @XmlElement(name = "YearOfProduction")
    protected BigInteger yearOfProduction;
    @XmlElement(name = "CountryOfProduction")
    protected String countryOfProduction;
    @XmlElement(name = "Style")
    protected String style;
    @XmlElement(name = "Show")
    protected String show;
    
    //License
    @XmlElement(name = "LicenseHolder", required = true)
    protected String licenseHolder;
    @XmlElement(name = "LicensePeriod", required = true)
    protected String licensePeriod;
    
    //Channels
    @XmlAttribute(name = "channelTag", required = true)
    protected String channelTag;
    @XmlAttribute(name = "channelName")
    protected String channelName;

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


    public CreateOrUpdateTitle withTitleID(String value) {
        setTitleID(value);
        return this;
    }

    public String getProgrammeTitle()
	{
		return programmeTitle;
	}

	public void setProgrammeTitle(String programmeTitle)
	{
		this.programmeTitle = programmeTitle;
	}

	public String getProductionNumber()
	{
		return productionNumber;
	}

	public void setProductionNumber(String productionNumber)
	{
		this.productionNumber = productionNumber;
	}

	public String getEpisodeTitle()
	{
		return episodeTitle;
	}

	public void setEpisodeTitle(String episodeTitle)
	{
		this.episodeTitle = episodeTitle;
	}

	public BigInteger getSeriesNumber()
	{
		return seriesNumber;
	}

	public void setSeriesNumber(BigInteger seriesNumber)
	{
		this.seriesNumber = seriesNumber;
	}

	public BigInteger getEpisodeNumber()
	{
		return episodeNumber;
	}

	public void setEpisodeNumber(BigInteger episodeNumber)
	{
		this.episodeNumber = episodeNumber;
	}

	public BigInteger getYearOfProduction()
	{
		return yearOfProduction;
	}

	public void setYearOfProduction(BigInteger yearOfProduction)
	{
		this.yearOfProduction = yearOfProduction;
	}

	public String getCountryOfProduction()
	{
		return countryOfProduction;
	}

	public void setCountryOfProduction(String countryOfProduction)
	{
		this.countryOfProduction = countryOfProduction;
	}

	public String getStyle()
	{
		return style;
	}

	public void setStyle(String style)
	{
		this.style = style;
	}

	public String getShow()
	{
		return show;
	}

	public void setShow(String show)
	{
		this.show = show;
	}

	public String getLicenseHolder()
	{
		return licenseHolder;
	}

	public void setLicenseHolder(String licenseHolder)
	{
		this.licenseHolder = licenseHolder;
	}

	public String getLicensePeriod()
	{
		return licensePeriod;
	}

	public void setLicensePeriod(String licensePeriod)
	{
		this.licensePeriod = licensePeriod;
	}

	public String getChannelTag()
	{
		return channelTag;
	}

	public void setChannelTag(String channelTag)
	{
		this.channelTag = channelTag;
	}

	public String getChannelName()
	{
		return channelName;
	}

	public void setChannelName(String channelName)
	{
		this.channelName = channelName;
	}
}