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
public class StreamFileAppConfig {

	private String sourceControlDriver;
	private String sourceControlSourceName;
	private String SourceControlPassword;
	private String name;
	private String serverName;
	private List<String> saveFieldList;
	private String version;
	private String uri;
}
