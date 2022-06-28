package org.opendelos.vodapp.services.opUser;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.scheduler.Schedule;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.structure.School;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.model.users.UserAccess;
import org.opendelos.vodapp.repository.delos.OpUserRepository;
import org.opendelos.vodapp.repository.resource.ResourceRepository;
import org.opendelos.vodapp.repository.scheduledEvent.ScheduledEventRepository;
import org.opendelos.vodapp.repository.scheduler.ScheduleRepository;
import org.opendelos.vodapp.repository.structure.CourseRepository;
import org.opendelos.vodapp.services.structure.DepartmentService;
import org.opendelos.vodapp.services.structure.SchoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;


//@CacheConfig(cacheNames = "users")
@Service
public class OpUserService {

    private final Logger logger = LoggerFactory.getLogger(OpUserService.class);

    private final OpUserRepository opUserRepository;
    private final CourseRepository courseRepository;
    private final ResourceRepository resourceRepository;
    private final DepartmentService departmentService;
    private final SchoolService schoolService;
    private final ScheduledEventRepository scheduledEventRepository;
    private final ScheduleRepository scheduleRepository;

    @Autowired
    Institution defaultInstitution;

    @Autowired
    public OpUserService(OpUserRepository opUserRepository, DepartmentService departmentService, CourseRepository courseRepository, ResourceRepository resourceRepository, SchoolService schoolService, ScheduledEventRepository scheduledEventRepository, ScheduleRepository scheduleRepository) {
        this.opUserRepository = opUserRepository;
        this.resourceRepository = resourceRepository;
        this.departmentService = departmentService;
        this.courseRepository = courseRepository;
        this.schoolService = schoolService;
        this.scheduledEventRepository = scheduledEventRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public void save(OpUser opUser) {
        opUserRepository.save(opUser);
    }

    public List<OpUser> findAll() {
        logger.trace("Users.findAll");
        Sort sort = Sort.by(Sort.Order.asc("Name"));
        return opUserRepository.findAll(sort);
    }
    public List<OpUser> findAllStaffMembers() {
        logger.trace("Users.findAllStaffMembers");
        return opUserRepository.findAllByAuthoritiesContains(UserAccess.UserAuthority.STAFFMEMBER);
    }
    public List<OpUser> findAllByPrimaryAffiliation(String primary_affiliation) {
        logger.trace("Users.findAllByPrimaryAffiliation");
        return opUserRepository.findByEduPersonPrimaryAffiliation(primary_affiliation);
    }
    public List<OpUser> findAllByNotPrimaryAffiliation(String primary_affiliation) {
        logger.trace("Users.findAllByNotPrimaryAffiliation");
        return opUserRepository.findByEduPersonPrimaryAffiliationNot(primary_affiliation);
    }
    public List<OpUser> findAllManagers() {
        logger.trace("Users.findAllManagers");
        return opUserRepository.findAllManagers();
    }
    public List<OpUser> findAllManagersOfType(String type) {

        logger.trace("Users.findAllManagersOfType:" + type);
        if (type.equalsIgnoreCase("SA")) {
            return opUserRepository.findAllSaUsers();
        }
        else if (type.equalsIgnoreCase("IM")) {
            return opUserRepository.findAllImUsers();
        }
        else if (type.equalsIgnoreCase("DM")) {
            return opUserRepository.findAllDmUsers();
        }
        else if (type.equalsIgnoreCase("SP")) {
            return opUserRepository.findAllSpUsers();
        }

        return opUserRepository.findAllManagers();

    }

    public List<OpUser>  getSupportPersonnelForCourseAndStaffMember(String courseId,String staffMemberId) {
        return opUserRepository.getSupportPersonnelForCourseAndStaffMember(courseId,staffMemberId);
    }

    //@CacheEvict(allEntries = true)
    public void deleteAll() {
        logger.trace("Users.deleteAll");
        try {
            opUserRepository.deleteAll();
        }
        catch (Exception e) {
            logger.error("error: Users.deleteAll:" + e.getMessage());
        }
    }

    public String create(OpUser opUser) {
        String generatedId = null;
        try {
            OpUser nUser =  opUserRepository.save(opUser);
            generatedId = nUser.getId();
            logger.info(String.format("Users.create: %s", opUser.getUid()));
        }
        catch (Exception e) {
            logger.info("error: Users.create:" + e.getMessage());
        }
        return generatedId;
    }

    public OpUser createAndReturn(OpUser opUser) {
        OpUser nUser = null;
        try {
            nUser =  opUserRepository.save(opUser);
            logger.trace(String.format("Users.create: %s", opUser.getUid()));
        }
        catch (Exception e) {
            logger.error("error: StudyProgram.create:" + e.getMessage());
        }
        return nUser;
    }
    //@Cacheable(key = "#id",unless="#result == null")
    public OpUser findById(String id) {
        logger.trace(String.format("Users.findUserById(%s)", id));
        return opUserRepository.findById(id).orElse(null);

    }
    public OpUser findByUid(String uid) {
        logger.trace(String.format("Users.findByUid(%s)", uid));
        return opUserRepository.findByUid(uid);

    }
    public OpUser findByToken(String token) {
        logger.trace(String.format("Users.findByToken(%s)", token));
        return opUserRepository.findByToken(token);

    }
    public OpUser findByEmail(String email) {
        logger.trace(String.format("Users.findByEmail(%s)", email));
        return opUserRepository.findByEmail(email);

    }
    public OpUser findByIdentity(String identity) {
        logger.trace(String.format("Users.findUserByIdentity(%s)", identity));

        return  opUserRepository.findByIdentity(identity);
    }

    //@CacheEvict(key = "#opUser.id")
    public void update(OpUser opUser) {
        logger.trace(String.format("StaffMember.update: %s", opUser.getName()));
        try {
            opUserRepository.save(opUser);
        }
        catch (Exception e) {
            logger.error("error: StaffMember.update:" + e.getMessage());
        }
    }

    //@CacheEvict(key = "#id")
    public void delete(String id) throws Exception {
        logger.trace(String.format("Manager.StaffMember: %s", id));
        OpUser opUser  = opUserRepository.findById(id).orElse(null);
        if (opUser!= null) {
            boolean UserRefInRes = this.isUserReferencedInResources(id);
            if (UserRefInRes) { throw new Exception("_FORBIDDEN_LECTURES"); }
            boolean UserRefInSc = this.isUserReferencedInScheduler(id);
            if (UserRefInSc) { throw new Exception("_FORBIDDEN_SCHEDULER"); }
            boolean UserRefInSe = this.isUserReferencedInScheduledEvents(id);
            if (UserRefInSe) { throw new Exception("_FORBIDDEN_SCHEDULED_EVENTS"); }
            //TODO: ENABLE FOR PRODUCTION
            opUserRepository.deleteById(id);
        }
        else {
            throw new Exception("_NOT_FOUND");
        }
    }

    //@CacheEvict(key = "#id")
    public void deleteManager(String id) throws Exception {
        logger.trace(String.format("Manager.delete: %s", id));
        OpUser opUser  = opUserRepository.findById(id).orElse(null);
        if (opUser!= null) {
            boolean UserRefInRes = this.isUserReferencedInResources(id);
            if (UserRefInRes) { throw new Exception("_FORBIDDEN_LECTURES"); }
            boolean UserRefInSc = this.isUserReferencedInScheduler(id);
            if (UserRefInSc) { throw new Exception("_FORBIDDEN_SCHEDULER"); }
            boolean UserRefInSe = this.isUserReferencedInScheduledEvents(id);
            if (UserRefInSe) { throw new Exception("_FORBIDDEN_SCHEDULED_EVENTS"); }
            opUserRepository.deleteById(id);
        }
        else {
            throw new Exception("_NOT_FOUND");
        }
    }

    //@CacheEvict(key = "#opUser.id")
    public void findAndUpdate(OpUser opUser) {
        try {
            opUserRepository.findAndUpdate(opUser);
            logger.info(String.format("OpUser.findAndUpdate: %s", opUser.getName()));
            /* BULK UPDATES */
            String sid = opUser.getId();
            String sName = opUser.getName();
            String sAff = opUser.getAffiliation();
            Unit sDepartment = opUser.getDepartment();
            long u_r_supervisor = opUserRepository.updateResourcesSupervisorInfo(sid,sName,sAff);
            long u_r_editor = opUserRepository.updateResourcesEditorInfo(sid,sName,sAff, sDepartment); //TODO: DEP TOO
            long u_r_se_editor = opUserRepository.updateResourcesScheduledEventEditorInfo(sid,sName,sAff, sDepartment); //TODO: DEP TOO
            long u_r_se_rp = opUserRepository.updateResourcesScheduledEventResponsiblePersonInfo(sid,sName,sAff);
            long u_se_editor = opUserRepository.updateScheduledEventsEditorInfo(sid,sName,sAff,sDepartment); //TODO: DEP TOO
            long u_se_rp = opUserRepository.updateScheduledEventsResponsiblePersonInfo(sid,sName,sAff);
            logger.info ("Updated u_r_supervisor:" + u_r_supervisor);
            logger.info ("Updated u_r_editor:" + u_r_editor);
            logger.info ("Updated u_r_se_editor:" + u_r_se_editor);
            logger.info ("Updated u_r_se_rp:" + u_r_se_rp);
            logger.info ("Updated u_se_editor:" + u_se_editor);
            logger.info ("Updated u_se_rp:" + u_se_rp);
        }
        catch (Exception e) {
            logger.error("error: Department.findAndUpdate:" + e.getMessage());
        }
    }

    public List<OpUser> findByDepartmentId(String id) {
        logger.trace(String.format("StaffMember.findByDepartmentId(%s)", id));
        return opUserRepository.findAllByDepartmentIdOrderByName(id);
    }

    public List<OpUser> findAllStaffMembersTeachingCourses(List<String> courseIds) {
        logger.trace(String.format("StaffMember.findAllStaffMembersTeachingCourses"));
        return opUserRepository.findAllStaffMembersTeachingCourses(courseIds);
    }

    public List<OpUser> findStaffMembersTeachingCourseId(String id) {
        logger.trace(String.format("StaffMember.findByCourseId(%s)", id));
        return opUserRepository.findAllStaffMembersForCourseId(id);
    }

    public long countStaffMembersTeachingCourseId(String id) {
        logger.trace(String.format("StaffMember.countByCourseId(%s)", id));
        return opUserRepository.countAllStaffMembersForCourseId(id);
    }

    public List<OpUser> findStaffMembersTeachingInDepartment(String id) {

        List<Course> department_courses = courseRepository.findAllByDepartmentId(id);
        List<String> courseIds = new ArrayList<>();
        for (Course course: department_courses) {
            courseIds.add(course.getId());
        }
        //return opUserRepository.findAllStaffMembersTeachingInDepartment(id);
        return opUserRepository.findAllStaffMembersTeachingCourses(courseIds);
    }

    public List<OpUser> findStaffMembersOfDepartment(String id) {
        return opUserRepository.findAllStaffMembersOfDepartment(id);
    }

    public List<String> findStaffMembersAssignedCourseIds(String id) {
        return opUserRepository.getAllStaffMemberCourseIds(id);
    }
    
    public List<OpUser> getAuthorizedStaffMembersOfCourseIdByEditor(OoUserDetails editor, String courseId, String ACCESS_TYPE) {

        List<OpUser> staffMemberList = new ArrayList<>();
        //This is correct. Editor (as MANAGER) has access to courses of department. He has all rights to edit regardless of staff member.
        if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SA")) || editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER")) ) {
             // Add all staffMMember teaching course
             staffMemberList = this.findStaffMembersTeachingCourseId(courseId);
        }
        else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPPORT"))) {
            OpUser opUser = this.findById(editor.getId());
            List<UserAccess.UserRights.CoursePermission> coursePermissions = opUser.getRights().getCoursePermissions();
            if (coursePermissions != null && coursePermissions.size()>0) {
                for (UserAccess.UserRights.CoursePermission coursePermission : coursePermissions) {
                    if (ACCESS_TYPE.equals("content") && coursePermission.isContentManager() || ACCESS_TYPE.equals("scheduler") && coursePermission.isScheduleManager()) {
                        if (coursePermission.getCourseId().equals(courseId)) {
                            OpUser supportedStaffMember = this.findById(coursePermission.getStaffMemberId());
                            staffMemberList.add(supportedStaffMember);
                        }
                    }
                }
            }
        }
        else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFFMEMBER"))) {
            OpUser opUser = this.findById(editor.getId());
            List<String> editor_courses = opUser.getCourses();
            for (String course_id: editor_courses) {
               if (course_id.equals(courseId)) {
                    staffMemberList.add(opUser);
                    break;
               }
            }
        }
        return staffMemberList;
    }

    public List<OpUser> getAuthorizedStaffMembersByEditor(OoUserDetails editor, String ACCESS_TYPE) {

        List<OpUser> staffMemberList = new ArrayList<>();

        if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SA"))) {
             staffMemberList.addAll(this.findAllStaffMembers());
        }
        else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            {
                OpUser opUser = this.findById(editor.getId());
                List<UserAccess.UserRights.UnitPermission> unitPermissions = opUser.getRights().getUnitPermissions();

                if (unitPermissions != null && unitPermissions.size() > 0) {  // user is Unit Manager
                    for (UserAccess.UserRights.UnitPermission unitPermission : unitPermissions) {
                        if (ACCESS_TYPE.equals("content") && unitPermission.isContentManager() || ACCESS_TYPE.equals("scheduler") && unitPermission.isScheduleManager()) {
                            if (unitPermission.getUnitType().equals(UserAccess.UnitType.INSTITUTION)) {
                                staffMemberList.addAll(this.findAllStaffMembers());
                                break; // nothing else to add
                            }
                            else if (unitPermission.getUnitType().equals(UserAccess.UnitType.SCHOOL)) {
                                List<Department> departmentList = departmentService.findBySchoolId(unitPermission.getUnitId());
                                for (Department department : departmentList) {
                                    staffMemberList.addAll(this.findStaffMembersTeachingInDepartment(department.getId()));
                                }
                            }
                            else if (unitPermission.getUnitType().equals(UserAccess.UnitType.DEPARTMENT)) {
                                staffMemberList.addAll(this.findStaffMembersTeachingInDepartment(unitPermission.getUnitId()));
                            }
                        }
                    } //For
                }
            }
        }
        else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPPORT"))) {

            OpUser opUser = this.findById(editor.getId());
            List<UserAccess.UserRights.CoursePermission> coursePermissions = opUser.getRights().getCoursePermissions();

            if (coursePermissions != null && coursePermissions.size() > 0) {    // user is Support Personnel
                for (UserAccess.UserRights.CoursePermission coursePermission : coursePermissions) {
                    if (ACCESS_TYPE.equals("content") && coursePermission.isContentManager() || ACCESS_TYPE.equals("scheduler") && coursePermission.isScheduleManager()) {
                       // if (coursePermission.isContentManager()) {
                            //do not add dublicates
                            List<OpUser> containsSelf =
                                    staffMemberList
                                            .stream()
                                            .filter(p-> p.getId().equals(coursePermission.getStaffMemberId()))
                                            .collect(Collectors.toList());
                            if (containsSelf.size() == 0) {
                                OpUser staffmember = this.findById(coursePermission.getStaffMemberId());
                                staffMemberList.add(staffmember);
                            }
                        }
                  //  }
                }
            }
        }
        if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFFMEMBER"))) {
            List<OpUser> containsSelf =
                    staffMemberList
                            .stream()
                            .filter(p-> p.getId().equals(editor.getId()))
                            .collect(Collectors.toList());
            if (containsSelf.size() == 0) {
                OpUser opUser = this.findById(editor.getId());
                staffMemberList.add(opUser);
            }
        }
        return staffMemberList;
    }

    public boolean containsStaffMemberWithId(final List<OpUser> list, final String id){
        return list.stream().anyMatch(o -> o.getId().equals(id));
    }

    //UNIT PERMISSION IDU
    //@CacheEvict(key = "#id")
    public void assignUnitPermissionToManagerById(String id, UserAccess.UserRights.UnitPermission unitPermission) {
        opUserRepository.AssignUnitPermissionToManager(id,unitPermission);
    }
    //@CacheEvict(key = "#id")
    public void assignUpdateUnitPermissionToManagerById(String id, UserAccess.UserRights.UnitPermission unitPermission) {
        opUserRepository.UnAssignUnitPermissionFromManager(id,unitPermission.getUnitId());
        opUserRepository.AssignUnitPermissionToManager(id,unitPermission);
    }
    //@CacheEvict(key = "#id")
    public void unassignUnitPermissionFromManagerById(String id, String unitId) {
        opUserRepository.UnAssignUnitPermissionFromManager(id,unitId);
    }
    //COURSE PERMISSION IDU
    //@CacheEvict(key = "#id")
    public void assignCoursePermissionToManagerById(String id, UserAccess.UserRights.CoursePermission coursePermission) {
        opUserRepository.AssignCoursePermissionToManager(id,coursePermission);
    }
    //@CacheEvict(key = "#id")
    public void assignUpdateCoursePermissionToManagerById(String id, UserAccess.UserRights.CoursePermission coursePermission) {
        opUserRepository.UnAssignCoursePermissionFromManager(id,coursePermission.getStaffMemberId(),coursePermission.getCourseId());
        opUserRepository.AssignCoursePermissionToManager(id,coursePermission);
    }
    //@CacheEvict(key = "#id")
    public void unassignCoursePermissionFromManagerById(String id, String staffMemberId,String courseId) {
        opUserRepository.UnAssignCoursePermissionFromManager(id,staffMemberId,courseId);
    }
    //EVENT PERMISSION IDU
    //@CacheEvict(key = "#id")
    public void assignEventPermissionToManagerById(String id, UserAccess.UserRights.EventPermission eventPermission) {
        opUserRepository.AssignEventPermissionToManager(id,eventPermission);
    }
    //@CacheEvict(key = "#id")
    public void assignUpdateEventPermissionToManagerById(String id, UserAccess.UserRights.EventPermission eventPermission) {
        opUserRepository.UnAssignEventPermissionFromManager(id,eventPermission.getStaffMemberId(),eventPermission.getEventId());
        opUserRepository.AssignEventPermissionToManager(id,eventPermission);
    }
    //@CacheEvict(key = "#id")
    public void unassignEventPermissionFromManagerById(String id, String staffMemberId,String eventId) {
        opUserRepository.UnAssignEventPermissionFromManager(id,staffMemberId,eventId);
    }
    //@CacheEvict(key = "#id")
    public void changeRoleOfManagerById(String id, String new_role) {

        OpUser opUser = opUserRepository.findById(id).orElse(null);
        if (opUser != null) {
            List<UserAccess.UserAuthority> user_authorities = opUser.getAuthorities();
            user_authorities.removeIf(userAuthority -> userAuthority.equals(UserAccess.UserAuthority.MANAGER) || userAuthority
                            .equals(UserAccess.UserAuthority.SUPPORT));
            if (new_role.equals("MANAGER")) { user_authorities.add(UserAccess.UserAuthority.MANAGER);}
            else if (new_role.equals("SUPPORT")) { user_authorities.add(UserAccess.UserAuthority.SUPPORT);}

            opUser.getRights().setIsSa(new_role.equals("SA"));
            if (opUser.getRights().getCoursePermissions() != null) {
                opUser.getRights().getCoursePermissions().clear();
            }
            if (opUser.getRights().getUnitPermissions() != null) {
                opUser.getRights().getUnitPermissions().clear();
            }
            opUserRepository.save(opUser);
        }
    }
    //@CacheEvict(key = "#staffMemberId")
    public void assignCoursesToStaffMemberById(String staffMemberId, String[] courseIds) {
        opUserRepository.AssignCoursesToStaffMember(staffMemberId,courseIds);
    }
    //@CacheEvict(key = "#staffMemberId")
    public void unassignCourseFromStaffMemberById(String staffMemberId, String id) {
        opUserRepository.UnAssignCourseFromStaffMember(staffMemberId,id);
    }
    public OpUser createNewStaffMemberFromCASAttributes(Map<String, Object> attributes){

        OpUser nsm = new OpUser();
        nsm.setUid((String) attributes.get("uid"));
        nsm.setEmail((String) attributes.get("mail"));
        nsm.setName((String) attributes.get("cn;lang-el"));
        nsm.setAltName((String) attributes.get("cn"));
        nsm.setAffiliation((String) attributes.get("title;lang-el"));
        nsm.setEduPersonPrimaryAffiliation((String) attributes.get("eduPersonPrimaryAffiliation"));
        if (attributes.get("eduPersonAffiliation") instanceof String) {
            List<String> eduPersonAffiliation = new ArrayList<>();
            eduPersonAffiliation.add((String) attributes.get("eduPersonAffiliation"));
            nsm.setEduPersonAffiliation(eduPersonAffiliation);
        }
        else if (attributes.get("eduPersonAffiliation") instanceof List) {
            nsm.setEduPersonAffiliation((List<String>) attributes.get("eduPersonAffiliation"));
        }
        Unit user_department = new Unit(StructureType.DEPARTMENT,"ASK","ASK");
        nsm.setDepartment(user_department);
        //Set temporary authority till department is set!
        List<UserAccess.UserAuthority> authorities = new ArrayList<>();
        authorities.add(UserAccess.UserAuthority.USER);
        nsm.setAuthorities(authorities);
        UserAccess.UserRights userRights = new UserAccess.UserRights();
        userRights.setIsSa(false);
        nsm.setRights(userRights);
        List<String> courses = new ArrayList<>();
        nsm.setCourses(courses);
        nsm.setActive(true);

        return nsm;
    }
    public OpUser createInMemoryStudentAccountFromCASAttributes(Map<String, Object> attributes){

        OpUser nsm = new OpUser();
        nsm.setUid((String) attributes.get("uid"));
        nsm.setEmail((String) attributes.get("mail"));
        Unit user_department = new Unit(StructureType.DEPARTMENT,"NONE","NONE");
        nsm.setDepartment(user_department);
        nsm.setName((String) attributes.get("cn;lang-el"));
        nsm.setAltName((String) attributes.get("cn"));
        nsm.setAffiliation((String) attributes.get("title;lang-el"));
        nsm.setEduPersonPrimaryAffiliation((String) attributes.get("eduPersonPrimaryAffiliation"));
        if (attributes.get("eduPersonAffiliation") instanceof String) {
            List<String> eduPersonAffiliation = new ArrayList<>();
            eduPersonAffiliation.add((String) attributes.get("eduPersonAffiliation"));
            nsm.setEduPersonAffiliation(eduPersonAffiliation);
        }
        else if (attributes.get("eduPersonAffiliation") instanceof List) {
            nsm.setEduPersonAffiliation((List<String>) attributes.get("eduPersonAffiliation"));
        }
        //Set temporary authority till department is set!
        List<UserAccess.UserAuthority> authorities = new ArrayList<>();
        authorities.add(UserAccess.UserAuthority.STUDENT);
        nsm.setAuthorities(authorities);
        UserAccess.UserRights userRights = new UserAccess.UserRights();
        userRights.setIsSa(false);
        nsm.setRights(userRights);
        List<String> courses = new ArrayList<>();
        nsm.setCourses(courses);
        nsm.setActive(true);

        return nsm;
    }
    public boolean updateUserInfoFromCASAttributes(OpUser opUser,Map<String, Object> attributes) {

        /*  FOR REFERENCE: CAS ATTRIBUTES
        String uid = (String) attributes.get("uid");                                                    //gatzonis
        String mail = (String) attributes.get("mail");                                                  //gatzonis@med.uoa.gr
        String eduPersonPrimaryAffiliation  = (String) attributes.get("eduPersonPrimaryAffiliation");  //staff
        String givenName_lang_el = (String) attributes.get("givenName;lang-el");                        //Μιχάλης
        String title_lang_el = (String) attributes.get("title;lang-el");                                //Διοικητικό Προσωπικό
        //This might be a List<String>
        //String eduPersonAffiliation = (String) attributes.get("eduPersonAffiliation");                  //staff
        String givenName = (String) attributes.get("givenName");                                        //Michalis
        String sn_lang_el = (String) attributes.get("sn;lang-el");                                      //Γκατζώνης
        String cn = (String) attributes.get("cn");                                                      //Michalis Gatzonis
        String title  = (String) attributes.get("title");                                               //Administrative Staff
        String cn_lang_el = (String) attributes.get("cn;lang-el");                                      //Μιχάλης Γκατζώνης
        String sn = (String) attributes.get("sn");                                                      //Gatzonis
        */
        boolean info_changed = false;

        String user_email = (String) attributes.get("mail");
        if (user_email != null && !opUser.getEmail().equalsIgnoreCase(user_email)) {
            opUser.setEmail(user_email);
            info_changed = true;
        }
        String user_surname = (String) attributes.get("sn;lang-el");
        String user_firstname = (String) attributes.get("givenName;lang-el");
        String user_name = user_surname + " " + user_firstname;

        if (!opUser.getName().equalsIgnoreCase(user_name)) {
            opUser.setName(user_name);
            info_changed = true;
        }
        String user_altname = (String) attributes.get("cn");
        if (!opUser.getAltName().equalsIgnoreCase(user_altname)) {
            opUser.setAltName(user_altname);
            info_changed = true;
        }
        String eduPersonPrimaryAffiliation = (String) attributes.get("eduPersonPrimaryAffiliation");
        if (!opUser.getEduPersonPrimaryAffiliation().equalsIgnoreCase(eduPersonPrimaryAffiliation)) {
            opUser.setEduPersonPrimaryAffiliation(eduPersonPrimaryAffiliation);
            info_changed = true;
        }
        List<String> eduPersonAffiliations = null;
        if (attributes.get("eduPersonAffiliation") instanceof String) {
            String eduPersonAffiliation = (String) attributes.get("eduPersonAffiliation");
            eduPersonAffiliations = new ArrayList<>();
            eduPersonAffiliations.add(eduPersonAffiliation);
        }
        else if (attributes.get("eduPersonAffiliation") instanceof List) {
            eduPersonAffiliations = (List<String>) attributes.get("eduPersonAffiliation");
        }
        List<String> user_eduPersonAffiliations = opUser.getEduPersonAffiliation();
        if (eduPersonAffiliations != null && eduPersonAffiliations.size()>0) {
            Collections.sort(eduPersonAffiliations);
            Collections.sort(user_eduPersonAffiliations);
            if (!user_eduPersonAffiliations.equals(eduPersonAffiliations)) {
                opUser.setEduPersonAffiliation(eduPersonAffiliations);
                info_changed = true;
            }
         }
        //>>> Delos only info
        //1. Set department to ASK (if null)
        if (opUser.getDepartment().getId() == null) {
           Unit user_department = new Unit(StructureType.DEPARTMENT,"ASK","ASK");
           opUser.setDepartment(user_department);
           info_changed = true;
        }

        // in case the user existed as manager but not as staff member
        List<UserAccess.UserAuthority> authorities;
        if (opUser.getAuthorities() == null) {
            authorities = new ArrayList<>();
        }
        else {
            authorities = opUser.getAuthorities();
        }
        if (eduPersonPrimaryAffiliation.equalsIgnoreCase("staff") ||  eduPersonPrimaryAffiliation.equalsIgnoreCase("faculty")) {
                if (!authorities.contains(UserAccess.UserAuthority.STAFFMEMBER)) {
                    authorities.add(UserAccess.UserAuthority.STAFFMEMBER);
                    info_changed = true;
                }
        }
        else if (eduPersonPrimaryAffiliation.equalsIgnoreCase("employees") ||  eduPersonPrimaryAffiliation.equalsIgnoreCase("affiliate")) {
            if (!authorities.contains(UserAccess.UserAuthority.USER)) {
                authorities.add(UserAccess.UserAuthority.USER);
                info_changed = true;
            }
        }
        else if (eduPersonPrimaryAffiliation.equalsIgnoreCase("student")) {
            if (!authorities.contains(UserAccess.UserAuthority.STUDENT)) {
                authorities.add(UserAccess.UserAuthority.STUDENT);
                info_changed = true;
            }
        }
        else {  //everybody else
            if (!authorities.contains(UserAccess.UserAuthority.USER)) {
                authorities.add(UserAccess.UserAuthority.USER);
                info_changed = true;
            }
        }
        opUser.setAuthorities(authorities);

        if (info_changed) {
            logger.info("User info updated from CAS:" + opUser.getUid());
        }
        return info_changed;
    }
    public boolean isUserReferencedInResources(String staffMemberId) {
        List<Resource> resources = resourceRepository.findAllUserIdReferencesInResources(staffMemberId,1);
        if (resources.size() == 0) {
            return false;
        }
        else {
            logger.warn("Found at least one Resource where user is referenced: " + staffMemberId);
            return true;
        }
    }
    /* used (for now!) to check usage if user in scheduled events as editor or RP. set limit to 1 for quick responses */
    public boolean isUserReferencedInScheduler(String staffMemberId) {
        List<Schedule> schedules = scheduleRepository.findAllUserIdReferencesInScheduler(staffMemberId,1);
        if (schedules.size() == 0) {
            return false;
        }
        else {
            logger.warn("Found at least one Schedule where user is referenced: " + staffMemberId);
            return true;
        }
    }
    public boolean isUserReferencedInScheduledEvents(String editorId) {
        List<ScheduledEvent> scheduledEvents = scheduledEventRepository.findAllUserIdReferencesInScheduledEvents(editorId,1);
        if (scheduledEvents.size() == 0) {
            return false;
        }
        else {
            logger.warn("Found at least one ScheduledEvent where user is referenced: " + editorId);
            return true;
        }
    }

    public List<UserAccess.UserRights.UnitPermission> getManagersUnitPermissions(String id) {
        List<UserAccess.UserRights.UnitPermission> unitPermissions = opUserRepository.getUsersUnitPermissions(id);

        List<UserAccess.UserRights.UnitPermission> error_list = new ArrayList<>();
        for (UserAccess.UserRights.UnitPermission unitPermission: unitPermissions) {
            if (unitPermission.getUnitType().equals(UserAccess.UnitType.DEPARTMENT)) {
                Department department = departmentService.findById(unitPermission.getUnitId());
                if (department != null) {
                    unitPermission.setUnitTitle(department.getTitle());
                }
                else {
                    error_list.add(unitPermission);
                }
            }
            else if (unitPermission.getUnitType().equals(UserAccess.UnitType.SCHOOL)) {
                School school = schoolService.findById(unitPermission.getUnitId());
                if (school != null) {
                    unitPermission.setUnitTitle(school.getTitle());
                }
                else {
                    error_list.add(unitPermission);
                }
            }
            else if (unitPermission.getUnitType().equals(UserAccess.UnitType.INSTITUTION)) {
                unitPermission.setUnitTitle(defaultInstitution.getTitle());
            }
        }
        if (error_list.size() > 0) {
            logger.error("There are: " + error_list.size() + " errors in users unitPermissions: " + id);
            unitPermissions.removeAll(error_list);
        }
        return unitPermissions;
    }
    public List<String> getManagersAuthorizedDepartmentIdsByAccessType(String user_id, String ACCESS_TYPE) {

        List<UserAccess.UserRights.UnitPermission> unitPermissionList = this.getManagersUnitPermissions(user_id);
        // Add all units. Not Just departments in order to cover scheduled events
        List<String> authorized_unit_ids = new ArrayList<>();
        for (UserAccess.UserRights.UnitPermission unitPermission: unitPermissionList) {
            if (ACCESS_TYPE.equals("content") && unitPermission.isContentManager() ||
                    ACCESS_TYPE.equals("scheduler") && unitPermission.isScheduleManager() ||
                    ACCESS_TYPE.equals("data") && unitPermission.isDataManager()) {
                if (unitPermission.getUnitType().equals(UserAccess.UnitType.DEPARTMENT)) {
                    logger.trace("Add Department to Authorized List: " + unitPermission.getUnitTitle());
                    authorized_unit_ids.add(unitPermission.getUnitId());
                }
                else if (unitPermission.getUnitType().equals(UserAccess.UnitType.SCHOOL)) {
                    authorized_unit_ids.add(unitPermission.getUnitId()); //of school
                    List<Department> school_departments = departmentService.findBySchoolId(unitPermission.getUnitId());
                    for (Department department: school_departments) {
                        logger.trace("Add Department Of School " + unitPermission.getUnitTitle()  + " to Authorized List: " + department.getTitle());
                        authorized_unit_ids.add(department.getId());
                    }
                }
                else if (unitPermission.getUnitType().equals(UserAccess.UnitType.INSTITUTION)) {
                    authorized_unit_ids.add("IGNORE_UNIT");
                }
            }
        }
        return authorized_unit_ids;
    }


    private List<UserAccess.UserRights.CoursePermission> getManagersCoursePermissions(String id) {
        List<UserAccess.UserRights.CoursePermission> coursePermissions = opUserRepository.getUsersCoursePermissions(id);
        List<UserAccess.UserRights.CoursePermission> error_list = new ArrayList<>();
        for (UserAccess.UserRights.CoursePermission coursePermission: coursePermissions) {
            OpUser staffMember = opUserRepository.findById(coursePermission.getStaffMemberId()).orElse(null);
            if (staffMember != null) {
                coursePermission.setStaffMemberName(staffMember.getName());
                if (coursePermission.getCourseId().equals("ALL_COURSES") || coursePermission.getCourseId().equals("*")) {
                    List<String> staffMember_courses = staffMember.getCourses();
                    for (String staffMember_course_id: staffMember_courses) {
                        Course course = courseRepository.findById(staffMember_course_id).orElse(null);
                        if (course != null) {
                            coursePermission.setCourseTitle(course.getTitle());
                            coursePermission.setCourseId(course.getId());
                        }
                        else {
                            error_list.add(coursePermission);
                        }
                    }
                }
                else {
                    Course course = courseRepository.findById(coursePermission.getCourseId()).orElse(null);
                    if (course != null) {
                        coursePermission.setCourseTitle(course.getTitle());
                    }
                    else {
                        error_list.add(coursePermission);
                    }
                }
            }
            else {
                error_list.add(coursePermission);
            }
        }
        if (error_list.size() > 0) {
            logger.error("There are: " + error_list.size() + " errors in users coursePermissions: " + id);
            coursePermissions.removeAll(error_list);
        }
        return coursePermissions;
    }
    public List<UserAccess.UserRights.CoursePermission> getManagersCoursePermissionsByAccessType(String id, String ACCESS_TYPE) {
        List<UserAccess.UserRights.CoursePermission> coursePermissions = this.getManagersCoursePermissions(id);
        if (ACCESS_TYPE.equals("IGNORE_ACCESS_TYPE")) {
            return coursePermissions;
        }
        List<UserAccess.UserRights.CoursePermission> coursePermissionsByAccess = new ArrayList<>();
        for (UserAccess.UserRights.CoursePermission coursePermission: coursePermissions) {
            if (ACCESS_TYPE.equals("content") && coursePermission.isContentManager() || ACCESS_TYPE.equals("scheduler") && coursePermission.isScheduleManager()) {
                coursePermissionsByAccess.add(coursePermission);
            }
        }
        return coursePermissionsByAccess;
    }

    private List<UserAccess.UserRights.EventPermission> getManagersEventPermissions(String id) {

        List<UserAccess.UserRights.EventPermission> eventPermissions = opUserRepository.getUsersEventPermissions(id);

        List<UserAccess.UserRights.EventPermission> error_list = new ArrayList<>();
        for (UserAccess.UserRights.EventPermission eventPermission: eventPermissions) {
            OpUser staffMember = opUserRepository.findById(eventPermission.getStaffMemberId()).orElse(null);
            if (staffMember != null) {
                eventPermission.setStaffMemberName(staffMember.getName());
                ScheduledEvent scheduledEvent = scheduledEventRepository.findById(eventPermission.getEventId()).orElse(null);
                if (scheduledEvent != null) {
                    eventPermission.setEventTitle(scheduledEvent.getTitle());
                }
                else {
                    error_list.add(eventPermission);
                }
            }
            else {
                error_list.add(eventPermission);
            }
        }
        if (error_list.size() > 0) {
            logger.error("There are: " + error_list.size() + " errors in users eventPermissions: " + id);
            eventPermissions.removeAll(error_list);
        }
        return eventPermissions;
    }


    public List<UserAccess.UserRights.EventPermission> getManagersEventPermissionsByAccessType(String id, String ACCESS_TYPE) {
        List<UserAccess.UserRights.EventPermission> eventsPermissions = this.getManagersEventPermissions(id);
        if (ACCESS_TYPE.equals("IGNORE_ACCESS_TYPE")) {
            return eventsPermissions;
        }
        List<UserAccess.UserRights.EventPermission> eventPermissionsByAccess = new ArrayList<>();
        for (UserAccess.UserRights.EventPermission eventPermission: eventsPermissions) {
            if (ACCESS_TYPE.equals("content") && eventPermission.isContentManager() || ACCESS_TYPE.equals("scheduler") && eventPermission.isScheduleManager()) {
                eventPermissionsByAccess.add(eventPermission);
            }
        }
        return eventPermissionsByAccess;
    }



    public List<String> getSupporterAuthorizedPersonIdsByAccessType(String user_id, String ACCESS_TYPE) {

        List<UserAccess.UserRights.CoursePermission> coursePermissionList = this.getManagersCoursePermissionsByAccessType(user_id, ACCESS_TYPE);
        // Add all units. Not Just departments in order to cover scheduled events
        List<String> authorized_person_ids = new ArrayList<>();
        for (UserAccess.UserRights.CoursePermission coursePermission: coursePermissionList) {
            if (ACCESS_TYPE.equals("content") && coursePermission.isContentManager()||
                    ACCESS_TYPE.equals("scheduler") && coursePermission.isScheduleManager()) {
                    authorized_person_ids.add(coursePermission.getStaffMemberId());
                }
        }
        return authorized_person_ids;
    }
}
