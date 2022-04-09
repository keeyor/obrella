package org.opendelos.model.structure;


import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.resources.Unit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "Courses")
@Getter
@Setter
public class Course implements Serializable {

    @Id
    protected String id;
    @TextIndexed(weight=5)
    protected String title;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String identity;
    protected String scopeId;
    @Indexed(direction = IndexDirection.ASCENDING)
    private String institutionId;
    @Indexed(direction = IndexDirection.ASCENDING)
    private String schoolId;
    protected Unit department;
    protected List<Unit> departmentsRelated;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String studyProgramId;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String study;
    protected List<LmsReference> lmsReferences;
    protected String semester;
    protected int teachingCounter; // how many are teaching the course currently
    protected long resourceCounter; // how many resources refer this course;
    protected long resourcePublicCounter; // how many public resources refer this course;
    protected int counter; // used in report
    protected String[] categories;
    protected boolean inactive; // to stop using the course in Creation (new lecture & new schedule) lists (absolete course!)

}

