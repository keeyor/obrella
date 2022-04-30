/* 
     Author: Michael Gatzonis - 7/12/2020 
     live
*/
package org.opendelos.control.mvc.system;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.opendelos.control.services.opUser.OpUserService;
import org.opendelos.control.services.structure.DepartmentService;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.model.users.UserAccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
public class UserEditController {

	@ModelAttribute("section")
	public String getAdminSection() {
		return "admin_system";
	}

	@Value("${app.default.license}")
	String default_license;
	@Value("${app.zone}")
	String app_zone;

	@Autowired
	Institution defaultInstitution;
	@Autowired
	String currentAcademicYear;

	private static final String ATTRIBUTE_NAME = "OpUser";
	private static final String BINDING_RESULT_NAME = "org.springframework.validation.BindingResult." + ATTRIBUTE_NAME;


	private final OpUserService opUserService;
	private final DepartmentService departmentService;
	private final OpUserRegistrationValidator opUserRegistrationValidator;

	@Autowired
	public UserEditController(OpUserService opUserService, DepartmentService departmentService,OpUserRegistrationValidator opUserRegistrationValidator) {
		this.opUserService = opUserService;
		this.departmentService = departmentService;
		this.opUserRegistrationValidator = opUserRegistrationValidator;
	}

	@GetMapping(value = {"admin/system/user-editor"})
	public String resourceUser(final Model model, @RequestParam(value = "id",  required = false) String id, HttpServletRequest request)  {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		//Get user search history
		if (request.getSession().getAttribute("userlist_search_history") != null) {
			model.addAttribute("userlist_search_history", request.getSession().getAttribute("userlist_search_history"));
		}
		else {
			model.addAttribute("userlist_search_history", "users");
		}

		OpUser adminUser;
		String role= "NOT_SET";
		boolean isStaffMember = false;
		StringBuilder authorities = new StringBuilder();
		if (!model.containsAttribute(BINDING_RESULT_NAME)) {
			if (id != null) {
				adminUser = opUserService.findById(id);
				//Rights
				UserAccess.UserRights rights = adminUser.getRights();
				if (rights.getIsSa()) {
					role = "SA";
				}
				if ((rights.getUnitPermissions() != null)
						&& (rights.getUnitPermissions().size() > 0)
						&& (rights.getUnitPermissions().get(0).getUnitType().equals(UserAccess.UnitType.INSTITUTION))) {
					role = "INSTITUTION_MANAGER";
				}
				else if (rights.getUnitPermissions() != null && rights.getUnitPermissions().size() > 0) {
					role = "MANAGER";
				}
				else if (rights.getCoursePermissions() != null && rights.getCoursePermissions().size() > 0) {
					role = "SUPPORT";
				}
				//Authorities
				for (UserAccess.UserAuthority userAuthority: adminUser.getAuthorities()) {
					authorities.append(userAuthority.value());
					if (userAuthority.equals(UserAccess.UserAuthority.STAFFMEMBER)) {
						isStaffMember = true;
					}
				}
				adminUser.setPassword("ISSET");
			}
			else {
				adminUser = this.createEmptyAdminUser();
			}
			model.addAttribute("OpUser", adminUser);
		}
		model.addAttribute("role", role);
		model.addAttribute("authorities",authorities.toString());
		model.addAttribute("isStaffMember",isStaffMember);
		model.addAttribute("id", id);
		model.addAttribute("defaultInstitution", defaultInstitution);

		return "admin/system/user-editor";
	}

	@PostMapping(value = {"admin/system/user-editor" ,"admin/system/user-editor/"})
	public String SearchPost(@Valid @ModelAttribute("OpUser") OpUser opUser, @RequestParam(value = "action", required = false) String action,
			final BindingResult bindingResult,HttpServletRequest request, HttpServletResponse response) throws Exception {

		final String view;

		String opUser_id = opUser.getId();
		if (action != null && (action.equals("delete")) ) {
			if (opUser_id != null && !opUser_id.equals("")) {
				//TODO: Check Delete constrains
				opUserService.delete(opUser_id);

				String[] attr = {"msg_type", "msg_val"};
				String[] values = {"alert-success", "Ο χρήστης διαγράφηκε!",};
				setFlashAttributes(request, response, attr, values);

				String userlist_search_history = "users";
				if (request.getSession().getAttribute("userlist_search_history") != null) {
					userlist_search_history = (String) request.getSession().getAttribute("userlist_search_history");
				}
				view = "redirect:" + userlist_search_history;
				return view;
			}
		}
		opUserRegistrationValidator.validate(opUser, bindingResult);

		if (opUser_id != null &&  !opUser_id.trim().equals("")) {
			OpUser opUser_o = opUserService.findById(opUser_id); //get Resource from Database or return empty Resource

			if (opUser_o != null) {
				if (opUser.getPassword() == null || opUser.getPassword().trim().equals("ISSET")) {
					opUser.setPassword(opUser_o.getPassword());
				}
				if (opUser.getEduPersonPrimaryAffiliation() == null || opUser.getEduPersonPrimaryAffiliation()
						.equals("")) {
					opUser.setEduPersonPrimaryAffiliation(opUser_o.getEduPersonPrimaryAffiliation());
				}
				if (opUser.getEduPersonAffiliation() == null || opUser.getEduPersonAffiliation().isEmpty()) {
					opUser.setEduPersonAffiliation(opUser_o.getEduPersonAffiliation());
				}
				if (opUser.getAuthorities() == null || opUser.getAuthorities().isEmpty()) {
					opUser.setAuthorities(opUser_o.getAuthorities());
				}
				if (opUser.getRights() == null) {
					opUser.setRights(opUser_o.getRights());
				}
				if (opUser.getCourses() == null || opUser.getCourses().isEmpty()) {
					opUser.setCourses(opUser_o.getCourses());
				}
				opUser.setLastLogin(opUser_o.getLastLogin());
				opUser.setActive(opUser_o.isActive());
			}
		}
		if (bindingResult.hasErrors()) {
			// create a flashmap
			FlashMap flashMap = new FlashMap();
			// store the message
			flashMap.put("msg_val", "Η αποθήκευση απέτυχε! Διορθώστε τα λάθη στη φόρμα εισαγωγής");
			flashMap.put("msg_type", "alert-danger");
			flashMap.put(BINDING_RESULT_NAME, bindingResult);
			flashMap.put(ATTRIBUTE_NAME, opUser);
			// create a flashMapManager with `request`
			FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
			// save the flash map data in session with flashMapManager
			if (flashMapManager != null) {
				flashMapManager.saveOutputFlashMap(flashMap, request, response);
			}
			if (opUser.getId() != null && !opUser.getId().equals("")) {
				view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
									 request.getServletPath()).path("?id=" + opUser_id).build().toUriString();
			}
			else {
				view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
									request.getServletPath()).build().toUriString();
			}
		} else {
			String departmentId = opUser.getDepartment().getId();
			Department opUser_department = departmentService.findById(departmentId);
			Unit unit = new Unit(StructureType.DEPARTMENT,departmentId,opUser_department.getTitle());
			opUser.setDepartment(unit);
			/* Save or Update Resource */
			if (opUser_id == null || opUser_id.trim().equals("")) {

				opUser.setId(null); // !important -> avoid null String
				opUser.setEduPersonPrimaryAffiliation("staff");

				List<String> opUser_edurPersonAffiliations = new ArrayList<>();
				opUser_edurPersonAffiliations.add("staff");
				opUser.setEduPersonAffiliation(opUser_edurPersonAffiliations);

				List<UserAccess.UserAuthority> opUser_authorities = new ArrayList<>();
				opUser_authorities.add(UserAccess.UserAuthority.SUPPORT);
				opUser.setAuthorities(opUser_authorities);

				UserAccess.UserRights userRights = new UserAccess.UserRights();
				userRights.setIsSa(false);
				userRights.setCoursePermissions(null);
				userRights.setUnitPermissions(null);
				opUser.setRights(userRights);

				BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
				String encoded_pass = bCryptPasswordEncoder.encode(opUser.getPassword());
				opUser.setPassword(encoded_pass);

				opUser_id = opUserService.create(opUser);
			}
			else {
					//Update password if changed!
					/*if (!opUser.getPassword().trim().equals("")) {
						BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
						String encoded_pass = bCryptPasswordEncoder.encode(opUser.getPassword());
						opUser.setPassword(encoded_pass);
					}*/
					opUserService.update(opUser);
			}

			String[] attr = {"msg_type","msg_val"};
			String[] values = {"alert-success","Η καταχώρηση αποθηκεύτηκε!"};
			this.setFlashAttributes(request, response, attr, values);

			/* Redirect to GET Resource page */
			view = "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
					request.getServletPath()).path("?id=" + opUser_id).build().toUriString();
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

	private OpUser createEmptyAdminUser() {

		OpUser adminUser = new OpUser();
		adminUser.setId(null);
		adminUser.setIdentity(null);
		adminUser.setPassword("");

		List<String> courses = new ArrayList<>();
		adminUser.setCourses(courses);

		return adminUser;
	}

}
