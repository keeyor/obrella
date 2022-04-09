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
public class PlayerOptions implements Serializable {

    private Boolean overlay;
    private Boolean showLicenseIntro;

    public PlayerOptions() {
    }

    public PlayerOptions(Boolean overlay, Boolean showLicenseIntro) {
        this.overlay = overlay;
        this.showLicenseIntro = showLicenseIntro;
    }
}