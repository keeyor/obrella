/* 
     Author: Michael Gatzonis - 23/11/2020 
     live
*/
package org.opendelos.model.properties;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StreamingProperties {
	private String description;
	private String codename;
	private String protocol;
	private String host;
	private String port;
	private String webDir;
	private String absDir;

	private String support_files_webDir;
	private int segmentation_duration;
	private String storage;
	private String storage_alt;
	private String live_server_url;
	private boolean use_recorder;
	private boolean allowRegularOverlaps;
	private boolean allowOnetimeOverlaps;
	private String overrideResourceUrl;		//play resources from uoa storage (debug)
}
