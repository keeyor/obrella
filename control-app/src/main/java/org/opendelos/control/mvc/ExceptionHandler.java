/* 
     Author: Michael Gatzonis - 25/3/2021 
     obrella
*/
package org.opendelos.control.mvc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@Slf4j
public class ExceptionHandler {


	@org.springframework.web.bind.annotation.ExceptionHandler(Throwable.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String exception(final Throwable throwable, final Model model) {
		log.error("Exception during execution of SpringSecurity application", throwable);
		String errorMessage = (throwable != null ? throwable.getMessage() : "Unknown error");
		model.addAttribute("errorMessage", errorMessage);

		String stacktrace ="";
		if (throwable != null) {
			stacktrace = ExceptionUtils.getStackTrace(throwable);
		}
		model.addAttribute("stackTrace", stacktrace);
		return "error";
	}

}
