/* 
     Author: Michael Gatzonis - 23/11/2020 
     live
*/
package org.opendelos.eventsapp.conf;


import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties("lms")
@Getter
@Setter
public class LmsProperties {
	private List<String> url;
	private List<String> name;
	private List<String> secret;
}
