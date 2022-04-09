/* 
     Author: Michael Gatzonis - 3/19/2019 
     OpenDelosDAC
*/
package org.opendelos.model.resources;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessPolicy implements Serializable {

    private String access;
    private int counter;
}
