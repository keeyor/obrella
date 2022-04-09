/* 
     Author: Michael Gatzonis - 12/27/2018 
     OpenDelosDAC
*/
package org.opendelos.live.repository.resource;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.resources.Resource;

@Getter
@Setter
public class QueryResourceResults {

    private long skip;
    private int limit;
    private String sort;
    private String direction;
    private long totalResults;

    private List<Resource> searchResultList;
}
