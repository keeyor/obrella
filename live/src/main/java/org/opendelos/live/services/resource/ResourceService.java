package org.opendelos.live.services.resource;

import java.util.ArrayList;
import java.util.List;

import org.opendelos.live.repository.resource.QueryResourceResults;
import org.opendelos.live.repository.resource.ResourceQuery;
import org.opendelos.live.repository.resource.ResourceRepository;
import org.opendelos.live.services.resource.Utils.ResourceUtils;
import org.opendelos.model.resources.Cuts;
import org.opendelos.model.resources.Presentation;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.ResourceAccess;
import org.opendelos.model.resources.Slide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResourceService {

    @Value("${server.servlet.context-path}")
    String app_path;

    private final Logger logger = LoggerFactory.getLogger(ResourceService.class);
    private final ResourceRepository resourceRepository;

    private final ResourceUtils resourceUtils;

    @Autowired
    public ResourceService(ResourceRepository resourceRepository,ResourceUtils resourceUtils) {
        this.resourceRepository = resourceRepository;
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
    public void removeByIdInCollection(String id, String collectionName) {
        logger.trace(String.format("Resource.findById(%s)", id));
        resourceRepository.deleteFromCollectionById(id,collectionName);
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

    public void updateToCollection(Resource resource, String collection) {
        logger.trace(String.format("Resource.update: %s", resource.getTitle()));
        try {
            resourceRepository.saveToCollection(resource, collection);
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

    public void removeResourceRealEditingPresentation(String id) {
        logger.trace(String.format("Resource.removeResourceRealEditingPresentation: %s", id));
        try {
            Resource resource = this.findById(id);
            resource.setRealEditingPresentation(null);
            update(resource);
        }
        catch (Exception e) {
            logger.error("error: Resource.removeResourceRealEditingPresentation:" + e.getMessage());
        }
    }

    public void acceptResourceRealEditingPresentation(String id) {
        logger.trace(String.format("Resource.removeResourceRealEditingPresentation: %s", id));
        try {
            Resource resource = this.findById(id);
            Presentation realEditingPresentation = resource.getRealEditingPresentation();
            Presentation presentation = resource.getPresentation();

            String actual_duration = realEditingPresentation.getRealDuration();

            presentation.setSlides(realEditingPresentation.getSlides());
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

    public QueryResourceResults searchPageableLectures(ResourceQuery resourceQuery) {
        logger.trace("Lectures.search");
        if (resourceQuery != null) {
            return resourceRepository.searchPageableLectures(resourceQuery);
        }
        else return null;
    }

    public List<Resource> searchLecturesOnFilters(ResourceQuery resourceQuery) {
        logger.trace("Lectures.filters.search");
        if (resourceQuery != null) {
            return resourceRepository.searchLecturesOnFilters(resourceQuery);
        }
        else return null;
    }

    /* used (for now!) to check usage if user in scheduled events as editor or RP. set limit to 1 for quick responses */
    public List<Resource> findAllUserIdReferencesInResources(String editorId, int limit) {
        logger.trace(String.format("Resource.findAllUserIdReferencesInResources(%s)", editorId));
        return resourceRepository.findAllUserIdReferencesInResources(editorId,limit);
    }

}
