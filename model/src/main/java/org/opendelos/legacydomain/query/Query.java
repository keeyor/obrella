//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.26 at 01:38:03 PM EEST 
//


package org.opendelos.legacydomain.query;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opendelos.legacydomain.videolecture.StatusType;


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
 *         &lt;element name="SearchTerms" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AdminFilter" type="{http://gunet.gr/Query}AdminFilterType" minOccurs="0"/>
 *         &lt;element name="FilterResults" type="{http://gunet.gr/Query}FilterResultsType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="FilterOperand" type="{http://gunet.gr/Query}OperandType" minOccurs="0"/>
 *         &lt;element name="RequireNotNull" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Status" type="{http://gunet.gr/VideoLecture}StatusType" minOccurs="0"/>
 *         &lt;element name="RequiredSecurityStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RequiredUserId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RequireResource" type="{http://gunet.gr/Query}RequireResourceType" minOccurs="0"/>
 *         &lt;element name="SearchCourse" type="{http://gunet.gr/Query}SearchCourseType" minOccurs="0"/>
 *         &lt;element name="SearchEvent" type="{http://gunet.gr/Query}SearchEventType" minOccurs="0"/>
 *         &lt;element name="DateFilter" type="{http://gunet.gr/Query}DateFilterType" minOccurs="0"/>
 *         &lt;element name="SortBy" type="{http://gunet.gr/Query}SortByType" minOccurs="0"/>
 *         &lt;element name="SortDirection" type="{http://gunet.gr/Query}SortDirectionType" minOccurs="0"/>
 *         &lt;element name="MaxResults" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="StartAt" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Results" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
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
    "searchTerms",
    "adminFilter",
    "filterResults",
    "filterOperand",
    "requireNotNull",
    "status",
    "requiredSecurityStatus",
    "requiredUserId",
    "requireResource",
    "searchCourse",
    "searchEvent",
    "dateFilter",
    "sortBy",
    "sortDirection",
    "maxResults",
    "startAt",
    "results"
})
@XmlRootElement(name = "Query")
public class Query {

    @XmlElement(name = "SearchTerms")
    protected String searchTerms;
    @XmlElement(name = "AdminFilter")
    protected AdminFilterType adminFilter;
    @XmlElement(name = "FilterResults")
    protected List<FilterResultsType> filterResults;
    @XmlElement(name = "FilterOperand")
    protected OperandType filterOperand;
    @XmlElement(name = "RequireNotNull")
    protected String requireNotNull;
    @XmlElement(name = "Status")
    protected StatusType status;
    @XmlElement(name = "RequiredSecurityStatus")
    protected String requiredSecurityStatus;
    @XmlElement(name = "RequiredUserId")
    protected String requiredUserId;
    @XmlElement(name = "RequireResource")
    protected RequireResourceType requireResource;
    @XmlElement(name = "SearchCourse")
    protected SearchCourseType searchCourse;
    @XmlElement(name = "SearchEvent")
    protected SearchEventType searchEvent;
    @XmlElement(name = "DateFilter")
    protected DateFilterType dateFilter;
    @XmlElement(name = "SortBy")
    protected SortByType sortBy;
    @XmlElement(name = "SortDirection")
    protected SortDirectionType sortDirection;
    @XmlElement(name = "MaxResults")
    protected Integer maxResults;
    @XmlElement(name = "StartAt")
    protected Integer startAt;
    @XmlElement(name = "Results")
    protected Integer results;

    /**
     * Gets the value of the searchTerms property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSearchTerms() {
        return searchTerms;
    }

    /**
     * Sets the value of the searchTerms property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSearchTerms(String value) {
        this.searchTerms = value;
    }

    /**
     * Gets the value of the adminFilter property.
     * 
     * @return
     *     possible object is
     *     {@link AdminFilterType }
     *     
     */
    public AdminFilterType getAdminFilter() {
        return adminFilter;
    }

    /**
     * Sets the value of the adminFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link AdminFilterType }
     *     
     */
    public void setAdminFilter(AdminFilterType value) {
        this.adminFilter = value;
    }

    /**
     * Gets the value of the filterResults property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the filterResults property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFilterResults().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FilterResultsType }
     * 
     * 
     */
    public List<FilterResultsType> getFilterResults() {
        if (filterResults == null) {
            filterResults = new ArrayList<FilterResultsType>();
        }
        return this.filterResults;
    }

    /**
     * Gets the value of the filterOperand property.
     * 
     * @return
     *     possible object is
     *     {@link OperandType }
     *     
     */
    public OperandType getFilterOperand() {
        return filterOperand;
    }

    /**
     * Sets the value of the filterOperand property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperandType }
     *     
     */
    public void setFilterOperand(OperandType value) {
        this.filterOperand = value;
    }

    /**
     * Gets the value of the requireNotNull property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequireNotNull() {
        return requireNotNull;
    }

    /**
     * Sets the value of the requireNotNull property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequireNotNull(String value) {
        this.requireNotNull = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link StatusType }
     *     
     */
    public StatusType getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusType }
     *     
     */
    public void setStatus(StatusType value) {
        this.status = value;
    }

    /**
     * Gets the value of the requiredSecurityStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequiredSecurityStatus() {
        return requiredSecurityStatus;
    }

    /**
     * Sets the value of the requiredSecurityStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequiredSecurityStatus(String value) {
        this.requiredSecurityStatus = value;
    }

    /**
     * Gets the value of the requiredUserId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequiredUserId() {
        return requiredUserId;
    }

    /**
     * Sets the value of the requiredUserId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequiredUserId(String value) {
        this.requiredUserId = value;
    }

    /**
     * Gets the value of the requireResource property.
     * 
     * @return
     *     possible object is
     *     {@link RequireResourceType }
     *     
     */
    public RequireResourceType getRequireResource() {
        return requireResource;
    }

    /**
     * Sets the value of the requireResource property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequireResourceType }
     *     
     */
    public void setRequireResource(RequireResourceType value) {
        this.requireResource = value;
    }

    /**
     * Gets the value of the searchCourse property.
     * 
     * @return
     *     possible object is
     *     {@link SearchCourseType }
     *     
     */
    public SearchCourseType getSearchCourse() {
        return searchCourse;
    }

    /**
     * Sets the value of the searchCourse property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchCourseType }
     *     
     */
    public void setSearchCourse(SearchCourseType value) {
        this.searchCourse = value;
    }

    /**
     * Gets the value of the searchEvent property.
     * 
     * @return
     *     possible object is
     *     {@link SearchEventType }
     *     
     */
    public SearchEventType getSearchEvent() {
        return searchEvent;
    }

    /**
     * Sets the value of the searchEvent property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchEventType }
     *     
     */
    public void setSearchEvent(SearchEventType value) {
        this.searchEvent = value;
    }

    /**
     * Gets the value of the dateFilter property.
     * 
     * @return
     *     possible object is
     *     {@link DateFilterType }
     *     
     */
    public DateFilterType getDateFilter() {
        return dateFilter;
    }

    /**
     * Sets the value of the dateFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link DateFilterType }
     *     
     */
    public void setDateFilter(DateFilterType value) {
        this.dateFilter = value;
    }

    /**
     * Gets the value of the sortBy property.
     * 
     * @return
     *     possible object is
     *     {@link SortByType }
     *     
     */
    public SortByType getSortBy() {
        return sortBy;
    }

    /**
     * Sets the value of the sortBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link SortByType }
     *     
     */
    public void setSortBy(SortByType value) {
        this.sortBy = value;
    }

    /**
     * Gets the value of the sortDirection property.
     * 
     * @return
     *     possible object is
     *     {@link SortDirectionType }
     *     
     */
    public SortDirectionType getSortDirection() {
        return sortDirection;
    }

    /**
     * Sets the value of the sortDirection property.
     * 
     * @param value
     *     allowed object is
     *     {@link SortDirectionType }
     *     
     */
    public void setSortDirection(SortDirectionType value) {
        this.sortDirection = value;
    }

    /**
     * Gets the value of the maxResults property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxResults() {
        return maxResults;
    }

    /**
     * Sets the value of the maxResults property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxResults(Integer value) {
        this.maxResults = value;
    }

    /**
     * Gets the value of the startAt property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getStartAt() {
        return startAt;
    }

    /**
     * Sets the value of the startAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setStartAt(Integer value) {
        this.startAt = value;
    }

    /**
     * Gets the value of the results property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getResults() {
        return results;
    }

    /**
     * Sets the value of the results property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setResults(Integer value) {
        this.results = value;
    }

}
