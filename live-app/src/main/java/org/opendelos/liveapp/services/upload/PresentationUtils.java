/* 
     Author: Michael Gatzonis - 4/1/2019 
     OpenDelosDAC
*/
package org.opendelos.liveapp.services.upload;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class PresentationUtils {

    private static Logger logger = Logger.getLogger(PresentationUtils.class.getName());


    public static boolean allowedSlideSize(double slideWidth, double slideHeight) {
        // Extract Slide Images Office 2003 case

        double SlideAspectRatio = slideWidth / slideHeight;

        return (SlideAspectRatio > 1.29) && (SlideAspectRatio < 1.8);

    }
    public static void saveImageToDesiredSizeFromImagePath(String sourcePath, String destPath, String imageFileName, int width, int height) throws IOException {

        String source_image_path = sourcePath + imageFileName;

        BufferedImage resized_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        resized_image.createGraphics().drawImage(ImageIO.read(new File(source_image_path)).getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH), 0, 0, null);

        String image_path = destPath + imageFileName;

        File destinationFolder = new File(destPath);
        if (!destinationFolder.isDirectory()) {
            boolean createFolders = destinationFolder.mkdirs();
            if (!createFolders) {
                logger.severe("Could create dir:" + destPath);
                throw new SecurityException();
            }
        }
        FileOutputStream resized_image_os;
        resized_image_os = new FileOutputStream(image_path);
        ImageIO.write(resized_image, "jpg", resized_image_os);
        resized_image_os.close();
    }

}
