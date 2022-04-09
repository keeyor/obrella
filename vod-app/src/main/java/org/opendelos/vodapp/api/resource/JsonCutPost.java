package org.opendelos.vodapp.api.resource;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
public class JsonCutPost {

	@JsonProperty("clip_begin")
	private String[] clipBegin;
	@JsonProperty("clip_end")
	private String[] clipEnd;
	@JsonProperty("trim_begin")
	private String[] trimBegin;
	@JsonProperty("trim_end")
	private String[] trimEnd;
	@JsonProperty("real_duration")
	private String[] realDuration;
	@JsonProperty("initial_duration")
	private String[] initialDuration;


	public String[] getClipBegin() {
		return clipBegin;
	}

	public void setClipBegin(String[] clipBegin) {
		this.clipBegin = clipBegin;
	}

	public String[] getClipEnd() {
		return clipEnd;
	}

	public void setClipEnd(String[] clipEnd) {
		this.clipEnd = clipEnd;
	}

	public String[] getTrimBegin() {
		return trimBegin;
	}

	public void setTrimBegin(String[] trimBegin) {
		this.trimBegin = trimBegin;
	}

	public String[] getTrimEnd() {
		return trimEnd;
	}

	public void setTrimEnd(String[] trimEnd) {
		this.trimEnd = trimEnd;
	}

	public String[] getRealDuration() {
		return realDuration;
	}

	public void setRealDuration(String[] realDuration) {
		this.realDuration = realDuration;
	}

	public String[] getInitialDuration() {
		return initialDuration;
	}

	public void setInitialDuration(String[] initialDuration) {
		this.initialDuration = initialDuration;
	}

}
