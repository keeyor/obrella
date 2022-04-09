/* 
     Author: Michael Gatzonis - 26/9/2020 
     live
*/
package org.opendelos.live.conf;

import java.util.Locale;

import org.opendelos.live.services.structure.InstitutionService;
import org.opendelos.model.structure.Institution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
@EnableCaching
public class MvcConfig implements WebMvcConfigurer {

	@Value("${default.institution.identity}")
	String institution_identity;

	private final InstitutionService institutionService;

	@Autowired
	public MvcConfig(InstitutionService institutionService) {
		this.institutionService = institutionService;
	}


	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("lang");
		registry.addInterceptor(localeChangeInterceptor);
		//MG 15-12-2020 : to avoid warning test
		//registry.addInterceptor(new HttpsConfig());
	}

	@Bean
	public LocaleResolver localeResolver() {
		CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
		cookieLocaleResolver.setDefaultLocale(Locale.forLanguageTag("el"));
		return cookieLocaleResolver;
	}

	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasenames("i18n/messages","i18n/options", "i18n/errors", "i18n/richlecture-messages");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}


	@Bean
	public Institution defaultInstitution() {
		return institutionService.findByIdentity(institution_identity);
	}
	@Bean
	public String currentAcademicYear() {
		return institutionService.getCurrentAcademicYear();
	}

}
