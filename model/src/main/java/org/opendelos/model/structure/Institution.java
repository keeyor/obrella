package org.opendelos.model.structure;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.dates.CustomPause;
import org.opendelos.model.dates.CustomPeriod;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "Institutions")
@Getter
@Setter
public class Institution {
    @Id
    private String id;

    @Indexed(direction = IndexDirection.ASCENDING)
    private String identity;
    private String title;
    private String url;
    private String logoUrl;
    private Administrator administrator;
    private String organizationLicense;
    private List<CustomPeriod> customPeriods;
    private List<CustomPause> customPauses;

    public Institution() {
    }

    public Institution(String identity, String title) {
        this.identity = identity;
        this.title = title;
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
    @Getter
    @Setter
    public static class Administrator {

        private String name;
        private String email;
        private List<String> telephone;
    }


}
