/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc.scheduler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.opendelos.control.api.common.ApiUtils;
import org.opendelos.control.services.async.AsyncQueryComponent;
import org.opendelos.control.services.async.QueryFilter;
import org.opendelos.control.services.resource.ResourceService;
import org.opendelos.control.services.scheduler.LiveService;
import org.opendelos.control.services.structure.ClassroomService;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@Controller
public class LiveController {

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_scheduler";
	}

	private final LiveService liveService;
	private final ResourceService resourceService;
	private final ClassroomService classroomService;
	private final AsyncQueryComponent asyncQueryComponent;

	@Autowired
	public LiveController(LiveService liveService, ResourceService resourceService, ClassroomService classroomService, AsyncQueryComponent asyncQueryComponent) {
		this.liveService = liveService;
		this.resourceService = resourceService;
		this.classroomService = classroomService;
		this.asyncQueryComponent = asyncQueryComponent;
	}

	@GetMapping(value = "admin/scheduler/live")
	public String getHomePage(final Model model,HttpServletRequest request,
		 @RequestParam(value = "d", required = false) String d) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		 OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		 model.addAttribute("user", editor);

		return "admin/scheduler/live";
	}


	@GetMapping(value = "admin/scheduler/liveLectures")
	@ResponseBody
	public byte[] getLiveLectures(HttpServletRequest request,
			@RequestParam(value = "d", required = false) String d) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();

		ResourceQuery resourceQuery = new ResourceQuery();
		resourceQuery.setCollectionName("Scheduler.Live");
		resourceQuery.setDepartmentId(d);
		resourceQuery.setSort("date");
		resourceQuery.setDirection("asc");
		resourceQuery.setResourceType("c");

		String queryString = request.getQueryString();
		resourceQuery.setQueryString(queryString);

		//> Get Live List
		QueryResourceResults liveResources = liveService.getLiveResourcesByQuery(resourceQuery);
		byte[] b1;
		b1 = ApiUtils.TransformResultsForDataTable(liveResources.getSearchResultList());

		return b1;
	}
	@GetMapping(value = "admin/scheduler/liveScheduledEvents")
	@ResponseBody
	public byte[] getLiveScheduledEvent(HttpServletRequest request,
			@RequestParam(value = "d", required = false) String d) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();

		ResourceQuery resourceQuery = new ResourceQuery();
		resourceQuery.setCollectionName("Scheduler.Live");
		resourceQuery.setDepartmentId(d);
		resourceQuery.setSort("date");
		resourceQuery.setDirection("asc");
		resourceQuery.setResourceType("e");

		String queryString = request.getQueryString();
		resourceQuery.setQueryString(queryString);

		//> Get Live List
		QueryResourceResults liveResources = liveService.getLiveResourcesByQuery(resourceQuery);
		byte[] b1;
		b1 = ApiUtils.TransformResultsForDataTable(liveResources.getSearchResultList());

		return b1;
	}

	@GetMapping(value = "admin/scheduler/liveToday")
	@ResponseBody
	public byte[] getTodayEvent(HttpServletRequest request,
			@RequestParam(value = "d", required = false) String d) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();

		ResourceQuery resourceQuery = new ResourceQuery();
		resourceQuery.setCollectionName("Scheduler.Live");
		resourceQuery.setDepartmentId(d);
		resourceQuery.setSort("date");
		resourceQuery.setDirection("asc");

		String queryString = request.getQueryString();
		resourceQuery.setQueryString(queryString);

		//> Get Live List
		QueryResourceResults todayResources = resourceService.searchPageableLectures(resourceQuery);
		classroomService.setClassroomNameToResults(todayResources);
		byte[] b1;
		b1 = ApiUtils.TransformResultsForDataTable(todayResources.getSearchResultList());

		return b1;
	}

	@RequestMapping(value = "admin/scheduler/live", method = RequestMethod.POST)
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

	private void setFilterDetails(Model model, ResourceQuery resourceQuery) throws ExecutionException, InterruptedException {

		CompletableFuture<QueryFilter> departmentFilter = new CompletableFuture<>();

		departmentFilter = asyncQueryComponent
				.findDepartmentTitleById(resourceQuery.getDepartmentId());

		//Wait for all async processes to complete
		CompletableFuture.allOf(departmentFilter).join();

		model.addAttribute("departmentFilter", departmentFilter.get());
	}



}
