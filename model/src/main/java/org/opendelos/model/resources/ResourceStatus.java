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
public class ResourceStatus  implements Serializable {

    protected int inclMultimedia;
    protected int inclPresentation;
    protected String videoSource;

    public ResourceStatus() {
    }

    public ResourceStatus(int inclMultimedia, int inclPresentation, String videoSource) {
        this.inclMultimedia = inclMultimedia;
        this.inclPresentation = inclPresentation;
        this.videoSource = videoSource;
    }


}
