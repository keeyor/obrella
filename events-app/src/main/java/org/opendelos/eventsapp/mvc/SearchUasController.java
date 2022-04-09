/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.eventsapp.mvc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.opendelos.eventsapp.services.i18n.MultilingualServices;
import org.opendelos.eventsapp.services.i18n.OptionServices;
import org.opendelos.eventsapp.services.resource.ResourceService;
import org.opendelos.eventsapp.services.resource.ResourceUtils;
import org.opendelos.model.common.SearchUtils;
import org.opendelos.model.properties.MultimediaProperties;
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
public class SearchUasController {

	private final OptionServices optionServices;
	private final ResourceService resourceService;
	private final ResourceUtils resourceUtils;
	private final MultilingualServices multilingualServices;
	private final MultimediaProperties multimediaProperties;


	@Autowired
	public SearchUasController(OptionServices optionServices, ResourceService resourceService, ResourceUtils resourceUtils, MultilingualServices multilingualServices, MultimediaProperties multimediaProperties) {
		this.optionServices = optionServices;
		this.resourceService = resourceService;
		this.resourceUtils = resourceUtils;
		this.multilingualServices = multilingualServices;

		this.multimediaProperties = multimediaProperties;
	}

	@GetMapping(value = {"/search-uas"})
	public String getHomePage(final Model model,HttpServletRequest request, Locale locale,
			@RequestParam(value = "cc", required = false) String cc,   // Category
			@RequestParam(value = "et", required = false) String et,   // EventType
			@RequestParam(value = "limit", required = false, defaultValue = "27") int limit,
			@RequestParam(value = "skip", required = false, defaultValue = "0") int skip,
			@RequestParam(value = "sort", required = false, defaultValue = "date") String sort,
			@RequestParam(value = "direction", required = false, defaultValue = "asc") String direction) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();

		ResourceQuery resourceQuery = new ResourceQuery();
		resourceQuery.setFt("");
		resourceQuery.setResourceType("e");
		resourceQuery.setEventArea("ea_uas");
		resourceQuery.setEventType(et);
		resourceQuery.setUniqueOnly(true);
		resourceQuery.setCategoryCode(cc);
		resourceQuery.setLimit(limit);

		if (skip < 0) { skip = 0; }

		resourceQuery.setSkip(skip);
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
		//Events Multimedia dia
		String events_base_web_dir = multimediaProperties.getEventWebDir();
		model.addAttribute("mEventsBaseWedDir",events_base_web_dir);

		//Languages
		String[] langList = optionServices.getLanguages(locale);
		model.addAttribute("langList",langList);

		String urlString   = request.getRequestURL().toString();
		if (request.getQueryString() != null) {
			urlString += "?" + request.getQueryString();
		}
		prepareLinkReplacements(model,urlString);

			//Categories
			HashMap<String, List<String>> thematicstList = optionServices.getSortedThematics(locale);
			model.addAttribute("catList", thematicstList);

			//Areas
		    model.addAttribute("areaTitle", multilingualServices.getValue("ea_uas",null,locale));
			String[] subAreasList  = optionServices.getSubAreasOfAreaByKey("ea_uas",locale);
			model.addAttribute("subAreasList", subAreasList);

			model.addAttribute("page","search-uas");

		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		return "search-uas";
	}

	@RequestMapping(value = {"/search-uas"}, method = RequestMethod.POST)
	public String SearchPost(@ModelAttribute("resourceQuery") final ResourceQuery resourceQuery,
							 @RequestParam(value = "categoryCode", required = false) String cc,
			 			     @RequestParam(value = "eventType", required = false) String et,
			HttpServletRequest request) throws UnsupportedEncodingException {

		final String view;
		StringBuilder builder = new StringBuilder();

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

		String params = builder.toString();
		view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +  request.getServletPath()).path(params).build().toUriString();
		return view;
	}

	private void prepareLinkReplacements (Model model, String urlString) throws UnsupportedEncodingException {

		String urlSkip= SearchUtils.prepareSearchLinksForParam(urlString,"skip","N");
		model.addAttribute("nSkip",urlSkip);
	}
}
