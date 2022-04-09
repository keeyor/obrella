/* 
     Author: Michael Gatzonis - 3/19/2019 
     OpenDelosDAC
*/
package org.opendelos.model.resources;

import lombok.Getter;

@Getter
public enum StructureType {

    INSTITUTION,
    SCHOOL,
    DEPARTMENT,
    STAFFMEMBER,
    OTHER;

    public String value() {
        return name();
    }

    public static StructureType fromValue(String v) {
        return valueOf(v);
    }

}
