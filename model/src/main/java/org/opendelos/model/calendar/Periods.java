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
public class Periods implements Serializable {

    protected String refId;
    protected String id;

    protected String inherit;
    protected List<Period> period;

    public List<Period> getPeriod() {
        if (period == null) {
            period = new ArrayList<>();
        }
        return this.period;
    }
}
