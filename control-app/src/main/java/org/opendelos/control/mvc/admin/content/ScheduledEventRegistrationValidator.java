/* 
     Author: Michael Gatzonis - 11/1/2018 
     OpenDelosDAC
*/
package org.opendelos.control.mvc.admin.content;

import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.dtos.ScheduledEventDto;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ScheduledEventRegistrationValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return ScheduledEventDto.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        ScheduledEventDto scheduledEventDto = (ScheduledEventDto) o;

         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", "ScheduledEvent.title.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "startDate", "ScheduledEvent.date.empty");

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "area", "ScheduledEvent.area.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "type", "ScheduledEvent.type.empty");

        if (scheduledEventDto.getArea().equals("ea_uas")) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "categories", "ScheduledEvent.categories.empty");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "responsiblePerson.id", "ScheduledEvent.supervisor.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "responsibleUnitIds", "ScheduledEvent.responsibleUnits.empty");

    }
}
