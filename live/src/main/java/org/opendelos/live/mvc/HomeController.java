/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.live.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping(value = "/")
	public String getHomePage() {

		return "home";
	}
}
