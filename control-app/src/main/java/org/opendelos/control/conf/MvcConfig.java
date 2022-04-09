/* 
     Author: Michael Gatzonis - 26/9/2020 
     live
*/
package org.opendelos.control.conf;


import java.util.Locale;

import org.opendelos.control.services.structure.InstitutionService;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.users.ActiveUserStore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
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


	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/admin/video-editor/editor_play_edited").setViewName("admin/video-editor/editor_play_edited");
		registry.addViewController("/admin/synchronizer").setViewName("admin/html5synchronizer/synchronizer");
		registry.addViewController("/admin/html5synchronizer/synchronizer_help").setViewName("admin/html5synchronizer/synchronizer_help");
		registry.addViewController("/login").setViewName("login");
		registry.addViewController("/ahome").setViewName("ahome");
		registry.addViewController("/403").setViewName("403");
		registry.addViewController("/404").setViewName("404");
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
		messageSource.setBasenames("i18n/messages","i18n/options", "i18n/errors", "i18n/richlecture-messages", "i18n/draseis");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

	@Bean
	public ActiveUserStore activeUserStore(){
		return new ActiveUserStore();
	}

	@Bean
	public Institution defaultInstitution() {
		return institutionService.findByIdentity(institution_identity);
	}
	@Bean
	public String currentAcademicYear() {
		return institutionService.getCurrentAcademicYear();
	}



	/*@Bean
	public RedisCacheConfiguration cacheConfiguration() {
		return RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofMinutes(60))
				.disableCachingNullValues()
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)));
	}
	@Bean
	public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
		return (builder) -> builder
				.withCacheConfiguration("departments",
						RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)))
				.withCacheConfiguration("users",
						RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)))
				.withCacheConfiguration("classrooms",
						RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)))
				.withCacheConfiguration("courses",
						RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)))
				.withCacheConfiguration("scheduledEvents",
						RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)))
				.withCacheConfiguration("study_programs",
						RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)))
				.withCacheConfiguration("streamers",
						RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)));



	}*/
}
