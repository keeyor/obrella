/* 
     Author: Michael Gatzonis - 4/1/2019 
     OpenDelosDAC
*/
package org.opendelos.vodapp.services.upload;

import java.io.File;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opendelos.model.properties.MultimediaProperties;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.model.resources.ResourceAccess;
import org.opendelos.vodapp.services.resource.ResourceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MultimediaAnalyzeComponent {

    private final ResourceService resourceService;
    private final MultimediaProperties multimediaProperties;

    private StreamingProperties streamingProperties;

    public void setStreamingProperties(StreamingProperties streamingProperties) {
        this.streamingProperties = streamingProperties;
    }

    @Autowired
    public MultimediaAnalyzeComponent(ResourceService resourceService, MultimediaProperties multimediaProperties) {
        this.resourceService = resourceService;
        this.multimediaProperties = multimediaProperties;
    }

    JsonMultimediaPost ValidateCall(String jsonString) throws Exception {

        String mfolder, lecture_id, action, actionData, name;

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, false);

        JsonMultimediaPost jsonMultimediaPost;
        try {

            jsonMultimediaPost = mapper.readValue(jsonString, JsonMultimediaPost.class);

            mfolder     = jsonMultimediaPost.getFolder();
            lecture_id  = jsonMultimediaPost.getId();
            action      = jsonMultimediaPost.getAction();
            actionData  = jsonMultimediaPost.getActionData();
            name        = jsonMultimediaPost.getFilename();
        }
        catch (Exception e) {
            throw new Exception("failed to read post");
        }

        if (mfolder == null || lecture_id == null || action == null || actionData == null || name == null) {
            throw new Exception("failed to read request");
        }

        return jsonMultimediaPost;
    }

    public  ResourceAccess ProcessMultimediaFile(String mmResourceFolder , String filename, String action, String actionData) throws Exception {

        String multStore 	 = streamingProperties.getAbsDir();
        String uploadFolder  = multStore  + mmResourceFolder + File.separator + "_tmp" + File.separator;
        String filePath 	 = uploadFolder + filename;

        JsonResponse ffmpeg_res = VideoProcess.processVideo(filePath,uploadFolder, multimediaProperties.getFfmpeg());
        ResourceAccess ap;

        if (ffmpeg_res.getStatus().equals("FAILED")) {
            String msg = ffmpeg_res.getMessage();
            throw new Exception("ffmpeg failed to parse file:" + msg);
        }
        ap = (ResourceAccess) ffmpeg_res.getResult();
        ap.setFileName(filename);
        ap.setSourceName("");
        ap.setFolder(mmResourceFolder);

        //On Replace with duration constrain: Check Duration of uploaded file.
        if (action.equals("REPLACE_VIDEO_DUC")) {
            long nDuration = DurationToMills(ap.getDuration());			//Duration of new file in milliseconds
            long iDuration = DurationToMills(actionData);					//Duration of old file in milliseconds
            if ((nDuration - iDuration > 500) || (iDuration - nDuration > 500)) {
                String msg = "Το νέο αρχείο έχει διαφορετική διάρκεια από το υφιστάμενο";
                throw new Exception(msg);
            }
        }
        return ap;
    }

    public void GenerateThumbnail(ResourceAccess ra, String mmResourceFolder, String filename) throws Exception {

        /* generate thumbnails */
        FfmpegUtils ffmpeg;
        String ffmpegExecPath = multimediaProperties.getFfmpeg();
        ffmpeg = new FfmpegUtils(ffmpegExecPath);

        String multStore 	 = streamingProperties.getAbsDir();
        String uploadFolder  = multStore  + mmResourceFolder + File.separator + "_tmp" + File.separator;

        String filePath  = uploadFolder + filename;

        if (filename.toLowerCase().endsWith("mp4")) {
            String resolution 	= ra.getResolution();
            String duration    = ra.getDuration();
            int videoWidth 	= Integer.parseInt(resolution.substring(0,resolution.indexOf("x")));
            int videoHeight	= Integer.parseInt(resolution.substring(resolution.indexOf("x")+1));
            int thumbWidth 	=  multimediaProperties.getThumbWidth();
            double shrinkRatio = thumbWidth / (double) videoWidth;
            int thumbHeight 	= (int) (videoHeight * shrinkRatio) ;

            int result = ffmpeg.generateThumbnails(filePath, new File(uploadFolder), duration, thumbWidth, thumbHeight, 1);
            if (result != 1) {
                FileUtils.deleteDirectory(new File(uploadFolder));
                String msg = "Παρουσιάστηκε πρόβλημα κατά τη δημιουργία της μικρογραφίας";
                throw new Exception(msg);
            }
        }
    }

    public int createWatermarkVideo(String mmResourceFolder, String filename) {

        FfmpegUtils ffmpeg;
        String ffmpegExecPath = multimediaProperties.getFfmpeg();
        ffmpeg = new FfmpegUtils(ffmpegExecPath);

        String multStore 	 = streamingProperties.getAbsDir();
        String destFolder   = multStore  + mmResourceFolder + File.separator;

        String filePath   = destFolder + filename;
        String watermark_image_path = multimediaProperties.getWatermark();

        int watermark_async  = ffmpeg.watermarkVideo(destFolder, filePath,watermark_image_path);

        return watermark_async;
    }

    public  void DeleteLeftOversFromPreviousUploads(String  mmResourceFolder) {

        String multStore 	 = streamingProperties.getAbsDir();

        File 	destFolder = new File(multStore  + mmResourceFolder + File.separator);
        //Delete existing Multimedia (FS)
        if (destFolder.isDirectory()) {
            String[] ext = new String[2];
            ext[0] = "mp4";
            ext[1] = "mp3";
            Iterator<File> previousFileList = FileUtils.iterateFiles(destFolder,ext, false);
            while (previousFileList.hasNext()) {
                File videoFile= previousFileList.next();
                FileUtils.deleteQuietly(videoFile);
            }
        }
        //Delete existing Thumbnail (FS)
        String  mediaStore = multimediaProperties.getAbsDir();
        File    mediaFolder = new File(mediaStore + mmResourceFolder + File.separator);
        if (mediaFolder.isDirectory()) {
            String[] ext = new String[2];
            ext[0] = "jpg";
            Iterator<File> previousFileList = FileUtils.iterateFiles(mediaFolder,ext, false);
            while (previousFileList.hasNext()) {
                File imageFile= previousFileList.next();
                FileUtils.deleteQuietly(imageFile);
            }
        }
    }

    public void MoveFilesToDestinationFolder(ResourceAccess ra, String mmResourceFolder, String filename) throws Exception {

        // Move new Files (Video, Thumbnail) to Final Destinations

        String mediaStore    = multimediaProperties.getAbsDir();
        String multStore 	 = streamingProperties.getAbsDir();

        String uploadFolder = multStore  + mmResourceFolder + File.separator + "_tmp" + File.separator;

        File   uploadedFile = new File(uploadFolder + filename);
        File 	destFolder = new File(multStore  + mmResourceFolder + File.separator);


        try {
            FileUtils.moveFileToDirectory(uploadedFile, destFolder, true);
            if (ra.getType().equals("VIDEO")) {
                String fileNameWithOutExt = FilenameUtils.removeExtension(filename);
                File newthumbFile = new File(uploadFolder + fileNameWithOutExt + "-1.jpg");
                FileUtils.moveFileToDirectory(newthumbFile, new File(mediaStore + mmResourceFolder + File.separator), true);
            }
        }
        catch (Exception e) {
            FileUtils.deleteDirectory(new File(uploadFolder));
            String msg = "Παρουσιάστηκε πρόβλημα κατά την αποθήκευση του αρχείου στον τελικό του φάκελο";
            throw new Exception(msg);
        }
    }

    public void UpdateDatabaseSetResourceAccess(String lecture_id, ResourceAccess ra, String mmResourceFolder) throws Exception {
        //Update Db
        String multStore 	 = streamingProperties.getAbsDir();
        String uploadFolder  = multStore  + mmResourceFolder + File.separator + "_tmp" + File.separator;

        try {
            resourceService.updateResourceAccess(lecture_id,ra);
        } catch (Exception e) {
            FileUtils.deleteDirectory(new File(uploadFolder));
            throw new Exception("Database Update Error:" + e.getMessage());
        }
        FileUtils.deleteDirectory(new File(uploadFolder));
    }

    private long DurationToMills(String duration) {

        String[] sdur	   = duration.split(":");
        String hours	   = sdur[0];
        String minutes    = sdur[1];

        int iHours	   = Integer.parseInt(hours);
        int iMinutes	   = Integer.parseInt(minutes);

        String[] secMills  = sdur[2].split("\\.");

        String seconds = secMills[0];

        int iMills = 0;
        if (secMills[1] != null) {
            iMills = Integer.parseInt(secMills[1]);
        }
        int iSeconds	 =  Integer.parseInt(seconds);

        return (long) iHours *60*60*1000 + (long) iMinutes *60*1000 + iSeconds * 1000L + iMills;
    }
}
