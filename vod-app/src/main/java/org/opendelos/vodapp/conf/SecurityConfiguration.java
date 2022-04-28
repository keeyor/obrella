/* 
     Author: Michael Gatzonis - 26/9/2020 
     live
*/
package org.opendelos.vodapp.conf;

import java.util.Arrays;

import org.opendelos.vodapp.security.token.CustomTokenAuthenticationFilter;
import org.opendelos.vodapp.security.token.TokenAuthService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Order(4)
	@Configuration
	public static class DatabaseConfigurationAdapter extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			http
					.authorizeRequests()
					.antMatchers("/**").permitAll()
					.anyRequest().authenticated()
					.and().exceptionHandling()
					.and().csrf()
					.ignoringAntMatchers("/search","/api/v1/**")
					.and()
					.logout()//.deleteCookies("JSESSIONID")
					.permitAll()
					.and()
					.headers().disable();

		}
	}


	@Configuration
	@Order(5)
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
		protected void configure(HttpSecurity http) throws Exception {
			// WORKING::: >>>>
			http.antMatcher("/api/v1/embed/**").headers().disable(); // enable access to all domains to this url
			// LIMIT X-FRAME-OPTIONS to specified domains (can use wildcards)
			//http.headers().contentSecurityPolicy("frame-ancestors self eclass.uoa.gr dimos.med.uoa.gr");
		}
	}

	@Configuration
	@Order(1)
	public static class ApiTokenSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

		private final TokenAuthService tokenAuthService;

		public ApiTokenSecurityConfigurationAdapter(TokenAuthService tokenAuthService) {
			this.tokenAuthService = tokenAuthService;
		}
		protected void configure(HttpSecurity http) throws Exception {
			http
					.cors().and()
			 		.addFilterAfter(new CustomTokenAuthenticationFilter("/api/**",tokenAuthService), BasicAuthenticationFilter.class);
		}

		@Bean
		CorsConfigurationSource corsConfigurationSource()
		{
			CorsConfiguration configuration = new CorsConfiguration();
			configuration.setAllowedOrigins(Arrays.asList("*"));
			configuration.setAllowedMethods(Arrays.asList("GET"));
			configuration.setAllowedHeaders(Arrays.asList("*"));
			UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
			source.registerCorsConfiguration("/api/**", configuration);
			return source;
		}
	}
 }
