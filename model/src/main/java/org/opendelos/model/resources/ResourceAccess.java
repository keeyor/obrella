/* 
     Author: Michael Gatzonis - 3/20/2019 
     OpenDelosDAC
*/
package org.opendelos.model.resources;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceAccess {

    private String aspectRatio;
    private String device;
    private String duration;
    private String format;
    private String quality;
    private String resolution;
    private String type;
    private String sourceName;
    private String folder;
    private String fileName;
    private long filesize;

    public ResourceAccess() {
        aspectRatio = "";
        device = "";
        duration = "0";
        format = "";
        quality = "";
        resolution = "";
        type = "";
        sourceName = "";
        folder = "";
        fileName = "";
        filesize = 0;
    }
}