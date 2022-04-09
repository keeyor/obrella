/* 
     Author: Michael Gatzonis - 17/11/2020 
     live
*/
package org.opendelos.control.services.async;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.poi.ss.formula.functions.T;
import org.opendelos.control.services.i18n.MultilingualServices;
import org.opendelos.control.services.opUser.OpUserService;
import org.opendelos.model.common.AdminFilterResults;
import org.opendelos.model.common.Tag;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.resources.AccessPolicy;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.resources.dtos.AreaDto;
import org.opendelos.model.resources.dtos.TypeDto;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Department;
import org.opendelos.control.services.resource.ResourceService;
import org.opendelos.control.services.scheduledEvent.ScheduledEventService;
import org.opendelos.control.services.scheduler.ScheduleService;
import org.opendelos.control.services.structure.CourseService;
import org.opendelos.control.services.structure.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class AsyncQueryComponent{

	@Value("${app.language}")
	String language_tag;

	private final Logger logger = LoggerFactory.getLogger(AsyncQueryComponent.class);

	private String status;
	private long time_elapsed;
	private AsyncQueryReport asyncQueryReport;

	private final MultilingualServices multilingualServices;
	private final ResourceService resourceService;
	private final ScheduleService scheduleService;

	private final DepartmentService departmentService;
	private final CourseService courseService;
	private final ScheduledEventService scheduledEventService;
	private final OpUserService opUserService;

	@Autowired
	public AsyncQueryComponent(MultilingualServices multilingualServices, ResourceService resourceService, ScheduleService scheduleService, DepartmentService departmentService, CourseService courseService, ScheduledEventService scheduledEventService, OpUserService opUserService) {
		this.multilingualServices = multilingualServices;
		this.resourceService = resourceService;
		this.scheduleService = scheduleService;
		this.departmentService = departmentService;
		this.courseService = courseService;
		this.scheduledEventService = scheduledEventService;
		this.opUserService = opUserService;
	}

	@Async("threadPoolQueryWatcher")
	public void RunQueryReport(ResourceQuery resourceQuery, HttpServletRequest request) throws ExecutionException, InterruptedException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();
		status = "Running";
		boolean recalc = true;
		if (resourceQuery.getQueryString() != null &&
				(
						(resourceQuery.getQueryString().contains("skip") ||
								resourceQuery.getQueryString().contains("sort") ||
								resourceQuery.getQueryString().contains("asc")  ||
								resourceQuery.getQueryString().contains("desc")
						)
				)
		) { recalc = false;}

		HttpSession httpSession= request.getSession(true);
		if (!recalc) {
			status = "Finished";
			logger.info("get Lectures report from session");
			asyncQueryReport = (AsyncQueryReport) httpSession.getAttribute("asyncLecturesQueryReport");
			Calendar time_now = Calendar.getInstance();
			Date endTime = time_now.getTime();
			time_elapsed = endTime.getTime() - startTime.getTime();
		}
		else {
			logger.info("re-calculate Lectures report from scratch");
			status = "Running";
			Future<AsyncQueryReport> asyncQueryReportFuture = this.SearchAsyncLectures(resourceQuery);

			while (true) {
				if (asyncQueryReportFuture.isDone()) {
					status = "Finished";
					asyncQueryReport = asyncQueryReportFuture.get();
					httpSession.setAttribute("asyncLecturesQueryReport", asyncQueryReportFuture.get());
					Calendar time_now = Calendar.getInstance();
					Date endTime = time_now.getTime();
					time_elapsed = endTime.getTime() - startTime.getTime();
					break;
				}
				status = "Running";
			}
		}
	}
	@Async("threadPoolQueryWatcher")
	public void RunLiveQueryReport(ResourceQuery resourceQuery, HttpServletRequest request) throws ExecutionException, InterruptedException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();
		status = "Running";
		boolean recalc = true;

		logger.trace("re-calculate report from scratch");
		Future<AsyncQueryReport> asyncQueryReportFuture = this.SearchLiveAsyncLectures(resourceQuery);
		while (true) {
				if (asyncQueryReportFuture.isDone()) {
					status = "Finished";
					asyncQueryReport = asyncQueryReportFuture.get();
					Calendar time_now = Calendar.getInstance();
					Date endTime = time_now.getTime();
					time_elapsed = endTime.getTime() - startTime.getTime();
					break;
				}
				status = "Running";
		}
	}

	@Async("threadPoolQueryWatcher")
	public void RunScheduledEventsQueryReport(ResourceQuery resourceQuery, HttpServletRequest request) throws ExecutionException, InterruptedException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();
		status = "Running";
		boolean recalc = true;
		if (resourceQuery.getQueryString() != null &&
				(
						(resourceQuery.getQueryString().contains("skip") ||
								resourceQuery.getQueryString().contains("sort") ||
								resourceQuery.getQueryString().contains("asc")  ||
								resourceQuery.getQueryString().contains("desc")
						)
				)
		) { recalc = false;}
		HttpSession httpSession= request.getSession(true);
		if (!recalc) {
			status = "Finished";
			logger.info("get Events report from session");
			asyncQueryReport = (AsyncQueryReport) httpSession.getAttribute("asyncEventsQueryReport");
			Calendar time_now = Calendar.getInstance();
			Date endTime = time_now.getTime();
			time_elapsed = endTime.getTime() - startTime.getTime();
		}
		else {
			logger.trace("re-calculate Events report from scratch");
			Future<AsyncQueryReport> asyncQueryReportFuture = this.SearchAsyncScheduledEvents(resourceQuery);
			while (true) {
				if (asyncQueryReportFuture.isDone()) {
					status = "Finished";
					asyncQueryReport = asyncQueryReportFuture.get();
					httpSession.setAttribute("asyncEventsQueryReport", asyncQueryReportFuture.get());
					Calendar time_now = Calendar.getInstance();
					Date endTime = time_now.getTime();
					time_elapsed = endTime.getTime() - startTime.getTime();
					break;
				}
				status = "Running";
			}
		}
	}

	public Future<AsyncQueryReport> SearchAsyncScheduledEvents(ResourceQuery resourceQuery) {

		//#SET THIS LIMIT TO LIMIT THE REPORT WIDTH!!!!
		resourceQuery.setLimit(-1);
		List<ScheduledEvent> resourceList = scheduledEventService.searchScheduledEventsOnFilters(resourceQuery);

		asyncQueryReport = new AsyncQueryReport();
		Map<String, Unit> 		DepartmentFilterResults		= new HashMap<>();
		Map<String, AreaDto> 	AreaFilterResults 			= new HashMap<>();
		Map<String, TypeDto> 	TypeFilterResults 			= new HashMap<>();
		Map<String, Person> 	RespPersonFilterResults 	= new HashMap<>();

		logger.trace("Evaluate:" + resourceQuery.getLimit() + " resources");
		for (ScheduledEvent scheduledEvent: resourceList) {

			if (resourceQuery.getDepartmentId() == null) {
				this.AddUnitFromEvent2DepartmentFilter(scheduledEvent, DepartmentFilterResults);
			}
			if (resourceQuery.getStaffMemberId() == null) {
				this.AddPersonFromEvent2StaffMemberFilter(scheduledEvent, RespPersonFilterResults);
			}
			if (resourceQuery.getEventArea() == null) {
				this.AddAreaFromEvent2AreaFilter(scheduledEvent, AreaFilterResults);
			}
			if (resourceQuery.getEventType() == null) {
				this.AddTypeFromEvent2AreaFilter(scheduledEvent, TypeFilterResults);
			}
		} //FOR

		asyncQueryReport.setStaffMemberFilterResults(RespPersonFilterResults);
		asyncQueryReport.setDepartmentFilterResults(DepartmentFilterResults);
		asyncQueryReport.setAreaFilterResults(AreaFilterResults);
		asyncQueryReport.setTypeFilterResults(TypeFilterResults);


		return new AsyncResult<>(asyncQueryReport);
	}



	@Async("threadPoolQueryWorker")
	public Future<AsyncQueryReport> SearchAsyncLectures(ResourceQuery resourceQuery) {

		List<Resource> resourceList = resourceService.searchLecturesOnFilters(resourceQuery);

		asyncQueryReport = new AsyncQueryReport();
		Map<String, Unit> 		DepartmentFilterResults 		= new HashMap<>();
		Map<String, Course> 	CourseFilterResults 			= new HashMap<>();
		Map<String, Person> 	StaffMemberFilterResults 		= new HashMap<>();
		Map<String, ScheduledEvent> ScheduledEventFilterResults	= new HashMap<>();

		AdminFilterResults adminFilterResults = new AdminFilterResults();
		Map<String, AccessPolicy> AccessPolicyFilterResults		= new HashMap<>();
		Map<String, Tag> TagsFilterResults						= new HashMap<>();


		logger.trace("Evaluate:" + resourceQuery.getLimit() + " resources");
		for (Resource resource: resourceList) {

			//AccessPolicy
			if (resourceQuery.getAccessPolicy() == null) {
				this.AddAccessPolicy2AccessPolicyFilter(resource, AccessPolicyFilterResults);
			}
			// EVENT FILTER
			if (resourceQuery.getEventId() == null) {
				if (resource.getEvent() != null) {
					this.AddEvent2ScheduledEventsFilter(resource, ScheduledEventFilterResults);
				}
			}
			// COURSE FILTER
			if (resourceQuery.getCourseId() == null) {
				if (resource.getCourse() != null) {
					this.AddCourse2CoursesFilter(resource, CourseFilterResults);
				}
			}
			//DEPARTMENT FILTER
			if (resourceQuery.getDepartmentId() == null) {
				if (resource.getEvent() != null) {
					this.AddUnitFromScheduledEvent2DepartmentFilter(resource, DepartmentFilterResults);
				}
				else if (resource.getCourse() != null) {
					this.AddUnitFromResource2DepartmentFilter(resource, DepartmentFilterResults);
				}
			}
			//	STAFFMEMBERS
			if (resourceQuery.getStaffMemberId() == null) {
				if (resource.getEvent() != null) {
					this.AddPersonFromScheduledEvent2StaffMemberFilter(resource, StaffMemberFilterResults);
				}
				if (resource.getCourse() != null) {
					this.AddPersonFromResource2StaffMemberFilter(resource, StaffMemberFilterResults);
				}
			}
		} //FOR

		asyncQueryReport.setScheduledEventsFilterResults(ScheduledEventFilterResults);
		asyncQueryReport.setCourseFilterResults(CourseFilterResults);
		asyncQueryReport.setStaffMemberFilterResults(StaffMemberFilterResults);
		asyncQueryReport.setDepartmentFilterResults(DepartmentFilterResults);
		asyncQueryReport.setAccessPolicyFilterResults(AccessPolicyFilterResults);

		return new AsyncResult<>(asyncQueryReport);
	}

	@Async("threadPoolQueryWorker")
	public Future<AsyncQueryReport> SearchLiveAsyncLectures(ResourceQuery resourceQuery) {

		resourceQuery.setLimit(1000);
		List<Resource> resourceList = resourceService.searchLecturesOnFilters(resourceQuery);
		QueryResourceResults queryResourceResults = scheduleService.getLiveResourceListFromTodaysSchedule(resourceList);
		resourceList = queryResourceResults.getSearchResultList();

		asyncQueryReport = new AsyncQueryReport();
		Map<String, Unit> 		DepartmentFilterResults 		= new HashMap<>();
		Map<String, Course> 	CourseFilterResults 			= new HashMap<>();
		Map<String, Person> 	StaffMemberFilterResults 		= new HashMap<>();
		Map<String, ScheduledEvent> ScheduledEventFilterResults	= new HashMap<>();

		AdminFilterResults adminFilterResults = new AdminFilterResults();
		Map<String, AccessPolicy> AccessPolicyFilterResults		= new HashMap<>();
		Map<String, Tag> TagsFilterResults						= new HashMap<>();


		logger.trace("Evaluate:" + resourceQuery.getLimit() + " resources");
		for (Resource resource: resourceList) {

			//AccessPolicy
			if (resourceQuery.getAccessPolicy() == null) {
				this.AddAccessPolicy2AccessPolicyFilter(resource, AccessPolicyFilterResults);
			}
			// EVENT FILTER
			if (resourceQuery.getEventId() == null) {
				if (resource.getEvent() != null) {
					this.AddEvent2ScheduledEventsFilter(resource, ScheduledEventFilterResults);
				}
			}
			// COURSE FILTER
			if (resourceQuery.getCourseId() == null) {
				if (resource.getCourse() != null) {
					this.AddCourse2CoursesFilter(resource, CourseFilterResults);
				}
			}
			//DEPARTMENT FILTER
			if (resourceQuery.getDepartmentId() == null) {
				if (resource.getEvent() != null) {
					this.AddUnitFromScheduledEvent2DepartmentFilter(resource, DepartmentFilterResults);
				}
				else if (resource.getCourse() != null) {
					this.AddUnitFromResource2DepartmentFilter(resource, DepartmentFilterResults);
				}
			}
			//	STAFFMEMBERS
			if (resourceQuery.getStaffMemberId() == null) {
				if (resource.getEvent() != null) {
					this.AddPersonFromScheduledEvent2StaffMemberFilter(resource, StaffMemberFilterResults);
				}
				if (resource.getCourse() != null) {
					this.AddPersonFromResource2StaffMemberFilter(resource, StaffMemberFilterResults);
				}
			}
		} //FOR

		asyncQueryReport.setScheduledEventsFilterResults(ScheduledEventFilterResults);
		asyncQueryReport.setCourseFilterResults(CourseFilterResults);
		asyncQueryReport.setStaffMemberFilterResults(StaffMemberFilterResults);
		asyncQueryReport.setDepartmentFilterResults(DepartmentFilterResults);
		asyncQueryReport.setAccessPolicyFilterResults(AccessPolicyFilterResults);

		return new AsyncResult<>(asyncQueryReport);
	}

	@Async("threadPoolFilterLookUpExecutor")
	public CompletableFuture<QueryFilter> findDepartmentTitleById(String id) {

		QueryFilter queryFilter = new QueryFilter();
		if (id != null) {
			Department department = departmentService.findById(id);
			if (department != null) {
				queryFilter.setId(id);
				queryFilter.setText(department.getTitle());
			}
		}
		return CompletableFuture.completedFuture(queryFilter);
	}

	@Async("threadPoolFilterLookUpExecutor")
	public CompletableFuture<QueryFilter> findCourseTitleById(String id) {

		QueryFilter queryFilter = new QueryFilter();
		if (id != null) {
			Course course = courseService.findById(id);
			if (course != null) {
				queryFilter.setId(id);
				queryFilter.setText(course.getTitle());
			}
		}
		return CompletableFuture.completedFuture(queryFilter);
	}

	@Async("threadPoolFilterLookUpExecutor")
	public CompletableFuture<QueryFilter> findStaffMemberNameById(String id) {

		QueryFilter queryFilter = new QueryFilter();
		if (id != null) {
			OpUser opUser = opUserService.findById(id);
			if (opUser != null) {
				queryFilter.setId(id);
				queryFilter.setText(opUser.getName());
			}
		}
		return CompletableFuture.completedFuture(queryFilter);
	}

	@Async("threadPoolFilterLookUpExecutor")
	public CompletableFuture<QueryFilter> findScheduledEventNameById(String id) {

		QueryFilter queryFilter = new QueryFilter();
		if (id != null) {
			ScheduledEvent scheduledEvent = scheduledEventService.findById(id);
			if (scheduledEvent != null) {
				queryFilter.setId(id);
				queryFilter.setText(scheduledEvent.getTitle());
			}
		}
		return CompletableFuture.completedFuture(queryFilter);
	}

	private void AddUnitFromScheduledEvent2DepartmentFilter(Resource resource, Map<String,Unit> DepartmentFilter) {

		ScheduledEvent scheduledEvent = resource.getEvent();
		if (scheduledEvent.getResponsibleUnit() != null) {
			for (Unit unit: scheduledEvent.getResponsibleUnit()) {
				if (unit.getStructureType().equals(StructureType.DEPARTMENT)) {
					this.addOrUpdateDepartmentFilterWithUnit(unit, DepartmentFilter);
				}
			}
		}

	}

	private void AddUnitFromResource2DepartmentFilter(Resource resource, Map<String,Unit> DepartmentFilter) {
		if (resource.getDepartment() != null) {
			Unit unit = resource.getDepartment();
			this.addOrUpdateDepartmentFilterWithUnit(unit,DepartmentFilter);

			//# check and add Διατμηματικά Τμήματα
			if (resource.getCourse().getDepartmentsRelated() != null && !resource.getCourse().getDepartmentsRelated().isEmpty()) {
				for (Unit runit: resource.getCourse().getDepartmentsRelated()) {
					this.addOrUpdateDepartmentFilterWithUnit(runit,DepartmentFilter);
				}
			}
		}
	}

	private void AddUnitFromEvent2DepartmentFilter(ScheduledEvent scheduledEvent, Map<String,Unit> DepartmentFilter) {
		if (scheduledEvent.getResponsibleUnit() != null) {
			for (Unit rUnit: scheduledEvent.getResponsibleUnit()) {
				if (rUnit.getStructureType().equals(StructureType.DEPARTMENT)) {
					this.addOrUpdateDepartmentFilterWithUnit(rUnit,DepartmentFilter);
				}
			}
		}
	}

	private void AddAreaFromEvent2AreaFilter(ScheduledEvent scheduledEvent, Map<String, AreaDto> AreaFilterResults) {
		if (scheduledEvent.getArea()!= null) {
			String event_area = scheduledEvent.getArea();
			AreaDto areaDto = new AreaDto();
			areaDto.setId(event_area);
			areaDto.setText(multilingualServices.getValue(event_area, Locale.getDefault()));
			if (!AreaFilterResults.containsKey(event_area)) {
				areaDto.setCounter(1);
				AreaFilterResults.put(event_area,areaDto);
			}
			else {
				AreaDto areaDto1 = AreaFilterResults.get(event_area);
				int counter = areaDto1.getCounter() + 1;
				areaDto1.setCounter(counter);
				AreaFilterResults.replace(areaDto1.getId(), areaDto1);
			}
		}
	}
	private void AddTypeFromEvent2AreaFilter(ScheduledEvent scheduledEvent, Map<String, TypeDto> TypeFilterResults) {
		if (scheduledEvent.getType()!= null) {
			String event_type = scheduledEvent.getType();
			TypeDto typeDto = new TypeDto();
			typeDto.setId(event_type);
			typeDto.setText(multilingualServices.getValue(event_type, Locale.getDefault()));
			if (!TypeFilterResults.containsKey(event_type)) {
				typeDto.setCounter(1);
				TypeFilterResults.put(event_type,typeDto);
			}
			else {
				TypeDto typeDto1 = TypeFilterResults.get(event_type);
				int counter = typeDto1.getCounter() + 1;
				typeDto1.setCounter(counter);
				TypeFilterResults.replace(typeDto1.getId(), typeDto1);
			}
		}
	}

	private void AddEvent2ScheduledEventsFilter(Resource resource, Map<String,ScheduledEvent> ScheduledEventsFilter) {

			ScheduledEvent scheduledEvent = resource.getEvent();

			if (!ScheduledEventsFilter.containsKey(scheduledEvent.getId())) {
				scheduledEvent.setCounter(1);
				ScheduledEventsFilter.put(scheduledEvent.getId(), scheduledEvent);
			}
			else {
				ScheduledEvent nScheduledEvent = ScheduledEventsFilter.get(scheduledEvent.getId());
				int counter = nScheduledEvent.getCounter() + 1;
				nScheduledEvent.setCounter(counter);
				ScheduledEventsFilter.replace(nScheduledEvent.getId(), nScheduledEvent);
			}
	}

	private void AddCourse2CoursesFilter(Resource resource, Map<String,Course> CoursesFilter) {

			Course course = resource.getCourse();
			if (!CoursesFilter.containsKey(course.getId())) {
				course.setCounter(1);
				CoursesFilter.put(course.getId(), course);
			}
			else {
				Course nCourse = CoursesFilter.get(course.getId());
				int counter = nCourse.getCounter() + 1;
				nCourse.setCounter(counter);
				CoursesFilter.replace(nCourse.getId(), nCourse);
			}

	}

	private void addOrUpdateDepartmentFilterWithUnit(Unit unit, Map<String,Unit> DepartmentFilter ) {
		if (!DepartmentFilter.containsKey(unit.getId())) {
			unit.setCounter(1);
			DepartmentFilter.put(unit.getId(), unit);
		}
		else {
			Unit rUnit = DepartmentFilter.get(unit.getId());
			int counter = rUnit.getCounter() +  1;
			rUnit.setCounter(counter);
			DepartmentFilter.replace(unit.getId(), rUnit);
		}
	}

	private void AddPersonFromScheduledEvent2StaffMemberFilter(Resource resource, Map<String,Person> StaffMembersFilter) {
		ScheduledEvent scheduledEvent = resource.getEvent();
		if (scheduledEvent.getResponsiblePerson() != null) {
			Person person = scheduledEvent.getResponsiblePerson();
			if (!StaffMembersFilter.containsKey(person.getId())) {
				person.setCounter(1);
				StaffMembersFilter.put(person.getId(),person);
			}
			else {
				Person rPerson = StaffMembersFilter.get(person.getId());
				int counter = rPerson.getCounter() + 1;
				rPerson.setCounter(counter);
				StaffMembersFilter.replace(person.getId(), rPerson);
			}
		}
	}

	private void AddPersonFromResource2StaffMemberFilter(Resource resource, Map<String,Person> StaffMembersFilter) {
		if (resource.getSupervisor() != null) {
			Person person = resource.getSupervisor();
			if (!StaffMembersFilter.containsKey(person.getId())) {
				person.setCounter(1);
				StaffMembersFilter.put(person.getId(),person);
			}
			else {
				Person rPerson = StaffMembersFilter.get(person.getId());
				int counter = rPerson.getCounter() + 1;
				rPerson.setCounter(counter);
				StaffMembersFilter.replace(person.getId(), rPerson);
			}
		}
	}

	private void AddPersonFromEvent2StaffMemberFilter(ScheduledEvent scheduledEvent, Map<String,Person> RespPersonFilterResults) {
		if (scheduledEvent.getResponsiblePerson() != null) {
			Person person = scheduledEvent.getResponsiblePerson();
			if (!RespPersonFilterResults.containsKey(person.getId())) {
				person.setCounter(1);
				RespPersonFilterResults.put(person.getId(),person);
			}
			else {
				Person rPerson = RespPersonFilterResults.get(person.getId());
				int counter = rPerson.getCounter() + 1;
				rPerson.setCounter(counter);
				RespPersonFilterResults.replace(person.getId(), rPerson);
			}
		}
	}

	private void AddAccessPolicy2AccessPolicyFilter(Resource resource, Map<String,AccessPolicy> accessPolicyFilter) {

		String resource_policy = resource.getAccessPolicy();
		if (!accessPolicyFilter.containsKey(resource_policy)) {
			AccessPolicy accessPolicy = new AccessPolicy();
			accessPolicy.setAccess(resource_policy);
			accessPolicy.setCounter(1);
			accessPolicyFilter.put(resource_policy, accessPolicy);
		}
		else {
			AccessPolicy accessPolicy = accessPolicyFilter.get(resource_policy);
			int counter = accessPolicy.getCounter() + 1;
			accessPolicy.setCounter(counter);
			accessPolicyFilter.replace(resource_policy, accessPolicy);
		}
	}

	private void AddTag2TagsFilterResults(String tagname, Map<String, Tag> tagsFilterResults) {

		if (!tagsFilterResults.containsKey(tagname)) {
			Tag tag = new Tag();
			tag.setTag(tagname);
			tag.setCounter(1);
			tagsFilterResults.put(tagname,tag);
		}
		else {
			Tag tag = tagsFilterResults.get(tagname);
			int counter = tag.getCounter() + 1;
			tag.setCounter(counter);
			tagsFilterResults.replace(tagname, tag);
		}
	}

}
