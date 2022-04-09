/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc.admin.content;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.opendelos.control.services.async.AsyncQueryComponent;
import org.opendelos.control.services.async.QueryFilter;
import org.opendelos.control.services.i18n.MultilingualServices;
import org.opendelos.control.services.i18n.OptionServices;
import org.opendelos.control.services.opUser.OpUserService;
import org.opendelos.control.services.scheduledEvent.ScheduledEventService;
import org.opendelos.control.services.structure.DepartmentService;
import org.opendelos.control.services.structure.InstitutionService;
import org.opendelos.control.services.structure.SchoolService;
import org.opendelos.control.mvc.admin.ResourceEditorUtils;
import org.opendelos.model.properties.MultimediaProperties;
import org.opendelos.model.common.SearchUtils;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.repo.QueryScheduledEventsResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.resources.dtos.ScheduledEventDto;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.structure.School;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class ScheduledEventsController {

	@ModelAttribute("section")
	public String getAdminSection() {
		return "events_content";
	}

	@Value("${default.institution.identity}")
	String institution_identity;
	@Value("${app.zone}")
	String app_zone;

	private static final String ATTRIBUTE_NAME = "ScheduledEventDto";
	private static final String BINDING_RESULT_NAME = "org.springframework.validation.BindingResult." + ATTRIBUTE_NAME;

	private final ScheduledEventService scheduledEventService;
	private final ResourceEditorUtils resourceEditorUtils;
	private final OpUserService opUserService;
	private final InstitutionService institutionService;
	private final SchoolService schoolService;
	private final DepartmentService departmentService;
	private final ScheduledEventRegistrationValidator scheduledEventRegistrationValidator;
	private final OptionServices optionServices;
	private final MultilingualServices multilingualServices;

	private final AsyncQueryComponent asyncQueryComponent;

	private final MultimediaProperties multimediaProperties;

	@Autowired
	public ScheduledEventsController(ScheduledEventService scheduledEventService, ResourceEditorUtils resourceEditorUtils, OpUserService opUserService, InstitutionService institutionService, SchoolService schoolService, DepartmentService departmentService, ScheduledEventRegistrationValidator scheduledEventRegistrationValidator, OptionServices optionServices, MultilingualServices multilingualServices, AsyncQueryComponent asyncQueryComponent, MultimediaProperties multimediaProperties) {
		this.scheduledEventService = scheduledEventService;
		this.resourceEditorUtils = resourceEditorUtils;
		this.opUserService = opUserService;
		this.institutionService = institutionService;
		this.schoolService = schoolService;
		this.departmentService = departmentService;
		this.scheduledEventRegistrationValidator = scheduledEventRegistrationValidator;
		this.optionServices = optionServices;
		this.multilingualServices = multilingualServices;
		this.asyncQueryComponent = asyncQueryComponent;
		this.multimediaProperties = multimediaProperties;
	}

	@GetMapping(value = {"admin/sevents", "admin/sevents/"})
	public String getEventsPanel(final Model model, HttpServletRequest request,
			@RequestParam(value = "ft", required = false, defaultValue = "") String ft,
			@RequestParam(value = "d", required = false) String d,     // Department
			@RequestParam(value = "s", required = false) String s,     // Staff Member
			@RequestParam(value = "y", required = false) String y,     // Academic Year
			@RequestParam(value = "ea", required = false) String ea,     // Event Area
			@RequestParam(value = "et", required = false) String et,     // Event Type
			@RequestParam(value = "limit", required = false, defaultValue = "50") int limit,
			@RequestParam(value = "skip", required = false, defaultValue = "0") int skip,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "direction", required = false, defaultValue = "desc") String direction,
			Locale locale) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();

		String c_page = "sevents";
		model.addAttribute("cpage", c_page);
		addUserAccessAttributes(model,editor);

		ResourceQuery resourceQuery = new ResourceQuery();
		if (ft != null && !ft.trim().equals("") && !ft.trim().equals("a")) {
			ft =  URLDecoder.decode(ft, "UTF-8");
		}
		resourceQuery.setDepartmentId(d);
		resourceQuery.setFt(ft);
		resourceQuery.setStaffMemberId(s);
		resourceQuery.setAcademicYear(y);
		resourceQuery.setEventArea(ea);
		resourceQuery.setEventType(et);
		resourceQuery.setLimit(limit);

		if (skip < 0) { skip = 0; }
		resourceQuery.setSkip(skip);

		if (sort == null) {
			if (ft == null || ft.trim().isEmpty()) { sort = "date"; }
			else { sort = "rel";}
		}
		resourceQuery.setSort(sort);
		resourceQuery.setDirection(direction);

		String queryString = request.getQueryString();
		resourceQuery.setQueryString(queryString);
		model.addAttribute("queryString",queryString );

		QueryScheduledEventsResults queryScheduledEventsResults;
		//if (queryString != null && !queryString.trim().equals("")) {
			resourceQuery = scheduledEventService.setAccessRestrictions(resourceQuery,editor);
			queryScheduledEventsResults = scheduledEventService.searchPageableScheduledEvents(resourceQuery);
			resourceQuery.setTotalResults(queryScheduledEventsResults.getTotalResults());
			model.addAttribute("QR", queryScheduledEventsResults);
			setFilterDetails(model,resourceQuery,locale);
		//}
		asyncQueryComponent.setStatus("Pending");
		ResourceQuery finalResourceQuery = resourceQuery;
		CompletableFuture<String> completableFuture
				= CompletableFuture.supplyAsync(() -> {
			try {
				asyncQueryComponent.RunScheduledEventsQueryReport(finalResourceQuery, request);
			}
			catch (ExecutionException | InterruptedException ex) {
				ex.printStackTrace();
			}
			return null;
		});

		model.addAttribute("resourceQuery", resourceQuery);

		String urlString   = request.getRequestURL().toString();
		if (request.getQueryString() != null) {
			urlString += "?" + request.getQueryString();
			request.getSession().setAttribute("user_events_history", c_page + "?" + request.getQueryString());
		}
		else {
			request.getSession().setAttribute("user_events_history", c_page);
		}
		prepareLinkReplacements(model,urlString);

		Calendar time_now = Calendar.getInstance();
		Date endTime = time_now.getTime();
		long diff = endTime.getTime() - startTime.getTime();
		model.addAttribute("PageLoadTime", diff);

		model.addAttribute("landing_page", "sevents");
		model.addAttribute("color", "blue");

		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		addUserAccessAttributes(model,editor);
		model.addAttribute("institution_identity",institution_identity);

		return "admin/content/scheduled_events/sevents";
	}

	@RequestMapping(value = { "admin/sevents", "admin/sevents/"}, method = RequestMethod.POST)
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

	@GetMapping(value = {"admin/sevent-editor", "admin/sevent-editor/"})
	public String getEventEditor(final Model model, HttpServletRequest request,@RequestParam(value = "id",  required = false) String id, Locale locale) {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		ScheduledEventDto scheduledEventDto;
		int responsibleUnitsSize = 0;
		if (!model.containsAttribute(BINDING_RESULT_NAME)) {
			if (id != null) {
				// CHECK ACCESS PERMISSIONS
				List<String> authorizedScheduledEventsIds = scheduledEventService.getAuthorizedScheduledEventsIdsByEditor(editor,"content");
				if (!authorizedScheduledEventsIds.contains(id)) {
					return "redirect:/403";
				}
				ScheduledEvent scheduledEvent = scheduledEventService.findById(id);
				scheduledEventDto = scheduledEventService.getScheduledEventDto(scheduledEvent);
				responsibleUnitsSize = scheduledEventDto.getResponsibleUnitIds().length;
			}
			else {
				scheduledEventDto = new ScheduledEventDto();
			}
			model.addAttribute("ScheduledEventDto", scheduledEventDto);
			model.addAttribute("responsibleUnitsSize",responsibleUnitsSize);
		}
		else { //# return from binding errors
			scheduledEventDto = (ScheduledEventDto) model.getAttribute("ScheduledEventDto");
			if (scheduledEventDto != null && scheduledEventDto.getResponsibleUnitIds() != null) {
				responsibleUnitsSize = scheduledEventDto.getResponsibleUnitIds().length;
			}
			model.addAttribute("responsibleUnitsSize",responsibleUnitsSize);
		}
		//Get user search history
		if (request.getSession().getAttribute("user_events_history") != null) {
			model.addAttribute("user_events_history", request.getSession().getAttribute("user_events_history"));
		}
		else {
			model.addAttribute("user_events_history", "");
		}

		String[] eventAreas = optionServices.getEventAreas(locale);
		model.addAttribute("areas",eventAreas);

		LinkedHashMap<String, List<String>> eventCategories = optionServices.getSortedThematics(locale);
		model.addAttribute("catList",eventCategories);

		model.addAttribute("id", id);
		model.addAttribute("upload_base_path", multimediaProperties.getEventWebDir());
		addUserAccessAttributes(model,editor);
		model.addAttribute("institution_identity",institution_identity);

		return "admin/content/scheduled_events/sevent-editor";
	}

	@PostMapping(value = {"admin/sevent-editor", "admin/sevent-editor/"})
	public String ScheduledEventPost(@Valid @ModelAttribute("ScheduledEventDto") ScheduledEventDto scheduledEventDto, @RequestParam(value = "action", required = false) String action,
			final BindingResult bindingResult,HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws Exception {

		String view;
		//#Fill Responsible Person Data
		if (scheduledEventDto.getResponsiblePerson().getId() != null) {
			OpUser responsiblePerson = opUserService.findById(scheduledEventDto.getResponsiblePerson().getId());
			if (responsiblePerson != null) {
				String rPerson_departmentId = responsiblePerson.getDepartment().getId();
				Department department = departmentService.findById(rPerson_departmentId);
				if (department != null) {
					Unit unit = new Unit(StructureType.DEPARTMENT,department.getId(),department.getTitle());
					scheduledEventDto.setResponsiblePerson(new Person(responsiblePerson.getId(), responsiblePerson.getName(), responsiblePerson.getAffiliation(),unit));
				}
			}
		}
		//#Fill Responsible Units Data
		List<Unit> responsibleUnits = new ArrayList<>();
		if (scheduledEventDto.getResponsibleUnitIds().length > 0) {
			for (int i =0; i < scheduledEventDto.getResponsibleUnitIds().length; i++) {
				 String unit_type = scheduledEventDto.getResponsibleUnitTypes()[i];
				 String unit_id = scheduledEventDto.getResponsibleUnitIds()[i];
				 Unit rUnit = null;
				 if (StructureType.valueOf(unit_type).equals(StructureType.INSTITUTION)) {
					 Institution institution = institutionService.findById(unit_id);
					 rUnit = new Unit(StructureType.INSTITUTION,institution.getId(),institution.getTitle());
				 }
				 else if  (StructureType.valueOf(unit_type).equals(StructureType.SCHOOL)) {
					   School school = schoolService.findById(unit_id);
					  rUnit = new Unit(StructureType.SCHOOL,school.getId(),school.getTitle());
				}
				else if  (StructureType.valueOf(unit_type).equals(StructureType.DEPARTMENT)) {
				 	 Department department = departmentService.findById(unit_id);
				 	 rUnit =  new Unit(StructureType.DEPARTMENT,department.getId(),department.getTitle());
				}
				if (rUnit != null) {
					responsibleUnits.add(rUnit);
				}
			}
		}
		scheduledEventDto.setResponsibleUnit(responsibleUnits);

		scheduledEventRegistrationValidator.validate(scheduledEventDto, bindingResult);
		String event_id = scheduledEventDto.getId();

		if (action != null && (action.equals("delete")) ) {
			if (event_id != null && !event_id.equals("")) {
				try {
					scheduledEventService.delete(event_id);

					String[] attr = {"msg_type", "msg_val"};
					String[] values = {"alert-success", "Η Εκδήλωση διαγράφηκε!",};
					setFlashAttributes(request, response, attr, values);

					String user_search_history = "";
					if (request.getSession().getAttribute("user_events_history") != null) {
						user_search_history = (String) request.getSession().getAttribute("user_events_history");
						view = "redirect:" + user_search_history;
					}
					else {
						view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
								request.getServletPath()).path("").build().toUriString();
					}
					return view;
				}
				catch (Exception e) {
					String msg = e.getMessage();
					String info="";
					if (msg.equals("_FORBIDDEN_LECTURES")) {
						info = "Η Προγραμματισμένη Εκδήλωση δεν μπορεί να διαγραφεί! Βρέθηκαν Διαλέξεις";
					}
					else if (msg.equals("_FORBIDDEN_SCHEDULER")) {
						info = "Το Προγραμματισμένη Εκδήλωση δεν μπορεί να διαγραφεί! Βρέθηκαν προγραμματισμένες Εκδηλώσεις";
					}
					else if (msg.equals("_NOT_FOUND")) {
						info = "Η Προγραμματισμένη Εκδήλωση δεν βρέθηκε";
					}
					else {
						info = "Άγνωστο Λάθος";
					}
					String[] attr = {"msg_type", "msg_val"};
					String[] values = {"alert-danger", info};
					setFlashAttributes(request, response, attr, values);

					view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
							request.getServletPath()).path("?id=" + event_id).build().toUriString();

					return view;
				}
			}
		}
		if (bindingResult.hasErrors()) {
			// create a flashmap
			FlashMap flashMap = new FlashMap();
			// store the message
			flashMap.put("msg_val", "Η αποθήκευση απέτυχε! Υπάρχουν ελλείψεις στη φόρμα εισαγωγής");
			flashMap.put("msg_type", "alert-danger");
			flashMap.put(BINDING_RESULT_NAME, bindingResult);
			flashMap.put(ATTRIBUTE_NAME, scheduledEventDto);
			// create a flashMapManager with `request`
			FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
			// save the flash map data in session with flashMapManager
			if (flashMapManager != null) {
				flashMapManager.saveOutputFlashMap(flashMap, request, response);
			}
			if (scheduledEventDto.getId() != null && !scheduledEventDto.getId().equals("")) {
				view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
						request.getServletPath()).path("?id=" + event_id).build().toUriString();
			}
			else {
				view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
						request.getServletPath()).build().toUriString();
			}
		} else {
			/* Update: Date Modified */
			scheduledEventDto.setDateModified(resourceEditorUtils.getDateTimeNow(app_zone));
			/* Update Editor if Needed */
			OoUserDetails ooUserDetails = (OoUserDetails) authentication.getPrincipal();
			if (scheduledEventDto.getEditor() == null || !ooUserDetails.getId().equals(scheduledEventDto.getEditor().getId())) {
				OpUser opUser = opUserService.findById(ooUserDetails.getId());
				Person editor = resourceEditorUtils.getPersonFromOpUser(opUser);
				scheduledEventDto.setEditor(editor);
			}
			/* delete photo file if null */
			String photo_rUrl = scheduledEventDto.getPhotoRelativeUrl();
			if (event_id != null && !event_id.trim().equals("") && photo_rUrl == null || photo_rUrl.equals("")) {
				String upload_base_path = multimediaProperties.getEventAbsDir();
				String photo_path = upload_base_path + event_id + "/" + event_id + ".jpg";
				FileUtils.deleteQuietly(new File(photo_path));
			}
			/* Save or Update Resource */
			ScheduledEvent scheduledEvent = new ScheduledEvent();
			BeanUtils.copyProperties(scheduledEventDto,scheduledEvent);
			long updatedResources = 0;
			if (event_id == null || scheduledEventDto.getId().trim().equals("")) {
				scheduledEvent.setId(null); //!Important in order to create new resource
				event_id = scheduledEventService.create(scheduledEvent);
			}
			else {
				updatedResources = scheduledEventService.findAndUpdate(scheduledEvent);
			}
			String[] attr = {"msg_type","msg_val"};
			String[] values = {"alert-success","Η καταχώρηση αποθηκεύτηκε! Ενημερώθηκαν " + updatedResources + " καταχωρήσεις",};
			this.setFlashAttributes(request, response, attr, values);

			view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri()
					.replacePath(request.getContextPath() +
							request.getServletPath()).path("?id=" + event_id).build().toUriString();
		}
		return view;
	}

	private void setFlashAttributes(HttpServletRequest request, HttpServletResponse response, String[] attr, String[] values) {

		// create a flashmap
		FlashMap flashMap = new FlashMap();
		// store the message
		for (int i=0;i<attr.length; i++) {
			flashMap.put(attr[i], values[i]);
		}
		// create a flashMapManager with `request`
		FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
		// save the flash map data in session with flashMapManager
		if (flashMapManager != null) {
			flashMapManager.saveOutputFlashMap(flashMap, request, response);
		}
	}

	private void addUserAccessAttributes(Model model, OoUserDetails editor) {

		String user_access="";
		boolean user_isStaffMember = false;
		boolean user_isManager = false;
		if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SA"))) {
			user_access = "SA";
		}
		else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
			user_access = "MANAGER";
		}
		else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPPORT"))) {
			user_access = "SUPPORT";
		}
		if (!user_access.equals("")) {
			user_isManager = true;
		}
		if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFFMEMBER"))) {
			user_isStaffMember = true;
		}
		model.addAttribute("user_access", user_access);
		model.addAttribute("user_isManager", user_isManager);
		model.addAttribute("user_isStaffMember",user_isStaffMember);
	}

	private void setFilterDetails(Model model, ResourceQuery resourceQuery, Locale locale) throws ExecutionException, InterruptedException {

		CompletableFuture<QueryFilter> departmentFilter = new CompletableFuture<>();
		CompletableFuture<QueryFilter> staffMemberFilter = new CompletableFuture<>();
 

		departmentFilter = asyncQueryComponent
				.findDepartmentTitleById(resourceQuery.getDepartmentId());

		staffMemberFilter = asyncQueryComponent
				.findStaffMemberNameById(resourceQuery.getStaffMemberId());

		model.addAttribute("areaFilter", "");
		model.addAttribute("areaFilterName", "");
		if (resourceQuery.getEventArea() != null && !resourceQuery.getEventArea().equals("")) {
			model.addAttribute("areaFilter", resourceQuery.getEventArea());
			model.addAttribute("areaFilterName", multilingualServices.getValue(resourceQuery.getEventArea(),locale));
		}
		model.addAttribute("etypeFilter", "");
		model.addAttribute("etypeFilterName", "");
		if (resourceQuery.getEventType() != null && !resourceQuery.getEventType().equals("")) {
			model.addAttribute("etypeFilter", resourceQuery.getEventType());
			model.addAttribute("etypeFilterName", multilingualServices.getValue(resourceQuery.getEventType(),locale));
		}


		//Wait for all async processes to complete
		CompletableFuture.allOf(departmentFilter,  staffMemberFilter).join();

		model.addAttribute("departmentFilter", departmentFilter.get());
		model.addAttribute("staffMemberFilter", staffMemberFilter.get());

	}

	private void prepareLinkReplacements (Model model, String urlString) throws UnsupportedEncodingException {

		String urlSkip= SearchUtils.prepareSearchLinksForParam(urlString,"skip","N");
		model.addAttribute("nSkip",urlSkip);
	}
}
