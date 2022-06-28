/* 
     Author: Michael Gatzonis - 12/2/2022 
     Balloon
*/
package org.opendelos.eventsapp.conf;

import org.opendelos.model.properties.StreamingProperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
public class StreamingPropertiesConfig {

	@Value( "${streaming.codename}" )
	private String codename;
	@Value( "${streaming.description}" )
	private String description;
	@Value( "${streaming.host}" )
	private String host;
	@Value( "${streaming.protocol}" )
	private String protocol;
	@Value( "${streaming.port}" )
	private String port;
	@Value( "${streaming.webdir}" )
	private String webDir;
	@Value( "${streaming.absdir}" )
	private String absDir;
	@Value( "${streaming.support_files_webDir}" )
	private String supportFilesWebDir;
	@Value( "${streaming.live_server_url}" )
	private String liveServerUrl;
	@Value( "${streaming.storage}" )
	private String storage;
	@Value( "${streaming.use_recorder}" )
	private boolean userRecorder;
	@Value( "${streaming.segmentation_duration}" )
	private int segmentationDuration;
	@Value( "${streaming.override_url}" )
	private String overrideResourceUrl;

	@Bean
	public StreamingProperties streamingProperties() {

		StreamingProperties streamingProperties = new StreamingProperties();
		streamingProperties.setCodename(codename);
		streamingProperties.setDescription(description);
		streamingProperties.setHost(host);
		streamingProperties.setProtocol(protocol);
		streamingProperties.setPort(port);
		streamingProperties.setWebDir(webDir);
		streamingProperties.setAbsDir(absDir);
		streamingProperties.setSupport_files_webDir(supportFilesWebDir);
		streamingProperties.setLive_server_url(liveServerUrl);
		streamingProperties.setStorage(storage);
		streamingProperties.setUse_recorder(userRecorder);
		streamingProperties.setSegmentation_duration(segmentationDuration);
		streamingProperties.setOverrideResourceUrl(overrideResourceUrl);

		return streamingProperties;
	}
}
