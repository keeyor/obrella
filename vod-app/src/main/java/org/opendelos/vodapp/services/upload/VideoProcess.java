/* 
     Author: Michael Gatzonis - 1/28/2019 
     OpenDelosDAC
*/
package org.opendelos.vodapp.services.upload;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.opendelos.model.resources.ResourceAccess;

public class VideoProcess {

    final static String FAILED = "FAILED";
    final static String SUCCESS = "SUCCESS";



    public static JsonResponse processVideo(String videoPath, String videoDirectory, String ffmpeg_path) {


        JsonResponse res = new JsonResponse();
        res.setSource("FFMPEG: ProccessVideo");

        FfmpegUtils ffmpeg;
        ffmpeg = new FfmpegUtils(ffmpeg_path);
        ffmpeg.executeInquiry(videoPath, videoDirectory);

        long filesize = FileUtils.sizeOf(new File(videoPath));

        if (!ffmpeg.getError().equals("0")) {
            res.setStatus(FAILED);
            res.setMessage(ffmpeg.getError());
            res.setResult(null);
            return res;
        }
        ResourceAccess ap = new ResourceAccess();

        ap.setDuration(ffmpeg.getDuration());
        ap.setFilesize(filesize);
        if (videoPath.toLowerCase().endsWith("mp4")) {
            ap.setResolution(ffmpeg.getResolution());
            ap.setAspectRatio(ffmpeg.getAspectRatio());
            ap.setFormat("MP4");
            ap.setType("VIDEO");
        }
        else {
            ap.setResolution("0:0");
            ap.setAspectRatio("16:9");
            ap.setFormat("MP3");
            ap.setType("AUDIO");
        }
        res.setStatus(SUCCESS);
        res.setResult(ap);

        return res;
    }


}
