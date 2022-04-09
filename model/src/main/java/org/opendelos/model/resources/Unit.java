/* 
     Author: Michael Gatzonis - 3/7/2019 
     OpenDelosDAC
*/
package org.opendelos.model.resources;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;


@Getter
@Setter
public class Unit implements Serializable  {

    private StructureType structureType;
    @Indexed(direction = IndexDirection.ASCENDING)
    private String id;
    private String parentId;
    private String title;
    private int counter; // QueryReport

    public Unit() {
    }

    public Unit(StructureType structureType, String id, String title) {
        this.structureType = structureType;
        this.id = id;
        this.title = title;
    }
    public Unit(StructureType structureType, String id, String parentId, String title) {
        this.structureType = structureType;
        this.id = id;
        this.parentId = parentId;
        this.title = title;
    }
}
