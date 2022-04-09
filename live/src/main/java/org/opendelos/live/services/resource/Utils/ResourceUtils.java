/* 
     Author: Michael Gatzonis - 23/11/2020 
     live
*/
package org.opendelos.live.services.resource.Utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.opendelos.live.services.i18n.MultilingualServices;
import org.opendelos.model.common.CcLicense;
import org.opendelos.model.properties.MultimediaProperties;
import org.opendelos.model.properties.StreamingProperties;
import org.opendelos.model.resources.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class ResourceUtils {

	@Value("${app.language}")
	String language_tag;
	@Value("${app.zone}")
	String app_zone;

	static final int MINUTES_PER_HOUR = 60;
	static final int SECONDS_PER_MINUTE = 60;
	static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

	private final MultilingualServices multilingualServices;
	private final StreamingProperties streamingProperties;
	private final MultimediaProperties multimediaProperties;

	private static String static_zone_tag;

	@Value("${app.zone}")
	public void setZone(String zone) {
		static_zone_tag = zone;
	}

	@Autowired
	public ResourceUtils(MultilingualServices multilingualServices, StreamingProperties streamingProperties, MultimediaProperties multimediaProperties) {
		this.multilingualServices = multilingualServices;
		this.streamingProperties = streamingProperties;
		this.multimediaProperties = multimediaProperties;
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

	public String getVideoUrlOfResource(Resource resource) {

		String video_filename = resource.getResourceAccess().getFileName();
		StringBuilder stringBuilder = this.getStreamingBaseWebPath()
				.append(resource.getResourceAccess().getFolder())
				.append("/")
				.append(video_filename);
		return  stringBuilder.toString();
	}

	public String getMediaBasePathOfResource(Resource resource) {

		StringBuilder stringBuilder = this.getMultimediaBaseWebPath();
		stringBuilder.append(resource.getResourceAccess().getFolder()).append("/");
		return  stringBuilder.toString();
	}

	public CcLicense getLicenseOfResource(Resource resource) {

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

	public static String format(Instant instant) {

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
