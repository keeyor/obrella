//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.11.20 at 10:49:25 AM EET 
//


package org.opendelos.model.calendar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Department implements Serializable {

    protected Periods periods;
    protected Argies argies;
    protected Studies studies;

    public Studies getStudies() {
        if (studies == null) {
            studies = new Studies();
        }
        return studies;
    }

    public static class Studies implements Serializable {
        protected List<Study> study;
        public List<Study> getStudy() {
            if (study == null) {
                study = new ArrayList<>();
            }
            return this.study;
        }
    }

}