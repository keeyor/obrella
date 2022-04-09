/* 
     Author: Michael Gatzonis - 12/27/2018 
     OpenDelosDAC
*/
package org.opendelos.model.repo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.resources.ScheduledEvent;

@Getter
@Setter
public class QueryScheduledEventsResults {

    private long skip;
    private int limit;
    private String sort;
    private String direction;
    private long totalResults;

    private List<ScheduledEvent> searchResultList;
}
