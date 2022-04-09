/* 
     Author: Michael Gatzonis - 12/7/2018 
     OpenDelosDAC
*/
package org.opendelos.liveapp.services.upload;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

//import java.util.logging.Logger;

@RestController
public class AnalyzerRestController {

    private final SlidesUploadExecutor slidesUploadExecutor;


    //private Logger logger = Logger.getLogger(AnalyzerRestController.class.getName());

    public AnalyzerRestController(SlidesUploadExecutor slidesUploadExecutor) {
        this.slidesUploadExecutor = slidesUploadExecutor;
    }

    @RequestMapping(value = "/admin/startSlidesProcessing", method = RequestMethod.POST,  consumes = "application/json")
    public @ResponseBody JsonResponse slidesProcessing(@RequestBody String jsonString) throws Exception {

        return  slidesUploadExecutor.ProcessSlides(jsonString);
    }


}


