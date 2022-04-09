/* 
     Author: Michael Gatzonis - 23/11/2020 
     live
*/
package org.opendelos.model.properties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CasProperties {
	private String service;
	private String key;
	private String saml11_ticker_validator;
	private String filter_processes_url;
	private String login_url;
	private String cas_server_url_prefix;
}
