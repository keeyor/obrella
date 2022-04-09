/* 
     Author: Michael Gatzonis - 11/1/2018 
     OpenDelosDAC
*/
package org.opendelos.vodapp.mvc;

import java.util.regex.Pattern;

import org.opendelos.model.common.Feedback;
import org.opendelos.model.resources.Resource;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class FeedbackValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Resource.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        Feedback feedback = (Feedback) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "Feedback.name.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "Feedback.email.empty");
        String emailAddress = feedback.getEmail();
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        if (!patternMatches(emailAddress,regexPattern)) {
            errors.rejectValue("email", "Feedback.email.invalid");
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "feedback", "Feedback.feedback.empty");
    }

    private static boolean patternMatches(String emailAddress, String regexPattern) {
        return Pattern.compile(regexPattern)
                .matcher(emailAddress)
                .matches();
    }
}
