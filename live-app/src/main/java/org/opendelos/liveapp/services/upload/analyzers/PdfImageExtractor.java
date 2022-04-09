package org.opendelos.liveapp.services.upload.analyzers;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PdfImageExtractor {
	
	private static final Logger logger = LoggerFactory.getLogger(PdfImageExtractor.class);

	private int slideWidth;
	private int slideHeight;

	public PdfImageExtractor() {}
	
	public String read_pdf(String pdfFile,String destPath) throws IOException 
    {
		 PDDocument document=null;
		  
		try {

	       document = PDDocument.load(new File(pdfFile));     
	       PDFRenderer pdfRenderer = new PDFRenderer(document);
	       
	       File path = new File(destPath);
	  	   path.mkdirs();
 
	  	  
	  	   int pageCounter = 0;
	  	   for(PDPage page : document.getPages()) {

	  		    int pHeight, pWidth;
	  		    
	  		    int rotation = page.getRotation();
	  		 
	  		    if (rotation == 90) {
	  		    	 pHeight = Math.round(page.getMediaBox().getWidth());
		    	  	 pWidth = Math.round(page.getMediaBox().getHeight());
	  		    }
	  		    else {
	  		    	 pHeight = Math.round(page.getMediaBox().getHeight());
	  		    	 pWidth = Math.round(page.getMediaBox().getWidth());
	  		    }
	  		    //TODO: Get Size from preperties
	    	  	String slideSize = "800"; //_appconfig.getProperty("dilos.slideSize");
	    	  	
	    	  	setImageSizes(pWidth, pHeight,slideSize);

	            BufferedImage img = pdfRenderer.renderImageWithDPI(pageCounter, 128, ImageType.RGB);
 
	            Image tmp = img.getScaledInstance(slideWidth, slideHeight, Image.SCALE_SMOOTH);
	            BufferedImage dimg = new BufferedImage(slideWidth, slideHeight, BufferedImage.TYPE_INT_RGB);
	            
	            Graphics2D g2d = dimg.createGraphics();
	            g2d.drawImage(tmp, 0, 0, null);
	            g2d.dispose();
	            	            
		        FileOutputStream out;
		        String img_path;

		        if (pageCounter>8)
		        	img_path = destPath + "slide"  + (pageCounter+1) + ".jpg";
		        else
		        	img_path = destPath + "slide0"  + (pageCounter+1) + ".jpg";

		        out = new FileOutputStream(img_path);
 
		        ImageIOUtil.writeImage(dimg, "jpg", out, 128);
		        out.close();

                pageCounter=pageCounter+1;
	      }
	      document.close();
	      return "";
	           
		} catch (Exception e1) {
			logger.error("ERR.PU.EIM.2007.a:" + e1.getMessage());
			assert document != null;
			document.close();
			return "-1";		
		}  
		
    }
	 private void setImageSizes(int sWidth, int sHeight, String slideSize) {
	      
		    double ImageAspectRatio = Integer.parseInt(slideSize) / (double) sWidth;
		    
		    slideWidth = Integer.parseInt(slideSize);
		    slideHeight =  (int) (sHeight * ImageAspectRatio) ;
	 	    	
	 }

}
