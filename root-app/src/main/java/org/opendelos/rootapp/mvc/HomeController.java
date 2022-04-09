/* 
     Author: Michael Gatzonis - 11/2/2022 
     Balloon
*/
package org.opendelos.rootapp.mvc;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping(value = "/")
	public String getLiveHomePage(final Model model, Locale locale) {


		model.addAttribute("default.institution.title", "Εθνικόν & Καποδιστριακόν Πανεπιστήμιον Αθηνών");
		return "home";
	}
}
