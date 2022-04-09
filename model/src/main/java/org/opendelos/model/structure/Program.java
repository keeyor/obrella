package org.opendelos.model.structure;


import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.resources.Unit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "Programs")
@Getter
@Setter
public class Program {

    @Id
    private String id;

    @Indexed(direction = IndexDirection.ASCENDING)
    private String departmentId;
    @Indexed(direction = IndexDirection.ASCENDING)
    @Field("study")
    private String studyId;
    private String title_el;
    private String title_en;
    private String description;
    private boolean enabled;      /* in case a postgraduate program is cancelled */
}
