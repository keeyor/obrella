/* 
     Author: Michael Gatzonis - 1/9/2020 
     opendelos-uoa
*/
package org.opendelos.model.scheduler.wowza.config;

import java.util.ArrayList;
import java.util.List;

public class StreamRecorderConfig {
	private String instanceName = "";
	private String fileVersionDelegateName = "";
	private String serverName = "";
	private String recoderName;
	private int currentSize = 0;
	private String segmentSchedule = "";
	private boolean startOnKeyFrame = true;
	private String outputPath= "";
	private String currentFile= "";
	private List<String> saveFieldList = new ArrayList<>();
	private boolean recordData = false;
	private String applicationName= "";
	private boolean moveFirstVideoFrameToZero = false;
	private String recorderErrorString= "";
	private int segmentSize= 0;
	private boolean defaultRecorder = false;
	private boolean splitOnTcDiscontinuity= false;
	private String version= "";
	private String baseFile= "";
	private long segmentDuration;
	private String recordingStartTime= "";
	private String fileTemplate= "";
	private int backBufferTime= 0;
	private String segmentationType= "duration";
	private int currentDuration= 0;
	private String fileFormat= "";
	private String recorderState= "";
	private String option= "";

	public String getInstanceName() {
		return instanceName;
	}

	public StreamRecorderConfig setInstanceName(String instanceName) {
		this.instanceName = instanceName;
		return this;
	}

	public String getFileVersionDelegateName() {
		return fileVersionDelegateName;
	}

	public StreamRecorderConfig setFileVersionDelegateName(String fileVersionDelegateName) {
		this.fileVersionDelegateName = fileVersionDelegateName;
		return this;
	}

	public String getServerName() {
		return serverName;
	}

	public StreamRecorderConfig setServerName(String serverName) {
		this.serverName = serverName;
		return this;
	}

	public String getRecoderName() {
		return recoderName;
	}

	public StreamRecorderConfig setRecoderName(String recoderName) {
		this.recoderName = recoderName;
		return this;
	}

	public int getCurrentSize() {
		return currentSize;
	}

	public StreamRecorderConfig setCurrentSize(int currentSize) {
		this.currentSize = currentSize;
		return this;
	}

	public String getSegmentSchedule() {
		return segmentSchedule;
	}

	public StreamRecorderConfig setSegmentSchedule(String segmentSchedule) {
		this.segmentSchedule = segmentSchedule;
		return this;
	}

	public boolean isStartOnKeyFrame() {
		return startOnKeyFrame;
	}

	public StreamRecorderConfig setStartOnKeyFrame(boolean startOnKeyFrame) {
		this.startOnKeyFrame = startOnKeyFrame;
		return this;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public StreamRecorderConfig setOutputPath(String outputPath) {
		this.outputPath = outputPath;
		return this;
	}

	public String getCurrentFile() {
		return currentFile;
	}

	public StreamRecorderConfig setCurrentFile(String currentFile) {
		this.currentFile = currentFile;
		return this;
	}

	public List<String> getSaveFieldList() {
		return saveFieldList;
	}

	public StreamRecorderConfig setSaveFieldList(List<String> saveFieldList) {
		this.saveFieldList = saveFieldList;
		return this;
	}

	public boolean isRecordData() {
		return recordData;
	}

	public StreamRecorderConfig setRecordData(boolean recordData) {
		this.recordData = recordData;
		return this;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public StreamRecorderConfig setApplicationName(String applicationName) {
		this.applicationName = applicationName;
		return this;
	}

	public boolean isMoveFirstVideoFrameToZero() {
		return moveFirstVideoFrameToZero;
	}

	public StreamRecorderConfig setMoveFirstVideoFrameToZero(boolean moveFirstVideoFrameToZero) {
		this.moveFirstVideoFrameToZero = moveFirstVideoFrameToZero;
		return this;
	}

	public String getRecorderErrorString() {
		return recorderErrorString;
	}

	public StreamRecorderConfig setRecorderErrorString(String recorderErrorString) {
		this.recorderErrorString = recorderErrorString;
		return this;
	}

	public int getSegmentSize() {
		return segmentSize;
	}

	public StreamRecorderConfig setSegmentSize(int segmentSize) {
		this.segmentSize = segmentSize;
		return this;
	}

	public boolean isDefaultRecorder() {
		return defaultRecorder;
	}

	public StreamRecorderConfig setDefaultRecorder(boolean defaultRecorder) {
		this.defaultRecorder = defaultRecorder;
		return this;
	}

	public boolean isSplitOnTcDiscontinuity() {
		return splitOnTcDiscontinuity;
	}

	public StreamRecorderConfig setSplitOnTcDiscontinuity(boolean splitOnTcDiscontinuity) {
		this.splitOnTcDiscontinuity = splitOnTcDiscontinuity;
		return this;
	}

	public String getVersion() {
		return version;
	}

	public StreamRecorderConfig setVersion(String version) {
		this.version = version;
		return this;
	}

	public String getBaseFile() {
		return baseFile;
	}

	public StreamRecorderConfig setBaseFile(String baseFile) {
		this.baseFile = baseFile;
		return this;
	}

	public long getSegmentDuration() {
		return segmentDuration;
	}

	public StreamRecorderConfig setSegmentDuration(int segmentDuration) {
		this.segmentDuration = segmentDuration;
		return this;
	}

	public String getRecordingStartTime() {
		return recordingStartTime;
	}

	public StreamRecorderConfig setRecordingStartTime(String recordingStartTime) {
		this.recordingStartTime = recordingStartTime;
		return this;
	}

	public String getFileTemplate() {
		return fileTemplate;
	}

	public StreamRecorderConfig setFileTemplate(String fileTemplate) {
		this.fileTemplate = fileTemplate;
		return this;
	}

	public int getBackBufferTime() {
		return backBufferTime;
	}

	public StreamRecorderConfig setBackBufferTime(int backBufferTime) {
		this.backBufferTime = backBufferTime;
		return this;
	}

	public String getSegmentationType() {
		return segmentationType;
	}

	public StreamRecorderConfig setSegmentationType(String segmentationType) {
		this.segmentationType = segmentationType;
		return this;
	}

	public int getCurrentDuration() {
		return currentDuration;
	}

	public StreamRecorderConfig setCurrentDuration(int currentDuration) {
		this.currentDuration = currentDuration;
		return this;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public StreamRecorderConfig setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
		return this;
	}

	public String getRecorderState() {
		return recorderState;
	}

	public StreamRecorderConfig setRecorderState(String recorderState) {
		this.recorderState = recorderState;
		return this;
	}

	public String getOption() {
		return option;
	}

	public StreamRecorderConfig setOption(String option) {
		this.option = option;
		return this;
	}
}
