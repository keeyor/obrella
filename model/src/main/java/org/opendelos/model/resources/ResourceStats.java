/* 
     Author: Michael Gatzonis - 3/19/2019 
     OpenDelosDAC
*/
package org.opendelos.model.resources;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceStats {

    private int views;

    public ResourceStats() {
    }

    public ResourceStats(int views) {
        this.views = views;
    }
}