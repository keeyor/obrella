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
public class ResourceRealEditingStatus implements Serializable {

    protected String status;
    protected String message;
    protected long startTime;
}
