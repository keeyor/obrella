/* 
     Author: Michael Gatzonis - 1/9/2020 
     opendelos-uoa
*/
package org.opendelos.model.scheduler.wowza.config;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StreamTargetConfig {

	private String serverName;
	private String sourceStreamName;
	private String entryName;
	private String profile;
	private String host;
	private String application;
	private String userName;
	private String password;
	private String streamName;
}
