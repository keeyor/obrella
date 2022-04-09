/* 
     Author: Michael Gatzonis - 25/9/2021 
     opendelos-uoa
*/
package org.opendelos.model.scheduler.wowza;

public class StreamFiles {

	private String serverName;
	private StreamType[] streamFiles;
	private String[] saveFiledList;
	private String version;

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public StreamType[] getStreamFiles() {
		return streamFiles;
	}

	public void setStreamFiles(StreamType[] streamFiles) {
		this.streamFiles = streamFiles;
	}

	String[] getSaveFiledList() {
		return saveFiledList;
	}

	void setSaveFiledList(String[] saveFiledList) {
		this.saveFiledList = saveFiledList;
	}

	String getVersion() {
		return version;
	}

	void setVersion(String version) {
		this.version = version;
	}
}
