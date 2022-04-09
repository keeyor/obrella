package org.opendelos.live.services.structure;

import java.util.ArrayList;
import java.util.List;

import org.opendelos.live.repository.resource.ResourceQuery;
import org.opendelos.live.repository.structure.CourseRepository;
import org.opendelos.live.services.opUser.OpUserService;
import org.opendelos.live.services.resource.ResourceService;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Department;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.model.users.UserAccess;
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
@CacheConfig(cacheNames = "courses")
public class CourseService {

    private final Logger logger = LoggerFactory.getLogger(CourseService.class);
    private final CourseRepository courseRepository;
    private final OpUserService opUserService;
    private final ResourceService resourceService;
    private final DepartmentService departmentService;

    @Autowired
    public CourseService(CourseRepository courseRepository, OpUserService opUserService, ResourceService resourceService, DepartmentService departmentService) {
        this.courseRepository = courseRepository;
        this.opUserService = opUserService;
        this.resourceService = resourceService;
        this.departmentService = departmentService;
    }

    public List<Course> findAll() {
        logger.trace("Course.findAll");
        Sort sort = Sort.by(Sort.Order.asc("title"));
        return courseRepository.findAll(sort);
    }

    @CacheEvict(allEntries = true)
    public void deleteAll() {
        logger.trace("Course.deleteAll");
        try {
            courseRepository.deleteAll();
        }
        catch (Exception e) {
            logger.error("error: deleteAll:" + e.getMessage());
        }
    }
    @CacheEvict(allEntries = true)
    public String create(Course course ) {
        String generatedId = null;
        try {
            course.setId(null); //force object creation (this can be "" from JavaScript code
            Course nInstitution =  courseRepository.save(course);
            generatedId = nInstitution.getId();
            logger.trace(String.format("Course.create: %s", course.getTitle()));
        }
        catch (Exception e) {
            logger.error("error: Course.create:" + e.getMessage());
        }
        return generatedId;
    }

    @Cacheable(key = "#id")
    public Course findById(String id) {
        logger.trace(String.format("Course.findById(%s)", id));
        return courseRepository.findById(id).orElse(null);
    }

    @CacheEvict(key = "#course.id")
    public void update(Course course) {
        logger.trace(String.format("Course.update: %s", course.getTitle()));
        try {
             courseRepository.save(course);
        }
        catch (Exception e) {
            logger.error("error: Course.update:" + e.getMessage());
        }
    }
    @CacheEvict(key = "#course.id")
    public void findAndUpdate(Course course) {
        try {
            courseRepository.findAndUpdate(course);
            long updateResourcesCourse = courseRepository.updateResourcesCourse(course);
            logger.info(String.format("Course.findAndUpdate: %s", course.getTitle()));
            logger.info(String.format("Course.updated Resource Courses updates: %s", updateResourcesCourse));
        }
        catch (Exception e) {
            logger.error("error: Department.findAndUpdate:" + e.getMessage());
        }
    }

    public List<Course> findByDepartmentId(String departmentId) {
        logger.trace(String.format("Course.findCoursesByDepartmentId(%s)", departmentId));
        return courseRepository.findAllByDepartmentId(departmentId);
    }

    @CacheEvict(key = "#id")
    public void delete(String id) throws Exception {
        //TODO: IMPORTANT !!! (probably) take additional actions when scheduler and calendar are implemented!!!
        Course course = courseRepository.findById(id).orElse(null);
        if (course!= null) {
            ResourceQuery resourceQuery = new ResourceQuery();
            resourceQuery.setResourceType("COURSE");
            resourceQuery.setCourseId(id);
            resourceQuery.setLimit(1);
            List<Resource> resources = resourceService.searchLecturesOnFilters(resourceQuery);
            if (resources.size() == 0) {
                courseRepository.deleteById(id);
            }
            else {
                throw new Exception("_FORBIDDEN_LECTURES");
            }
        }
        else {
            throw new Exception("_NOT_FOUND");
        }
    }

    public Course findByIdentity(String identity) {
        logger.trace(String.format("Course.findByIdentity(%s)", identity));
        return courseRepository.findByIdentity(identity);
    }

    public List<Course> getAuthorizedCoursesByStaffIdAndUserId(String staffId, String editor_id) {
        /*
            StaffId is already of authorized staffmembers from previous call
            if staffId is in coursePermission:: add those courses  only
            else add all staffId courses
         */
        List<Course> courseList = new ArrayList<>();
        OpUser editor = opUserService.findById(editor_id);

        boolean support_staff = false;
        List<UserAccess.UserRights.CoursePermission> coursePermissions = editor.getRights().getCoursePermissions();
        if (coursePermissions != null && coursePermissions.size() > 0) {
            for (UserAccess.UserRights.CoursePermission coursePermission : coursePermissions) {
                if (coursePermission.getStaffMemberId().equals(staffId)) {
                    Course supportedCourse = this.findById(coursePermission.getCourseId());
                    courseList.add(supportedCourse);
                    support_staff = true;
                }
            }
            if (staffId.equals(editor_id)) {
                List<String> staffMembers_courseIds = opUserService.findById(staffId).getCourses();
                List<Course> staffMember_courses = this.findFromIds(staffMembers_courseIds);
                courseList.addAll(staffMember_courses);
            }
        }
        if (!support_staff) { 
            List<String> staffMembers_courseIds = opUserService.findById(staffId).getCourses();
            List<Course> staffMember_courses = this.findFromIds(staffMembers_courseIds);
            courseList.addAll(staffMember_courses);
        }
        return courseList;
    }

    public List<Course> getAuthorizedCoursesByEditor(OoUserDetails editor, String ACCESS_TYPE) {
        List<Course> courseList = new ArrayList<>();

        if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SA"))) {
                courseList = this.findAll();
        }
        else {
            if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {

                OpUser opUser = opUserService.findById(editor.getId());
                List<UserAccess.UserRights.UnitPermission> unitPermissions = opUser.getRights().getUnitPermissions();

                if (unitPermissions != null && unitPermissions.size() > 0) {  // user is Unit Manager
                    for (UserAccess.UserRights.UnitPermission unitPermission : unitPermissions) {
                        if (ACCESS_TYPE.equals("content") && unitPermission.isContentManager() ||
                            ACCESS_TYPE.equals("scheduler") && unitPermission.isScheduleManager()) {
                                if (unitPermission.isContentManager() && unitPermission.getUnitType()
                                        .equals(UserAccess.UnitType.INSTITUTION)) {
                                    courseList = this.findAll();
                                    break; // nothing else to add
                                }
                                else if (unitPermission.isContentManager() && unitPermission.getUnitType()
                                        .equals(UserAccess.UnitType.SCHOOL)) {
                                    List<Department> departmentList = departmentService
                                            .findBySchoolId(unitPermission.getUnitId());
                                    for (Department department : departmentList) {
                                        courseList.addAll(this.findByDepartmentId(department.getId()));
                                    }
                                }
                                else if (unitPermission.isContentManager() && unitPermission.getUnitType()
                                        .equals(UserAccess.UnitType.DEPARTMENT)) {
                                    courseList.addAll(this.findByDepartmentId(unitPermission.getUnitId()));
                                }
                        }
                    } //For
                }
            }
            else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPPORT"))) {
                OpUser opUser = opUserService.findById(editor.getId());
                List<UserAccess.UserRights.CoursePermission> coursePermissions = opUser.getRights().getCoursePermissions();

                if (coursePermissions != null && coursePermissions.size() > 0) {    // user is Support Personnel
                      for (UserAccess.UserRights.CoursePermission coursePermission : coursePermissions) {
                          if (ACCESS_TYPE.equals("content") && coursePermission.isContentManager() ||
                              ACCESS_TYPE.equals("scheduler") && coursePermission.isScheduleManager()) {
                                if (coursePermission.isContentManager()) {
                                        Course supported_course = this.findById(coursePermission.getCourseId());
                                        courseList.add(supported_course);
                                }
                          }
                      }
                }
            }
            if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFFMEMBER"))) {
                List<String> editors_courseIds = opUserService.findStaffMembersAssignedCourseIds(editor.getId());
                for (String course_id : editors_courseIds) {
                    if (!containsCourseId(courseList, course_id)) {
                        Course staffmember_course = this.findById(course_id);
                        courseList.add(staffmember_course);
                    }
                }
            }
        }
        return courseList;
    }

    public List<Course> findWithCriteria(String schoolId, String departmentId, String study, String program) {
        return courseRepository.findWithCriteria(schoolId,departmentId,study, program);
    }
    public List<Course> findFromIds(List<String> ids) {
        List<Course> courseList = new ArrayList<>();
        List<Course> coursesFromIds  =  courseRepository.findFromIds(ids);
        if (coursesFromIds != null && coursesFromIds.size()>0) {
            courseList.addAll(coursesFromIds);
        }
        return courseList;
    }
    public List<Course> findExcludingIds(List<String> ids, String departmentId) {
        return courseRepository.findExcludingIds(ids, departmentId);
    }

    public boolean containsCourseId(final List<Course> list, final String id){
        return list.stream().map(Course::getId).anyMatch(id::equals);
    }
}
