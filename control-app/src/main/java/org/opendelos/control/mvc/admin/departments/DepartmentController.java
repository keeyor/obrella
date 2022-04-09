/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc.admin.departments;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.opendelos.control.services.i18n.MultilingualServices;
import org.opendelos.control.services.i18n.OptionServices;
import org.opendelos.control.services.structure.DepartmentService;
import org.opendelos.control.services.structure.SchoolService;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.structure.School;
import org.opendelos.control.conf.LmsProperties;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DepartmentController {

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_department";
	}

	@Value("${default.institution.identity}")
	String institution_identity;

	@Autowired
	Institution defaultInstitution;

	@ModelAttribute("mInstitution")
	private Institution getInstitution()  {
		return  defaultInstitution;
	}

	private final DepartmentService departmentService;
	private final SchoolService schoolService;
	private final OptionServices optionServices;
	private final LmsProperties lmsProperties;
	private final MultilingualServices multilingualServices;

	@Autowired
	public DepartmentController(DepartmentService departmentService, SchoolService schoolService, OptionServices optionServices, LmsProperties lmsProperties, MultilingualServices multilingualServices) {
		this.departmentService = departmentService;
		this.schoolService = schoolService;
		this.optionServices = optionServices;
		this.lmsProperties = lmsProperties;
		this.multilingualServices = multilingualServices;
	}

	@GetMapping(value = {"admin/department/courses/", "admin/department/courses"})
	public String getDepartmentCourses(final Model model, HttpServletRequest request, Locale locale,@RequestParam(value = "id",  required = false) String id) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		HttpSession session = request.getSession();
		//Department
		Department department = null;
		School school = null;
		if (id != null && !id.equals("")) {
			department = departmentService.findById(id);
			school = schoolService.findById(department.getSchoolId());
			session.setAttribute("user_dp", id);
		}
		else {
				String user_dp = (String) session.getAttribute("user_dp");
				if (user_dp != null) {
					department = departmentService.findById(user_dp);
					if (department != null) {
						school = schoolService.findById(department.getSchoolId());
					}
				}
		}
		model.addAttribute("department", department);
		model.addAttribute("school", school);
		model.addAttribute("institution_identity",institution_identity);
		//Categories
		HashMap<String, List<String>> catList = optionServices.getSortedCategories(locale);
		model.addAttribute("catList", catList);
		//Licenses
		String[] licenseList = optionServices.getLicenses(locale);
		model.addAttribute("licenseList", licenseList);

		//LMSs'
		model.addAttribute("lms", lmsProperties);
		model.addAttribute("page", "course");

		return "admin/departments/courses";
	}
	@GetMapping(value = {"admin/department/staff/", "admin/department/staff"})
	public String getDepartmentStaff(final Model model, HttpServletRequest request, Locale locale,@RequestParam(value = "id",  required = false) String id) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		HttpSession session = request.getSession();
		//Department
		Department department = null;
		School school = null;
		if (id != null && !id.equals("")) {
			department = departmentService.findById(id);
			school = schoolService.findById(department.getSchoolId());
			session.setAttribute("user_dp", id);
		}
		else {
			String user_dp = (String) session.getAttribute("user_dp");
			if (user_dp != null) {
				department = departmentService.findById(user_dp);
				school = schoolService.findById(department.getSchoolId());
			}
		}
		model.addAttribute("department", department);
		model.addAttribute("school", school);
		model.addAttribute("institution_identity",institution_identity);

		//Licenses
		String[] licenseList = optionServices.getLicenses(locale);
		model.addAttribute("licenseList", licenseList);

		//LMSs'
		model.addAttribute("lms", lmsProperties);
		model.addAttribute("page", "staff");

		return "admin/departments/staff";
	}
	@GetMapping(value = {"admin/department/programs/", "admin/department/programs"})
	public String getDepartmentPrograms(final Model model, HttpServletRequest request, Locale locale,@RequestParam(value = "id",  required = false) String id) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		HttpSession session = request.getSession();
		//Department
		Department department = null;
		School school = null;
		if (id != null && !id.equals("")) {
			department = departmentService.findById(id);
			school = schoolService.findById(department.getSchoolId());
			session.setAttribute("user_dp", id);
		}
		else {
			String user_dp = (String) session.getAttribute("user_dp");
			if (user_dp != null) {
				department = departmentService.findById(user_dp);
				school = schoolService.findById(department.getSchoolId());
			}
		}
		model.addAttribute("department", department);
		model.addAttribute("school", school);
		model.addAttribute("institution_identity",institution_identity);
		//Licenses
		String[] licenseList = optionServices.getLicenses(locale);
		model.addAttribute("licenseList", licenseList);

		//LMSs'
		model.addAttribute("lms", lmsProperties);
		model.addAttribute("page", "program");

		return "admin/departments/programs";
	}
	@GetMapping(value = {"admin/department/classrooms/", "admin/department/classrooms"})
	public String getDepartmentClassrooms(final Model model, HttpServletRequest request, Locale locale,@RequestParam(value = "id",  required = false) String id) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		HttpSession session = request.getSession();
		//Department
		Department department = null;
		School school = null;
		if (id != null && !id.equals("")) {
			department = departmentService.findById(id);
			school = schoolService.findById(department.getSchoolId());
			session.setAttribute("user_dp", id);
		}
		else {
			String user_dp = (String) session.getAttribute("user_dp");
			if (user_dp != null) {
				department = departmentService.findById(user_dp);
				school = schoolService.findById(department.getSchoolId());
			}
		}
		model.addAttribute("department", department);
		model.addAttribute("school", school);
		model.addAttribute("institution_identity",institution_identity);
		//Licenses
		String[] licenseList = optionServices.getLicenses(locale);
		model.addAttribute("licenseList", licenseList);

		//LMSs'
		model.addAttribute("lms", lmsProperties);
		model.addAttribute("page", "classroom");

		return "admin/departments/classrooms";
	}

	@RequestMapping(value = "admin/department/acalendar",   method = RequestMethod.GET)
	public String getCurrentPeriod(final Model model, HttpServletRequest request, Locale locale,@RequestParam(value = "id",  required = false) String id) throws Exception {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		HttpSession session = request.getSession();
		//Department
		Department department = null;
		School school = null;
		if (id != null && !id.equals("")) {
			department = departmentService.findById(id);
			school = schoolService.findById(department.getSchoolId());
			session.setAttribute("user_dp", id);
		}
		else {
			String user_dp = (String) session.getAttribute("user_dp");
			if (user_dp != null) {
				department = departmentService.findById(user_dp);
				school = schoolService.findById(department.getSchoolId());
			}
		}
		model.addAttribute("department", department);
		model.addAttribute("school", school);
		model.addAttribute("institution_identity",institution_identity);
		//Licenses
		String[] licenseList = optionServices.getLicenses(locale);
		model.addAttribute("licenseList", licenseList);

		//LMSs'
		model.addAttribute("lms", lmsProperties);
		model.addAttribute("page", "calendar");

		model.addAttribute("institutionName", multilingualServices.getValue("default.institution.title",locale));

		//List<School> schools =  schoolService.findAllSortedByTitle();
		//model.addAttribute("schools",schools);

		return "admin/departments/academicCalendar";
	}


}
