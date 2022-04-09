/* 
     Author: Michael Gatzonis - 20/3/2021 
     obrella
*/
package org.opendelos.model.scheduler.wowza.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncomingStreamConfigExtended extends  IncomingStreamConfig {

	private String serverCode;
	private String applicationName;
	private String roomCode;
	private boolean isRecording;
}
