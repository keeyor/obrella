/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc;

import java.io.UnsupportedEncodingException;
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
import org.opendelos.model.common.SearchUtils;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class LiveHomeController {

	private final LiveService liveService;
	private final ResourceService resourceService;
	private final AsyncQueryComponent asyncQueryComponent;
	private final ClassroomService classroomService;

	@Autowired
	public LiveHomeController(LiveService liveService, ResourceService resourceService, AsyncQueryComponent asyncQueryComponent, ClassroomService classroomService) {
		this.liveService = liveService;
		this.resourceService = resourceService;
		this.asyncQueryComponent = asyncQueryComponent;
		this.classroomService = classroomService;
	}

	@GetMapping(value = "/live")
	public String getLivePage(final Model model, HttpServletRequest request,
			@RequestParam(value = "d", required = false) String d,     // Department
			@RequestParam(value = "rt", required = false) String rt,   // ResourceType
			@RequestParam(value = "c", required = false) String c,     // Course
			@RequestParam(value = "e", required = false) String e,     // Event
			@RequestParam(value = "s", required = false) String s     // Staff Member
	) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();

		/*//Messages
		List<SystemMessage> messageList = systemMessageService.getAllByVisibleIs(true);
		model.addAttribute("messageList", messageList);

		String urlString   = request.getRequestURL().toString();
		if (request.getQueryString() != null) {
			urlString += "?" + request.getQueryString();
		}
		prepareLinkReplacements(model,urlString);

		ResourceQuery resourceQuery = new ResourceQuery();
		resourceQuery.setCollectionName("Scheduler.Live");
		resourceQuery.setResourceType(rt);
		resourceQuery.setDepartmentId(d);
		resourceQuery.setCourseId(c);
		resourceQuery.setEventId(e);
		resourceQuery.setStaffMemberId(s);
		resourceQuery.setSort("date");
		resourceQuery.setDirection("asc");

		String queryString = request.getQueryString();
		resourceQuery.setQueryString(queryString);
		model.addAttribute("queryString",queryString );

		//> Get Live List
		QueryResourceResults liveResources = liveService.getLiveResourcesByQuery(resourceQuery);
		resourceQuery.setTotalResults(liveResources.getTotalResults());
		model.addAttribute("QR", liveResources);

		asyncQueryComponent.setStatus("Pending");
		asyncQueryComponent.RunLiveQueryReport(resourceQuery, request);

		setFilterDetails(model,resourceQuery);
		//< Get Today's Schedule

		model.addAttribute("resourceQuery", resourceQuery);*/
		model.addAttribute("landing_page", "live");
		model.addAttribute("color", "red");

		Calendar time_now = Calendar.getInstance();
		Date endTime = time_now.getTime();
		long diff = endTime.getTime() - startTime.getTime();
		model.addAttribute("PageLoadTime", diff);

		return "live";
	}

	@GetMapping(value = "liveLectures")
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
	@GetMapping(value = "liveScheduledEvents")
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
		//resourceQuery.setBroadcastToChannel(false);

		String queryString = request.getQueryString();
		resourceQuery.setQueryString(queryString);

		//> Get Live List
		QueryResourceResults liveResources = liveService.getLiveResourcesByQuery(resourceQuery);
		byte[] b1;
		b1 = ApiUtils.TransformResultsForDataTable(liveResources.getSearchResultList());

		return b1;
	}

	@GetMapping(value = "liveToday")
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

}
