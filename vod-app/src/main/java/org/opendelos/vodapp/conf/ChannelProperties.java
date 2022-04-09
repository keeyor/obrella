/* 
     Author: Michael Gatzonis - 23/11/2020 
     live
*/
package org.opendelos.vodapp.conf;


import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("channel")
@Getter
@Setter
public class ChannelProperties {
	private String streaming_server_code;
	private String stream_name;
	private String live_stream_id;
	private List<String> playlist;
}
