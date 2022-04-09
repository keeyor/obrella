/* 
     Author: Michael Gatzonis - 23/11/2020 
     live
*/
package org.opendelos.control.conf;

import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties("youtube")
@Getter
@Setter
public class YouTubeProperties {
	private String callback;
	private String clientSecretsFile;
	private String datastore;
	private Collection<String> scopes;
	private String host;
	private String application;
	private String profile;
	private String append2Target;
	private String username;
	private String password;

	private String framerate;
	private String ingestionType;
	private String resolution;

	private int minutesBefore;
	private int minutesAfter;

	private String wowzaHost;
	private int wowzaRestPort;
	private String wowzaUser;
	private String wowzaPassword;
	private String wowzaApplication;
}
