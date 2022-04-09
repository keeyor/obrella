package org.opendelos.control.services.upload.analyzers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.opendelos.control.services.upload.PresentationUtils;
import org.opendelos.model.properties.MultimediaProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.SlideShow;*/


@Service
public class PowerPointImageExtractor {

    private static final Logger logger = Logger.getLogger(PowerPointImageExtractor.class.getName());

    private final MultimediaProperties multimediaProperties;

    @Autowired
    public PowerPointImageExtractor(MultimediaProperties multimediaProperties) {
        this.multimediaProperties = multimediaProperties;
    }

    public List<org.opendelos.model.resources.Slide> extractImagesHSLF(String openfile, String destPath) throws Exception {

        List<org.opendelos.model.resources.Slide> slides = new ArrayList<>();

        try {
            // Extract Slide Images Office 2003 case
            FileInputStream is = new FileInputStream(openfile);
            HSLFSlideShow pptImage = new HSLFSlideShow(is);
            is.close();

            Dimension pgsize = pptImage.getPageSize();

            if (!PresentationUtils.allowedSlideSize(pgsize.width, pgsize.height)) {
                throw new InvalidFormatException("UnSupported Slide Size");
            }

            List<HSLFSlide> slide = pptImage.getSlides();

            // Loop over slides
            for (int i = 0; i < slide.size(); i++) {

                HSLFSlide slideI = slide.get(i);
                String slide_title = setSlideTitle(slideI);
                BufferedImage bufferedImage = getBufferedImageFromSlideHSLF(slideI, pgsize);
                String image_name = randomizeImageNameAndSave(bufferedImage, i, destPath);
                org.opendelos.model.resources.Slide createPresentationSlide = this
                        .createPresentationSlide(slide_title, image_name);
                slides.add(createPresentationSlide);
            }
        }
        catch (Exception e) {
            throw  new Exception("extractImagesHSLF Error:" + e.getMessage());
        }
        return slides;
    }
    /**
     * Extract images from power point ver. 2007
     */
    public List<org.opendelos.model.resources.Slide> extractImagesXSLF(String openFile, String destPath) throws Exception {

        List<org.opendelos.model.resources.Slide> slides = new ArrayList<>();
        XMLSlideShow ppt;

        ppt = new XMLSlideShow(OPCPackage.open(openFile, PackageAccess.READ));
        //create a new empty slide show
        XMLSlideShow n_ppt = new XMLSlideShow();

        List<XSLFSlide> slide = ppt.getSlides();
        for (int i = 0; i < slide.size(); i++) {
            try {
                n_ppt.createSlide().importContent(slide.get(i));
            } catch (Exception e) {
                throw new InvalidFormatException("Slide Number:" + (i + 1) + " has issues");
            }
        }

        Dimension pgsize = ppt.getPageSize();

        if (!PresentationUtils.allowedSlideSize(pgsize.width, pgsize.height)) {
            throw new InvalidFormatException("UnSupported Slide Size");
        }

        //Change n_ppt to ppt to use uploaded File
        slide = n_ppt.getSlides();

        for (int i = 0; i < slide.size(); i++) {

            //if (slidenum != -1 && slidenum != (i + 1)) continue;

            XSLFSlide slideI = slide.get(i);
            String slide_title = setSlideTitle(slideI);
            BufferedImage bufferedImage = getBufferedImageFromSlideXSLF(slideI, pgsize);
            String image_name = randomizeImageNameAndSave(bufferedImage, i, destPath);
            org.opendelos.model.resources.Slide createPresentationSlide = this.createPresentationSlide(slide_title, image_name);
            slides.add(createPresentationSlide);
        }

        ppt.getPackage().revert();
        return slides;
    }

    private org.opendelos.model.resources.Slide createPresentationSlide(String title,String url) {

        org.opendelos.model.resources.Slide presentationSlide = new org.opendelos.model.resources.Slide();
        presentationSlide.setTitle(title);
        presentationSlide.setTime("-1");
        presentationSlide.setUrl(url);

        return presentationSlide;
    }

    private String setSlideTitle(Object o) {

        String slide_title = null;
        if (o instanceof HSLFSlide) {
            HSLFSlide slide = (HSLFSlide) o;
            if (slide.getTitle() != null) {
                slide_title = slide.getTitle().trim().replaceAll("&", "&amp;");
            }
        } else if (o instanceof XSLFSlide) {
            XSLFSlide slide = (XSLFSlide) o;
            if (slide.getTitle() != null) {
                slide_title = slide.getTitle().trim().replaceAll("&", "&amp;");
            }
        }
        if (slide_title != null) {
            return slide_title;
        } else {
            return "";
        }
    }

    private BufferedImage getBufferedImageFromSlideHSLF(HSLFSlide slide, Dimension pgsize) {

        BufferedImage img;

        img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.SCALE_SMOOTH);
        Graphics2D graphics = img.createGraphics();
        //clear the drawing area
        graphics.setPaint(Color.white);
        graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));

        slide.draw(graphics);

        return this.createBufferedImage(img, pgsize.width, pgsize.height);
    }

    private BufferedImage getBufferedImageFromSlideXSLF(XSLFSlide xslfSlide, Dimension pgsize) {

        BufferedImage img;

        img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();

        // default rendering options
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        graphics.setColor(Color.white);
        graphics.clearRect(0, 0, pgsize.width, pgsize.height);

        try {
            xslfSlide.draw(graphics);
        } catch (Exception p1) {
            logger.severe(p1.getMessage());
        }

        return  this.createBufferedImage(img, pgsize.width, pgsize.height);
    }

    private BufferedImage createBufferedImage(BufferedImage img, int sWidth, int sHeight) {

        int slideWidth = multimediaProperties.getSlideWidth();
        double ImageAspectRatio = 800 / (double) sWidth;
        int slideHeight = (int) (sHeight * ImageAspectRatio);

        BufferedImage bufferedImage = new BufferedImage(slideWidth, slideHeight, img.getType());
        Graphics2D g = bufferedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, slideWidth, slideHeight, 0, 0, sWidth, sHeight, null);
        g.dispose();

        return bufferedImage;
    }

    private String randomizeImageNameAndSave(BufferedImage bufferedImage, int slide_number, String destinationDir) throws IOException {

        FileOutputStream out;
        String img_path;
        String slide_name;
        String index;
        if (slide_number > 8) {
            index = "s" + (slide_number+1);
        }
        else {
            index = "s0" + (slide_number+1);
        }
        String renameTo = RandomStringUtils.randomAlphanumeric(32);
        slide_name = index + "_" + renameTo + ".jpg";

        img_path = destinationDir + slide_name;
        out = new FileOutputStream(img_path);
        ImageIO.write(bufferedImage, "jpg", out);
        out.close();

        return slide_name;
    }







}
