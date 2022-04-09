/* 
     Author: Michael Gatzonis - 2/27/2019 
     OpenDelosDAC
*/
package org.opendelos.liveapp.services.upload;

import org.opendelos.model.resources.ResourceAccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MultimediaRestController {

    private final MultimediaAnalyzeComponent multimediaAnalyzeComponent;

    @Autowired
    public MultimediaRestController(MultimediaAnalyzeComponent multimediaAnalyzeComponent) {
        this.multimediaAnalyzeComponent = multimediaAnalyzeComponent;
    }

    @RequestMapping(value = "/admin/startMultimediaProcessing", method = RequestMethod.POST)
    public @ResponseBody JsonResponse multimediaProcessing(@RequestBody String jsonString)  {

        try {
             JsonMultimediaPost jsonMultimediaPost = multimediaAnalyzeComponent.ValidateCall(jsonString);

             String mfolder = jsonMultimediaPost.getFolder();
             String lecture_id = jsonMultimediaPost.getId();
             String action = jsonMultimediaPost.getAction();
             String actionData = jsonMultimediaPost.getActionData();
             String name = jsonMultimediaPost.getFilename();

             ResourceAccess resourceAccess = multimediaAnalyzeComponent.ProcessMultimediaFile(mfolder,name,action,actionData);
             multimediaAnalyzeComponent.GenerateThumbnail(resourceAccess,mfolder,name);
             multimediaAnalyzeComponent.DeleteLeftOversFromPreviousUploads(mfolder);
             multimediaAnalyzeComponent.MoveFilesToDestinationFolder(resourceAccess, mfolder, name);
             multimediaAnalyzeComponent.UpdateDatabaseSetResourceAccess(lecture_id, resourceAccess, mfolder);

             return createReturnResponse("SUCCESS", null,resourceAccess);
        }
        catch(Exception e) {
            return createReturnResponse("FAILED", e.getMessage(),null);
        }
    }

    @RequestMapping(value = "/admin/createWatermark", method = RequestMethod.POST)
    public String  createWatermark(@RequestBody String jsonString)  {

        try {
            JsonMultimediaPost jsonMultimediaPost = multimediaAnalyzeComponent.ValidateCall(jsonString);

            String mfolder = jsonMultimediaPost.getFolder();
            String lecture_id = jsonMultimediaPost.getId();
            String action = jsonMultimediaPost.getAction();
            String actionData = jsonMultimediaPost.getActionData();
            String name = jsonMultimediaPost.getFilename();

            multimediaAnalyzeComponent.createWatermarkVideo(mfolder,name);

            return "success";
        }
        catch(Exception e) {
            return "failed";
        }
    }

    private JsonResponse createReturnResponse(String Status, String Message, Object Result) {

        JsonResponse res = new JsonResponse();

        res.setStatus(Status);
        res.setSource("PROCESS_MULTIMEDIA_FILE");
        res.setMessage(Message);
        res.setResult(Result);

        return res;
    }
}
