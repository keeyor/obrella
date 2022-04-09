package org.opendelos.vodapp.api.resource;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
public class JsonSlidesSyncPost {

	@JsonProperty("urls")
	private String[] urls;
	@JsonProperty("times")
	private String[] times;
	@JsonProperty("initial_duration")
	private String initial_duration;

	public String[] getUrls() {
		return urls;
	}

	public void setUrls(String[] urls) {
		this.urls = urls;
	}

	public String[] getTimes() {
		return times;
	}

	public void setTimes(String[] times) {
		this.times = times;
	}

	public String getInitial_duration() {
		return initial_duration;
	}

	public void setInitial_duration(String initial_duration) {
		this.initial_duration = initial_duration;
	}

}
