/* 
     Author: Michael Gatzonis - 4/3/2019 
     OpenDelosDAC
*/
package org.opendelos.liveapp.mvc;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LocaleController {

    @RequestMapping(value = "/locale/change", method = RequestMethod.POST, produces =  "application/json")
    public String ChangeLocale(@RequestParam String localeCode, HttpServletRequest request)  {

        String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }
}
