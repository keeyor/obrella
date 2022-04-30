//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.26 at 01:38:03 PM EEST 
//


package org.opendelos.legacydomain.query;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SortByType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SortByType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="Relevance"/>
 *     &lt;enumeration value="Title"/>
 *     &lt;enumeration value="Creator"/>
 *     &lt;enumeration value="Time"/>
 *     &lt;enumeration value="Date"/>
 *     &lt;enumeration value="DateModified"/>
 *     &lt;enumeration value="CreatorId"/>
 *     &lt;enumeration value="Editor"/>
 *     &lt;enumeration value="EditorId"/>
 *     &lt;enumeration value="Organization"/>
 *     &lt;enumeration value="OrganizationId"/>
 *     &lt;enumeration value="Unit"/>
 *     &lt;enumeration value="UnitId"/>
 *     &lt;enumeration value="Category"/>
 *     &lt;enumeration value="AcademicYear"/>
 *     &lt;enumeration value="Semester"/>
 *     &lt;enumeration value="Views"/>
 *     &lt;enumeration value="Source"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SortByType")
@XmlEnum
public enum SortByType {

    Relevance,
    Title,
    Creator,
    Time,
    Date,
    DateModified,
    CreatorId,
    Editor,
    EditorId,
    Organization,
    OrganizationId,
    Unit,
    UnitId,
    Category,
    AcademicYear,
    Semester,
    Views,
    Source;

    public String value() {
        return name();
    }

    public static SortByType fromValue(String v) {
        return valueOf(v);
    }

}