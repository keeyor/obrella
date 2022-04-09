/* 
     Author: Michael Gatzonis - 26/9/2020 
     live
*/
package org.opendelos.vodapp.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

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
			http
					.sessionManagement()
					.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
		}
	}

	@Configuration
	@Order(5)
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
		protected void configure(HttpSecurity http) throws Exception {
			// WORKING::: >>>>
			http.antMatcher("/api/v1/embed/**").headers().disable(); // enable access to all domains to this url
			// LIMIt X-FRAME-OPTIONS to specified domains (can use wildcards)
			//http.headers().contentSecurityPolicy("frame-ancestors self eclass.uoa.gr dimos.med.uoa.gr");
		}
	}
 }
