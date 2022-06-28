/* 
     Author: Michael Gatzonis - 23/11/2020 
     live
*/
package org.opendelos.eventsapp.services.resource;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.opendelos.eventsapp.conf.ChannelProperties;
import org.opendelos.model.common.CcLicense;
import org.opendelos.model.properties.MultimediaProperties;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.structure.StreamingServer;
import org.opendelos.eventsapp.services.i18n.MultilingualServices;
import org.opendelos.eventsapp.services.structure.StreamingServerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class ResourceUtils {

	static final int MINUTES_PER_HOUR = 60;
	static final int SECONDS_PER_MINUTE = 60;
	static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

	private final MultilingualServices multilingualServices;
	private final StreamingServerService streamingServerService;
	private final ChannelProperties channelProperties;
	private final MultimediaProperties multimediaProperties;
	private final StreamingProperties streamingProperties;

	private static String static_zone_tag;

	@Value("${app.zone}")
	public void setZone(String zone) {
		static_zone_tag = zone;
	}

	@Autowired
	public ResourceUtils(MultilingualServices multilingualServices, StreamingServerService streamingServerService, ChannelProperties channelProperties, MultimediaProperties multimediaProperties, StreamingProperties streamingProperties) {
		this.multilingualServices = multilingualServices;
		this.streamingServerService = streamingServerService;
		this.channelProperties = channelProperties;
		this.multimediaProperties = multimediaProperties;
		this.streamingProperties = streamingProperties;
	}

	public StringBuilder getStreamingBaseWebPath() {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(streamingProperties.getProtocol()).append("://")
				.append(streamingProperties.getHost());
				if (!streamingProperties.getPort().equals("443")) {
					stringBuilder.append(":").append(streamingProperties.getPort());
				}
				stringBuilder.append(streamingProperties.getWebDir());

		return stringBuilder;
	}

	public StringBuilder getMultimediaBaseWebPath() {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(multimediaProperties.getProtocol()).append("://")
				.append(multimediaProperties.getHost());
		if (!multimediaProperties.getPort().equals("443")) {
			stringBuilder.append(":").append(multimediaProperties.getPort());
		}
		stringBuilder.append(multimediaProperties.getWebDir());

		return stringBuilder;
	}

	public StringBuilder getEventFilesBaseWebPath() {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(streamingProperties.getProtocol()).append("://")
				.append(streamingProperties.getHost());
		if (!streamingProperties.getPort().equals("443")) {
			stringBuilder.append(":").append(streamingProperties.getPort());
		}
		stringBuilder.append(multimediaProperties.getEventWebDir());

		return stringBuilder;
	}

	public StringBuilder getSupportFilesBaseWebPath() {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(streamingProperties.getProtocol()).append("://")
				.append(streamingProperties.getHost());
		if (!streamingProperties.getPort().equals("443")) {
			stringBuilder.append(":").append(streamingProperties.getPort());
		}
		stringBuilder.append(streamingProperties.getSupport_files_webDir());

		return stringBuilder;
	}

	public String getLiveVideoUrlOfResource(Resource resource) {

		StreamingServer streamingServer = streamingServerService.findById(resource.getStreamingServerId());

		String streaming_protocol = streamingServer.getProtocol();
		String streaming_server_url = streamingServer.getServer();
		String streaming_port = streamingServer.getPort();
		String streaming_application = streamingServer.getApplication();
		String stream_id = resource.getStreamId();
		String streaming_suffix = "playlist.m3u8";

		return streaming_protocol + "://" + streaming_server_url + ":" + streaming_port + "/"
				+ streaming_application + "/" + stream_id + ".stream" + "/" + streaming_suffix;
				//(if transcoded with watermark   + streaming_application + "/" + stream_id + ".stream_720p" + "/" + streaming_suffix;
	}

	public String getLiveChannelVideoUrl() {

		String streaming_server_code = channelProperties.getStreaming_server_code();
		String stream_name = channelProperties.getStream_name();
		StreamingServer streamingServer = streamingServerService.findByCode(streaming_server_code);
		if (streamingServer == null) {
			return null;
		}
		String streaming_protocol = streamingServer.getProtocol();
		String streaming_server_url = streamingServer.getServer();
		String streaming_port = streamingServer.getPort();
		String streaming_application = streamingServer.getApplication();
		String streaming_suffix = "playlist.m3u8";

		return streaming_protocol + "://" + streaming_server_url + ":" + streaming_port + "/"
				+ streaming_application + "/" + stream_name  + "/" + streaming_suffix;
	}

	public String getVideoUrlOfResource(Resource resource) {

		String video_filename = resource.getResourceAccess().getFileName();
		if (streamingProperties.getOverrideResourceUrl() == null) {
			StringBuilder stringBuilder = this.getStreamingBaseWebPath()
					.append(resource.getResourceAccess().getFolder())
					.append("/")
					.append(video_filename);
			return stringBuilder.toString();
		}
		else {
			return streamingProperties.getOverrideResourceUrl() + resource.getResourceAccess()
					.getFolder() + "/" + video_filename;
		}
	}

	public String getMediaBasePathOfResource(Resource resource) {

		StringBuilder stringBuilder = this.getMultimediaBaseWebPath();
		stringBuilder.append(resource.getResourceAccess().getFolder()).append("/");
		return  stringBuilder.toString();
	}

	public CcLicense getLicenseOfResource(Resource resource, String language_tag) {

		String license;
		if (resource.getLicense() == null) { license="by";} //set as default
		else { license = resource.getLicense();}

		CcLicense ccLicense = new CcLicense();
		ccLicense.setType(multilingualServices.getValue("Lic_Title", Locale.forLanguageTag(language_tag)));
		ccLicense.setIdentity(license);
		ccLicense.setTitle(multilingualServices.getValue(license, Locale.forLanguageTag(language_tag)));
		ccLicense.setUrl(multilingualServices.getValue(license + ".url",Locale.forLanguageTag(language_tag)));
		ccLicense.setImage_url(this.getSupportFilesBaseWebPath() + multilingualServices.getValue(license + ".image",Locale.forLanguageTag(language_tag)));
		ccLicense.setIntro_url(this.getSupportFilesBaseWebPath() + multilingualServices.getValue(license + ".intro.mp4",Locale.forLanguageTag(language_tag)));

		return ccLicense;
	}

	public CcLicense getLicenseOfType(String type, String language_tag) {

		String license;
		if (type == null) { license="by";} //set as default
		else { license = type;}

		CcLicense ccLicense = new CcLicense();
		ccLicense.setType(multilingualServices.getValue("Lic_Title", Locale.forLanguageTag(language_tag)));
		ccLicense.setIdentity(license);
		ccLicense.setTitle(multilingualServices.getValue(license, Locale.forLanguageTag(language_tag)));
		ccLicense.setUrl(multilingualServices.getValue(license + ".url",Locale.forLanguageTag(language_tag)));
		ccLicense.setImage_url(this.getSupportFilesBaseWebPath() + multilingualServices.getValue(license + ".image",Locale.forLanguageTag(language_tag)));
		ccLicense.setIntro_url(this.getSupportFilesBaseWebPath() + multilingualServices.getValue(license + ".intro.mp4",Locale.forLanguageTag(language_tag)));

		return ccLicense;
	}

	public static String format(Instant instant) {

		if (instant == null) {
			return "BAD DATE FORMAT";
		}
		LocalDateTime lc = LocalDateTime.ofInstant(instant, ZoneId.of(static_zone_tag));
		return timeDifferenceFromNow(lc);
	}

	public static String formatLiveTime(String startTime, String duration) {

		Instant start=  Instant.parse(startTime);
		int broadcast_hour = Integer.parseInt(duration.substring(0,2));
		int broadcast_min = Integer.parseInt(duration.substring(3,5));
		Instant end = start.plus(broadcast_hour, ChronoUnit.HOURS).plus(broadcast_min,ChronoUnit.MINUTES);

		Instant now = Instant.now();
		if (start.isBefore(now) && end.isAfter(now)) {
			return "live";			// live
		}
		else if (start.isAfter(now)) {
			return "future";				//future
		}
		else {
			return "past";                    //past
		}
	}

	public static String formatInstant(String date) {

		Locale locale = Locale.forLanguageTag("el");
		LocalDate localDate = LocalDate.parse(date);
		String month_text = Month.of(localDate.getMonthValue()).getDisplayName(TextStyle.FULL_STANDALONE, locale);

		return month_text + ' ' + localDate.getDayOfMonth() + ", " + localDate.getYear();
	}

	private static String timeDifferenceFromNow(LocalDateTime then) {

		LocalDateTime toDateTime = LocalDateTime.now();

		Period period = Period.between(then.toLocalDate(), toDateTime.toLocalDate());
		Duration duration = Duration.between(then.toLocalTime(), toDateTime.toLocalTime());

		if (duration.isNegative()) {
			period = period.minusDays(1);
			duration = duration.plusDays(1);
		}
		long seconds = duration.getSeconds();
		long hours = seconds / SECONDS_PER_HOUR;
		long minutes = ((seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
		long secs = (seconds % SECONDS_PER_MINUTE);
		long[] time = {hours, minutes,secs};

		String timeDifference = "";
		if (period.getYears() >0) timeDifference += period.getYears() + " χρόνια ";
		if (period.getMonths() >0) timeDifference += period.getMonths() + " μήνες ";
		if (period.getDays() > 0) timeDifference += period.getDays() + " ημέρες ";
		if (time[0] > 0) timeDifference += + time[0] + " ώρες ";
		if (time[1] > 0) timeDifference += + time[1] + " λεπτά ";
		if (time[2] > 0) timeDifference += + time[2] + "'' ";
		return timeDifference;
	}
}
