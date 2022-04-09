/* 
     Author: Michael Gatzonis - 23/11/2020 
     live
*/
package org.opendelos.model.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CcLicense {

	private String type;
	private String identity;
	private String title;
	private String url;
	private String intro_url;
	private String image_url;
}
