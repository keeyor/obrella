/* 
     Author: Michael Gatzonis - 26/9/2020 
     live
*/
package org.opendelos.live.security;

import org.opendelos.live.security.database.DatabaseAuthenticationFailureHandler;
import org.opendelos.live.security.database.DatabaseAuthenticationSuccessHandler;
import org.opendelos.live.security.database.DatabaseUserDetailsServiceImpl;
import org.opendelos.live.services.opUser.OpUserService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration  {

	@Order(4)
	@Configuration
	public static class DatabaseConfigurationAdapter extends WebSecurityConfigurerAdapter {

		private final OpUserService opUserService;
		private final PopulateActiveUser populateActiveUser;

		public DatabaseConfigurationAdapter(OpUserService opUserService, PopulateActiveUser populateActiveUser) {
			this.opUserService = opUserService;
			this.populateActiveUser = populateActiveUser;
		}

		@Bean
		public UserDetailsService daoUserDetailsService(){
			return new DatabaseUserDetailsServiceImpl(opUserService, populateActiveUser);
		}
		@Bean
		public DaoAuthenticationProvider daoAuthenticationProvider() {
			DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
			daoAuthenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder());
			daoAuthenticationProvider.setUserDetailsService(daoUserDetailsService());
			return daoAuthenticationProvider;
		}
		@Bean
		public AuthenticationSuccessHandler databaseAuthenticationSuccessHandlerDB() {
			return new DatabaseAuthenticationSuccessHandler();
		}
		@Bean
		public AuthenticationFailureHandler databaseAuthenticationFailureHandlerDB(){
			return new DatabaseAuthenticationFailureHandler();
		}
		@Override
		public void configure(AuthenticationManagerBuilder auth) throws Exception {
			try {
				auth.authenticationProvider(daoAuthenticationProvider());
			}
			catch (Exception e) {
				throw new Exception("Database Authentication Provider exception");
			}
		}
		@Override
		protected void configure(HttpSecurity http) throws Exception {

			http
					.formLogin()
					.loginPage("/login")
					.successHandler(databaseAuthenticationSuccessHandlerDB())
					.failureHandler(databaseAuthenticationFailureHandlerDB())
					.and()
					.authorizeRequests()
					.antMatchers("/", "/error", "/index", "/css/**", "/js/**", "/public/**", "/api/**").permitAll()
					.antMatchers("/search/**", "/import_legacy", "/lib/**", "/status/*", "/login").permitAll()
					.antMatchers("/admin/**").hasAnyRole("SA", "MANAGER", "SUPPORT", "STAFFMEMBER")
					.anyRequest().authenticated()
					.and().exceptionHandling()
					.and().csrf()
					.ignoringAntMatchers("/admin/user_profile", "/admin/multimediaUpload", "/admin/startMultimediaProcessing", "/admin/startSlidesProcessing", "/api/v1/**")
					.and()
					.logout()//.deleteCookies("JSESSIONID")
					.permitAll()
					.and()
					.headers().disable();
		}
	}
 }
