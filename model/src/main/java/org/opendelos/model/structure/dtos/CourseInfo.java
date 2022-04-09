package org.opendelos.model.structure.dtos;


import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseInfo implements Serializable {

    protected String id;
    protected String title;

    public CourseInfo(String id, String title) {
        this.id = id;
        this.title = title;
    }
    public CourseInfo() {
        this.id = "";
        this.title = "";
    }
}

