/* 
     Author: Michael Gatzonis - 12/3/2018 
     OpenDelosDAC
*/
package org.opendelos.model.resources;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;


@Getter
@Setter
public class Person implements Serializable {

    @Indexed(direction = IndexDirection.ASCENDING)
    private String id;
    @TextIndexed(weight=5)
    private String name;
    private String affiliation;
    private Unit department;
    private int counter; //For Report

    public Person() {
    }

    public Person(String id, String name, String affiliation) {
        this.id = id;
        this.name = name;
        this.affiliation = affiliation;
    }
    public Person(String id, String name, String affiliation, Unit department) {
        this.id = id;
        this.name = name;
        this.affiliation = affiliation;
        this.department = department;
    }
}