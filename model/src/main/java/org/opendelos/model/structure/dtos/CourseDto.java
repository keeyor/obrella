package org.opendelos.model.structure.dtos;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.LmsReference;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
public class CourseDto extends Course implements Serializable {

    protected String studyProgramTitle;
    protected String studyTitle;
    protected List<Person> supportedBy;
    protected long teaching;


}

