//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.16 at 10:48:33 AM EET 
//


package org.opendelos.legacydomain.institution;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
	"studyIdentity",	
    "title"
})
@XmlRootElement(name = "Study")
public class Study implements Serializable {

    @XmlElement(name = "StudyIdentity", required = true)
    protected String studyIdentity;
    @XmlElement(name = "StudyTitle", required = true)
    protected String title;
    
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getStudyIdentity() {
		return studyIdentity;
	}
	public void setStudyIdentity(String studyIdentity) {
		this.studyIdentity = studyIdentity;
	}

     
}