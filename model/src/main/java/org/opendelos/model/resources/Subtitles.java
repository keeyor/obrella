/* 
     Author: Michael Gatzonis - 2/25/2019 
     OpenDelosDAC
*/
package org.opendelos.model.resources;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Subtitles {

    protected List<Sub> subs;
    @Getter
    @Setter
    public static class Sub {

        private String description;
        private String url;
    }
}
