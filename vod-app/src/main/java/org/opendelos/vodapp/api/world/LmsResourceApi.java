/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.vodapp.api.world;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.security.TokenInfo;
import org.opendelos.model.structure.Course;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.vodapp.api.common.ApiUtils;
import org.opendelos.vodapp.security.PopulateActiveUser;
import org.opendelos.vodapp.services.opUser.OpUserService;
import org.opendelos.vodapp.services.resource.ResourceService;
import org.opendelos.vodapp.services.structure.CourseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class LmsResourceApi {

	private final ResourceService resourceService;
	private final CourseService courseService;
	private final OpUserService opUserService;
	private final PopulateActiveUser populateActiveUser;


	@Autowired
	public LmsResourceApi(ResourceService resourceService, CourseService courseService, OpUserService opUserService, PopulateActiveUser populateActiveUser) {
		this.resourceService = resourceService;
		this.courseService = courseService;
		this.opUserService = opUserService;
		this.populateActiveUser = populateActiveUser;
	}

	@RequestMapping(value = "/api/dataservices/private/lms/{lmsCode}", method = RequestMethod.GET,  produces =  "application/json")
	public List<Resource> getPrivateResourcesByLmsCode(@PathVariable String lmsCode)
	{
		TokenInfo tokenInfo = (TokenInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (tokenInfo != null) {
			return GetResourcesForTokenRequest(tokenInfo, lmsCode,null,0,-1);
		}
		return new ArrayList<>();

	}

	@RequestMapping(value = "/api/dataservices/private/lms/{lmsCode}/ay/{academicYear}", method = RequestMethod.GET,  produces =  "application/json")
	public List<Resource> getPrivateResourcesByLmsCodeAndYear(@PathVariable String lmsCode, @PathVariable String academicYear)
	{
		TokenInfo tokenInfo = (TokenInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (tokenInfo != null) {
			return GetResourcesForTokenRequest(tokenInfo, lmsCode,academicYear,0,-1);
		}
		return new ArrayList<>();
	}

 	private List<Resource> GetResourcesForTokenRequest(TokenInfo tokenInfo, String lmsCode, String academicYear, int skip, int limit) {

		List<Resource> resourceList = new ArrayList<>();

		String lmsId = tokenInfo.getDomainName();
		String requester_userId = tokenInfo.getUserId();

		OpUser reqUser = opUserService.findById(requester_userId);
		OoUserDetails ooUserDetails = populateActiveUser.populate(reqUser);

		List<Course> courses = courseService.findByLmsIdAndCode(lmsId,lmsCode);

		for (Course course: courses) {
			String courseId_withLmsCode = course.getId();
			ResourceQuery resourceQuery = new ResourceQuery();
			resourceQuery.setCourseId(courseId_withLmsCode);
			resourceQuery.setResourceType("c"); 					// Lectures only!
			resourceQuery.setAccessPolicy("private");	// only private for this type of call
			if (academicYear != null) {
				resourceQuery.setAcademicYear(academicYear);
			}
			//Set Security Restrictions by User type (SA,MANAGER, SUPPORT, TEACHER etc)
			resourceQuery = resourceService.setAccessRestrictions(resourceQuery,ooUserDetails);
			resourceQuery.setLimit(limit);
			resourceQuery.setSkip(skip);
			QueryResourceResults resourceResults = resourceService.searchLMSLectures(resourceQuery);
			if (!resourceResults.getSearchResultList().isEmpty()) {
				resourceList.addAll(resourceResults.getSearchResultList());
			}
		}

		return resourceList;
	}

	@RequestMapping(value = "/api/lms/public/{lmsCode}", method = RequestMethod.GET,  produces =  "application/json")
	public List<Resource>  getPublicResourcesByLmsCode(@PathVariable String lmsCode)
	{
		TokenInfo tokenInfo = (TokenInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (tokenInfo != null) {
			return getPublicResourcesByLmsCode(tokenInfo, lmsCode,null,0,-1);
		}
		return new ArrayList<>();

	}

	@RequestMapping(value = "/api/lms/public/{lmsCode}/ay/{academicYear}", method = RequestMethod.GET,  produces =  "application/json")
	public List<Resource> getPublicResourcesByLmsCodeAndYear(@PathVariable String lmsCode, @PathVariable String academicYear)
	{
		TokenInfo tokenInfo = (TokenInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (tokenInfo != null) {
			return getPublicResourcesByLmsCode(tokenInfo, lmsCode,academicYear,0,-1);
		}
		return new ArrayList<>();
	}


    public List<Resource> getPublicResourcesByLmsCode(TokenInfo tokenInfo, String lmsCode, String academicYear, int skip, int limit) {

		List<Resource> resourceList = new ArrayList<>();
		String lmsId = tokenInfo.getDomainName();

		List<Course> courses = courseService.findByLmsIdAndCode(lmsId,lmsCode);

		for (Course course: courses) {
			String courseId_withLmsCode = course.getId();
			ResourceQuery resourceQuery = new ResourceQuery();
			resourceQuery.setCourseId(courseId_withLmsCode);
			resourceQuery.setAccessPolicy("public");	// only public for this type of call
			resourceQuery.setResourceType("c"); // Lectures only!
			if (academicYear != null) {
				resourceQuery.setAcademicYear(academicYear);
			}
			resourceQuery.setLimit(limit);
			resourceQuery.setSkip(skip);
			QueryResourceResults resourceResults = resourceService.searchLMSLectures(resourceQuery);
			if (!resourceResults.getSearchResultList().isEmpty()) {
				resourceList.addAll(resourceResults.getSearchResultList());
			}
		}
		return resourceList;

	}

	//DEBUG - TESTING API ( NOT USED! - Use from Postman )
	@RequestMapping(value="/api/v1/s2/courses.web/lms/{lmsCode}", method = RequestMethod.GET, produces =  "application/json")
	public ResponseEntity<String> getCoursesbyLmsCode(@PathVariable String lmsCode) {

		List<Course> courses = courseService.findByLmsIdAndCode("eclass.uoa.gr",lmsCode);

		try {
			String s2courses= ApiUtils.FormatResultsForSelect2(courses);
			return new ResponseEntity<>(s2courses, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	private  void debugPrintFields(List<Resource> resourceList) {

		for (Resource resource: resourceList) {
			log.info("YEAR: " + resource.getAcademicYear() + " ACCESS: " + resource.getAccessPolicy());
		}
	}
}
