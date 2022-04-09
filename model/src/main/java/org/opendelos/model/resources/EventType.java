/* 
     Author: Michael Gatzonis - 3/19/2019 
     OpenDelosDAC
*/
package org.opendelos.model.resources;

import lombok.Getter;

@Getter
public enum EventType {

    seminar,
    conference,
    workshop,
    events,
    play,
    other;

    public String value() {
        return name();
    }

    public static EventType fromValue(String v) {
        return valueOf(v);
    }

}
