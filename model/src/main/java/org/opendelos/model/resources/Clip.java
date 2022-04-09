package org.opendelos.model.resources;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Clips")
public class Clip {

    @Id
    private String id;


}
