package org.opendelos.eventsapp.services.upload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsonMultimediaPost {

    @JsonProperty("id")
    private String id;
    @JsonProperty("folder")
    private String folder;
	@JsonProperty("action")
    private String action;
    @JsonProperty("actionData")
    private String actionData;
    @JsonProperty("fileName")
    private String filename;

	@Override
    public String toString() {
        String str = "=================================\r\n";
        
        str += "Folder: " + folder + "\r\n" +
        	   "action:" + action + "\r\n";

        return str;     
    }
}
