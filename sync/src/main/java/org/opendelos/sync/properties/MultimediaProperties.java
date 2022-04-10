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
@PropertySource("classpath:media.properties")
@ConfigurationProperties("media")
@Getter
@Setter
public class MultimediaProperties {
	private String protocol;
	private String host;
	private String port;
	private String webDir;
	private String absDir;
	private String ffmpeg;
	private int slideWidth;
	private int thumbWidth;
	private String watermark;
}
