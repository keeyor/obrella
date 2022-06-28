/* 
     Author: Michael Gatzonis - 6/2/2021 
     live
*/
package org.opendelos.live.services.scheduler;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.bson.types.ObjectId;
import org.opendelos.live.services.resource.Utils.MultimediaAnalyzeComponent;
import org.opendelos.live.services.resource.ResourceService;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.ResourceAccess;
import org.opendelos.model.structure.StreamingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class SchedulerUpdateAspects {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerUpdateAspects.class);

	private final ResourceService resourceService;
	private final LiveUtils liveUtils;
	private final StreamingProperties streamingProperties;
	private final MultimediaAnalyzeComponent multimediaAnalyzeComponent;
	private final LiveService liveService;

	public SchedulerUpdateAspects(ResourceService resourceService, LiveUtils liveUtils, StreamingProperties streamingProperties, MultimediaAnalyzeComponent multimediaAnalyzeComponent, LiveService liveService) {
		this.resourceService = resourceService;
		this.liveUtils = liveUtils;
		this.streamingProperties = streamingProperties;
		this.multimediaAnalyzeComponent = multimediaAnalyzeComponent;
		this.liveService = liveService;
	}

	/********************************* POINTCUTS *************************************************************/

	/* LIVE SERVICE */
	@Pointcut("execution(* org.opendelos.live.services.wowza.WowzaRestService.STREAM_STOP(..)) && " + "args(resource, streamingServer)")
	public void afterStreamStop(Resource resource, StreamingServer streamingServer){}

	/* ************************************************** ADVICES ********************************************/

	/* LIVE SERVICES */
	@AfterReturning(value = "afterStreamStop(resource, streamingServer)", argNames = "resource,streamingServer")
	public void createVodAndCleanup(Resource resource, StreamingServer streamingServer) {

		//SKIP getFiles if Using RECORDER SERVER AND THIS IS NOT A RECORDER
		boolean actualGet = true;
		boolean useRecorderServer = streamingProperties.isUse_recorder();
		Map<String, StreamingServer> recordingServersMap = liveService.getStreamingServersHM("true","recorder");

		if (useRecorderServer && recordingServersMap.isEmpty()) {
			logger.trace("No valid Recorders found. Disabling RS use!");
			useRecorderServer = false;
		}

		if (useRecorderServer && !streamingServer.getType().equals("recorder")) {
			actualGet = false;
			logger.trace(" Abort VoD process. Using RECORDER SERVER. This is:" + streamingServer.getType());
		}

		if (actualGet && resource.isRecording()) {
			//1. Get Video files
			File[] recorded_files =  liveUtils.getRecordedVideoFiles(resource.getId());
			if (recorded_files != null) {
				boolean multipleParts = recorded_files.length > 1;
				int v_SegmentNumber = 0;

				for (File vFile : recorded_files) {

					ObjectId newResourceId = new ObjectId(); //Generates unique id

					File videoFile = new File(vFile.getAbsolutePath());
					String videoFileName = videoFile.getName();
					if (!videoFile.isFile()) {
						continue;
					}

					String sub_dir_by_year_month_and_id = LocalDate.now().getMonthValue() + "-" + LocalDate.now()
							.getYear() + "/" + newResourceId; //+ resource.getId();
					String videoDstDir = streamingProperties.getAbsDir() + sub_dir_by_year_month_and_id;

					File videoDstFolder = new File(videoDstDir);
					try {
						FileUtils.moveFileToDirectory(videoFile, videoDstFolder, true);
					}
					catch (IOException ioe) {
						logger.error("Error moving recorded file: " + videoFileName + " to destination folder");
						continue;
					}
					//process video
					ResourceAccess resourceAccess;
					try {
						resourceAccess = multimediaAnalyzeComponent.ProcessMultimediaFile(videoDstDir, videoFileName, sub_dir_by_year_month_and_id);
						multimediaAnalyzeComponent.GenerateThumbnail(resourceAccess, videoDstDir, videoFileName, resource.getId());
					}
					catch (Exception e) {
						logger.error("Error processing recorded file: " + videoFile.getName() + ". Skipping");
						continue;
					}
					if (multipleParts) {
						v_SegmentNumber++;
					}
					//2. Create VOD (just copy live entry to resources collection
					Resource vod_resource = new Resource();
					BeanUtils.copyProperties(resource, vod_resource);
					// --> SOS
					vod_resource.setId(newResourceId.toString());
					vod_resource.setIdentity(null);
					vod_resource.setStorage(newResourceId.toString());
					// < -- SOS
					vod_resource.setRealDuration(resourceAccess.getDuration());
					vod_resource.setResourceAccess(resourceAccess);
					vod_resource.getStatus().setInclMultimedia(1);

					String videoFileName_woExtension = FilenameUtils.removeExtension(videoFileName);
					String[] split = videoFileName_woExtension.split("_");
					String v_SegmentFullTime = split[2];    // in Wowza time format == yyyy-MM-dd-HH.mm.ss.SSS-zzz
					String v_SegmentSimpleTime = v_SegmentFullTime.substring(0, v_SegmentFullTime.lastIndexOf("."));

					vod_resource.setTitle(vod_resource.getTitle());
					/* the exact time that the recording started :: use this instead in the title */
					vod_resource.setRecTime(v_SegmentSimpleTime.replace(".", ":"));
					//vod_resource.setId(null);

					if (multipleParts) {
						vod_resource.setParts(true);
						vod_resource.setPartNumber(v_SegmentNumber);
					}
					if (resource.getPublication().equals("public")) {
						vod_resource.setAccessPolicy("public");
					}
					else {
						vod_resource.setAccessPolicy("private");
					}
					resourceService.update(vod_resource);
					logger.trace("New VoD for Broadcast: " + vod_resource.getStreamName());
				} //For each video file
				logger.info("#VoDs for Room: " + resource.getStreamName() + " :: " + recorded_files.length);
			}
			else {
				logger.info("No VoD created for Broadcast: " + resource.getStreamName());
			}
		}
	}
}
