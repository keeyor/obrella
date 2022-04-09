/* 
     Author: Michael Gatzonis - 26/9/2020 
     live
*/
package org.opendelos.eventsapp.security;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Saml11TicketValidator;
import org.opendelos.eventsapp.security.CasUserDetailsServiceImpl;
import org.opendelos.eventsapp.security.CsrfCookieGeneratorFilter;
import org.opendelos.eventsapp.security.OdSimpleUrlAuthenticationSuccessHandler;
import org.opendelos.eventsapp.security.PopulateActiveUser;
import org.opendelos.eventsapp.services.opUser.OpUserService;
import org.opendelos.model.properties.CasProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.csrf.CsrfFilter;

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
					.antMatchers("/admin/**").hasAnyRole("SA", "MANAGER", "SUPPORT", "STAFFMEMBER")
					.anyRequest().authenticated()
					.and().exceptionHandling()
					.and().csrf()
					.ignoringAntMatchers("/search","/api/v1/**","/protected_player")
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

		private final CasProperties casProperties;
		private final OpUserService opUserService;
		private final PopulateActiveUser populateActiveUser;

		@Autowired
		public CasConfigurationAdapter(CasProperties casProperties, OpUserService opUserService, PopulateActiveUser populateActiveUser) {
			this.casProperties = casProperties;
			this.opUserService = opUserService;
			this.populateActiveUser = populateActiveUser;
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			http	.antMatcher("/cas/**")
					.authorizeRequests()
					.antMatchers("/cas/live_player").hasAnyRole("SA", "MANAGER", "SUPPORT", "STAFFMEMBER","STUDENT","USER");

			http
					.addFilterAfter(new CsrfCookieGeneratorFilter(), CsrfFilter.class).exceptionHandling()
					.authenticationEntryPoint(casAuthenticationEntryPoint()).and().addFilter(casAuthenticationFilter())
					.addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class);
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
			sp.setService(casProperties.getService());
			sp.setSendRenew(false);
			return sp;
		}

		@Bean
		public CasAuthenticationProvider casAuthenticationProvider() {
			CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
			casAuthenticationProvider.setAuthenticationUserDetailsService(casUserDetailsService());
			casAuthenticationProvider.setServiceProperties(serviceProperties());
			casAuthenticationProvider.setTicketValidator(casSamlServiceTicketValidator());
			casAuthenticationProvider.setKey(casProperties.getKey());
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
			return new Saml11TicketValidator(casProperties.getSaml11_ticker_validator());
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
			casAuthenticationFilter.setFilterProcessesUrl(casProperties.getFilter_processes_url());
			casAuthenticationFilter.setAuthenticationSuccessHandler(odUrlAuthenticationSuccessHandlerCAS());
			return casAuthenticationFilter;
		}

		@Bean
		public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
			CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
			casAuthenticationEntryPoint.setLoginUrl(casProperties.getLogin_url());
			casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
			return casAuthenticationEntryPoint;
		}

		@Bean
		public SingleSignOutFilter singleSignOutFilter() {
			SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
			singleSignOutFilter.setCasServerUrlPrefix(casProperties.getCas_server_url_prefix());
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
