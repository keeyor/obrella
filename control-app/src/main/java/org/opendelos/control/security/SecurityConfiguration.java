/* 
     Author: Michael Gatzonis - 26/9/2020 
     live
*/
package org.opendelos.control.security;

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Saml11TicketValidator;
import org.opendelos.control.conf.CasServiceConfig;
import org.opendelos.control.mvc.CustomAccessDeniedHandler;
import org.opendelos.control.services.opUser.OpUserService;
import org.opendelos.control.security.database.DatabaseAuthenticationFailureHandler;
import org.opendelos.control.security.database.DatabaseAuthenticationSuccessHandler;
import org.opendelos.control.security.database.DatabaseUserDetailsServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.csrf.CsrfFilter;

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
		@Bean
		public AccessDeniedHandler accessDeniedHandler(){
			return new CustomAccessDeniedHandler();
		}
		@Override
		protected void configure(HttpSecurity http) throws Exception {

			http
					.formLogin()
					.loginPage("/")
					.successHandler(databaseAuthenticationSuccessHandlerDB())
					.failureHandler(databaseAuthenticationFailureHandlerDB())
					.and()
					.authorizeRequests()
					.antMatchers("/", "/error", "/index", "/css/**", "/js/**", "/public/**", "/api/**", "/live_player", "/player", "/live_channel","/404", "/403","/live", "/daily","/calendar", "/ahome", "/archive/*","/liveLectures","/liveScheduledEvents","/liveToday","/protected_player").permitAll()
					.antMatchers("/search/**", "/import_legacy", "/lib/**", "/status/*", "/login").permitAll()
					.antMatchers("/admin/user_profile").hasAnyRole("SA", "MANAGER", "SUPPORT", "STAFFMEMBER")
					.antMatchers("/admin/**").hasAnyRole("SA", "MANAGER", "SUPPORT", "STAFFMEMBER")
					.antMatchers("/admin/institution/**").hasAnyRole("SA")
					.antMatchers("/admin/system/**").hasAnyRole("SA")
					.antMatchers("/admin/department/**").hasAnyRole("SA","MANAGER")
					.anyRequest().authenticated()
					.and().exceptionHandling().accessDeniedHandler(accessDeniedHandler())
					.and().csrf()
					.ignoringAntMatchers("/admin/user_profile", "/search" ,"/admin/multimediaUpload", "/admin/imageUpload","/admin/startMultimediaProcessing", "/admin/startSlidesProcessing", "/api/v1/**",
							"/secure/image_upload","/admin/sevent-editor","/api/youtube/setBroadcast","/api/youtube/unsetBroadcast/**", "/protected_player", "/api/youtube/store")
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

	@Order(1)
	@Configuration
	public static class CasConfigurationAdapter extends WebSecurityConfigurerAdapter {

		private final CasServiceConfig casServiceConfig;
		private final OpUserService opUserService;
		private final PopulateActiveUser populateActiveUser;

		public CasConfigurationAdapter(CasServiceConfig casServiceConfig, OpUserService opUserService, PopulateActiveUser populateActiveUser) {
			this.casServiceConfig = casServiceConfig;
			this.opUserService = opUserService;
			this.populateActiveUser = populateActiveUser;
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			http	.antMatcher("/cas/**")
					.authorizeRequests()
					.antMatchers("/cas/**").hasAnyRole("SA", "MANAGER", "SUPPORT", "STAFFMEMBER");

			http
					.addFilterAfter(new CsrfCookieGeneratorFilter(), CsrfFilter.class).exceptionHandling()
					.authenticationEntryPoint(casAuthenticationEntryPoint()).and().addFilter(casAuthenticationFilter())
					.addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class);

			http
					.sessionManagement()
					.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
		}

		@Override
		public void configure(AuthenticationManagerBuilder auth) throws Exception {
			try {
				auth.authenticationProvider(casAuthenticationProvider());
			}
			catch (Exception e) {
				throw new Exception("Cas Authentication Provider exception");
			}
		}

		@Bean
		public ServiceProperties serviceProperties() {
			ServiceProperties sp = new ServiceProperties();
			sp.setService(casServiceConfig.casProperties().getService());
			sp.setSendRenew(false);
			return sp;
		}

		@Bean
		public CasAuthenticationProvider casAuthenticationProvider() {
			CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
			casAuthenticationProvider.setAuthenticationUserDetailsService(casUserDetailsService());
			casAuthenticationProvider.setServiceProperties(serviceProperties());
			casAuthenticationProvider.setTicketValidator(casSamlServiceTicketValidator());
			casAuthenticationProvider.setKey(casServiceConfig.casProperties().getKey());
			return casAuthenticationProvider;
		}

		@Bean
		public AuthenticationUserDetailsService<CasAssertionAuthenticationToken> casUserDetailsService() {
			return new CasUserDetailsServiceImpl(opUserService, populateActiveUser);
		}

		@Bean
		public SessionAuthenticationStrategy sessionStrategy() {
			return new SessionFixationProtectionStrategy();
		}

		@Bean
		public Saml11TicketValidator casSamlServiceTicketValidator() {
			return new Saml11TicketValidator(casServiceConfig.casProperties().getSaml11_ticker_validator());
		}

		@Bean
		public AuthenticationSuccessHandler odUrlAuthenticationSuccessHandlerCAS() {
			return new OdSimpleUrlAuthenticationSuccessHandler();
		}

		@Bean
		public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
			CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
			casAuthenticationFilter.setAuthenticationManager(authenticationManager());
			casAuthenticationFilter.setSessionAuthenticationStrategy(sessionStrategy());
			casAuthenticationFilter.setFilterProcessesUrl(casServiceConfig.casProperties().getFilter_processes_url());
			casAuthenticationFilter.setAuthenticationSuccessHandler(odUrlAuthenticationSuccessHandlerCAS());
			return casAuthenticationFilter;
		}

		@Bean
		public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
			CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
			casAuthenticationEntryPoint.setLoginUrl(casServiceConfig.casProperties().getLogin_url());
			casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
			return casAuthenticationEntryPoint;
		}

		@Bean
		public SingleSignOutFilter singleSignOutFilter() {
			SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
			singleSignOutFilter.setCasServerUrlPrefix(casServiceConfig.casProperties().getCas_server_url_prefix());
			singleSignOutFilter.setIgnoreInitConfiguration(true);
			return singleSignOutFilter;
		}

		//Single Logout
		@Bean
		public SecurityContextLogoutHandler securityContextLogoutHandler() {
			return new SecurityContextLogoutHandler();
		}

		@Bean
		public LogoutFilter logoutFilter() {
			LogoutFilter logoutFilter = new LogoutFilter("https://sso.uoa.gr/logout", securityContextLogoutHandler());
			logoutFilter.setFilterProcessesUrl("/logout/cas");
			return logoutFilter;
		}

	}
 }
