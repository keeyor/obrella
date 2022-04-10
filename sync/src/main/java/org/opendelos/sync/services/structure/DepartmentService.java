package org.opendelos.sync.services.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opendelos.legacydomain.calendar.AcademicCalendarSe;
import org.opendelos.legacydomain.calendar.Argia;
import org.opendelos.legacydomain.calendar.Argies;
import org.opendelos.model.calendar.Period;
import org.opendelos.model.calendar.Periods;
import org.opendelos.model.dates.CustomPause;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.structure.Department;
import org.opendelos.sync.repository.delos.OpUserRepository;
import org.opendelos.sync.repository.structure.CourseRepository;
import org.opendelos.sync.repository.structure.DepartmentRepository;
import org.opendelos.sync.repository.structure.InstitutionRepository;
import org.opendelos.sync.repository.structure.StudyProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DepartmentService {

    private final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final OpUserRepository opUserRepository;
    private final StudyProgramRepository studyProgramRepository;
    private final InstitutionRepository institutionRepository;


    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository, CourseRepository courseRepository, OpUserRepository opUserRepository, StudyProgramRepository studyProgramRepository, InstitutionRepository institutionRepository) {
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.opUserRepository = opUserRepository;
        this.studyProgramRepository = studyProgramRepository;
        this.institutionRepository = institutionRepository;
    }

    public List<Department> findAll() {
        logger.trace("Department.findAll");
        return departmentRepository.findAll();
    }

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

    public Department findById(String id) {
        logger.trace(String.format("Department.findById(%s)", id));
        return departmentRepository.findById(id).orElse(null);
    }

    public List<Department> findBySchoolId(String id) {
        logger.trace(String.format("Department.findBySchoolId(%s)", id));
        return departmentRepository.findAllBySchoolId(id);
    }


    public void update(Department department) {
        try {
            departmentRepository.save(department);
            logger.trace(String.format("Department.update: %s", department.getTitle()));
        }
        catch (Exception e) {
            logger.error("error: Department.update:" + e.getMessage());
        }
    }

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
        }
        catch (Exception e) {
            logger.error("error: Department.findAndUpdate:" + e.getMessage());
        }
    }
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

    public void assignRoomsToDepartment(String departmentId, String[] roomsIds) {
       departmentRepository.AssignRoomsToDepartment(departmentId,roomsIds);
    }

    public void unAssignRoomFromDepartment(String departmentId, String roomId) {
        departmentRepository.UnAssignRoomFromDepartment(departmentId,roomId);
    }

    public void addClassroomToDepartment(String departmentId, String classroomId) {
        departmentRepository.addClassroomToDepartment(departmentId,classroomId);
    }

    public void removeClassroomFromDepartment(String departmentId, String classroomId) {
        departmentRepository.deleteClassroomFromDepartment(departmentId,classroomId);
    }

    public void removeClassroomAssignmentsFromAllDepartment(String classroomId) {
        departmentRepository.removeClassroomAssignmentsFromAllDepartments(classroomId);
    }
    /* Calendar  */

    public void saveCustomPeriod(String id, CustomPeriod customPeriod){
        departmentRepository.saveCustomPeriod(id,customPeriod);
    }

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

    public void saveCustomPause(String id, CustomPause customPause){
        departmentRepository.saveCustomPause(id,customPause);
    }

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
            if (customPeriod != null) {
                customPeriod.setInherited(true);
            }
            else {
                logger.error("CANNOT FIND CALENDAR FOR INSTITUTION:" + iid + " YEAR:" + year + ". Creating DEFAULT....");
                CustomPeriod customPeriod1 = this.CreateDefaultCalendarForYear(iid,year);
                return customPeriod1;
            }
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

    public CustomPeriod CreateDefaultCalendarForYear(String id, String year) {
        CustomPeriod institution_period = institutionRepository.getCustomPeriod(id, year);
        if (institution_period != null) {
            return null;
        }
        CustomPeriod customPeriod = new CustomPeriod();
        customPeriod.setYear(year);
        customPeriod.setPeriods(this.createDefaultPeriods(year));
        try {
            institutionRepository.saveCustomPeriod(id, customPeriod);
            return customPeriod;
        }
        catch (Exception e) {
            return null;
        }
    }

    private org.opendelos.model.calendar.Periods createDefaultPeriods(String year) {

        int iyear = Integer.parseInt(year);
        //PERIOD names should be taken from properties files
        List<Period> periodList = new ArrayList<>();

        Period winter = new Period();
        winter.setName("winter");
        winter.setStartDate(iyear + "-09-01");
        winter.setEndDate((iyear+1) + "-01-15");
        periodList.add(winter);

        Period intermediate = new Period();
        intermediate.setName("intervening");
        intermediate.setStartDate((iyear+1) + "-01-16");
        intermediate.setEndDate((iyear+1) + "-02-15");
        periodList.add(intermediate);

        Period spring = new Period();
        spring.setName("spring");
        spring.setStartDate((iyear+1) + "-02-16");
        spring.setEndDate((iyear+1) + "-07-31");
        periodList.add(spring);

        Period summer = new Period();
        summer.setName("summer");
        summer.setStartDate((iyear+1) + "-08-01");
        summer.setEndDate((iyear+1) + "-08-31");
        periodList.add(summer);

        org.opendelos.model.calendar.Periods periods = new Periods();
        periods.setPeriod(periodList);

        return periods;
    }


}
