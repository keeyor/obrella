/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc.admin.scheduler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.opendelos.control.services.async.AsyncQueryComponent;
import org.opendelos.control.services.async.QueryFilter;
import org.opendelos.control.services.resource.ResourceService;
import org.opendelos.control.services.structure.ClassroomService;
import org.opendelos.model.common.SearchUtils;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.structure.Classroom;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
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
public class DaySearchController {

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_scheduler";
	}

	private final ResourceService resourceService;
	private final ClassroomService classroomService;
	private final AsyncQueryComponent asyncQueryComponent;

	@Autowired
	public DaySearchController(ResourceService resourceService, ClassroomService classroomService, AsyncQueryComponent asyncQueryComponent) {
		this.resourceService = resourceService;
		this.classroomService = classroomService;
		this.asyncQueryComponent = asyncQueryComponent;
	}

	@GetMapping(value = "admin/scheduler/daily")
	public String getHomePage(final Model model,HttpServletRequest request,
			@RequestParam(value = "ft", required = false, defaultValue = "") String ft,
			@RequestParam(value = "d", required = false) String d,     // Department
			@RequestParam(value = "sc", required = false) String sc,   // School
			@RequestParam(value = "rt", required = false) String rt,   // ResourceType
			@RequestParam(value = "c", required = false) String c,     // Course
			@RequestParam(value = "e", required = false) String e,     // Event
			@RequestParam(value = "s", required = false) String s,     // Staff Member
			@RequestParam(value = "ca", required = false) String ca,   // Category
			@RequestParam(value = "p", required = false) String p,     // Period  (semester)
			@RequestParam(value = "y", required = false) String y,     // Academic Year
			@RequestParam(value = "ap", required = false) String ap,   // AccessPolicy
			@RequestParam(value = "t", required = false) String t,     // Tag
			@RequestParam(value = "limit", required = false, defaultValue = "50") int limit,
			@RequestParam(value = "skip", required = false, defaultValue = "0") int skip,
			@RequestParam(value = "sort", required = false, defaultValue = "date") String sort,
			@RequestParam(value = "direction", required = false, defaultValue = "asc") String direction) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user", editor);

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();

		String urlString   = request.getRequestURL().toString();
		if (request.getQueryString() != null) {
			urlString += "?" + request.getQueryString();
			request.getSession().setAttribute("user_search_history","?" + request.getQueryString());
		}
		prepareLinkReplacements(model,urlString);

		ResourceQuery resourceQuery = new ResourceQuery();
		resourceQuery.setCollectionName("Scheduler.Live");

		if (rt != null && (rt.equals("c") || rt.equals("e"))) {	//reject all other values
			resourceQuery.setResourceType(rt);
		}

 		if (ft != null && !ft.trim().equals("") && !ft.trim().equals("a")) {
			ft =  URLDecoder.decode(ft, "UTF-8");
		}
		resourceQuery.setFt(ft);
 		if (d!= null) {
			resourceQuery.setDepartmentId(d);
		}
		resourceQuery.setSchoolId(sc);
		resourceQuery.setCourseId(c);
		resourceQuery.setEventId(e);
		resourceQuery.setStaffMemberId(s);
		resourceQuery.setCategoryCode(ca);
		resourceQuery.setPeriod(p);
		resourceQuery.setAcademicYear(y);
		resourceQuery.setTag(t);
		resourceQuery.setLimit(limit);

		if (skip < 0) { skip = 0; }

		resourceQuery.setSkip(skip);
		if (sort == null) {
			if (ft == null || ft.trim().isEmpty()) { sort = "date"; }
			else { sort = "rel"; }
		}
		resourceQuery.setSort(sort);
		resourceQuery.setDirection(direction);
		if (ap != null) {
			resourceQuery.setAccessPolicy(ap);
		}

		String queryString = request.getQueryString();
		resourceQuery.setQueryString(queryString);
		model.addAttribute("queryString",queryString );

		//> Get Today's Schedule
			QueryResourceResults queryResourceResults;
			queryResourceResults = resourceService.searchPageableLectures(resourceQuery);
			resourceQuery.setTotalResults(queryResourceResults.getSearchResultList().size());
			this.setClassroomNameToResults(queryResourceResults);
			model.addAttribute("QR", queryResourceResults);

			asyncQueryComponent.setStatus("Pending");
			asyncQueryComponent.RunQueryReport(resourceQuery, request);

			setFilterDetails(model,resourceQuery);
			model.addAttribute("resourceQuery", resourceQuery);
		//< Get Today's Schedule

		model.addAttribute("landing_page", "daily-admin");
		model.addAttribute("color", "green");
		Calendar time_now = Calendar.getInstance();
		Date endTime = time_now.getTime();
		long diff = endTime.getTime() - startTime.getTime();
		model.addAttribute("PageLoadTime", diff);

		return "admin/scheduler/daily";
	}

	@RequestMapping(value = "admin/scheduler/day", method = RequestMethod.POST)
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

	}


	private QueryResourceResults getLiveResourceListFromTodaysSchedule(List<Resource> todaysResourceList) {

		Instant _now =  Instant.now();
		List<Resource> liveResourceList = new ArrayList<>();
		for (Resource todayResource: todaysResourceList) {
			Instant startDateTime = todayResource.getDate();
			int broadcast_hour = Integer.parseInt(todayResource.getRealDuration().substring(0,2));
			int broadcast_min = Integer.parseInt(todayResource.getRealDuration().substring(3,5));
			Instant endDateTime	  = startDateTime.plus(broadcast_hour, ChronoUnit.HOURS).plus(broadcast_min, ChronoUnit.MINUTES);
			if (startDateTime.isBefore(_now) && endDateTime.isAfter(_now)) {
				liveResourceList.add(todayResource);
			}
		}
		QueryResourceResults liveResources = new QueryResourceResults();
		liveResources.setSearchResultList(liveResourceList);
		liveResources.setLimit(100);
		liveResources.setSkip(0);
		liveResources.setSort("date");
		liveResources.setDirection("asc");

		return  liveResources;
	}

	private void setClassroomNameToResults(QueryResourceResults queryResourceResults) {

		for (Resource resource: queryResourceResults.getSearchResultList()) {
			String classroomId = resource.getClassroom();
			Classroom classroom = classroomService.findById(classroomId);
			if (classroom != null && classroom.getName() != null) {
				resource.setClassroomName(classroom.getName());
			}
			else {
				resource.setClassroomName("not found");
			}

		}
	}
}
