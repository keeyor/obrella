/* 
     Author: Michael Gatzonis - 4/1/2019 
     OpenDelosDAC
*/
package org.opendelos.control.services.upload.analyzers;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.opendelos.model.resources.Slide;

import org.springframework.stereotype.Service;

@Service
public class SlideRecorderExtractor {

    /**
     * Extract Times from SLIDE RECORDER SyncFile
     */
    public List<Slide> extractDataFromSlideRecorder(File syncFile) throws Exception {

        List<Slide> slides = new ArrayList<>();

        RecorderSlideShow syncobject;

        JAXBContext jc = JAXBContext.newInstance(RecorderSlideShow.class);
        Unmarshaller um = jc.createUnmarshaller();
        StringBuffer xmlStr = new StringBuffer(FileUtils.readFileToString(syncFile, "UTF-8"));
        syncobject = (RecorderSlideShow) um
                .unmarshal(new StreamSource(new StringReader(xmlStr
                        .toString())));

        int slide_numbers = syncobject.getSlides().getSlide().size();

        for (int i = 0; i < slide_numbers; i++) {

            String slide_title = "";
            if (syncobject.getSlides().getSlide().get(i).getTitle() != null) {
                slide_title = syncobject.getSlides().getSlide().get(i).getTitle();
            }
            Slide pSlide = new Slide();
            pSlide.setTitle(slide_title);
            String slide_url = syncobject.getSlides().getSlide().get(i).getUrl();
            pSlide.setUrl(slide_url);
            String slide_time = syncobject.getSlides().getSlide().get(i).getTime();
            pSlide.setTime(slide_time);
        }

        return  slides;
    }
}
