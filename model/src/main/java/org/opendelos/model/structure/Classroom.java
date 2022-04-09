package org.opendelos.model.structure;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "Classrooms")
@Getter
@Setter
public class Classroom implements Serializable  {

    @Id
    private String id;

    @Indexed(direction = IndexDirection.ASCENDING)
    protected String code;
    @Indexed(direction = IndexDirection.ASCENDING)
    protected String identity;
    protected String name;
    protected String description;
    protected String location;
    protected String map;
    protected String calendar;
    @Field("devices")
    private List<Device> devices;
    protected String usage; //lectures, events, both
    protected List<String> availableTo;


    public List<String> getAvailableTo() {
        if (availableTo == null) {
            availableTo = new ArrayList<>();
        }
        return availableTo;
    }

}
