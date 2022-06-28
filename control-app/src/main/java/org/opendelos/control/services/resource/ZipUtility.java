package org.opendelos.control.services.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtility
{
    List<String> fileList = new ArrayList<String>(); 
    private String SOURCE_FOLDER;
	
    public ZipUtility(String out, String source) 
    {
    	SOURCE_FOLDER=source;
    	this.generateFileList(new File(source));
    	this.zipIt(out);
    }
    public ZipUtility(String source) 
    {
    	SOURCE_FOLDER=source;
    }
    /**
     * Zip it
     * @param zipFile output ZIP file location
     */
    public void zipIt(String zipFile){

     byte[] buffer = new byte[1024];
    	
     try{
    		
    	FileOutputStream fos = new FileOutputStream(zipFile);
    	ZipOutputStream zos = new ZipOutputStream(fos);
    		  		
    	for(String file : this.fileList){
   		
    		String source_path = "";
    		ZipEntry ze = null;
    		
    		if (file.endsWith(".xml")) {
    			source_path = SOURCE_FOLDER;
    			ze = new ZipEntry(file);
    		}
    		else if (!file.endsWith("/"))   {
    			source_path = SOURCE_FOLDER;
    			ze = new ZipEntry("slides/" + file);
    		}
    		else {
    			ze = new ZipEntry(file);
    		}
        	zos.putNextEntry(ze);
               
        	
        	
        	
        		
        	FileInputStream in = 
                       new FileInputStream(source_path + file);
       	   
        	int len;
        	while ((len = in.read(buffer)) > 0) {
        		zos.write(buffer, 0, len);
        	}
               
        	in.close();
 
    	}
    		
    	zos.closeEntry();
    	//close it
    	zos.close();
          

    } catch(IOException ex){
       ex.printStackTrace();   
    }
   }
    
    public void zipThese(String zipFile, List<String> inFiles) throws IOException {

        byte[] buffer = new byte[1024];
		FileOutputStream fos;
		ZipOutputStream zipOut = null;

				fos  = new FileOutputStream(zipFile);
				zipOut = new ZipOutputStream(fos);
				for (String sourceFile : inFiles){
					File fileToZip;
					if (sourceFile.endsWith("/")) {						// 'slides/' folder
						zipOut.putNextEntry(new ZipEntry(sourceFile));
						zipOut.closeEntry();
					}
					else {
						if (sourceFile.endsWith(".xml")) {
							 fileToZip = new File(SOURCE_FOLDER + "/" + sourceFile);
						}
						else {
							 fileToZip = new File(SOURCE_FOLDER + "/slides/" + sourceFile);
						}
						FileInputStream fis = new FileInputStream(fileToZip);
						ZipEntry zipEntry;
						if (sourceFile.endsWith(".xml")) {					//'slideSync.xml' file
							 zipEntry = new ZipEntry(fileToZip.getName());
						}
						else {												// slide images *.jpg
							 zipEntry = new ZipEntry("slides/" + fileToZip.getName());
						}
						zipOut.putNextEntry(zipEntry);
						byte[] bytes = new byte[1024];
						int length;
						while((length = fis.read(bytes)) >= 0) {
							zipOut.write(bytes, 0, length);
						}
						fis.close();
					}
				}
				zipOut.close();
				fos.close();
    }
    /**
     * Traverse a directory and get all files,
     * and add the file into fileList  
     * @param node file or directory
     */
    public void generateFileList(File node){
    	
     fileList.add(generateZipEntry(node.getName() + "/"));
     File[] files = node.listFiles();
         for (File file : files) {
             if (file.isFile()) {
            	 fileList.add(generateZipEntry(file.getAbsoluteFile().toString()));
             }
         }
    }

    /**
     * Format the file path for zip
     * @param file file path
     * @return Formatted file path
     */
    private String generateZipEntry(String file){
    	return file.substring(file.lastIndexOf(File.separator)+1);
    }
}
