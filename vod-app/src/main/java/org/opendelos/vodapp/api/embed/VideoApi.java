/* 
     Author: Michael Gatzonis - 11/2/2021 
     live
*/
package org.opendelos.vodapp.api.embed;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.ResourceAccess;
import org.opendelos.model.structure.Institution;
import org.opendelos.vodapp.services.resource.ResourceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoApi {

	private final ResourceService resourceService;
	@Value("${app.url}")
	String app_url;
	@Autowired
	Institution defaultInstitution;
	@Value("${streaming.protocol}")
	String streaming_protocol;
	@Value("${streaming.host}")
	String streaming_host;
	@Value("${streaming.port}")
	String streaming_port;
	@Value("${streaming.webdir}")
	String streaming_webdir;
	@Value("${streaming.support_files_webDir}")
	String support_files_webDir;

	public VideoApi(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	@RequestMapping(value = "/apiw/v1/embed/{id}", method = RequestMethod.GET)
	public String CreateEmbedCodeForVideoPlayer(@PathVariable("id") String id) {

		StringBuilder return_html = new StringBuilder();
		return_html.append("<div id='video_wrapper'>")
				.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"")
				.append(app_url).append("/js/player/embed.css\"/>")
				.append("<input id='vid' type='hidden' value='").append(id).append("'>")
				.append("<a href=\"").append(app_url).append("\" target=\"_blank\">")
				.append("<span id='title_overlay' style='position:absolute;top:10px;left:10px;order:9000;color:white;font-size:.8em' class='alpha60'></span>")
				.append("</a>")
				.append("<span id='title_overlay_1' style='position:absolute;left:10px;bottom:50px;order:10000;color:white;font-size:.8em' class='alpha10'>")
				.append(defaultInstitution.getTitle())
				.append("</span>")
				.append("<video id='player' controls preload='auto' autoplay controlsList='nodownload' width='530px'>")
				.append("<source id='mp4'  src=\"\" type='video/mp4'>")
				.append("</video>")
				.append("<script src=\"").append(app_url).append("/")
				.append("/js/player/embed.js\" type=\"application/javascript\"></script>")
				.append("<div>");

		return return_html.toString();
	}

	/** gets called from embed.js in order to created embeded content **/
	@RequestMapping(value = "/apiw/v1/emurl/{id}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<EmdedResponse> getUrlForResourceId(@PathVariable("id") String id, Locale locale) {

		DateTimeFormatter formatter =
				DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
						.withLocale(locale)
						.withZone(ZoneId.systemDefault());
		StringBuilder url_builder = new StringBuilder();


		EmdedResponse emdedResponse = new EmdedResponse();
		Resource resource = resourceService.findById(id);
		if (resource != null && resource.getResourceAccess() != null && resource.getAccessPolicy().equals("public")) {
			url_builder.append(streaming_protocol).append("://").append(streaming_host).append(":")
					.append(streaming_port).append("/").append(streaming_webdir);
			ResourceAccess resourceAccess = resource.getResourceAccess();
			if (resourceAccess.getFolder() != null && resourceAccess.getFileName() != null) {
				String url = url_builder + resourceAccess.getFolder() + "/" + resourceAccess.getFileName();


				StringBuilder title_builder = new StringBuilder();
				title_builder.append(resource.getTitle());
				if (resource.isParts()) {
					title_builder.append(" / Μέρος ").append(resource.getPartNumber());
				}
				if (resource.getType().equals("COURSE")) {
					emdedResponse.setSupervisor(resource.getSupervisor().getName() + ", " + resource.getSupervisor()
							.getAffiliation());
				}
				StringBuilder subtitle_builder = new StringBuilder();
				if (resource.getDepartment() != null) {
					subtitle_builder.append("Τμήμα ").append(resource.getDepartment().getTitle()).append(", ")
							.append(defaultInstitution.getTitle());
				}
				else {
					subtitle_builder.append(defaultInstitution.getTitle());
				}

				// DATE-TIME
				String date_output = formatter.format(resource.getDate());
				//
				emdedResponse.setTitle(title_builder.toString());
				emdedResponse.setSubtitle(subtitle_builder.toString());
				emdedResponse.setDatetime(date_output);
				emdedResponse.setUrl(url);

				return new ResponseEntity<>(emdedResponse, HttpStatus.ACCEPTED);
			}
		}

		url_builder.append(streaming_protocol).append("://").append(streaming_host).append(":").append(streaming_port)
				.append(support_files_webDir);
		String date_output = formatter.format(Instant.now());
		emdedResponse.setTitle("");
		emdedResponse.setSubtitle("");
		emdedResponse.setSupervisor("");
		emdedResponse.setDatetime(date_output);
		emdedResponse.setUrl(url_builder + "notice.mp4");
		return new ResponseEntity<>(emdedResponse, HttpStatus.BAD_REQUEST);

	}

}
