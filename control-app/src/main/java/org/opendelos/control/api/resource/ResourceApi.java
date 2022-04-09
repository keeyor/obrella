/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.control.api.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendelos.control.services.resource.ResourceService;
import org.opendelos.model.resources.Cuts;
import org.opendelos.model.resources.Presentation;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.Slide;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResourceApi {

	private final ResourceService resourceService;


	@Autowired
	public ResourceApi(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	@RequestMapping(value= "/api/v1/resource/{id}", method = RequestMethod.GET, produces =  "application/json")
	public Resource getResourceById(@PathVariable String id) {
		return resourceService.findById(id);
	}

	@RequestMapping(value= "/api/v1/resource/slides/{id}", method = RequestMethod.GET, produces =  "application/json")
	public List<Slide> getResourceSlidesById(@PathVariable String id) {
		return resourceService.getResourceSlides(id);
	}

	@RequestMapping(value= "/api/v1/resource/presentation/{id}", method = RequestMethod.GET, produces =  "application/json")
	public @ResponseBody Presentation getResourcePresentationById(@PathVariable String id) {
		return resourceService.getResourcePresentation(id);
	}
	@RequestMapping(value= "/api/v1/resource/real_edited_presentation/{id}", method = RequestMethod.GET, produces =  "application/json")
	public @ResponseBody Presentation getResourceRealEditedPresentationById(@PathVariable String id) {
		return resourceService.getResourceRealEditedPresentation(id);
	}

	@RequestMapping(value= "/api/v1/resource/delete_slide/{id}", method= RequestMethod.POST, headers = {"Accept=text/html, text/xml, application/json"})
	public @ResponseBody String deleteResourceSlideByIndex(@PathVariable String id,@RequestBody String jsonString) {

		try {
			String slideId = this.readDeleteResourceSlideByIndexJson(jsonString);
		 	resourceService.rmResourceSlide(id, slideId);
 			return "1";
		}
		catch (Exception e) {
			return "-1";
		}
	}

	@RequestMapping(value= "/api/v1/resource/update_slide_title/{id}", method= RequestMethod.POST, headers = {"Accept=text/html, text/xml, application/json"})
	public @ResponseBody String updateSlideTitle(@PathVariable String id,@RequestBody String jsonString)  {

		int position=-1;
		String title= null;
		String options = null;

		try {
			JsonFactory jfactory = new JsonFactory();
			/*** read from string ***/
			JsonParser jParser = jfactory.createParser(jsonString);
			while (jParser.nextToken() != JsonToken.END_OBJECT) {
				String fieldname = jParser.getCurrentName();
				if (fieldname !=null) {
					switch (fieldname) {
					case  "position":
						jParser.nextToken();
						position = Integer.parseInt(jParser.getText());
						break;
					case  "title":
						jParser.nextToken();
						title = jParser.getText();
						break;
					case  "options":
						jParser.nextToken();
						options = jParser.getText();
						break;
					}
				}
			}
			jParser.close();
		}
		catch (IOException e) {
			return "-1";
		}
		try {
			resourceService.updateResourceSlideTitle(id,position,title);
			return "1";
		}
		catch (Exception e) {
			return "-1";
		}
	}

	@RequestMapping(value = "/api/v1/resource/cuts/{id}" , method = RequestMethod.POST, headers = {"Accept=text/html, text/xml, application/json"})
	public @ResponseBody String setResourceCuts(@PathVariable String id, @RequestBody String jsonString) {

		String cuts;
		try {
			 this.setResourcePresentationCuts(id, jsonString);
			 cuts = "1";
		} catch (Exception e) {
			cuts = "-1";
		}
		return cuts;
	}
	@RequestMapping(value = "/api/v1/resource/slides/{id}" , method = RequestMethod.POST, headers = {"Accept=text/html, text/xml, application/json"})
	public @ResponseBody String setResourceSlidesSync(@PathVariable String id, @RequestBody String jsonString) {

		String slides;
		try {
			this.setResourcePresentationSlidesSync(id, jsonString);
			slides = "1";
		} catch (Exception e) {
			slides = e.getMessage();
		}
		return slides;
	}
	public void setResourcePresentationCuts(String rid, String jsonCuts) throws Exception  {

		String[] parts = jsonCuts.split("}");

		String[] clip_begin=null;
		String[] clip_end=null;
		String[] trim_begin=null;
		String[] trim_end=null;
		String[] real_duration=null;
		String[] initial_duration=null;

		//Carefully: Multiple JSON Strings in call
		for (String part : parts) {
			String jsonCut = part.concat("}");


			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, false);

			JsonCutPost jsonCutPost = null;
			jsonCutPost = mapper.readValue(jsonCut, JsonCutPost.class);

			if (jsonCutPost.getClipBegin() != null)
				clip_begin = jsonCutPost.getClipBegin();
			if (jsonCutPost.getClipEnd() != null)
				clip_end = jsonCutPost.getClipEnd();
			if (jsonCutPost.getTrimBegin() != null)
				trim_begin = jsonCutPost.getTrimBegin();
			if (jsonCutPost.getTrimEnd() != null)
				trim_end = jsonCutPost.getTrimEnd();
			if (jsonCutPost.getRealDuration() != null)
				real_duration = jsonCutPost.getRealDuration();
			if (jsonCutPost.getInitialDuration() != null)
				initial_duration = jsonCutPost.getInitialDuration();
		}

		Cuts cuts = new Cuts();
		Cuts.Clips clip = new Cuts.Clips();
		List<Cuts.Clips.Cut> cutList = new ArrayList<>();
		clip.setCuts(cutList);
		for (int i = 0; i< Objects.requireNonNull(clip_begin).length; i++)
		{
			Cuts.Clips.Cut cut = new Cuts.Clips.Cut();
			cut.setBegin(clip_begin[i]);
			assert clip_end != null;
			cut.setEnd(clip_end[i]);
			clip.getCuts().add(cut);
		}

		Cuts.Trims.Start start = new Cuts.Trims.Start();
		Cuts.Trims.Finish finish = new Cuts.Trims.Finish();
		Cuts.Trims trims = new Cuts.Trims();

		assert trim_begin != null;
		if (trim_begin.length != 0) {
			if (trim_begin[0].equals("00:00:00")) {
				start.setBegin(trim_begin[0]);
				assert trim_end != null;
				start.setEnd(trim_end[0]);
				trims.setStart(start);
			}
			else {
				finish.setBegin(trim_begin[0]);
				assert trim_end != null;
				finish.setEnd(trim_end[0]);
				trims.setFinish(finish);
			}
			if (trim_begin.length >1) {
				if (trim_begin[1].equals("00:00:00")) {
					start.setBegin(trim_begin[1]);
					start.setEnd(trim_end[1]);
					trims.setStart(start);
				}
				else {

					finish.setBegin(trim_begin[1]);
					finish.setEnd(trim_end[1]);
					trims.setFinish(finish);
				}
			}
		}
		cuts.setClips(clip);
		cuts.setTrims(trims);
		try {
			assert real_duration != null;
			assert initial_duration != null;
			resourceService.updateResourceCuts(rid,cuts,real_duration[0],initial_duration[0]);
		}
		catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	public void setResourcePresentationSlidesSync(String id, String jsonSlides) throws Exception {

		String[] urls;
		String[] times;
		String initial_duration;

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, false);

		JsonSlidesSyncPost jsonSlidesSyncPost = null;
		jsonSlidesSyncPost= mapper.readValue(jsonSlides, JsonSlidesSyncPost.class);

		urls = jsonSlidesSyncPost.getUrls();
		times = jsonSlidesSyncPost.getTimes();
		initial_duration = jsonSlidesSyncPost.getInitial_duration();

		Presentation presentation = resourceService.getResourcePresentation(id);

		String result_code="0";
		if (presentation !=null) {

			String realEditingResourceStatus = "0"; //TODO:_dlmService.getRealEditingResourceStatus(identity);
			if (realEditingResourceStatus.equals("DRE"))
				throw new Exception("4");
			else if (realEditingResourceStatus.equals("DARE"))
				throw new Exception("5");
			// Continue if result_code:=0
			if (result_code.equals("0")) {
				List<Slide> slides = presentation.getSlides();
				String stored_duration = presentation.getDuration();
				int stored_duration_sec = this.convertTimeToSeconds(stored_duration);
				int initial_duration_sec = this.convertTimeToSeconds(initial_duration);

				int stored_slide_number = slides.size();

				if ((stored_duration_sec != initial_duration_sec) && (stored_slide_number!= urls.length)) {
					throw new Exception("23");
				}
				else if (stored_duration_sec != initial_duration_sec)
					throw new Exception("2");
				else if (stored_slide_number!= urls.length)
					throw new Exception("3");
				else {
					//** UPDATE RESOURCE SLIDES WITH NEW VALUES*/
					Slide slide;
					int slide_index=0;
					int array_index=0;
					while (array_index < urls.length){
						slide = slides.get(slide_index);
						if (slide.getUrl().equalsIgnoreCase(urls[array_index])) {
							slide.setTime(times[array_index]);
							slide_index=slide_index+1;
							array_index=array_index+1;
						}
						else slide_index=slide_index+1;
					}
					try {
						resourceService.updateResourceSlides(id,slides);
					}
					catch (Exception e) {
						throw new Exception(e.getMessage());
					}
				}
			}
		}
	}

	private String readDeleteResourceSlideByIndexJson(String jsonString) throws IOException {

		String slideId = "";
		String options;
		JsonFactory jfactory = new JsonFactory();
		/*** read from string ***/
		JsonParser jParser = jfactory.createParser(jsonString);
		while (jParser.nextToken() != JsonToken.END_OBJECT) {
			String fieldname = jParser.getCurrentName();

			if (fieldname !=null) {
				switch (fieldname) {
				case  "slideId":
					jParser.nextToken();
					slideId = jParser.getText();
					break;
				case  "options":
					jParser.nextToken();
					options = jParser.getText();
					break;
				}
			}
		}
		jParser.close();

		return slideId;
	}

	private int convertTimeToSeconds (String time) {

		int plusSecond = 0;
		time = time.trim();

		if (time.contains(".")) {					//throw away milliseconds if < 50, else add a second
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
}
