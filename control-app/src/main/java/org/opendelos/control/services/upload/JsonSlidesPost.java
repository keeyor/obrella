package org.opendelos.control.services.upload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class JsonSlidesPost {

    @JsonProperty("folder")
    private String folder;
    @JsonProperty("id")
	private String id;
	@JsonProperty("action")
    private String action;
    @JsonProperty("actionData")
    private String actionData;
    @JsonProperty("module")
    private String module;

    @Override
    public String toString() {
        String str = "=================================\r\n";
        
        str += "Folder: " + folder + "\r\n" +
        	   "toDo:" + action + "\r\n";

        return str;     
    }
}
