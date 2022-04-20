/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc.content;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.opendelos.control.services.async.AsyncQueryComponent;
import org.opendelos.control.services.async.QueryFilter;
import org.opendelos.control.services.resource.ResourceService;
import org.opendelos.control.services.structure.InstitutionService;
import org.opendelos.model.common.SearchUtils;
import org.opendelos.model.common.Select2GenChild;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.users.OoUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@Controller
public class AdminSearchController {

	private final Logger logger = LoggerFactory.getLogger(AdminSearchController.class);

	private final InstitutionService institutionService;
	private final ResourceService resourceService;
	private final AsyncQueryComponent asyncQueryComponent;

	@Autowired
	String currentAcademicYear;
	@Autowired
	Institution defaultInstitution;

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_content";
	}

	@Autowired
	public AdminSearchController(InstitutionService institutionService, ResourceService resourceService, AsyncQueryComponent asyncQueryComponent) {
		this.institutionService = institutionService;
		this.resourceService = resourceService;
		this.asyncQueryComponent = asyncQueryComponent;
	}

	@GetMapping(value = { "admin/search", "admin/portfolio"})
	@Async
	public String getHomePage(final Model model,HttpServletRequest request,
			@RequestParam(value = "ft", required = false, defaultValue = "") String ft,
			@RequestParam(value = "d", required = false) String d,     // Department
			@RequestParam(value = "rt", required = false) String rt,   // ResourceType
			@RequestParam(value = "c", required = false) String c,     // Course
			@RequestParam(value = "e", required = false) String e,     // Event
			@RequestParam(value = "s", required = false) String s,     // Staff Member
			@RequestParam(value = "p", required = false) String p,     // Period  (semester)
			@RequestParam(value = "y", required = false) String y,     // Academic Year
			@RequestParam(value = "ap", required = false) String ap,   // AccessPolicy
			@RequestParam(value = "t", required = false) String t,     // Tag
			@RequestParam(value = "dt", required = false) String dt,   // Date -> secret field
			@RequestParam(value = "limit", required = false, defaultValue = "50") int limit,
			@RequestParam(value = "skip", required = false, defaultValue = "0") int skip,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "direction", required = false, defaultValue = "desc") String direction) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();

		String c_page = "search";
		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user", editor);

		addUserAccessAttributes(model,editor);

		ResourceQuery resourceQuery = new ResourceQuery();
		if (rt != null && (rt.equals("c") || rt.equals("e"))) {	//reject all other values
			resourceQuery.setResourceType(rt);
		}
 		if (ft != null && !ft.trim().equals("") && !ft.trim().equals("a")) {
			ft =  URLDecoder.decode(ft, "UTF-8");
		}
 		if (y == null || y.equals("")) {
 			resourceQuery.setAcademicYear(currentAcademicYear);
		}
 		else {
 			resourceQuery.setAcademicYear(y);
		}
		resourceQuery.setDepartmentId(d);
		resourceQuery.setFt(ft);
		resourceQuery.setStaffMemberId(s);
		resourceQuery.setCourseId(c);
		resourceQuery.setEventId(e);
		resourceQuery.setPeriod(p);
		resourceQuery.setTag(t);
		resourceQuery.setLimit(limit);
		resourceQuery.setDate(dt);

		if (skip < 0) { skip = 0; }

		resourceQuery.setSkip(skip);
		if (sort == null) {
			if (ft == null || ft.trim().isEmpty()) { sort = "date"; }
			else { sort = "rel";}
		}

		resourceQuery.setSort(sort);
		resourceQuery.setDirection(direction);
		if (ap != null) {
			resourceQuery.setAccessPolicy(ap);
		}

		String queryString = request.getQueryString();
		resourceQuery.setQueryString(queryString);
		model.addAttribute("queryString",queryString );

		QueryResourceResults queryResourceResults;
		//if (queryString != null && !queryString.trim().equals("")) {
			resourceQuery = resourceService.setAccessRestrictions(resourceQuery,editor);
			queryResourceResults = resourceService.searchPageableLectures(resourceQuery);
			resourceQuery.setTotalResults(queryResourceResults.getTotalResults());
			model.addAttribute("QR", queryResourceResults);

			asyncQueryComponent.setStatus("Pending");
			ResourceQuery filtersResourceQuery = new ResourceQuery();
			BeanUtils.copyProperties(resourceQuery,filtersResourceQuery);
			filtersResourceQuery.setLimit(-1);
			CompletableFuture<String> completableFuture
					= CompletableFuture.supplyAsync(() -> {
				try {
					asyncQueryComponent.RunQueryReport(filtersResourceQuery, request);
				}
				catch (ExecutionException | InterruptedException ex) {
					ex.printStackTrace();
				}
				return null;
			});

			Calendar time_after = Calendar.getInstance();
			Date endTime_after = time_after.getTime();
			long diff1 = endTime_after.getTime()- startTime.getTime();

			setFilterDetails(model,resourceQuery);
		//}
		model.addAttribute("resourceQuery", resourceQuery);

		//Get User's History by default
		if (d == null && c == null && e == null && s == null) {
			ResourceQuery historyQuery = new ResourceQuery();
			historyQuery.setEditorId(editor.getId());
			historyQuery.setAccessPolicy(null);
			historyQuery.setSort("dateModified");
			historyQuery.setDirection("desc");
			historyQuery.setLimit(10);
			historyQuery.setSkip(0);
			QueryResourceResults historyResourceResults = resourceService.searchPageableLectures(historyQuery);
			model.addAttribute("HR", historyResourceResults);
		}

		String urlString   = request.getRequestURL().toString();
		if (request.getQueryString() != null) {
			urlString += "?" + request.getQueryString();
			request.getSession().setAttribute("user_search_history", c_page + "?" + request.getQueryString());
		}
		else {
			request.getSession().setAttribute("user_search_history", c_page);
		}
		prepareLinkReplacements(model,urlString);

		model.addAttribute("cpage", c_page);

		Calendar time_now = Calendar.getInstance();
		Date endTime = time_now.getTime();
		long diff = endTime.getTime() - startTime.getTime();
		model.addAttribute("PageLoadTime", diff);

		model.addAttribute("landing_page", "search");
		model.addAttribute("color", "blue");

		return "admin/content/search/admin-search";
	}

	@RequestMapping(value = { "admin/search", "admin/portfolio"}, method = RequestMethod.POST)
	public String SearchPost(@ModelAttribute("resourceQuery") final ResourceQuery resourceQuery,
							 @RequestParam("ft") String ft, HttpServletRequest request) throws UnsupportedEncodingException {


		final String view;
		StringBuilder builder = new StringBuilder();

		if (ft != null && !ft.isEmpty() && !ft.trim().equals("a")) {
			builder.append("?ft=").append(URLEncoder.encode(ft, "UTF-8"));
		}
		else if (resourceQuery.getFt() != null  && !resourceQuery.getFt().isEmpty() && !resourceQuery.getFt().trim().equals("a"))  {
			builder.append("?ft=").append(URLEncoder.encode(resourceQuery.getFt(), "UTF-8"));
		}
		String params = builder.toString();
		view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +  request.getServletPath()).path(params).build().toUriString();

		return view;
	}

	private void prepareLinkReplacements (Model model, String urlString) throws UnsupportedEncodingException {

		String urlSkip= SearchUtils.prepareSearchLinksForParam(urlString,"skip","N");
		model.addAttribute("nSkip",urlSkip);
	}

	private void addUserAccessAttributes(Model model, OoUserDetails editor) {

		String user_access="";
		boolean user_isStaffMember = false;
		if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SA"))) {
			user_access = "SA";
		}
		else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
			user_access = "MANAGER";
		}
		else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPPORT"))) {
			user_access = "SUPPORT";
		}
		if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFFMEMBER"))) {
			user_isStaffMember = true;
		}
		model.addAttribute("user_access", user_access);
		model.addAttribute("user_isStaffMember",user_isStaffMember);
	}

	private void setFilterDetails(Model model, ResourceQuery resourceQuery) throws ExecutionException, InterruptedException {

		CompletableFuture<QueryFilter> departmentFilter = new CompletableFuture<>();
		CompletableFuture<QueryFilter> courseFilter = new CompletableFuture<>();
		CompletableFuture<QueryFilter> staffMemberFilter = new CompletableFuture<>();
		CompletableFuture<QueryFilter> scheduledEventFilter = new CompletableFuture<>();

		departmentFilter = asyncQueryComponent
					.findDepartmentTitleById(resourceQuery.getDepartmentId());

		courseFilter = asyncQueryComponent
					.findCourseTitleById(resourceQuery.getCourseId());

		staffMemberFilter = asyncQueryComponent
					.findStaffMemberNameById(resourceQuery.getStaffMemberId());

		scheduledEventFilter = asyncQueryComponent
					.findScheduledEventNameById(resourceQuery.getEventId());

		//Wait for all async processes to complete
		CompletableFuture.allOf(departmentFilter, courseFilter, staffMemberFilter, scheduledEventFilter).join();

		model.addAttribute("departmentFilter", departmentFilter.get());
		model.addAttribute("courseFilter", courseFilter.get());
		model.addAttribute("staffMemberFilter", staffMemberFilter.get());
		model.addAttribute("scheduledEventFilter", scheduledEventFilter.get());
		model.addAttribute("tag",resourceQuery.getTag());
		model.addAttribute("resource_type", resourceQuery.getResourceType());

		String selected_academic_year = "";
		if (resourceQuery.getAcademicYear() != null && !resourceQuery.getAcademicYear().equals("")) {
			selected_academic_year = resourceQuery.getAcademicYear();
		}
		model.addAttribute("academicYear",selected_academic_year);

		//AcademicYears && Current Academic Year
		List<String> ayList = institutionService.getAvailableAcademicCalendarYears(defaultInstitution.getId());
 		List<Select2GenChild> aYearList= new ArrayList<>();
		for (String at: ayList) {
			int next_ay = Integer.parseInt(at) + 1;
			Select2GenChild select2GenChild = new Select2GenChild();
			select2GenChild.setId(at);
			select2GenChild.setText("Ακαδημαϊκό Έτος " + at + " - " + next_ay);
			aYearList.add(select2GenChild);
		}
		Comparator<Select2GenChild> idSorter  = Comparator.comparing(Select2GenChild::getId);
		aYearList.sort(idSorter);
		model.addAttribute("ayList", aYearList);
	}

}
