/* 
     Author: Michael Gatzonis - 23/11/2020 
     live
*/
package org.opendelos.model.properties;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MultimediaProperties {
	private String protocol;
	private String host;
	private String port;
	private String webDir;
	private String absDir;
	private String eventWebDir;
	private String eventAbsDir;
	private String ffmpeg;
	private int slideWidth;
	private int thumbWidth;
	private int imageMaxWidth;
	private int imageMaxHeight;
	private String watermark;
}
