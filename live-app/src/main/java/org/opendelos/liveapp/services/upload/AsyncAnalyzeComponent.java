/* 
     Author: Michael Gatzonis - 12/23/2018 
     OpenDelosDAC
*/
package org.opendelos.liveapp.services.upload;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.opendelos.liveapp.services.resource.ResourceService;
import org.opendelos.liveapp.services.upload.analyzers.PdfImageExtractor;
import org.opendelos.liveapp.services.upload.analyzers.PowerPointImageExtractor;
import org.opendelos.liveapp.services.upload.analyzers.SlideRecorderExtractor;
import org.opendelos.model.common.SlideRecorder;
import org.opendelos.model.properties.MultimediaProperties;
import org.opendelos.model.resources.Slide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class AsyncAnalyzeComponent {

    private final Logger logger = LoggerFactory.getLogger(AsyncAnalyzeComponent.class);

    private String process_status;
    private String process_state;

    private int cancelProcess = 0;
    private int failedProcess = 0;

    private final String[] accepted_fileExtensions = {"ppt", "pptx","pdf","zip","jpg"};
    private final String[] accepted_imageExtensions = {"jpg", "JPG"};

    private final ResourceService resourceService;
    private final MultimediaProperties multimediaProperties;
    private final PowerPointImageExtractor powerPointImageExtractor;
    private final SlideRecorderExtractor slideRecorderExtractor;

    @Autowired
    public AsyncAnalyzeComponent(ResourceService resourceService, PowerPointImageExtractor powerPointImageExtractor,
            SlideRecorderExtractor slideRecorderExtractor,  MultimediaProperties multimediaProperties) {
        this.resourceService = resourceService;
        this.multimediaProperties = multimediaProperties;
        this.powerPointImageExtractor = powerPointImageExtractor;
        this.slideRecorderExtractor = slideRecorderExtractor;
    }

    boolean isCancelled() {
        return cancelProcess == 1;
    }
    boolean hasFailed() {
        return failedProcess == 1;
    }


    @Async("asyncAnalyzeExecutor")
    public Future<SlideAnalysisDto> InitAnalyzer(String jsonString) {

        this.process_status = "Αρχικοποίηση";
        this.cancelProcess = 0;
        this.failedProcess = 0;

        SlideAnalysisDto sadto = new SlideAnalysisDto();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, false);

        JsonSlidesPost jsonSlidesPost;

        try {
            jsonSlidesPost = mapper.readValue(jsonString, JsonSlidesPost.class);

            String resourceId = jsonSlidesPost.getId();
            String resourceFolder = jsonSlidesPost.getFolder();
            String analAction = jsonSlidesPost.getAction();
            String analActionParam = jsonSlidesPost.getActionData();
            String targetModule = jsonSlidesPost.getModule();

            Objects.requireNonNull(resourceId, "invalid resource id");
            Objects.requireNonNull(resourceFolder, "invalid resource folder");
            Objects.requireNonNull(analAction, "invalid analysis action");
            Objects.requireNonNull(targetModule, "invalid target module");

            String globalMediaStore  = multimediaProperties.getAbsDir();
            sadto.setDestinationDir(this.getDestinationDirectory(resourceFolder, targetModule, globalMediaStore));
            sadto.setUploadDir(globalMediaStore  + resourceFolder + File.separator + "_tmp" + File.separator);
            sadto.setResourceId(resourceId);
            sadto.setResourceFolder(resourceFolder);
            sadto.setAnalAction(analAction);
            sadto.setAnalActionParam(analActionParam);
            sadto.setTargetModule(targetModule);
            sadto.setMsg("Init success");
            setFailedProcess(0);
        }
        catch (NullPointerException | IOException e) {
            sadto.setMsg("Invalid post request:" + e.getMessage());
            setFailedProcess(1);
            return new AsyncResult<>(sadto);
        }
        return  new AsyncResult<>(sadto);
    }

    @Async("asyncAnalyzeExecutor")
    public Future<SlideAnalysisDto> ParseFiles(SlideAnalysisDto sadto)  throws Exception {

        this.process_status = "Ανάγνωση";

        String uploadDir = sadto.getUploadDir();
        File uploadFolder = new File(uploadDir);


        int upload_counter=0;
        String status;
        String tmpFolder = uploadDir;

        Iterator<File> uploadFileList = FileUtils.iterateFiles(uploadFolder,accepted_fileExtensions, false);
        while (uploadFileList.hasNext()) {

            upload_counter++;
            File srcFile= uploadFileList.next();
            String srcFileName = srcFile.getName();
            String _ext = FilenameUtils.getExtension(srcFileName);

            switch (_ext.toLowerCase()) {
                case "ppt":
                case "pptx": {
                    List<Slide> slides;
                    String uploadedFile = uploadDir + File.separator + srcFileName;
                    tmpFolder = uploadDir + FilenameUtils.getBaseName(srcFileName) + File.separator;
                    FileUtils.forceMkdir(new File(tmpFolder));

                    if (_ext.equalsIgnoreCase("ppt")) {
                        try {
                            slides = powerPointImageExtractor.extractImagesHSLF(uploadedFile, tmpFolder);
                        }
                        catch (Exception e) {
                            logger.error("Invalid PPT:" + e.getMessage());
                            sadto.setMsg("Invalid PPT:" + e.getMessage());
                            setFailedProcess(1);
                            return new AsyncResult<>(sadto);
                        }
                    } else {
                        try {
                            slides = powerPointImageExtractor.extractImagesXSLF(uploadedFile, tmpFolder);
                        }
                        catch (Exception e) {
                            logger.error("Invalid PPTX:" + e.getMessage());
                            sadto.setMsg("Invalid PPTX:" + e.getMessage());
                            setFailedProcess(1);
                            return new AsyncResult<>(sadto);
                        }
                    }
                     sadto.setSlides(slides);
                    break;
                }
                case "zip": {
                    tmpFolder = uploadDir + "slides" + File.separator;
                    FileUtils.deleteQuietly(new File(tmpFolder)); // delete previous tries if any...
                    String filepath = uploadDir + File.separator + srcFileName;
                    FileUtils.forceMkdir(new File(tmpFolder));
                    unzipFile(filepath,new File(tmpFolder));
                    //zip file may contain another slides folder when unzipped (i.e. slide recorder). Move files in that 'slides' dir to parent 'slides' dir
                    if (new File(tmpFolder + "/slides").isDirectory()) {
                        Iterator<File> upFileList = FileUtils.iterateFiles(new File(tmpFolder + "/slides"), accepted_imageExtensions, false);
                        while (upFileList.hasNext()) {
                            FileUtils.moveFileToDirectory(upFileList.next(), new File(uploadDir + "slides"), false);
                        }
                        //Read slideSync.xml if exists.
                        File slideRecorderSyncFile = new File(tmpFolder + "/slideSync.xml");
                        if (slideRecorderSyncFile.isFile()) {
                            SlideRecorder slideRecorder = null;
                            List<Slide> slidesList = new ArrayList<>();
                            try {
                                slideRecorder = readSlideRecorderFile(slideRecorderSyncFile); //in case of errors reading sync file
                            }
                            catch (Exception ignored) {}
                            if (slideRecorder != null && slideRecorder.getSlides() != null) {
                               for (SlideRecorder.Slides.Slide slideRecorderSlide : slideRecorder.getSlides()
                                       .getSlide()) {
                                   Slide slide = new Slide();
                                   String slide_url_in_xml = slideRecorderSlide.getUrl();
                                   slide.setUrl(slide_url_in_xml.substring(slide_url_in_xml.indexOf("/") + 1));
                                   slide.setTime(slideRecorderSlide.getTime());
                                   if (slideRecorderSlide.getTitle() != null) {
                                       slide.setTitle(slideRecorderSlide.getTitle());
                                   }
                                   else {
                                       slide.setTitle("");
                                   }
                                   slidesList.add(slide);
                               }
                               sadto.setSlides(slidesList);
                           }
                        }
                    }
                    break;
                }
                case "pdf": {
                    String uploadedFile = uploadDir + File.separator + srcFileName;
                    tmpFolder = uploadDir + FilenameUtils.getBaseName(srcFileName) + File.separator;
                    FileUtils.forceMkdir(new File(tmpFolder));
                    PdfImageExtractor ex = new PdfImageExtractor();

                    status = ex.read_pdf(uploadedFile, tmpFolder);
                    FileUtils.deleteQuietly(new File(uploadedFile));
                    if (!status.equals("")) {
                        FileUtils.deleteDirectory(new File(uploadDir));
                        sadto.setMsg("Invalid File (PDF):" + " could not read file");
                        setFailedProcess(1);
                        return new AsyncResult<>(sadto);
                    } else {
                        this.renameUploadFiles(tmpFolder);
                    }
                    break;
                }
                case "jpg": {
                    tmpFolder = uploadDir + "slides" + File.separator;
                    String filepath = uploadDir + File.separator + srcFileName;
                    FileUtils.forceMkdir(new File(tmpFolder));
                    String index = "s";
                    if (upload_counter < 10) index = "s0";
                    index = index + upload_counter;
                    String renameTo = RandomStringUtils.randomAlphanumeric(32);
                    renameTo = index + "_" + renameTo + ".jpg";
                    FileUtils.moveFile(new File(filepath), new File(tmpFolder + renameTo));

                    break;
                }
                default: {
                    String filepath = uploadDir + File.separator + srcFileName;
                    FileUtils.deleteQuietly(new File(filepath));
                    FileUtils.deleteDirectory(new File(uploadDir));
                    String msg = "Άγνωστος τύπος αρχείου";
                    sadto.setMsg("Invalid file:" + msg);
                    setFailedProcess(1);
                    return new AsyncResult<>(sadto);
                }
            }

        }
        sadto.setTempDir(tmpFolder);
        return new AsyncResult<>(sadto);
    }

    @Async("asyncAnalyzeExecutor")
    public Future<SlideAnalysisDto> CreateThumbnails(SlideAnalysisDto sadto)  throws Exception {

        this.process_status = "Δημιουργία Μικρογραφιών";

        String tmpFolder = sadto.getTempDir();
        //Iterate through tmpfolder and get all JPG files
        String uploadFolder = sadto.getUploadDir();
        File uploadDir = new File(uploadFolder);

        Iterator<File> upFileList = FileUtils.iterateFiles(new File(tmpFolder), accepted_imageExtensions, false);

        while (upFileList.hasNext()) {

            File srcFile = upFileList.next();
            String srcFileName = srcFile.getName();

            try {

                String source_image_path = tmpFolder + srcFileName;
                BufferedImage source_image = ImageIO.read(new File(source_image_path));
                boolean allowedSize = PresentationUtils.allowedSlideSize(source_image.getWidth(), source_image.getHeight());
                if (!allowedSize) {
                    String status = "Μή αποδεκτή αναλογία απεικόνισης (aspect ratio)";
                    logger.error(status);
                    FileUtils.deleteDirectory(uploadDir);
                    sadto.setMsg(status);
                    setFailedProcess(1);
                    return  new AsyncResult<>(sadto);
                }
                //Resize to desired size and save
                int slideWidth = multimediaProperties.getSlideWidth();
                double ImageAspectRatio = 800 / (double) source_image.getWidth();
                int slideHeight = (int) (source_image.getHeight() * ImageAspectRatio);

                PresentationUtils.saveImageToDesiredSizeFromImagePath(tmpFolder,tmpFolder,srcFileName, slideWidth, slideHeight);

                //Create thumbnail to desired size
                int thumbWidth = multimediaProperties.getThumbWidth();
                double ThumbAspectRatio = thumbWidth / (double) source_image.getWidth();
                int thumbHeight = (int) (source_image.getHeight() * ThumbAspectRatio);

                String thumbnail_path = tmpFolder + "small" + File.separator;
                PresentationUtils.saveImageToDesiredSizeFromImagePath(tmpFolder, thumbnail_path,  srcFileName, thumbWidth, thumbHeight);
            }
            catch (Exception e) {
                FileUtils.deleteDirectory(uploadDir);
                sadto.setMsg(e.getMessage());
                failedProcess = 1;
                return  new AsyncResult<>(sadto);
            }
            //finalFileList.add(srcFileName);
        }

        //sadto.setSlidesUrls(finalFileList);

        return new AsyncResult<>(sadto);
    }

    @Async("asyncAnalyzeExecutor")
    public Future<SlideAnalysisDto> MoveFilesToFinalDir(SlideAnalysisDto sadto)  throws Exception {

        this.process_status = "Μετακίνηση Αρχείων";

        String tmpFolder = sadto.getTempDir();
        String uploadDir = sadto.getUploadDir();
        File uploadFolder = new File(uploadDir);
        String destFolderStr = sadto.getDestinationDir();

        File destFolder = new File(destFolderStr);

        //so far: files uploaded to _tmp and thumbs at _tmp/small
        // MOVE Images & Thumbs to final Dir
        if (sadto.getSlides() == null) {
            //FileUtils.moveDirectory(new File(tmpFolder), new File(destFolderStr));
            //Iterate through tmpfolder and get all JPG files
            List<Slide> slidesList = new ArrayList<>();
            String[] ext_jpg = new String[2];
            ext_jpg[0] = "jpg";
            ext_jpg[1] = "JPG";
            Iterator<File> upFileList = FileUtils.iterateFiles(new File(tmpFolder),ext_jpg,false);
            while (upFileList.hasNext()) {
                File srcFile= upFileList.next();
                Slide slide = new Slide();
                slide.setUrl(srcFile.getName());
                slide.setTime("-1");
                slide.setTitle("");
                slidesList.add(slide);
            }
            sadto.setSlides(slidesList);
        }

        for (Slide s : sadto.getSlides()) {
                File imageFile = new File(tmpFolder + File.separator + s.getUrl());
                FileUtils.moveFileToDirectory(imageFile, destFolder, true);
                File thumbFile = new File(tmpFolder + File.separator + "small/" + s.getUrl());
                FileUtils.moveFileToDirectory(thumbFile, new File(destFolderStr + "small/"), true);
        }

        FileUtils.deleteDirectory(uploadFolder);

        return new AsyncResult<>(sadto);
    }

    @Async("asyncAnalyzeExecutor")
    public Future<SlideAnalysisDto> UpdateDatabase(SlideAnalysisDto sadto)  {

        this.process_status = "Ενημέρωση Βάσης Δεδομένων";

         this.setPresentation(sadto);

        this.process_status = "Ολοκλήρωση";

        return new AsyncResult<>(sadto);
    }


    public String getStatus() {
        return process_status;
    }

    public Boolean isRunning() {

        boolean isImportRunning;
        String status = getStatus();
        isImportRunning = status.startsWith("Running");

        return isImportRunning;
    }

    private void renameUploadFiles(String upDirectory) throws IOException {

        Iterator<File> uploadFileList = FileUtils.iterateFiles(new File(upDirectory),accepted_imageExtensions, false);

        while (uploadFileList.hasNext()) {
            File srcFile= uploadFileList.next();
            String index = "s" + srcFile.getName().substring(5, 7);
            String renameTo = RandomStringUtils.randomAlphanumeric(32);
            renameTo = index + "_" + renameTo + ".jpg";
            FileUtils.moveFile(srcFile, new File(upDirectory +  renameTo));
        }

    }
    private String getDestinationDirectory(String resourceIdentity, String targetModule, String globalMediaStore) {

        String destinationDir;
        if (targetModule == null || targetModule.equals("")) {
            destinationDir = globalMediaStore  + resourceIdentity + File.separator + "slides" + File.separator;
        }
        else {
            destinationDir = globalMediaStore  + "_Repo" + File.separator + resourceIdentity + File.separator + "slides" +  File.separator;

        }
        return  destinationDir;
    }


    private void setPresentation(SlideAnalysisDto sadto) {

        String status="";
        try {
            if (sadto.getTargetModule() == null || sadto.getTargetModule().equals(""))
            {
                if (!sadto.getAnalAction().equals("REPLACE_IMAGE"))
                    resourceService.updateResourcePresentation(sadto);
                else
                    status = "0"; // _dlmService.replaceSideInPresentation(rid,uploadedFileList,action_data);
            }
            else
            if (!sadto.getAnalAction().equals("REPLACE_IMAGE"))
                status =  "0"; //_dlmService.setSchedulerPresentationSlides(rid,uploadedFileList, slideTitles,slideTimes);
            else {

                status =  "0"; //_dlmService.replaceSideInCoursePresentation(rid,uploadedFileList,action_data);
            }

        }
        catch (Exception e) {

            status = "ERR.PU.DB.u";
        }

    }

    private void unzipFile(String scrFile, File destDir) throws IOException {

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(scrFile));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }
                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public SlideRecorder readSlideRecorderFile(File syncFile) throws Exception {

        String status = "0";

        try {
            SlideRecorder slideRecorder;

            JAXBContext jc = JAXBContext
                    .newInstance(SlideRecorder.class);
            Unmarshaller um = jc.createUnmarshaller();
            StringBuffer xmlStr = new StringBuffer(FileUtils.readFileToString(syncFile, "UTF-8"));
            slideRecorder = (SlideRecorder) um
                    .unmarshal(new StreamSource(new StringReader(xmlStr
                            .toString())));
            return slideRecorder;
        }
        catch (Exception e) {
            throw new Exception("[Error]:" + "Extracting Times from SlideSync file");
        }
    }
}
