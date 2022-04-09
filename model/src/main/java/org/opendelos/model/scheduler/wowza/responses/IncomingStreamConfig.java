/* 
     Author: Michael Gatzonis - 20/3/2021 
     obrella
*/
package org.opendelos.model.scheduler.wowza.responses;


import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncomingStreamConfig implements Serializable {

	private int ptzPollingIntervalMinimum;
	private String applicationInstance;
	@JsonProperty("isPTZEnabled")
	private boolean isPTZEnabled;
	@JsonProperty("isConnected")
	private boolean isConnected;
	private String serverName;
	private String version;
	private int ptzPollingInterval;
	@JsonProperty("isRecordingSet")
	private boolean isRecordingSet;
	private String sourceIp;
	private String name;
	@JsonProperty("isPublishedVOD")
	private boolean isPublishedToVOD;
	private List<String> saveFieldList;
	@JsonProperty("isStreamManagerStream")
	private boolean isStreamManagerStream;
}
