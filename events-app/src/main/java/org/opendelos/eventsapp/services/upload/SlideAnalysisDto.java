/* 
     Author: Michael Gatzonis - 2/21/2019 
     OpenDelosDAC
*/
package org.opendelos.eventsapp.services.upload;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.resources.Slide;

@Getter
@Setter
public class SlideAnalysisDto {

    private String resourceId;
    private String resourceFolder;

    private String analAction;
    private String analActionParam;

    private String targetModule;

    private String multimediaDir;
    private String resourcesDir;
    private String tempDir;
    private String uploadDir;
    private String destinationDir;
    private List<String> slidesTitle;
    private List<String> slidesUrls;
    private List<String> slidesTimes;


    private List<Slide> slides;
    private String msg;
}
