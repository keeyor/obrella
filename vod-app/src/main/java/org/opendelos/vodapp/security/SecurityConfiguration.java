/* 
     Author: Michael Gatzonis - 26/9/2020 
     live
*/
package org.opendelos.vodapp.security;

import java.util.Arrays;


import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Saml11TicketValidator;
import org.opendelos.vodapp.conf.CasServiceConfig;
import org.opendelos.vodapp.security.database.DatabaseAuthenticationFailureHandler;
import org.opendelos.vodapp.security.database.DatabaseAuthenticationSuccessHandler;
import org.opendelos.vodapp.security.database.DatabaseUserDetailsServiceImpl;
import org.opendelos.vodapp.security.token.CustomTokenAuthenticationFilter;
import org.opendelos.vodapp.security.token.TokenAuthService;
import org.opendelos.vodapp.services.opUser.OpUserService;

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
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Order(3)
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
			return new DatabaseAuthenticationSuccessHandler(opUserService);
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
					.loginPage("/rlogin")
					.loginProcessingUrl("/rlogin")
					.successHandler(databaseAuthenticationSuccessHandlerDB())
					.failureHandler(databaseAuthenticationFailureHandlerDB())
					.and()
					.authorizeRequests()
					.antMatchers("/rlogin").permitAll()
					.antMatchers("/*", "/css/**","/lib/**", "/error", "/index", "/css/**", "/js/**", "/public/**", "/apiw/**", "/player","/locale/change").permitAll()
					.antMatchers("/admin/index").hasAnyRole("SA", "MANAGER", "SUPPORT", "STAFFMEMBER")
					.anyRequest().authenticated()
					.and().exceptionHandling().accessDeniedHandler(accessDeniedHandler())
					.and().csrf()
					.ignoringAntMatchers("/search","/apiw/**","/api/**")
					.and()
					.logout()
					.invalidateHttpSession(true)
					.permitAll();
			http
					.sessionManagement()
					.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);

		}
	}

 /*
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
*/

	@Order(2)
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
 	@Configuration
	@Order(1)
	public static class ApiTokenSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

		private final TokenAuthService tokenAuthService;
		private final OpUserService opUserService;

		@Autowired
		public ApiTokenSecurityConfigurationAdapter(TokenAuthService tokenAuthService, OpUserService opUserService) {
			this.tokenAuthService = tokenAuthService;
			this.opUserService = opUserService;
		}
		protected void configure(HttpSecurity http) throws Exception {

			http	.antMatcher("/api/**")
					.authorizeRequests()
					.antMatchers("/api/**").hasAnyRole("SA", "MANAGER", "SUPPORT", "STAFFMEMBER");
			http
					.cors().and()
			 		.addFilterAfter(new CustomTokenAuthenticationFilter("/api/**",tokenAuthService, opUserService), BasicAuthenticationFilter.class);
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
