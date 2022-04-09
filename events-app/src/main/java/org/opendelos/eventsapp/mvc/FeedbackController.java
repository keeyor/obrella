/* 
     Author: Michael Gatzonis - 23/3/2022 
     obrella
*/
package org.opendelos.eventsapp.mvc;

import java.text.StringCharacterIterator;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.opendelos.eventsapp.services.system.FeedbackService;
import org.opendelos.model.common.Feedback;

import org.springframework.beans.factory.annotation.Autowired;
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
public class FeedbackController {

	private static final String ATTRIBUTE_NAME = "Feedback";
	private static final String BINDING_RESULT_NAME = "org.springframework.validation.BindingResult." + ATTRIBUTE_NAME;

	private final FeedbackService feedbackService;
	private final FeedbackValidator feedbackValidator;

	@Autowired
	public FeedbackController(FeedbackService feedbackService, FeedbackValidator feedbackValidator) {
		this.feedbackService = feedbackService;
		this.feedbackValidator = feedbackValidator;
	}

	@GetMapping(value = "/feedback")
	public String GetCommentsForm(final Model model,@RequestParam(value = "id",  required = false) String id, Locale locale) {

		Feedback feedback;
		boolean submitted = false;
		if (!model.containsAttribute(BINDING_RESULT_NAME)) {
			if (id != null) {
				submitted = true;
				feedback = feedbackService.findById(id);
				if (feedback == null) {
					feedback = new Feedback();
					submitted = false;
				}
			}
			else {
				feedback = new Feedback();
			}
			model.addAttribute("Feedback", feedback);
		}

		model.addAttribute("submitted",submitted);
		//Locale
		model.addAttribute("localeData", locale.getDisplayName());
		model.addAttribute("localeCode", locale.getLanguage());
		model.addAttribute("id", id);

		return "feedback";
	}

	@PostMapping(value = "/feedback")
	public String PostComment(@Valid @ModelAttribute("Feedback") Feedback feedback, final BindingResult bindingResult,HttpServletRequest request, HttpServletResponse response) {


		feedbackValidator.validate(feedback, bindingResult);

		if (bindingResult.hasErrors()) {
			// create a flashmap
			FlashMap flashMap = new FlashMap();
			// store the message
			flashMap.put("msg_val", "Η καταχώρηση απέτυχε! Υπάρχουν ελλείψεις στη φόρμα");
			flashMap.put("msg_type", "alert-danger");
			flashMap.put(BINDING_RESULT_NAME, bindingResult);
			flashMap.put(ATTRIBUTE_NAME, feedback);
			// create a flashMapManager with `request`
			FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
			// save the flash map data in session with flashMapManager
			if (flashMapManager != null) {
				flashMapManager.saveOutputFlashMap(flashMap, request, response);
			}
			if (feedback.getId() != null && !feedback.getId().equals("")) {
				return  "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
						request.getServletPath()).path("?id=" + feedback.getId()).build().toUriString();
			}
			else {
				return "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(request.getContextPath() +
						request.getServletPath()).build().toUriString();
			}
		}
		else {
			String[] attr = {"msg_type", "msg_val"};
			String[] values = {"alert-success", "Η γνώμη σας καταχωρήθηκε. Ευχαριστούμε πολύ!"};
			setFlashAttributes(request, response, attr, values);

			feedback.setSite("events");

			//Encode values to prevent script injections!
			feedback.setName(HTMLEncode(feedback.getName()));
			feedback.setEmail(HTMLEncode(feedback.getEmail()));
			feedback.setFeedback(HTMLEncode(feedback.getFeedback()));

			String feedback_id = feedbackService.create(feedback);
			return "redirect:" + ServletUriComponentsBuilder.fromCurrentRequestUri()
					.replacePath(request.getContextPath() +
							request.getServletPath()).path("?id=" + feedback_id).build().toUriString();
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

	private String HTMLEncode(String aTagFragment) {
		final StringBuffer result = new StringBuffer();
		final StringCharacterIterator iterator = new
				StringCharacterIterator(aTagFragment);
		char character = iterator.current();
		while (character != StringCharacterIterator.DONE )
		{
			if (character == '<')
				result.append("&lt;");
			else if (character == '>')
				result.append("&gt;");
			else if (character == '\"')
				result.append("&quot;");
			else if (character == '\'')
				result.append("&#039;");
			else if (character == '\\')
				result.append("&#092;");
			else if (character == '&')
				result.append("&amp;");
			else {
				//the char is not a special one
				//add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}
}
