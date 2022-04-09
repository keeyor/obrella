/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.vodapp.mvc;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.opendelos.model.common.QueryFilter;
import org.opendelos.model.common.SearchUtils;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.system.SystemMessage;
import org.opendelos.vodapp.services.async.AsyncQueryComponent;
import org.opendelos.vodapp.services.resource.ResourceService;
import org.opendelos.vodapp.services.system.SystemMessageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@Controller
public class SearchController {

	private final ResourceService resourceService;
	private final AsyncQueryComponent asyncQueryComponent;
	private final SystemMessageService systemMessageService;

	@Autowired
	public SearchController(ResourceService resourceService, AsyncQueryComponent asyncQueryComponent, SystemMessageService systemMessageService) {
		this.resourceService = resourceService;
		this.asyncQueryComponent = asyncQueryComponent;
		this.systemMessageService = systemMessageService;
	}

	@GetMapping(value = "/search")
	public String getHomePage(final Model model, HttpServletRequest request,
			@RequestParam(value = "ft", required = false, defaultValue = "") String ft,
			@RequestParam(value = "d", required = false) String d,     // Department
			@RequestParam(value = "sc", required = false) String sc,   // School
			@RequestParam(value = "rt", required = false, defaultValue = "c") String rt,   // ResourceType
			@RequestParam(value = "c", required = false) String c,     // Course
			@RequestParam(value = "e", required = false) String e,     // Event
			@RequestParam(value = "s", required = false) String s,     // Staff Member
			@RequestParam(value = "ca", required = false) String ca,   // Category
			@RequestParam(value = "p", required = false) String p,     // Period  (semester)
			@RequestParam(value = "y", required = false) String y,     // Academic Year
			@RequestParam(value = "dt", required = false) String dt,   // Date -> secret field
			@RequestParam(value = "limit", required = false, defaultValue = "50") int limit,
			@RequestParam(value = "skip", required = false, defaultValue = "0") int skip,
			@RequestParam(value = "sort", required = false, defaultValue = "date") String sort,
			@RequestParam(value = "direction", required = false, defaultValue = "desc") String direction,
			Locale locale) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();

		//Messages
		List<SystemMessage> messageList = systemMessageService.getAllByVisibleIs(true);
		model.addAttribute("messageList", messageList);

		String urlString = request.getRequestURL().toString();
		if (request.getQueryString() != null) {
			urlString += "?" + request.getQueryString();
		}
		prepareLinkReplacements(model, urlString);

		ResourceQuery resourceQuery = new ResourceQuery();
		resourceQuery.setResourceType(rt);

		if (ft != null && !ft.trim().equals("") && !ft.trim().equals("a")) {
			ft = URLDecoder.decode(ft, "UTF-8");
		}
		resourceQuery.setFt(ft);
		resourceQuery.setDepartmentId(d);
		resourceQuery.setSchoolId(sc);
		resourceQuery.setCourseId(c);
		resourceQuery.setEventId(e);
		resourceQuery.setStaffMemberId(s);
		resourceQuery.setCategoryCode(ca);
		resourceQuery.setPeriod(p);
		resourceQuery.setAcademicYear(y);
		resourceQuery.setLimit(limit);
		resourceQuery.setDate(dt);

		if (skip < 0) {
			skip = 0;
		}

		resourceQuery.setSkip(skip);
		if (sort == null) {
			if (ft == null || ft.trim().isEmpty()) {
				sort = "date";
			}
			else {
				sort = "rel";
			}
		}
		resourceQuery.setSort(sort);
		resourceQuery.setDirection(direction);
		resourceQuery.setAccessPolicy("public");

		String queryString = request.getQueryString();
		resourceQuery.setQueryString(queryString);
		model.addAttribute("queryString", queryString);

		QueryResourceResults queryResourceResults;
		if (queryString != null && !queryString.trim().equals("")) {
			queryResourceResults = resourceService.searchPageableLectures(resourceQuery);
			resourceQuery.setTotalResults(queryResourceResults.getTotalResults());
			model.addAttribute("QR", queryResourceResults);

			asyncQueryComponent.setStatus("Pending");
			asyncQueryComponent.RunQueryReport(resourceQuery, request);

			setFilterDetails(model, resourceQuery);
		}

		model.addAttribute("resourceQuery", resourceQuery);

		Calendar time_now = Calendar.getInstance();
		Date endTime = time_now.getTime();
		long diff = endTime.getTime() - startTime.getTime();
		model.addAttribute("PageLoadTime", diff);

		model.addAttribute("page", "search");
		model.addAttribute("color", "blue");

		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		return "search";
	}

	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public String SearchPost(@ModelAttribute("resourceQuery") final ResourceQuery resourceQuery,
			@RequestParam("ft") String ft, HttpServletRequest request) throws UnsupportedEncodingException {

		final String view;
		StringBuilder builder = new StringBuilder();

		if (ft != null && !ft.isEmpty() && !ft.trim().equals("a")) {
			builder.append("?ft=").append(URLEncoder.encode(ft, "UTF-8"));
		}
		else if (resourceQuery.getFt() != null && !resourceQuery.getFt().isEmpty() && !resourceQuery.getFt().trim()
				.equals("a")) {
			builder.append("?ft=").append(URLEncoder.encode(resourceQuery.getFt(), "UTF-8"));
		}
		String params = builder.toString();
		view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri()
				.replacePath(request.getContextPath() + request.getServletPath()).path(params).build().toUriString();
		return view;
	}

	private void prepareLinkReplacements(Model model, String urlString) throws UnsupportedEncodingException {

		String urlSkip = SearchUtils.prepareSearchLinksForParam(urlString, "skip", "N");
		model.addAttribute("nSkip", urlSkip);
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

}
