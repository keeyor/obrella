/* 
     Author: Michael Gatzonis - 14/2/2022 
     obrella
*/
package org.opendelos.control.services.youtube;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CdnSettings;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastContentDetails;
import com.google.api.services.youtube.model.LiveBroadcastSnippet;
import com.google.api.services.youtube.model.LiveBroadcastStatus;
import com.google.api.services.youtube.model.LiveStream;
import com.google.api.services.youtube.model.LiveStreamContentDetails;
import com.google.api.services.youtube.model.LiveStreamSnippet;
import com.google.api.services.youtube.model.MonitorStreamInfo;
import org.opendelos.control.services.wowza.WowzaRestService;
import org.opendelos.control.conf.YouTubeProperties;
import org.opendelos.control.services.scheduledEvent.ScheduledEventService;
import org.opendelos.control.services.scheduler.ScheduleService;
import org.opendelos.control.services.structure.ClassroomService;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.scheduler.Schedule;
import org.opendelos.model.scheduler.common.YouTubeBroadcast;
import org.opendelos.model.structure.Classroom;
import org.opendelos.model.users.OoUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

@Service
public class YouTubePublicationService {


	private final boolean DEBUG = false;
	private final Logger logger = LoggerFactory.getLogger(YouTubePublicationService.class);
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

	private static final String APPLICATION_NAME = "OpenDelos Development";

	private final ScheduleService scheduleService;
	private final ScheduledEventService scheduledEventService;
	private final ClassroomService classroomService;
	private final WowzaRestService wowzaRestService;
	private final YouTubeProperties youTubeProperties;

	@Autowired
	public YouTubePublicationService(ScheduleService scheduleService, ScheduledEventService scheduledEventService, ClassroomService classroomService, WowzaRestService wowzaRestService, YouTubeProperties youTubeProperties) {
		this.scheduleService = scheduleService;
		this.scheduledEventService = scheduledEventService;
		this.classroomService = classroomService;
		this.wowzaRestService = wowzaRestService;
		this.youTubeProperties = youTubeProperties;
	}


	public static YouTube getService(Credential credential) throws GeneralSecurityException, IOException {
		final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME)
				.build();
	}

	public Credential getSingedInUserGoogleCredentials() throws IOException, GeneralSecurityException {

		OoUserDetails ooUserDetails = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		DataStoreFactory DATA_STORE_FACTORY=new FileDataStoreFactory(new File(youTubeProperties.getDatastore()));

		final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		// Load client secrets.
		File file = ResourceUtils.getFile(youTubeProperties.getClientSecretsFile());
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader(file.getAbsolutePath()));
		//try to get credentials
		Collection<String> SCOPES = youTubeProperties.getScopes();
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).build();

		return  flow.loadCredential(ooUserDetails.getUid());
	}

	public String getGoogleClientId() throws IOException, GeneralSecurityException {
		// Load client secrets.
		File file = ResourceUtils.getFile(youTubeProperties.getClientSecretsFile());
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader(file.getAbsolutePath()));

		if (clientSecrets != null) {
			return clientSecrets.getDetails().getClientId();
		}
		else {
			return null;
		}
	}

	public String ScheduleYoutubeLiveBroadcast(Credential credential, String scheduleId) throws IOException, GeneralSecurityException {

				String liveBroadcastId;
				String youtube_stream_name;

				YouTube youTubeService = getService(credential);
				Schedule schedule = scheduleService.findById(scheduleId);
				String scheduledEventId = schedule.getEvent();
				ScheduledEvent scheduledEvent = scheduledEventService.findById(scheduledEventId);
				Classroom classroom = classroomService.findById(schedule.getClassroom());

				String liveStreamTitle = scheduledEvent.getTitle();
				String liveStreamDescription = "Ζωντανή Μετάδοση Εκδήλωσης" + " από " + classroom.getName();

				//# create Live Broadcast
				int startHour = Integer.parseInt(schedule.getStartTime().substring(0,2));
				int startMinute = Integer.parseInt(schedule.getStartTime().substring(3,5));
				LocalDateTime broadcastStartDateTime = schedule.getDate().atTime(startHour,startMinute);

				int durationHours = schedule.getDurationHours();
				int durationMinutes = schedule.getDurationMinutes();

				if (!DEBUG) {
					LiveBroadcast liveBroadcast = createLiveBroadcast(youTubeService, liveStreamTitle, liveStreamDescription, broadcastStartDateTime, durationHours, durationMinutes);

					liveBroadcastId = liveBroadcast.getId();
					logger.info("Created LiveBroadcast with id {}", liveBroadcastId);
					//# create Live Stream
					LiveStream liveStream = createLiveStream(youTubeService);
					String liveStreamId = liveStream.getId();
					logger.info("Created LiveStream with id {} & title {}", liveStreamId, liveStreamTitle);
					//# bind Live Broadcast to Live Stream
					bindBroadcast2Stream(youTubeService, liveBroadcastId, liveStreamId);
					logger.info("Bound Broadcast {} to Steam {}", liveBroadcastId, liveStreamId);
					youtube_stream_name = liveStream.getCdn().getIngestionInfo().getStreamName();
				}
				else {
					//To DEBUG
					youtube_stream_name = "youtube_stream"; /// actual ==> liveStream.getCdn().getIngestionInfo().getStreamName()
					liveBroadcastId = "_test_broadcast_id";
				}

				//# create Stream Target to Wowza Server
				//Note: we have to add time to streamName in case there are many broadcasts from the same room
				//Make date and time variable

				int year  		= 	broadcastStartDateTime.getYear();
				int month  		=	broadcastStartDateTime.getMonthValue();
				int dayOfYear 	= 	broadcastStartDateTime.getDayOfYear();

				StringBuilder wowza_streamName_target = new StringBuilder();
				wowza_streamName_target.append(classroom.getCode()).append("@").append(startHour).append(startMinute).append(dayOfYear).append(month).append(year);

				wowzaRestService.createStreamTargetInWowza(wowza_streamName_target.toString(), youtube_stream_name);
				logger.info("Created Wowza Publication Target for Stream {}", wowza_streamName_target);

				//# Update Scheduler entry with youtube publication info
				this.UpdateScheduleEntryWithYouTubePublicationInfo(scheduleId,liveBroadcastId);
				return liveBroadcastId;
	}

	public void UnScheduleYoutubeLiveBroadcast(Credential credential, String broadcastId, String scheduleId) throws IOException, GeneralSecurityException {

		YouTube youTubeService = getService(credential);
		Schedule schedule = scheduleService.findById(scheduleId);
		Classroom classroom = classroomService.findById(schedule.getClassroom());
		this.deleteBroadcast(youTubeService,broadcastId);

		int startHour = Integer.parseInt(schedule.getStartTime().substring(0,2));
		int startMinute = Integer.parseInt(schedule.getStartTime().substring(3,5));
		LocalDateTime scheduledDateTime = schedule.getDate().atTime(startHour,startMinute);

		int year  = scheduledDateTime.getYear();
		int month  =scheduledDateTime.getMonthValue();
		int dayOfYear = scheduledDateTime.getDayOfYear();

		wowzaRestService.deleteStreamTargetFromWowza(classroom.getCode() + "@" + startHour + startMinute + dayOfYear + month + year);
		this.UpdateScheduleEntryWithYouTubePublicationCancellation(scheduleId);
	}

	public LiveBroadcast createLiveBroadcast(YouTube youtubeService, String title, String description, LocalDateTime broadcastStartDateTime, int durationHours, int durationMinutes)  throws GeneralSecurityException, IOException  {


		// Define the LiveBroadcast object, which will be uploaded as the request body.
		LiveBroadcast liveBroadcast = new LiveBroadcast();

		// Add the contentDetails object property to the LiveBroadcast object.
		LiveBroadcastContentDetails contentDetails = new LiveBroadcastContentDetails();
		contentDetails.setEnableClosedCaptions(false);
		contentDetails.setEnableContentEncryption(true);
		contentDetails.setEnableDvr(true);
		contentDetails.setEnableEmbed(false);
		contentDetails.setRecordFromStart(true);
		contentDetails.setStartWithSlate(false);
		contentDetails.setEnableAutoStart(true);

		MonitorStreamInfo monitorStreamInfo = new MonitorStreamInfo();
		monitorStreamInfo.setEnableMonitorStream(false);
		contentDetails.setMonitorStream(monitorStreamInfo);
		liveBroadcast.setContentDetails(contentDetails);

		// Add the snippet object property to the LiveBroadcast object.
		LiveBroadcastSnippet snippet = new LiveBroadcastSnippet();
		// Scheduled Start & End Time
		DateTime startDateTime = this.calcLiveYoutubeBroadcastStartDateTime(broadcastStartDateTime);
		snippet.setScheduledStartTime(startDateTime);

		DateTime endDateTime = this.calcLiveYoutubeBroadcastEndDateTime(broadcastStartDateTime,durationHours,durationMinutes);
		snippet.setScheduledEndTime(endDateTime);

		snippet.setTitle(title);
		snippet.setDescription(description);
		liveBroadcast.setSnippet(snippet);
		// Add the status object property to the LiveBroadcast object.
		LiveBroadcastStatus status = new LiveBroadcastStatus();
		status.setPrivacyStatus("private");
		status.setSelfDeclaredMadeForKids(false);
		liveBroadcast.setStatus(status);

		// Define and execute the API request
		YouTube.LiveBroadcasts.Insert request = youtubeService.liveBroadcasts().insert("snippet,contentDetails,status", liveBroadcast);


		return request.execute();
	}

	public LiveStream createLiveStream(YouTube youtubeService) throws GeneralSecurityException, IOException  {

		// Define the LiveStream object, which will be uploaded as the request body.
		LiveStream liveStream = new LiveStream();

		// Add the cdn object property to the LiveStream object.
		CdnSettings cdn = new CdnSettings();
		cdn.setFrameRate(youTubeProperties.getFramerate());
		cdn.setIngestionType(youTubeProperties.getIngestionType());
		cdn.setResolution(youTubeProperties.getResolution());
		liveStream.setCdn(cdn);

		// Add the contentDetails object property to the LiveStream object.
		LiveStreamContentDetails contentDetails = new LiveStreamContentDetails();
		contentDetails.setIsReusable(false);
		liveStream.setContentDetails(contentDetails);

		// Add the snippet object property to the LiveStream object.
		LiveStreamSnippet snippet = new LiveStreamSnippet();
		// Dummy (it does not matter or have find where it appears!)
		snippet.setDescription("stream_description");
		snippet.setTitle("stream_title");
		liveStream.setSnippet(snippet);

		// Define and execute the API request
		YouTube.LiveStreams.Insert request = youtubeService.liveStreams().insert("snippet,cdn,contentDetails,status", liveStream);

		return request.execute();
	}
	public void bindBroadcast2Stream(YouTube youtubeService, String broadcastId, String streamId) throws GeneralSecurityException, IOException {

		YouTube.LiveBroadcasts.Bind request = youtubeService.liveBroadcasts().bind(broadcastId, "snippet,contentDetails,status");
		request.setStreamId(streamId).execute();
	}

	public void UpdateScheduleEntryWithYouTubePublicationInfo(String scheduleId, String broadcastId) {

		Schedule schedule = scheduleService.findById(scheduleId);
		YouTubeBroadcast youTubeBroadcast = new YouTubeBroadcast();
		youTubeBroadcast.setBroadcast(true);
		youTubeBroadcast.setBroadcastId(broadcastId);

		schedule.setYouTubeBroadcast(youTubeBroadcast);
		scheduleService.update(schedule);
	}

	public void UpdateScheduleEntryWithYouTubePublicationCancellation(String scheduleId) {

		Schedule schedule = scheduleService.findById(scheduleId);
		schedule.setYouTubeBroadcast(null);
		scheduleService.update(schedule);
	}

	public void deleteBroadcast(YouTube youtubeService, String broadcastId) throws GeneralSecurityException, IOException {

		YouTube.LiveBroadcasts.Delete request = youtubeService.liveBroadcasts().delete(broadcastId);
		request.execute();
	}

	private DateTime calcLiveYoutubeBroadcastStartDateTime(LocalDateTime localStartTime) {

		//# Set Youtube schedule Time "minusMinutes" before actual (Scheduled) time
		int startMinutesBefore = youTubeProperties.getMinutesBefore();
		localStartTime = localStartTime.minus(startMinutesBefore, ChronoUnit.MINUTES);
		long scheduledStartMilli = localStartTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		//# Conversion to com.google.api.client.util.DateTime
		Date scheduledDate = new Date(scheduledStartMilli);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		sdf.setTimeZone(TimeZone.getDefault());
		String startText = sdf.format(scheduledDate);
		logger.info("YouTube :: Broadcast Scheduled for {}",startText);

		return  new DateTime(startText);
	}

	private DateTime calcLiveYoutubeBroadcastEndDateTime(LocalDateTime localStartTime, int durationHours, int durationMinutes) {

		// Stop broadcast "minutesAfter" minute after scheduled end time
		int endMinutesAfter = youTubeProperties.getMinutesAfter();
		LocalDateTime broadcastEndTime = localStartTime.plus(durationHours, ChronoUnit.HOURS).plus(durationMinutes, ChronoUnit.MINUTES).plus(endMinutesAfter, ChronoUnit.MINUTES);
		long scheduledEndMilli = broadcastEndTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		//# Conversion to com.google.api.client.util.DateTime
		Date scheduledEndDate = new Date(scheduledEndMilli);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		sdf.setTimeZone(TimeZone.getDefault());
		String endText = sdf.format(scheduledEndDate);
		logger.info("YouTube :: Broadcast End Scheduled for {}",endText);

		return new DateTime(endText);
	}
}
