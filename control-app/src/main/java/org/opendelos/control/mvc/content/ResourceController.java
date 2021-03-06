/* 
     Author: Michael Gatzonis - 7/12/2020 
     live
*/
package org.opendelos.control.mvc.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opendelos.control.services.i18n.MultilingualServices;
import org.opendelos.control.services.i18n.OptionServices;
import org.opendelos.control.services.opUser.OpUserService;
import org.opendelos.control.services.resource.FileNameCleaner;
import org.opendelos.control.services.resource.GreekChar;
import org.opendelos.control.services.resource.ResourceService;
import org.opendelos.control.services.resource.ResourceUtils;
import org.opendelos.control.services.resource.ZipUtility;
import org.opendelos.control.services.scheduledEvent.ScheduledEventService;
import org.opendelos.control.services.structure.CourseService;
import org.opendelos.control.services.structure.InstitutionService;
import org.opendelos.legacydomain.slidesync.Slideshow;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.Presentation;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.ResourceAccess;
import org.opendelos.model.resources.ResourceStatus;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.resources.Slide;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.model.users.UserAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class ResourceController {

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_content";
	}

	@Value("${app.default.license}")
	String default_license;
	@Value("${app.zone}")
	String app_zone;
	@Value("${default.institution.identity}")
	String institution_identity;

	@Autowired
	Institution defaultInstitution;
	@Autowired
	String currentAcademicYear;

	private static final String ATTRIBUTE_NAME = "Resource";
	private static final String BINDING_RESULT_NAME = "org.springframework.validation.BindingResult." + ATTRIBUTE_NAME;

	private final ResourceService resourceService;
	private final InstitutionService institutionService;
	private final OpUserService opUserService;
	private final CourseService courseService;
	private final ScheduledEventService scheduledEventService;

	private final MultilingualServices multilingualServices;
	private final OptionServices optionServices;
	private final ResourceUtils resourceUtils;

	private final ResourceRegistrationValidator resourceRegistrationValidator;

	private final Logger logger = LoggerFactory.getLogger(ResourceController.class);

	@Autowired
	public ResourceController(ResourceService resourceService, InstitutionService institutionService, OpUserService opUserService, CourseService courseService, ScheduledEventService scheduledEventService, MultilingualServices multilingualServices, OptionServices optionServices, ResourceUtils resourceUtils, ResourceRegistrationValidator resourceRegistrationValidator) {
		this.resourceService = resourceService;
		this.institutionService = institutionService;
		this.opUserService = opUserService;
		this.courseService = courseService;
		this.scheduledEventService = scheduledEventService;
		this.multilingualServices = multilingualServices;
		this.optionServices = optionServices;
		this.resourceUtils = resourceUtils;
		this.resourceRegistrationValidator = resourceRegistrationValidator;
	}

	@GetMapping(value = {"admin/lecture-editor","admin/event-editor"})
	public String resourceEditor(final Model model, @RequestParam(value = "id",  required = false) String id, Locale locale, HttpServletRequest request)  {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		boolean userIsStaffMemberOnly = editor.getUserAuthorities()
				.contains(UserAccess.UserAuthority.STAFFMEMBER) && editor.getUserAuthorities().size() == 1;
		model.addAttribute("userIsStaffMemberOnly",userIsStaffMemberOnly);

		String urlString  = request.getRequestURL().toString();
		String resourceType = urlString.contains("event-editor") ? "EVENT" : "COURSE";
		String pageSelect = urlString.contains("event-editor") ? "event" : "lecture";
		model.addAttribute("resourceType", resourceType);
		model.addAttribute("pageSelect", pageSelect);

		//Get user search history
		if (request.getSession().getAttribute("user_search_history") != null) {
			model.addAttribute("user_search_history", request.getSession().getAttribute("user_search_history"));
		}
		else {
			model.addAttribute("user_search_history", "");
		}

		Resource resource;
		if (!model.containsAttribute(BINDING_RESULT_NAME)) {
			if (id != null) {
				resource = resourceService.findById(id);
				if (resource.getType().equals("COURSE")) {
					List<String> authorizedCourseIds = courseService.getAuthorizedCourseIdsByEditor(editor, "content");
					if (!authorizedCourseIds.contains(resource.getCourse().getId())) {
						return "redirect:/403";
					}
				}
				else {
					List<String> authorizedScheduledEventsIds = scheduledEventService.getAuthorizedScheduledEventsIdsByEditor(editor,"content");
					if (!authorizedScheduledEventsIds.contains(resource.getEvent().getId())) {
						return "redirect:/403";
					}
				}
				//
				this.setStorageFolders(model,resource);
				/* find resources from same series */
				/*if (resource.isParts()) {
					String parent_id = resource.getId();
					if (resource.getParentId() != null) {
						parent_id = resource.getParentId();
					}
					HashMap<String,Resource> resourceSeries = new LinkedHashMap<>();
					List<Resource> related_parts = resourceService.findRelatedPartsById(parent_id,null);
					for (Resource related_resource : related_parts) {
						resourceSeries.put(related_resource.getId(), related_resource);
					}
					model.addAttribute("resourceSeries",resourceSeries);
				}*/
			}
			else {
				resource = this.createEmptyResource(resourceType,currentAcademicYear);
			}
			//Authorize Edit to current User
			//>if (id!= null && !authorizationServices.AuthorizedEditorOnCourse(user, course)) {
			//>	throw new AccessDeniedException("Access Denied: Failed");
			//>}
			model.addAttribute("Resource", resource);
		}
		model.addAttribute("id", id);
		String editorId = editor.getId();

		this.SetModelAttributes(model,locale, editorId,currentAcademicYear);

		return "admin/content/resource/resource-editor";
	}

	@PostMapping(value = {"admin/lecture-editor" ,"admin/lecture-editor/", "admin/event-editor" ,"admin/event-editor/"})
	public String SearchPost(@Valid @ModelAttribute("Resource") Resource resource, @RequestParam(value = "action", required = false) String action,
			final BindingResult bindingResult,HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {

		final String view;
		String urlString  = request.getRequestURL().toString();
		String type;
		if (urlString.contains("event-editor")) {type = "EVENT";} else { type = "COURSE";}

		String resource_id = resource.getId();
		if (action != null && (action.equals("delete")) ) {
			if (resource_id != null && !resource_id.equals("")) {
				resourceService.delete(resource_id);

				String[] attr = {"msg_type", "msg_val"};
				String[] values = {"alert-success", "?? ???????????????????? ????????????????????!",};
				setFlashAttributes(request, response, attr, values);

				String user_search_history = "";
				if (request.getSession().getAttribute("user_search_history") != null) {
					user_search_history = (String) request.getSession().getAttribute("user_search_history");
				}
				view = "redirect:" + user_search_history;
				return view;
			}
		}
		if (action != null && (action.equals("copy")) ) {
			Resource resource_o = resourceService.findById(resource_id); //get Resource from Database or return empty Resource
			Resource resource_cloned = new Resource();
			BeanUtils.copyProperties(resource_o,resource_cloned);
			try {
				resource_cloned.setDateModified(this.getDateTimeNow(app_zone));
				resource_cloned.setResourceAccess(new ResourceAccess());
				resource_cloned.setPresentation(new Presentation());
				resource_cloned.getStatus().setInclMultimedia(-1);
				resource_cloned.getStatus().setInclPresentation(-1);
				resource_cloned.setIdentity(null);
				resource_cloned.setStorage(null);
				resource_cloned.setAccessPolicy("private");
				resource_cloned.setPartNumber(0);
				resource_cloned.setParentId(null);
				resource_cloned.setParts(false);
				resource_id = resourceService.create(resource_cloned);
				String[] attr = {"msg_type", "msg_val"};
				String[] values = {"alert-success", "?? ???????????????????? ??????????????????????!"};
				setFlashAttributes(request, response, attr, values);
			}
			catch (Exception e) {
				logger.error("Error copying Resource" + e.getMessage());
				String[] attr = {"msg_type","msg_val"};
				String[] values = {"alert-danger", "???????????????? ???????? ?????????????????? ?????? ??????????????????????!"};
				setFlashAttributes(request, response, attr, values);
			}
			view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
					request.getServletPath()).path("?id=" + resource_id).build().toUriString();
			return view;
		}
		if (action != null && (action.equals("clone")) ) {
				Resource resource_o = resourceService.findById(resource_id); //get Resource from Database or return empty Resource
				try {
					Resource resource_cloned = new Resource();
					BeanUtils.copyProperties(resource_o,resource_cloned);

					resource_cloned.setDateModified(this.getDateTimeNow(app_zone));
					resource_cloned.setResourceAccess(new ResourceAccess());
					resource_cloned.setPresentation(new Presentation());
					resource_cloned.getStatus().setInclMultimedia(-1);
					resource_cloned.getStatus().setInclPresentation(-1);
					resource_cloned.setIdentity(null);
					resource_cloned.setStorage(null);
					resource_cloned.setAccessPolicy("private");
					/* Save new Resource */
					if (resource_cloned.isParts()) {
						//get the last part of all related items
						List<Resource> related_parts;
						if (type.equals("COURSE")) {
							related_parts = resourceService.findRelatedCourseResources(resource_o, null);
						}
						else {
							related_parts = resourceService.findRelatedEventResourcesByEventId(resource_o.getEvent().getId(),null);
						}
						//last part is in the last position of the list -> because are sorted by partNumber
						Resource last_part = related_parts.get(related_parts.size()-1);
						int old_part_number = last_part.getPartNumber();

						int new_part_number = old_part_number+1;
						resource_cloned.setPartNumber(new_part_number);
						if (resource_o.getParentId() == null) { resource_cloned.setParentId(resource_o.getId());}
						resource_id = resourceService.create(resource_cloned);
						String[] attr = {"msg_type", "msg_val"};
						String[] values = {"alert-success", "?? ???????????????????? ??????????????????????!"};
						setFlashAttributes(request, response, attr, values);
					}
					else {
						resource_id = resourceService.create(resource_cloned);
						String[] attr = {"msg_type", "msg_val"};
						String[] values = {"alert-warning", "?? ???????????????????? ?????? ?????????? ?????????? ????????????! ?????? ???????????? ???? ?????????????????? ?????? ??????????"};
						setFlashAttributes(request, response, attr, values);
					}

				}
				catch (Exception e) {
					logger.error("Error cloning Resource" + e.getMessage());
					String[] attr = {"msg_type","msg_val"};
					String[] values = {"alert-danger", "???????????????? ???????? ?????????????????? ?????? ??????????????????????!"};
					setFlashAttributes(request, response, attr, values);
				}

				view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
					request.getServletPath()).path("?id=" + resource_id).build().toUriString();
				return view;
		}
		else if (action != null && action.equals("mm_delete") ) {
			try {
				  	resourceService.removeResourceMultimedia(resource_id);
					String[] attr = {"msg_type","msg_val"};
					String[] values = {"alert-success", "???? ???????????? ?????????????????? ????????????????????!"};
					setFlashAttributes(request, response, attr, values);
			}
			catch (Exception e) {
					logger.error("Error removing Resource multimedia" + e.getMessage());
					String[] attr = {"msg_type","msg_val"};
					String[] values = {"alert-danger", "???????????????? ?????? ???????????????? ?????? ??????????????????!"};
					setFlashAttributes(request, response, attr, values);
			}

			view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
					request.getServletPath()).path("?id=" + resource_id).build().toUriString();
			return view;
		}
		else if (action != null && action.equals("delete_pp") ) {
			try {
					resourceService.removeResourcePresentation(resource_id);
					String[] attr = {"msg_type","msg_val"};
					String[] values = {"alert-success", "?? ???????????????????? ????????????????????!"};
					setFlashAttributes(request, response, attr, values);
			}
			catch (Exception e) {
					logger.error("Error removing Resource Presentation" + e.getMessage());
					String[] attr = {"msg_type","msg_val"};
					String[] values = {"alert-danger", "???????????????? ?????? ???????????????? ?????? ??????????????????????!"};
					setFlashAttributes(request, response, attr, values);
			}
			view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath()
					+  request.getServletPath()).path("?id=" + resource_id).build().toUriString();

			return view;
		}
		else if (action != null && (action.equals("publish") || action.equals("unpublish"))) {
			try {
					String state = "public";
					if (action.equals("unpublish")) {
						state = "private";
					}
					resourceService.updateAccessPolicy(resource_id,state);
					String[] attr = {"msg_type","msg_val"};
					String[] values = {"alert-success", "?? ?????????????????? ?????? ?????????????????????? ????????????!"};
					setFlashAttributes(request, response, attr, values);
			}
			catch (Exception e) {
				logger.error("Error altering resource state" + e.getMessage());
				String[] attr = {"msg_type","msg_val"};
				String[] values = {"alert-danger", "???????????????? ?????? ???????????? ????????????????????!"};
				setFlashAttributes(request, response, attr, values);
			}
			view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath()
					+  request.getServletPath()).path("?id=" + resource_id).build().toUriString();

			return view;
		}
		resource.setType(type);
		resourceRegistrationValidator.validate(resource, bindingResult);

		Resource resource_o = resourceService.findById(resource_id); //get Resource from Database or return empty Resource

		/* Update: Date Modified */
		resource.setDateModified(this.getDateTimeNow(app_zone));
		/* Update Editor if Needed */
		OoUserDetails ooUserDetails = (OoUserDetails) authentication.getPrincipal();
		if (resource.getEditor() == null || !ooUserDetails.getId().equals(resource.getEditor().getId())) {
			OpUser opUser = opUserService.findById(ooUserDetails.getId());
			Person editor = opUserService.getPersonFromOpUser(opUser);
			resource.setEditor(editor);
		}
		//License>>
		if (resource.getLicense() == null || resource.getLicense().equals("")) { // set default value
			resource.setLicense(default_license);
		}
		//Status+ResourceAccess+Presentation
		if (resource.getId() == null || resource.getId().trim().equals("")) {
			ResourceStatus status = new ResourceStatus(-1,-1,"EDITOR");
			resource.setStatus(status);
			ResourceAccess resourceAccess = new ResourceAccess();
			resource.setResourceAccess(resourceAccess);
			resource.setPresentation(new Presentation());
		}
		else {
			resource.setStatus(resource_o.getStatus());
			resource.setResourceAccess(resource_o.getResourceAccess());
			resource.setIdentity(resource_o.getIdentity());
			resource.setAccessPolicy(resource_o.getAccessPolicy());
			//Presentation
			if (resource_o.getPresentation() != null) {
				resource.setPresentation(resource_o.getPresentation());
			}
			//parentId
			resource.setParentId(resource_o.getParentId());
			//Real Editing
			if (resource_o.getRteStatus() != null) {
				resource.setRteStatus(resource_o.getRteStatus());
			}
			if (resource_o.getRealEditingPresentation() != null) {
				resource.setRealEditingPresentation(resource_o.getRealEditingPresentation());
			}
		}

		if (bindingResult.hasErrors()) {
			// create a flashmap
			FlashMap flashMap = new FlashMap();
			// store the message
			flashMap.put("msg_val", "?? ???????????????????? ??????????????! ???????????????? ?????????????????? ?????? ?????????? ??????????????????");
			flashMap.put("msg_type", "alert-danger");
			flashMap.put(BINDING_RESULT_NAME, bindingResult);
			flashMap.put(ATTRIBUTE_NAME, resource);
			// create a flashMapManager with `request`
			FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
			// save the flash map data in session with flashMapManager
			if (flashMapManager != null) {
				flashMapManager.saveOutputFlashMap(flashMap, request, response);
			}
			if (resource.getId() != null && !resource.getId().equals("")) {
				view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
									 request.getServletPath()).path("?id=" + resource_id).build().toUriString();
			}
			else {
				view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
									request.getServletPath()).build().toUriString();
			}
		} else {
			if (type.equals("COURSE")) {
				//Update Supervisor
				String supervisor_id = resource.getSupervisor().getId();
				OpUser supervisor = opUserService.findById(supervisor_id);
				Person p_s = opUserService.getPersonFromOpUser(supervisor);
				resource.setSupervisor(p_s);
				//Course
				String course_id = resource.getCourse().getId();
				Course course = courseService.findById(course_id);
				resource.setCourse(course);
				resource.setDepartment(new Unit(StructureType.DEPARTMENT, course.getDepartment().getId(), course
						.getDepartment().getTitle()));
				resource.setInstitution(course.getInstitutionId());
				resource.setSchool(course.getSchoolId());
			}
			else  {
				//Scheduled Event
				String event_id = resource.getEvent().getId();
				ScheduledEvent scheduledEvent = scheduledEventService.findById(event_id);
				resource.setEvent(scheduledEvent);
				resource.setCategories(scheduledEvent.getCategories()); // important: otherwise you will need to save all older scheduledEvents
				if (scheduledEvent.getResponsiblePerson() != null && scheduledEvent.getResponsiblePerson().getId() != null) {
					//Update Supervisor
					String supervisor_id = scheduledEvent.getResponsiblePerson().getId();
					OpUser supervisor = opUserService.findById(supervisor_id);
					Person p_s = opUserService.getPersonFromOpUser(supervisor);
					resource.setSupervisor(p_s);
					Unit unit = scheduledEvent.getResponsiblePerson().getDepartment();
					resource.setDepartment(new Unit(StructureType.DEPARTMENT,unit.getId(), unit.getTitle()));
				}
				resource.setInstitution(defaultInstitution.getId());
			}
			/* part Number */
			resource.setParts(resource.getPartNumber() != 0);
			/* Save or Update Resource */
			if (resource_id == null || resource.getId().trim().equals("")) {
				resource_id = resourceService.create(resource);
			}
			else {
				resourceService.update(resource);
			}
			String[] attr = {"msg_type","msg_val"};
			String[] values = {"alert-success","?? ???????????????????? ????????????????????????!"};
			this.setFlashAttributes(request, response, attr, values);

			/* Redirect to GET Resource page */
			view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
					request.getServletPath()).path("?id=" + resource_id).build().toUriString();
		}
		return view;

	}

	@GetMapping(value = {"admin/download-video"})
	public void downloadVideoFile(@RequestParam(value = "id") String id, HttpServletResponse response)  {

		Resource resource = resourceService.findById(id);
		String gen_filename = "";
		if (resource.getType().equals("COURSE")) {
			gen_filename += resource.getCourse().getTitle();
		}
		else {
			gen_filename += resource.getEvent().getTitle();
		}
		if (resource.isParts()) {
			gen_filename += "-part-" + resource.getPartNumber();
		}
		String original_filename = resource.getResourceAccess().getFileName();
		LocalDateTime ldt = LocalDateTime.ofInstant(resource.getDate(),ZoneId.of(app_zone));
		gen_filename += "-" + ldt;
		String latin_gen_filename = GreekChar.translate(gen_filename);
		latin_gen_filename = FileNameCleaner.cleanFileName(latin_gen_filename)  + FilenameUtils.getExtension(original_filename);

		String dataDirectory = resourceUtils.getStreamingFolder(resource).toString();

		this.DownloadFile(dataDirectory, original_filename,latin_gen_filename, response);
	}

	@GetMapping(value = {"admin/download-presentation"})
	public void downloadPresentationInZipFile(@RequestParam(value = "id") String id, HttpServletResponse response)  {

		Resource resource = resourceService.findById(id);
		String gen_filename = "";
		if (resource.getType().equals("COURSE")) {
			gen_filename += resource.getCourse().getTitle();
		}
		else {
			gen_filename += resource.getEvent().getTitle();
		}
		if (resource.isParts()) {
			gen_filename += "-part-" + resource.getPartNumber();
		}
		LocalDateTime ldt = LocalDateTime.ofInstant(resource.getDate(),ZoneId.of(app_zone));
		gen_filename += "-" + ldt;
		String latin_gen_filename = GreekChar.translate(gen_filename);
		latin_gen_filename = FileNameCleaner.cleanFileName(latin_gen_filename) + ".zip";

		String dataDirectory = resourceUtils.getMultimediaFolder(resource).toString();

		String zipFilePath = dataDirectory + latin_gen_filename;
		//Delete previously create zip file
		File zipFile = new File(zipFilePath);
		if (zipFile.isFile()) FileUtils.deleteQuietly(zipFile);

		Presentation presentation = resource.getPresentation();

		//Create & send to download
		List<String> inFiles = new ArrayList<>();
		Slideshow slidesync = new Slideshow();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		slidesync.setDate(sdf.format(cal.getTime()));
		sdf = new SimpleDateFormat("hh:mm:ss");
		slidesync.setNowtime(sdf.format(cal.getTime()));
		Slideshow.Slides legacy_slides = new Slideshow.Slides();
		legacy_slides.setDescription(gen_filename);
		//Add Directory First
		inFiles.add("slides/");
		inFiles.add("slideSync.xml");

		for (Slide slide: presentation.getSlides()) {

			Slideshow.Slides.Slide slideshow_slide = new Slideshow.Slides.Slide();
			slideshow_slide.setUrl(slide.getUrl());
			slideshow_slide.setTime(slide.getTime());
			slideshow_slide.setTitle(slide.getTitle());
			legacy_slides.getSlide().add(slideshow_slide);
			String slideFileName = slide.getUrl();
			inFiles.add(slideFileName);
		}
		slidesync.setSlides(legacy_slides);

		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance("org.opendelos.legacydomain.slidesync");
			// Marshal Object to the Document
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			OutputStream os = new FileOutputStream(dataDirectory + "slideSync.xml");
			marshaller.marshal(slidesync, os );
			os.close();
		}
		catch (JAXBException | IOException e) {
			logger.error("Presentation export error:" + e.getMessage());
		}


		ZipUtility zipUtility = new ZipUtility(dataDirectory);
		try {
			zipUtility.zipThese(zipFilePath, inFiles);
		}
		catch (IOException e) {
			logger.error("Presentation export to ZIP error:" + e.getMessage());
		}

		File slideSyncFile = new File(dataDirectory + "slideSync.xml");
		if (slideSyncFile.exists()) {
			FileUtils.deleteQuietly(slideSyncFile);
		}

		if (zipFile.isFile()) {
			this.DownloadFile(dataDirectory,latin_gen_filename,latin_gen_filename,response);
		}
	}

	private void DownloadFile(String dataDirectory, String fileName, String wanted_name, HttpServletResponse response) {

		Path file = Paths.get(dataDirectory, fileName);
		if (Files.exists(file))
		{
			response.setContentType("application/force-download");
			response.addHeader("Content-Disposition", "attachment; filename=" + wanted_name);
			try
			{
				//logger.info("Downloading file:" + wanted_name);
				Files.copy(file, response.getOutputStream());
				response.getOutputStream().flush();
			}
			catch (IOException ex) {
				logger.error("Download File error:" + wanted_name);
			}
		}
	}

	private void SetModelAttributes(Model model,Locale locale, String editorId, String currentAcademicYear) {

		//AcademicYears && Current Academic Year
		List<String> ayList = institutionService.getAvailableAcademicCalendarYears(defaultInstitution.getId());
		List<String> ayListText = new ArrayList<>();
		for (String at: ayList) {
			int next_ay = Integer.parseInt(at) + 1;
			ayListText.add(at + " - " + next_ay);
		}
		model.addAttribute("ayCurr", currentAcademicYear);
		model.addAttribute("ayList", ayList);
		model.addAttribute("ayListText", ayListText);
		//Periods
		HashMap<String,String> periods_hash = new HashMap<>();
		String[] periods = multilingualServices.getValue("Period.keys",locale).split(",");
		for (String period : periods) {
			periods_hash.put(period, multilingualServices.getValue(period, locale));
		}
		model.addAttribute("pdList", periods_hash);
		//Categories
		HashMap<String, List<String>> catList = optionServices.getSortedCategories(locale);
		model.addAttribute("catList", catList);
		//Languages
		String[] langList = optionServices.getLanguages(locale);
		model.addAttribute("langList",langList);
		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());
		//Licenses
		String[] licenseList = optionServices.getLicenses(locale);
		model.addAttribute("licenseList", licenseList);
		//Supervisors
//		List<OpUser> staffMemberList = opUserService.getAuthorizedStaffMembersByUserId(editorId);
//		model.addAttribute("svList", staffMemberList);
		//Editor
		model.addAttribute("editorId", editorId);
		//StreamingBaseUrl
		model.addAttribute("streamingBaseUrl", resourceUtils.getStreamingBaseWebPath());
		//MediaBaseurl
		model.addAttribute("mediaBaseUrl", resourceUtils.getMultimediaBaseWebPath());
		// Date Time Now
		model.addAttribute("localDateTime", LocalDateTime.now());
	}

	private void setStorageFolders(Model model, Resource resource) {

		int inclPresentation = resource.getStatus().getInclPresentation();
		int inclMultimedia	 = resource.getStatus().getInclMultimedia();

		if (inclPresentation == -1 && inclMultimedia == -1) {
			Calendar rightNow = Calendar.getInstance();
			model.addAttribute("media_folder", (rightNow.get(Calendar.MONTH)+1) + "-" + rightNow.get(Calendar.YEAR) + "/" + resource.getId());
			model.addAttribute("video_folder", (rightNow.get(Calendar.MONTH)+1) + "-" + rightNow.get(Calendar.YEAR) + "/" + resource.getId());
		}
		else if (inclPresentation == 1) {
			model.addAttribute("media_folder", resource.getPresentation().getFolder());
			model.addAttribute("video_folder", resource.getPresentation().getFolder());
		}
		else if (inclMultimedia == 1) {
			model.addAttribute("media_folder", resource.getResourceAccess().getFolder());
			model.addAttribute("video_folder", resource.getResourceAccess().getFolder());
		}
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

	private Resource createEmptyResource(String type, String currentAcademicYear) {

		Resource resource = new Resource();
		resource.setType(type);
		resource.setStatus(new ResourceStatus(-1,-1,"EDITOR"));
		ResourceAccess resourceAccess = new ResourceAccess();
		resource.setResourceAccess(resourceAccess);
		resource.setAccessPolicy("private");
		resource.setAcademicYear(currentAcademicYear);
		return resource;
	}

	private Instant getDateTimeNow(String zone) {

		ZoneId z = ZoneId.of(zone);
		LocalDateTime ldt = LocalDateTime.now();
		ZonedDateTime zdt = ldt.atZone(z);

		return  zdt.toInstant();
	}



}
