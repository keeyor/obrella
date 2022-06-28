/* 
     Author: Michael Gatzonis - 24/10/2021 
     obrella
*/
package org.opendelos.sync.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.opendelos.legacydomain.calendar.AcademicCalendarSe;
import org.opendelos.legacydomain.calendar.Argia;
import org.opendelos.legacydomain.calendar.Argies;
import org.opendelos.legacydomain.calendar.Period;
import org.opendelos.legacydomain.calendar.Periods;
import org.opendelos.legacydomain.dlmuser.DlmUser;
import org.opendelos.legacydomain.dlmusers.DlmUsers;
import org.opendelos.legacydomain.event.Event;
import org.opendelos.legacydomain.institution.Study;
import org.opendelos.legacydomain.queryresponse.QueryResponse;
import org.opendelos.legacydomain.queryresponse.ResourcesType;
import org.opendelos.legacydomain.scheduler.CalendarType;
import org.opendelos.legacydomain.scheduler.DsmCalendarXml;
import org.opendelos.legacydomain.scheduler.DsmCalendarXmls;
import org.opendelos.legacydomain.videolecture.VideoLecture;
import org.opendelos.legacydomain.xevents.XEvents;
import org.opendelos.model.dates.CustomPause;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.scheduler.Schedule;
import org.opendelos.model.structure.Classroom;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.Device;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.structure.LmsReference;
import org.opendelos.model.structure.School;
import org.opendelos.model.structure.StreamingServer;
import org.opendelos.model.structure.StudyProgram;
import org.opendelos.sync.legacyrepo.CalendarRepo.AcademicCalendarSeDAO;
import org.opendelos.sync.legacyrepo.ElegacyRepository;
import org.opendelos.sync.services.opUser.OpUserService;
import org.opendelos.sync.services.resource.ResourceService;
import org.opendelos.sync.services.scheduledEvent.ScheduledEventService;
import org.opendelos.sync.services.scheduler.ScheduleService;
import org.opendelos.sync.services.structure.ClassroomService;
import org.opendelos.sync.services.structure.CourseService;
import org.opendelos.sync.services.structure.DepartmentService;
import org.opendelos.sync.services.structure.InstitutionService;
import org.opendelos.sync.services.structure.SchoolService;
import org.opendelos.sync.services.structure.StreamingServerService;
import org.opendelos.sync.services.structure.StudyProgramService;
import org.xmldb.api.base.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

@Service("ImportLegacyService")
public class ImportLegacyService {

	@Value("${import_url}")
	String import_url;

	@Value("${public_only}")
	boolean public_only;

	@Value("${main_col}")
	String main_col;


	private final ElegacyRepository elegacyRepository;
	private final AcademicCalendarSeDAO academicCalendarSeDAO;

	private final InstitutionService institutionService;
	private final SchoolService schoolService;
	private final DepartmentService departmentService;
	private final StudyProgramService studyProgramService;
	private final ClassroomService classroomService;
	private final CourseService courseService;
	private final OpUserService opUserService;
	private final StreamingServerService streamingServerService;
	private final ScheduledEventService scheduledEventService;
	private final ResourceService resourceService;
	private final ScheduleService scheduleService;

	private final UpdateService updateService;

	private final Logger logger = Logger.getLogger(ImportLegacyService.class.getName());

	@Autowired
	public ImportLegacyService(ElegacyRepository elegacyRepository, AcademicCalendarSeDAO academicCalendarSeDAO, InstitutionService institutionService, SchoolService schoolService, DepartmentService departmentService, ClassroomService classroomService, CourseService courseService, OpUserService opUserService, StudyProgramService studyProgramService, StreamingServerService streamingServerService, ScheduledEventService scheduledEventService, ResourceService resourceService, ScheduleService scheduleService, UpdateService updateService) {
		this.elegacyRepository = elegacyRepository;
		this.academicCalendarSeDAO = academicCalendarSeDAO;
		this.institutionService = institutionService;
		this.schoolService = schoolService;
		this.departmentService = departmentService;
		this.classroomService = classroomService;
		this.courseService = courseService;
		this.opUserService = opUserService;
		this.studyProgramService = studyProgramService;
		this.streamingServerService = streamingServerService;
		this.scheduledEventService = scheduledEventService;
		this.resourceService = resourceService;
		this.scheduleService = scheduleService;
		this.updateService = updateService;
	}

 	public boolean ImportEverything(Institution institution, org.opendelos.legacydomain.institution.Institution legacyInstitution) throws Exception {

		String institution_id = institution.getId();

		boolean res = this.ImportInstitutionCalendar(institution_id, null);
		if (res) {
			logger.info("CALENDAR IMPORT FINISHED");
		}
		res = this.ImportClassroomsAndDevices(legacyInstitution);
		if (res) {
			logger.info("CLASSROOMS AND DEVICES FINISHED");
			res = this.ImportSchoolsAndDepartmentsAndCoursesAndStaffMembers(legacyInstitution, institution_id);
		}
		if (res) {
			logger.info("SCHOOLS, DEPARTMENTS, STUDYPROGRAMS, COURSES, STAFFMEMBERS FINISHED");

	 		res = this.ImportNonStaffMembers(institution);
		}
		if (res) {
			logger.info("MANAGERS FINISHED");
		 	res = this.ImportStreamingServers(legacyInstitution);
		}
		if (res) {
			logger.info("STREAMING SERVERS FINISHED");
			res = this.ImportEvents(legacyInstitution, institution_id);
		}
		if (res) {
			logger.info("SCHEDULED EVENTS FINISHED");
	 		this.ImportResources(institution_id);
		}
		logger.info("END IMPORT");

		return res;
	}

	public void ImportCalendar(String triggerCollection, String triggerDocument, Institution institution) {

		if (triggerDocument.contains("2021")) { 			//ignore other calendars for now!
			Collection col = elegacyRepository.getDatabaseCollection(import_url, "guest", "guest", triggerCollection);
			AcademicCalendarSe academicCalendarSe;
			try {
				academicCalendarSe = (AcademicCalendarSe) elegacyRepository.GetDataBaseObject(col, triggerDocument, AcademicCalendarSe.class);
				if (academicCalendarSe != null) {
					this.ImportInstitutionCalendar(institution.getId(),"2021");
					for (org.opendelos.legacydomain.calendar.Department oCalDepartment: academicCalendarSe.getInstitution().getDepartments().getDepartment()){
						this.importDepartmentCalendar(null,oCalDepartment.getPeriods().getRefId(),"2021");
/*						if (oCalDepartment.getStudies() != null && oCalDepartment.getStudies().getStudy() != null &&
								!oCalDepartment.getStudies().getStudy().isEmpty()) {
							for (org.opendelos.legacydomain.calendar.Study oCalStudy : oCalDepartment.getStudies()
									.getStudy()) {
								this.importStudyProgramCalendar(oCalDepartment.getPeriods()
										.getRefId(), oCalStudy.getPeriods().getRefId(), null, "2021");
							}
						}*/
					}
				}
			}
			catch (Exception e) {
				logger.info("Failed to update:" + triggerDocument + " deleted?");
			}
		}
	}


	public boolean ImportInstitutionCalendar(String institution_id, String year) throws Exception {

		List<String> conf_years = new ArrayList<>();
		if (year == null) {
			conf_years = academicCalendarSeDAO.getAvailableAcademicCalendarYears();
		}
		else {
			conf_years.add(year);
		}
		CustomPeriod customPeriod = null;
		CustomPause customPause = null;
		for (String academicYear: conf_years) {
			AcademicCalendarSe academicCalendarSe = academicCalendarSeDAO.getCalendarByYear(academicYear);
			customPeriod = new CustomPeriod();
			Periods default_periods = academicCalendarSeDAO.getDefaultCalendarPeriods(academicCalendarSe);
			customPeriod.setInherited(false);
			customPeriod.setYear(academicYear);
			org.opendelos.model.calendar.Periods nPeriods = new org.opendelos.model.calendar.Periods();
			for (Period period: default_periods.getPeriod()) {
				String _startdate = period.getStartDate();
				String _enddate = period.getEndDate();
				String _name = period.getName();
				//#new
				org.opendelos.model.calendar.Period nPeriod = new org.opendelos.model.calendar.Period();
				nPeriod.setStartDate(_startdate);
				nPeriod.setEndDate(_enddate);
				nPeriod.setName(getPeriodNameFromOldName(_name));
				nPeriods.getPeriod().add(nPeriod);
			}
			customPeriod.setPeriods(nPeriods);
			customPause = new CustomPause();
			Argies default_argies = academicCalendarSeDAO.getDefaultCalendarArgies(academicCalendarSe);
			customPause.setYear(academicYear);
			org.opendelos.model.calendar.Argies nArgies = new org.opendelos.model.calendar.Argies();
			for (Argia argia: default_argies.getArgia()) {
				String _startdate = argia.getStartDate();
				String _enddate = argia.getEndDate();
				String _name = argia.getName();
				//#new
				org.opendelos.model.calendar.Argia nArgia = new org.opendelos.model.calendar.Argia();
				nArgia.setStartDate(_startdate);
				nArgia.setEndDate(_enddate);
				nArgia.setName(_name);
				nArgies.getArgia().add(nArgia);
			}
			customPause.setArgies(nArgies);
		}
		institutionService.saveCustomPeriod(institution_id,customPeriod);
		institutionService.saveCustomPause(institution_id,customPause);

		return true;
	}

	public boolean importDepartmentCalendar(String department_id, String department_identity, String year) throws Exception {

		List<String> conf_years = new ArrayList<>();
		if (year == null) {
			conf_years = academicCalendarSeDAO.getAvailableAcademicCalendarYears();
		}
		else {
			conf_years.add(year);
		}
		if (department_id == null) {
			Department department = departmentService.findByIdentity(department_identity);
			department_id = department.getId();
		}

		CustomPeriod customPeriod;
		CustomPause customPause;
		for (String academicYear: conf_years) {
			AcademicCalendarSe academicCalendarSe = academicCalendarSeDAO.getCalendarByYear(academicYear);
			customPeriod = new CustomPeriod();
			Periods default_periods = academicCalendarSeDAO.getDepartmentPeriods(department_identity,academicCalendarSe);
			if (default_periods.getInherit().equals("0")) {
				customPeriod.setInherited(Boolean.getBoolean(default_periods.getInherit()));
				customPeriod.setYear(academicYear);
				org.opendelos.model.calendar.Periods nPeriods = new org.opendelos.model.calendar.Periods();
				nPeriods.setRefId(department_id);
				for (Period period : default_periods.getPeriod()) {
					String _startdate = period.getStartDate();
					String _enddate = period.getEndDate();
					String _name = period.getName();
					//#new
					org.opendelos.model.calendar.Period nPeriod = new org.opendelos.model.calendar.Period();
					nPeriod.setStartDate(_startdate);
					nPeriod.setEndDate(_enddate);
					nPeriod.setName(getPeriodNameFromOldName(_name));
					nPeriods.getPeriod().add(nPeriod);
				}
				customPause = new CustomPause();
				Argies default_argies = academicCalendarSeDAO.getDepartmentArgies(department_identity,academicCalendarSe);
				customPause.setYear(academicYear);
				org.opendelos.model.calendar.Argies nArgies = new org.opendelos.model.calendar.Argies();
				for (Argia argia: default_argies.getArgia()) {
					String _startdate = argia.getStartDate();
					String _enddate = argia.getEndDate();
					String _name = argia.getName();
					//#new
					org.opendelos.model.calendar.Argia nArgia = new org.opendelos.model.calendar.Argia();
					nArgia.setStartDate(_startdate);
					nArgia.setEndDate(_enddate);
					nArgia.setName(_name);
					nArgies.getArgia().add(nArgia);
				}
				customPause.setArgies(nArgies);
				customPeriod.setPeriods(nPeriods);
				departmentService.saveCustomPeriod(department_id, customPeriod);
				departmentService.saveCustomPause(department_id, customPause);
			}
		}

		return true;
	}

	public boolean importStudyProgramCalendar(String department_identity,String study_identity, String study_id, String year) throws Exception {

		List<String> conf_years = new ArrayList<>();
		if (year == null) {
			conf_years = academicCalendarSeDAO.getAvailableAcademicCalendarYears();
		}
		else {
			conf_years.add(year);
		}
		if (study_id == null) {
			StudyProgram studyProgram = studyProgramService.findByIdentity(study_identity);
			study_id = studyProgram.getId();
		}
		CustomPeriod customPeriod;
		for (String academicYear: conf_years) {
			AcademicCalendarSe academicCalendarSe = academicCalendarSeDAO.getCalendarByYear(academicYear);
			customPeriod = new CustomPeriod();
			Periods default_periods = academicCalendarSeDAO.getStudyPeriods(department_identity,study_identity,academicCalendarSe);
			if (default_periods.getInherit().equals("0")) {
				customPeriod.setInherited(Boolean.getBoolean(default_periods.getInherit()));
				customPeriod.setYear(academicYear);
				org.opendelos.model.calendar.Periods nPeriods = new org.opendelos.model.calendar.Periods();
				nPeriods.setRefId(study_id);
				for (Period period : default_periods.getPeriod()) {
					String _startdate = period.getStartDate();
					String _enddate = period.getEndDate();
					String _name = period.getName();
					//#new
					org.opendelos.model.calendar.Period nPeriod = new org.opendelos.model.calendar.Period();
					nPeriod.setStartDate(_startdate);
					nPeriod.setEndDate(_enddate);
					nPeriod.setName(getPeriodNameFromOldName(_name));
					nPeriods.getPeriod().add(nPeriod);
				}
				customPeriod.setPeriods(nPeriods);
				studyProgramService.saveCustomPeriod(study_id, customPeriod);
			}
		}

		return true;
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
	public boolean ImportClassroomsAndDevices(org.opendelos.legacydomain.institution.Institution legacyInstitution) {
		try {
			classroomService.deleteAll();
			if (legacyInstitution.getClassrooms() != null) {
				for (org.opendelos.legacydomain.institution.Classroom oClassroom : legacyInstitution.getClassrooms().getClassroom()) {

					String nclassroomIdentity = oClassroom.getId();
					Classroom classroom = classroomService.findByIdentity(nclassroomIdentity);

					if (classroom == null) {
						classroom = new Classroom();
						classroom.setName(oClassroom.getName());
						classroom.setCode(oClassroom.getCodeName());
						classroom.setIdentity(nclassroomIdentity);
						classroom.setDescription(oClassroom.getDescription());
						classroom.setLocation(oClassroom.getLocation());
						classroom.setMap(oClassroom.getMap());
						classroom.setCalendar(oClassroom.getCalendar());
						classroom.setUsage("both");
						String classroomId = classroomService.create(classroom);
						classroom.setId(classroomId);
					}
					List<Device> classroomDevices = new ArrayList<>(); //Wipe-out in order to get updates!
					if (oClassroom.getDevices() != null) {
						for (org.opendelos.legacydomain.institution.Device odevice : oClassroom.getDevices().getDevice()) {

							Device nDevice = new Device();
							nDevice.setType(odevice.getType());
							nDevice.setIdentity(odevice.getId());
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
				}
				return true;
			}
			else {
				logger.severe("NO classrooms found in eXist-db. Aborting");
				return false;
			}
		}
		catch (Exception e) {
			logger.severe("General Exception importing OLD DB Classrooms:" + e.getMessage());
			return false;
		}
	}

	public boolean ImportSchoolsAndDepartmentsAndCoursesAndStaffMembers(org.opendelos.legacydomain.institution.Institution legacyInstitution, String institution_id) {
		int ImportedUsers;
		int ImportedDuplcateUsers;
		try {
			schoolService.deleteAll();
			departmentService.deleteAll();
			studyProgramService.deleteAll();
			courseService.deleteAll();
			opUserService.deleteAll();
			if (legacyInstitution.getSchools() != null) {
				for (org.opendelos.legacydomain.institution.School oschool : legacyInstitution.getSchools().getSchool()) {
					logger.info("SCHOOL:" + oschool.getName());
					School school = new School();
					school.setTitle(oschool.getName().trim());
					school.setIdentity(oschool.getId());
					String schoolId = schoolService.create(school);

					if (oschool.getDepartments() != null) {
						for (org.opendelos.legacydomain.institution.Department odepartment : oschool.getDepartments().getDepartment()) {
							logger.info("DEPARTMENT:" + odepartment.getName());
							Department department = new Department();
							department.setIdentity(odepartment.getId());
							department.setTitle(odepartment.getName().trim());
							department.setLogoUrl(odepartment.getLogoUrl());
							department.setUrl(odepartment.getUrl());
							department.setPassword(odepartment.getPassword());
							department.setInstitutionId(institution_id);
							department.setSchoolId(schoolId);

							//* Classroom References for Department --> Requires that classrooms are already imported!
							if (odepartment.getClassroomId() != null) {
								List<String> nDepartmentClassrooms = new ArrayList<>();
								for (String oClassroomIdentity : odepartment.getClassroomId()) {
									Classroom classroom = classroomService.findByIdentity(oClassroomIdentity);
									if (classroom != null) {
										String classroomId = classroom.getId();
										if (!nDepartmentClassrooms.contains(classroomId)) {
											nDepartmentClassrooms.add(classroomId);
										}
									}
								}
								department.setClassrooms(nDepartmentClassrooms);
							}
							String departmentId;
							try {
								departmentId = departmentService.create(department);
							}
							catch (Exception e) {
								continue;
							}
							boolean calendar_import = this.importDepartmentCalendar(departmentId,odepartment.getId(),null);
							if (!calendar_import) {
								logger.warning("Error importing calendar for department:" + odepartment.getName());
							}

							boolean create_under_study = true;
							//* Studies for Department
							String under_graduate_study_id = null;
							if (odepartment.getStudies() != null && odepartment.getStudies().getStudy() != null) {
								for (Study oStudy : odepartment.getStudies().getStudy()) {
									StudyProgram studyProgram = new StudyProgram();
									studyProgram.setIdentity(oStudy.getStudyIdentity());
									studyProgram.setSchoolId(schoolId);
									studyProgram.setDepartmentId(departmentId);
									studyProgram.setTitle(oStudy.getTitle());
									if (oStudy.getTitle().contains("Προπτ")) {
											//# NOTE: DO NOT IMPORT...
											continue;
											/*studyProgram.setStudy("under");
											create_under_study = false;
											under_graduate_study_id = studyProgramService.create(studyProgram);
											calendar_import = this.importStudyProgramCalendar(odepartment.getId(),oStudy.getStudyIdentity(),under_graduate_study_id,null);*/
									}
									else {
											studyProgram.setStudy("post");
											String study_id = studyProgramService.create(studyProgram);
											calendar_import = this.importStudyProgramCalendar(odepartment.getId(),oStudy.getStudyIdentity(),study_id,null);
									}
									if (!calendar_import) {
										logger.warning("Error importing calendar for study:" + oStudy.getTitle());
									}
								}
							}
							//#Create under graduate study for department if not already created ( 29-03:: Everytime!!!)
							if (create_under_study) {
								StudyProgram studyProgram = new StudyProgram();
								studyProgram.setIdentity(null);
								studyProgram.setSchoolId(schoolId);
								studyProgram.setDepartmentId(departmentId);
								studyProgram.setTitle("Προπτυχιακό");
								studyProgram.setStudy("under");
								under_graduate_study_id = studyProgramService.create(studyProgram);
							}

							//* Course for Department
							if (odepartment.getCourses() != null && odepartment.getCourses().getCourse() != null) {
								for (org.opendelos.legacydomain.institution.Course ocourse : odepartment.getCourses().getCourse()) {
									Course course = new Course();
									course.setTitle(ocourse.getTitle().trim());
									course.setIdentity(ocourse.getCId());
									try {
										course.setScopeId(ocourse.getUId()); // may be null
									} catch (Exception npe) {
										course.setScopeId(ocourse.getCId());	//set default cid for scopeId
									}
									//set all imported courses to under_graduate_study
									course.setStudy("under");
									course.setStudyProgramId(under_graduate_study_id);
									course.setInstitutionId(institution_id);
									course.setSchoolId(schoolId);
									course.setDepartment(new Unit(StructureType.DEPARTMENT, departmentId, department.getTitle()));
									course.setSemester(ocourse.getSemester());
									course.setTeachingCounter(0);
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
									//Ignore StaffMembers courses References -> Pick them up from StaffMembers below!
									courseService.create(course);
								} // For Courses
							} // Courses != null
							//* StaffMembers for Department
							if (odepartment.getStaffMembers() != null && odepartment.getStaffMembers().getStaffMember() !=null) {
								ImportedUsers = 0;
								ImportedDuplcateUsers = 0;
								for (org.opendelos.legacydomain.institution.StaffMember oStaff : odepartment.getStaffMembers().getStaffMember()) {
									Unit unit = new Unit(StructureType.DEPARTMENT, departmentId, department.getTitle());
									int result = updateService.CreateStaffMember(oStaff,legacyInstitution,unit,null);
									if (result == 1) {
										ImportedUsers++;
									}
									else if (result == -1) {
										logger.severe("Error creating StaffMember");
									}
								}	// For StaffMembers
								logger.info("Imported CMs' FOR DEP: " + department.getTitle() + " -> NEW=" + ImportedUsers + " Duplicates=" + ImportedDuplcateUsers);
							}  // StaffMembers != null
						}   //For Departments
					}   // Departments != null
				}   //For Schools
				return true;
			} // Schools ! = null
			else {
				return false;
			}
		}
		catch (Exception e) {
			logger.severe("General Exception importing OLD DB Schools, Departments and Courses:" + Arrays.toString(e.getStackTrace()));
			return false;
		}
	}

	public boolean ImportNonStaffMembers(Institution institution) {

		int ImportedUsers = 0;

		DlmUsers dlmUsers = elegacyRepository.getDlmUsers(import_url, "guest", "guest", main_col + "Users", "nonStaffMembers", "");
		logger.info("MANAGERS - ALL_USERS=" + dlmUsers.getUser().size());
		try {
			if (!dlmUsers.getUser().isEmpty()) {
				for (int i = 0; i < dlmUsers.getUser().size(); i++) {
					DlmUser dlmUser = dlmUsers.getUser().get(i).getDlmUser();
					boolean res = updateService.Create_NONSTAFFMEMBER(dlmUser, institution,"after-create-document");
					if (res) {
						ImportedUsers++;
					}
				} // FOR
				logger.info("IMPORTED MANAGERS:" + ImportedUsers);
				return true;
			}
			else {
				logger.severe("NO MANAGERS FOUND IN LEGACY EXIST-DB. Abort");
				return false;
			}
		}
		catch (Exception e) {
			logger.severe("EXCEPTION IMPORTING MANAGERS FROM LEGACY EXIST-DB. Abort");
			return false;
		}
	}

	public boolean ImportStreamingServers(org.opendelos.legacydomain.institution.Institution legacyInstitution) {

		int ImportedStreamingServers = 0;
		streamingServerService.deleteAll();
		try {

			if (legacyInstitution.getStreamingServers() != null) {
				logger.info("Importing :" + legacyInstitution.getStreamingServers().getStreamingServer().size() + " servers...");
				for (int i = 0; i < legacyInstitution.getStreamingServers().getStreamingServer().size(); i++) {
					StreamingServer streamingserver = new StreamingServer();
					org.opendelos.legacydomain.institution.StreamingServer oserver;
					oserver = legacyInstitution.getStreamingServers().getStreamingServer().get(i);
					streamingserver.setType(oserver.getType());
					streamingserver.setIdentity(oserver.getId());
					streamingserver.setCode(oserver.getCode());
					streamingserver.setDescription(oserver.getDescription());
					streamingserver.setServer(oserver.getServer());
					streamingserver.setPort(oserver.getPort());
					streamingserver.setProtocol(oserver.getProtocol());
					streamingserver.setApplication(oserver.getApplication());
					streamingserver.setAdminUser(oserver.getAdminUser());
					streamingserver.setAdminPassword(oserver.getAdminPassword());
					streamingserver.setAdminPort(oserver.getAdminPort());
					streamingserver.setRestPort(oserver.getRestPort());
					streamingserver.setEnabled("false");
					streamingserver.setProtocol("https");
					streamingServerService.create(streamingserver);
					ImportedStreamingServers++;
				}
				logger.info("Imported Streaming Servers:" + ImportedStreamingServers);
				return true;
			}
			else {
				logger.severe("NO Streaming Servers FOUND IN LEGACY EXIST-DB. Abort");
				return false;
			}
		}
		catch (Exception e) {
			logger.severe("EXCEPTION IMPORTING STREAMING SERVERS FROM LEGACY EXIST-DB. Abort");
			return false;
		}
	}

	public boolean ImportEvents(org.opendelos.legacydomain.institution.Institution legacyInstitution, String institution_id) {

		int i;
		int validEvents = 0;
		scheduledEventService.deleteAll();
		XEvents xEvents = elegacyRepository.findAllEvents(import_url, "guest", "guest", main_col + "Events", legacyInstitution.getId());
		try {
			if (xEvents != null && xEvents.getEvent() != null && xEvents.getEvent().size() > 0) {
				logger.info("Importing :" + xEvents.getEvent().size() + " scheduled events...");
				for (i = 0; i < xEvents.getEvent().size(); i++) {
					Event oevent = xEvents.getEvent().get(i);
					updateService.createUpdateScheduledEvent(oevent,"after-create-document",legacyInstitution,institution_id);
					validEvents++;
				} //FOR
				logger.info("Imported Scheduled Events :" + validEvents);
				return true;
			}
			else {
				logger.severe("NO Scheduled Events FOUND IN LEGACY EXIST-DB. Abort");
				return false;
			}

		}
		catch (Exception e) {
			logger.severe("EXCEPTION IMPORTING Scheduled Events FROM LEGACY EXIST-DB. Abort");
			return false;
		}
	}

	public boolean ImportScheduled(String institution_id, String year) {

		int i;
		int validEvents = 0;

		int counter_inactive = 0;
		int counter_events = 0;
		int counter_extra = 0;
		int counter_errors = 0;

		List<DsmCalendarXml> dsmEvents = elegacyRepository.findAllScheduled(import_url, "guest", "guest", main_col +  "Scheduler/Calendar", institution_id,year);
		try {
			if (dsmEvents != null && dsmEvents.size() > 0) {
				logger.info("Importing :" + dsmEvents.size() + " scheduled...");
				for (DsmCalendarXml dsmCalendarXml: dsmEvents) {
					Schedule schedule = new Schedule();
					// import only lectures and regular
					if (dsmCalendarXml.getUnitId().equals("-1")) {
						counter_events++;
						continue;
					}
					if (!dsmCalendarXml.getTypeId().equals("1")) {
						if (dsmCalendarXml.getTypeId().equals("2")) {
							counter_extra++;
						}
						continue;
					}
					if (!dsmCalendarXml.getStatus().equals("active")) {
						counter_inactive++;
						continue;
					}
					schedule.setEnabled(true);
					schedule.setType("lecture");
					schedule.setRepeat("regular");
					schedule.setAcademicYear(year);
					schedule.setDateModified(LocalDateTime.now());
					//editor
					OpUser opUser = opUserService.findByIdentity(dsmCalendarXml.getCreatorId());
					if (opUser == null) {
						opUser = opUserService.findByUid("sa");
					}
					schedule.setEditor(opUser.getId());
					//supervisor
					OpUser supervisor = opUserService.findByIdentity(dsmCalendarXml.getTeacherId());
					if (supervisor == null) {
						counter_errors++;
						continue;
					}
					schedule.setSupervisor(supervisor.getId());
					//period
					if (dsmCalendarXml.getPeriodId().contains("Winter")) {
						schedule.setPeriod("winter");
					}
					else if (dsmCalendarXml.getPeriodId().contains("Intermediate")) {
						schedule.setPeriod("intervening");
					}
					else if (dsmCalendarXml.getPeriodId().contains("Summer")) {
						schedule.setPeriod("summer");
					}
					else {
						schedule.setPeriod("spring");
					}
					//department
					Department department = departmentService.findByIdentity(dsmCalendarXml.getUnitId());
					if (department == null) {
						 counter_errors++;
						 continue;
					}
 					schedule.setDepartment(department.getId());
 					//course
 					Course course = courseService.findByIdentity(dsmCalendarXml.getCourseId());
 					if (course == null) {
 						counter_errors++;
 						continue;
					}
					schedule.setCourse(course.getId());
					//classroom
					Classroom classroom = classroomService.findByIdentity(dsmCalendarXml.getRoomId());
					if (classroom == null) {
						counter_errors++;
						continue;
					}
					schedule.setClassroom(classroom.getId());
					//broadcast
					if (dsmCalendarXml.getBroadcast().equals("broadcasting")) {
						schedule.setBroadcast(true);
					}
					//record
					if (dsmCalendarXml.getRecord().equals("recording")) {
						schedule.setRecording(true);
					}
					//publish
					if (dsmCalendarXml.getPublish().equals("public")) {
						schedule.setPublication("public");
					}
					else {
						schedule.setPublication("private");
					}
					//access
					switch (dsmCalendarXml.getActionAccessId()) {
					case "open":
						schedule.setAccess("open");
						break;
					case "secured":
						schedule.setAccess("sso");
						break;
					case "restricted":
						schedule.setAccess("sso");
						break;
					case "password":
						schedule.setAccess("sso");
						break;
					}
					int dayOfWeek = Integer.parseInt(dsmCalendarXml.getDayId());
					if (dayOfWeek == 1) {
						dayOfWeek = 7;
					}
					else {
						dayOfWeek = dayOfWeek -1;
					}
					schedule.setDayOfWeek(DayOfWeek.of(dayOfWeek));
					//startTime  + duration
					schedule.setStartTime(dsmCalendarXml.getHourId() + ":" + dsmCalendarXml.getMinutes());
					schedule.setDurationHours(Integer.parseInt(dsmCalendarXml.getDurationHours()));
					schedule.setDurationMinutes(Integer.parseInt(dsmCalendarXml.getDurationMinutes()));

					schedule.setId(null);
					scheduleService.create(schedule);
					validEvents++;
				} //FOR

				logger.info("Imported Scheduled  :" + validEvents);
				logger.info("Inactive  :" + counter_inactive);
				logger.info("events  :" + counter_events);
				logger.info("extra  :" + counter_extra);
				logger.info("errros  :" + counter_errors);

				return true;
			}
			else {
				logger.severe("NO Scheduled Events FOUND IN LEGACY EXIST-DB. Abort");
				return false;
			}

		}
		catch (Exception e) {
			logger.severe("EXCEPTION IMPORTING Scheduled Events FROM LEGACY EXIST-DB. Abort");
			return false;
		}
	}

	public void ImportResources(String institution_id) {
		long imported = 0;
		long invalid = 0;
		try {
			QueryResponse queryResults;
			String xmlQuery= this.ConstructXQuery();
			queryResults   = (QueryResponse) elegacyRepository.QueryDatabase(import_url,"guest","guest",
					main_col + "Videolectures",xmlQuery,QueryResponse.class);
			logger.info("READ VIDEOLECTURES:" + queryResults.getResources().size());
			if (!queryResults.getResources().isEmpty()) {
				 resourceService.deleteAll();
				for (ResourcesType resourcesType: queryResults.getResources()) {
					VideoLecture videoLecture = resourcesType.getVideoLecture();
					int result = updateService.createUpdateDocument(videoLecture,"after-create-document",institution_id);
					if (result == 1) {
						imported++;
					}
					else {
						invalid++;
					}
				}
				logger.info("Resource Imported:" + imported);
				logger.info("Invalid Resources:" + invalid);
			}
		}
		catch (Exception e) {
			logger.severe("FAILED IMPORTING LECTURES");
		}
	}



	private String ConstructXQuery() {

		StringBuilder _EvalQuery = new StringBuilder();
		String xQuery;
		String  namespace  = " declare namespace vl=\"http://gunet.gr/VideoLecture\";";
		namespace += " declare namespace tns=\"http://gunet.gr/QueryResponse\";";
		namespace += " declare namespace ft =\"http://exist-db.org/xquery/lucene\"; ";
		namespace += " declare namespace xmldb=\"http://exist-db.org/xquery/xmldb\"; ";

		String collection = " for $m1 in xmldb:xcollection('" + main_col + "Videolectures/')//vl:VideoLecture";

		StringBuilder prereq = new StringBuilder();


		//# MG: SKIP THIS FOR NOW! 20-02-2022
/*

		prereq.append("[(vl:Relation/vl:Event/vl:Identity>'!') or ");
		String startDate = "2021-09-01";
		String endDate = "2022-08-31";
		prereq.append("((vl:Date > '").append(startDate).append("') and ");
		prereq.append("(vl:Date < '").append(endDate).append("'))]");

*/

	/*	prereq.append("[vl:Status/vl:StatusProperty[@name='Video'] = '1']");*/

	 	if (public_only) {
			prereq.append("[vl:Rights/vl:Security = 'public']");
		}


		_EvalQuery.append(" return $m1 ");
		// Construct __EvalQuery
		_EvalQuery.insert(0, collection + prereq);
		xQuery = namespace + " let $recs:=util:eval(\"" + _EvalQuery + "\"), ";
		xQuery = xQuery + " $count := count($recs) ";
		xQuery = xQuery
				+ " return <tns:QueryResponse><tns:NumofResults>{$count}</tns:NumofResults>"
				+ "{for $p in $recs return (<tns:Resources><tns:ResourceID>"
				+ "{substring-before(util:document-name($p),'.')}</tns:ResourceID>{$p}</tns:Resources>)}"
				+ "</tns:QueryResponse>";
		return xQuery;

	}

	public void matchRelations() {

		List<Resource> resources = resourceService.findAll();
		for (Resource resource: resources) {
			if ( resource.getType().equals("EVENT") && resource.isParts()) {
/*				if ( resource.getEvent() != null && resource.getEvent().getId() != null ) {

					List<Resource> parentResources = resourceService.findParentEventResource(resource.getEvent().getId());
					if ( !parentResources.isEmpty() ) {
						if (parentResources.size() >1) {
							logger.warning("More than 1 parents found.. abort");
							continue;
						}
						resource.setParentId(parentResources.get(0).getId());
						resourceService.update(resource);
					}
				}*/
				// NOTE! IMPORTANT!
				//NO NEED TO FIND RELATED PARTS FOR SCHEDULED EVENTS:: YOU CAN FIND EXACT RESOURCES BY event.id field
			}
 			if (resource.getType().equals("COURSE") && resource.isParts()) {
				if (resource.getCourse() != null && resource.getCourse().getId() != null) {
					ResourceQuery resourceQuery = new ResourceQuery();
					resourceQuery.setResourceType("c");
					resourceQuery.setCourseId(resource.getCourse().getId());
					resourceQuery.setClassroomId(resource.getClassroom());
					resourceQuery.setPeriod(resource.getPeriod());
					resourceQuery.setAcademicYear(resource.getAcademicYear());
					try {
						resourceQuery.setInstantDate(resource.getDate());
					}
					catch (Exception e) {
						continue;
					}
					resourceQuery.setLimit(-1);
					resourceQuery.setSort("date");
					resourceQuery.setDirection("asc");
					QueryResourceResults queryResourceResults = resourceService.searchPageableLectures(resourceQuery);
					if (queryResourceResults.getTotalResults() > 0) {
						logger.info("Found matching COURSE Resources: " + queryResourceResults.getTotalResults());
						List<String> relatedParts = new ArrayList<>();
						for (Resource related_resource: queryResourceResults.getSearchResultList()) {
							if (!related_resource.getId().equals(resource.getId())) {
								relatedParts.add(related_resource.getId());
							}
						}
						resource.setRelatedParts(relatedParts.toArray(new String[0]));
						resourceService.update(resource);
					}
				}
			}
		}
	}

	public void restoreEvents() {

		List<ScheduledEvent> newEvents = scheduledEventService.findAllInCollection("opendelos.events");

		int i=0;
		for (ScheduledEvent scheduledEvent: newEvents) {
			try {
				ScheduledEvent filled_event = scheduledEventService.findMatchInCollection(scheduledEvent.getTitle(), scheduledEvent.getStartDate(), "opendelos.events.filled");
				if (filled_event != null) {
					scheduledEvent.setArea(filled_event.getArea());
					scheduledEvent.setType(filled_event.getType());
					scheduledEvent.setCategories(filled_event.getCategories());
					scheduledEventService.update(scheduledEvent);
					i++;
				}
			}
			catch (Exception e) {
				continue;
			}
		}
		logger.info("Updated:" + i + " events");

		int j=0;
		List<Resource> resourceList = resourceService.findAll();
		for (Resource resource: resourceList) {
			if (resource.getType().equals("EVENT")) {
				String event_id = resource.getEvent().getId();
				ScheduledEvent scheduledEvent = scheduledEventService.findById(event_id);
				if (scheduledEvent != null) {
					resource.setEvent(scheduledEvent);
					resourceService.update(resource);
					j++;
				}
			}
		}
		logger.info("Updated:" + j + " resources");
	}

}
