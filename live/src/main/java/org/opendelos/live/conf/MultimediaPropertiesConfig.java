/* 
     Author: Michael Gatzonis - 23/11/2020 
     live
*/
package org.opendelos.live.conf;

import org.opendelos.model.properties.MultimediaProperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
public class MultimediaPropertiesConfig {

	@Value( "${media.host}" )
	private String host;
	@Value( "${media.protocol}" )
	private String protocol;
	@Value( "${media.port}" )
	private String port;
	@Value( "${media.webdir}" )
	private String webDir;
	@Value( "${media.absdir}" )
	private String absDir;
	@Value( "${media.eventWebDir}" )
	private String eventWebDir;
	@Value( "${media.eventAbsDir}" )
	private String eventAbsDir;
	@Value( "${media.ffmpeg}" )
	private String ffmpeg;
	@Value( "${media.slideWidth}" )
	private int slideWidth;
	@Value( "${media.thumbWidth}" )
	private int thumbWidth;
	@Value( "${media.imageMaxWidth}" )
	private int imageMaxWidth;
	@Value( "${media.imageMaxHeight}" )
	private int imageMaxHeight;

	@Bean
	public MultimediaProperties multimediaProperties() {

		MultimediaProperties multimediaProperties = new MultimediaProperties();
		multimediaProperties.setHost(host);
		multimediaProperties.setProtocol(protocol);
		multimediaProperties.setPort(port);
		multimediaProperties.setWebDir(webDir);
		multimediaProperties.setAbsDir(absDir);
		multimediaProperties.setEventAbsDir(eventAbsDir);
		multimediaProperties.setEventAbsDir(eventAbsDir);
		multimediaProperties.setFfmpeg(ffmpeg);
		multimediaProperties.setSlideWidth(slideWidth);
		multimediaProperties.setThumbWidth(thumbWidth);
		multimediaProperties.setImageMaxWidth(imageMaxWidth);
		multimediaProperties.setImageMaxHeight(imageMaxHeight);


		return multimediaProperties;
	}

}
