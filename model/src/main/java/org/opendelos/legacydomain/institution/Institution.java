//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.16 at 10:48:33 AM EET 
//


package org.opendelos.legacydomain.institution;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
 *         &lt;element name="InstitutionName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="InstitutionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Url" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SupportUrl" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="LogoUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AbcdUrl" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="LmsUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OrganizationLicense" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Administrator">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="Email" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="Telephone" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Schools">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://gunet.gr/Institution}School" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Classrooms">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://gunet.gr/Institution}Classroom" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="StreamingServers" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://gunet.gr/Institution}StreamingServer" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "institutionName",
    "institutionId",
    "url",
    "supportUrl",
    "logoUrl",
    "abcdUrl",
    "lmsUrl",
    "organizationLicense",
    "administrator",
    "schools",
    "classrooms",
    "streamingServers"
})
@XmlRootElement(name = "Institution")
public class Institution {

    @XmlElement(name = "InstitutionName", required = true)
    protected String institutionName;
    @XmlElement(name = "InstitutionId", required = true)
    protected String institutionId;
    @XmlElement(name = "Url", required = true)
    protected String url;
    @XmlElement(name = "SupportUrl", required = true)
    protected String supportUrl;
    @XmlElement(name = "LogoUrl")
    protected String logoUrl;
    @XmlElement(name = "AbcdUrl", required = true)
    protected String abcdUrl;
    @XmlElement(name = "LmsUrl")
    protected String lmsUrl;
    @XmlElement(name = "OrganizationLicense", required = true)
    protected String organizationLicense;
    @XmlElement(name = "Administrator", required = true)
    protected Administrator administrator;
    @XmlElement(name = "Schools", required = true)
    protected Schools schools;
    @XmlElement(name = "Classrooms", required = true)
    protected Classrooms classrooms;
    @XmlElement(name = "StreamingServers")
    protected StreamingServers streamingServers;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return institutionName;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.institutionName = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return institutionId;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.institutionId = value;
    }

    /**
     * Gets the value of the url property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     * Gets the value of the supportUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupportUrl() {
        return supportUrl;
    }

    /**
     * Sets the value of the supportUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupportUrl(String value) {
        this.supportUrl = value;
    }

    /**
     * Gets the value of the logoUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogoUrl() {
        return logoUrl;
    }

    /**
     * Sets the value of the logoUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogoUrl(String value) {
        this.logoUrl = value;
    }

    /**
     * Gets the value of the abcdUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAbcdUrl() {
        return abcdUrl;
    }

    /**
     * Sets the value of the abcdUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAbcdUrl(String value) {
        this.abcdUrl = value;
    }

    /**
     * Gets the value of the lmsUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLmsUrl() {
        return lmsUrl;
    }

    /**
     * Sets the value of the lmsUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLmsUrl(String value) {
        this.lmsUrl = value;
    }

    /**
     * Gets the value of the organizationLicense property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganizationLicense() {
        return organizationLicense;
    }

    /**
     * Sets the value of the organizationLicense property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganizationLicense(String value) {
        this.organizationLicense = value;
    }

    /**
     * Gets the value of the administrator property.
     * 
     * @return
     *     possible object is
     *     {@link Administrator }
     *     
     */
    public Administrator getAdministrator() {
        return administrator;
    }

    /**
     * Sets the value of the administrator property.
     * 
     * @param value
     *     allowed object is
     *     {@link Administrator }
     *     
     */
    public void setAdministrator(Administrator value) {
        this.administrator = value;
    }

    /**
     * Gets the value of the schools property.
     * 
     * @return
     *     possible object is
     *     {@link Schools }
     *     
     */
    public Schools getSchools() {
        return schools;
    }

    /**
     * Sets the value of the schools property.
     * 
     * @param value
     *     allowed object is
     *     {@link Schools }
     *     
     */
    public void setSchools(Schools value) {
        this.schools = value;
    }

    /**
     * Gets the value of the classrooms property.
     * 
     * @return
     *     possible object is
     *     {@link Classrooms }
     *     
     */
    public Classrooms getClassrooms() {
        return classrooms;
    }

    /**
     * Sets the value of the classrooms property.
     * 
     * @param value
     *     allowed object is
     *     {@link Classrooms }
     *     
     */
    public void setClassrooms(Classrooms value) {
        this.classrooms = value;
    }

    /**
     * Gets the value of the streamingServers property.
     * 
     * @return
     *     possible object is
     *     {@link StreamingServers }
     *     
     */
    public StreamingServers getStreamingServers() {
        return streamingServers;
    }

    /**
     * Sets the value of the streamingServers property.
     * 
     * @param value
     *     allowed object is
     *     {@link StreamingServers }
     *     
     */
    public void setStreamingServers(StreamingServers value) {
        this.streamingServers = value;
    }


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
     *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="Email" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="Telephone" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "name",
        "email",
        "telephone"
    })
    public static class Administrator {

        @XmlElement(name = "Name", required = true)
        protected String name;
        @XmlElement(name = "Email", required = true)
        protected String email;
        @XmlElement(name = "Telephone")
        protected List<String> telephone;

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setName(String value) {
            this.name = value;
        }

        /**
         * Gets the value of the email property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEmail() {
            return email;
        }

        /**
         * Sets the value of the email property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEmail(String value) {
            this.email = value;
        }

        /**
         * Gets the value of the telephone property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the telephone property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTelephone().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getTelephone() {
            if (telephone == null) {
                telephone = new ArrayList<String>();
            }
            return this.telephone;
        }

    }


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
     *         &lt;element ref="{http://gunet.gr/Institution}Classroom" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "classroom"
    })
    public static class Classrooms {

        @XmlElement(name = "Classroom")
        protected List<Classroom> classroom;

        /**
         * Gets the value of the classroom property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the classroom property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getClassroom().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Classroom }
         * 
         * 
         */
        public List<Classroom> getClassroom() {
            if (classroom == null) {
                classroom = new ArrayList<Classroom>();
            }
            return this.classroom;
        }

    }


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
     *         &lt;element ref="{http://gunet.gr/Institution}School" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "school"
    })
    public static class Schools {

        @XmlElement(name = "School")
        protected List<School> school;

        /**
         * Gets the value of the school property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the school property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSchool().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link School }
         * 
         * 
         */
        public List<School> getSchool() {
            if (school == null) {
                school = new ArrayList<School>();
            }
            return this.school;
        }

    }


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
     *         &lt;element ref="{http://gunet.gr/Institution}StreamingServer" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "streamingServer"
    })
    public static class StreamingServers {

        @XmlElement(name = "StreamingServer")
        protected List<StreamingServer> streamingServer;

        /**
         * Gets the value of the streamingServer property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the streamingServer property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getStreamingServer().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link StreamingServer }
         * 
         * 
         */
        public List<StreamingServer> getStreamingServer() {
            if (streamingServer == null) {
                streamingServer = new ArrayList<StreamingServer>();
            }
            return this.streamingServer;
        }

    }

}
