/* 
     Author: Michael Gatzonis - 11/1/2018 
     OpenDelosDAC
*/
package org.opendelos.control.mvc.content;

import org.opendelos.model.resources.Resource;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ResourceRegistrationValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Resource.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        Resource resource = (Resource) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", "Resource.title.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "date", "Resource.date.empty");

        if (resource.getType().equals("COURSE")) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "supervisor.id", "Resource.supervisor.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "course.id", "Resource.course.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "categories", "Resource.categories.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "period", "Resource.period.empty");
        }
        else if (resource.getType().equals("EVENT")) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "event.id", "Resource.event.empty");
        }
    }
}
