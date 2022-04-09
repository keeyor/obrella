/* 
     Author: Michael Gatzonis - 1/16/2019 
     OpenDelosDAC
*/
package org.opendelos.live.services.i18n;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class MultilingualServices implements MessageSourceAware {

    private MessageSource messageSource;

    public void setMessageSource(@NonNull MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getValue(String code, Object[] objects, Locale locale) {
        return messageSource.getMessage(code,objects,locale);
    }
    public String getValue(String code, Locale locale) {
        return messageSource.getMessage(code,null,locale);
    }

}
