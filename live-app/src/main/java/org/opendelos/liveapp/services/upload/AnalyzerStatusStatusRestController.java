/* 
     Author: Michael Gatzonis - 12/5/2018 
     OpenDelosDAC
*/
package org.opendelos.liveapp.services.upload;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyzerStatusStatusRestController {

    private final AsyncAnalyzeComponent asyncAnalyzeComponent;

    public AnalyzerStatusStatusRestController(AsyncAnalyzeComponent asyncAnalyzeComponent) {
        this.asyncAnalyzeComponent = asyncAnalyzeComponent;
    }

    @PostMapping(value = "/cancel_process")
    public void cancelImport() {
        asyncAnalyzeComponent.setCancelProcess(1);
    }

    @GetMapping(value = "/status/inProgress")
    public boolean isRunning() {
        return asyncAnalyzeComponent.isRunning();
    }


    @GetMapping(value = "/status/process_status")
    public String fetchState() {
       return  asyncAnalyzeComponent.getStatus();
    }

}
