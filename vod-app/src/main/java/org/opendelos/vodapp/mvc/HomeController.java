/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.vodapp.mvc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.opendelos.model.common.Select2GenGroup;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.structure.Course;
import org.opendelos.vodapp.services.i18n.OptionServices;
import org.opendelos.vodapp.services.opUser.OpUserService;
import org.opendelos.vodapp.services.resource.ResourceService;
import org.opendelos.vodapp.services.structure.CourseService;
import org.opendelos.vodapp.services.structure.DepartmentService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

	@Value("${app.live.url}")
	String app_live_url;
	@Value("${app.events.url}")
	String app_events_url;

	private final ResourceService resourceService;
	private final OptionServices optionServices;
	private final DepartmentService departmentService;
	private final OpUserService opUserService;
	private final CourseService courseService;


	public HomeController(ResourceService resourceService, OptionServices optionServices, DepartmentService departmentService, OpUserService opUserService, CourseService courseService) {
		this.resourceService = resourceService;
		this.optionServices = optionServices;
		this.departmentService = departmentService;
		this.opUserService = opUserService;
		this.courseService = courseService;
	}

	@GetMapping(value = "/")
	public String getHomePage(final Model model, Locale locale) {

		long no_lectures = resourceService.countPublicResourcesByType("COURSE");
		model.addAttribute("no_lectures", no_lectures);

		long no_courses = this.CountCoursesWithAtLeastOneResource();
		model.addAttribute("no_courses",no_courses);

		long no_staff = this.CountStaffMembersWithAtLeastOneResource();
		model.addAttribute("no_staff",no_staff);

		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		model.addAttribute("page", "home");

		model.addAttribute("app_live_url", app_live_url);
		model.addAttribute("app_events_url",app_events_url);

		return "home";
	}


	@RequestMapping(value = {"/themareas"})
	public String getThematicAreaList(final Model model, Locale locale) {

		LinkedHashMap<String, List<String>> lhm = optionServices.getSortedCategoriesIds(locale);
		model.addAttribute("categories",lhm);
		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		return "themareas";
	}

	@RequestMapping(value = {"/departments"})
	public String getDepartmentList(final Model model, Locale locale) {

		List<Select2GenGroup> departmentList = departmentService.getAllDepartmentsGroupedBySchool("dummy", locale);
		model.addAttribute("departmentList",departmentList);
		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		return "departments";
	}

	@RequestMapping(value = {"/staffmembers"})
	public String getStaffMembersList(final Model model, Locale locale) {

		List<OpUser> staffMembers = opUserService.findAllStaffMembers();
		LinkedHashMap<String, List<OpUser>> listForEachLetter = new LinkedHashMap<>();

		char c;
		for(c = 'Α'; c <= 'Ω'; ++c) {
			if (c != 930) {	// unknown character
				List<OpUser> staffList = new ArrayList<>();
				listForEachLetter.put(String.valueOf(c).toUpperCase(locale), staffList);
			}
		}

		List<OpUser> staffList_others = new ArrayList<>();
		listForEachLetter.put("A-Z 0-9", staffList_others);

		for (OpUser staff: staffMembers) {
			String name = staff.getName();
			char firstChar = name.charAt(0);

			//902 A, 904 E, 905 H, 906 I, 908 O, 910 Y, 911 Ω
			int ccode = firstChar;
			if (ccode == 902 || ccode == 904 || ccode == 905  || ccode == 906 || ccode == 908 || ccode == 910 || ccode == 911) {
				if (ccode == 902) { ccode = ccode + 11;} // 913 :'A to A
				if (ccode == 904) { ccode = ccode + 13;} // 917 : Έ το Ε
				if (ccode == 905) { ccode = ccode + 14;} // 919 Η
				if (ccode == 906) { ccode = ccode + 15;} // 921 Ι
				if (ccode == 908) { ccode = ccode + 19;} // 927 Ο
				if (ccode == 910) { ccode = ccode + 23;} // 933 Υ
				if (ccode == 911) { ccode = ccode + 26;} // 937 Ω

				firstChar = (char) ccode;
			}

			if ((int) firstChar > 912 && (int) firstChar < 938 && firstChar != 930) {
				if (listForEachLetter.containsKey(String.valueOf(firstChar).toUpperCase(locale))) {
					List<OpUser> staffList = listForEachLetter.get(String.valueOf(firstChar));
					// Do not use PublicCounters for now! until you find a clever way to update the counters
					 if (staff.getResourcePublicCounter() > 0) {
						staffList.add(staff);
					 }
				}
				else {
					System.out.println("Unknown Character " + firstChar + " -> This should not happen!");
				}
			}
			else {
				if (listForEachLetter.containsKey("A-Z 0-9")) {
					// DEBUG: System.out.println(" Unknown:" + firstChar + " of " + name);
					List<OpUser> staffList = listForEachLetter.get("A-Z 0-9");
					// Do not use PublicCounters for now! until you find a clever way to update the counters
					 if (staff.getResourcePublicCounter() > 0) {
						staffList.add(staff);
					 }
				}
				else {
					System.out.println("Unknown Character " + firstChar + " -> This should not happen!");
				}
			}
		}

		//# sort each individual list
		for (Map.Entry<String, List<OpUser>> mapElement : listForEachLetter.entrySet()) {
			// Adding some bonus marks to all the students
			List<OpUser> staffList=  mapElement.getValue();
			Comparator<OpUser> nameSorter  = Comparator.comparing(OpUser::getName);
			staffList.sort(nameSorter);
		}

		model.addAttribute("staffMembersByLetter",listForEachLetter);

		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		return "staffmembers";
	}

	@RequestMapping(value = {"/courses"})
	public String getCourseList(final Model model, Locale locale) {

		List<Course> courses = courseService.findAll();
		LinkedHashMap<String, List<Course>> listForEachLetter = new LinkedHashMap<>();

		char c;
		for(c = 'Α'; c <= 'Ω'; ++c) {
			if (c != 930) {	// unknown character
				List<Course> courseList = new ArrayList<>();
				listForEachLetter.put(String.valueOf(c).toUpperCase(locale), courseList);
			}
		}

		for(c = 'A'; c <= 'Z'; ++c) {
			List<Course> courseList = new ArrayList<>();
			listForEachLetter.put(String.valueOf(c).toUpperCase(locale), courseList);
		}

		List<Course> courseList_others = new ArrayList<>();
		listForEachLetter.put("0-9 ?", courseList_others);

		for (Course course: courses) {
			String title = course.getTitle();
			char firstChar = title.toUpperCase().charAt(0);

			//902 A, 904 E, 905 H, 906 I, 908 O, 910 Y, 911 Ω
			int ccode = firstChar;
			if (ccode == 902 || ccode == 904 || ccode == 905  || ccode == 906 || ccode == 908 || ccode == 910 || ccode == 911) {
				if (ccode == 902) { ccode = ccode + 11;} // 913 :'A to A
				if (ccode == 904) { ccode = ccode + 13;} // 917 : Έ το Ε
				if (ccode == 905) { ccode = ccode + 14;} // 919 Η
				if (ccode == 906) { ccode = ccode + 15;} // 921 Ι
				if (ccode == 908) { ccode = ccode + 19;} // 927 Ο
				if (ccode == 910) { ccode = ccode + 23;} // 933 Υ
				if (ccode == 911) { ccode = ccode + 26;} // 937 Ω

				firstChar = (char) ccode;
			}

			if ((int) firstChar > 912 && (int) firstChar < 938 && firstChar != 930) {
				if (listForEachLetter.containsKey(String.valueOf(firstChar).toUpperCase(locale))) {
					List<Course> courseList = listForEachLetter.get(String.valueOf(firstChar));
					// Do not use PublicCounters for now! until you find a clever way to update the counters
					 if (course.getResourcePublicCounter()> 0) {
						courseList.add(course);
					 }
				}
				else {
					System.out.println("Unknown Character " + firstChar + " -> This should not happen!");
				}
			}
			else if ((int) firstChar > 64 && (int) firstChar < 91) {
				if (listForEachLetter.containsKey(String.valueOf(firstChar).toUpperCase(locale))) {
					List<Course> courseList = listForEachLetter.get(String.valueOf(firstChar));
					// Do not use PublicCounters for now! until you find a clever way to update the counters
					 if (course.getResourcePublicCounter()> 0) {
						courseList.add(course);
					 }
				}
				else {
					System.out.println("Unknown Character " + firstChar + " -> This should not happen!");
				}
			}
			else {
				if (listForEachLetter.containsKey("0-9 ?")) {
					// DEBUG: System.out.println(" Unknown:" + firstChar + " of " + name);
					List<Course> courseList = listForEachLetter.get("0-9 ?");
					// Do not use PublicCounters for now! until you find a clever way to update the counters
					 if (course.getResourcePublicCounter()> 0) {
						courseList.add(course);
					 }
				}
				else {
					System.out.println("Unknown Character " + firstChar + " -> This should not happen!");
				}
			}
		}

		//# sort each individual list
		for (Map.Entry<String, List<Course>> mapElement : listForEachLetter.entrySet()) {
			// Adding some bonus marks to all the students
			List<Course> courseList=  mapElement.getValue();
			Comparator<Course> titleSorter  = Comparator.comparing(Course::getTitle);
			courseList.sort(titleSorter);
		}

		model.addAttribute("coursesByLetter",listForEachLetter);

		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		return "courses";
	}

	// Do not use PublicCounters for now! until you find a clever way to update the counters
 	private long CountCoursesWithAtLeastOneResource() {

		List<Course> courses = courseService.findAll();
		List<Course> removeList = new ArrayList<>();
		for (Course course: courses) {
			if (course.getResourcePublicCounter() == 0) {
				removeList.add(course);
			}
		}
		courses.removeAll(removeList);
		return courses.size();
	}

	private long CountAllCourses() {

		List<Course> courses = courseService.findAll();
		return courses.size();
	}

	// Do not use PublicCounters for now! until you find a clever way to update the counters
 	private long CountStaffMembersWithAtLeastOneResource() {

		List<OpUser> staffMembers = opUserService.findAllStaffMembers();
		List<OpUser> removeList = new ArrayList<>();
		for (OpUser opUser: staffMembers) {
			if (opUser.getResourcePublicCounter() == 0) {
				removeList.add(opUser);
			}
		}
		staffMembers.removeAll(removeList);
		return staffMembers.size();
	}


	private long CountAllStaffMembers() {

		List<OpUser> staffMembers = opUserService.findAllStaffMembers();
		return staffMembers.size();
	}
}
