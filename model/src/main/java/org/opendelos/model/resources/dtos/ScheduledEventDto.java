package org.opendelos.model.resources.dtos;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.resources.ScheduledEvent;


@Getter
@Setter
public class ScheduledEventDto extends ScheduledEvent {

    protected String[] responsibleUnitIds;
    protected String[] responsibleUnitTypes;

}
