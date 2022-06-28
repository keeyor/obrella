/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.vodapp.api.world;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.opendelos.legacydomain.queryresponse.QueryResponse;
import org.opendelos.legacydomain.queryresponse.ResourcesType;
import org.opendelos.legacydomain.videolecture.OrganizationType;
import org.opendelos.legacydomain.videolecture.PersonType;
import org.opendelos.legacydomain.videolecture.VideoLecture;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.security.TokenInfo;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Institution;
import org.opendelos.vodapp.security.token.TokenAuthService;
import org.opendelos.vodapp.services.opUser.OpUserService;
import org.opendelos.vodapp.services.resource.ResourceService;
import org.opendelos.vodapp.services.structure.CourseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@Slf4j
public class LmsResourceApi {

	public final String HEADER_SECURITY_TOKEN = "X-CustomToken";

	@Value("${app.zone}")
	String app_zone;
	@Value("${app.vod.url}")
	String app_vod_url;
	@Autowired
	Institution defaultInstitution;

	private final ResourceService resourceService;
	private final CourseService courseService;
	private final OpUserService opUserService;
	private final TokenAuthService tokenAuthService;

	@Autowired
	public LmsResourceApi(ResourceService resourceService, CourseService courseService, OpUserService opUserService, TokenAuthService tokenAuthService) {
		this.resourceService = resourceService;
		this.courseService = courseService;
		this.opUserService = opUserService;
		this.tokenAuthService = tokenAuthService;
	}

	@RequestMapping(value = "/api/dataservices/private/check_auth", method = RequestMethod.GET,  produces =  "application/json")
	public @ResponseBody byte[] checkAuth()
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json2 = gson.toJson("Valid");
		byte[] bytes = json2.getBytes(StandardCharsets.UTF_8);

		return  bytes;
	}

	@RequestMapping(value = "/api/lms/public/{lmsCode}", method = RequestMethod.GET,  produces =  "application/json")
	public @ResponseBody byte[] getPublicResourcesByLmsCode(@PathVariable String lmsCode, HttpServletRequest request,
															@RequestParam(value = "limit", required = false, defaultValue = "-1") int limit,
															@RequestParam(value = "skip", required = false, defaultValue = "0") int skip) throws Exception {

		String token = request.getHeader(HEADER_SECURITY_TOKEN);

		TokenInfo tokenInfo = tokenAuthService.loadTokenDetails(token,"PUBLIC");
		if (tokenInfo != null) {
			List<Resource> public_resources = getPublicResourcesByLmsCode(tokenInfo, lmsCode,null,skip,limit);
			return Object2Json(this.FormatResults2LegacyVideoLectureFormat(public_resources));
		}

		QueryResponse queryResponse = new QueryResponse();
		queryResponse.setNumofResults(0);

		return Object2Json(queryResponse);

	}

	@RequestMapping(value = "/api/lms/public/{lmsCode}/ay/{academicYear}", method = RequestMethod.GET,  produces =  "application/json")
	public @ResponseBody byte[] getPublicResourcesByLmsCodeAndYear(@PathVariable String lmsCode, @PathVariable String academicYear,HttpServletRequest request,
																   @RequestParam(value = "limit", required = false, defaultValue = "-1") int limit,
																   @RequestParam(value = "skip", required = false, defaultValue = "0") int skip) throws Exception {
		String token = request.getHeader(HEADER_SECURITY_TOKEN);

		TokenInfo tokenInfo = tokenAuthService.loadTokenDetails(token,"PUBLIC");
		if (tokenInfo != null) {
			List<Resource> public_resources = getPublicResourcesByLmsCode(tokenInfo, lmsCode,academicYear,skip,limit);
			return Object2Json(this.FormatResults2LegacyVideoLectureFormat(public_resources));
		}

		QueryResponse queryResponse = new QueryResponse();
		queryResponse.setNumofResults(0);

		return Object2Json(queryResponse);
	}


	@RequestMapping(value = "/api/dataservices/private/lms/{lmsCode}", method = RequestMethod.GET,  produces =  "application/json")
	public @ResponseBody byte[] getPrivateResourcesByLmsCode(@PathVariable String lmsCode, HttpServletRequest request,
															 @RequestParam(value = "limit", required = false, defaultValue = "-1") int limit,
															 @RequestParam(value = "skip", required = false, defaultValue = "0") int skip) throws AuthenticationException {

		OpUser user;
		TokenInfo tokenInfo = (TokenInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		tokenInfo.setUserId(tokenInfo.getUserId());
		user = opUserService.findById(tokenInfo.getUserId());
		List<Resource> private_resources = GetPrivateResourcesForTokenRequest(user,tokenInfo, lmsCode,null,skip,limit);
		return Object2Json(this.FormatResults2LegacyVideoLectureFormat(private_resources));

	}

	@RequestMapping(value = "/api/dataservices/private/lms/{lmsCode}/ay/{academicYear}", method = RequestMethod.GET,  produces =  "application/json")
	public @ResponseBody byte[] getPrivateResourcesByLmsCodeAndYear(@PathVariable String lmsCode, @PathVariable String academicYear,HttpServletRequest request,
																	@RequestParam(value = "limit", required = false, defaultValue = "-1") int limit,
																	@RequestParam(value = "skip", required = false, defaultValue = "0") int skip) throws Exception {

		OpUser user;
		TokenInfo tokenInfo = (TokenInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		tokenInfo.setUserId(tokenInfo.getUserId());
		user = opUserService.findById(tokenInfo.getUserId());
		List<Resource> private_resources = GetPrivateResourcesForTokenRequest(user,tokenInfo, lmsCode,null,skip,limit);
		return Object2Json(this.FormatResults2LegacyVideoLectureFormat(private_resources));
	}

 	private List<Resource> GetPrivateResourcesForTokenRequest(OpUser user,TokenInfo tokenInfo, String lmsCode, String academicYear, int skip, int limit) {

		List<Resource> resourceList = new ArrayList<>();

		String lmsId = tokenInfo.getDomainName();
		List<Course> courses = courseService.findByLmsIdAndCode(lmsId,lmsCode);

		for (Course course: courses) {
			String courseId_withLmsCode = course.getId();
			ResourceQuery resourceQuery = new ResourceQuery();
			resourceQuery.setCourseId(courseId_withLmsCode);
			resourceQuery.setResourceType("c"); 		// Lectures only!
			resourceQuery.setAccessPolicy("private");	// only private for this type of call
			if (academicYear != null) {
				resourceQuery.setAcademicYear(academicYear);
			}
			//Set Security Restrictions by User type (SA,MANAGER, SUPPORT, TEACHER etc)
			resourceQuery = resourceService.setAccessRestrictions(resourceQuery,user);
			resourceQuery.setLimit(limit);
			resourceQuery.setSkip(skip);
			QueryResourceResults resourceResults = resourceService.searchLMSLectures(resourceQuery);
			if (!resourceResults.getSearchResultList().isEmpty()) {
				resourceList.addAll(resourceResults.getSearchResultList());
			}
		}

		return resourceList;
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


	private QueryResponse FormatResults2LegacyVideoLectureFormat(List<Resource> search_results) {

		QueryResponse queryResponse = new QueryResponse();
		queryResponse.setNumofResults(search_results.size());
		queryResponse.setPlayerBasePath(app_vod_url + "/player");
		List<ResourcesType> resourcesTypeList = new ArrayList<>();
		for (Resource resource: search_results) {
			ResourcesType resourcesType = new ResourcesType();
			resourcesType.setResourceID(resource.getId());
			VideoLecture videoLecture = new VideoLecture();
			videoLecture.setIdentifier(resource.getId());
			videoLecture.setTitle(resource.getTitle());
			videoLecture.setDescription(resource.getDescription());

			LocalDateTime ldt = LocalDateTime.ofInstant(resource.getDate(), ZoneId.of(app_zone));
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			String formatDateTime = ldt.format(formatter);
			videoLecture.setDate(formatDateTime);


			OrganizationType organizationType = new OrganizationType();
			organizationType.setName(defaultInstitution.getTitle());
			videoLecture.setOrganization(organizationType);
			
			
			VideoLecture.Rights video_rights = new VideoLecture.Rights();
			PersonType personType = new PersonType();
			personType.setAffiliation(resource.getSupervisor().getAffiliation());
			personType.setIdentity(resource.getSupervisor().getId());
			personType.setName(resource.getSupervisor().getName());
			personType.setUnitName(resource.getDepartment().getTitle());
			video_rights.setCreator(personType);
			videoLecture.setRights(video_rights);

			resourcesType.setVideoLecture(videoLecture);
			resourcesTypeList.add(resourcesType);
		}
		queryResponse.getResources().addAll(resourcesTypeList);
		return queryResponse;
	}


	private byte[] Object2Json(Object o) {

		String json = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			if (o != null) {
				json = mapper.writeValueAsString(o);
			} else {
				json = "[]";
			}
		}
		catch (IOException e) {
			log.error("Error: FormatResultsForSelect");
			return null;
		}

		return json.getBytes(StandardCharsets.UTF_8);
	}
}
