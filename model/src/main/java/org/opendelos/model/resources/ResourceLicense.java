/* 
     Author: Michael Gatzonis - 3/20/2019 
     OpenDelosDAC
*/
package org.opendelos.model.resources;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceLicense implements Serializable {

    private String identity;

    public ResourceLicense() {
    }

    public ResourceLicense(String identity) {
        this.identity = identity;
    }
}
