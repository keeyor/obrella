/* 
     Author: Michael Gatzonis - 28/9/2020 
     live
*/
package org.opendelos.eventsapp.mvc;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class LiveController {

	@GetMapping(value = "/live")
	public String getLivePage(final Model model, Locale locale) throws ExecutionException, InterruptedException, UnsupportedEncodingException {

		Calendar time_start = Calendar.getInstance();
		Date startTime = time_start.getTime();
		model.addAttribute("page", "live");
		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());

		Calendar time_now = Calendar.getInstance();
		Date endTime = time_now.getTime();
		long diff = endTime.getTime() - startTime.getTime();
		model.addAttribute("PageLoadTime", diff);

		return "live";
	}


}
