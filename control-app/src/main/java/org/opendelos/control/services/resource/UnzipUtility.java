
package org.opendelos.control.services.resource;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility extracts files and directories of a standard zip file to
 * a destination directory.
 * @author www.codejava.net
 *
 */
public class UnzipUtility {
	
	private static final Logger logger = LoggerFactory.getLogger(UnzipUtility.class);

    /**
     * Size of the buffer to read/write data
     */
    private static final int BUFFER_SIZE = 4096;
    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    public void unzip(String zipFilePath, String destDirectory)  {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipEntry entry =null;
        ZipInputStream zipIn=null;
        
        try {
         zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
         
         entry = zipIn.getNextEntry();
         
         if (entry==null) entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        }
        catch (Exception e) {
        	try {
				zipIn.close();
			} catch (IOException e1) {
			}
        }
    }
    
    public String unzipA(String file,String OUTPUT_FOLDER) throws FileNotFoundException, IOException, ArchiveException {
    	 
        File inputFile = new File(file);
 
 
        InputStream is = new FileInputStream(inputFile);
        ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream("zip", is);
        ZipEntry entry = null;
                
        try {
        while ((entry = (ZipArchiveEntry) ais.getNextEntry()) != null) {

            if (entry.getName().endsWith("/")) {
                File dir = new File(OUTPUT_FOLDER + File.separator + entry.getName());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                continue;
            }
 
            File outFile = new File(OUTPUT_FOLDER + File.separator + entry.getName());
 
            if (outFile.isDirectory()) {
                continue;
            }
 
            if (outFile.exists()) {
                continue;
            }
 
            FileOutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = ais.read(buffer)) > 0) {
                out.write(buffer, 0, length);
                out.flush();
            }
            out.close();
 
        }
        is.close();
        return "0";
        }
        catch (Exception e) {
        	try {
				is.close();
			} catch (IOException e1) {
				logger.error("IN unzipA:" + e1.getMessage());
			}
        	return e.getMessage();
        }
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}

