//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.06.19 at 12:51:15 PM EEST 
//


package org.opendelos.legacydomain.slidesync;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.gunet.dilosdlm.schemas.slidesync package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.gunet.dilosdlm.schemas.slidesync
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Slideshow }
     * 
     */
    public Slideshow createSlideshow() {
        return new Slideshow();
    }

    /**
     * Create an instance of {@link Slideshow.Slides }
     * 
     */
    public Slideshow.Slides createSlideshowSlides() {
        return new Slideshow.Slides();
    }

    /**
     * Create an instance of {@link Slideshow.Slides.Slide }
     * 
     */
    public Slideshow.Slides.Slide createSlideshowSlidesSlide() {
        return new Slideshow.Slides.Slide();
    }

}