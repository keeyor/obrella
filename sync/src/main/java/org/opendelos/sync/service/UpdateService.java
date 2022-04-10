/* 
     Author: Michael Gatzonis - 8/11/2021 
     obrella
*/
package org.opendelos.sync.service;

import java.io.StringReader;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.validator.routines.EmailValidator;
import org.opendelos.legacydomain.dlmuser.DlmUser;
import org.opendelos.legacydomain.dlmuser.RoleType;
import org.opendelos.legacydomain.event.Event;
import org.opendelos.legacydomain.event.OrganizerType;
import org.opendelos.legacydomain.institution.Study;
import org.opendelos.legacydomain.institution.XStudies;
import org.opendelos.legacydomain.institution.XStudy;
import org.opendelos.legacydomain.presentation.Presentation;
import org.opendelos.legacydomain.scheduler.DsmCalendarXml;
import org.opendelos.legacydomain.slides.Slides;
import org.opendelos.legacydomain.videolecture.AccessPropertiesType;
import org.opendelos.legacydomain.videolecture.StatusType;
import org.opendelos.legacydomain.videolecture.VideoLecture;
import org.opendelos.legacydomain.xcourse.XCourse;
import org.opendelos.legacydomain.xdepartment.XDepartment;
import org.opendelos.legacydomain.xstaffmember.XStaffMember;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.Cuts;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.PlayerOptions;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.ResourceAccess;
import org.opendelos.model.resources.ResourceStatus;
import org.opendelos.model.resources.ResourceTags;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.resources.Slide;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Subtitles;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.structure.Classroom;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.Device;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.structure.LmsReference;
import org.opendelos.model.structure.School;
import org.opendelos.model.structure.StreamingServer;
import org.opendelos.model.structure.StudyProgram;
import org.opendelos.model.users.UserAccess;
import org.opendelos.sync.legacyrepo.ElegacyRepository;
import org.opendelos.sync.repository.resource.ResourceRepository;
import org.opendelos.sync.services.opUser.OpUserService;
import org.opendelos.sync.services.resource.ResourceService;
import org.opendelos.sync.services.scheduledEvent.ScheduledEventService;
import org.opendelos.sync.services.structure.ClassroomService;
import org.opendelos.sync.services.structure.CourseService;
import org.opendelos.sync.services.structure.DepartmentService;
import org.opendelos.sync.services.structure.SchoolService;
import org.opendelos.sync.services.structure.StreamingServerService;
import org.opendelos.sync.services.structure.StudyProgramService;
import org.xmldb.api.base.Collection;
import org.xmldb.api.modules.XMLResource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("UpdateService")
public class UpdateService {

	private final Logger logger = Logger.getLogger(UpdateService.class.getName());

	@Value("${import_url}")
	String import_url;
	@Value("${app.zone}")
	String app_zone;


	@Value("${current_year_only}")
	boolean current_year_only;

	private final ElegacyRepository elegacyRepository;
	private final SchoolService schoolService;
	private final DepartmentService departmentService;
	private final StudyProgramService studyProgramService;
	private final ClassroomService classroomService;
	private final CourseService courseService;
	private final ResourceService resourceService;
	private final OpUserService opUserService;
	private final ScheduledEventService scheduledEventService;
	private final StreamingServerService streamingServerService;

	private final ResourceRepository resourceRepository;

	public UpdateService(ElegacyRepository elegacyRepository, SchoolService schoolService, DepartmentService departmentService, StudyProgramService studyProgramService, ClassroomService classroomService, CourseService courseService, ResourceService resourceService, OpUserService opUserService, ScheduledEventService scheduledEventService, StreamingServerService streamingServerService, ResourceRepository resourceRepository) {
		this.elegacyRepository = elegacyRepository;
		this.schoolService = schoolService;
		this.departmentService = departmentService;
		this.studyProgramService = studyProgramService;
		this.classroomService = classroomService;
		this.courseService = courseService;
		this.resourceService = resourceService;
		this.opUserService = opUserService;
		this.scheduledEventService = scheduledEventService;
		this.streamingServerService = streamingServerService;
		this.resourceRepository = resourceRepository;
	}

	public void AssignCourses2StaffMember(String triggerUri) {

	  String[] parts = triggerUri.split("-");
	  String staffIdentity = parts[0];
	  String courseIdentity = parts[1];
	  Course course = courseService.findByIdentity(courseIdentity);
	  OpUser staffMember = opUserService.findByIdentity(staffIdentity);
	  if (course != null && staffMember != null) {
		  String[] courseIds = new String[1];
		  courseIds[0] = course.getId();
		  opUserService.assignCoursesToStaffMemberById(staffMember.getId(), courseIds);
	  }
    }

    public void UnAssignCourseFromStaffMember(String triggerUri)  {

		String[] parts = triggerUri.split("-");
		String staffIdentity = parts[0];
		String courseIdentity = parts[1];
		Course course = courseService.findByIdentity(courseIdentity);
		OpUser staffMember = opUserService.findByIdentity(staffIdentity);
		if (course != null && staffMember != null) {
			String courseId = course.getId();
			opUserService.unassignCourseFromStaffMemberById(staffMember.getId(),courseId);
		}
	}

    public void AssignClassroom2Department(String triggerUri)  {

		String[] parts = triggerUri.split("-");
		String departmentIdentity = parts[0];
		String classroomIdentity = parts[1];
		Department department = departmentService.findByIdentity(departmentIdentity);
		Classroom classroom = classroomService.findByIdentity(classroomIdentity);
		if (department != null && classroom != null) {
			String departmentId = department.getId();
			String classroomId = classroom.getId();
			departmentService.addClassroomToDepartment(departmentId,classroomId);
		}
	}

	public void UnAssignClassroomFromDepartment(String triggerUri)  {

		String[] parts = triggerUri.split("-");
		String departmentIdentity = parts[0];
		String classroomIdentity = parts[1];
		Department department = departmentService.findByIdentity(departmentIdentity);
		Classroom classroom = classroomService.findByIdentity(classroomIdentity);
		if (department != null && classroom != null) {
			String departmentId = department.getId();
			String classroomId = classroom.getId();
			departmentService.removeClassroomFromDepartment(departmentId,classroomId);
		}
	}

	public void CreateSchool(String schoolIdentity, org.opendelos.legacydomain.institution.Institution legacyInstitution) throws  Exception {

		if (legacyInstitution != null) {
			for (org.opendelos.legacydomain.institution.School oschool : legacyInstitution.getSchools().getSchool()) {
				if (oschool.getId().equals(schoolIdentity)) {
					School new_school = new School();
					new_school.setIdentity(oschool.getId());
					new_school.setTitle(oschool.getName());
					schoolService.create(new_school);
					break;
				}
			}
		}
	}

	public void UpdateSchool(String schoolIdentity, org.opendelos.legacydomain.institution.Institution legacyInstitution)  {

		School school = schoolService.findByIdentity(schoolIdentity);
		if (school != null) {
			if (legacyInstitution != null) {
				for (org.opendelos.legacydomain.institution.School oschool : legacyInstitution.getSchools()
						.getSchool()) {
					if (oschool.getId().equals(schoolIdentity)) {
						school.setTitle(oschool.getName());
						school.setIdentity(oschool.getId());
						schoolService.update(school);
						break;
					}
				}
			}
		}
	}

	public void CreateDepartment(String triggerUri, String institution_id, org.opendelos.legacydomain.institution.Institution legacyInstitution) throws Exception {

		String[] parts = triggerUri.split("-");
		String departmentIdentity = parts[0];
		String schoolIdentity = parts[1];

		if (legacyInstitution != null) {
			XDepartment xDepartment = elegacyRepository.getInstitutionDepartment(legacyInstitution, departmentIdentity);
			if (xDepartment != null && xDepartment.getDepartment() != null) {
				org.opendelos.legacydomain.institution.Department odepartment = xDepartment.getDepartment();
				Department new_department = new Department();
				new_department.setIdentity(odepartment.getId());
				new_department.setTitle(odepartment.getName());
				new_department.setInstitutionId(institution_id);
				School school1 = schoolService.findByIdentity(schoolIdentity);
				new_department.setSchoolId(school1.getId());
				if (odepartment.getPassword() != null)
					new_department.setPassword(odepartment.getPassword());
				if (odepartment.getUrl() != null)
					odepartment.setUrl(odepartment.getUrl());
				departmentService.create(new_department);
			}
		}
	}

	public void UpdateDepartment(String departmentIdentity, org.opendelos.legacydomain.institution.Institution legacyInstitution)  {

		Department department = departmentService.findByIdentity(departmentIdentity);

		if (legacyInstitution != null) {
			XDepartment xDepartment = elegacyRepository.getInstitutionDepartment(legacyInstitution, departmentIdentity);
			if (xDepartment != null && xDepartment.getDepartment() != null && department != null) {
				org.opendelos.legacydomain.institution.Department odepartment = xDepartment.getDepartment();
				if (!odepartment.getId().equals(departmentIdentity) || !odepartment.getName().equals(department.getTitle())) {
					department.setTitle(odepartment.getName());
					department.setIdentity(odepartment.getId());
					department.setPassword(odepartment.getPassword());
					department.setUrl(odepartment.getUrl());
					departmentService.findAndUpdate(department);
				}
				else {
					department.setPassword(odepartment.getPassword());
					department.setUrl(odepartment.getUrl());
					departmentService.update(department);
				}
			}
		}
	}

	public void CreateStudyProgram(String triggerUri, org.opendelos.legacydomain.institution.Institution legacyInstitution)  {

		String[] parts = triggerUri.split("-");
		String schoolIdentity = parts[0];
		String departmentIdentity = parts[1];
		String studyIdentity = parts[2];

		if (legacyInstitution != null) {
			XStudies xStudies = elegacyRepository.getDepartmentStudies(legacyInstitution, departmentIdentity);
			for (XStudy xStudy: xStudies.getXStudy())
				if (xStudy.getStudy().getStudyIdentity().equals(studyIdentity)) {
					Study ostudy = xStudy.getStudy();
					StudyProgram studyProgram = new StudyProgram();
					studyProgram.setTitle(ostudy.getTitle());
					studyProgram.setIdentity(studyIdentity);
					School school1 = schoolService.findByIdentity(schoolIdentity);
					studyProgram.setSchoolId(school1.getId());
					Department department1 = departmentService.findByIdentity(departmentIdentity);
					studyProgram.setDepartmentId(department1.getId());
					studyProgram.setStudy("under");
					studyProgramService.create(studyProgram);
					break;
				}
		}
	}

	public void UpdateStudyProgram(String triggerUri, org.opendelos.legacydomain.institution.Institution legacyInstitution)  {

		String[] parts = triggerUri.split("-");
		String departmentIdentity = parts[1];
		String studyIdentity = parts[2];

		StudyProgram studyProgram = studyProgramService.findByIdentity(studyIdentity);
		if (legacyInstitution != null && studyProgram != null) {
			XStudies xStudies = elegacyRepository.getDepartmentStudies(legacyInstitution, departmentIdentity);
			for (XStudy xStudy: xStudies.getXStudy())
				if (xStudy.getStudy().getStudyIdentity().equals(studyIdentity)) {
					Study ostudy = xStudy.getStudy();
					//only title can change in legacy app!
					studyProgram.setTitle(ostudy.getTitle());
					studyProgramService.findAndUpdate(studyProgram);
					break;
				}
		}
	}

	public void DeleteStudyProgram(String studyProgramIdentity) throws Exception {

		StudyProgram studyProgram1 = studyProgramService.findByIdentity(studyProgramIdentity);
		studyProgramService.delete(studyProgram1.getId());
	}

	public void CreateUpdateStaffMember(String staffIdentity, String action, org.opendelos.legacydomain.institution.Institution legacyInstitution) {

		if (legacyInstitution != null) {
			XStaffMember xStaffMember = elegacyRepository.getStaffMemberbyId(legacyInstitution, staffIdentity);
			if (xStaffMember != null && xStaffMember.getStaffMember() != null) {
				Department department = departmentService.findByIdentity(xStaffMember.getDepartmentId());
				Unit unit = new Unit(StructureType.DEPARTMENT, department.getTitle(), department.getId());
				this.CreateStaffMember(xStaffMember.getStaffMember(), legacyInstitution, unit, action);
			}
			else {
				logger.severe("Could not create NEW StaffMember");
			}
		}
	}

	public void DeleteStaffMember(String staffIdentity, org.opendelos.legacydomain.institution.Institution legacyInstitution) throws Exception {

		if (legacyInstitution != null) {
				OpUser opUser = opUserService.findByIdentity(staffIdentity);
				opUserService.delete(opUser.getId());
		}
	}

	public int CreateStaffMember(org.opendelos.legacydomain.institution.StaffMember oStaff,org.opendelos.legacydomain.institution.Institution legacyInstitution, Unit unit, String action) {

		try {
			if (oStaff.getSId() == null || oStaff.getSId().equals("null")) {
				logger.info("Invalid SID. SKipping" + " NAME:" + oStaff.getName());
				return 0;
			}
			if (!EmailValidator.getInstance().isValid(oStaff.getEmail())) {
				logger.info("(Validator) Invalid EMAIL. SKipping" + oStaff.getEmail());
				return 0;
			}
			//#check if there is already an opUser for this staffMember
			int indexOfPapi = oStaff.getEmail().lastIndexOf("@");
			if (indexOfPapi == -1) {
				logger.info("Invalid EMAIL. SKipping= " + oStaff.getEmail());
				return 0;
			}
			if (oStaff.getEmail().startsWith("-") || oStaff.getEmail().startsWith("0")) {
				logger.info("Invalid EMAIL. SKipping= " + oStaff.getEmail());
				return 0;
			}
			String dlmUser_Uid = oStaff.getEmail().substring(0, indexOfPapi);
			OpUser opUser = opUserService.findByUid(dlmUser_Uid);

			if (opUser == null) {
				OpUser staffMember = new OpUser();
				List<String> identities = new ArrayList<>();
				identities.add(oStaff.getSId());
				staffMember.setIdentity(identities);
				staffMember.setUid(dlmUser_Uid);
				staffMember.setEmail(oStaff.getEmail());
				staffMember.setDepartment(unit);
				staffMember.setName(oStaff.getName().trim());
				staffMember.setAltName(oStaff.getAltName().trim());
				staffMember.setAffiliation(oStaff.getAffiliation().get(0));
				List<String> personAffiliations = new ArrayList<>();
				personAffiliations.add("faculty");
				staffMember.setEduPersonAffiliation(personAffiliations);
				staffMember.setEduPersonPrimaryAffiliation("faculty");
				List<UserAccess.UserAuthority> staffMemberAuthorities = new ArrayList<>();
				staffMemberAuthorities.add(UserAccess.UserAuthority.STAFFMEMBER);
				staffMember.setAuthorities(staffMemberAuthorities);
				UserAccess.UserRights staffMemberRights = new UserAccess.UserRights();
				staffMemberRights.setIsSa(false);
				staffMember.setRights(staffMemberRights);
				//#Teaching Courses
				List<String> course_ids_teaching = new ArrayList<>(); // Wipe-out to get Updates
				if (oStaff.getStaffMemberCourses() != null) {
					for (int sm = 0; sm < oStaff.getStaffMemberCourses().size(); sm++) {
						String course_identity = oStaff.getStaffMemberCourses().get(sm)
								.getCId();
						Course course_user_teaching = courseService.findByIdentity(course_identity);
						if (course_user_teaching != null) {
							course_ids_teaching.add(course_user_teaching.getId());
							//update course with teacher counter
							int tCounter = course_user_teaching.getTeachingCounter();
							course_user_teaching.setTeachingCounter(tCounter+1);
							courseService.update(course_user_teaching);
						}
					}
				}
				staffMember.setCourses(course_ids_teaching);
				//# get ACTIVE status and Last Login from DlmUser if exists
				DlmUser dlmUser_as_staff = null;
				try {
					Collection col = elegacyRepository.getDatabaseCollection(import_url, "guest", "guest", "/db/apps/delos-uoa/Users");
					dlmUser_as_staff = (DlmUser) elegacyRepository.GetDataBaseObject(col, oStaff.getSId(), DlmUser.class);
				}
				catch (Exception ignored) {}
				if (dlmUser_as_staff != null) {
					staffMember.setActive(dlmUser_as_staff.getStatus().equals("ACTIVE"));
					if (dlmUser_as_staff.getLastVisit() != null && !dlmUser_as_staff.getLastVisit().equals("-1")) {
						DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
						Date formDate = format.parse(dlmUser_as_staff.getLastVisit());
						staffMember.setLastLogin(formDate.toInstant());
					}
				}
				else {
					staffMember.setActive(false);
				}
				opUserService.create(staffMember);
				return 1;
			}
			else {
				logger.info("Duplicate/Update:" + opUser.getName() + " ID:" + dlmUser_Uid);
				XStaffMember xStaffMember = elegacyRepository.getStaffMemberbyId(legacyInstitution, oStaff.getSId());
				if (xStaffMember != null && xStaffMember.getStaffMember() != null) {
					if (xStaffMember.getStaffMember().getStaffMemberCourses() != null) {
						//# add additional Courses
						List<String> course_ids_teaching;
						if (action != null && action.equals("UPDATE")) {
							course_ids_teaching = new ArrayList<>();
							opUser.setEmail(oStaff.getEmail());
							opUser.setDepartment(unit);
							opUser.setName(oStaff.getName());
							opUser.setAltName(oStaff.getAltName());
						}
						else {
							course_ids_teaching = opUser.getCourses();
						}
						for (int sm = 0; sm < xStaffMember.getStaffMember()
								.getStaffMemberCourses().size(); sm++) {
							String course_identity = xStaffMember.getStaffMember()
									.getStaffMemberCourses().get(sm).getCId();
							Course course_user_teaching = courseService.findByIdentity(course_identity);
							if (course_user_teaching != null && !course_ids_teaching.contains(course_user_teaching.getId())) {
								course_ids_teaching.add(course_user_teaching.getId());
							}
						}
						opUser.setCourses(course_ids_teaching); //# update teaching courses
					}
					if (action == null) {
						//# get ACTIVE status and Last Login from additional DlmUser if exists
						DlmUser dlmUser_as_staff = null;
						try {
							Collection col = elegacyRepository.getDatabaseCollection(import_url, "guest", "guest", "/db/apps/delos-uoa/Users");
							dlmUser_as_staff = (DlmUser) elegacyRepository.GetDataBaseObject(col, oStaff.getSId(), DlmUser.class);
						}
						catch (Exception ignored) {}

						if (dlmUser_as_staff != null) {
							opUser.setActive(dlmUser_as_staff.getStatus().equals("ACTIVE"));
							if (dlmUser_as_staff.getLastVisit() != null && !dlmUser_as_staff.getLastVisit().equals("-1")) {
								DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
								Date formDate = format.parse(dlmUser_as_staff.getLastVisit());
								Instant last_visit = formDate.toInstant();
								if (opUser.getLastLogin() == null || opUser.getLastLogin().isBefore(last_visit)) {
									opUser.setLastLogin(last_visit);
								}
							}
						}
						if (!opUser.getIdentity().contains(oStaff.getSId())) {
							opUser.getIdentity().add(oStaff.getSId());
						}
					}
					opUserService.update(opUser);
					if (action != null && action.equals("UPDATE")) { //update related resources
						opUserService.findAndUpdate(opUser);
					}
					return 2;
				}
			}
		}
		catch (Exception e) {
			logger.severe("exception in oStaff:" + oStaff.getSId());
			return -1;
		}
		return 0;
	}

	public void CreateCourse(String triggerUri, String institution_id, org.opendelos.legacydomain.institution.Institution legacyInstitution)  {

		String[] parts = triggerUri.split("-");
		String schoolIdentity = parts[0];
		String departmentIdentity = parts[1];
		String courseIdentity = parts[2];


		if (legacyInstitution != null) {
			XCourse xCourse = elegacyRepository.getDepartmentCourse(legacyInstitution, departmentIdentity, courseIdentity);
			if (xCourse != null && xCourse.getCourse() != null) {
				org.opendelos.legacydomain.institution.Course ocourse = xCourse.getCourse();
				Course course = new Course();
				course.setTitle(ocourse.getTitle().trim());
				course.setIdentity(ocourse.getCId());
				try {
					course.setScopeId(ocourse.getUId()); 	// may be null
				}
				catch (Exception npe) {
					course.setScopeId(ocourse.getCId());    //set default cid for scopeId
				}
				course.setInstitutionId(institution_id);
				School school1 = schoolService.findByIdentity(schoolIdentity);
				course.setSchoolId(school1.getId());
				Department department1 = departmentService.findByIdentity(departmentIdentity);
				course.setDepartment(new Unit(StructureType.DEPARTMENT, department1.getId(), department1.getTitle()));
				course.setSemester(ocourse.getSemester());
				course.setTeachingCounter(0);
				//LMS & SEC Codes
				List<LmsReference> lmsReferences = new ArrayList<>(); //Wipe-out to get updates
				List<org.opendelos.legacydomain.institution.Course.CourseId> courseIds = ocourse.getCourseId();
				if (courseIds != null && courseIds.size() > 0) {
					for (org.opendelos.legacydomain.institution.Course.CourseId courseId : ocourse.getCourseId()) {
						if (courseId.getSource().equals("sec")) {
							course.setScopeId(courseId.getValue());
						}
						else if (courseId.getSource().equals("lms")) {
							String[] multipleLMS = courseId.getValue().split(",");
							for (String lms : multipleLMS) {
								String[] lmsparts = lms.split("::");
								LmsReference lmsReference = new LmsReference();
								if (lmsparts.length == 1) {
									lmsReference.setLmsCode(lmsparts[0]);
									lmsReference.setLmsId("eclass.uoa.gr");
								}
								else {
									lmsReference.setLmsCode(lmsparts[1]);
									lmsReference.setLmsId(lmsparts[0]);
								}
								lmsReferences.add(lmsReference);
							}
						}
					}
				}
				course.setLmsReferences(lmsReferences);
				courseService.create(course);
			}
		}
	}

	public void UpdateCourse(String triggerUri, org.opendelos.legacydomain.institution.Institution legacyInstitution) {

		String[] parts = triggerUri.split("-");
		String departmentIdentity = parts[0];
		String courseIdentity = parts[1];
		Course course = courseService.findByIdentity(courseIdentity);

		if (legacyInstitution != null && course != null) {
			XCourse xCourse = elegacyRepository.getDepartmentCourse(legacyInstitution, departmentIdentity, courseIdentity);
			if (xCourse != null && xCourse.getCourse() != null) {
				org.opendelos.legacydomain.institution.Course ocourse = xCourse.getCourse();
				boolean findAndUpdate = !course.getTitle()
						.equals(ocourse.getTitle()) || !course.getScopeId()
						.equals(ocourse.getUId()) || !course.getSemester()
						.equals(ocourse.getSemester());
				course.setTitle(ocourse.getTitle());
				course.setIdentity(ocourse.getCId());
				try {
					course.setScopeId(ocourse.getUId()); // may be null
				}
				catch (Exception npe) {
					course.setScopeId(ocourse.getCId());    //set default cid for scopeId
				}
				course.setSemester(ocourse.getSemester());
				//LMS & SEC Codes
				List<LmsReference> lmsReferences = new ArrayList<>(); //Wipe-out to get updates
				List<org.opendelos.legacydomain.institution.Course.CourseId> courseIds = ocourse.getCourseId();
				if (courseIds != null && courseIds.size() > 0)
					for (org.opendelos.legacydomain.institution.Course.CourseId courseId : ocourse.getCourseId()) {
						if (courseId.getSource().equals("sec")) {
							course.setScopeId(courseId.getValue());
						}
						else if (courseId.getSource().equals("lms")) {
							String[] multipleLMS = courseId.getValue().split(",");
							for (String lms : multipleLMS) {
								String[] lmsparts = lms.split("::");
								LmsReference lmsReference = new LmsReference();
								if (lmsparts.length == 1) {
									lmsReference.setLmsCode(lmsparts[0]);
									lmsReference.setLmsId("eclass.uoa.gr");
								}
								else {
									lmsReference.setLmsCode(lmsparts[1]);
									lmsReference.setLmsId(lmsparts[0]);
								}
								lmsReferences.add(lmsReference);
							}
						}
					}
				course.setLmsReferences(lmsReferences);
				if (findAndUpdate) {
					logger.info("update course trigger - UPDATE relevant");
					courseService.findAndUpdate(course);
				}
				else {
					courseService.update(course);
				}
			}
		}
	}

    public void DeleteCourse(String courseIdentity) throws Exception {

		Course course1 = courseService.findByIdentity(courseIdentity);
		courseService.delete(course1.getId());
	}


	public void CreateClassroom(String classroomIdentity, org.opendelos.legacydomain.institution.Institution legacyInstitution)  {

		if (legacyInstitution != null) {
			for (org.opendelos.legacydomain.institution.Classroom oClassroom : legacyInstitution.getClassrooms().getClassroom()) {
				if (oClassroom.getId().equals(classroomIdentity)) {
					Classroom classroom = new Classroom();
					classroom.setName(oClassroom.getName());
					classroom.setCode(oClassroom.getCodeName());
					classroom.setIdentity(classroomIdentity);
					classroom.setDescription(oClassroom.getDescription());
					classroom.setLocation(oClassroom.getLocation());
					classroom.setMap(oClassroom.getMap());
					classroom.setCalendar(oClassroom.getCalendar());
					classroom.setUsage("both");

					List<Device> classroomDevices = new ArrayList<>();
					if (oClassroom.getDevices() != null) {
						for (org.opendelos.legacydomain.institution.Device odevice : oClassroom.getDevices()
								.getDevice()) {
							Device nDevice = new Device();
							nDevice.setType(odevice.getType());
							nDevice.setDescription(odevice.getDescription());
							nDevice.setIpAddress(odevice.getIPAddress());
							nDevice.setMacAddress(odevice.getMACAddress());
							nDevice.setTechnology(odevice.getTechnology());
							nDevice.setSocket(odevice.getSocket());
							nDevice.setStreamAccessUrl(null);
							classroomDevices.add(nDevice);
						}
					}
					classroom.setDevices(classroomDevices);
					classroomService.create(classroom);
					break;
				}
			}
		}
	}

	public void UpdateClassroom(String classroomIdentity, org.opendelos.legacydomain.institution.Institution legacyInstitution)  {

		Classroom classroom = classroomService.findByIdentity(classroomIdentity);
		if (classroom != null) {

			if (legacyInstitution != null) {
				for (org.opendelos.legacydomain.institution.Classroom oClassroom : legacyInstitution.getClassrooms().getClassroom()) {
					if (oClassroom.getId().equals(classroomIdentity)) {
						classroom.setName(oClassroom.getName());
						classroom.setCode(oClassroom.getCodeName());
						classroom.setIdentity(classroomIdentity);
						classroom.setDescription(oClassroom.getDescription());
						classroom.setLocation(oClassroom.getLocation());
						classroom.setMap(oClassroom.getMap());
						classroom.setCalendar(oClassroom.getCalendar());
						classroom.setUsage("both");
						List<Device> classroomDevices = new ArrayList<>();
						if (oClassroom.getDevices() != null) {
							for (org.opendelos.legacydomain.institution.Device odevice : oClassroom.getDevices().getDevice()) {
								Device nDevice = new Device();
								nDevice.setType(odevice.getType());
								nDevice.setDescription(odevice.getDescription());
								nDevice.setIpAddress(odevice.getIPAddress());
								nDevice.setMacAddress(odevice.getMACAddress());
								nDevice.setTechnology(odevice.getTechnology());
								nDevice.setSocket(odevice.getSocket());
								nDevice.setStreamAccessUrl(null);
								classroomDevices.add(nDevice);
							}
						}
						classroom.setDevices(classroomDevices);
						classroomService.update(classroom);
						break;
					}
				}
			}
		}
	}

	public void DeleteClassroom(String classroomIdentity) throws Exception {

		Classroom classroom1 = classroomService.findByIdentity(classroomIdentity);
		classroomService.delete(classroom1.getId());
	}


	private void createUpdateUser(DlmUser dlmUser, String triggerEvent, Institution institution) throws Exception {

		if (!triggerEvent.equals("after-create-document") && !triggerEvent.equals("after-update-document") && !triggerEvent.equals("after-delete-document")) {
			logger.warning("Unknown Trigger Type. Skipping...");
		}
		if (dlmUser == null) {
			logger.severe("Invalid DlmUser: Null. Skipping import");
			return;
		}
		if (triggerEvent.equals("after-create-document")  || triggerEvent.equals("after-update-document")) {
			if (dlmUser.getUserRights() != null) {
				boolean res;
				if (dlmUser.getUserRights().get(0).getRole() != RoleType.CM){ // HANDLE CM ELSEWHERE
					if (triggerEvent.equals("after-create-document")) {
						res = this.Create_NONSTAFFMEMBER(dlmUser, institution,"after-create-document");
					}
					else {
						res = this.Create_NONSTAFFMEMBER(dlmUser, institution,"after-update-document");
					}
					if (res) {
						logger.info("UPDATED NONSTAFFMEMBER: " + triggerEvent);
					}
					else {
						logger.severe("ERROR updating NONSTAFFMEMBER: " + triggerEvent);
					}
				}
			}
		}
	}

	public void DeleteUser(String triggerDocument) {

		OpUser opUser = opUserService.findByIdentity(triggerDocument);
		if (opUser != null) {
			try {
				opUserService.delete(opUser.getId());
			}
			catch (Exception e) {
				logger.warning("Failed to delete user:" + e.getMessage());
			}
		}
	}


	public void createUpdateScheduledEvent(Event oevent, String triggerEvent, org.opendelos.legacydomain.institution.Institution legacyInstitution, String institution_id) {

		if (!triggerEvent.equals("after-create-document") && !triggerEvent.equals("after-update-document")) {
			logger.warning("Unknown Trigger Type. Skipping...");
		}
		if (oevent == null) {
			logger.severe("Invalid Resource: Null. Skipping import");
			return;
		}

		ScheduledEvent scheduledEvent;

		boolean create_new = false;
		scheduledEvent = scheduledEventService.findByIdentity(oevent.getIdentifier());
		if (scheduledEvent == null) {
			scheduledEvent = new ScheduledEvent();
			create_new = true;
		}

		try {
			scheduledEvent.setIdentity(oevent.getIdentifier());
			scheduledEvent.setTitle(oevent.getTitle());
			switch (oevent.getType()) {
			case "seminar":
				scheduledEvent.setType("uas_sem");
				break;
			case "conference":
				scheduledEvent.setType("uas_sconf");
				break;
			case "workshop":
				scheduledEvent.setType("uas_work");
				break;
			case "events":
				scheduledEvent.setType("uas_other");
				break;
			default:
				scheduledEvent.setType("uas_other");
				break;
			}
			// Organizers
			List<Unit> unitList = new ArrayList<>();
			for (int j = 0; j < oevent.getOrganizer().size(); j++) {
				OrganizerType oType = oevent.getOrganizer().get(j);
				if (oType.getStructure().equals(org.opendelos.legacydomain.videolecture.StructureType.Other)) {
					Unit unit = new Unit(StructureType.OTHER, "", oType.getName());
					unitList.add(unit);
				}
				else if (oType.getStructure()
						.equals(org.opendelos.legacydomain.videolecture.StructureType.Department)) {
					Department department = departmentService.findByIdentity(oType.getIdentity());
					if (department != null) {
						Unit unit = new Unit(StructureType.DEPARTMENT, department.getId(), department.getTitle());
						unitList.add(unit);
					}
					else {
						logger.warning(String.format("EVENT> Warning. Organiser-Department (%s) of Event (%s) not found. Skipping Import!",
								oType.getIdentity(), oevent.getIdentifier()));
					}
				}
				else if (oType.getStructure()
						.equals(org.opendelos.legacydomain.videolecture.StructureType.School)) {
					School school = schoolService.findByIdentity(oType.getIdentity());
					if (school != null) {
						Unit unit = new Unit(StructureType.SCHOOL, school.getId(), school.getTitle());
						unitList.add(unit);
					}
					else {
						logger.warning(String.format("EVENT> Warning. Organiser-School (%s) of Event (%s) not found. Skipping Import!", oType.getIdentity(),
								oevent.getIdentifier()));
					}
				}
				else if (oType.getStructure()
						.equals(org.opendelos.legacydomain.videolecture.StructureType.Institution)) {
					Unit unit = new Unit(StructureType.INSTITUTION, institution_id, legacyInstitution.getName());
					unitList.add(unit);
				}
			}
			scheduledEvent.setResponsibleUnit(unitList);

			// Manager aka Responsible Person
			if (oevent.getResponsiblePerson() != null && oevent.getResponsiblePerson()
					.getIdentity() != null && !oevent.getResponsiblePerson().getIdentity().equals("")) {
				String responsiblePersonIdentity = oevent.getResponsiblePerson().getIdentity();
				OpUser ooUser = opUserService.findByIdentity(responsiblePersonIdentity);
				if (ooUser != null) {
					Unit unit = ooUser.getDepartment();
					scheduledEvent.setResponsiblePerson(new Person(ooUser.getId(), ooUser.getName(), ooUser.getAffiliation(), unit));
				}
			}
			if (scheduledEvent.getResponsiblePerson() == null) {
				//try creator
				String creatorIdentity = oevent.getCreator().getIdentity();
				OpUser ooUserCreator = opUserService.findByIdentity(creatorIdentity);
				if (ooUserCreator == null) {
					logger.warning(String.format("EVENTS> Import Event. Resource (%s): Invalid Creator & ResponsiblePerson (%s). Skipping import!",
							oevent.getIdentifier(), creatorIdentity));
					return;
				}
				else {
					Unit unit = ooUserCreator.getDepartment();
					scheduledEvent.setResponsiblePerson(new Person(ooUserCreator.getId(), ooUserCreator.getName(), ooUserCreator.getAffiliation(), unit));
				}
			}
			//Dates
			DateTimeFormatter f = DateTimeFormatter.ofPattern("uuuu-MM-dd");
			ZoneId z = ZoneId.of("Europe/Athens");
			if (oevent.getStartDate() != null && !oevent.getStartDate().equals("")) {
				String input = "";
				try {
					input = oevent.getStartDate();
					LocalDate ld = LocalDate.parse(input, f);
					LocalDateTime ldt = ld.atStartOfDay();
					ZonedDateTime zdt = ldt.atZone(z);
					scheduledEvent.setStartDate(zdt.toInstant().plusSeconds(zdt.getOffset().getTotalSeconds()));
				}
				catch (Exception npe) {
					logger.warning("Identifier:" + oevent.getIdentifier() + " Msg:" + npe.getMessage() + " Date Parsed:" + input);
				}
			}
			if (oevent.getEndDate() != null && !oevent.getEndDate().equals("")) {
				String input;
				try {
					input = oevent.getEndDate();
					LocalDate ld = LocalDate.parse(input, f);
					LocalDateTime ldt = ld.atStartOfDay();
					ZonedDateTime zdt = ldt.atZone(z);
					scheduledEvent.setEndDate(zdt.toInstant().plusSeconds(zdt.getOffset().getTotalSeconds()));
				}
				catch (Exception npe) {
					//logger.warning("Identifier:" + oevent.getIdentifier() + " Msg:" + npe.getMessage() + " Date Parsed:" + input);
				}
			}
			//AccessPolicy [i.e. password]
			if (scheduledEvent.getAccessPolicy() != null) {
				scheduledEvent.setAccessPolicy(oevent.getAccessPolicy());
			}
			else {
				scheduledEvent.setAccessPolicy("");
			}
			//Editor
			String editorIdentity = oevent.getEditor().getIdentity();
			OpUser ooUserEditor = opUserService.findByIdentity(editorIdentity);
			if (ooUserEditor == null) {
				logger.warning(String.format("EVENTS> Import Event. Resource (%s): Invalid Editor (%s). Skipping import!", oevent.getIdentifier(), editorIdentity));
				return;
			}
			scheduledEvent.setEditor(new Person(ooUserEditor.getId(), ooUserEditor.getName(), ooUserEditor.getAffiliation()));
			//Status
			if (oevent.isActive() != null) {
				scheduledEvent.setIsActive(oevent.isActive());
			}
			else {
				scheduledEvent.setIsActive(false);
			}
		}
		catch (Exception e) {
			logger.severe("Error Importing Scheduled Event:" + Arrays.toString(e.getStackTrace()));
			return;
		}

		if (create_new) {
			scheduledEventService.create(scheduledEvent);
			logger.fine(" NEW SCHEDULED EVENT WITH ID:" + scheduledEvent.getId() + " CREATED ");
		}
		else {
			scheduledEventService.update(scheduledEvent);
			//scheduledEventService.findAndUpdate(scheduledEvent); // update resources with changed event...NOT NEEDED? Will update with Resource
			logger.fine(" UPDATED SCHEDULED EVENT WITH ID:" + scheduledEvent.getId());
		}
	}

	public int createUpdateDocument(VideoLecture videoLecture, String triggerEvent, String institution_id) {

		Resource resourceV4;

		if (!triggerEvent.equals("after-create-document") && !triggerEvent.equals("after-update-document")) {
			logger.warning("Unknown Trigger Type. Skipping...");
		}
		if (videoLecture == null) {
			logger.severe("Invalid Resource: Null. Skipping import");
			return -1;
		}
		if (videoLecture.getRelation() == null || (videoLecture.getRelation().getCourse() == null && videoLecture.getRelation().getEvent() == null)) {
			logger.severe("Invalid Resource: No Course and no Event. Skipping import");
			return -1;
		}

		boolean create_new = false;
		resourceV4 = resourceService.findByIdentity(videoLecture.getIdentifier());
		if (resourceV4 == null) {
			resourceV4 = new Resource();
			create_new = true;
		}

		try {
			resourceV4.setTitle(videoLecture.getTitle());
			resourceV4.setDescription(videoLecture.getDescription());
			resourceV4.setIdentity(videoLecture.getIdentifier());
			//*date + year

			//CHECK YEAR ONLY FOR COURSES ( NOT! EVENTS!!)
			DateTimeFormatter f = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
			ZoneId z = ZoneId.of("Europe/Athens");
			int academicYear = ZonedDateTime.now(z).getYear();



				if (videoLecture.getDate() != null && !videoLecture.getDate().equals("")) {
					Instant instant = this.setDate(videoLecture.getDate(), videoLecture.getTime(), f, z);
					resourceV4.setDate(instant);
					//#get Academic Year
					ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, z);
					academicYear = zdt.getYear();
					int calendarMonth = zdt.getMonth().getValue();
					if (calendarMonth > 8) {
						academicYear++;
					}
					academicYear = academicYear - 1;
					resourceV4.setAcademicYear(Integer.toString(academicYear));
					if (videoLecture.getRelation().getCourse() != null) {
						if (current_year_only) {
							if (!Integer.toString(academicYear).equals("2021") && !Integer.toString(academicYear)
									.equals("2022")) {
								logger.info("NOT FOR THIS ACADEMIC YEAR... SKIP!!!: " + academicYear);
								return -1;
							}
						}
					}
				}



			//# Classroom
			if (videoLecture.getRoom() != null && videoLecture.getRoom().getId() != null && !videoLecture.getRoom().getId().equals("")) {
				Classroom classroom = classroomService.findByIdentity(videoLecture.getRoom().getId());
				if (classroom != null) {
					resourceV4.setClassroom(classroom.getId());
				}
			}
 			if (!videoLecture.getSortOrder().getDescription().equals("not-applicable")) {
				resourceV4.setParts(true);
				try {
					int partNumber = Integer.parseInt(videoLecture.getSortOrder().getOrder());
					resourceV4.setPartNumber(partNumber);
				}
				catch (Exception ignored) {

				}
			}
			resourceV4.setInstitution(institution_id);
			//# editor
			String editorIdentity = videoLecture.getRights().getEditor().getIdentity();
			OpUser editor = opUserService.findByIdentity(editorIdentity);
			if (editor != null) {
				resourceV4.setEditor(new Person(editor.getId(), editor.getName(), editor.getAffiliation(), editor.getDepartment()));
			}
			else { //set Sa
				logger.fine("Editor:" + editorIdentity + " not found: Setting SA as default");
				editor = opUserService.findByUid("sa");
				resourceV4.setEditor(new Person(editor.getId(), editor.getName(), editor.getAffiliation(), editor.getDepartment()));
			}

			//* Language
			if (videoLecture.getLanguage() != null) {
				resourceV4.setLanguage(videoLecture.getLanguage());
			}

			//*date-modified
			if (videoLecture.getDateModified() != null && !videoLecture.getDateModified().equals("")) {
				Instant instant = this.setDate(videoLecture.getDateModified(), videoLecture.getTimeModified(), f, z);
				resourceV4.setDateModified(instant);
			}
			//*topics (keywords)
			if (videoLecture.getSubject() != null) {
				List<String> topics = new ArrayList<>(videoLecture.getSubject());
				resourceV4.setTopics(topics.toArray(new String[0]));
			}
			//*categories
			if (videoLecture.getCategory() != null) {
				List<String> categories = new ArrayList<>(videoLecture.getCategory());
				resourceV4.setCategories(categories.toArray(new String[0]));
			}
			//*access Policy
			resourceV4.setAccessPolicy(videoLecture.getRights().getSecurity());
			//*license
			resourceV4.setLicense(videoLecture.getLicenses().getIdentity());
			//Resource Stats
			resourceV4.setStatistics(videoLecture.getViews());
			//Resource Status
			resourceV4.setStatus(this.getResourceStatus(videoLecture));
			//Tags
			resourceV4.setTags(this.getResourceTags(videoLecture));

			//*storageLocation
			resourceV4.setStorage(videoLecture.getIdentifier());
			//*playerOptions
			PlayerOptions playerOptions = new PlayerOptions();
			try {
				playerOptions.setOverlay(Boolean.parseBoolean(videoLecture.getPlayerOptions().getOverlay()));
			}
			catch (Exception npe) {
				playerOptions.setOverlay(false);
			}
			try {
				//playerOptions.setShowLicenseIntro(Boolean.parseBoolean(videoLecture.getLicenses().getShowIntro()));
				playerOptions.setShowLicenseIntro(false); // !! for now because license files are missing!
			}
			catch (Exception npe) {
				playerOptions.setShowLicenseIntro(false);
			}
			resourceV4.setPlayerOptions(playerOptions);
			//*resource Multimedia
			if (videoLecture.getAccess() != null && videoLecture.getAccess()
					.getProperties() != null && videoLecture.getAccess().getProperties().size() > 0) {
				AccessPropertiesType accessPropertiesType = videoLecture.getAccess().getProperties().get(0);
				resourceV4.setResourceAccess(this.getMultimediaProperties(accessPropertiesType, videoLecture.getIdentifier())); //2nd is for folder
			}
			else {
				resourceV4.setResourceAccess(null);
			}
			//*presentation

			if (videoLecture.getRelation().getCourse() != null) {
				resourceV4.setType("COURSE");
				//Course
				String courseIdentity = videoLecture.getRelation().getCourse().getIdentity();
				Course course = courseService.findByIdentity(courseIdentity);
				if (course == null) {
					logger.fine(String.format("LECTURES> Warning. (d) Course (%s) of Lecture (%s) not found. Skipping import!", courseIdentity, videoLecture.getIdentifier()));
					return -1;
				}
				else {
					resourceV4.setCourse(course);
					if (videoLecture.getRights().getSecurity().equalsIgnoreCase("public")) {
						course.setResourcePublicCounter(course.getResourcePublicCounter() + 1);
					}
					course.setResourceCounter(course.getResourceCounter() + 1);
					courseService.update(course);
				}
				//Department
				String departmentIdentity = videoLecture.getUnit().getIdentity();
				Department department = departmentService.findByIdentity(departmentIdentity);
				Unit department_as_unit;
				if (department != null) {
					logger.fine("Create-Update Resource for Department:" + department.getTitle());
					department_as_unit = new Unit(StructureType.DEPARTMENT, department.getId(), department.getTitle());
					resourceV4.setDepartment(department_as_unit);
					resourceV4.setSchool(department.getSchoolId());
				}
				else {
					logger.fine("No Department found with identity:" + departmentIdentity);
					return -1;
				}
				resourceV4.setSchool(department.getSchoolId());
				//*Supervisor of Lecture is 'CREATOR'
				String creatorIdentity = videoLecture.getRights().getCreator().getIdentity();
				OpUser creator = opUserService.findByIdentity(creatorIdentity);
				if (creator == null) {
					logger.fine(String.format("LECTURES> Warning. (d) Creator (%s) of Lecture (%s) not found. Skipping import!", creatorIdentity, videoLecture.getIdentifier()));
					return -1;
				} else {
					resourceV4.setSupervisor(new Person(creator.getId(), creator.getName(), creator.getAffiliation(), creator.getDepartment()));
					if (videoLecture.getRights().getSecurity().equalsIgnoreCase("public")) {
						creator.setResourcePublicCounter(creator.getResourcePublicCounter() + 1);
					}
					creator.setResourceCounter(editor.getResourceCounter() + 1);
					opUserService.update(creator);
				}
				//Period and Academic Year
				CustomPeriod department_periods = departmentService.getDepartmentCalendar(department.getId(),institution_id,Integer.toString(academicYear));
				Instant resource_date = resourceV4.getDate();
				if (resourceV4.getIdentity().startsWith("213af88f")) {
					logger.warning("period error");
				}
				Instant period_startDate = null;
				Instant period_endDate = null;
				logger.fine("year:" + academicYear);
				logger.fine("resource:" + videoLecture.getIdentifier());
				logger.fine("department:" + department.getId());
				for (org.opendelos.model.calendar.Period period: department_periods.getPeriods().getPeriod()) {
					//default, ISO_LOCAL_DATE
					try {
						period_startDate = this.setDate(period.getStartDate(), "00:00:00", f, z);
						period_endDate = this.setDate(period.getEndDate(), "23:59:59", f, z);
						//logger.info(" ps:" + period_startDate + " pe:" + period_endDate);
						if ((resource_date.isAfter(period_startDate) || resource_date.equals(period_startDate)) &&
								(resource_date.isBefore(period_endDate) || resource_date.equals(period_endDate))) {
							resourceV4.setPeriod(period.getName());
							break;
						}
					}
					catch (Exception ex) {
						logger.warning("Error getting resource period:" + ex.getMessage() + " resource_date=" +  resource_date);
					}
				}
				if (resourceV4.getPeriod() == null) {
					logger.warning("Could not get Period of resource:" + videoLecture.getIdentifier() + " date of res:" + resource_date + " ps:" + period_startDate + " pe:" + period_endDate);
				}
			} // IF LECTURE
			else if (videoLecture.getRelation().getEvent() != null) {

				resourceV4.setType("EVENT");
				//EventID
				String eventIdentity = videoLecture.getRelation().getEvent().getIdentity();
				ScheduledEvent scheduledEvent = scheduledEventService.findByIdentity(eventIdentity);
				if (scheduledEvent == null) {
					logger.fine(String.format("LECTURES> Warning. (d) EVENT (%s) of Lecture  not found. Skipping import!", eventIdentity));
					return -1;
				}
				else {
					resourceV4.setEvent(scheduledEvent);
				}
				//*Supervisor :: responsible person if exists
				if (scheduledEvent.getResponsiblePerson() != null) {
					String rPersonIdentity = scheduledEvent.getIdentity();
					OpUser supervisor = opUserService.findByIdentity(rPersonIdentity);
					if (supervisor != null) {
						resourceV4.setSupervisor(new Person(supervisor.getId(), supervisor.getName(), supervisor.getAffiliation(), supervisor.getDepartment()));
					}
				}
				else {
					// Supervisor is creator
					String creatorIdentity = videoLecture.getRights().getCreator().getIdentity();
					OpUser supervisor = opUserService.findByIdentity(creatorIdentity);
					if (supervisor == null) {
						logger.fine(String.format("LECTURES> Warning. (d) Creator (%s) of Lecture (%s) not found. Skipping import!", creatorIdentity, videoLecture.getIdentifier()));
						return -1;
					} else {
						resourceV4.setSupervisor(new Person(supervisor.getId(), supervisor.getName(), supervisor.getAffiliation(), supervisor.getDepartment()));
					}
				}
			} //IF EVENT
		}
		catch (Exception e) {
			logger.severe("Error Importing Document:" + Arrays.toString(e.getStackTrace()));
			return -1;
		}
		// ------------  SAVE (NEW or UPDATE) ---------------- //
		if (create_new) {
			resourceService.create(resourceV4);
			logger.fine(" NEW RESOURCE WITH ID:" + resourceV4.getId() + " CREATED ");
		}
		else {
			resourceService.update(resourceV4);
			logger.fine(" UPDATED RESOURCE WITH ID:" + resourceV4.getId());
		}
		return 1;
	}

	public void UpdateCreateScheduledEvent(String triggerCollection,String triggerDocument, String triggerEvent,org.opendelos.legacydomain.institution.Institution legacyInstitution, String institution_id ) {

		Collection col = elegacyRepository.getDatabaseCollection(import_url, "guest", "guest", triggerCollection);
		Event oevent;
		if (legacyInstitution != null) {
			try {
				oevent = (Event) elegacyRepository.GetDataBaseObject(col, triggerDocument, Event.class);
				if (oevent != null) {
					this.createUpdateScheduledEvent(oevent, triggerEvent, legacyInstitution, institution_id);
				}
			}
			catch (Exception e) {
				logger.info("Failed to update:" + triggerDocument + " deleted?");
			}
		}
	}

	public void CreateUpdateUser(String triggerCollection, String triggerDocument, String triggerEvent, org.opendelos.legacydomain.institution.Institution legacyInstitution, Institution institution) throws Exception {

		Collection col = elegacyRepository.getDatabaseCollection(import_url, "guest", "guest", triggerCollection);
		DlmUser dlmUser = (DlmUser) elegacyRepository.GetDataBaseObject(col, triggerDocument, DlmUser.class);

		if (legacyInstitution != null) {
			this.createUpdateUser(dlmUser, triggerEvent, institution);
		}
	}

	public void CreateUpdateResource(String triggerCollection, String triggerDocument, String triggerEvent, String institution_id)  throws Exception {

		Collection col = elegacyRepository.getDatabaseCollection(import_url, "guest", "guest", triggerCollection);
		VideoLecture videoLecture;
		try {
			videoLecture = (VideoLecture) elegacyRepository.GetDataBaseObject(col, triggerDocument, VideoLecture.class);
			if (videoLecture != null) {
				this.createUpdateDocument(videoLecture, triggerEvent, institution_id);
			}
		}
		catch (Exception e) {
			throw new Exception("Failed to update:" + triggerDocument + " deleted?");
		}
	}

	public void DeleteResource(String triggerDocument) {

		String documentIdentity = triggerDocument.substring(0,triggerDocument.lastIndexOf("."));
		Resource toDeleteResourse = resourceService.findByIdentity(documentIdentity);
		if (toDeleteResourse != null) {
			resourceService.delete(toDeleteResourse.getId());
		}
	}

	public void CreateUpdatePresentation(String triggerCollection, String triggerDocument) {

		Collection col = elegacyRepository.getDatabaseCollection(import_url, "guest", "guest", triggerCollection);
		Presentation opresentation;
		try {
			String resourceIdentity = triggerDocument.substring(2,triggerDocument.lastIndexOf("."));
			Resource resource = resourceService.findByIdentity(resourceIdentity);
			opresentation = (Presentation) elegacyRepository.GetDataBaseObject(col, triggerDocument, Presentation.class);
			if (opresentation != null && (opresentation.getCuts() != null || opresentation.getSlides() != null )) {
				org.opendelos.model.resources.Presentation presentation = fillPresentationFromLegacyPresentation(opresentation);
				presentation.setFolder(resourceIdentity);
				logger.info("update presentation resource with identity:" +resourceIdentity);
				if (opresentation.getSlides() !=null && !opresentation.getSlides().getSlide().isEmpty()) {
					resource.getStatus().setInclPresentation(1);
				}
				else {
					resource.getStatus().setInclPresentation(0);
				}
				resource.setPresentation(presentation);
				resourceService.update(resource);
			}
			else {
				resource.setPresentation(new org.opendelos.model.resources.Presentation());
				resourceService.update(resource);
			}
		}
		catch (Exception e) {
			logger.info("Failed to update:" + triggerDocument + " deleted?");
		}
	}

	public void DeleteScheduledEvent(String scheduledEventIdentity) throws Exception {

		ScheduledEvent scheduledEvent = scheduledEventService.findByIdentity(scheduledEventIdentity);
		scheduledEventService.delete(scheduledEvent.getId());
	}

	public OpUser createManager(DlmUser dlmUser, Department department) {

		OpUser opUser = new OpUser();
		 try {
			 List<String> identities = new ArrayList<>();
			 identities.add(dlmUser.getSid());
			 opUser.setIdentity(identities);
			 opUser.setDepartment(new Unit(StructureType.DEPARTMENT, department.getId(), department.getTitle()));
			 opUser.setUid(dlmUser.getEmail().substring(0, dlmUser.getEmail().lastIndexOf("@")));
			 opUser.setName(dlmUser.getName());
			 opUser.setAltName("");
			 opUser.setEmail(dlmUser.getEmail());
			 opUser.setAffiliation(dlmUser.getAffiliation());
			 if (dlmUser.getPassword() != null) {
				 opUser.setPassword(dlmUser.getPassword());
			 }
			 List<String> personAffiliations = new ArrayList<>();
			 personAffiliations.add("employee");
			 opUser.setEduPersonAffiliation(personAffiliations);
			 opUser.setEduPersonPrimaryAffiliation("staff");
			 List<UserAccess.UserAuthority> userAuthorities = new ArrayList<>();
			 if (dlmUser.getUserRights() != null && dlmUser.getUserRights().get(0) != null &&
					 dlmUser.getUserRights().get(0).getRole() != null && dlmUser.getUserRights().get(0).getRole()
					 .equals(RoleType.SP)) {
				 userAuthorities.add(UserAccess.UserAuthority.SUPPORT);
			 }
			 else {
				 userAuthorities.add(UserAccess.UserAuthority.MANAGER);
			 }
			 opUser.setAuthorities(userAuthorities);
			 opUser.setActive(dlmUser.getStatus().equals("ACTIVE"));
			 return opUser;
		 }
		 catch (Exception e) {
		 	logger.severe(e.getStackTrace().toString());
		 }
		 return null;
	}

	public void updateManager(OpUser opUser, DlmUser dlmUser, Department department) {

		List<String> identities = new ArrayList<>();
		identities.add(dlmUser.getSid());
		opUser.setIdentity(identities);
		opUser.setDepartment(new Unit(StructureType.DEPARTMENT, department.getId(), department.getTitle()));
		opUser.setUid(dlmUser.getEmail().substring(0, dlmUser.getEmail().lastIndexOf("@")));
		opUser.setName(dlmUser.getName());
		opUser.setAltName("");
		opUser.setEmail(dlmUser.getEmail());
		opUser.setAffiliation(dlmUser.getAffiliation());
		if (dlmUser.getPassword() != null) {
			opUser.setPassword(dlmUser.getPassword());
		}
		List<String> personAffiliations = new ArrayList<>();
		personAffiliations.add("employee");
		opUser.setEduPersonAffiliation(personAffiliations);
		opUser.setEduPersonPrimaryAffiliation("staff");
		List<UserAccess.UserAuthority> userAuthorities = new ArrayList<>();
		if (dlmUser.getUserRights().get(0).getRole().equals(RoleType.SP)) {
			opUser.getAuthorities().add(UserAccess.UserAuthority.SUPPORT);
		}
		else {
			opUser.getAuthorities().add(UserAccess.UserAuthority.MANAGER);
		}
		opUser.setAuthorities(userAuthorities);
		opUser.setActive(dlmUser.getStatus().equals("ACTIVE"));

	}


	public boolean Create_NONSTAFFMEMBER(DlmUser dlmUser, Institution institution, String triggerEvent) throws ParseException {

		String dlmUserDepartmentIdentity = dlmUser.getDepartment().getIdentity();
		Department department = departmentService.findByIdentity(dlmUserDepartmentIdentity);

		OpUser opUser = opUserService.findByIdentity(dlmUser.getSid());
		if (opUser == null) {
			//Duplicates
			String user_uid = dlmUser.getEmail().substring(0, dlmUser.getEmail().lastIndexOf("@"));
			opUser = opUserService.findByUid(user_uid);
			if (opUser == null) {
				opUser = this.createManager(dlmUser, department);
				if (opUser == null) {
					return false;
				}
			}
			else {
				if (!opUser.getAuthorities().contains(UserAccess.UserAuthority.STAFFMEMBER)) {
					logger.warning("User with uid:" + user_uid + " & identity:" + dlmUser.getSid() + " not imported for duplicate UID");
					return false;
				}
				else {
					logger.info("User with uid:" + user_uid + " found as staffMember. Add manager rights");
					if (dlmUser.getUserRights().get(0).getRole().equals(RoleType.SP)) {
						opUser.getAuthorities().add(UserAccess.UserAuthority.SUPPORT);
					}
					else {
						opUser.getAuthorities().add(UserAccess.UserAuthority.MANAGER);
					}
					opUser.getIdentity().add(dlmUser.getSid());
				}
			}
		}
		else {
			this.updateManager(opUser, dlmUser, department);
		}
		//Set User Rights according to Role (SA, IM, SM, DM,SP)
		UserAccess.UserRights userRights = opUser.getRights();
		if (userRights == null) {
			userRights = new UserAccess.UserRights();
		}
		//Set User Role in legacy Db
		String dlmUserRole = dlmUser.getUserRights().get(0).getRole().toString();
		List<UserAccess.UserRights.UnitPermission> unitPermissions = new ArrayList<>();
		List<UserAccess.UserRights.CoursePermission> coursePermissions = new ArrayList<>();

		switch (dlmUserRole) {

		case "SA":
			userRights.setIsSa(true);
			break;
		case "IM":
		case "SM":
		case "DM":
			userRights.setIsSa(false);
			for (int ur = 0; ur < dlmUser.getUserRights()
					.size(); ur++) {            //read user rights on department units
				UserAccess.UserRights.UnitPermission unitPermission = this.setManagerUnitPermissions(dlmUser, ur, dlmUserRole, institution);
				unitPermissions.add(unitPermission);
			}
			break;
		case "SP":
			userRights.setIsSa(false);
			//List<String> course_identities = new ArrayList<>();
			for (int sr = 0; sr < dlmUser.getUserRights().size(); sr++) {            //read user rights on staffmembers + courses

				String staffMemberIdentity = dlmUser.getUserRights().get(sr).getScope().getIdentity();
				String course_identity = dlmUser.getUserRights().get(sr).getCscope().getIdentity();
				UserAccess.UserRights.CoursePermission coursePermission = new UserAccess.UserRights.CoursePermission();
				OpUser staffMember = opUserService.findByIdentity(staffMemberIdentity);
				if (staffMember != null) {
					String staffMemberId = staffMember.getId();
					Course course = courseService.findByIdentity(course_identity);
					if (course != null) {
						coursePermission.setStaffMemberId(staffMemberId);
						coursePermission.setCourseId(course.getId());
						coursePermissions.add(coursePermission);
						coursePermission.setContentManager(dlmUser.getUserRights().get(sr).isContentManager());
						coursePermission.setScheduleManager(dlmUser.getUserRights().get(sr).isScheduler());
					}
				}

			}

			break;
		}
		userRights.setUnitPermissions(unitPermissions);
		userRights.setCoursePermissions(coursePermissions);
		opUser.setRights(userRights);
		if (opUser.getUid().equals("sa")) {
			opUser.setActive(true);
			opUser.setPassword("$2a$10$u0XbBWfoy8O90OX20Aauj.UnHsIYy2PSLJ1d8gtFnuwNsIqqaiHm2");
		}
		if (dlmUser.getLastVisit() != null && !dlmUser.getLastVisit().equals("-1")) {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date formDate = format.parse(dlmUser.getLastVisit());
			Instant last_visit = formDate.toInstant();
			opUser.setLastLogin(last_visit);
		}
		if (triggerEvent.equals("after-create-document")) {
			opUserService.save(opUser);
		}
		else if (triggerEvent.equals("after-update-document")) {
			opUserService.save(opUser);
			opUserService.findAndUpdate(opUser);
		}
		return true;
	}


	public void CreateLiveEntry(String triggerCollection, String triggerDocument, org.opendelos.legacydomain.institution.Institution legacyInstitution, Institution institution) {

		Collection col = elegacyRepository.getDatabaseCollection(import_url, "guest", "guest", "/db/apps/delos-uoa/Scheduler/Calendar/");
		DsmCalendarXml oLiveEntry = null;
		try {
			XMLResource xmlResource= elegacyRepository.GetDatabaseResourceById(col, triggerDocument);
			JAXBContext jc = JAXBContext.newInstance(DsmCalendarXml.class);
			Unmarshaller um = jc.createUnmarshaller();
			StringBuffer xmlStr = new StringBuffer(xmlResource.getContent().toString());
			oLiveEntry = (DsmCalendarXml) um.unmarshal(
					new StreamSource(new StringReader(xmlStr.toString())));
			if (oLiveEntry == null) {
				logger.severe("Failed Live file");
			}
		}
		catch (Exception e) {
			logger.severe("Failed to update:" + triggerDocument + " deleted?");
		}

		if (oLiveEntry != null) {
			Resource liveEntry = new Resource();
			//STREAMID
			String recId = this.getRecId(oLiveEntry);
			logger.info("RecId of Live:" + recId);
			String streamId = this.getEncryptedStreamName(recId);
			if (streamId != null) {
				logger.info("StreamId of Live:" + streamId);
				liveEntry.setStreamId(streamId);
			}
			else {
				logger.severe("Could not generate streamId for scheduled item:" + oLiveEntry.getId());
			}
			liveEntry.setId(null);
			liveEntry.setIdentity(triggerDocument.substring(0,triggerDocument.lastIndexOf(".")));
			liveEntry.setBroadcastToChannel(false); //!Important : do not broadcast to channel by default
			//liveEntry.setStreamName(scheduleDTO.getClassroom().getStreamName());
			//liveEntry.setScheduleId(scheduleDTO.getId());
			if (oLiveEntry.getTypeId().equals("1") || oLiveEntry.getTypeId().equals("2")) {
				Course course = courseService.findByIdentity(oLiveEntry.getCourseId());
				liveEntry.setTitle(course.getTitle());
				if (oLiveEntry.getTypeId().equals("1")) {
					liveEntry.setDescription("Προγραμματισμένη Μετάδοση Μαθήματος");
				}
				else if (oLiveEntry.getTypeId().equals("2")) {
					liveEntry.setDescription("Έκτακτη Μετάδοση Μαθήματος");
				}
				Department department = departmentService.findByIdentity(oLiveEntry.getUnitId());
				liveEntry.setInstitution(institution.getId());
				liveEntry.setSchool(department.getSchoolId());
				liveEntry.setDepartment(new Unit(StructureType.DEPARTMENT, department.getId(), department.getTitle()));
				String creatorIdentity = oLiveEntry.getTeacherId();
				OpUser creator = opUserService.findByIdentity(creatorIdentity);
				liveEntry.setSupervisor(new Person(creator.getId(), creator.getName(), creator.getAffiliation(), creator.getDepartment()));
				liveEntry.setCourse(course);
				liveEntry.setType("COURSE");
			}
			else if (oLiveEntry.getTypeId().equals("4")) {
				ScheduledEvent scheduledEvent = scheduledEventService.findByIdentity(oLiveEntry.getCourseId());
				liveEntry.setTitle(scheduledEvent.getTitle());
				liveEntry.setDescription("Προγραμματισμένη Μετάδοση Εκδήλωσης");
				liveEntry.setSchool(null);
				liveEntry.setDepartment(null);
				if (scheduledEvent.getResponsiblePerson() != null) {
					liveEntry.setSupervisor(scheduledEvent.getResponsiblePerson());
				}
				liveEntry.setEvent(scheduledEvent);
				liveEntry.setType("EVENT");
			}
			//COMMON PROPERTIES
			liveEntry.setAcademicYear("2021");
			liveEntry.setPartNumber(0);
/*			OpUser editor = opUserService.findByIdentity(oLiveEntry.getRights().getEditor().getIdentity());
			liveEntry.setEditor(new Person(editor.getId(), editor.getName(), editor.getAffiliation(), editor.getDepartment()));
			liveEntry.setSpeakers("");
			liveEntry.setExt_speakers("");
			liveEntry.setLanguage("el");*/
			//>Date & Time
			DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			int broadcast_hour = Integer.parseInt(oLiveEntry.getHourId());
			int broadcast_min = Integer.parseInt(oLiveEntry.getMinutes());
			LocalDateTime broadcast_datetime = LocalDate.now().atTime(broadcast_hour, broadcast_min);
			//LocalDateTime broadcast_datetime = scheduleDTO.getDate().atTime(broadcast_hour,broadcast_min);
			Instant instant = broadcast_datetime.atZone(ZoneId.of(app_zone)).toInstant();
			liveEntry.setDate(instant);
			liveEntry.setDateModified(Instant.now());
			//others
			liveEntry.setTopics(null);
			String[] set_atleast_one = new String[1];
			set_atleast_one[0] = "othersubsubj";
			liveEntry.setCategories(set_atleast_one);
			liveEntry.setAccessPolicy(oLiveEntry.getActionAccessId());
			if (oLiveEntry.getActionAccessId().equals("password") && oLiveEntry.getPassword() !=null ) {
					liveEntry.setBroadcastCode(oLiveEntry.getPassword());
			}
			liveEntry.setAccess(oLiveEntry.getActionAccessId());
			liveEntry.setBroadcast(oLiveEntry.getBroadcast().equals("broadcasting"));
			liveEntry.setRecording(oLiveEntry.getRecord().contains("recording"));
			liveEntry.setPublication(oLiveEntry.getPublish());



			liveEntry.setLicense(institution.getOrganizationLicense());
			liveEntry.setStatistics(0);
			liveEntry.setStatus(new ResourceStatus(-1, -1, "SCHEDULER"));
			liveEntry.setPlayerOptions(new PlayerOptions(true, false));
			//Real Duration
			int dHours = Integer.parseInt(oLiveEntry.getDurationHours());
			int dMinutes = Integer.parseInt(oLiveEntry.getDurationMinutes());
			DecimalFormat df = new DecimalFormat("00");
			String h = df.format(dHours);
			String m = df.format(dMinutes);
			liveEntry.setRealDuration(h + ":" +  m); //should re-set at recording end
			liveEntry.setResourceAccess(null);
			liveEntry.setPresentation(null);
			String classroomIdentity = oLiveEntry.getRoomId();
			Classroom classroom = classroomService.findByIdentity(classroomIdentity);
			liveEntry.setClassroom(classroom.getId());
			//LIVE ENTRIES
 			for (org.opendelos.legacydomain.institution.Classroom classroom1 : legacyInstitution.getClassrooms().getClassroom()) {
				if (classroom1.getId().equals(classroomIdentity)) {
					String serverIdentity = classroom1.getDevices().getDevice().get(0).getMediaServer();
					StreamingServer streamingServer = streamingServerService.findByIdentity(serverIdentity);
					liveEntry.setStreamingServerId(streamingServer.getId());
					break;
				}
			}
			resourceRepository.saveToCollection(liveEntry, "Scheduler.Live");
		}
	}

	public void DeleteLiveEntry(String triggerDocument) {

		String documentIdentity = triggerDocument.substring(0,triggerDocument.lastIndexOf("."));
		Resource resourse = resourceRepository.findByIdentityInCollection(documentIdentity,"Scheduler.Live");

		if (resourse != null) {
			resourceRepository.deleteFromCollection(resourse,"Scheduler.Live");
		}
	}

	public org.opendelos.legacydomain.institution.Institution getLegacyInstitution() {

		org.opendelos.legacydomain.institution.Institution legacyInstitution = null;
		try {
			legacyInstitution = elegacyRepository.getLegacyInstitution(import_url, "guest", "guest", "/db/apps/delos-uoa/institutions", "uoa");
		}
		catch (Exception e) {
			logger.severe("FAILED: legacy institution");
		}
		return legacyInstitution;
	}


	private Instant setDate(String legacy_date, String legacy_time, DateTimeFormatter f, ZoneId z) throws DateTimeParseException {

		String oTime = legacy_time;
		if (oTime.length() > 8) {
			oTime = oTime.substring(0, 8);
		}
		if (oTime.split(":").length == 2) {
			oTime = oTime + ":00";
		}
		String input = legacy_date + " " + oTime;
		LocalDateTime ldt;

		ldt = LocalDateTime.parse(input, f);

		ZonedDateTime zdt = ldt.atZone(z);

		return zdt.toInstant().plusSeconds(zdt.getOffset().getTotalSeconds());
	}


	private ResourceStatus getResourceStatus(VideoLecture videoLecture) {

		ResourceStatus resourceStatus = null;
		if (videoLecture.getStatus() != null && videoLecture.getStatus().getStatusProperty() != null && videoLecture.getStatus().getStatusProperty().size() > 0) {
			resourceStatus = new ResourceStatus();
			resourceStatus.setInclMultimedia(-1);
			resourceStatus.setInclPresentation(-1);
			resourceStatus.setVideoSource("");
			for (int s = 0; s < videoLecture.getStatus().getStatusProperty().size(); s++) {
				StatusType.StatusProperty statusProperty = videoLecture.getStatus().getStatusProperty().get(s);
				switch (statusProperty.getName()) {
				case "Video":
					resourceStatus.setInclMultimedia(Integer.parseInt(statusProperty.getValue()));
					break;
				case "Presentation":
					resourceStatus.setInclPresentation(Integer.parseInt(statusProperty.getValue()));
					break;
				case "VideoSource":
					resourceStatus.setVideoSource(statusProperty.getValue());
					break;
				}
			}
		}
		return resourceStatus;
	}

	private ResourceTags getResourceTags(VideoLecture videoLecture) {

		ResourceTags resourceTags = null;

		if (videoLecture.getTags() != null) {

			resourceTags = new ResourceTags("undefined");
			if (videoLecture.getTags().getMetEdt() != null && !videoLecture.getTags().getMetEdt().equals("")) {
				resourceTags.setMetEdt(videoLecture.getTags().getMetEdt());
			}
			if (videoLecture.getTags().getMultEdt() != null && !videoLecture.getTags().getMultEdt().equals("")) {
				resourceTags.setMultEdt(videoLecture.getTags().getMultEdt());
			}
			if (videoLecture.getTags().getMultRed() != null && !videoLecture.getTags().getMultRed().equals("")) {
				resourceTags.setMultRed(videoLecture.getTags().getMultRed());
			}
			if (videoLecture.getTags().getPreSyn() != null && !videoLecture.getTags().getPreSyn().equals("")) {
				resourceTags.setPreSyn(videoLecture.getTags().getPreSyn());
			}
			if (videoLecture.getTags().getPreUp() != null && !videoLecture.getTags().getPreUp().equals("")) {
				resourceTags.setPreUp(videoLecture.getTags().getPreUp());
			}
			if (videoLecture.getTags().getResApp() != null && !videoLecture.getTags().getResApp().equals("")) {
				resourceTags.setResApp(videoLecture.getTags().getResApp());
			}
			if (videoLecture.getTags().getResFin() != null && !videoLecture.getTags().getResFin().equals("")) {
				resourceTags.setResFin(videoLecture.getTags().getResFin());
			}
			if (videoLecture.getTags().getResPub() != null && !videoLecture.getTags().getResPub().equals("")) {
				resourceTags.setResPub(videoLecture.getTags().getResPub());
			}
			if (videoLecture.getTags().getSub() != null && !videoLecture.getTags().getSub().equals("")) {
				resourceTags.setSub(videoLecture.getTags().getSub());
			}
		}
		return resourceTags;
	}
	private ResourceAccess getMultimediaProperties(AccessPropertiesType accessPropertiesType, String identifier) {

		ResourceAccess resourceAccess = new ResourceAccess();
		resourceAccess.setAspectRatio(accessPropertiesType.getAspectRatio());
		resourceAccess.setDevice(accessPropertiesType.getDevice());
		resourceAccess.setDuration(accessPropertiesType.getDuration());
		resourceAccess.setFileName(accessPropertiesType.getFilename());
		resourceAccess.setFolder(identifier);
		resourceAccess.setFormat(accessPropertiesType.getFormat());
		resourceAccess.setQuality(accessPropertiesType.getQuality());
		resourceAccess.setResolution(accessPropertiesType.getResolution());
		resourceAccess.setSourceName(accessPropertiesType.getSourceName());
		resourceAccess.setType(accessPropertiesType.getType());

		return resourceAccess;
	}


	private org.opendelos.model.resources.Presentation fillPresentationFromLegacyPresentation(Presentation opr) {

		org.opendelos.model.resources.Presentation presentation = new org.opendelos.model.resources.Presentation();
		if (opr.getSlides()  != null && opr.getSlides().getSlide() != null && opr.getSlides().getSlide().size() > 0) {
			List<Slide> slides = new ArrayList<>();
			for (int s = 0; s < opr.getSlides().getSlide().size(); s++) {
				Slides.Slide oSlide = opr.getSlides().getSlide().get(s);
				Slide nSlide = new Slide();
				nSlide.setTitle(oSlide.getTitle());
				nSlide.setUrl(oSlide.getUrl().substring(oSlide.getUrl().lastIndexOf("/") + 1));
				nSlide.setTime(oSlide.getTime());
				slides.add(nSlide);
			}
			presentation.setSlides(slides);
		}
		if (opr.getCuts()  != null && opr.getCuts() != null) {

			boolean existCuts = false;
			boolean existTrims = false;

			Cuts cuts = new Cuts();
			Cuts.Clips clips = new Cuts.Clips();
			if (opr.getCuts().getClips() != null && opr.getCuts().getClips().getCut() != null && opr.getCuts().getClips().getCut().size() > 0) {
				existCuts = true;
				List<Cuts.Clips.Cut> pcuts = new ArrayList<>();
				for (int c = 0; c < opr.getCuts().getClips().getCut().size(); c++) {
					org.opendelos.legacydomain.cuts.Cuts.Clips.Cut oCut = opr.getCuts().getClips().getCut().get(c);
					Cuts.Clips.Cut nCut = new Cuts.Clips.Cut();
					nCut.setBegin(oCut.getBegin());
					nCut.setEnd(oCut.getEnd());
					pcuts.add(nCut);
				}
				clips.setCuts(pcuts);
				cuts.setClips(clips);
			}
			if (opr.getCuts().getTrims() != null) {
				Cuts.Trims trims = new Cuts.Trims();
				if (opr.getCuts().getTrims().getStart() != null) {
					existTrims = true;
					Cuts.Trims.Start start = new Cuts.Trims.Start();
					start.setBegin(opr.getCuts().getTrims().getStart().getBegin());
					start.setEnd(opr.getCuts().getTrims().getStart().getEnd());
					trims.setStart(start);
				}
				if (opr.getCuts().getTrims().getFinish() != null) {
					existTrims = true;
					Cuts.Trims.Finish finish = new Cuts.Trims.Finish();
					finish.setBegin(opr.getCuts().getTrims().getFinish().getBegin());
					finish.setEnd(opr.getCuts().getTrims().getFinish().getEnd());
					trims.setFinish(finish);
				}
				if (existTrims)
					cuts.setTrims(trims);
			}
			if (existCuts || existTrims) {              // Set Only if Cuts or Trims != null
				presentation.setCuts(cuts);
				presentation.setRealDuration(opr.getRealDuration());
			}
		}
		if (opr.getSubs() != null) {
			Subtitles subtitles = new Subtitles();
			List<Subtitles.Sub> subs = new ArrayList<>();
			for (int t=0; t < opr.getSubs().getSub().size(); t++) {
				Subtitles.Sub sub = new Subtitles.Sub();
				sub.setDescription(opr.getSubs().getSub().get(t).getDescription());
				sub.setUrl(opr.getSubs().getSub().get(t).getUrl());
				subs.add(sub);
			}
			subtitles.setSubs(subs);
		}

		return presentation;
	}

	private UserAccess.UserRights.UnitPermission setManagerUnitPermissions(DlmUser dlmUser, int userIdx, String userType, Institution institution) {

		String unitIdentity = dlmUser.getUserRights().get(userIdx).getScope().getIdentity();

		UserAccess.UserRights.UnitPermission unitPermission = new UserAccess.UserRights.UnitPermission();

		switch (userType) {
		case "IM":
			unitPermission.setUnitId(institution.getId());
			unitPermission.setUnitType(UserAccess.UnitType.INSTITUTION);
			break;
		case "SM":
			School ur_school = schoolService.findByIdentity(unitIdentity);
			unitPermission.setUnitId(ur_school.getId());
			unitPermission.setUnitType(UserAccess.UnitType.SCHOOL);
			break;
		case "DM":
			Department ur_department = departmentService.findByIdentity(unitIdentity);
			unitPermission.setUnitId(ur_department.getId());
			unitPermission.setUnitType(UserAccess.UnitType.DEPARTMENT);
			break;
		}
		unitPermission.setContentManager(dlmUser.getUserRights().get(userIdx).isContentManager());
		unitPermission.setDataManager(dlmUser.getUserRights().get(userIdx).isDataManager());
		unitPermission.setScheduleManager(dlmUser.getUserRights().get(userIdx).isScheduler());

		return unitPermission;
	}

	private String getPeriodNameFromOldName(String oldName) {
		if (oldName.startsWith("Winter_")) {
			return "winter";
		}
		else if (oldName.startsWith("Spring_")) {
			return "spring";
		}
		else if (oldName.startsWith("Intermediate_")) {
			return "intervening";
		}
		else if (oldName.startsWith("Summer_")) {
			return "summer";
		}
		return "";
	}

	public void ReCreatedSAUser() {
		//RE-create sa
		OpUser sa = new OpUser();
		sa.setActive(true);
		sa.setEmail("sa@med.uoa.gr");
		sa.setUid("sa");
		sa.setName("Διαχειριστής Συστήματος");
		sa.setAltName("System Admin");
		sa.setAffiliation("ΕΔΙΠ");
		sa.setEduPersonPrimaryAffiliation("staff");
		List<String> eduPersonAffiliations = new ArrayList<>();
		eduPersonAffiliations.add("staff");
		sa.setEduPersonAffiliation(eduPersonAffiliations);
		List<UserAccess.UserAuthority> authorities = new ArrayList<>();
		authorities.add(UserAccess.UserAuthority.MANAGER);
		sa.setAuthorities(authorities);
		UserAccess.UserRights userRights = new UserAccess.UserRights();
		userRights.setIsSa(true);
		sa.setRights(userRights);
		sa.setPassword("$2a$10$u0XbBWfoy8O90OX20Aauj.UnHsIYy2PSLJ1d8gtFnuwNsIqqaiHm2");
		sa.setLastLogin(Instant.now());
		//Department department = departmentService.findByIdentity("med");
		//sa.setDepartment(new Unit(StructureType.DEPARTMENT, department.getId(),department.getTitle()));
		opUserService.create(sa);
	}

	public String getEncryptedStreamName(String str)   {

		StringBuilder sb = new StringBuilder();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(str.getBytes());

			byte[] byteData = md.digest();

			//convert the byte to hex format

			for (byte byteDatum : byteData) {
				sb.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
			}
		}
		catch (Exception ignored)
		{

		}

		return sb.toString();
	}

	public String getRecId(DsmCalendarXml dsmCalendarDto){
		String recId = null;
		try {
				String unitId = dsmCalendarDto.getUnitId();
				String personId = dsmCalendarDto.getTeacherId();
				if (dsmCalendarDto.getTypeId().equals("4")){
					unitId = "-1";
					personId = "-1";
				}
				java.util.Calendar start = java.util.Calendar.getInstance();
				start.setTime(new Date());
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
				start.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(dsmCalendarDto.getHourId()));
				start.set(java.util.Calendar.MINUTE, Integer.parseInt(dsmCalendarDto.getMinutes()));
				start.set(java.util.Calendar.SECOND, 0);
				recId = dsmCalendarDto.getOrganizationId()
						+ "." + unitId
						+ "." + dsmCalendarDto.getRoomId()
						+ "." + dsmCalendarDto.getCourseId()
						+ "." + personId
						+ "." + dateFormat.format(start.getTime());
		}	catch(Exception e){
			logger.severe("c49:" + e.getLocalizedMessage());
			return null;
		}
		return recId;
	}

}
