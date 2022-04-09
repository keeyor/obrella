/* 
     Author: Michael Gatzonis - 10/28/2018 
     OpenDelosDAC
*/
package org.opendelos.control.mvc.admin.content;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opendelos.model.properties.MultimediaProperties;
import org.opendelos.model.properties.StreamingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class UploadController {

    private final MultimediaProperties multimediaProperties;
    private final StreamingProperties streamingProperties;

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    public UploadController(MultimediaProperties multimediaProperties, StreamingProperties streamingProperties) {
        this.multimediaProperties = multimediaProperties;
        this.streamingProperties = streamingProperties;
    }


/*

    @RequestMapping(value = "/api/v1/resource/slide", method = RequestMethod.POST)
    public ResponseEntity<String> formUploadResource(
            @RequestBody MultipartFile data,
            @RequestParam String name,
            @RequestParam String resourceId,
            @RequestParam String slideIndex,
            @RequestParam String timestamp) throws Exception {

        //TODO: Not Used for now! Use it to record slide and timestamp for synchronization!!!
        logger.info("Just came in:" + slideIndex + " with timestamp:" + timestamp);


        String uploadDir = applicationPropertiesConfig.getSlidesFolder() + resourceId + File.separator;
        String playedSlidesDir = uploadDir + File.separator + "played" + File.separator;
        createFolder(playedSlidesDir);

        logSlideTime(applicationPropertiesConfig.getSlidesFolder() + resourceId + File.separator + "timestamps.log", "slide:" + slideIndex + " time:" + timestamp);


        File currentSlideFile = new File(uploadDir + "currentSlide.jpg");
        if (currentSlideFile.isFile()) {
            FileUtils.moveFile(currentSlideFile, new File(playedSlidesDir + RandomStringUtils.randomAlphanumeric(32) + ".jpg"));
        }
        Path uploadFilePath = new File(uploadDir + "currentSlide.jpg").toPath();
        try {
            Files.write(uploadFilePath, data.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new Exception();
        }

        return new ResponseEntity<>("Saved", HttpStatus.OK);
    }
*/

    @RequestMapping(value = "admin/multimediaUpload", method = RequestMethod.POST)
    public String formUploadResource(
            @RequestBody MultipartFile file,
            @RequestParam String name,
            @RequestParam(required=false, defaultValue="-1") int chunks,
            @RequestParam(required=false, defaultValue="-1") int chunk,
            @RequestParam(value = "m_folder") String m_folder) throws Exception {


        String mediaStore = multimediaProperties.getAbsDir();
        File mediaStoreFolder = new File(mediaStore);

        if (!mediaStoreFolder.isDirectory() || !mediaStoreFolder.canWrite())
        {
            if (!mediaStoreFolder.isDirectory()) {
                logger.error("Media.AbsDir NOT found: " + mediaStoreFolder.getAbsolutePath());
                return "Ο κατάλογος αποθήκευσης εικόνων [attr:media.absdir] ΔΕΝ βρέθηκε";
            }
            else {
                logger.error("Media.AbsDir NOT Writable: " + mediaStoreFolder.getAbsolutePath());
                return "Ο κατάλογος αποθήκευσης εικόνων [attr:media.absdir] ΔΕΝ είναι εγγράψιμος";
            }
        }

        String multStore = streamingProperties.getAbsDir();
        File multStoreFolder = new File(multStore);

        if (!multStoreFolder.isDirectory() || !multStoreFolder.canWrite())
        {
            if (!multStoreFolder.isDirectory())
                return "Ο κατάλογος αποθήκευσης πολυμέσων [attr:dilos.vodps.absdir] ΔΕΝ βρέθηκε";
            else
                return "Ο κατάλογος αποθήκευσης πολυμέσων [attr:dilos.vodps.absdir] ΔΕΝ είναι εγγράψιμος";
        }

        String uploadFolder;

        String _ext = FilenameUtils.getExtension(name).toLowerCase();

        switch (_ext) {
            case "ppt": case "pptx": case "zip":
            case "pdf": case "jpg": case "srt":
                uploadFolder  = mediaStore + m_folder + File.separator + "_tmp" + File.separator;
                break;
            case "mp4":  case "mp3":
                uploadFolder  = multStore  + m_folder + File.separator + "_tmp" + File.separator;
                break;
            default:
                return "Unknown file type";
        }
        boolean folders;

        folders = createFolder(uploadFolder);
        if (!folders) {
            return "Αδύνατη η δημιουργία του φακέλου μεταφόρτωσης";
        }

        Path uploadFilePath = new File(uploadFolder + name).toPath();
        String result=null;
        if (chunks > 0 && chunk > 0)	// Write or append uploaded chunk to uploaded file
        {
            //Need to append the bytes in this chunk
            try {
                Files.write(uploadFilePath, file.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                result = e.getMessage();
            }
        }
        else {
            //First chunk: Need to write the bytes in this chunk
            try {
                Files.write(uploadFilePath, file.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException e) {
                result = e.getMessage();
            }
        }

        if (result==null) {
            return 	"UPLOAD_OK";
        }
        else {
            FileUtils.deleteDirectory(new File(uploadFolder));
            return result;
        }

    }

    private boolean createFolder(String folderName) {

        boolean result = true;
        File folder= new File(folderName);
        if (!folder.isDirectory()) {
            result = folder.mkdirs();
        }
        return result;
    }

    private void logSlideTime(String path, String line) {

        PrintWriter pw = null;

        try {
            File file = new File(path);
            FileWriter fw = new FileWriter(file, true);
            pw = new PrintWriter(fw);
            pw.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

}
