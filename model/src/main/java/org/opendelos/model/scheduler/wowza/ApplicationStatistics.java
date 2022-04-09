/* 
     Author: Michael Gatzonis - 9/2/2021 
     live
*/
package org.opendelos.model.scheduler.wowza;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationStatistics {

	// NOT USED FOR NOW!
	private String serverName;
	private int uptime;
	private int bytesIn;
	private int bytesOut;
	private int bytesInRate;
	private int bytesOutRate;
	private int totalConnections;
	private ConnectionCount connectionCount;

	@Getter
	@Setter
	public static class ConnectionCount {

		private int WEBM;
		private int DVRCHUNKS;
		private int RTMP;
		private int MPEGDASH;
		private int CUPERTINO;
		private int SANJOSE;
		private int SMOOTH;
		private int RTP;
	}
}

