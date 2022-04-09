package org.opendelos.model.structure;


import java.util.List;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Schools")
@Getter
@Setter
public class School {

    @Id
    private String id;

    @Indexed(direction = IndexDirection.ASCENDING)
    private String identity;
    private String title;
    private String title_en;

    public String getTitle(Locale locale) {
        if (locale.getLanguage().equals("en")) {
            return this.getTitle_en();
        }
        else {
            return this.getTitle();
        }
    }
}

