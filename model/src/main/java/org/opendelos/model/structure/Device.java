package org.opendelos.model.structure;


import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
public class Device implements Serializable  {

    @Indexed(direction = IndexDirection.ASCENDING)
    protected String type;
    protected String identity;
    protected String description;
    protected String ipAddress;
    protected String macAddress;
    protected String technology;
    protected String socket;
    protected String streamAccessUrl;

}
