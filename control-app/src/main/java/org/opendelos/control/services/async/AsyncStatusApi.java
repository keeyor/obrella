/* 
     Author: Michael Gatzonis - 17/11/2020 
     live
*/
package org.opendelos.control.services.async;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.opendelos.control.api.common.ApiUtils;
import org.opendelos.model.common.Select2GenChild;
import org.opendelos.model.resources.AccessPolicy;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.resources.dtos.AreaDto;
import org.opendelos.model.resources.dtos.ResourceTypeDto;
import org.opendelos.model.resources.dtos.TypeDto;
import org.opendelos.model.structure.Course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AsyncStatusApi {

	private final AsyncQueryComponent asyncQueryComponent;

	@Autowired
	public AsyncStatusApi(AsyncQueryComponent asyncQueryComponent) {
		this.asyncQueryComponent = asyncQueryComponent;
	}

	@RequestMapping(value= "/api/v1/queryReportStatus", method = RequestMethod.GET, produces =  "text/html")
	public String getQueryReportStatus() {
		return asyncQueryComponent.getStatus();
	}

	@RequestMapping(value= "/api/v1/queryReportTime", method = RequestMethod.GET, produces =  "text/html")
	public String getQueryReportTime() {
		return String.valueOf(asyncQueryComponent.getTime_elapsed());
	}

	@RequestMapping(value= "/api/v1/getCoursesOfReport", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getCoursesQueryReport() {

		List<Course> courseList = new ArrayList<>();
		Map<String, Course> coursesFilter = asyncQueryComponent.getAsyncQueryReport().getCourseFilterResults();
		for (Map.Entry<String, Course> pair : coursesFilter.entrySet()) {
			courseList.add(pair.getValue());
		}
		Comparator<Course> titleSorter  = Comparator.comparing(Course::getTitle);
		courseList.sort(titleSorter);

		return ApiUtils.TransformResultsForDataTable(courseList);

	}

	@RequestMapping(value= "/api/v1/getStaffOfReport", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getStaffMembersQueryReport() {

		List<Person> staffList = new ArrayList<>();
		Map<String, Person> StaffMembersFilter = asyncQueryComponent.getAsyncQueryReport().getStaffMemberFilterResults();
		for (Map.Entry<String, Person> pair : StaffMembersFilter.entrySet()) {
			staffList.add(pair.getValue());
		}
		Comparator<Person> nameSorter  = Comparator.comparing(Person::getName);
		staffList.sort(nameSorter);
		return ApiUtils.TransformResultsForDataTable(staffList);
	}

	@RequestMapping(value= "/api/v1/getEventsOfReport", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getEventsQueryReport(HttpServletRequest request) {

		List<ScheduledEvent> eventList = new ArrayList<>();
		Map<String, ScheduledEvent> ScheduledEventsFilter = asyncQueryComponent.getAsyncQueryReport().getScheduledEventsFilterResults();
		for (Map.Entry<String, ScheduledEvent> pair : ScheduledEventsFilter.entrySet()) {
			eventList.add(pair.getValue());
		}
		Comparator<ScheduledEvent> titleSorter  = Comparator.comparing(ScheduledEvent::getTitle);
		eventList.sort(titleSorter);
		request.getSession().setAttribute("events_on_report",eventList);
		return ApiUtils.TransformResultsForDataTable(eventList);
	}

	@RequestMapping(value= "/api/v1/getAreasOfReport", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getAreasQueryReport(HttpServletRequest request) {

		List<AreaDto> areaList = new ArrayList<>();
		Map<String, AreaDto> ScheduledEventsFilter = asyncQueryComponent.getAsyncQueryReport().getAreaFilterResults();
		if (ScheduledEventsFilter != null) {
			for (Map.Entry<String, AreaDto> pair : ScheduledEventsFilter.entrySet()) {
				areaList.add(pair.getValue());
			}
			Comparator<AreaDto> titleSorter = Comparator.comparing(AreaDto::getText);
			areaList.sort(titleSorter);
		}
		return ApiUtils.TransformResultsForDataTable(areaList);
	}

	@RequestMapping(value= "/api/v1/getEventCategoriesOfReport", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getEventCategoriesQueryReport(HttpServletRequest request) {

		List<TypeDto> catList = new ArrayList<>();
		Map<String, TypeDto> ScheduledEventsCatFilter = asyncQueryComponent.getAsyncQueryReport().getEventCategoryFilterResults();
		if (ScheduledEventsCatFilter != null) {
			for (Map.Entry<String, TypeDto> pair : ScheduledEventsCatFilter.entrySet()) {
				catList.add(pair.getValue());
			}
			Comparator<TypeDto> titleSorter = Comparator.comparing(TypeDto::getText);
			catList.sort(titleSorter);
		}
		return ApiUtils.TransformResultsForDataTable(catList);
	}

	@RequestMapping(value= "/api/v1/getResourceTypeCounterOfReport", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getResourceTypeCounterQueryReport(HttpServletRequest request) {

		List<ResourceTypeDto> resourceTypeList = new ArrayList<>();
		Map<String, Long> ResourceTypeCounter = asyncQueryComponent.getAsyncQueryReport().getResourceTypeCounter();
		for (Map.Entry<String, Long> pair : ResourceTypeCounter.entrySet()) {
			ResourceTypeDto resourceTypeDto = new ResourceTypeDto();
			resourceTypeDto.setType(pair.getKey());
			resourceTypeDto.setCounter(pair.getValue());
			resourceTypeList.add(resourceTypeDto);
		}
		return ApiUtils.TransformResultsForDataTable(resourceTypeList);
	}

	@RequestMapping(value= "/api/v1/getTypesOfReport", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getTypesQueryReport(HttpServletRequest request) {

		List<TypeDto> typeList = new ArrayList<>();
		Map<String, TypeDto> ScheduledEventsFilter = asyncQueryComponent.getAsyncQueryReport().getTypeFilterResults();
		for (Map.Entry<String, TypeDto> pair : ScheduledEventsFilter.entrySet()) {
			typeList.add(pair.getValue());
		}
		Comparator<TypeDto> titleSorter  = Comparator.comparing(TypeDto::getText);
		typeList.sort(titleSorter);
		return ApiUtils.TransformResultsForDataTable(typeList);
	}

	@RequestMapping(value= "/api/v1/getDepartmentsOfReport", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getDepartmentsQueryReport() {

		List<Unit> unitList = new ArrayList<>();
		Map<String, Unit> DepartmentFilter = asyncQueryComponent.getAsyncQueryReport().getDepartmentFilterResults();
		for (Map.Entry<String, Unit> pair : DepartmentFilter.entrySet()) {
			unitList.add(pair.getValue());
		}
		Comparator<Unit> titleSorter  = Comparator.comparing(Unit::getTitle);
		unitList.sort(titleSorter);
		return ApiUtils.TransformResultsForDataTable(unitList);
	}

 	@RequestMapping(value= "/api/v1/getAccessPolicyOfReport", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getAccessPolicyQueryReport() {

		List<AccessPolicy> apList = new ArrayList<>();
		Map<String, AccessPolicy> apFilter = asyncQueryComponent.getAsyncQueryReport().getAccessPolicyFilterResults();
		for (Map.Entry<String, AccessPolicy> pair : apFilter.entrySet()) {
			apList.add(pair.getValue());
		}
		return ApiUtils.TransformResultsForDataTable(apList);
	}

/*	@RequestMapping(value= "/api/v1/getAdminFiltersOfReport", method = RequestMethod.GET, produces =  "application/json")
	public byte[] getAccessPolicyQueryReport(HttpServletRequest request) {

		AdminFilterResults adminFilterResults = asyncQueryComponent.getAsyncQueryReport().getAdminFilterResults();
		return ApiUtils.TransformResultsForDataTable(adminFilterResults);
	}*/

}


