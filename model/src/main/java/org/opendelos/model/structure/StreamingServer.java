package org.opendelos.model.structure;


import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "StreamingServers")
@Getter
@Setter
public class StreamingServer implements Serializable {

    @Id
    protected String id;

    @Indexed(direction = IndexDirection.ASCENDING)
    protected String identity;
    protected String serverType;
    protected String type;
    protected String code;
    protected String description;
    protected String server;
    protected String port;
    protected String protocol;
    protected String application;
    protected String adminUser;
    protected String adminPassword;
    protected String adminPort;
    protected String restPort;
    protected String enabled;

}
