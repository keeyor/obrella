/* 
     Author: Michael Gatzonis - 23/11/2020 
     live
*/
package org.opendelos.sync.properties;


import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:streaming.properties")
@ConfigurationProperties("streaming")
@Getter
@Setter
public class StreamingProperties {
	private String description;
	private String codename;
	private String protocol;
	private String host;
	private String port;
	private String webDir;
	private String absDir;

	private String support_files_webDir;
	private String live_server_url;
}
