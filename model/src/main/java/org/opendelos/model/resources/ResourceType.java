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
public class ResourceType implements Serializable {

    private String type;
    private int counter;
}
