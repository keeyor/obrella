/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.control.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {



	@GetMapping(value = "/")
	public String getHomePage(final Model model, @RequestParam(value = "error", required = false) String error) {

		if (error != null) {
			model.addAttribute("error", "Bad_Credentials_Message");
		}
		return "home";
	}

	@RequestMapping(value = {"/login"})
	public String signIn(final Model model, @RequestParam(value = "error", required = false) String error) {

		if (error != null) {
			model.addAttribute("error", "Bad_Credentials_Message");
		}
		return "login";
	}

}
