package org.opendelos.liveapp.services.resource;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.opendelos.liveapp.services.upload.FfmpegUtils;
import org.opendelos.model.properties.MultimediaProperties;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.model.resources.Cuts;
import org.opendelos.model.resources.Presentation;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.ResourceAccess;
import org.opendelos.model.resources.Slide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RealEditingToolkit {


	private static final Logger logger = LoggerFactory.getLogger(RealEditingToolkit.class);

    private final StreamingProperties streamingProperties;
    private final MultimediaProperties multimediaProperties;
    private final ResourceService resourceService;

	@Autowired
	public RealEditingToolkit(StreamingProperties streamingProperties, MultimediaProperties multimediaProperties, ResourceService resourceService) {
		this.streamingProperties = streamingProperties;
		this.multimediaProperties = multimediaProperties;
		this.resourceService = resourceService;
	}

	public Vector<String> createFFmpegCommands (String id) {
		
		Presentation presentation = resourceService.getResourcePresentation(id);
		Vector<String> listOfCommands = new Vector<>();
		Vector<String> listOfCuts = new Vector<>();
		int segment_count = 0;
		
		try {
			String original_media_path = streamingProperties.getAbsDir() + presentation.getFolder() + "/";
			// Before we begin, delete any existing old working dir...
			FileUtils.deleteQuietly(new File(original_media_path + "ffmpeg/"));
			if ( createWorkingDir(original_media_path + "ffmpeg/") == -1 ) {
				return null;
			}
			String original_media_filename = presentation.getFilename();
			String tmp_list_file = original_media_path + "ffmpeg/parts_of_" + id + ".txt";
			String final_cutted_media = original_media_path + "ffmpeg" + "/" + id + "_final_cutted.mp4";
			
			//http://stackoverflow.com/questions/11357945/java-convert-seconds-into-day-hour-minute-and-seconds-using-timeunit
			//https://answers.yahoo.com/question/index?qid=20110228112221AAs9i0l
			String OUTPUT_SEEKING_TIME = "00:00:30";
			int output_seeking_time = convertTimeToSeconds(OUTPUT_SEEKING_TIME);
			
			boolean hasTrimmedFinish = false;
			
			FfmpegUtils utils;
			String ffmpeg_path = multimediaProperties.getFfmpeg();
			utils = new FfmpegUtils(ffmpeg_path);
			utils.executeInquiry(original_media_filename,original_media_path);
			
			String duration = utils.getDuration();
			String delimiter = "\\.";
			String [] parts = duration.split(delimiter);
			String milliseconds = parts[1];
			
			
			Cuts.Trims t = presentation.getCuts().getTrims();
			
			if ( t != null ) {
			
				if ( t.getStart() != null )
					listOfCuts.add(t.getStart().getEnd());
				else
					listOfCuts.add("00:00:00");
				
				if ( t.getFinish() != null ) {
					listOfCuts.add(t.getFinish().getBegin());
					hasTrimmedFinish = true;
				}
				else 
					listOfCuts.add(presentation.getDuration());				

			} else {
				listOfCuts.add("00:00:00");
				listOfCuts.add(presentation.getDuration());
			}
			
			
			if ( presentation.getCuts().getClips() != null )
			for ( Cuts.Clips.Cut c : presentation.getCuts().getClips().getCuts() ) {
				listOfCuts.add(c.getBegin());
				listOfCuts.add(c.getEnd());
			}
			
			
			int[] listOfCutsInSec = new int[listOfCuts.size()];
			
			for ( int i=0; i < listOfCuts.size(); i++ )
				listOfCutsInSec[i] = convertTimeToSeconds(listOfCuts.elementAt(i));

			Arrays.sort(listOfCutsInSec);
			logger.debug("Real Editing: list of cut points in sec: ");
			for (int number : listOfCutsInSec) logger.debug( number + ",");

			
			for ( int i = 0; i < listOfCutsInSec.length; i++ ) {
				int start_cut_time = listOfCutsInSec[i];
				int end_cut_time = listOfCutsInSec[++i];
				logger.debug("Real Editing: Will extract video segment  from " + start_cut_time + " to " + end_cut_time);
				int input_seeking_time = start_cut_time - output_seeking_time;
				segment_count ++;
				String output_segment_path = original_media_path + "ffmpeg" + "/" + id + "_" + segment_count + ".mp4";
				String output_segment_filename = id + "_" + segment_count + ".mp4";
				
				if (start_cut_time <= output_seeking_time) {
					if (hasTrimmedFinish || i != listOfCutsInSec.length-1)
						listOfCommands.add(ffmpeg_path + " -i " + original_media_path + original_media_filename + " -ss " + start_cut_time + " -y -c copy -avoid_negative_ts 1 -to " + end_cut_time + " " + output_segment_path);
					else
						listOfCommands.add(ffmpeg_path + " -i " + original_media_path + original_media_filename + " -ss " + start_cut_time + " -y -c copy -avoid_negative_ts 1 -to " + end_cut_time + "." + milliseconds + " " + output_segment_path);
					
					if ( writeConcatFile (tmp_list_file,output_segment_filename) != 1 )
						return null;
					
				} else {
					if (hasTrimmedFinish || i != listOfCutsInSec.length-1)
						listOfCommands.add(ffmpeg_path + " -ss " + input_seeking_time + " -i " + original_media_path + original_media_filename + " -ss " + output_seeking_time + " -y -c copy -avoid_negative_ts 1 -to " + (end_cut_time-input_seeking_time) + " " + output_segment_path);
					else
						listOfCommands.add(ffmpeg_path + " -ss " + input_seeking_time + " -i " + original_media_path + original_media_filename + " -ss " + output_seeking_time + " -y -c copy -avoid_negative_ts 1 -to " + (end_cut_time-input_seeking_time) + "." + milliseconds + " " + output_segment_path);
						
					if ( writeConcatFile (tmp_list_file,output_segment_filename) != 1 ) 
						return null;
				}
			}			
			
			listOfCommands.add(ffmpeg_path + " -f concat -i " + tmp_list_file + " -y -c copy " + final_cutted_media);
			
			
		} catch (Exception e) {
			logger.error("Real Editing (createFFmpegCommands): " + e.getMessage());
			return null;
		}
		
		return listOfCommands;
	}

	/**
	 * Executes the list of commands for the real editing of a resource
	 * @return 1 on success
	 */
	public int executeFFmpegCommands (Vector<String> listOfCommands) {
		
		Map<String,String> argsMap;
		FfmpegUtils utils;
		Process p;
		
		long e_total_clip_dur_milli = 0; // expected total clip duration
		String ffmpeg_path = multimediaProperties.getFfmpeg();
		try {
			
			for (String cmd:listOfCommands) {
				logger.debug("Real Editing: Running ffmpeg command " + cmd);
				p = Runtime.getRuntime().exec(cmd);
				p.waitFor();

				BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader error_reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line;
                
				while ((line = error_reader.readLine())!= null) {
					logger.debug(line + "\n");
				}
				
				//Check for quality of real cut per clip
				if  ( StringUtils.countOccurrencesOf(cmd, "concat") <= 0 ) {
					
					argsMap = parseFFMpegCommand (cmd);
					logger.debug("Parse ffmpeg command");
					logger.debug("Parse ffmpeg command-->>" + cmd);
					for (@SuppressWarnings("rawtypes") Map.Entry entry : argsMap.entrySet()) {
						logger.debug(entry.getKey() + ": " + entry.getValue());
						logger.debug(entry.getKey() + ": " + entry.getValue());
					}
 
					utils = new FfmpegUtils(ffmpeg_path);
					utils.executeInquiry(argsMap.get("fv_filename"),argsMap.get("fv_dir"));
					
					String r_duration = utils.getDuration(); //real clip duration
					
					long r_duration_milli = convertTimeToMilli(r_duration); //real clip duration in milliseconds
					long e_duration_milli = convertSecToMilli(argsMap.get("clip_duration")); //expected clip duration in milliseconds
					
					e_total_clip_dur_milli = e_total_clip_dur_milli + e_duration_milli;
					logger.debug(" samon Real Editing diff-->>" + Math.abs(r_duration_milli-e_duration_milli));
					if (Math.abs(r_duration_milli-e_duration_milli) > 1000) {
						logger.error("Real Editing: Not qualitative cut!");
						return -1;
					}
				}

			}
			
		} catch (Exception e) {
			logger.error("Real Editing (executeFFmpegCommands): " + e.getMessage());
			return -1;
		}
 
		return 1;
	}
	
	/**
	 * Updates the xml files related to the resource which has just edited
	 * @return 1 on success
	 */
	public int updateXMLFiles (String id) {

		//Get Default Presentation and createa new RealEditedPresentation with updated values (slides, cuts, trims etc).
		//In case of reject: remove, in case of access, copy to old updating values:  duration, realduration
		//AND update resourceAccess

		Presentation presentation;
		Vector<Cuts.Clips.Cut> listOfCuts = new Vector<>();
		Vector<Slide> listOfSlides = new Vector<>();
		Vector<Slide> listOfShiftedSlides = new Vector<>();

		String ffmpeg_path = multimediaProperties.getFfmpeg();
		try {
			
			presentation = resourceService.getResourcePresentation(id);

			String redited_media_path = streamingProperties.getAbsDir() + presentation.getFolder() + "/ffmpeg/";
			String redited_media_filename = id + "_final_cutted.mp4";

			FfmpegUtils utils = new FfmpegUtils(ffmpeg_path);
			utils.executeInquiry(redited_media_filename,redited_media_path);

			String f_redited_duration = utils.getDuration(); // real duration in HH:mm:ss.SSS format 
			String [] flds = f_redited_duration.split("\\.");
			String redited_duration = flds[0]; // real duration in HH:mm:ss format

			Cuts.Trims t = presentation.getCuts().getTrims();
			
			if ( t != null ) {

				if ( t.getStart() != null ) {
					Cuts.Clips.Cut cut = new Cuts.Clips.Cut();
					cut.setBegin(t.getStart().getBegin());
					cut.setEnd(t.getStart().getEnd());
					listOfCuts.add(cut);
				}
				
				if ( t.getFinish() != null ) {
					Cuts.Clips.Cut cut = new Cuts.Clips.Cut();
					cut.setBegin(t.getFinish().getBegin());
					cut.setEnd(t.getFinish().getEnd());
					listOfCuts.add(cut);
				}
			} 

			if ( presentation.getCuts().getClips() != null )
			for ( Cuts.Clips.Cut c : presentation.getCuts().getClips().getCuts() ) {
				Cuts.Clips.Cut cut = new Cuts.Clips.Cut();
				cut.setBegin(c.getBegin());
				cut.setEnd(c.getEnd());
				listOfCuts.add(cut);
			}
			
			
			if ( presentation.getSlides() != null ) {
				for ( Slide s : presentation.getSlides() ) {
					//Create a list with original slides 
					Slide tmp_s = new Slide ();
					tmp_s.setTime(s.getTime());
					tmp_s.setTitle(s.getTitle());
					tmp_s.setUrl(s.getUrl());
					listOfSlides.add(tmp_s);
					//Create a replica of the above list, which will be shifted later...
					Slide tmp_ss = new Slide ();
					tmp_ss.setTime(s.getTime());
					tmp_ss.setTitle(s.getTitle());
					tmp_ss.setUrl(s.getUrl());
					listOfShiftedSlides.add(tmp_ss);
				}
			}
			
				
			for (int l = 0; l < listOfSlides.size(); l++ ) {
				
				if ( !listOfSlides.get(l).getTime().equalsIgnoreCase("-1")) {

					for (Cuts.Clips.Cut listOfCut : listOfCuts) {

						int slide_viewpoint = convertTimeToSeconds(listOfSlides.get(l).getTime());
						int shifted_slide_viewpoint = convertTimeToSeconds(listOfShiftedSlides.get(l).getTime());
						int cut_begin = convertTimeToSeconds(listOfCut.getBegin());
						int cut_end = convertTimeToSeconds(listOfCut.getEnd());

						logger.debug("Real Editing: Examining slide at " + slide_viewpoint + " in comparison with cut (" + cut_begin + "," + cut_end + ")");

						if (slide_viewpoint >= cut_end) {
							int time_shift = cut_end - cut_begin;
							listOfShiftedSlides.get(l)
									.setTime(convertSecondsToTime(shifted_slide_viewpoint - time_shift));
							logger.debug("Real Editing: Slide is on the right of cut so we move it to " + (slide_viewpoint - time_shift));

						}
						else {
							if (slide_viewpoint > cut_begin) {
								int time_shift = slide_viewpoint - cut_begin;
								listOfShiftedSlides.get(l)
										.setTime(convertSecondsToTime(shifted_slide_viewpoint - time_shift));
								logger.debug("Real Editing: Slide is inside the cut so we move it to " + (slide_viewpoint - time_shift));
							}
							else {
								logger.debug("Real Editing: Slide is not effected by the real editing");
							}
						}
					}
				}
			}
			
			
			// Two fixes of the above algorithm...
			for (int i=0; i < listOfShiftedSlides.size(); i++) {
				
				if ( !listOfSlides.get(i).getTime().equalsIgnoreCase("-1")) {
					
					// if >1 slides fall into the same cut, keep only the latest one. (Alternatively, the presence of a slide starts and ends in a cut)
					if ( (i+1) < listOfShiftedSlides.size() ) // don't check the last slide
						if ( listOfShiftedSlides.get(i).getTime().equalsIgnoreCase(listOfShiftedSlides.get(i+1).getTime()) ) {
							listOfShiftedSlides.get(i).setTime("-1");
						}
					
					// if a slide fall into the trim end, hide it!
					if ( listOfShiftedSlides.get(i).getTime().equalsIgnoreCase( redited_duration ) ) {
						listOfShiftedSlides.get(i).setTime("-1");
					}
				}
			}

			logger.debug("Real Editing: Original Slides ");
			for (Slide s: listOfSlides)
				logger.debug("Real Editing: Slide at " + s.getTime());
			
			logger.debug("Real Editing: Shifted Slides ");
			for (Slide s: listOfShiftedSlides)
				logger.debug("Real Editing: Slide at " + s.getTime());

			if ( presentation.getSlides() != null ) {
				int num_of_slides = presentation.getSlides().size();
				presentation.getSlides().clear();
				for ( int i = 0; i < num_of_slides; i++ ) {
					presentation.getSlides().add(i, listOfShiftedSlides.elementAt(i));
				}
			}

			List<Slide> shifted_slides 	= presentation.getSlides();
			String orig_folder 	 		= presentation.getFolder();
			String provider			 	= presentation.getProvider();
			String orig_video_url 	 = presentation.getVideo_url();

			presentation 			 	= new Presentation();

			presentation.setSlides(shifted_slides);
			presentation.setDuration(redited_duration);
			presentation.setFolder(orig_folder + "/ffmpeg");
			presentation.setFilename(redited_media_filename);
			presentation.setProvider(provider);

			String _edited_video_url = orig_video_url.substring(0,orig_video_url.lastIndexOf("/")) + "/ffmpeg/" + redited_media_filename;
			presentation.setVideo_url(_edited_video_url);

			resourceService.updateResourceRealEditingPresentation(id,presentation);

		} catch (Exception e) {
			logger.error("Real Editing (updateXMLFiles): " + e.getMessage());
			e.printStackTrace();
			return -1;
		}
		
		return 1;
	}
		
	/**
	 *  Applies the user approval command in a real editing of a resource
	 * @return 1 on success
	 */
	public int approveNewMedia (String id) {

		try {
			String save = "failed";
			String delation_pres = "failed";
			int delation_subs_dir = -1;
			int replacement;
			int update_vl;
			int delation_dir = -1;

			//TODO: ERROR: NOt UPDATED CORRECTLY: PRESENTATION AND FILE BY ME> SEE AGAiN
			resourceService.acceptResourceRealEditingPresentation(id);
			update_vl = updateVideolecture(id);
			replacement = replaceVideoWithRealEdited(id);
			FileUtils.deleteQuietly(new File(streamingProperties.getAbsDir() + id + "/" + "ffmpeg"));
			
			logger.debug("Real Editing: Approval - Update presentation:" + save);
			logger.debug("Real Editing: Approval - Delete temp presentation:" + delation_pres);
			logger.debug("Real Editing: Approval - Delete subtitles dir:" + delation_subs_dir);
			logger.debug("Real Editing: Approval - Replace video:" + replacement);
			logger.debug("Real Editing: Approval - Update videolecture:" + update_vl);
			logger.debug("Real Editing: Approval - Delete working dir:" + delation_dir);

			return 1;
			
		} catch (Exception e) {
			logger.error("Real Editing (approveNewMedia): " + e.getMessage() );
			return -1;
		}

	}
	
	/**
	 * Applies the user rejection command in a real editing of a resource
	 * @return 1 on success
	 */
	public int rejectNewMedia (String rid) {
		try {
			resourceService.removeResourceRealEditingPresentation(rid);
			FileUtils.deleteQuietly(new File(streamingProperties.getAbsDir() + rid + "/" + "ffmpeg"));
			return 1;
		} catch (Exception e) {
			logger.error("Real Editing (rejectNewMedia): " + e.getMessage() );
			return -1;
		}

	}	
	
	
	private int writeConcatFile (String filename, String line) {
		
		try ( PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true))) ) {
		    out.println("file " + line);
		    return 1;
		} catch (IOException e) {
			logger.error("Real Editing (writeConcatFile): " + e.getMessage() );
			return -1;
		}
	}
	
	
	private int convertTimeToSeconds (String time) {

		int plusSecond = 0;
		time = time.trim();
		
		if (time.contains(".")) {					//throw away milliseconds
			String milli = time.substring(time.indexOf(".")+1);
			if (Integer.parseInt(milli) > 50) {
				plusSecond = 1;
			}
			time = time.substring(0,time.indexOf("."));
		}
		
		String [] flds = time.split(":"); 

		if (flds.length == 1)
				return Integer.parseInt(time);
		
		int hrs = Integer.parseInt(flds[0]);
		int min = Integer.parseInt(flds[1]); 
		int secs = Integer.parseInt(flds[2]) + plusSecond;
		
		return (hrs*3600 + min*60 + secs);
		
	}

	
	private String convertSecondsToTime (int duration) {

	    if (duration < 0) return null;
	    
	    String time;
        int hours = (duration % 86400 ) / 3600 ;
        int minutes = ((duration % 86400 ) % 3600 ) / 60 ;
        int seconds = ((duration % 86400 ) % 3600 ) % 60  ;

        time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
	    
	    return time;

	}	
	
	
	private long convertTimeToMilli (String time) {

	    if (time == null) return -1;
	    
	    time = time.trim();
	    
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	    
	    long milliseconds;
	
		try {

			Date reference_date = sdf.parse("1970-01-01 00:00:00.000");
			Date date = sdf.parse("1970-01-01 " + time);
			milliseconds = date.getTime()-reference_date.getTime();
			
		} catch (ParseException e) {
			logger.error("Real Editing (convertTimeToMilli): " + e.getMessage());
			return -1;
		}
	    
	    return milliseconds;

	}		
	
	
	@SuppressWarnings("unused")
	private String convertMilliToTime(long miliSeconds) {
		
		if (miliSeconds < 0) return null;
		
		int hrs = (int) TimeUnit.MILLISECONDS.toHours(miliSeconds) % 24;
		int min = (int) TimeUnit.MILLISECONDS.toMinutes(miliSeconds) % 60;
		int sec = (int) TimeUnit.MILLISECONDS.toSeconds(miliSeconds) % 60;
		int milli = (int) miliSeconds % 1000;
		
		return String.format("%02d:%02d:%02d.%03d", hrs, min, sec, milli);
	}
	 
	
	private long convertSecToMilli (String seconds) {
		
		if (seconds == null) return -1;
		
		seconds = seconds.trim();
		
		if  (seconds.contains(".")) {
			String [] flds = seconds.split("\\."); 

			int sec = Integer.parseInt(flds[0]);
			int milli = Integer.parseInt(flds[1]);
			
			return (sec* 1000L + milli);
			
		} else {
			int sec = Integer.parseInt(seconds);
			return (sec* 1000L);
		}
	}
	

	private String convertMilliToSec (long miliSeconds) {
		
		if (miliSeconds < 0) return null;
		
		int sec = (int) TimeUnit.MILLISECONDS.toSeconds(miliSeconds);
		int milli = (int) miliSeconds % 1000;
		
		return String.format("%d.%03d",sec, milli);

	}	
	
	
	private Map<String,String> parseFFMpegCommand (String command) {
		
		if ( command == null ) return null;
		
		Map<String,String> argsMap = new HashMap<>();

		try {
			
			command = command.trim();
			
			if ( command.lastIndexOf( " " ) != -1 ) {
				
				String fv_fullpath = command.substring(command.lastIndexOf( " " )); // final video full path
				fv_fullpath = fv_fullpath.trim();
				String fv_filename = fv_fullpath.substring(fv_fullpath.lastIndexOf( "/" )+1);
				String fv_dir = fv_fullpath.substring(0, fv_fullpath.lastIndexOf( "/" )+1);
				
				argsMap.put("fv_fullpath",fv_fullpath);
				argsMap.put("fv_filename",fv_filename);
				argsMap.put("fv_dir",fv_dir);
			}
			
			
			int is_concat_cmd = StringUtils.countOccurrencesOf(command, "concat");

			if (is_concat_cmd <= 0) {
				
				if ( command.lastIndexOf( "-ss" ) != -1 && command.lastIndexOf( "-y" ) != -1 && 
						 command.lastIndexOf( "-to" ) != -1 && command.lastIndexOf( " " ) != -1) {
						
						String start_cut_time = command.substring( command.lastIndexOf("-ss")+3, command.lastIndexOf("-y"));
						argsMap.put("start_cut_time", start_cut_time);
						
						String end_cut_time = command.substring( command.lastIndexOf("-to")+3, command.lastIndexOf(" "));
						argsMap.put("end_cut_time", end_cut_time);
						
/*						
						int occurance = StringUtils.countOccurrencesOf(command, "-ss");
						
						if ( occurance > 1) {
							
							String input_seeking_time = command.substring( command.indexOf("-ss")+3, command.indexOf("-i"));
							String cust_end_cut_time = command.substring( command.lastIndexOf("-to")+3, command.lastIndexOf(" "));
							
							long end_cut_time = convertSecToMilli(cust_end_cut_time) + convertSecToMilli(input_seeking_time);
							
							argsMap.put("end_cut_time", convertMilliToSec(end_cut_time));
							
						} else {
							
							String end_cut_time = command.substring( command.lastIndexOf("-to")+3, command.lastIndexOf(" "));
							
							argsMap.put("end_cut_time", end_cut_time);
						}
*/						
						
						long start_point = convertSecToMilli(argsMap.get("start_cut_time"));
						long end_point = convertSecToMilli(argsMap.get("end_cut_time"));
						
						argsMap.put("clip_duration", convertMilliToSec(end_point-start_point));

					}
			}
			
		} catch (Exception e) {
			logger.error("Real Editing (parseFFMpegCommand): " + e.getMessage());
			return null;
		}
	    
	    return argsMap;

	} 
	
	
	private int createWorkingDir (String name) {

		File theDir = new File(name);
		
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			
		    boolean result;

		    try {
		        result = theDir.mkdir();
		    } 
		    catch(SecurityException se){
		    	logger.error("Real Editing: " + se.getMessage());
		    	return -1;
		    }        
		    if(result) {
		        return 1;  
		    }
		}
		
		return 0;
	}

	private int replaceVideoWithRealEdited (String id) {

		String str_absdir = streamingProperties.getAbsDir();
		String final_video = str_absdir + id + "/ffmpeg/" + id + "_final_cutted.mp4";
		String folder = str_absdir + id + "/";
		
		String origFileName  = this.getVideoFileFromFolder(str_absdir + id + "/");
		
		String original_video =  folder + origFileName;
		
		File final_videofile = new File(final_video);
		File original_videofile = new File(original_video);
		
		if ( final_videofile.exists() &&  original_videofile.exists() ) {
			
		    boolean delation;
		    boolean replacement;

		    try {
		        
		    	delation = original_videofile.delete();
		    	replacement = final_videofile.renameTo(original_videofile);
		    } 
		    catch ( SecurityException se ) {
		    	logger.error("Real Editing: " + se.getMessage());
		    	return -1;
		    }
		    
		    if( delation && replacement ) {
		        return 1;  
		    }
		}
		logger.error("Real Editing (replaceVideoWithRealEdited): Source or Edited file not found");
		return -1;
		
	}
	
	private String getVideoFileFromFolder(String folder) {
		
		 String filename=null;
		 String[] ext = new String[2];
		 
		 ext[0] = "mp4";
		 ext[1] = "MP4";

	 	 Iterator<File> vFileList = FileUtils.iterateFiles(new File(folder),ext, false);
		 while (vFileList.hasNext()) { 
		    	File srcFile= vFileList.next();
			    filename = srcFile.getName();
			    break;
		 }
		 return filename;
	}

	private int updateVideolecture (String id) {
		
 		Resource resource;
		
		try {
			resource = resourceService.findById(id);

			String redited_media_path = streamingProperties.getAbsDir() + id + "/ffmpeg/";
			String redited_media_filename = id + "_final_cutted.mp4";
			
 
			FfmpegUtils utils = new FfmpegUtils(multimediaProperties.getFfmpeg());
			utils.executeInquiry(redited_media_filename,redited_media_path);
			String redited_duration = utils.getDuration();
			
			resource.setRealDuration(redited_duration);
			
			ResourceAccess access = resource.getResourceAccess();
		  	access.setDuration(redited_duration);
		  	resource.setResourceAccess(access);
		  	resourceService.update(resource);
			return 1;
			
		} catch (Exception e) {
			logger.error("Real Editing updateVideolecture): " + e.getMessage());
			return -1;
		}
	}	
	
}
