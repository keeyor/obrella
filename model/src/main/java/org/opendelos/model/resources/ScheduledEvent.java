package org.opendelos.model.resources;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Language;

@Document(collection = "opendelos.events",language="en")
@Getter
@Setter
public class ScheduledEvent implements Serializable {

    @Language
    String lang;

    @Id
    private String id;

    @Indexed(direction = IndexDirection.ASCENDING)
    protected String identity;
    @TextIndexed(weight=3)
    protected String title;
    @TextIndexed(weight=3)
    protected String description;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String type;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String area;
    protected String[] categories;
    protected String url;
    protected String photoRelativeUrl;
    protected List<Unit> responsibleUnit;
    protected Person responsiblePerson;
    protected String place;
    protected Instant startDate;
    protected Instant endDate;
    protected String accessPolicy;
    protected Person editor;
    protected Boolean isActive;
    protected Boolean isFeatured;
    protected Instant dateModified;
    protected String textTags;
    protected int counter; // ReportQuery

}
