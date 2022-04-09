/* 
     Author: Michael Gatzonis - 2/25/2019 
     OpenDelosDAC
*/
package org.opendelos.model.resources;

import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class Slide {
    protected String title;
    protected String url;
    protected String time;
    protected int index; // order of appearance in synced slides
}
