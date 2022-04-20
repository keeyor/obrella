/* 
     Author: Michael Gatzonis - 10/28/2018 
     OpenDelosDAC
*/
package org.opendelos.control.mvc.content;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opendelos.control.services.scheduledEvent.ScheduledEventService;
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
    private final ScheduledEventService scheduledEventService;

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    public UploadController(MultimediaProperties multimediaProperties, StreamingProperties streamingProperties, ScheduledEventService scheduledEventService) {
        this.multimediaProperties = multimediaProperties;
        this.streamingProperties = streamingProperties;
        this.scheduledEventService = scheduledEventService;
    }

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

    @RequestMapping(value = "admin/imageUpload" , method = RequestMethod.POST)
    public String formUploadResource(
            @RequestBody MultipartFile file,
            @RequestParam String name,
            @RequestParam(value = "target") String target,
            @RequestParam(value = "id") String id,
            @RequestParam(required=false, defaultValue="-1") int chunks,
            @RequestParam(required=false, defaultValue="-1") int chunk, Locale locale)  {

        String result;

        String uploadPath = multimediaProperties.getEventAbsDir();
        logger.trace("Uploading Image to:" + uploadPath);

        if (!new File(uploadPath).isDirectory()) {
            logger.error("IMAGE_UPLOAD_ERROR: Unknown Upload Path" + uploadPath);
            return "IMAGE_UPLOAD_ERROR: Unknown Upload Path";
        }

        // Check upload file extension
        String _ext = FilenameUtils.getExtension(name).toLowerCase();
        if (!_ext.equals("jpg") && !_ext.equals("jpeg")) {
            logger.error("IMAGE_UPLOAD_ERROR: Unknown extension");
            return "IMAGE_UPLOAD_ERROR: Unknown extension";
        }
        //HANDLE UPLOADED DATA
        logger.trace("Uploading Image Path to:" + uploadPath + "/" + name);

        Path uploadFilePath = new File(uploadPath + "/" + name).toPath();
        if (chunks > 0 && chunk > 0)	// Write or append uploaded chunk to uploaded file
        {
            //Need to append the bytes in this chunk
            try {
                Files.write(uploadFilePath, file.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                FileUtils.deleteQuietly(new File(uploadPath + "/" + name));
                result = e.getMessage();
                logger.error("IMAGE_UPLOAD_ERROR: Uploading APPEND:" + e.getMessage());
                return result;
            }
        }
        else {
            //First chunk: Need to write the bytes in this chunk
            try {
                Files.write(uploadFilePath, file.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException e) {
                FileUtils.deleteQuietly(new File(uploadPath + "/" + name));
                result = e.getMessage();
                logger.error("IMAGE_UPLOAD_ERROR: Uploading CREATE:" + e.getMessage());
                return result;
            }
        }

        if (target.equals("SCHEDULED_EVENT")) {
            //check upload image against width, height constrains
            int maxWidth = multimediaProperties.getImageMaxWidth();
            int maxHeight = multimediaProperties.getImageMaxHeight();
            String eventsWebDir = multimediaProperties.getEventWebDir();

            boolean isImageValidSize;
            try {
                isImageValidSize = isImageValidSize(uploadPath + "/" + name, maxWidth, maxHeight);
                if (isImageValidSize) {
                    moveImageFile2Destination(uploadPath, name, id);
                    scheduledEventService.updatePhotoUrl(id, id + ".jpg");
                    return null;
                }
                else {
                    return "IMAGE_UPLOAD_ERROR: Invalid Dimensions";
                }
            }
            catch (IOException e) {
                FileUtils.deleteQuietly(new File(uploadPath + "/" + name));
                logger.error("IMAGE_UPLOAD_ERROR:" + e.getMessage());
                return e.getMessage();
            }
        }
        else {
            return "IMAGE_UPLOAD_ERROR: Unknown TARGET";
        }
    }

    private void moveImageFile2Destination(String uploadPath, String filename, String targetBaseName) throws IOException {

        try {
                File srcFile = new File(uploadPath + "/" + filename);
                if (srcFile.renameTo(new File(uploadPath + "/" + targetBaseName + ".jpg"))) {
                    srcFile = new File(uploadPath + "/" + targetBaseName + ".jpg");
                    File dstDir = new File(uploadPath + targetBaseName + "/");
                    try {
                        FileUtils.cleanDirectory(dstDir);
                    }
                    catch (Exception ignored) {}
                    FileUtils.moveFileToDirectory(srcFile, dstDir, true);
                }
        }
        catch (IOException ioe) {
            throw new IOException("IMAGE_UPLOAD_ERROR: MoveImageFile2Destination");
        }
    }

    private static boolean isImageValidSize(String srcPath, int maxWidth, int maxHeight) throws IOException {

        try {
            BufferedImage source_image = ImageIO.read(new File(srcPath));
            return (source_image.getWidth() <= maxWidth) && (source_image.getHeight() <= maxHeight);
        }
        catch (IOException ioe) {
            throw  new IOException("IMAGE_UPLOAD_ERROR: isImageValidSize");
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
