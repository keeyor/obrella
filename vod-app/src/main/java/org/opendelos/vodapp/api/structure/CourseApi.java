/* 
     Author: Michael Gatzonis - 2/10/2020 
     live
*/
package org.opendelos.vodapp.api.structure;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.opendelos.model.calendar.Period;
import org.opendelos.model.common.Select2GenChild;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.StudyProgram;
import org.opendelos.model.structure.dtos.CourseDto;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.vodapp.api.common.ApiUtils;
import org.opendelos.vodapp.services.i18n.MultilingualServices;
import org.opendelos.vodapp.services.opUser.OpUserService;
import org.opendelos.vodapp.services.scheduler.ScheduleUtils;
import org.opendelos.vodapp.services.structure.CourseService;
import org.opendelos.vodapp.services.structure.DepartmentService;
import org.opendelos.vodapp.services.structure.StudyProgramService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CourseApi {

	private final CourseService courseService;
	private final DepartmentService departmentService;
	private final StudyProgramService studyProgramService;
	private final OpUserService opUserService;
	private final MultilingualServices multilingualServices;
	private final ScheduleUtils scheduleUtils;
	@Value("${app.zone}")
	String app_zone;

	@Autowired
	public CourseApi(CourseService courseService, DepartmentService departmentService, StudyProgramService studyProgramService, OpUserService opUserService, MultilingualServices multilingualServices, ScheduleUtils scheduleUtils) {
		this.courseService = courseService;
		this.departmentService = departmentService;
		this.studyProgramService = studyProgramService;
		this.opUserService = opUserService;
		this.multilingualServices = multilingualServices;
		this.scheduleUtils = scheduleUtils;
	}

	@RequestMapping(value = "/api/v1/s2/courses.web/department/{id}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getCoursesByDepartmentIds2(@PathVariable String id) {

		List<Course> courses;
		if (id.trim().equals("dummy") || id.trim().equals("")) {
			courses = courseService.findAll();
		}
		else {
			courses = courseService.findByDepartmentId(id);
		}
		List<Select2GenChild> children = new ArrayList<>();
		for (Course course : courses) {
			Select2GenChild child = new Select2GenChild();
			child.setId(course.getId());
			child.setText(course.getTitle());
			children.add(child);
		}
		try {
			String s2users = ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2users, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/dt/courses.web/department/{id}", method = RequestMethod.GET)
	public byte[] findByIdDt(@PathVariable("id") String id) {

		List<Course> courses;
		if (id.trim().equals("dummy") || id.trim().equals("")) {
			courses = courseService.findAll();
		}
		else {
			courses = courseService.findByDepartmentId(id);
		}
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(courses);
		return b;
	}

	@RequestMapping(value = "/api/v1/dt/courses.web/all", method = RequestMethod.GET)
	public byte[] findAllCoursesDt(Locale locale) {

		List<CourseDto> courseDtoList = new ArrayList<>();
		List<Course> courses;
		courses = courseService.findAll();
		for (Course course : courses) {
			CourseDto courseDto = new CourseDto();
			BeanUtils.copyProperties(course, courseDto);
			if (course.getStudy() != null && !course.getStudy().equals("")) {
				String studyTitle = multilingualServices.getValue(course.getStudy(), locale);
				courseDto.setStudyTitle(studyTitle);
			}
			else {
				String studyTitle = multilingualServices.getValue("under", locale);
				courseDto.setStudy("under");
				courseDto.setStudyTitle(studyTitle);
			}
			if (course.getStudyProgramId() != null && !course.getStudyProgramId().equals("program_default")) {
				String studyProgramTitle = studyProgramService.findById(course.getStudyProgramId()).getTitle();
				courseDto.setStudyProgramTitle(studyProgramTitle);
			}
			else {
				String studyProgramTitle = multilingualServices.getValue("program_default", locale);
				courseDto.setStudyProgramTitle(studyProgramTitle);
				courseDto.setStudyProgramId("program_default");
			}
			courseDtoList.add(courseDto);
		}
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(courseDtoList);
		return b;
	}

	@RequestMapping(value = "/api/v1/dt/courses.web/staff/{sid}/department/{did}", method = RequestMethod.GET)
	public byte[] getCoursesByStaffIdDt(@PathVariable("sid") String sid, @PathVariable("did") String did) {

		List<Course> courses = new ArrayList<>();
		for (String course_id : opUserService.findById(sid).getCourses()) {
			Course smCourse = courseService.findById(course_id);
			courses.add(smCourse);
		}
		courses.removeIf(course -> !course.getDepartment().getId().equals(did));

		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(courses);
		return b;
	}

	@RequestMapping(value = "/api/v1/s2/courses.web/staff/{sid}/user/{uid}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getAuthorizedCoursesByStaffIdDt(@PathVariable("sid") String sid, @PathVariable("uid") String uid) {

		List<Course> courses = courseService.getAuthorizedCoursesByStaffIdAndUserId(sid, uid);

		courses.sort(Comparator.comparing(Course::getTitle));

		List<Select2GenChild> children = new ArrayList<>();
		for (Course course : courses) {
			Select2GenChild child = new Select2GenChild();
			child.setId(course.getId());
			child.setText(course.getTitle());
			child.setSubheader("Τμήμα " + course.getDepartment().getTitle());
			children.add(child);
		}
		try {
			String s2courses = ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2courses, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/s2/courses.web/staff/{id}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getCoursesByStaffId(@PathVariable("id") String id) {

		List<String> courseIds = opUserService.findStaffMembersAssignedCourseIds(id);
		List<Course> courses = courseService.findFromIds(courseIds);
		courses.sort(Comparator.comparing(Course::getTitle));

		List<Select2GenChild> children = new ArrayList<>();
		for (Course course : courses) {
			Select2GenChild child = new Select2GenChild();
			child.setId(course.getId());
			child.setText(course.getTitle());
			child.setSubheader("Τμήμα " + course.getDepartment().getTitle());
			children.add(child);
		}
		try {
			String s2courses = ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2courses, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}


	@RequestMapping(value = "/api/v2/dt/courses.web/authorized/{access}", method = RequestMethod.GET, produces = "application/json")
	public byte[] getAuthorizedCoursesOfEditorDT(@PathVariable String access) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		List<Course> courses = courseService.getAuthorizedCoursesByEditor(editor, access);
		courses.sort(Comparator.comparing(Course::getTitle));

		return ApiUtils.TransformResultsForDataTable(courses);
	}

	@RequestMapping(value = "/api/v2/dt/courses.web/authorized/{access}/d/{departmentId}", method = RequestMethod.GET, produces = "application/json")
	public byte[] getAuthorizedCoursesOfEditorAndDepartmentIdDT(@PathVariable String access, @PathVariable String departmentId) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		List<Course> courses = courseService.getAuthorizedCoursesByEditor(editor, access);
		courses.sort(Comparator.comparing(Course::getTitle));

		List<Course> remove_not_in_department = new ArrayList<>();
		for (Course course : courses) {
			if (!course.getDepartment().getId().equals(departmentId)) {
				remove_not_in_department.add(course);
			}
		}
		if (!remove_not_in_department.isEmpty()) {
			courses.removeAll(remove_not_in_department);
		}

		return ApiUtils.TransformResultsForDataTable(courses);
	}

	@RequestMapping(value = "/api/v1/s2/courses.web/authorized/{access}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getAuthorizedCoursesOfEditor(@PathVariable String access) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		List<Course> courses = courseService.getAuthorizedCoursesByEditor(editor, access);
		courses.sort(Comparator.comparing(Course::getTitle));

		List<Select2GenChild> children = new ArrayList<>();
		for (Course course : courses) {
			Select2GenChild child = new Select2GenChild();
			child.setId(course.getId());
			child.setText(course.getTitle());
			child.setSubheader("Τμήμα " + course.getDepartment().getTitle());
			if (!listContainsCourseById(children, course.getId())) {
				children.add(child);
			}
		}
		try {
			String s2courses = ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2courses, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	public boolean listContainsCourseById(final List<Select2GenChild> list, final String id) {
		return list.stream().anyMatch(o -> o.getId().equals(id));
	}

	@RequestMapping(value = "/api/v1/dt/courses.web/school/{schoolId}/department/{departmentId}/study/{study}/program/{programId}", method = RequestMethod.GET)
	public byte[] findProgramsWithCriteria(@PathVariable("schoolId") String schoolId,
			@PathVariable("departmentId") String departmentId,
			@PathVariable("study") String study, @PathVariable("programId") String program, Locale locale) {

		List<CourseDto> courseDtoList = new ArrayList<>();
		List<Course> courses = courseService.findWithCriteria(schoolId, departmentId, study, program);

		for (Course course : courses) {
			CourseDto courseDto = new CourseDto();
			BeanUtils.copyProperties(course, courseDto);
			if (course.getStudy() != null && !course.getStudy().equals("")) {
				String studyTitle = multilingualServices.getValue(course.getStudy(), locale);
				courseDto.setStudyTitle(studyTitle);
			}
			else {
				String studyTitle = multilingualServices.getValue("under", locale);
				courseDto.setStudy("under");
				courseDto.setStudyTitle(studyTitle);
			}
			if (course.getStudyProgramId() != null && !course.getStudyProgramId()
					.equals("") && !course.getStudyProgramId().equals("program_default")) {
				StudyProgram studyProgram = studyProgramService.findById(course.getStudyProgramId());
				String studyProgramTitle = studyProgram.getTitle();
				courseDto.setStudyProgramTitle(studyProgramTitle);
			}
			else {
				String studyProgramTitle = multilingualServices.getValue("program_default", locale);
				courseDto.setStudyProgramTitle(studyProgramTitle);
				courseDto.setStudyProgramId("program_default");
			}
			long countTeachingStaff = opUserService.countStaffMembersTeachingCourseId(course.getId());
			courseDto.setTeaching(countTeachingStaff);
			courseDtoList.add(courseDto);
		}

		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(courseDtoList);
		return b;

	}

	@RequestMapping(value = "/api/v1/courses/save", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> saveCourse(@RequestBody Course course) {

		String _id = null;
		;
		//set course department +institution +school
		Department department = departmentService.findById(course.getDepartment().getId());
		Unit course_dep = new Unit(StructureType.DEPARTMENT, department.getId(), department.getTitle());
		course.setDepartment(course_dep);
		course.setSchoolId(department.getSchoolId());
		course.setInstitutionId(department.getInstitutionId());
		//set course study
		if (course.getStudyProgramId().equals("program_default")) {
			course.setStudy("under");
		}
		else {
			StudyProgram studyProgram = studyProgramService.findById(course.getStudyProgramId());
			course.setStudy(studyProgram.getStudy());
		}
		try {
			if (course.getId() == null || course.getId().equals("")) {
				_id = courseService.create(course);
			}
			else {
				courseService.findAndUpdate(course);
				_id = course.getId();
			}
			return new ResponseEntity<>(_id, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(_id, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/courses/delete/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteCourse(@PathVariable("id") String id) {

		try {
			courseService.delete(id);
			return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/course/{id}", method = RequestMethod.GET)
	public ResponseEntity<Course> getCourseById(@PathVariable("id") String id) {

		try {
			Course course = courseService.findById(id);
			return new ResponseEntity<>(course, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}


	@RequestMapping(method = RequestMethod.GET, value = "/api/v1/course/{id}/period/{period_name}/year/{year}")
	public Period getPeriodBoundariesByCourseAndYear(@PathVariable("id") String id, @PathVariable("period_name") String period_name, @PathVariable("year") String year) {

		//Note: We only need the course to get department and/or studyProgram
		Course course = courseService.findById(id);
		if (course != null) {
			String departmentId = course.getDepartment().getId();
			String institutionId = course.getInstitutionId();

			if (course.getStudyProgramId() == null) {
				return departmentService.getDepartmentPeriod(departmentId, institutionId, year, period_name);
			}
			else {
				String studyProgramId = course.getStudyProgramId();
				return studyProgramService.getStudyPeriod(studyProgramId, departmentId, institutionId, year, period_name);
			}
		}
		return null;
	}


	@RequestMapping(method = RequestMethod.GET, value = "/api/v1/course/{id}/date/{date}/year/{year}")
	public String getPeriodNameByCourseAndYear(@PathVariable("id") String id, @PathVariable("date") long date, @PathVariable("year") String year) {

		Period period = null;
		//Note: We only need the course to get department and/or studyProgram
		Course course = courseService.findById(id);
		if (course != null) {
			String departmentId = course.getDepartment().getId();
			LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(date, 0, getZoneOffSetFromZoneId(ZoneId.of(app_zone)));

			if (course.getStudyProgramId() == null) {
				period = scheduleUtils.getDepartmentPeriodByDate(departmentId, year, localDateTime.toLocalDate());
			}
			else {
				String studyProgramId = course.getStudyProgramId();
				period = scheduleUtils.getStudyPeriodByDate(studyProgramId, year, localDateTime.toLocalDate());
			}
		}
		if (period != null) {
			return period.getName();
		}
		return null;
	}

	private ZoneOffset getZoneOffSetFromZoneId(ZoneId zoneId) {
		Instant instant = Instant.now(); //can be LocalDateTime
		return zoneId.getRules().getOffset(instant);
	}

}
