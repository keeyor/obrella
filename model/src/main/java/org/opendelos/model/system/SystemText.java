/* 
     Author: Michael Gatzonis - 4/4/2021 
     obrella
*/
package org.opendelos.model.system;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "System.Texts")
@Getter
@Setter
public class SystemText implements Serializable {
	@Id
	private String id;
	private String site;
 	private String code;
 	private String title;
 	private String title_en;
 	private String content;
 	private String content_en;
}
