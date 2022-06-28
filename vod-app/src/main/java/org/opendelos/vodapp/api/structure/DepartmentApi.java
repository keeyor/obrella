/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.vodapp.api.structure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendelos.model.calendar.Argia;
import org.opendelos.model.calendar.Argies;
import org.opendelos.model.calendar.Periods;
import org.opendelos.model.common.Select2GenChild;
import org.opendelos.model.common.Select2GenGroup;
import org.opendelos.model.dates.CustomPause;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.structure.Classroom;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.Device;
import org.opendelos.model.structure.School;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.vodapp.api.common.ApiUtils;
import org.opendelos.vodapp.services.opUser.OpUserService;
import org.opendelos.vodapp.services.scheduler.ScheduleUtils;
import org.opendelos.vodapp.services.structure.ClassroomService;
import org.opendelos.vodapp.services.structure.CourseService;
import org.opendelos.vodapp.services.structure.DepartmentService;
import org.opendelos.vodapp.services.structure.InstitutionService;
import org.opendelos.vodapp.services.structure.SchoolService;

import org.springframework.beans.factory.annotation.Autowired;
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
public class DepartmentApi {

	private final DepartmentService departmentService;
	private final InstitutionService institutionService;
	private final SchoolService schoolService;
	private final ClassroomService classroomService;
	private final OpUserService opUserService;
	private final CourseService courseService;
	private final ScheduleUtils scheduleUtils;

	@Autowired
	public DepartmentApi(DepartmentService departmentService, InstitutionService institutionService, SchoolService schoolService, ClassroomService classroomService, OpUserService opUserService, CourseService courseService, ScheduleUtils scheduleUtils) {
		this.departmentService = departmentService;
		this.institutionService = institutionService;
		this.schoolService = schoolService;
		this.classroomService = classroomService;
		this.opUserService = opUserService;
		this.courseService = courseService;
		this.scheduleUtils = scheduleUtils;
	}

	@RequestMapping(value = "/apiw/v1/s2/departments.web/school/{schoolId}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getDepartmentsBySchoolIds2(@PathVariable String schoolId, Locale locale) {

		List<Select2GenGroup> select2GenGroupList = departmentService.getAllDepartmentsGroupedBySchool(schoolId, locale);
		try {
			//select2GenGroupList.sort(new TitleSorter());
			String s2departments = ApiUtils.FormatResultsForSelect2(select2GenGroupList);
			return new ResponseEntity<>(s2departments, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/apiw/v2/s2/departments.web/authorized/{access}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getAuthorizedDepartmentsBySchoolIds2(@PathVariable String access) {

		OoUserDetails user = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		OpUser user_info = opUserService.findByUid(user.getUid());
		boolean isSA = user_info.getRights().getIsSa();

		List<String> authorized_unit_ids = opUserService.getManagersAuthorizedDepartmentIdsByAccessType(user.getId(), access);

		List<School> schools;
		schools = schoolService.findAllSortedByTitle();

		List<Select2GenGroup> select2GenGroupList = new ArrayList<>();
		for (School school : schools) {
			//set group properties
			Select2GenGroup select2GenGroup = new Select2GenGroup();
			select2GenGroup.setId(school.getId());
			select2GenGroup.setText(school.getTitle());
			//set children properties
			List<Select2GenChild> groupChildren = new ArrayList<>();
			List<Department> schoolDepartments = departmentService.findBySchoolId(school.getId());
			for (Department department : schoolDepartments) {
				if (authorized_unit_ids.contains(department.getId()) || isSA || authorized_unit_ids.contains("IGNORE_UNIT")) {
					Select2GenChild select2GenChild = new Select2GenChild();
					select2GenChild.setId(department.getId());
					select2GenChild.setText(department.getTitle());
					groupChildren.add(select2GenChild);
				}
			}
			if (groupChildren.size() > 0) {
				select2GenGroup.setChildren(groupChildren);
				select2GenGroupList.add(select2GenGroup);
			}
		}
		try {
			String s2departments = ApiUtils.FormatResultsForSelect2(select2GenGroupList);
			return new ResponseEntity<>(s2departments, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/apiw/v1/s3/departments.web/school/{schoolId}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getDepartmentsIdentityBySchoolIds2(@PathVariable String schoolId) {

		List<School> schools = new ArrayList<>();
		if (schoolId.trim().equals("dummy") || schoolId.trim().equals("")) {
			schools = schoolService.findAllSortedByTitle();
		}
		else {
			School school = schoolService.findById(schoolId);
			schools.add(school);
		}
		List<Select2GenGroup> select2GenGroupList = new ArrayList<>();
		for (School school : schools) {
			//set group properties
			Select2GenGroup select2GenGroup = new Select2GenGroup();
			select2GenGroup.setId(school.getId());
			select2GenGroup.setText(school.getTitle());
			//set children properties
			List<Select2GenChild> groupChildren = new ArrayList<>();
			List<Department> schoolDepartments = departmentService.findBySchoolId(school.getId());
			for (Department department : schoolDepartments) {
				Select2GenChild select2GenChild = new Select2GenChild();
				select2GenChild.setId(department.getIdentity());
				select2GenChild.setText(department.getTitle());
				groupChildren.add(select2GenChild);
			}
			select2GenGroup.setChildren(groupChildren);
			select2GenGroupList.add(select2GenGroup);
		}
		try {
			String s2departments = ApiUtils.FormatResultsForSelect2(select2GenGroupList);
			return new ResponseEntity<>(s2departments, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/apiw/v1/dt/departments.web", method = RequestMethod.GET)
	public byte[] findAllForDt() {

		List<Department> departments = departmentService.findAll();
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(departments);
		return b;
	}

	@RequestMapping(value = "/apiw/v1/dt/class.web/department/{id}", method = RequestMethod.GET)
	public byte[] findAllDepartmentClassroomsForDt(@PathVariable("id") String id) {

		List<Classroom> classrooms = new ArrayList<>();
		List<String> classroomIds = departmentService.findClassroomIdsById(id);
		for (String classroomsId : classroomIds) {
			Classroom classroom = classroomService.findById(classroomsId);
			if (classroom == null) {
				System.out.println("Classroom: " + classroomsId + " not found");
				continue;
			}
			classrooms.add(classroom);
		}
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(classrooms);
		return b;
	}

	@RequestMapping(value = "/apiw/v1/dt/class.web/u/department/{id}", method = RequestMethod.GET)
	public byte[] findAllUnAssignedDepartmentClassroomsForDt(@PathVariable("id") String id) {

		List<String> department_assigned_classrooms = departmentService.findClassroomIdsById(id);
		List<Classroom> unaAssignedClassRooms = classroomService.findAllExcludingIds(department_assigned_classrooms);
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(unaAssignedClassRooms);
		return b;
	}

	@RequestMapping(value = "/apiw/v1/department/assign_rooms/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public void AssignRoomsToDepartment(@RequestBody String[] ids, @PathVariable("id") String id) {
		departmentService.assignRoomsToDepartment(id, ids);
	}

	@RequestMapping(value = "/apiw/v1/department/unassign_room/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public void UnAssignCourseFromStaffMember(@RequestBody String roomId, @PathVariable("id") String id) {
		departmentService.unAssignRoomFromDepartment(id, roomId);
	}

	@RequestMapping(value = "/apiw/v1/dt/devices.web/classroom/{id}", method = RequestMethod.GET)
	public byte[] findAllDevicesForClassroomByIdForDt(@PathVariable("id") String id) {

		Classroom classroom = classroomService.findById(id);
		List<Device> classrooms = classroom.getDevices();

		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(classrooms);
		return b;
	}

	@RequestMapping(value = "/apiw/v1/dt/departments.web/school/{schoolId}", method = RequestMethod.GET)
	public byte[] findBySchoolIdForDt(@PathVariable("schoolId") String schoolId) {

		List<Department> departments = departmentService.findBySchoolId(schoolId);
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(departments);
		return b;
	}

	@RequestMapping(value = "/apiw/v1/department/save", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> updateDepartment(@RequestBody Department department) {

		String _id;
		try {
			if (department.getId() == null || department.getId().equals("")) {
				_id = departmentService.create(department);
			}
			else {
				departmentService.findAndUpdate(department);
				_id = department.getId();
			}
			return new ResponseEntity<>(_id, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/apiw/v1/department/delete/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteDepartment(@PathVariable("id") String id) {

		try {
			departmentService.delete(id);
			return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/* CALENDAR ACTION */
	@RequestMapping(method = RequestMethod.GET, value = "/apiw/v1/dt/institution/{iid}/department/{id}/calendar/{year}")
	public byte[] getDepartmentCalendarDt(@PathVariable("iid") String iid, @PathVariable("id") String id, @PathVariable("year") String year) {

		CustomPeriod customPeriod = departmentService.getDepartmentCalendar(id, iid, year);
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(customPeriod);
		return b;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/apiw/v1/department/{id}/calendar/update/{year}", consumes = "application/json", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> updateCalendar(@PathVariable("id") String id, @RequestBody String jsonString, @PathVariable("year") String year) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
		Periods periods;
		periods = mapper.readValue(jsonString, Periods.class);
		CustomPeriod customPeriod = new CustomPeriod();
		customPeriod.setYear(year);
		customPeriod.setPeriods(periods);
		customPeriod.setInherited(true);
		try {
			departmentService.saveCustomPeriod(id, customPeriod);
			return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/apiw/v1/department/{id}/calendar/reset/{year}", consumes = "application/json", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> resetCalendar(@PathVariable("id") String id, @PathVariable("year") String year) throws JsonProcessingException {

		try {
			departmentService.deleteCustomPeriod(id, year);
			return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/* ARGIES ACTION */
	@RequestMapping(method = RequestMethod.GET, value = "/apiw/v1/dt/department/{id}/pause/{year}")
	public byte[] getPauseDt(@PathVariable("id") String id, @PathVariable("year") String year) {

		CustomPause customPause = departmentService.getCustomPause(id, year);
		if (customPause == null) {
			customPause = new CustomPause();
			customPause.setArgies(new Argies());
		}
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(customPause);
		return b;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/apiw/v1/department/{id}/pause/update/{year}", consumes = "application/json", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> updatePause(@PathVariable("id") String id, @RequestBody String jsonString, @PathVariable("year") String year) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
		Argies argies;
		argies = mapper.readValue(jsonString, Argies.class);
		CustomPause customPause = new CustomPause();
		customPause.setYear(year);
		customPause.setArgies(argies);
		try {
			departmentService.saveCustomPause(id, customPause);
			return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/apiw/v1/department/{id}/pause/delete/{year}", consumes = "application/json", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> deletePause(@PathVariable("id") String id, @PathVariable("year") String year) throws JsonProcessingException {

		try {
			departmentService.deleteCustomPause(id, year);
			return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/* RECURSIVE PAUSES */
	@RequestMapping(method = RequestMethod.GET, value = "/apiw/v1/dt/institution/{iid}/department/{id}/pause-recursive/{year}")
	public byte[] getPauseRecursiveDt(@PathVariable("iid") String iid, @PathVariable("id") String id, @PathVariable("year") String year) {

		CustomPause customPause = departmentService.getCustomPause(id, year);
		if (customPause == null) {
			customPause = new CustomPause();
			customPause.setYear(year);
			customPause.setArgies(new Argies());
		}
		/* Get institution argies */
		CustomPause customPause_institution = institutionService.getCustomPause(id, year);
		List<Argia> argiaList_institution = customPause_institution.getArgies().getArgia();
		/* Add institution argies to department argies */
		List<Argia> argiaList = customPause.getArgies().getArgia();
		argiaList.addAll(argiaList_institution);

		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(customPause);
		return b;

	}

	public class TitleSorter implements Comparator<Select2GenGroup> {
		@Override
		public int compare(Select2GenGroup o1, Select2GenGroup o2) {
			return o1.getText().compareToIgnoreCase(o2.getText());
		}
	}

}
