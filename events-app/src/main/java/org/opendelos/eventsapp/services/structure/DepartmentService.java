package org.opendelos.eventsapp.services.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.opendelos.model.calendar.Period;
import org.opendelos.model.common.Select2GenChild;
import org.opendelos.model.common.Select2GenGroup;
import org.opendelos.model.dates.CustomPause;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.School;
import org.opendelos.eventsapp.repository.delos.OpUserRepository;
import org.opendelos.eventsapp.repository.structure.CourseRepository;
import org.opendelos.eventsapp.repository.structure.DepartmentRepository;
import org.opendelos.eventsapp.repository.structure.InstitutionRepository;
import org.opendelos.eventsapp.repository.structure.StudyProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
//@CacheConfig(cacheNames = "departments")
public class DepartmentService {

    private final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final OpUserRepository opUserRepository;
    private final StudyProgramRepository studyProgramRepository;
    private final InstitutionRepository institutionRepository;
    private final SchoolService schoolService;

    //@Autowired
    //CacheManager cacheManager;

    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository, CourseRepository courseRepository, OpUserRepository opUserRepository, StudyProgramRepository studyProgramRepository, InstitutionRepository institutionRepository, SchoolService schoolService) {
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.opUserRepository = opUserRepository;
        this.studyProgramRepository = studyProgramRepository;
        this.institutionRepository = institutionRepository;
        this.schoolService = schoolService;
    }

    public List<Department> findAll() {
        logger.trace("Department.findAll");
        return departmentRepository.findAll();
    }

    //@CacheEvict(allEntries = true)
    public void deleteAll() {
        logger.trace("Department.deleteAll");
        try {
            departmentRepository.deleteAll();
        }
        catch (Exception e) {
            logger.error("error: Department.deleteAll:" + e.getMessage());
        }
    }

    public String create(Department department) throws Exception {

        String generatedId;
        department.setIdentity(department.getIdentity().toLowerCase());
        if (departmentRepository.findByIdentity(department.getIdentity()) == null) {
            department.setId(null);
            Department nDepartment =  departmentRepository.save(department);
            generatedId = nDepartment.getId();
            logger.trace(String.format("Department.created with id: %s:",generatedId));
        }
        else {
            throw new Exception("_DUPLICATE_IDENTITY");
        }
        return generatedId;
    }

    //@Cacheable(key = "#id",unless="#result == null")
    public Department findById(String id) {
        logger.trace(String.format("Department.findById(%s)", id));
        return departmentRepository.findById(id).orElse(null);
    }

    public List<Department> findBySchoolId(String id) {
        logger.trace(String.format("Department.findBySchoolId(%s)", id));
        return departmentRepository.findAllBySchoolId(id);
    }

    //@CacheEvict(key = "#department.id")
    public void update(Department department) {
        try {
            departmentRepository.save(department);
            logger.trace(String.format("Department.update: %s", department.getTitle()));
        }
        catch (Exception e) {
            logger.error("error: Department.update:" + e.getMessage());
        }
    }
    //@CacheEvict(key = "#department.id")
    public void findAndUpdate(Department department) {
        try {
            departmentRepository.findAndUpdate(department);
            /* Courses */
            long updateCoursesDepartments = departmentRepository.updateCoursesDepartment(department.getId(),
                                                department.getIdentity(),department.getTitle());
            /* StaffMembers */
            long updatedSMDepartments = departmentRepository.updateStaffMembersDepartment(department.getId(),
                                                                department.getIdentity(),department.getTitle());
            long updatedSMCourses = departmentRepository.updateStaffMembersCourseDepartment(department.getId(),
                                                                department.getIdentity(),department.getTitle());
            /* Scheduled Events */
            long updatedSERUDepartments = departmentRepository.updateScheduledEventsResponsibleUnitsDepartment(department.getId(),
                                                                department.getIdentity(),department.getTitle());
            long updatedSERPDepartments = departmentRepository.updateScheduledEventsResponsiblePersonDepartment(department.getId(),
                                                                department.getIdentity(),department.getTitle());
            long updatedSEEditorDepartments = departmentRepository.updateScheduledEventsEditorDepartment(department.getId(),
                                                                department.getIdentity(),department.getTitle());
            /* Resourcess */
            long updatedResourcesDepartments = departmentRepository.updateResourcesDepartment(department.getId(),
                                                                    department.getIdentity(),department.getTitle());
            long updatedResourcesSupervisorDepartments = departmentRepository.updateResourcesSupervisorDepartment(department.getId(),
                                                                    department.getIdentity(),department.getTitle());
            long updatedResourcesEditorDepartments = departmentRepository.updateResourcesEditorDepartment(department.getId(),
                                                                    department.getIdentity(),department.getTitle());
            long updatedResourcesCourseDepartments = departmentRepository.updateResourcesCourseDepartment(department.getId(),
                                                                    department.getIdentity(),department.getTitle());
            long updatedResourcesSERPDepartment = departmentRepository.updateResourcesScheduledEventResponsiblePersonDepartment(department.getId(),
                                                                    department.getIdentity(),department.getTitle());
            long updatedResourcesSERUDepartment = departmentRepository.updateResourcesScheduledEventsResponsibleUnitsDepartment(department.getId(),
                                                                    department.getIdentity(),department.getTitle());
            long updatedResourcesSEEditorDepartment = departmentRepository.updateResourcesScheduledEventsEditorDepartment(department.getId(),
                                                                    department.getIdentity(),department.getTitle());
            logger.info(String.format("Department.updated Courses departments: %s", updateCoursesDepartments));
            logger.info(String.format("Department.updated SM departments: %s", updatedSMDepartments));
            logger.info(String.format("Department.updated SM courses: %s", updatedSMCourses));
            logger.info(String.format("Department.updated SE RU departments: %s", updatedSERUDepartments));
            logger.info(String.format("Department.updated SE RP departments: %s", updatedSERPDepartments));
            logger.info(String.format("Department.updated SE Editor departments: %s", updatedSEEditorDepartments));

            logger.info(String.format("Department.updated Resources department: %s", updatedResourcesDepartments));
            logger.info(String.format("Department.updated Resources Supervisor department: %s", updatedResourcesSupervisorDepartments));
            logger.info(String.format("Department.updated Resources Editor department: %s", updatedResourcesEditorDepartments));
            logger.info(String.format("Department.updated Resources Course department: %s", updatedResourcesCourseDepartments));
            logger.info(String.format("Department.updated Resources Event RP department: %s", updatedResourcesSERPDepartment));
            logger.info(String.format("Department.updated Resources Event RU departments: %s", updatedResourcesSERUDepartment));
            logger.info(String.format("Department.updated Resources Event Editor departments: %s", updatedResourcesSEEditorDepartment));
            //Objects.requireNonNull(cacheManager.getCache("departments")).evict(department.getId());
        }
        catch (Exception e) {
            logger.error("error: Department.findAndUpdate:" + e.getMessage());
        }
    }

    //@CacheEvict(key = "#id")
    public void delete(String id) throws Exception {
        //TODO: IMPORTANT !!! (probably) take additional actions when scheduler and calendar are implemented!!!
        Department department = departmentRepository.findById(id).orElse(null);
        if (department!= null) {
            if (studyProgramRepository.findByDepartmentId(id).size() == 0) {
                if (courseRepository.findAllByDepartmentId(id).size() == 0) {
                    if (opUserRepository.findAllStaffMembersTeachingInDepartment(id).size() == 0) {
                        departmentRepository.deleteById(id);
                    }
                    else {
                        throw new Exception("_FORBIDDEN_STAFF");
                    }
                }
                else {
                    throw new Exception("_FORBIDDEN_COURSES");
                }
            }
            else {
                throw new Exception("_FORBIDDEN_STUDYPROGRAMS");
            }
        }
        else {
            throw new Exception("_NOT_FOUND");
        }
    }

    public Department findByIdentity(String identity) {
        logger.trace(String.format("Department.findByIdentity(%s)", identity));
        return departmentRepository.findByIdentity(identity);
    }

    public List<String> findClassroomIdsById(String id) {
        logger.trace(String.format("Department.findClassroomsIdsByIdentity(%s)", id));
        return departmentRepository.getAllClassrooms(id);
    }
    //@CacheEvict(key = "#departmentId")
    public void assignRoomsToDepartment(String departmentId, String[] roomsIds) {
       departmentRepository.AssignRoomsToDepartment(departmentId,roomsIds);
    }
    //@CacheEvict(key = "#departmentId")
    public void unAssignRoomFromDepartment(String departmentId, String roomId) {
        departmentRepository.UnAssignRoomFromDepartment(departmentId,roomId);
    }
    //@CacheEvict(key = "#departmentId")
    public void addClassroomToDepartment(String departmentId, String classroomId) {
        departmentRepository.addClassroomToDepartment(departmentId,classroomId);
    }
    //@CacheEvict(key = "#departmentId")
    public void removeClassroomFromDepartment(String departmentId, String classroomId) {
        departmentRepository.deleteClassroomFromDepartment(departmentId,classroomId);
    }
    //@CacheEvict(allEntries = true)
    public void removeClassroomAssignmentsFromAllDepartment(String classroomId) {
        departmentRepository.removeClassroomAssignmentsFromAllDepartments(classroomId);
    }
    /* Calendar  */
    //@CacheEvict(key = "#id")
    public void saveCustomPeriod(String id, CustomPeriod customPeriod){
        departmentRepository.saveCustomPeriod(id,customPeriod);
    }
    //@CacheEvict(key = "#id")
    public void deleteCustomPeriod(String id, String year) {
        departmentRepository.deleteCustomPeriod(id,year);
    }
    public List<CustomPeriod> getCustomPeriods(String id) {
        return  departmentRepository.getCustomPeriods(id);
    }

    public CustomPeriod getCustomPeriod(String id, String year){
        return  departmentRepository.getCustomPeriod(id,year);
    }
    public Period getDepartmentPeriod(String id, String iid, String year, String period){
        CustomPeriod departmentPeriods = departmentRepository.getCustomPeriod(id,year);
        if (departmentPeriods == null) {
            departmentPeriods = institutionRepository.getCustomPeriod(iid,year);
            departmentPeriods.setInherited(true);
        }
        else {
            departmentPeriods.setInherited(false);
        }
        for (Period department_period: departmentPeriods.getPeriods().getPeriod()) {
            if (department_period.getName().equals(period)) {
                return department_period;
            }
        }
        return null;
    }


    /* Argies */
    //@CacheEvict(key = "#id")
    public void saveCustomPause(String id, CustomPause customPause){
        departmentRepository.saveCustomPause(id,customPause);
    }
    //@CacheEvict(key = "#id")
    public void deleteCustomPause(String id, String year) {
        departmentRepository.deleteCustomPause(id,year);
    }
    public List<CustomPause> getCustomPauses(String id) {
        return  departmentRepository.getCustomPauses(id);
    }
    public CustomPause getCustomPause(String id, String year){
        return  departmentRepository.getCustomPause(id,year);
    }
    public CustomPeriod getDepartmentCalendar(String id, String iid, String year) {
        CustomPeriod customPeriod = this.getCustomPeriod(id,year);
        if (customPeriod == null) {
            customPeriod = institutionRepository.getCustomPeriod(iid,year);
            customPeriod.setInherited(true);
        }
        else {
            customPeriod.setInherited(false);
        }
        return customPeriod;
    }

    public List<CustomPause> getDepartmentPauses(String id, String iid) {
        List<CustomPause> pauses = new ArrayList<>();
        List<CustomPause> departmentPauses = this.getCustomPauses(id);
        if (departmentPauses != null && departmentPauses.size()>0) {
            pauses.addAll(departmentPauses);
        }
        List<CustomPause> institutionPauses = institutionRepository.getCustomPauses(iid);
        if (institutionPauses != null && institutionPauses.size()>0) {
            pauses.addAll(institutionPauses);
        }
        return pauses;
    }

    public List<Select2GenGroup> getAllDepartmentsGroupedBySchool(String schoolId) {

        List<School> schools = new ArrayList<>();
        if (schoolId.trim().equals("dummy") || schoolId.trim().equals("")) {
            schools = schoolService.findAllSortedByTitle();
        }
        else {
            School school = schoolService.findById(schoolId);
            schools.add(school);
        }
        List<Select2GenGroup> select2GenGroupList = new ArrayList<>();
        for (School school: schools) {
            //set group properties
            Select2GenGroup select2GenGroup = new Select2GenGroup();
            select2GenGroup.setId(school.getId());
            select2GenGroup.setText(school.getTitle());
            //set children properties
            List<Select2GenChild> groupChildren = new ArrayList<>();
            List<Department> schoolDepartments = this.findBySchoolId(school.getId());
            for (Department department: schoolDepartments) {
                Select2GenChild select2GenChild = new Select2GenChild();
                select2GenChild.setId(department.getId());
                select2GenChild.setText(department.getTitle());
                groupChildren.add(select2GenChild);
            }
            select2GenGroup.setChildren(groupChildren);
            select2GenGroupList.add(select2GenGroup);
        }
        return select2GenGroupList;
    }
    public List<Select2GenGroup> getAllDepartmentsGroupedBySchool(String schoolId, Locale locale) {

        List<School> schools = new ArrayList<>();
        if (schoolId.trim().equals("dummy") || schoolId.trim().equals("")) {
            schools = schoolService.findAllSortedByTitle();
        }
        else {
            School school = schoolService.findById(schoolId);
            schools.add(school);
        }
        List<Select2GenGroup> select2GenGroupList = new ArrayList<>();
        for (School school: schools) {
            //set group properties
            Select2GenGroup select2GenGroup = new Select2GenGroup();
            select2GenGroup.setId(school.getId());
            select2GenGroup.setText(school.getTitle(locale));
            //set children properties
            List<Select2GenChild> groupChildren = new ArrayList<>();
            List<Department> schoolDepartments = this.findBySchoolId(school.getId());
            for (Department department: schoolDepartments) {
                Select2GenChild select2GenChild = new Select2GenChild();
                select2GenChild.setId(department.getId());
                select2GenChild.setText(department.getTitle(locale));
                groupChildren.add(select2GenChild);
            }
            select2GenGroup.setChildren(groupChildren);
            select2GenGroupList.add(select2GenGroup);
        }
        return select2GenGroupList;
    }
}
