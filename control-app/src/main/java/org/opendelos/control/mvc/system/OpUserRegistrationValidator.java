/* 
     Author: Michael Gatzonis - 11/1/2018 
     OpenDelosDAC
*/
package org.opendelos.control.mvc.system;

import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.Resource;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class OpUserRegistrationValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return OpUser.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        OpUser opUser = (OpUser) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "OpUser.name.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "affiliation", "OpUser.affiliation.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "uid", "OpUser.uid.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "department.id", "OpUser.department.empty");
        if (opUser.getId() == null || opUser.getId().trim().equals("")) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "OpUser.password.empty");
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "OpUser.email.empty");

        if (opUser.getPassword() != null && !opUser.getPassword().trim().equals("") && opUser.getPassword().length() < 8) {
            errors.rejectValue("password", "OpUser.password.length");
        }
    }
}
