/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.eventsapp.mvc;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.opendelos.eventsapp.services.i18n.MultilingualServices;
import org.opendelos.eventsapp.services.i18n.OptionServices;
import org.opendelos.eventsapp.services.resource.ResourceService;
import org.opendelos.eventsapp.services.resource.ResourceUtils;
import org.opendelos.eventsapp.services.structure.DepartmentService;
import org.opendelos.model.common.SearchUtils;
import org.opendelos.model.common.Select2GenGroup;
import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;

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

	private final OptionServices optionServices;
	private final ResourceService resourceService;
	private final ResourceUtils resourceUtils;
	private final DepartmentService departmentService;
	private final MultilingualServices multilingualServices;


	@Autowired
	public SearchController(OptionServices optionServices, ResourceService resourceService, ResourceUtils resourceUtils, DepartmentService departmentService, MultilingualServices multilingualServices) {
		this.optionServices = optionServices;
		this.resourceService = resourceService;
		this.resourceUtils = resourceUtils;
		this.departmentService = departmentService;

		this.multilingualServices = multilingualServices;
	}

	@GetMapping(value = {"/search"})
	public String getHomePage(final Model model,HttpServletRequest request, Locale locale,
			@RequestParam(value = "ft", required = false, defaultValue = "") String ft,
			@RequestParam(value = "d", required = false) String d,     // Department
			@RequestParam(value = "cc", required = false) String cc,   // Category
			@RequestParam(value = "sc", required = false) String sc,   // School
			@RequestParam(value = "et", required = false) String et,   // EventType
			@RequestParam(value = "ea", required = false) String ea,   // EventArea
			@RequestParam(value = "limit", required = false, defaultValue = "27") int limit,
			@RequestParam(value = "skip", required = false, defaultValue = "0") int skip,
			@RequestParam(value = "sort", required = false, defaultValue = "date") String sort,
			@RequestParam(value = "direction", required = false, defaultValue = "desc") String direction) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();

		if (ft != null && !ft.trim().equals("") && !ft.trim().equals("a")) {
			ft =  URLDecoder.decode(ft, "UTF-8");
		}
		ResourceQuery resourceQuery = new ResourceQuery();
		resourceQuery.setFt(ft);
		resourceQuery.setResourceType("e");
		resourceQuery.setEventArea(ea);
		resourceQuery.setEventType(et);
		resourceQuery.setUniqueOnly(true);
		resourceQuery.setDepartmentId(d);
		resourceQuery.setCategoryCode(cc);
		resourceQuery.setSchoolId(sc);
		resourceQuery.setLimit(limit);

		if (skip < 0) { skip = 0; }

		resourceQuery.setSkip(skip);
		if (sort == null) {
			if (ft == null || ft.trim().isEmpty()) { sort = "date"; }
			else { sort = "rel"; }
		}
		resourceQuery.setSort(sort);
		resourceQuery.setDirection(direction);
		resourceQuery.setAccessPolicy("public");

		String queryString = request.getQueryString();
		resourceQuery.setQueryString(queryString);
		model.addAttribute("queryString",queryString );

		QueryResourceResults queryResourceResults;
		queryResourceResults = resourceService.searchPageableLectures(resourceQuery);
		resourceQuery.setTotalResults(queryResourceResults.getTotalResults());
		model.addAttribute("QR", queryResourceResults);
		model.addAttribute("resourceQuery", resourceQuery);

		Calendar time_now = Calendar.getInstance();
		Date endTime = time_now.getTime();
		long diff = endTime.getTime() - startTime.getTime();
		model.addAttribute("PageLoadTime", diff);

		//Multimedia Web BaseDir (for all Resources)
		StringBuilder multimedia_base_web_dir = resourceUtils.getMultimediaBaseWebPath();
		model.addAttribute("mBaseWedDir",multimedia_base_web_dir.toString());
		//Languages
		String[] langList = optionServices.getLanguages(locale);
		model.addAttribute("langList",langList);

		String urlString  = request.getRequestURL().toString();
		prepareLinkReplacements(model,urlString);

		String[] subAreasList = null;
		HashMap<String, List<String>> thematicstList = null;
		List<Select2GenGroup> departmentsBySchool = null;
		if (ea != null) {
			if (ea.equals("ea_es")) {
				subAreasList = optionServices.getSubAreasOfAreaByKey("ea_es", locale);
				departmentsBySchool = departmentService.getAllDepartmentsGroupedBySchool("", locale);
			}
			else if (ea.equals("ea_uas")) {
				subAreasList = optionServices.getSubAreasOfAreaByKey("ea_uas", locale);
				//Categories
				thematicstList = optionServices.getSortedThematics(locale);
			}
		}
		model.addAttribute("subAreasList", subAreasList);
		model.addAttribute("catList", thematicstList);
		model.addAttribute("unitFilter", departmentsBySchool);

	 	model.addAttribute("page","search");

		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		return "search";
	}

	@RequestMapping(value = {"/search"}, method = RequestMethod.POST)
	public String SearchPost(@ModelAttribute("resourceQuery") final ResourceQuery resourceQuery,
							 @RequestParam("ft") String ft,
							 @RequestParam(value = "categoryCode", required = false) String cc,
			 			     @RequestParam(value = "eventType", required = false) String et,
							 @RequestParam(value = "departmentId", required = false) String d,
							 @RequestParam(value = "schoolId", required = false) String s,
							 @RequestParam(value = "eventArea", required = false) String ea,   // EventArea
			HttpServletRequest request) throws UnsupportedEncodingException {

		final String view;
		StringBuilder builder = new StringBuilder();

		if (ft != null && !ft.isEmpty() && !ft.trim().equals("a")) {
			builder.append("?ft=").append(URLEncoder.encode(ft, "UTF-8"));
		}
		else if (resourceQuery.getFt() != null  && !resourceQuery.getFt().isEmpty() && !resourceQuery.getFt().trim().equals("a"))  {
			builder.append("?ft=").append(URLEncoder.encode(resourceQuery.getFt(), "UTF-8"));
		}

		if (cc != null && !cc.isEmpty()) {
			if (builder.toString().length() == 0) {
				builder.append("?");
			}
			else {
				builder.append("&");
			}
			builder.append("cc=").append(URLEncoder.encode(cc, "UTF-8"));
		}
		else if (resourceQuery.getCategoryCode() != null  && !resourceQuery.getCategoryCode().isEmpty())  {
			if (builder.toString().length() == 0) {
				builder.append("?");
			}
			else {
				builder.append("&");
			}
			builder.append("cc=").append(URLEncoder.encode(resourceQuery.getCategoryCode(), "UTF-8"));
		}

		if (ea != null && !ea.isEmpty()) {
			if (builder.toString().length() == 0) {
				builder.append("?");
			}
			else {
				builder.append("&");
			}
			builder.append("ea=").append(URLEncoder.encode(ea, "UTF-8"));
		}
		else if (resourceQuery.getEventArea() != null  && !resourceQuery.getEventArea().isEmpty())  {
			if (builder.toString().length() == 0) {
				builder.append("?");
			}
			else {
				builder.append("&");
			}
			builder.append("ea=").append(URLEncoder.encode(resourceQuery.getEventArea(), "UTF-8"));
		}

		if (et != null && !et.isEmpty()) {
			if (builder.toString().length() == 0) {
				builder.append("?");
			}
			else {
				builder.append("&");
			}
			builder.append("et=").append(URLEncoder.encode(et, "UTF-8"));
		}
		else if (resourceQuery.getEventType() != null  && !resourceQuery.getEventType().isEmpty())  {
			if (builder.toString().length() == 0) {
				builder.append("?");
			}
			else {
				builder.append("&");
			}
			builder.append("et=").append(URLEncoder.encode(resourceQuery.getEventType(), "UTF-8"));
		}
		if (d != null && !d.isEmpty()) {
			if (builder.toString().length() == 0) {
				builder.append("?");
			}
			else {
				builder.append("&");
			}
			builder.append("d=").append(URLEncoder.encode(d, "UTF-8"));
		}
		else if (resourceQuery.getDepartmentId() != null  && !resourceQuery.getDepartmentId().isEmpty())  {
			if (builder.toString().length() == 0) {
				builder.append("?");
			}
			else {
				builder.append("&");
			}
			builder.append("d=").append(URLEncoder.encode(resourceQuery.getDepartmentId(), "UTF-8"));
		}
		if (s != null && !s.isEmpty()) {
			if (builder.toString().length() == 0) {
				builder.append("?");
			}
			else {
				builder.append("&");
			}
			builder.append("sc=").append(URLEncoder.encode(s, "UTF-8"));
		}
		else if (resourceQuery.getSchoolId() != null  && !resourceQuery.getSchoolId().isEmpty())  {
			if (builder.toString().length() == 0) {
				builder.append("?");
			}
			else {
				builder.append("&");
			}
			builder.append("sc=").append(URLEncoder.encode(resourceQuery.getSchoolId(), "UTF-8"));
		}
		String params = builder.toString();
		view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +  request.getServletPath()).path(params).build().toUriString();
		return view;
	}

	private void prepareLinkReplacements (Model model, String urlString) throws UnsupportedEncodingException {

		String urlSkip= SearchUtils.prepareSearchLinksForParam(urlString,"skip","N");
		model.addAttribute("nSkip",urlSkip);
	}
}
