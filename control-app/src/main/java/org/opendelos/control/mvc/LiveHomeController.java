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
	private final ClassroomService classroomService;

	@Autowired
	public LiveHomeController(LiveService liveService, ResourceService resourceService, ClassroomService classroomService) {
		this.liveService = liveService;
		this.resourceService = resourceService;
		this.classroomService = classroomService;
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
}
