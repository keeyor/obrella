/* 
     Author: Michael Gatzonis - 11/14/2018 
     OpenDelosDAC
*/
package org.opendelos.liveapp.services.upload;


import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SlidesUploadExecutor {


    //private Logger logger = Logger.getLogger(SlidesUploadExecutor.class.getName());

    private final AsyncAnalyzeComponent asyncAnalyzeComponent;

    @Autowired
    public SlidesUploadExecutor(AsyncAnalyzeComponent asyncAnalyzeComponent) {
        this.asyncAnalyzeComponent = asyncAnalyzeComponent;
    }

    public JsonResponse ProcessSlides(String jsonString) throws Exception {

       asyncAnalyzeComponent.setFailedProcess(0); // re-initialize in case of previous error
       asyncAnalyzeComponent.setProcess_status("Running");

       Future<SlideAnalysisDto> initAsync = asyncAnalyzeComponent.InitAnalyzer(jsonString);

       JsonResponse jsonResponse;
        while (true) {
            if (initAsync.isDone() || asyncAnalyzeComponent.isCancelled() || asyncAnalyzeComponent.hasFailed()) {
                if (ThreadCancelledOrFailed()) {
                    jsonResponse = this.createReturnResponse("FAILED", "INIT",initAsync.get().getMsg(),null);
                    asyncAnalyzeComponent.setProcess_status("Failed");
                    initAsync.cancel(true);
                    return jsonResponse;
                }
               break;
            }
        }

        Future<SlideAnalysisDto> parseAsync = asyncAnalyzeComponent.ParseFiles(initAsync.get());

        while (true) {
            if (parseAsync.isDone() || asyncAnalyzeComponent.isCancelled() || asyncAnalyzeComponent.hasFailed()) {
                if (ThreadCancelledOrFailed()) {
                    jsonResponse = this.createReturnResponse("FAILED", "PARSE",initAsync.get().getMsg(),null);
                    asyncAnalyzeComponent.setProcess_status("Failed");
                    parseAsync.cancel(true);
                    return jsonResponse;
                }
                break;
            }
        }


        Future<SlideAnalysisDto> thumbAsync = asyncAnalyzeComponent.CreateThumbnails(parseAsync.get());

        while (true) {
            if (thumbAsync.isDone() || asyncAnalyzeComponent.isCancelled() || asyncAnalyzeComponent.hasFailed()) {
                if (ThreadCancelledOrFailed()) {
                    jsonResponse = this.createReturnResponse("FAILED", "THUMBS",initAsync.get().getMsg(),null);
                    asyncAnalyzeComponent.setProcess_status("Failed");
                    thumbAsync.cancel(true);
                    return jsonResponse;
                }
                break;
            }
        }


        Future<SlideAnalysisDto> mvAsync = asyncAnalyzeComponent.MoveFilesToFinalDir(thumbAsync.get());

        while (true) {
            if (mvAsync.isDone() || asyncAnalyzeComponent.isCancelled() || asyncAnalyzeComponent.hasFailed()) {
                if (ThreadCancelledOrFailed()) {
                    jsonResponse = this.createReturnResponse("FAILED", "MV",mvAsync.get().getMsg(),null);
                    asyncAnalyzeComponent.setProcess_status("Failed");
                    asyncAnalyzeComponent.setFailedProcess(1);
                    mvAsync.cancel(true);
                    return jsonResponse;
                }
                break;
            }
        }


        Future<SlideAnalysisDto> dbAsync = asyncAnalyzeComponent.UpdateDatabase(mvAsync.get());

        while (true) {
            if (dbAsync.isDone() || asyncAnalyzeComponent.isCancelled() || asyncAnalyzeComponent.hasFailed()) {
                if (ThreadCancelledOrFailed()) {
                    jsonResponse = this.createReturnResponse("FAILED", "DB",initAsync.get().getMsg(),null);
                    asyncAnalyzeComponent.setProcess_status("Failed");
                    dbAsync.cancel(true);
                    return jsonResponse;
                }
                jsonResponse = this.createReturnResponse("SUCCESS", "DB", "results", dbAsync.get().getSlides());
                break;
            }
        }

        return jsonResponse;

    }

    private boolean ThreadCancelledOrFailed() {
        if (asyncAnalyzeComponent.isCancelled()) {
            asyncAnalyzeComponent.setProcess_status("Canceled");
            return true;
        }
        else if (asyncAnalyzeComponent.hasFailed()) {
            asyncAnalyzeComponent.setProcess_status("Failed");
            return true;
        }
        return false;
    }

    private JsonResponse createReturnResponse(String Status, String Source, String Message, Object Result) {

        JsonResponse res = new JsonResponse();

        res.setStatus(Status);
        res.setSource(Source);
        res.setMessage(Message);
        res.setResult(Result);

        return res;
    }
}
