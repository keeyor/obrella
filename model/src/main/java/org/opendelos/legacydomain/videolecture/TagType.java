//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.16 at 10:09:14 AM EEST 
//


package org.opendelos.legacydomain.videolecture;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TagType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TagType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ResPub" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ResApp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ResFin" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MetEdt" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MultEdt" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MultRed" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PreUp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PreSyn" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Sub" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TagType", propOrder = {
    "resPub",
    "resApp",
    "resFin",
    "metEdt",
    "multEdt",
    "multRed",
    "preUp",
    "preSyn",
    "sub"
})
public class TagType {

    @XmlElement(name = "ResPub")
    protected String resPub;
    @XmlElement(name = "ResApp")
    protected String resApp;
    @XmlElement(name = "ResFin")
    protected String resFin;
    @XmlElement(name = "MetEdt")
    protected String metEdt;
    @XmlElement(name = "MultEdt")
    protected String multEdt;
    @XmlElement(name = "MultRed")
    protected String multRed;
    @XmlElement(name = "PreUp")
    protected String preUp;
    @XmlElement(name = "PreSyn")
    protected String preSyn;
    @XmlElement(name = "Sub")
    protected String sub;

    /**
     * Gets the value of the resPub property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResPub() {
        return resPub;
    }

    /**
     * Sets the value of the resPub property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResPub(String value) {
        this.resPub = value;
    }

    /**
     * Gets the value of the resApp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResApp() {
        return resApp;
    }

    /**
     * Sets the value of the resApp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResApp(String value) {
        this.resApp = value;
    }

    /**
     * Gets the value of the resFin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResFin() {
        return resFin;
    }

    /**
     * Sets the value of the resFin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResFin(String value) {
        this.resFin = value;
    }

    /**
     * Gets the value of the metEdt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMetEdt() {
        return metEdt;
    }

    /**
     * Sets the value of the metEdt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMetEdt(String value) {
        this.metEdt = value;
    }

    /**
     * Gets the value of the multEdt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMultEdt() {
        return multEdt;
    }

    /**
     * Sets the value of the multEdt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMultEdt(String value) {
        this.multEdt = value;
    }

    /**
     * Gets the value of the multRed property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMultRed() {
        return multRed;
    }

    /**
     * Sets the value of the multRed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMultRed(String value) {
        this.multRed = value;
    }

    /**
     * Gets the value of the preUp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreUp() {
        return preUp;
    }

    /**
     * Sets the value of the preUp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreUp(String value) {
        this.preUp = value;
    }

    /**
     * Gets the value of the preSyn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreSyn() {
        return preSyn;
    }

    /**
     * Sets the value of the preSyn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreSyn(String value) {
        this.preSyn = value;
    }

    /**
     * Gets the value of the sub property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSub() {
        return sub;
    }

    /**
     * Sets the value of the sub property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSub(String value) {
        this.sub = value;
    }

}