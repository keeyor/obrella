package org.opendelos.control.services.structure;

import java.util.List;
import java.util.Objects;

import org.opendelos.control.services.resource.ResourceService;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.scheduler.Schedule;
import org.opendelos.model.structure.Classroom;
import org.opendelos.model.structure.Device;
import org.opendelos.control.repository.scheduler.ScheduleRepository;
import org.opendelos.control.repository.structure.ClassroomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = "classrooms")
public class ClassroomService {

    private final Logger logger = LoggerFactory.getLogger(ClassroomService.class);

    @Autowired
    CacheManager cacheManager;

    private final ClassroomRepository classroomRepository;
    private final ScheduleRepository scheduleRepository;
    private final ResourceService resourceService;
    private final DepartmentService departmentService;

    @Autowired
    public ClassroomService(ClassroomRepository classroomRepository, ScheduleRepository scheduleRepository, ResourceService resourceService, DepartmentService departmentService) {
        this.classroomRepository = classroomRepository;
        this.scheduleRepository = scheduleRepository;
        this.resourceService = resourceService;
        this.departmentService = departmentService;
    }
    public List<Classroom> findAll() {
        logger.trace("Classroom.findAll");
        return classroomRepository.findAll();
    }
    @CacheEvict(allEntries = true)
    public void deleteAll() {
        logger.trace("Classroom.deleteAll");
        try {
            classroomRepository.deleteAll();
        }
        catch (Exception e) {
            logger.error("error: deleteAll:" + e.getMessage());
        }
    }

    public String create(Classroom classroom) {

        Classroom checkClassroomCode = this.findByCode(classroom.getCode());
        if (checkClassroomCode != null) {
            logger.error("error: Classroom.create:" + "Code eXists");
            return "-1";
        }
        String generatedId= null;
        try {
            Classroom nInstitution =  classroomRepository.save(classroom);
            generatedId = nInstitution.getId();
            logger.trace(String.format("Classroom.created with id: %s:",generatedId));
        }
        catch (Exception e) {
            logger.error("error: Classroom.create:" + e.getMessage());
        }
        return generatedId;
    }
    @Cacheable(key = "#id",unless="#result == null")
    public Classroom findById(String id) {
        logger.trace(String.format("Classroom.findById(%s)", id));
        return  classroomRepository.findById(id).orElse(null);
    }

    public Classroom findByCode(String code) {
        logger.trace(String.format("Classroom.findByCode(%s)", code));
        return classroomRepository.findByCode(code);
    }

    public void findAndUpdate(Classroom classroom) {
        logger.info(String.format("Classroom.findAndUpdate: %s", classroom.getName()));
        try {
            logger.info("Update to:" + classroom.getCalendar());
            classroomRepository.findAndUpdate(classroom);
            Objects.requireNonNull(cacheManager.getCache("classrooms")).evict(classroom.getId());
        }
        catch (Exception e) {
            logger.error("error: Classroom.update:" + e.getMessage());
        }
    }
    @CacheEvict(key = "#classroom.id")
    public void update(Classroom classroom) {
        logger.info(String.format("Classroom.update: %s", classroom.getName()));
        try {
            classroomRepository.save(classroom);
        }
        catch (Exception e) {
            logger.error("error: Classroom.update:" + e.getMessage());
        }
    }
    @CacheEvict(key = "#id")
    public void delete(String id) throws Exception {
        logger.trace(String.format("Classroom.delete: %s", id));
        Classroom classroom = classroomRepository.findById(id).orElse(null);
        if (classroom!= null) {
            boolean RmRefInRes = this.isClassroomReferencedInResource(id);
            if (RmRefInRes) { throw new Exception("_FORBIDDEN_LECTURES"); }
            boolean RmRefInSc = this.isClassroomReferencedInScheduler(id);
            if (RmRefInSc) { throw new Exception("_FORBIDDEN_SCHEDULER"); }
            classroomRepository.deleteById(id);
            departmentService.removeClassroomAssignmentsFromAllDepartment(id);
        }
        else {
            throw new Exception("_NOT_FOUND");
        }
    }

    public List<Classroom> n_findAssignedClassrooms(List<String> departmentIds, String usage) {
        logger.trace("n_assigned_classrooms with usage:" + usage);
        return classroomRepository.n_findAssignedClassrooms(departmentIds,usage);
    }

    public List<Classroom> findAllExcludingIds(List<String> ids) {
        logger.trace(String.format("Classroom.findAllExcludingIds: (%s)", ids.toString()));
        return classroomRepository.findAllExcludingIds(ids);
    }
    public List<Classroom> findAllByUsage(String usage) {
        logger.trace(String.format("Classroom.findAllByUsage: (%s)", usage));
        return classroomRepository.findByUsage(usage);
    }
    public List<Classroom> findAllByCalendar(String enabled) { // "true" or "false"
        logger.trace(String.format("Classroom.findAllByEnabled: (%s)", enabled));
        return classroomRepository.findAllByCalendar(enabled);
    }
    public Classroom findByIdentity(String identity) {
        logger.trace(String.format("Classroom.findByIdentity(%s)", identity));
        return classroomRepository.findByIdentity(identity);
    }

    public long countClassrooms() {
        logger.trace("Classroom.countClassrooms");
        return classroomRepository.count();
    }

    public void SaveClassroomDevice(String id,int idx, Device device) {
        classroomRepository.updateClassroomDevice(id,idx,device);
    }
    public void DeleteClassroomDevice(String id,int idx) {
        classroomRepository.deleteClassroomDevice(id,idx);
    }
    public List<Device> getClassroomDevices(String id) {
        return classroomRepository.getClassroomDevices(id);
    }


    public void setClassroomNameToResults(QueryResourceResults queryResourceResults) {

        for (Resource resource: queryResourceResults.getSearchResultList()) {
            String classroomId = resource.getClassroom();
            Classroom classroom = this.findById(classroomId);
            if (classroom != null && classroom.getName() != null) {
                resource.setClassroomName(classroom.getName());
            }
            else {
                resource.setClassroomName("not found");
            }

        }
    }

    /* used (for now!) to check usage if classroom in resources set limit to 1 for quick responses */
    public boolean isClassroomReferencedInResource(String classroomId) {
        ResourceQuery resourceQuery = new ResourceQuery();
        resourceQuery.setClassroomId(classroomId);
        resourceQuery.setLimit(1);
        List<Resource> resources = resourceService.searchLecturesOnFilters(resourceQuery);
        if (resources.size() == 0) {
            return false;
        }
        else {
            logger.warn("Found at least one Resource where classroom is referenced: " + classroomId);
            return true;
        }
    }
    /* used (for now!) to check usage if classroom in resources set limit to 1 for quick responses */
    public boolean isClassroomReferencedInScheduler(String classroomId) {
        List<Schedule> schedules = scheduleRepository.findAllClassroomReferencesInScheduler(classroomId,1);
        if (schedules.size() == 0) {
            return false;
        }
        else {
            logger.warn("Found at least one Schedule where classroom is referenced: " + classroomId);
            return true;
        }
    }
    public int getClassroomStatus(String classroomId) {
        int res = 0;
        Classroom classroom = this.findById(classroomId);
        if (classroom == null) {
            res = 1;
        }
        else {
            logger.debug("Έλεγχος Αίθουσας:" + classroom.getName() + " " + classroom.getCalendar());
            if (!classroom.getCalendar().equals("true")) {
                res = 2;
            }
        }
        return res;
    }
}
