package org.opendelos.model.resources;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Presentation {

    protected List<Slide> slides;
    protected Cuts cuts;
    protected Subtitles subtitles;
    protected String realDuration;
    protected String duration;
    protected String title;
    protected String logo;
    protected String date;
    protected String provider;
    protected String filename;
    protected String basepath;
    protected String folder;
    protected String video_url;

}
