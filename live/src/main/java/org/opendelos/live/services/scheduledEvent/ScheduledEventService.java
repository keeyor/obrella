package org.opendelos.live.services.scheduledEvent;

import java.util.ArrayList;
import java.util.List;

import org.opendelos.live.repository.resource.ResourceQuery;
import org.opendelos.live.repository.scheduledEvent.ScheduledEventRepository;
import org.opendelos.live.services.opUser.OpUserService;
import org.opendelos.live.services.resource.ResourceService;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.users.OoUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = "scheduledEvents")
public class ScheduledEventService {

    private final Logger logger = LoggerFactory.getLogger(ScheduledEventService.class);

    private final ScheduledEventRepository scheduledEventRepository;
    private final ResourceService resourceService;
    private final OpUserService opUserService;

    @Autowired
    public ScheduledEventService(ScheduledEventRepository scheduledEventRepository, ResourceService resourceService, OpUserService opUserService) {
        this.scheduledEventRepository = scheduledEventRepository;
        this.resourceService = resourceService;
        this.opUserService = opUserService;
    }

    public List<ScheduledEvent> findAll() {
        logger.trace("ScheduledEvent.findAll");
        Sort sort = Sort.by(Sort.Order.desc("startDate"));
        return scheduledEventRepository.findAll(sort);
    }
    @CacheEvict(allEntries = true)
    public void deleteAll() {
        logger.trace("ScheduledEvent.deleteAll");
        try {
            scheduledEventRepository.deleteAll();
        }
        catch (Exception e) {
            logger.error("error: deleteAll:" + e.getMessage());
        }
    }

    public String create(ScheduledEvent scheduledEvent ) {
        String generatedId = null;
        try {
            ScheduledEvent nScheduledEvent =  scheduledEventRepository.save(scheduledEvent);
            generatedId = nScheduledEvent.getId();
            logger.trace(String.format("Course.create: %s", scheduledEvent.getTitle()));
        }
        catch (Exception e) {
            logger.error("error: scheduledEvent.create:" + e.getMessage());
        }
        return generatedId;
    }

    @Cacheable(key = "#id",unless="#result == null")
    public ScheduledEvent findById(String id) {
        logger.trace(String.format("scheduledEvent.findById(%s)", id));
        return scheduledEventRepository.findById(id).orElse(null);
    }

    @CacheEvict(key = "#scheduledEvent.id")
    public void update(ScheduledEvent scheduledEvent) {
        logger.trace(String.format("ScheduledEvent.update: %s", scheduledEvent.getTitle()));
        try {
            scheduledEventRepository.save(scheduledEvent);
        }
        catch (Exception e) {
            logger.error("error: ScheduledEvent.update:" + e.getMessage());
        }
    }
    @CacheEvict(key = "#id")
    public void delete(String id) throws Exception {
        logger.trace(String.format("ScheduledEvent.delete: %s", id));
        //TODO: IMPORTANT !!! (probably) take additional actions when scheduler and calendar are implemented!!!
        ScheduledEvent scheduledEvent = scheduledEventRepository.findById(id).orElse(null);
        if (scheduledEvent!= null) {
            ResourceQuery resourceQuery = new ResourceQuery();
            resourceQuery.setResourceType("EVENT");
            resourceQuery.setEventId(id);
            resourceQuery.setLimit(1);
            List<Resource> resources = resourceService.searchLecturesOnFilters(resourceQuery);
            if (resources.size() == 0) {
                scheduledEventRepository.deleteById(id);
            }
            else {
                throw new Exception("_FORBIDDEN_LECTURES");
            }
        }
        else {
            throw new Exception("_NOT_FOUND");
        }
    }

    public ScheduledEvent findByIdentity(String identity) {
        logger.trace(String.format("ScheduledEvent.findByIdentity(%s)", identity));
        return scheduledEventRepository.findByIdentity(identity);
    }
    public List<ScheduledEvent> findAllByResponsiblePersonId(String id) {
        logger.trace(String.format("ScheduledEvent.findByResponsiblePersonId(%s)", id));
        return scheduledEventRepository.findAllByResponsiblePersonId(id,-1);
    }

    /* used (for now!) to check usage if user in scheduled events as editor or RP. set limit to 1 for quick responses */
    public List<ScheduledEvent> findAllUserIdReferencesInScheduledEvents(String editorId, int limit) {
        logger.trace(String.format("ScheduledEvent.findAllUserIdReferencesInScheduledEvents(%s)", editorId));
        return scheduledEventRepository.findAllUserIdReferencesInScheduledEvents(editorId,limit);
    }

    public List<ScheduledEvent> getAuthorizedScheduledEventsByEditor(OoUserDetails editor, String access_type) {

        List<ScheduledEvent> scheduledEvents = new ArrayList<>();

        if (!access_type.startsWith("prs")) {
            if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SA"))) {
                scheduledEvents = this.findAll();
            }
            else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
                List<String> authorized_unit_ids = opUserService
                        .getManagersAuthorizedUnitIdsByAccessType(editor.getId(), access_type);
                scheduledEvents = scheduledEventRepository.findAllWhereResponsibleUnitInIdList(authorized_unit_ids);
            }
            else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPPORT"))) {
                List<String> authorized_person_ids = opUserService
                        .getSupporterAuthorizedPersonIdsByAccessType(editor.getId(), access_type);
                scheduledEvents = scheduledEventRepository.findAllWhereResponsiblePersonInIdList(authorized_person_ids);
            }
        }
        if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFFMEMBER"))) {
                List<ScheduledEvent> editors_scheduledEvents = scheduledEventRepository.findAllByResponsiblePersonId(editor.getId(), -1);
                for (ScheduledEvent scheduledEvent : editors_scheduledEvents) {
                    if (!containsScheduledEventId(scheduledEvents, scheduledEvent.getId())) {
                        scheduledEvents.add(scheduledEvent);
                    }
                }
        }

        return scheduledEvents;
    }

    public boolean containsScheduledEventId(final List<ScheduledEvent> list, final String id){
        return list.stream().map(ScheduledEvent::getId).anyMatch(id::equals);
    }
}
