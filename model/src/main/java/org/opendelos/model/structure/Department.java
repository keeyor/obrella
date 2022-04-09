package org.opendelos.model.structure;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.dates.CustomPause;
import org.opendelos.model.dates.CustomPeriod;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Departments")
@Getter
@Setter
public class Department implements Serializable {

    @Id
    protected String id;

    protected String title;
    protected String title_en;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String identity;
    protected String url;
    protected String logoUrl;
    protected String password;
    @Indexed(direction = IndexDirection.ASCENDING)
    private String institutionId;
    @Indexed(direction = IndexDirection.ASCENDING)
    private String schoolId;
    @Indexed(direction = IndexDirection.ASCENDING)
    private List<String> classrooms;
    private List<CustomPeriod> customPeriods;
    private List<CustomPause> customPauses;
    
    public List<String> getClassrooms() {
        if (classrooms == null) {
            classrooms = new ArrayList<>();
        }
        return classrooms;
    }
    public List<CustomPeriod> getCustomPeriods() {
        if (customPeriods == null) {
            customPeriods = new ArrayList<>();
        }
        return customPeriods;
    }
    public List<CustomPause> getCustomPauses() {
        if (customPauses == null) {
            customPauses = new ArrayList<>();
        }
        return customPauses;
    }
    public String getTitle(Locale locale) {
        if (locale.getLanguage().equals("en")) {
            return this.getTitle_en();
        }
        else {
            return this.getTitle();
        }
    }
}

