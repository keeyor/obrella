package org.opendelos.live.services.resource.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides helper methods for working with ffmpeg
 *
 * @author shaines
 */
public class FfmpegUtils
{
    
	private static final Logger logger = LoggerFactory.getLogger(FfmpegUtils.class);
	
	private String ffmpegCommand;
    private Map<String,String> fieldMap = new HashMap<String,String>();

    public FfmpegUtils(String ffmpegCommandPath)
    {
        this.ffmpegCommand = ffmpegCommandPath;
    }

    public void executeInquiry(String filename, String workingdir)
    {
        logger.trace( "Execute Inquiry for file: " + filename );
       
        try
        {
            logger.info("ffmpegCommand:" + ffmpegCommand);
            // Build the command line
            String[] cmd = {ffmpegCommand, "-i",filename};
            File dir  = new File(workingdir);
            // Execute the command
            Process p = Runtime.getRuntime().exec(cmd,null,dir);
            // Read the response
            BufferedReader input = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
            BufferedReader error = new BufferedReader( new InputStreamReader( p.getErrorStream() ) );

            // Parse the input stream
            String line = input.readLine();
            logger.trace( "ffmpeg execution of: " + filename );
            logger.trace( "on folder: " + dir );
            while( line != null )
            {
                logger.trace( "\t***" + line );
                line = input.readLine();
            }

            // Parse the error stream
            line = error.readLine();
            logger.trace( "Error Stream: " + filename );
            while( line != null )
            {
                // Handle the line
                if( line.startsWith( "FFmpeg version" ) )
                {
                    // Handle the version line:
                    //    FFmpeg version 0.6.2-4:0.6.2-1ubuntu1, Copyright (c) 2000-2010 the Libav developers
                    String version = line.substring( 15, line.indexOf( ", Copyright", 16  ) );
                    fieldMap.put( "version", version );
                }
                else if(line.contains("Duration:"))
                {
                    // Handle Duration line:
                    //    Duration: 00:42:53.59, start: 0.000000, bitrate: 1136 kb/s
                    String duration = line.substring( line.indexOf( "Duration: " ) + 10, line.indexOf( ", start:" ) );
                    fieldMap.put( "duration", duration );

                    String bitrate = line.substring( line.indexOf( "bitrate: " ) + 9 );
                    fieldMap.put( "bitrate", bitrate );
                }
                else if ((line.contains("Stream #0:")) && (line.contains("Video:")))
                {
                    
                	String resolution="";
                	// Get Resolution
                	Pattern pattern = Pattern.compile("\\b\\d{3,4}x\\d{3,4}\\b");
                	Matcher matcher = pattern.matcher(line);
                	while (matcher.find()) {
                		
                		   resolution = matcher.group();
                	       fieldMap.put("resolution",resolution);
                	       
                	       break;
                	}
                	// Get DAR (Dispay Aspect Ratio)
                	pattern = Pattern.compile("\\bDAR (\\d{1,2}:\\d{1,2})\\b");
                	matcher = pattern.matcher(line);
                	if (!matcher.matches() && !resolution.equals("")) {
                		int screenWidth = Integer.parseInt(resolution.substring(0, resolution.indexOf("x")));
                		int screenHeight = Integer.parseInt(resolution.substring(resolution.indexOf("x")+1,resolution.length()));
                		int factor = greatestCommonFactor(screenWidth, screenHeight);

                		double widthRatio = screenWidth / factor;
                		double heightRatio = screenHeight / factor;
                		
                		double  check_aspect = widthRatio / heightRatio;
                		String der_aspect="";
                		if (check_aspect >= 1.55) der_aspect="16:9";
                		else der_aspect = "4:3";
                		//fieldMap.put("aspect-ratio",widthRatio + ":" + heightRatio);
                		fieldMap.put("aspect-ratio",der_aspect);
  
                	}
                	else {
                		while (matcher.find()) {
                	       fieldMap.put("aspect-ratio",matcher.group(1));
                	       break;
                		}
                	}
                	
                }
                // Read the next line
                logger.trace( "\t***" + line );
                line = error.readLine();
            }
        }
        catch( Exception e )
        {
        	logger.error( "ffmpeg failed with error:" + e.getMessage().toString() );
        	fieldMap.put("ffmpeg-error",e.getMessage().toString());
        }

        // Debug: dump fields:
        logger.trace( "Fields:" );
        for( String field : fieldMap.keySet() )
        {
            logger.trace( "\t" + field + " = " + fieldMap.get( field ) );
        }
 
    }

    public String getDuration()
    {
        //Map<String,String> fieldMap = executeInquiry( filename );
        if( fieldMap.containsKey( "duration" ) )
        {
            return fieldMap.get( "duration" );
        }
        return "0:00";
    }
    public String getError()
    {
        //Map<String,String> fieldMap = executeInquiry( filename );
        if( fieldMap.containsKey( "ffmpeg-error" ) )
        {
            return fieldMap.get( "ffmpeg-error" );
        }
        return "0";
    }

    public String getResolution()
    {
        //Map<String,String> fieldMap = executeInquiry( filename );
        if( fieldMap.containsKey( "resolution" ) )
        {
            return fieldMap.get( "resolution" );
        }
        return "000x000";
    }
    public String getAspectRatio()
    {
        //Map<String,String> fieldMap = executeInquiry( filename );
        if( fieldMap.containsKey( "aspect-ratio" ) )
        {
            return fieldMap.get( "aspect-ratio" );
        }
        return "0:0";
    }

    public int generateThumbnails( String filename, File thumbdir, String duration, int width, int height, int count )
    {
        // The following example shows how to generate thumbnails at seconds 4, 8, 12, and 16
        // ffmpeg  -itsoffset -4  -i test.avi -vcodec mjpeg -vframes 1 -an -f rawvideo -s 320x240 test.jpg
        // ffmpeg  -itsoffset -8  -i test.avi -vcodec mjpeg -vframes 1 -an -f rawvideo -s 320x240 test.jpg
        // ffmpeg  -itsoffset -12  -i test.avi -vcodec mjpeg -vframes 1 -an -f rawvideo -s 320x240 test.jpg
        // ffmpeg  -itsoffset -16  -i test.avi -vcodec mjpeg -vframes 1 -an -f rawvideo -s 320x240 test.jpg

        // Have to compute seconds, format is: 00:42:53.59
 
        int hours = Integer.parseInt( duration.substring( 0, 2 ) );
        int minutes = Integer.parseInt( duration.substring( 3, 5 ) );
        int seconds = Integer.parseInt( duration.substring( 6, 8 ) );
        int totalSeconds = hours * 3600 + minutes * 60 + seconds;
        logger.trace( "Total Seconds: " + totalSeconds ); 

        // Create the thumbnails
        String shortFilename = filename;
        if(filename.contains(File.separator))
        {
            // Strip the path
            shortFilename = filename.substring(filename.lastIndexOf(File.separator) + 1);
        }
        if(shortFilename.contains("."))
        {
            // Strip extension
            shortFilename = shortFilename.substring( 0, shortFilename.lastIndexOf( "." ) );
        }

        // Define a shift in seconds
        int shift = 5;

        // The step is the number of seconds to step between thumbnails
        int step = totalSeconds / count;
        
        //Delete previous thumbnail: if exists
        StringBuilder sb1 = new StringBuilder();
        sb1.append(thumbdir.getAbsolutePath() );
        sb1.append(File.separator );
        sb1.append(shortFilename);
        sb1.append( "-" );
        sb1.append(Integer.toString(1));
        sb1.append( ".jpg");
        
        File oldthumb = new File(sb1.toString());
        if (oldthumb.isFile()) 
        	{ 	oldthumb.delete();
        		logger.trace("Old Thumbnail Deleted:" + sb1.toString());
        	}
        for( int index = 0; index < count; index++ )
        {
            // Build the command
            StringBuilder sb = new StringBuilder();
            sb.append( ffmpegCommand );
            sb.append( " -itsoffset -" );
            sb.append( Integer.toString( shift + ( index * step ) ) );
            sb.append( " -i " );
            sb.append( "" + filename + "");
            sb.append( " -vcodec mjpeg -vframes 1 -an -f rawvideo -s " );
            sb.append( Integer.toString( width ) );
            sb.append( "x" );
            sb.append( Integer.toString( height ) );
            sb.append( " " );
            sb.append( thumbdir.getAbsolutePath() );
            sb.append( File.separator );
            sb.append( "" + shortFilename );
            sb.append( "-" );
            sb.append( Integer.toString( index + 1 ) );
            sb.append( ".jpg");
            
        	logger.trace("Thumbnail Command:" + sb.toString());
        	
        	
        	File file = new File(filename);
        	String absolutePath = file.getAbsolutePath();
        	String filePath = absolutePath.
        	    substring(0,absolutePath.lastIndexOf(File.separator));
        	File dir = new File(filePath);
        	
            try
            {
            	// Execute the command
                Process p = Runtime.getRuntime().exec(sb.toString(),null,dir);
 
                // Detach from the process
                p.getOutputStream().close();
                consumeStream( p.getInputStream() );
                consumeStream( p.getErrorStream() );
            }
            catch( Exception e )
            {
                logger.error("Thumbnail creation failed with error" + e.getMessage());
                return -1;
            }
        }
        return 1;
    }

    public String generateThumbnailOnTime( String filename, File thumbdir, String whereTo, int width, int height) throws IOException {

        String bOutput = whereTo.replace(":", "_");
        bOutput.substring(0,bOutput.lastIndexOf("."));
        // Build the command
        StringBuilder sb = new StringBuilder();
        sb.append( ffmpegCommand );
        sb.append( " -i " );
        sb.append( "" + filename + "");
        sb.append( " -ss " );
        sb.append( whereTo );
        sb.append( " -y -xerror -vcodec mjpeg -vframes 1 -an -f rawvideo -s " );
        sb.append( Integer.toString( width ) );
        sb.append( "x" );
        sb.append( Integer.toString( height ) );
        sb.append( " " );
        sb.append( thumbdir.getAbsolutePath() );
        sb.append( File.separator );
        sb.append( "" + bOutput + ".jpg" + " -abort_on empty_output");

        logger.info(" ffmpeg command:" + sb.toString());

        File file = new File(filename);
        String absolutePath = file.getAbsolutePath();
        String filePath = absolutePath.
                substring(0,absolutePath.lastIndexOf(File.separator));
        File dir = new File(filePath);

        Process p=null;
        try
        {
            // Execute the command
            p = Runtime.getRuntime().exec(sb.toString(),null,dir);
            // Detach from the process
            BufferedReader input = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
            BufferedReader error = new BufferedReader( new InputStreamReader( p.getErrorStream() ) );

            // Parse the input stream
            String line = input.readLine();
            while( line != null )
            {
                logger.info( "\t***" + line );
                line = input.readLine();
            }
            line = error.readLine();
            logger.info( "Error Stream: " + filename );
            while( line != null ) {
                logger.info( "\t***" + line );
                line = error.readLine();
            }

        }
        catch( Exception e )
        {
            logger.error("Thumbnail creation failed with error" + e.getMessage());
        }

        return "1";
    }


    /**
     * Helper method that reads a stream until it is complete and returns its contents
     * as a String
     *
     * @param is        The input stream to read from
     * @return          The response of the input stream as a String
     */
    protected String consumeStream( InputStream is )
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
            String line = br.readLine();
            while( line != null )
            {
                sb.append( line );
                line = br.readLine();
                logger.trace("ffmpeg: " + line);
            }
        }
        catch( Exception e )
        {
            logger.error("FFMPEG Error: consume Stream");
        }
        return sb.toString();
    }


    public String getFfmpegCommand()
    {
        return ffmpegCommand;
    }

    public void setFfmpegCommand(String ffmpegCommand)
    {
        this.ffmpegCommand = ffmpegCommand;
    }
    private int greatestCommonFactor(int a, int b) {
    	return (b == 0) ? a : greatestCommonFactor(b, a % b);
    }

    public int watermarkVideo(String finalResourceFolder, String filename,  String watermark_image_path)
    {
        // Create the thumbnails
        String shortFilename = filename;
        if(filename.contains(File.separator))
        {
            // Strip the path
            shortFilename = filename.substring(filename.lastIndexOf(File.separator) + 1);
        }
        if(shortFilename.contains("."))
        {
            // Strip extension
            shortFilename = shortFilename.substring( 0, shortFilename.lastIndexOf( "." ) );
        }
            // Build the command
            StringBuilder sb = new StringBuilder();
            sb.append( ffmpegCommand );
            sb.append( " -i " );
            sb.append( "\"" + filename + "\"");
            sb.append( " -i " );
            sb.append( " \"" + watermark_image_path + "\"" );
            sb.append( " -filter_complex \"[1]format=rgba,colorchannelmixer=aa=0.2[logo];[0][logo]overlay=(W-w)/2:(H-h)/2:format=auto,format=yuv420p\" -c:a copy ");
            sb.append( " \"" + finalResourceFolder + shortFilename + "-wm.mp4\"");

            logger.info("Watermark Command:" + sb.toString());

            File file = new File(filename);
            String absolutePath = file.getAbsolutePath();
            String filePath = absolutePath.
                    substring(0,absolutePath.lastIndexOf(File.separator));
            File dir = new File(filePath);
            logger.info("Working dir:" + dir);

            Process p = null;
            try
            {
                // Execute the command
                p = Runtime.getRuntime().exec(sb.toString(),null,dir);
                p.getOutputStream().close();
                ReadStream s1 = new ReadStream("stdin", p.getInputStream ());
                ReadStream s2 = new ReadStream("stderr", p.getErrorStream ());
                s1.start ();
                s2.start ();
                p.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            } finally {
                if(p != null)
                    p.destroy();
            }
            return 1;
    }
}