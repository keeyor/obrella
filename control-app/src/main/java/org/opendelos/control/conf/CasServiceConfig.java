/* 
     Author: Michael Gatzonis - 23/11/2020 
     live
*/
package org.opendelos.control.conf;


import org.opendelos.model.properties.CasProperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
public class CasServiceConfig {

	@Value( "${cas.service}" )
	private String casService;
	@Value( "${cas.key}" )
	private String key;
	@Value( "${cas.saml11_ticker_validator}" )
	private String samlValidator;
	@Value( "${cas.filter_processes_url}" )
	private String filterUrl;
	@Value( "${cas.login_url}" )
	private String loginUrl;
	@Value( "${cas.cas_server_url_prefix}" )
	private String serverUrlPrefix;

	@Value( "${app.host}" )
	private String app_host;

	@Bean
	public CasProperties casProperties() {

		CasProperties casProperties = new CasProperties();
		casProperties.setService(app_host + casService);
		casProperties.setKey(key);
		casProperties.setSaml11_ticker_validator(samlValidator);
		casProperties.setFilter_processes_url(filterUrl);
		casProperties.setLogin_url(loginUrl);
		casProperties.setCas_server_url_prefix(serverUrlPrefix);

		return casProperties;
	}

}
