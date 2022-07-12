package org.opendelos.control.services.resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.opendelos.control.services.opUser.OpUserService;
import org.opendelos.control.services.upload.SlideAnalysisDto;
import org.opendelos.control.services.scheduledEvent.ScheduledEventService;
import org.opendelos.control.services.structure.CourseService;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.resources.Cuts;
import org.opendelos.model.resources.Presentation;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.ResourceAccess;
import org.opendelos.model.resources.ResourceRealEditingStatus;
import org.opendelos.model.resources.Slide;
import org.opendelos.model.properties.MultimediaProperties;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.control.repository.resource.ResourceRepository;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.model.users.UserAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class ResourceService {

    @Value("${server.servlet.context-path}")
    String app_path;

    private final Logger logger = LoggerFactory.getLogger(ResourceService.class);
    private final ResourceRepository resourceRepository;
    private final StreamingProperties streamingProperties;
    private final MultimediaProperties multimediaProperties;
    private final OpUserService opUserService;

    private final ResourceUtils resourceUtils;

    @Autowired
    public ResourceService(ResourceRepository resourceRepository, StreamingProperties streamingProperties, MultimediaProperties multimediaProperties, OpUserService opUserService, ResourceUtils resourceUtils) {
        this.resourceRepository = resourceRepository;
        this.streamingProperties = streamingProperties;
        this.multimediaProperties = multimediaProperties;
        this.opUserService = opUserService;
        this.resourceUtils = resourceUtils;
    }

    public List<Resource> findAll() {
        logger.trace("Resource.findAll");
        return resourceRepository.findAll();
    }

    public void deleteAll() {
        logger.trace("Resource.deleteAll");
        try {
            resourceRepository.deleteAll();
        }
        catch (Exception e) {
            logger.error("error: deleteAll:" + e.getMessage());
        }
    }

    public void deleteAll(String collectionName) {
        logger.trace("Resource.deleteAll from Collection:" + collectionName);
        try {
            resourceRepository.clearCollection(collectionName);
        }
        catch (Exception e) {
            logger.error("error: deleteAll (in Collection):" + e.getMessage());
        }
    }

    public String create(Resource resource) {
        String generatedId= null;
        resource.setId(null); // ensure that is not empty String
        try {
            Resource nInstitution =  resourceRepository.save(resource);
            generatedId = nInstitution.getId();
            logger.trace(String.format("Resource.created with id: %s:",generatedId));
        }
        catch (Exception e) {
            logger.error("error: Resource.create:" + e.getMessage());
        }
        return generatedId;
    }

    public Resource findById(String id) {
        logger.trace(String.format("Resource.findById(%s)", id));
        return  resourceRepository.findById(id).orElse(null);
    }

    public Resource findByIdInCollection(String id, String collectionName) {
        logger.trace(String.format("Resource.findById(%s)", id));
        return  resourceRepository.findByIdInCollection(id,collectionName);
    }

    public List<Resource> findByStreamNameInCollection(String streamName, String collectionName) {
        logger.trace(String.format("Resource.findByStreamNameInCollection(%s)", streamName));
        return  resourceRepository.findByStreamNameInCollection(streamName,collectionName);
    }

    public Resource findByStreamIdInCollection(String streamId, String collectionName) {
        logger.trace(String.format("Resource.findByStreamIdInCollection(%s)", streamId));
        return  resourceRepository.findByStreamIdInCollection(streamId,collectionName);
    }

    public Resource findLiveStreamByIdOrNameInCollection(String idOrName, String collectionName) {
        logger.trace(String.format("Resource.findLiveStreamByIdOrNameInCollection(%s)", idOrName));

        Resource liveResource = null;
        List<Resource> resourceList = resourceRepository.findLiveStreamByIdOrNameInCollection(idOrName,collectionName);
        if (resourceList != null && resourceList.size() == 1 ) {
            liveResource =resourceList.get(0);
        }
        else if (resourceList != null) {
            Instant _now =  Instant.now();
            for (Resource checkResource: resourceList) {
                Instant startDateTime = checkResource.getDate();
                int broadcast_hour = Integer.parseInt(checkResource.getRealDuration().substring(0,2));
                int broadcast_min = Integer.parseInt(checkResource.getRealDuration().substring(3,5));
                Instant endDateTime	  = startDateTime.plus(broadcast_hour, ChronoUnit.HOURS).plus(broadcast_min, ChronoUnit.MINUTES);
                if (startDateTime.isBefore(_now) && endDateTime.isAfter(_now)) {
                    liveResource = checkResource;
                    break;
                }
            }
        }
        return liveResource;
    }

    public boolean existsById(String id) {
        return resourceRepository.existsById(id);
    }

    public List<Resource> findRelatedCourseResources(Resource resource, String accessPolicy)  {
        return resourceRepository.findRelatedCourseResources(resource,accessPolicy);
    }

    public List<Resource> findRelatedEventResourcesByEventId(String id, String accessPolicy) {
        return resourceRepository.findRelatedEventResourcesByEventId(id,accessPolicy);
    }

     public List<Slide> getResourceSlides(String id) {
        logger.trace(String.format("Resource.getResourceSlides(%s)", id));
        List<Slide> slideList = new ArrayList<>();
        Resource resource = findById(id);
        if (resource != null && resource.getPresentation() != null && resource.getPresentation().getSlides() != null) {
            slideList = resource.getPresentation().getSlides();
        }
        return slideList;
    }

    public Presentation getResourcePresentation(String id) {
        logger.trace(String.format("Resource.getResourcePresentation(%s)", id));
        Resource resource = findById(id);

        Presentation presentation = resource.getPresentation();
        if (presentation == null) { presentation = new Presentation();}

            String storageFolder  = resource.getResourceAccess().getFolder();
            String video_filename = resource.getResourceAccess().getFileName();

            String basePath = resourceUtils.getMultimediaBaseWebPath().toString() + storageFolder + "/";
            presentation.setBasepath(basePath);
            presentation.setLogo(app_path + "/public/images/logos/uoa.png");
            presentation.setTitle(resource.getTitle());
            presentation.setDuration(resource.getResourceAccess().getDuration());
            if (video_filename.toLowerCase().endsWith("mp4")) {
                presentation.setProvider("video");
            }
            else if (video_filename.toLowerCase().endsWith("mp3")) {
                presentation.setProvider("audio");
            }
            presentation.setDate(resource.getDate().toString());
            presentation.setFolder(storageFolder);
            if (resource.getRealDuration() == null) {
                presentation.setRealDuration(resource.getResourceAccess().getDuration());
            }
            String mm_basepath = resourceUtils.getStreamingBaseWebPath().toString();
            presentation.setVideo_url(mm_basepath + resource.getResourceAccess()
                    .getFolder() + "/" + resource.getResourceAccess().getFileName());
            presentation.setFolder(resource.getResourceAccess().getFolder());
            presentation.setFilename(video_filename);

            return presentation;
    }

    public Presentation getResourceRealEditedPresentation(String id) {
        logger.trace(String.format("Resource.getResourceRealEditedPresentation(%s)", id));
        Resource resource = findById(id);
        return resource.getRealEditingPresentation();
    }

    public Resource findByIdentity(String identity) {
        logger.trace(String.format("Resource.findByIdentity(%s)", identity));
        return  resourceRepository.findByIdentity(identity);
    }

    public void update(Resource resource) {
        logger.trace(String.format("Resource.update: %s", resource.getTitle()));
        try {
            resourceRepository.save(resource);
        }
        catch (Exception e) {
            logger.error("error: Resource.update:" + e.getMessage());
        }
    }

    public void updateResourceAccess(String id, ResourceAccess resourceAccess) {
        logger.trace(String.format("Resource.updateResourceAccess: %s", id));
        try {
                Resource resource = this.findById(id);
                resource.setResourceAccess(resourceAccess);
                resource.getStatus().setInclMultimedia(1);
                resource.getStatus().setVideoSource("EDITOR");
                resourceRepository.save(resource);
        }
        catch (Exception e) {
            logger.error("error: Resource.updateResourceAccess:" + e.getMessage());
        }
    }

    public Resource updateViewsAndGetById(String id) {
        logger.trace(String.format("Resource.updateViewsAndGet: %s", id));
        try {
            Resource resource = this.findById(id);
            int views = resource.getStatistics();
            resource.setStatistics(views+1);
            return resourceRepository.save(resource);
        }
        catch (Exception e) {
            logger.error("error: Resource.updateViewsAndGet:" + e.getMessage());
            return null;
        }
    }

    public Resource updateViewsAndGetByIdentity(String identity) {
        logger.trace(String.format("Resource.updateViewsAndGetByIdentity: %s", identity));
        try {
            Resource resource = this.findByIdentity(identity);
            int views = resource.getStatistics();
            resource.setStatistics(views+1);
            return resourceRepository.save(resource);
        }
        catch (Exception e) {
            logger.error("error: Resource.updateViewsAndGet:" + e.getMessage());
            return null;
        }
    }

    public void updateResourceCuts(String id, Cuts cuts,String realDuration, String duration) throws Exception {
        logger.trace(String.format("Resource.updateResourceCuts: %s", id));
        try {
            Resource resource = this.findById(id);
            Presentation presentation = resource.getPresentation();
            if (presentation == null) { presentation = new Presentation();}
            presentation.setCuts(cuts);
            presentation.setRealDuration(realDuration);
            presentation.setDuration(duration);
            resource.setRealDuration(realDuration);
            resource.setPresentation(presentation);
            resourceRepository.save(resource);
        }
        catch (Exception e) {
            throw new Exception("error: Resource.updateResourceAccess:" + e.getMessage());
        }
    }
    public void updateResourceSlides(String id, List<Slide> slides) throws Exception {
        logger.trace(String.format("Resource.updateResourceCuts: %s", id));
        try {
            Resource resource = this.findById(id);
            Presentation presentation = resource.getPresentation();
            presentation.setSlides(slides);
            resource.setPresentation(presentation);
            resourceRepository.save(resource);
        }
        catch (Exception e) {
            throw new Exception("error: Resource.updateResourceAccess:" + e.getMessage());
        }
    }

    public void updateResourcePresentation(SlideAnalysisDto slideAnalysisDto) {

        String id= slideAnalysisDto.getResourceId();
        logger.trace(String.format("Resource.updateResourcePresentation: %s", slideAnalysisDto));
        try {
            Resource resource = this.findById(id);
            if (resource.getPresentation() == null) {
                Presentation presentation = new Presentation();
                List<Slide> slideList = new ArrayList<>();
                presentation.setSlides(slideList);
                resource.setPresentation(presentation);
            }
            resource.getPresentation().setSlides(slideAnalysisDto.getSlides());
            resource.getPresentation().setFolder(slideAnalysisDto.getResourceFolder());
            resource.getStatus().setInclPresentation(1);
            resourceRepository.save(resource);
        }
        catch (Exception e) {
            logger.error("error: Resource.updateResourcePresentation:" + e.getMessage());
        }
    }
    public void updateResourceRealEditingPresentation(String id, Presentation presentation) {
        logger.trace(String.format("Resource.updateResourcePresentation: %s", id));
        try {
            Resource resource = this.findById(id);
            resource.setRealEditingPresentation(presentation);
            resourceRepository.save(resource);
        }
        catch (Exception e) {
            logger.error("error: Resource.updateResourcePresentation:" + e.getMessage());
        }
    }

    public void removeResourceRealEditingPresentation(Resource resource) {
        logger.trace(String.format("Resource.removeResourceRealEditingPresentation: %s", resource.getId()));
        try {
            resource.setRealEditingPresentation(null);
            update(resource);
        }
        catch (Exception e) {
            logger.error("error: Resource.removeResourceRealEditingPresentation:" + e.getMessage());
        }
    }

    public void acceptResourceRealEditingPresentation(Resource resource) {
        logger.trace(String.format("Resource.removeResourceRealEditingPresentation: %s", resource.getId()));
        try {
            Presentation realEditingPresentation = resource.getRealEditingPresentation();
            Presentation presentation = resource.getPresentation();

            String actual_duration = realEditingPresentation.getDuration(); //has the actual cutted duration

            if (realEditingPresentation.getSlides() != null) {
                presentation.setSlides(realEditingPresentation.getSlides());
            }
            //# 16-04-22: just note :: clear cuts -> video has been edited!
            presentation.setCuts(null);
            presentation.setDuration(actual_duration);
            presentation.setRealDuration(actual_duration);

            resource.setRealDuration(actual_duration);
            resource.setRealEditingPresentation(null);

            update(resource);
        }
        catch (Exception e) {
            logger.error("error: Resource.removeResourceRealEditingPresentation:" + e.getMessage());
        }
    }

    public void removeResourceMultimedia(String id) throws Exception {
        try {
            Resource resource = findById(id);
            this.removeResourceMultimedia(resource);
        }
        catch (Exception e) {
                throw new Exception("error removing resource multimedia");
        }
    }

    public void removeResourcePresentation(String id)  throws Exception {
        try {
            Resource resource = findById(id);
            this.removeResourcePresentation(resource);
        }
        catch (Exception e) {
            throw new Exception("error removing resource presentation:" + e.getMessage());
        }
    }

    public void  rmResourceSlide(String id, String index)  throws Exception {
        try {
            Resource resource = findById(id);
            List<Slide> slideList= resource.getPresentation().getSlides();
            boolean removed = slideList.removeIf(slide -> slide.getUrl().equals(index));
            if (removed) {
                resource.getPresentation().setSlides(slideList);
                update(resource);
            }
        }
        catch (Exception e) {
            throw new Exception("error removing slide with url:" + index);
        }
    }
    public void  updateResourceSlideTitle(String id, int position, String title)  throws Exception {
        try {
            Resource resource = findById(id);
            List<Slide> slideList= resource.getPresentation().getSlides();
            slideList.get(position).setTitle(title);
            resource.getPresentation().setSlides(slideList);
            update(resource);
        }
        catch (Exception e) {
            throw new Exception("error removing slide with index:" + position);
        }
    }

    public void delete(String id) {
        logger.trace(String.format("Resource.delete: %s", id));
        try {
             Resource resource = findById(id);
             if (resource.getStatus().getInclMultimedia() == 1) {
                 this.removeResourceMultimedia(resource);
             }
             if (resource.getStatus().getInclPresentation() == 1) {
                 this.removeResourcePresentation(resource);
             }
             resourceRepository.deleteById(id);
        }
        catch (Exception e) {
            logger.error("error: Resource.delete:" + e.getMessage());
        }
    }

    public void deleteThrowingException(String id) throws Exception {

        logger.trace(String.format("Resource.deleteWithThrow: %s", id));

            Resource resource = findById(id);
            if (resource == null) {
                throw new Exception("RESOURCE_NOTFOUND_ERROR");
            }
            if (resource.getAccessPolicy().equals("public")) {
                throw new Exception("RESOURCE_ACCESS_DENIED_ERROR");
            }
            if (resource.isOpenCoursesResource()) {
                throw new Exception("RESOURCE_ACCESS_DENIED_ERROR");
            }
            if (resource.getStatus().getInclMultimedia() == 1) {
                try {
                    this.removeResourceMultimedia(resource);
                }
                catch (Exception e) {
                    throw new Exception("MULTIMEDIA_REMOVE_ERROR");
                }
            }
            if (resource.getStatus().getInclPresentation() == 1) {
                try {
                    this.removeResourcePresentation(resource);
                }
                catch (Exception e1) {
                    throw new Exception("PRESENTATION_REMOVE_ERROR");
                }
            }
            try {
                resourceRepository.deleteById(id);
            }
            catch (Exception e2) {
                throw new Exception("RESOURCE_REMOVE_ERROR");
            }
    }

    public QueryResourceResults searchPageableLectures(ResourceQuery resourceQuery) {
        logger.trace("Lectures.search");
        if (resourceQuery != null) {
            return resourceRepository.searchPageableLectures(resourceQuery);
        }
        else return null;
    }

    public ResourceQuery setAccessRestrictions(ResourceQuery resourceQuery, OoUserDetails editor) {

        resourceQuery.setManagerId(editor.getId());
        if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFFMEMBER"))) {
            resourceQuery.setStaffMember(true);
        }
        if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SA"))) {
            resourceQuery.setSA(true);
            return resourceQuery;
        }
        if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            resourceQuery.setManager(true);
            List<String> authorizedUnits = opUserService.getManagersAuthorizedDepartmentIdsByAccessType(editor.getId(),"content");
            resourceQuery.setAuthorizedUnitIds(authorizedUnits);
        }
        else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPPORT"))) {
            resourceQuery.setSupport(true);
            List<UserAccess.UserRights.CoursePermission> editors_course_support;
            List<UserAccess.UserRights.EventPermission> editors_event_support;

            editors_course_support = opUserService.getManagersCoursePermissionsByAccessType(editor.getId(),"content");
            editors_event_support = opUserService.getManagersEventPermissionsByAccessType(editor.getId(),"content");
            resourceQuery.setAuthorized_courses(editors_course_support);
            resourceQuery.setAuthorized_events(editors_event_support);
        }

        return resourceQuery;
    }


    public List<Resource> searchLecturesOnFilters(ResourceQuery resourceQuery) {
        logger.trace("Lectures.filters.search");
        if (resourceQuery != null) {
            return resourceRepository.searchLecturesOnFilters(resourceQuery);
        }
        else return null;
    }

    public void updateRealTimeEditing(String id, String status, String message, long startTime) {

        Resource resource = this.findById(id);
        ResourceRealEditingStatus rteStatus = resource.getRteStatus();
        if (rteStatus == null) {
            rteStatus = new ResourceRealEditingStatus();
        }
        rteStatus.setStatus(status);
        rteStatus.setMessage(message);
        rteStatus.setStartTime(startTime);
        resource.setRteStatus(rteStatus);
        this.update(resource);
    }

    public void updateAccessPolicy(String id, String status) {

        Resource resource = this.findById(id);
        resource.setAccessPolicy(status);
        this.update(resource);
    }

    public void updateAccessPolicyThrowingError(String id, String status) throws Exception {

        Resource resource = this.findById(id);
        if (resource == null) {
            throw new Exception("RESOURCE_NOTFOUND_ERROR");
        }
        if (status.equals("public") && resource.getAccessPolicy().equals("public")) {
            throw new Exception("RESOURCE_PUBLISH_IGNORE");
        }
        if (status.equals("private") && resource.getAccessPolicy().equals("private")) {
            throw new Exception("RESOURCE_PUBLISH_IGNORE");
        }
        if (status.equals("private") && resource.isOpenCoursesResource()) {
            throw new Exception("RESOURCE_ACCESS_DENIED_ERROR");
        }
        if (status.equals("public") && resource.getStatus().getInclMultimedia() != 1) {
            throw new Exception("RESOURCE_PUBLISH_ERROR");
        }
        resource.setAccessPolicy(status);
        this.update(resource);
    }

    boolean isFirstTimeRealEdited(String id) {
        Resource resource = this.findById(id);
        ResourceRealEditingStatus rteStatus = resource.getRteStatus();
        return rteStatus == null || rteStatus.getStatus() == null || rteStatus.getStatus().equals("_NONE") || rteStatus.getStatus().equals("FLRE") || rteStatus
                .getStatus().equals("");
    }

    private void removeResourcePresentation(Resource resource) throws Exception {
        try {
            Presentation presentation = resource.getPresentation();
            String folder = resource.getPresentation().getFolder(); //!important: save before deletion
            presentation.setSlides(null);
            presentation.setFolder(null);
            resource.setPresentation(presentation);
            resource.getStatus().setInclPresentation(-1);
            update(resource);
            if (folder != null) {   //!Important: be very careful or might delete entire vl dir
                FileUtils.deleteQuietly(new File(multimediaProperties.getAbsDir() + folder + "/slides"));
                //delete presentation folder if not empty
                File presentation_folder = new File(multimediaProperties.getAbsDir() + folder);
                if (FileUtils.listFiles(presentation_folder,null,true).size() == 0) {
                    FileUtils.deleteDirectory(presentation_folder);
                }
            }
        }
        catch (Exception e) {
            throw new Exception("error removing resource presentation:" + e.getMessage());
        }
    }
    public void removeResourceMultimedia(Resource resource) throws Exception {

        try {
            String folder = resource.getResourceAccess().getFolder();  //!important: save before deletion
            String filename = resource.getResourceAccess().getFileName(); //!important: save before deletion
            ResourceAccess resourceAccess = new ResourceAccess();
            resource.setResourceAccess(resourceAccess);
            //## add 16-04-22 -> delete cuts
            if (resource.getPresentation() != null) {
                resource.getPresentation().setDuration(null);
                resource.getPresentation().setRealDuration(null);
                resource.getPresentation().setCuts(null);
            }
            resource.getStatus().setInclMultimedia(-1);
            update(resource);
            if (folder != null) {   //!Important: be very careful or might delete entire vl dir
                File video_file = new File(streamingProperties.getAbsDir() + folder + "/" + filename);
                logger.trace("DELETE video file:" + video_file);
                FileUtils.deleteQuietly(video_file);
                //delete streaming folder if not empty
                File video_dir = new File(streamingProperties.getAbsDir() + folder);
                if (FileUtils.listFiles(video_dir,null,true).size() == 0) {
                    FileUtils.deleteDirectory(video_dir);
                }
                // delete thumbnail
                String name_wExt = filename.substring(0, filename.lastIndexOf("."));
                String thumbnail = name_wExt + "-1.jpg";
                File thumb_file = new File(multimediaProperties.getAbsDir() + folder + "/" + thumbnail);
                logger.trace("DELETE thumb file:" + thumb_file);
                FileUtils.deleteQuietly(thumb_file);
                //delete multimedia folder if not empty (if no presentation uploaded -> dir should be empty)
                File multimedia_dir= new File(multimediaProperties.getAbsDir() + folder);
                if (FileUtils.listFiles(multimedia_dir,null,true).size() == 0) {
                    FileUtils.deleteDirectory(multimedia_dir);
                }
            }
        }
        catch (Exception e) {
            throw new Exception("error removing resource multimedia");
        }
    }

    /* used (for now!) to check usage if user in scheduled events as editor or RP. set limit to 1 for quick responses */
    public List<Resource> findAllUserIdReferencesInResources(String editorId, int limit) {
        logger.trace(String.format("Resource.findAllUserIdReferencesInResources(%s)", editorId));
        return resourceRepository.findAllUserIdReferencesInResources(editorId,limit);
    }
    public long countPublicResourcesByType(String type) {
       return resourceRepository.CountPublicResourcesByType(type);
    }

    public long countCollectionDocuments(String collectionName) {
        return resourceRepository.CountCollectionDocuments(collectionName);
    }

    public boolean isEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
                return !directory.iterator().hasNext();
            }
        }

        return false;
    }
    public long CountResourcesByStaffMemberAsSupervisor(String staffId) {
        return resourceRepository.CountResourcesByStaffMemberAsSupervisor(staffId);
    }
    public long CountScheduledByStaffMemberAsSupervisor(String staffId, String collectionName) {
        return resourceRepository.CountScheduledByStaffMemberAsSupervisor(staffId, collectionName);
    }
    public long CountResourcesByManagerAsEditor(String userId) {
        return resourceRepository.CountResourcesByManagerAsEditor(userId);
    }
    public long CountScheduledByManagerAsEditor(String userId, String collectionName) {
        return resourceRepository.CountScheduledByManagerAsEditor(userId, collectionName);
    }
}
