/* 
     Author: Michael Gatzonis - 4/26/2020 
     napro
*/
package org.opendelos.control.mvc.admin.content;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opendelos.model.properties.MultimediaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImageUploadController {

	private static final Logger logger = LoggerFactory.getLogger(ImageUploadController.class);

	private final MultimediaProperties multimediaProperties;

	public ImageUploadController(MultimediaProperties multimediaProperties) {
		this.multimediaProperties = multimediaProperties;
	}

	@RequestMapping(value = {"/secure/image_upload" ,"/secure/image_upload/"}, method = RequestMethod.POST)
	public String formUploadResource(
			@RequestBody MultipartFile file,
			@RequestParam String name,
			@RequestParam(value = "target") String target,
			@RequestParam(value = "id") String id,
			@RequestParam(required=false, defaultValue="-1") int chunks,
			@RequestParam(required=false, defaultValue="-1") int chunk, Locale locale)  {

		String result = "";

		String uploadPath = multimediaProperties.getEventAbsDir();
		logger.trace("Uploading Image to:" + uploadPath);

		if (!new File(uploadPath).isDirectory()) {
			logger.error("unknown uploadpath:" + uploadPath);
			return "Uknown Upload Path";
		}

		// Check upload file extension
		String _ext = FilenameUtils.getExtension(name).toLowerCase();
		if (!_ext.equals("jpg") && !_ext.equals("jpeg")) {
			logger.error("Uknown extension");
			return "Uknown extension";
		}
		//HANDLE UPLOADED DATA
		logger.trace("Uploading Image Path to:" + uploadPath + "/" + name);

		Path uploadFilePath = new File(uploadPath + "/" + name).toPath();
		if (chunks > 0 && chunk > 0)	// Write or append uploaded chunk to uploaded file
		{
			//Need to append the bytes in this chunk
			try {
				Files.write(uploadFilePath, file.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				FileUtils.deleteQuietly(new File(uploadPath + "/" + name));
				result = e.getMessage();
				logger.error("Uploading APPEND error:" + e.getMessage());
			}
		}
		else {
			//First chunk: Need to write the bytes in this chunk
			try {
				Files.write(uploadFilePath, file.getBytes(), StandardOpenOption.CREATE);
			} catch (IOException e) {
				result = e.getMessage();
				logger.error("Uploading CREATE error:" + e.getMessage());
			}
		}
		if (result.equals("")) {
			int maxWidth, maxHeight;

			maxWidth = multimediaProperties.getImageMaxWidth();
			maxHeight = multimediaProperties.getImageMaxHeight();

			boolean isImageValidSize;
 			try {
				isImageValidSize = isImageValidSize(uploadPath + "/" + name, maxWidth,maxHeight);
			}
			catch (IOException e) {
				FileUtils.deleteQuietly(new File(uploadPath + "/" + name));
				logger.error("Uploading SIZE error:" + e.getMessage());
				return  e.getMessage();
			}
			if (!isImageValidSize) {
				FileUtils.deleteQuietly(new File(uploadPath + "/" + name));
				logger.error("Uploading SIZE Invalid");
  				return "Uploading SIZE Invalid";
			}
  			else  {
  				try {
					File srcFile = new File(uploadPath + "/" + name);
					if (srcFile.renameTo(new File(uploadPath + "/" + id + ".jpg"))) {
						srcFile = new File(uploadPath + "/" + id + ".jpg");
						File dstDir = new File(uploadPath + id + "/");
						FileUtils.cleanDirectory(dstDir);
						FileUtils.moveFileToDirectory(srcFile, dstDir, true);
						return "";
					}
					else {
						return "error renaming file";
					}
				}
  				catch (IOException ioe) {
  					return "error moving and renaming file";
				}

			}
		}
		else {
			logger.error("SOME Errors. Delete upload file");
			FileUtils.deleteQuietly(new File(uploadPath + "/" + name));
			return result;
		}
	}

	private static boolean isImageValidSize(String srcPath, int maxWidth, int maxHeight) throws IOException {

		BufferedImage source_image = ImageIO.read(new File(srcPath));
		return (source_image.getWidth() <= maxWidth) && (source_image.getHeight() <= maxHeight);
	}

}
