/* 
     Author: Michael Gatzonis - 11/1/2018 
     OpenDelosDAC
*/
package org.opendelos.control.mvc.admin.scheduler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.opendelos.model.scheduler.ScheduleDTO;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ScheduleRegistrationValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return ScheduleDTO.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        ScheduleDTO scheduleDTO = (ScheduleDTO) o;

        if (scheduleDTO.getType().equals("lecture")) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "supervisor.id", "Resource.supervisor.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "course.id", "Resource.course.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "classroom.id", "Resource.classroom.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "startTime", "Resource.time.empty");
            if (scheduleDTO.getRepeat().equals("regular")) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "dayOfWeek", "Resource.dayOfWeek.empty");
            }
            else if (scheduleDTO.getRepeat().equals("onetime")) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "date", "Resource.date.empty");
                try {
                    LocalDate.parse(scheduleDTO.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
                catch ( DateTimeParseException dtpe) {
                    errors.rejectValue("date", "Resource.date.badformat");
                }

            }
            if (scheduleDTO.getAccess() != null && scheduleDTO.getAccess().equals("password")) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "broadcastCode", "Resource.code.empty");
            }
            if (scheduleDTO.getDurationHours() == 0 && scheduleDTO.getDurationMinutes() == 0) {
                errors.rejectValue("durationHours", "Resource.duration.zero");
            }
        }
        else if (scheduleDTO.getType().equals("event")) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "scheduledEvent.id", "Resource.event.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "classroom.id", "Resource.classroom.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "startTime", "Resource.time.empty");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "date", "Resource.date.empty");
            if (scheduleDTO.getAccess() != null && scheduleDTO.getAccess().equals("password")) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "broadcastCode", "Resource.code.empty");
            }
            if (scheduleDTO.getDurationHours() == 0 && scheduleDTO.getDurationMinutes() == 0) {
                errors.rejectValue("durationHours", "Resource.duration.zero");
            }
        }

        if (!scheduleDTO.isBroadcast() && !scheduleDTO.isRecording()) {
            errors.rejectValue("broadcast", "Resource.atleast.one");
            errors.rejectValue("recording", "Resource.atleast.one");
        }
    }
}
