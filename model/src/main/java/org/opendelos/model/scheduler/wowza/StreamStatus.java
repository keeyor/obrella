/* 
     Author: Michael Gatzonis - 1/7/2022 
     obrella
*/
package org.opendelos.model.scheduler.wowza;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StreamStatus {

	protected String fatalError;

	protected boolean scheduled2Stream;
	protected String StreamingServerId;
	protected boolean isStreamAlive;
	protected int streamingErrorCode;
	protected String streamingError;

	protected boolean scheduled2Record;
	protected String recordingServerId;
	protected boolean isRecAlive;
	protected boolean isRecording;
	protected int recordingErrorCode;
	protected String recordingError;
}
