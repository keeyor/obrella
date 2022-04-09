/* 
     Author: Michael Gatzonis - 2/10/2020 
     live
*/
package org.opendelos.liveapp.api.structure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.opendelos.liveapp.api.common.ApiUtils;
import org.opendelos.liveapp.services.i18n.MultilingualServices;
import org.opendelos.liveapp.services.opUser.OpUserService;
import org.opendelos.liveapp.services.structure.CourseService;
import org.opendelos.liveapp.services.structure.DepartmentService;
import org.opendelos.liveapp.services.structure.StudyProgramService;
import org.opendelos.model.common.Select2GenChild;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.dtos.CourseDto;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.model.users.UserAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StaffMemberApi {

	private final Logger logger = LoggerFactory.getLogger(StaffMemberApi.class);

	private final OpUserService opUserService;
	private final CourseService courseService;
	private final DepartmentService departmentService;
	private final StudyProgramService studyProgramService;
	private final MultilingualServices multilingualServices;

	@Autowired
	public StaffMemberApi(OpUserService opUserService, CourseService courseService, DepartmentService departmentService, StudyProgramService studyProgramService, MultilingualServices multilingualServices) {
		this.opUserService = opUserService;
		this.courseService = courseService;
		this.departmentService = departmentService;
		this.studyProgramService = studyProgramService;
		this.multilingualServices = multilingualServices;
	}

	@RequestMapping(value= "/api/v1/s2/staff.web/department/{departmentId}", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getStaffByDepartmentIds2(@PathVariable String departmentId) {

		List<OpUser> staffMembers;
		if (departmentId.trim().equals("dummy") || departmentId.trim().equals("")) {
			staffMembers = opUserService.findAllStaffMembers();
		}
		else {
			staffMembers = opUserService.findStaffMembersOfDepartment(departmentId);
		}
		List<Select2GenChild> children = new ArrayList<>();
		for (OpUser staffMember: staffMembers) {
			Select2GenChild child = new Select2GenChild();
			child.setId(staffMember.getId());
			child.setText(staffMember.getName());
			children.add(child);
		}
		try {
			String s2users= ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2users, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value="/api/v1/dt/staff.web/department/{id}",method = RequestMethod.GET)
	public byte[] getStaffByDepartmentIdDt(@PathVariable("id") String id) {

		List<OpUser> staffMembers;
		if (id.trim().equals("dummy") || id.trim().equals("")) {
			staffMembers = opUserService.findAllStaffMembers();
		}
		else {
			staffMembers = opUserService.findStaffMembersOfDepartment(id);
		}
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(staffMembers);
		return b;
	}

	@RequestMapping(value="/api/v1/dt/staff.web/course/{id}",method = RequestMethod.GET,produces =  "application/json")
	public byte[] getStaffByCourseIdDt(@PathVariable("id") String id) {

		List<OpUser> staffMembers;
		staffMembers = opUserService.findStaffMembersTeachingCourseId(id);

		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(staffMembers);
		return b;
	}

	@RequestMapping(value="/api/v2/dt/staff.web/authorized/{access}",method = RequestMethod.GET, produces =  "application/json")
	public byte[] getAuthorizedStaffMembersOfEditorDT(@PathVariable String access) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		List<OpUser> staffMembers;
		staffMembers = opUserService.getAuthorizedStaffMembersByEditor(editor,access);
		staffMembers.sort(Comparator.comparing(OpUser::getName));

		return ApiUtils.TransformResultsForDataTable(staffMembers);

	}
	@RequestMapping(value="/api/v2/dt/staff.web/authorized/{access}/d/{departmentId}",method = RequestMethod.GET, produces =  "application/json")
	public byte[] getAuthorizedStaffMembersOfEditorAndDepartmentIdDT(@PathVariable String access,@PathVariable String departmentId) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		List<OpUser> staffMembers;
		staffMembers = opUserService.getAuthorizedStaffMembersByEditor(editor,access);
		staffMembers.sort(Comparator.comparing(OpUser::getName));

		if (!departmentId.equals("dummy")) {
			List<OpUser> remove_not_in_department = new ArrayList<>();
			for (OpUser opUser : staffMembers) {
				if (!opUser.getDepartment().getId().equals(departmentId)) {
					remove_not_in_department.add(opUser);
				}
			}
			if (!remove_not_in_department.isEmpty()) {
				staffMembers.removeAll(remove_not_in_department);
			}
		}
		return ApiUtils.TransformResultsForDataTable(staffMembers);

	}

	@RequestMapping(value="/api/v1/s2/staff.web/authorized/{access}",method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getAuthorizedStaffMembersOfEditor(@PathVariable String access, @RequestParam(value = "term", required = false) String term,
			@RequestParam(value = "department", required = false) String departmentId) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		List<OpUser> staffMembers;
		staffMembers = opUserService.getAuthorizedStaffMembersByEditor(editor,access);

		List<Select2GenChild> children = new ArrayList<>();
		for (OpUser staffMember: staffMembers) {
			if (departmentId != null && !departmentId.equals("") && !staffMember.getDepartment().getId().equals(departmentId)) {
					continue;
			}
			Select2GenChild child = new Select2GenChild();
			child.setId(staffMember.getId());
			child.setText(staffMember.getName());
			child.setSubheader(staffMember.getDepartment().getTitle());
			if (!listContainsStaffMemberById(children,staffMember.getId())) {
				if (term != null && term.length() > 1) {
					if (staffMember.getName().contains(term)) {
						children.add(child);
					}
				}
				else {
					children.add(child);
				}
			}
		}
		try {
			String s2users= ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2users, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}

	}

	public boolean listContainsStaffMemberById(final List<Select2GenChild> list, final String id){
		return list.stream().anyMatch(o -> o.getId().equals(id));
	}

	@RequestMapping(value="/api/v1/s2/staff.web/authorized/course/{id}/{access}",method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getAuthorizedStaffMembersTeachingCourse(@PathVariable("id") String courseId,@PathVariable String access) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		List<OpUser> staffMembers;
		staffMembers = opUserService.getAuthorizedStaffMembersOfCourseIdByEditor(editor, courseId,access);

		List<Select2GenChild> children = new ArrayList<>();
		for (OpUser staffMember: staffMembers) {
			Select2GenChild child = new Select2GenChild();
				child.setId(staffMember.getId());
				child.setText(staffMember.getName() + ", " + staffMember.getAffiliation());
				child.setSubheader("Τμήμα " + staffMember.getDepartment().getTitle());
				children.add(child);
		}
		try {
			String s2users= ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2users, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}
	@RequestMapping(value="/api/v1/s2/staff.web/course/{id}",method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getStaffMembersTeachingCourse(@PathVariable("id") String courseId) {

		List<OpUser> staffMembers;
		staffMembers = opUserService.findStaffMembersTeachingCourseId(courseId);

		List<Select2GenChild> children = new ArrayList<>();
		for (OpUser staffMember: staffMembers) {
			Select2GenChild child = new Select2GenChild();
			child.setId(staffMember.getId());
			child.setText(staffMember.getName() + ", " + staffMember.getAffiliation());
			child.setSubheader("Τμήμα " + staffMember.getDepartment().getTitle());
			children.add(child);
		}
		try {
			String s2users= ApiUtils.FormatResultsForSelect2(children);
			return new ResponseEntity<>(s2users, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value="/api/v1/dt/staff.web/teaching/department/{id}",method = RequestMethod.GET)
	public byte[] getStaffTeachingInDepartmentDt(@PathVariable("id") String id) {

		List<OpUser> staffMembers;
		staffMembers = opUserService.findStaffMembersTeachingInDepartment(id);

		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(staffMembers);
		return b;
	}


	@RequestMapping(value= "/api/v1/dt/courses.web/staff/{id}", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getAssignedCoursesByStaffId(@PathVariable("id") String id, Locale locale) {

		List<Course> courses;
		List<CourseDto> courseDtoList = new ArrayList<>();
		List<String> courseIds = opUserService.findStaffMembersAssignedCourseIds(id);

		courses = courseService.findFromIds(courseIds);

		for (Course course: courses) {
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
			//# Get course+staff => Supported Personnel
			List<OpUser> supportPersonnel  = opUserService.getSupportPersonnelForCourseAndStaffMember(course.getId(),id);
			//Debug
			List<Person> supportedBy = new ArrayList<>();
			if (supportPersonnel != null && !supportPersonnel.isEmpty()) {
				for (OpUser opUser : supportPersonnel) {
					 Person person = new Person();
					 person.setId(opUser.getId());
					 person.setName(opUser.getName());
					 supportedBy.add(person);
					//logger.info("For Course:" + course.getTitle() + " is Support Person:" + supportPersonnel.get(i).getName());
				}
			}
			courseDto.setSupportedBy(supportedBy);
			courseDtoList.add(courseDto);
		}
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(courseDtoList);
		return b;
	}

	@RequestMapping(value= "/api/v1/dt/courses.web/staff/u/{id}/dp/{did}", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getUnAssignedCoursesByStaffId(@PathVariable("id") String id, @PathVariable("did") String did, Locale locale) {

		List<Course> courses;
		List<CourseDto> courseDtoList = new ArrayList<>();
		List<String> courseIds = opUserService.findStaffMembersAssignedCourseIds(id);

		courses = courseService.findExcludingIds(courseIds,did);

		for (Course course: courses) {
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

	@RequestMapping(value = "/api/v1/staff/assign_courses/{id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public void AssignCoursesToStaffMember(@RequestBody String[] ids, @PathVariable("id") String id)  {

		opUserService.assignCoursesToStaffMemberById(id,ids);
	}

	@RequestMapping(value = "/api/v1/staff/unassign_course/{staff_id}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public void UnAssignCourseFromStaffMember(@RequestBody String id, @PathVariable("staff_id") String staff_id)  {

		opUserService.unassignCourseFromStaffMemberById(staff_id,id);
	}

	@RequestMapping(value = "/api/v1/staff/save", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> saveStaffMember(@RequestBody OpUser staffMember)  {

		String _id;
		//set course department
		Department department = departmentService.findById(staffMember.getDepartment().getId());
		Unit staff_dep = new Unit(StructureType.DEPARTMENT,department.getId(), department.getTitle());
		staffMember.setDepartment(staff_dep);
		try {
			if (staffMember.getId() == null || staffMember.getId().equals("")) {
				 OpUser opUser = opUserService.findByUid(staffMember.getUid());
				 if (opUser != null) {
					 return new ResponseEntity<>("Το όνομα χρήστη χρησιμοποιείτε ήδη. Προσπαθήστε πάλι", HttpStatus.BAD_REQUEST);
				 }
				 else {
					 opUser = opUserService.findByEmail(staffMember.getEmail());
					 if (opUser != null && opUser.getEmail() != null && opUser.getEmail().equals(staffMember.getEmail())) {
						 return new ResponseEntity<>("Η e-mail διεύθυνση χρησιμοποιείτε ήδη. Προσπαθήστε πάλι", HttpStatus.BAD_REQUEST);
					 }
				 }
				 //identity
				 staffMember.setIdentity(null);
				 //courses
				 List<String> courses = new ArrayList<>();
				 staffMember.setCourses(courses);
				 //user rights
				 UserAccess.UserRights userRights = new UserAccess.UserRights();
				 userRights.setIsSa(false);
				 userRights.setCoursePermissions(null);
				 userRights.setUnitPermissions(null);
				 staffMember.setRights(userRights);
				 //affiliations
				 staffMember.setEduPersonPrimaryAffiliation("staff");
				 List<String> user_affiliations = new ArrayList<>();
				 user_affiliations.add("staff");
				 staffMember.setEduPersonAffiliation(user_affiliations);
				 //user preferences
				 UserAccess.UserPreferences userPreferences = new UserAccess.UserPreferences();
				 userPreferences.setBroadcast("broadcast");
				 userPreferences.setRecord("recording");
				 userPreferences.setAccess("open");
				 userPreferences.setPublish("private");
				 //active or not
				 staffMember.setActive(staffMember.isActive());
				 staffMember.setId(null);
				 //password
				 BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
				 String encoded_pass = bCryptPasswordEncoder.encode(staffMember.getPassword());
				 staffMember.setPassword(encoded_pass);
				_id = opUserService.create(staffMember);
				 logger.info("Created User:" + _id);
			}
			else {
				  if (staffMember.getPassword() != null && !staffMember.getPassword().equals("")) {
					  //password
					  BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
					  String encoded_pass = bCryptPasswordEncoder.encode(staffMember.getPassword());
					  staffMember.setPassword(encoded_pass);
				  }
				  opUserService.findAndUpdate(staffMember);
				 _id = staffMember.getId();
			}
			return new ResponseEntity<>(_id, HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/staff/delete/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteStaffMember(@PathVariable("id") String id) {
		try {
				opUserService.delete(id);
				return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	@RequestMapping(value = "/api/v1/staff/get/{id}", method = RequestMethod.GET)
	public OpUser getStaffMember(@PathVariable("id") String id) {
		try {
			return opUserService.findById(id);
		}
		catch(Exception e) {
			return null;
		}
	}
}
